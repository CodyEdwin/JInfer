package com.jinfer.engine;

import com.jinfer.config.GenerationConfig;
import com.jinfer.config.ModelConfig;
import com.jinfer.sampling.SamplerFactory;
import com.jinfer.sampling.SamplingStrategy;
import com.jinfer.tokenization.HuggingFaceTokenizer;
import com.jinfer.tokenization.SimpleTokenizer;
import com.jinfer.tokenization.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Main LLM inference engine implementation.
 * Handles model loading, tokenization, and autoregressive generation.
 */
public class JInferEngine implements LLMEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(JInferEngine.class);
    
    private InferenceSession session;
    private Tokenizer tokenizer;
    private ModelConfig modelConfig;
    private boolean modelLoaded = false;

    public JInferEngine() {
        // Default constructor
    }

    @Override
    public void loadModel(ModelConfig config) throws Exception {
        this.modelConfig = config;
        
        logger.info("Loading model with config: {}", config);
        
        // Load tokenizer
        loadTokenizer(config.getTokenizerPath());
        
        // Load inference session based on model format
        loadSession(config);
        
        this.modelLoaded = true;
        logger.info("Model loaded successfully");
    }

    private void loadTokenizer(Path tokenizerPath) throws Exception {
        if (tokenizerPath != null && Files.exists(tokenizerPath)) {
            Path tokenizerJson = tokenizerPath;
            if (Files.isDirectory(tokenizerPath)) {
                tokenizerJson = tokenizerPath.resolve("tokenizer.json");
            }
            
            if (Files.exists(tokenizerJson)) {
                this.tokenizer = new HuggingFaceTokenizer(tokenizerJson);
                logger.info("Loaded HuggingFace tokenizer from: {}", tokenizerJson);
                return;
            }
        }
        
        // Fallback to simple tokenizer for testing
        logger.warn("Using SimpleTokenizer as fallback");
        this.tokenizer = new SimpleTokenizer();
    }

    private void loadSession(ModelConfig config) throws Exception {
        Path modelPath = config.getModelPath();
        
        if (modelPath != null && Files.exists(modelPath)) {
            String format = config.getModelFormat().toLowerCase();
            
            if (format.equals("onnx") || modelPath.toString().endsWith(".onnx")) {
                this.session = new OnnxInferenceSession(
                    modelPath, 
                    tokenizer.getVocabSize(),
                    config.getContextLength()
                );
                logger.info("Loaded ONNX model");
                return;
            }
        }
        
        // Fallback to mock session for testing
        logger.warn("Using MockInferenceSession for testing");
        this.session = new MockInferenceSession(
            tokenizer.getVocabSize(),
            config.getContextLength()
        );
    }

    /**
     * Create engine with mock components for testing.
     */
    public static JInferEngine createMockEngine(int vocabSize, int contextLength) {
        JInferEngine engine = new JInferEngine();
        engine.tokenizer = new SimpleTokenizer();
        engine.session = new MockInferenceSession(vocabSize, contextLength);
        engine.modelConfig = ModelConfig.builder()
                .contextLength(contextLength)
                .build();
        engine.modelLoaded = true;
        return engine;
    }

    @Override
    public boolean isModelLoaded() {
        return modelLoaded;
    }

    @Override
    public String generate(String prompt, GenerationConfig config) {
        if (!modelLoaded) {
            throw new IllegalStateException("Model not loaded");
        }
        
        logger.debug("Generating with config: {}", config);
        
        // Tokenize input
        Tokenizer.EncodingResult encoding = tokenizer.encodeWithAttention(prompt);
        List<Long> inputIds = new ArrayList<>();
        for (long id : encoding.getInputIds()) {
            inputIds.add(id);
        }
        
        // Create sampler
        SamplingStrategy sampler = SamplerFactory.create(config);
        logger.debug("Using sampler: {}", sampler.getName());
        
        // Generation loop
        StringBuilder generated = new StringBuilder();
        long eosToken = tokenizer.getEosTokenId();
        int maxLength = Math.min(
            inputIds.size() + config.getMaxNewTokens(),
            session.getMaxContextLength()
        );
        
        for (int step = 0; step < config.getMaxNewTokens(); step++) {
            // Check context length
            if (inputIds.size() >= maxLength) {
                logger.debug("Reached max context length");
                break;
            }
            
            // Prepare inputs
            long[] ids = inputIds.stream().mapToLong(Long::longValue).toArray();
            long[] mask = new long[ids.length];
            Arrays.fill(mask, 1L);
            
            // Forward pass
            float[] logits = session.forward(ids, mask);
            
            // Sample next token
            int nextToken = sampler.sample(logits);
            
            // Check for EOS
            if (nextToken == eosToken) {
                logger.debug("Generated EOS token at step {}", step);
                break;
            }
            
            // Check for stop sequence
            String tokenText = tokenizer.decode(nextToken);
            generated.append(tokenText);
            
            if (config.getStopSequence() != null && 
                generated.toString().contains(config.getStopSequence())) {
                logger.debug("Hit stop sequence");
                break;
            }
            
            // Add token to context
            inputIds.add((long) nextToken);
        }
        
        return generated.toString().trim();
    }

    @Override
    public Iterator<String> generateStream(String prompt, GenerationConfig config) {
        if (!modelLoaded) {
            throw new IllegalStateException("Model not loaded");
        }
        
        return new StreamingIterator(prompt, config);
    }

    @Override
    public ModelInfo getModelInfo() {
        if (!modelLoaded) {
            return null;
        }
        
        return new ModelInfo(
            modelConfig.getModelPath() != null ? modelConfig.getModelPath().getFileName().toString() : "mock",
            modelConfig.getModelFormat(),
            tokenizer.getVocabSize(),
            modelConfig.getContextLength()
        );
    }

    @Override
    public void close() throws Exception {
        if (session != null) {
            session.close();
        }
        if (tokenizer instanceof AutoCloseable) {
            ((AutoCloseable) tokenizer).close();
        }
        modelLoaded = false;
        logger.info("Engine closed");
    }

    /**
     * Get the tokenizer for direct access.
     */
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    /**
     * Iterator for streaming token generation.
     */
    private class StreamingIterator implements Iterator<String> {
        private final GenerationConfig config;
        private final SamplingStrategy sampler;
        private final List<Long> inputIds;
        private final long eosToken;
        private final int maxLength;
        private final StringBuilder generated;
        private int step = 0;
        private boolean finished = false;
        private String nextToken = null;

        public StreamingIterator(String prompt, GenerationConfig config) {
            this.config = config;
            this.sampler = SamplerFactory.create(config);
            this.eosToken = tokenizer.getEosTokenId();
            this.generated = new StringBuilder();
            
            Tokenizer.EncodingResult encoding = tokenizer.encodeWithAttention(prompt);
            this.inputIds = new ArrayList<>();
            for (long id : encoding.getInputIds()) {
                inputIds.add(id);
            }
            
            this.maxLength = Math.min(
                inputIds.size() + config.getMaxNewTokens(),
                session.getMaxContextLength()
            );
            
            advance();
        }

        private void advance() {
            if (finished || step >= config.getMaxNewTokens() || inputIds.size() >= maxLength) {
                finished = true;
                nextToken = null;
                return;
            }
            
            long[] ids = inputIds.stream().mapToLong(Long::longValue).toArray();
            long[] mask = new long[ids.length];
            Arrays.fill(mask, 1L);
            
            float[] logits = session.forward(ids, mask);
            int tokenId = sampler.sample(logits);
            
            if (tokenId == eosToken) {
                finished = true;
                nextToken = null;
                return;
            }
            
            String tokenText = tokenizer.decode(tokenId);
            generated.append(tokenText);
            
            if (config.getStopSequence() != null && 
                generated.toString().contains(config.getStopSequence())) {
                finished = true;
                nextToken = null;
                return;
            }
            
            inputIds.add((long) tokenId);
            step++;
            nextToken = tokenText;
        }

        @Override
        public boolean hasNext() {
            return nextToken != null;
        }

        @Override
        public String next() {
            if (nextToken == null) {
                throw new NoSuchElementException();
            }
            String result = nextToken;
            advance();
            return result;
        }
    }
}

package com.jinfer.engine;

import com.jinfer.config.GenerationConfig;
import com.jinfer.config.ModelConfig;

import java.util.Iterator;

/**
 * Interface for LLM inference engine.
 */
public interface LLMEngine extends AutoCloseable {
    
    /**
     * Load a model with the given configuration.
     */
    void loadModel(ModelConfig config) throws Exception;
    
    /**
     * Check if a model is loaded.
     */
    boolean isModelLoaded();
    
    /**
     * Generate text from a prompt.
     *
     * @param prompt The input prompt
     * @param config Generation configuration
     * @return The generated text
     */
    String generate(String prompt, GenerationConfig config);
    
    /**
     * Generate text with streaming output.
     *
     * @param prompt The input prompt
     * @param config Generation configuration
     * @return Iterator of generated tokens
     */
    Iterator<String> generateStream(String prompt, GenerationConfig config);
    
    /**
     * Get information about the loaded model.
     */
    ModelInfo getModelInfo();
    
    /**
     * Model information record.
     */
    class ModelInfo {
        private final String name;
        private final String format;
        private final int vocabSize;
        private final int contextLength;
        
        public ModelInfo(String name, String format, int vocabSize, int contextLength) {
            this.name = name;
            this.format = format;
            this.vocabSize = vocabSize;
            this.contextLength = contextLength;
        }
        
        public String getName() { return name; }
        public String getFormat() { return format; }
        public int getVocabSize() { return vocabSize; }
        public int getContextLength() { return contextLength; }
        
        @Override
        public String toString() {
            return "ModelInfo{name='" + name + "', format='" + format + 
                   "', vocabSize=" + vocabSize + ", contextLength=" + contextLength + '}';
        }
    }
}

package com.jinfer.tokenization;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * HuggingFace tokenizer implementation using DJL.
 */
public class HuggingFaceTokenizer implements Tokenizer, AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceTokenizer.class);
    
    private final ai.djl.huggingface.tokenizers.HuggingFaceTokenizer tokenizer;
    private long eosTokenId = 2;  // Default EOS
    private long padTokenId = 0;  // Default PAD

    public HuggingFaceTokenizer(Path tokenizerPath) throws IOException {
        logger.info("Loading tokenizer from: {}", tokenizerPath);
        
        this.tokenizer = ai.djl.huggingface.tokenizers.HuggingFaceTokenizer.newInstance(tokenizerPath);
        
        logger.info("Tokenizer loaded successfully");
    }

    public HuggingFaceTokenizer(Path tokenizerPath, Map<String, Long> specialTokens) throws IOException {
        this(tokenizerPath);
        
        if (specialTokens != null) {
            if (specialTokens.containsKey("eos_token_id")) {
                this.eosTokenId = specialTokens.get("eos_token_id");
            }
            if (specialTokens.containsKey("pad_token_id")) {
                this.padTokenId = specialTokens.get("pad_token_id");
            }
        }
    }

    @Override
    public long[] encode(String text) {
        Encoding encoding = tokenizer.encode(text);
        return encoding.getIds();
    }

    @Override
    public EncodingResult encodeWithAttention(String text) {
        Encoding encoding = tokenizer.encode(text);
        return new EncodingResult(encoding.getIds(), encoding.getAttentionMask());
    }

    @Override
    public String decode(long[] tokenIds) {
        return tokenizer.decode(tokenIds);
    }

    @Override
    public String decode(long tokenId) {
        return tokenizer.decode(new long[]{tokenId});
    }

    @Override
    public int getVocabSize() {
        // DJL tokenizer doesn't expose vocab size directly
        // Return a common default, actual models will have their own
        return 32000;
    }

    @Override
    public long getEosTokenId() {
        return eosTokenId;
    }

    @Override
    public long getPadTokenId() {
        return padTokenId;
    }

    public void setEosTokenId(long eosTokenId) {
        this.eosTokenId = eosTokenId;
    }

    public void setPadTokenId(long padTokenId) {
        this.padTokenId = padTokenId;
    }

    @Override
    public void close() {
        // DJL tokenizer cleanup if needed
    }
}

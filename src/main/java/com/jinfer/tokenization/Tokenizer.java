package com.jinfer.tokenization;

/**
 * Interface for tokenization operations.
 */
public interface Tokenizer {
    
    /**
     * Encode text to token IDs.
     */
    long[] encode(String text);
    
    /**
     * Encode text with attention mask.
     */
    EncodingResult encodeWithAttention(String text);
    
    /**
     * Decode token IDs to text.
     */
    String decode(long[] tokenIds);
    
    /**
     * Decode a single token ID to text.
     */
    String decode(long tokenId);
    
    /**
     * Get the vocabulary size.
     */
    int getVocabSize();
    
    /**
     * Get the end-of-sequence token ID.
     */
    long getEosTokenId();
    
    /**
     * Get the padding token ID.
     */
    long getPadTokenId();
    
    /**
     * Result of encoding with attention mask.
     */
    class EncodingResult {
        private final long[] inputIds;
        private final long[] attentionMask;
        
        public EncodingResult(long[] inputIds, long[] attentionMask) {
            this.inputIds = inputIds;
            this.attentionMask = attentionMask;
        }
        
        public long[] getInputIds() { return inputIds; }
        public long[] getAttentionMask() { return attentionMask; }
    }
}

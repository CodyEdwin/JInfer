package com.jinfer.engine;

/**
 * Represents an inference session with a loaded model.
 */
public interface InferenceSession extends AutoCloseable {
    
    /**
     * Run forward pass and get logits for next token prediction.
     *
     * @param inputIds Array of input token IDs
     * @param attentionMask Attention mask (1 for real tokens, 0 for padding)
     * @return Logits array for the vocabulary
     */
    float[] forward(long[] inputIds, long[] attentionMask);
    
    /**
     * Get the vocabulary size.
     */
    int getVocabSize();
    
    /**
     * Get the maximum context length.
     */
    int getMaxContextLength();
}

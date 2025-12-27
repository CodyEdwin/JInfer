package com.jinfer.sampling;

/**
 * Interface for token sampling strategies.
 */
public interface SamplingStrategy {
    
    /**
     * Sample the next token from logits.
     *
     * @param logits The logits array from model output
     * @return The selected token ID
     */
    int sample(float[] logits);
    
    /**
     * Get the name of this sampling strategy.
     */
    String getName();
}

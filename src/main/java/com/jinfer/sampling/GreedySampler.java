package com.jinfer.sampling;

/**
 * Greedy sampling - always selects the token with highest probability.
 * Deterministic output.
 */
public class GreedySampler implements SamplingStrategy {

    @Override
    public int sample(float[] logits) {
        int maxIndex = 0;
        float maxValue = logits[0];
        
        for (int i = 1; i < logits.length; i++) {
            if (logits[i] > maxValue) {
                maxValue = logits[i];
                maxIndex = i;
            }
        }
        
        return maxIndex;
    }

    @Override
    public String getName() {
        return "greedy";
    }
}

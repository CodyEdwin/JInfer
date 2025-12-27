package com.jinfer.sampling;

import java.util.Arrays;
import java.util.Random;

/**
 * Top-K sampling - samples from the K most probable tokens.
 */
public class TopKSampler implements SamplingStrategy {
    
    private final int k;
    private final float temperature;
    private final Random random;

    public TopKSampler(int k, float temperature) {
        this(k, temperature, new Random());
    }

    public TopKSampler(int k, float temperature, long seed) {
        this(k, temperature, new Random(seed));
    }

    public TopKSampler(int k, float temperature, Random random) {
        if (k <= 0) {
            throw new IllegalArgumentException("K must be positive");
        }
        this.k = k;
        this.temperature = temperature;
        this.random = random;
    }

    @Override
    public int sample(float[] logits) {
        int vocabSize = logits.length;
        int effectiveK = Math.min(k, vocabSize);
        
        // Create indexed array for sorting
        Integer[] indices = new Integer[vocabSize];
        for (int i = 0; i < vocabSize; i++) {
            indices[i] = i;
        }
        
        // Sort by logit value descending
        final float[] finalLogits = logits;
        Arrays.sort(indices, (a, b) -> Float.compare(finalLogits[b], finalLogits[a]));
        
        // Take top-k and apply temperature
        float[] topKLogits = new float[effectiveK];
        for (int i = 0; i < effectiveK; i++) {
            topKLogits[i] = logits[indices[i]] / temperature;
        }
        
        // Compute softmax over top-k
        float[] probs = softmax(topKLogits);
        
        // Sample from top-k distribution
        int sampledIdx = sampleFromDistribution(probs);
        
        return indices[sampledIdx];
    }

    private float[] softmax(float[] logits) {
        float maxLogit = Float.NEGATIVE_INFINITY;
        for (float logit : logits) {
            maxLogit = Math.max(maxLogit, logit);
        }
        
        float sumExp = 0.0f;
        float[] probs = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            probs[i] = (float) Math.exp(logits[i] - maxLogit);
            sumExp += probs[i];
        }
        
        for (int i = 0; i < probs.length; i++) {
            probs[i] /= sumExp;
        }
        
        return probs;
    }

    private int sampleFromDistribution(float[] probs) {
        float r = random.nextFloat();
        float cumulative = 0.0f;
        
        for (int i = 0; i < probs.length; i++) {
            cumulative += probs[i];
            if (r <= cumulative) {
                return i;
            }
        }
        
        return probs.length - 1;
    }

    @Override
    public String getName() {
        return "top_k(" + k + ", temp=" + temperature + ")";
    }
}

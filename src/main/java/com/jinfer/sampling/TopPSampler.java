package com.jinfer.sampling;

import java.util.Arrays;
import java.util.Random;

/**
 * Top-P (Nucleus) sampling - samples from smallest set of tokens 
 * whose cumulative probability exceeds P.
 */
public class TopPSampler implements SamplingStrategy {
    
    private final float p;
    private final float temperature;
    private final Random random;

    public TopPSampler(float p, float temperature) {
        this(p, temperature, new Random());
    }

    public TopPSampler(float p, float temperature, long seed) {
        this(p, temperature, new Random(seed));
    }

    public TopPSampler(float p, float temperature, Random random) {
        if (p <= 0 || p > 1) {
            throw new IllegalArgumentException("P must be in (0, 1]");
        }
        this.p = p;
        this.temperature = temperature;
        this.random = random;
    }

    @Override
    public int sample(float[] logits) {
        int vocabSize = logits.length;
        
        // Apply temperature
        float[] scaledLogits = new float[vocabSize];
        for (int i = 0; i < vocabSize; i++) {
            scaledLogits[i] = logits[i] / temperature;
        }
        
        // Compute softmax probabilities
        float[] probs = softmax(scaledLogits);
        
        // Create indexed array for sorting
        Integer[] indices = new Integer[vocabSize];
        for (int i = 0; i < vocabSize; i++) {
            indices[i] = i;
        }
        
        // Sort by probability descending
        final float[] finalProbs = probs;
        Arrays.sort(indices, (a, b) -> Float.compare(finalProbs[b], finalProbs[a]));
        
        // Find nucleus (smallest set with cumulative prob >= p)
        float cumulative = 0.0f;
        int nucleusSize = 0;
        for (int i = 0; i < vocabSize; i++) {
            cumulative += probs[indices[i]];
            nucleusSize++;
            if (cumulative >= p) {
                break;
            }
        }
        
        // Normalize probabilities within nucleus
        float[] nucleusProbs = new float[nucleusSize];
        float nucleusSum = 0.0f;
        for (int i = 0; i < nucleusSize; i++) {
            nucleusProbs[i] = probs[indices[i]];
            nucleusSum += nucleusProbs[i];
        }
        for (int i = 0; i < nucleusSize; i++) {
            nucleusProbs[i] /= nucleusSum;
        }
        
        // Sample from nucleus
        int sampledIdx = sampleFromDistribution(nucleusProbs);
        
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
        return "top_p(" + p + ", temp=" + temperature + ")";
    }
}

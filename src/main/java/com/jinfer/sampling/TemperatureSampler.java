package com.jinfer.sampling;

import java.util.Random;

/**
 * Temperature-based sampling with softmax.
 * Higher temperature = more random, lower = more deterministic.
 */
public class TemperatureSampler implements SamplingStrategy {
    
    private final float temperature;
    private final Random random;

    public TemperatureSampler(float temperature) {
        this(temperature, new Random());
    }

    public TemperatureSampler(float temperature, long seed) {
        this(temperature, new Random(seed));
    }

    public TemperatureSampler(float temperature, Random random) {
        if (temperature <= 0) {
            throw new IllegalArgumentException("Temperature must be positive");
        }
        this.temperature = temperature;
        this.random = random;
    }

    @Override
    public int sample(float[] logits) {
        // Apply temperature scaling
        float[] scaledLogits = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            scaledLogits[i] = logits[i] / temperature;
        }
        
        // Compute softmax probabilities
        float[] probs = softmax(scaledLogits);
        
        // Sample from distribution
        return sampleFromDistribution(probs);
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
        return "temperature(" + temperature + ")";
    }
}

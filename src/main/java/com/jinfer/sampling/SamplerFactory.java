package com.jinfer.sampling;

import com.jinfer.config.GenerationConfig;

import java.util.Random;

/**
 * Factory for creating sampling strategies from generation config.
 */
public class SamplerFactory {

    /**
     * Create a sampler based on generation configuration.
     */
    public static SamplingStrategy create(GenerationConfig config) {
        Random random = config.getSeed() >= 0 
            ? new Random(config.getSeed()) 
            : new Random();

        // If temperature is very low or sampling disabled, use greedy
        if (!config.isDoSample() || config.getTemperature() < 0.01f) {
            return new GreedySampler();
        }

        // Use Top-P if specified
        if (config.getTopP() < 1.0f && config.getTopP() > 0.0f) {
            return new TopPSampler(config.getTopP(), config.getTemperature(), random);
        }

        // Use Top-K if specified
        if (config.getTopK() > 0 && config.getTopK() < Integer.MAX_VALUE) {
            return new TopKSampler(config.getTopK(), config.getTemperature(), random);
        }

        // Default to temperature sampling
        return new TemperatureSampler(config.getTemperature(), random);
    }
}

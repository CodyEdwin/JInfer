package com.jinfer.sampling;

import com.jinfer.config.GenerationConfig;
import org.junit.Test;
import static org.junit.Assert.*;

public class SamplerFactoryTest {

    @Test
    public void testGreedyWhenDoSampleFalse() {
        GenerationConfig config = GenerationConfig.builder()
                .doSample(false)
                .temperature(1.0f)
                .build();

        SamplingStrategy sampler = SamplerFactory.create(config);
        
        assertTrue(sampler instanceof GreedySampler);
    }

    @Test
    public void testGreedyWhenTemperatureNearZero() {
        GenerationConfig config = GenerationConfig.builder()
                .doSample(true)
                .temperature(0.001f)
                .build();

        SamplingStrategy sampler = SamplerFactory.create(config);
        
        assertTrue(sampler instanceof GreedySampler);
    }

    @Test
    public void testTopPWhenSpecified() {
        GenerationConfig config = GenerationConfig.builder()
                .doSample(true)
                .temperature(1.0f)
                .topP(0.9f)
                .topK(0)
                .build();

        SamplingStrategy sampler = SamplerFactory.create(config);
        
        assertTrue(sampler instanceof TopPSampler);
    }

    @Test
    public void testTopKWhenSpecified() {
        GenerationConfig config = GenerationConfig.builder()
                .doSample(true)
                .temperature(1.0f)
                .topP(1.0f)  // Disabled
                .topK(50)
                .build();

        SamplingStrategy sampler = SamplerFactory.create(config);
        
        assertTrue(sampler instanceof TopKSampler);
    }

    @Test
    public void testTemperatureSamplerAsFallback() {
        GenerationConfig config = GenerationConfig.builder()
                .doSample(true)
                .temperature(0.8f)
                .topP(1.0f)  // Disabled
                .topK(Integer.MAX_VALUE)  // Disabled
                .build();

        SamplingStrategy sampler = SamplerFactory.create(config);
        
        assertTrue(sampler instanceof TemperatureSampler);
    }

    @Test
    public void testSeedIsUsed() {
        GenerationConfig config = GenerationConfig.builder()
                .doSample(true)
                .temperature(1.0f)
                .seed(42)
                .build();

        SamplingStrategy sampler1 = SamplerFactory.create(config);
        SamplingStrategy sampler2 = SamplerFactory.create(config);
        
        float[] logits = {1.0f, 2.0f, 3.0f, 4.0f};
        
        // Same seed should give same results
        assertEquals(sampler1.sample(logits), sampler2.sample(logits));
    }
}

package com.jinfer.sampling;

import org.junit.Test;
import static org.junit.Assert.*;

public class TemperatureSamplerTest {

    @Test
    public void testLowTemperatureIsNearlyDeterministic() {
        TemperatureSampler sampler = new TemperatureSampler(0.01f, 42L);
        
        float[] logits = {1.0f, 10.0f, 2.0f, 3.0f};
        
        // With very low temperature, should almost always pick the max
        int maxIndex = 1;
        int countMax = 0;
        
        for (int i = 0; i < 100; i++) {
            if (sampler.sample(logits) == maxIndex) {
                countMax++;
            }
        }
        
        // Should pick max at least 95% of the time
        assertTrue(countMax >= 95);
    }

    @Test
    public void testHighTemperatureIncreasesVariance() {
        TemperatureSampler sampler = new TemperatureSampler(2.0f, 42L);
        
        float[] logits = {1.0f, 2.0f, 1.5f, 1.8f};
        
        int[] counts = new int[4];
        
        for (int i = 0; i < 1000; i++) {
            counts[sampler.sample(logits)]++;
        }
        
        // With high temperature, all tokens should be sampled sometimes
        for (int count : counts) {
            assertTrue("Expected some samples for all tokens", count > 10);
        }
    }

    @Test
    public void testSeedReproducibility() {
        float[] logits = {1.0f, 2.0f, 3.0f, 4.0f};
        
        TemperatureSampler sampler1 = new TemperatureSampler(1.0f, 12345L);
        TemperatureSampler sampler2 = new TemperatureSampler(1.0f, 12345L);
        
        for (int i = 0; i < 10; i++) {
            assertEquals(sampler1.sample(logits), sampler2.sample(logits));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroTemperatureThrows() {
        new TemperatureSampler(0.0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeTemperatureThrows() {
        new TemperatureSampler(-1.0f);
    }

    @Test
    public void testGetName() {
        TemperatureSampler sampler = new TemperatureSampler(0.7f);
        assertTrue(sampler.getName().contains("0.7"));
    }
}

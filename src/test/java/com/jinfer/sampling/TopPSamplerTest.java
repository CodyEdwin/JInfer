package com.jinfer.sampling;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class TopPSamplerTest {

    @Test
    public void testNucleusSampling() {
        TopPSampler sampler = new TopPSampler(0.5f, 1.0f, 42L);
        
        // Create logits where top tokens have > 50% cumulative probability
        float[] logits = {1.0f, 10.0f, 1.0f, 1.0f, 1.0f};
        
        Set<Integer> selected = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            selected.add(sampler.sample(logits));
        }
        
        // Index 1 has highest probability and should dominate
        assertTrue(selected.contains(1));
    }

    @Test
    public void testP1SamplesAllTokens() {
        TopPSampler sampler = new TopPSampler(1.0f, 1.0f, 42L);
        
        float[] logits = {2.0f, 2.0f, 2.0f, 2.0f};
        
        Set<Integer> selected = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            selected.add(sampler.sample(logits));
        }
        
        // With p=1, should sample from all tokens
        assertEquals(4, selected.size());
    }

    @Test
    public void testLowPFocusesOnTop() {
        TopPSampler sampler = new TopPSampler(0.1f, 0.5f, 42L);
        
        // Strong preference for index 2
        float[] logits = {1.0f, 1.0f, 20.0f, 1.0f, 1.0f};
        
        int countTop = 0;
        for (int i = 0; i < 100; i++) {
            if (sampler.sample(logits) == 2) {
                countTop++;
            }
        }
        
        // Should almost always pick the top token
        assertTrue(countTop > 90);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPZeroThrows() {
        new TopPSampler(0.0f, 1.0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPNegativeThrows() {
        new TopPSampler(-0.1f, 1.0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPGreaterThan1Throws() {
        new TopPSampler(1.1f, 1.0f);
    }

    @Test
    public void testGetName() {
        TopPSampler sampler = new TopPSampler(0.9f, 0.7f);
        String name = sampler.getName();
        assertTrue(name.contains("0.9"));
        assertTrue(name.contains("0.7"));
    }
}

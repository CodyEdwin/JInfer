package com.jinfer.sampling;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class TopKSamplerTest {

    @Test
    public void testOnlyTopKSelected() {
        TopKSampler sampler = new TopKSampler(2, 1.0f, 42L);
        
        // Logits: index 3 and 1 are highest
        float[] logits = {1.0f, 5.0f, 2.0f, 10.0f, 0.5f};
        
        Set<Integer> selected = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            selected.add(sampler.sample(logits));
        }
        
        // Should only select from top-2: indices 1 and 3
        assertTrue(selected.contains(3) || selected.contains(1));
        assertFalse(selected.contains(0));
        assertFalse(selected.contains(2));
        assertFalse(selected.contains(4));
    }

    @Test
    public void testK1IsDeterministic() {
        TopKSampler sampler = new TopKSampler(1, 1.0f, 42L);
        
        float[] logits = {1.0f, 5.0f, 2.0f, 3.0f};
        
        // K=1 should always select the maximum
        for (int i = 0; i < 10; i++) {
            assertEquals(1, sampler.sample(logits));
        }
    }

    @Test
    public void testKLargerThanVocab() {
        TopKSampler sampler = new TopKSampler(100, 1.0f, 42L);
        
        float[] logits = {1.0f, 2.0f, 3.0f};
        
        // Should not crash, effectively samples from all
        Set<Integer> selected = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            selected.add(sampler.sample(logits));
        }
        
        // With enough samples, should hit all tokens
        assertEquals(3, selected.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKZeroThrows() {
        new TopKSampler(0, 1.0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKNegativeThrows() {
        new TopKSampler(-1, 1.0f);
    }

    @Test
    public void testGetName() {
        TopKSampler sampler = new TopKSampler(50, 0.8f);
        String name = sampler.getName();
        assertTrue(name.contains("50"));
        assertTrue(name.contains("0.8"));
    }
}

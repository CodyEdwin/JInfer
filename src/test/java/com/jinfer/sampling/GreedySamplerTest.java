package com.jinfer.sampling;

import org.junit.Test;
import static org.junit.Assert.*;

public class GreedySamplerTest {

    @Test
    public void testSelectsMaximum() {
        GreedySampler sampler = new GreedySampler();
        
        float[] logits = {1.0f, 5.0f, 2.0f, 3.0f};
        
        int selected = sampler.sample(logits);
        
        assertEquals(1, selected);
    }

    @Test
    public void testSelectsFirstMaxWhenTied() {
        GreedySampler sampler = new GreedySampler();
        
        float[] logits = {5.0f, 5.0f, 3.0f};
        
        int selected = sampler.sample(logits);
        
        assertEquals(0, selected);
    }

    @Test
    public void testNegativeLogits() {
        GreedySampler sampler = new GreedySampler();
        
        float[] logits = {-3.0f, -1.0f, -2.0f, -5.0f};
        
        int selected = sampler.sample(logits);
        
        assertEquals(1, selected);
    }

    @Test
    public void testSingleElement() {
        GreedySampler sampler = new GreedySampler();
        
        float[] logits = {42.0f};
        
        int selected = sampler.sample(logits);
        
        assertEquals(0, selected);
    }

    @Test
    public void testDeterministic() {
        GreedySampler sampler = new GreedySampler();
        
        float[] logits = {1.0f, 3.0f, 2.0f, 4.0f, 0.5f};
        
        // Should always return same result
        for (int i = 0; i < 10; i++) {
            assertEquals(3, sampler.sample(logits));
        }
    }

    @Test
    public void testGetName() {
        GreedySampler sampler = new GreedySampler();
        assertEquals("greedy", sampler.getName());
    }
}

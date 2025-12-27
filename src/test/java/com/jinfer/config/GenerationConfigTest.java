package com.jinfer.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class GenerationConfigTest {

    @Test
    public void testDefaultValues() {
        GenerationConfig config = new GenerationConfig();
        
        assertEquals(256, config.getMaxNewTokens());
        assertEquals(1.0f, config.getTemperature(), 0.001f);
        assertEquals(0.9f, config.getTopP(), 0.001f);
        assertEquals(50, config.getTopK());
        assertNull(config.getStopSequence());
        assertTrue(config.isDoSample());
        assertEquals(-1, config.getSeed());
    }

    @Test
    public void testBuilder() {
        GenerationConfig config = GenerationConfig.builder()
                .maxNewTokens(128)
                .temperature(0.7f)
                .topP(0.95f)
                .topK(40)
                .stopSequence("\n")
                .doSample(true)
                .seed(42)
                .build();

        assertEquals(128, config.getMaxNewTokens());
        assertEquals(0.7f, config.getTemperature(), 0.001f);
        assertEquals(0.95f, config.getTopP(), 0.001f);
        assertEquals(40, config.getTopK());
        assertEquals("\n", config.getStopSequence());
        assertTrue(config.isDoSample());
        assertEquals(42, config.getSeed());
    }

    @Test
    public void testSetters() {
        GenerationConfig config = new GenerationConfig();
        
        config.setMaxNewTokens(512);
        config.setTemperature(0.5f);
        config.setTopP(0.8f);
        config.setTopK(100);
        config.setStopSequence("END");
        config.setDoSample(false);
        config.setSeed(123);

        assertEquals(512, config.getMaxNewTokens());
        assertEquals(0.5f, config.getTemperature(), 0.001f);
        assertEquals(0.8f, config.getTopP(), 0.001f);
        assertEquals(100, config.getTopK());
        assertEquals("END", config.getStopSequence());
        assertFalse(config.isDoSample());
        assertEquals(123, config.getSeed());
    }

    @Test
    public void testToString() {
        GenerationConfig config = GenerationConfig.builder()
                .maxNewTokens(100)
                .temperature(0.8f)
                .build();

        String str = config.toString();
        assertTrue(str.contains("maxNewTokens=100"));
        assertTrue(str.contains("temperature=0.8"));
    }
}

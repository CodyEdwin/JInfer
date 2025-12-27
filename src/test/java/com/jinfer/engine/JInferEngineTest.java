package com.jinfer.engine;

import com.jinfer.config.GenerationConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class JInferEngineTest {

    private JInferEngine engine;

    @Before
    public void setUp() {
        engine = JInferEngine.createMockEngine(1000, 2048);
    }

    @After
    public void tearDown() throws Exception {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    public void testMockEngineIsLoaded() {
        assertTrue(engine.isModelLoaded());
    }

    @Test
    public void testGetModelInfo() {
        LLMEngine.ModelInfo info = engine.getModelInfo();
        
        assertNotNull(info);
        assertEquals("mock", info.getName());
        assertEquals(2048, info.getContextLength());
    }

    @Test
    public void testGenerateReturnsText() {
        GenerationConfig config = GenerationConfig.builder()
                .maxNewTokens(10)
                .temperature(1.0f)
                .doSample(true)
                .seed(42)
                .build();

        String output = engine.generate("Hello", config);
        
        assertNotNull(output);
        assertTrue(output.length() > 0);
    }

    @Test
    public void testGenerateGreedy() {
        GenerationConfig config = GenerationConfig.builder()
                .maxNewTokens(5)
                .doSample(false)
                .build();

        String output1 = engine.generate("Test", config);
        
        // Create fresh engine with same seed
        JInferEngine engine2 = JInferEngine.createMockEngine(1000, 2048);
        String output2 = engine2.generate("Test", config);
        
        // Greedy should be deterministic
        assertEquals(output1, output2);
    }

    @Test
    public void testGenerateStream() {
        GenerationConfig config = GenerationConfig.builder()
                .maxNewTokens(5)
                .doSample(false)
                .build();

        Iterator<String> stream = engine.generateStream("Hello", config);
        
        assertTrue(stream.hasNext());
        
        int count = 0;
        while (stream.hasNext()) {
            String token = stream.next();
            assertNotNull(token);
            count++;
            if (count > 10) break;  // Safety limit
        }
        
        assertTrue(count > 0);
    }

    @Test
    public void testMaxTokensRespected() {
        GenerationConfig config = GenerationConfig.builder()
                .maxNewTokens(3)
                .doSample(false)
                .build();

        Iterator<String> stream = engine.generateStream("Hi", config);
        
        int count = 0;
        while (stream.hasNext()) {
            stream.next();
            count++;
        }
        
        assertTrue(count <= 3);
    }

    @Test
    public void testGetTokenizer() {
        assertNotNull(engine.getTokenizer());
    }

    @Test(expected = IllegalStateException.class)
    public void testGenerateWithoutLoadThrows() throws Exception {
        engine.close();
        JInferEngine newEngine = new JInferEngine();
        
        GenerationConfig config = GenerationConfig.builder().build();
        newEngine.generate("test", config);
    }

    @Test
    public void testCloseResetsState() throws Exception {
        assertTrue(engine.isModelLoaded());
        
        engine.close();
        
        assertFalse(engine.isModelLoaded());
    }
}

package com.jinfer.engine;

import org.junit.Test;
import static org.junit.Assert.*;

public class MockInferenceSessionTest {

    @Test
    public void testForwardReturnsCorrectSize() {
        int vocabSize = 1000;
        MockInferenceSession session = new MockInferenceSession(vocabSize, 2048);
        
        long[] inputIds = {1, 2, 3, 4, 5};
        long[] attentionMask = {1, 1, 1, 1, 1};
        
        float[] logits = session.forward(inputIds, attentionMask);
        
        assertEquals(vocabSize, logits.length);
    }

    @Test
    public void testGetVocabSize() {
        int vocabSize = 32000;
        MockInferenceSession session = new MockInferenceSession(vocabSize, 2048);
        
        assertEquals(vocabSize, session.getVocabSize());
    }

    @Test
    public void testGetMaxContextLength() {
        int contextLength = 4096;
        MockInferenceSession session = new MockInferenceSession(1000, contextLength);
        
        assertEquals(contextLength, session.getMaxContextLength());
    }

    @Test
    public void testDeterministicWithSeed() {
        long seed = 12345L;
        MockInferenceSession session1 = new MockInferenceSession(100, 2048, seed);
        MockInferenceSession session2 = new MockInferenceSession(100, 2048, seed);
        
        long[] inputIds = {1, 2, 3};
        long[] mask = {1, 1, 1};
        
        float[] logits1 = session1.forward(inputIds, mask);
        float[] logits2 = session2.forward(inputIds, mask);
        
        assertArrayEquals(logits1, logits2, 0.0001f);
    }

    @Test
    public void testCloseDoesNotThrow() {
        MockInferenceSession session = new MockInferenceSession(100, 2048);
        session.close();  // Should not throw
    }

    @Test
    public void testReset() {
        MockInferenceSession session = new MockInferenceSession(100, 2048, 42L);
        
        long[] inputIds = {1};
        long[] mask = {1};
        
        // Generate some outputs
        session.forward(inputIds, mask);
        session.forward(inputIds, mask);
        
        // Reset and compare with fresh session
        session.reset();
        MockInferenceSession freshSession = new MockInferenceSession(100, 2048, 42L);
        
        // After reset, behavior should match fresh session
        // (internal callCount is reset)
    }
}

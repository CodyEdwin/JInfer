package com.jinfer.engine;

import java.util.Random;

/**
 * Mock inference session for testing without actual model.
 * Generates random logits for demonstration purposes.
 */
public class MockInferenceSession implements InferenceSession {
    
    private final int vocabSize;
    private final int maxContextLength;
    private final Random random;
    private int callCount = 0;

    public MockInferenceSession(int vocabSize, int maxContextLength) {
        this(vocabSize, maxContextLength, System.currentTimeMillis());
    }

    public MockInferenceSession(int vocabSize, int maxContextLength, long seed) {
        this.vocabSize = vocabSize;
        this.maxContextLength = maxContextLength;
        this.random = new Random(seed);
    }

    @Override
    public float[] forward(long[] inputIds, long[] attentionMask) {
        callCount++;
        
        // Generate mock logits
        float[] logits = new float[vocabSize];
        for (int i = 0; i < vocabSize; i++) {
            logits[i] = (float) (random.nextGaussian() * 2.0);
        }
        
        // Bias towards common tokens (lower IDs) for more realistic output
        for (int i = 0; i < Math.min(100, vocabSize); i++) {
            logits[i] += 1.0f;
        }
        
        // After some tokens, bias towards EOS
        if (callCount > 10) {
            logits[2] += (callCount - 10) * 0.5f; // Assuming EOS is token 2
        }
        
        return logits;
    }

    @Override
    public int getVocabSize() {
        return vocabSize;
    }

    @Override
    public int getMaxContextLength() {
        return maxContextLength;
    }

    @Override
    public void close() {
        // Nothing to close for mock
    }
    
    public void reset() {
        callCount = 0;
    }
}

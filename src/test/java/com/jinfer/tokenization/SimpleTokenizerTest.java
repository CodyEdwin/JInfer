package com.jinfer.tokenization;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleTokenizerTest {

    private SimpleTokenizer tokenizer;

    @Before
    public void setUp() {
        tokenizer = new SimpleTokenizer();
    }

    @Test
    public void testEncodeDecodeRoundTrip() {
        String text = "hello world";
        
        long[] ids = tokenizer.encode(text);
        String decoded = tokenizer.decode(ids);
        
        assertEquals(text, decoded);
    }

    @Test
    public void testSpecialTokensExist() {
        assertTrue(tokenizer.getVocabSize() >= 4);
        assertTrue(tokenizer.getEosTokenId() >= 0);
        assertTrue(tokenizer.getPadTokenId() >= 0);
    }

    @Test
    public void testEncodeWithAttention() {
        String text = "test input";
        
        Tokenizer.EncodingResult result = tokenizer.encodeWithAttention(text);
        
        assertNotNull(result.getInputIds());
        assertNotNull(result.getAttentionMask());
        assertEquals(result.getInputIds().length, result.getAttentionMask().length);
        
        // All attention mask values should be 1
        for (long mask : result.getAttentionMask()) {
            assertEquals(1L, mask);
        }
    }

    @Test
    public void testVocabGrows() {
        int initialSize = tokenizer.getVocabSize();
        
        tokenizer.encode("unique_word_xyz");
        
        assertTrue(tokenizer.getVocabSize() > initialSize);
    }

    @Test
    public void testDecodeSingleToken() {
        long[] ids = tokenizer.encode("test");
        
        String decoded = tokenizer.decode(ids[0]);
        
        assertEquals("test", decoded);
    }

    @Test
    public void testEmptyString() {
        long[] ids = tokenizer.encode("");
        
        assertEquals(0, ids.length);
    }

    @Test
    public void testMultipleWords() {
        String text = "the quick brown fox";
        
        long[] ids = tokenizer.encode(text);
        
        assertEquals(4, ids.length);
    }
}

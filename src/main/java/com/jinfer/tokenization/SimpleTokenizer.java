package com.jinfer.tokenization;

import java.util.*;

/**
 * Simple word-based tokenizer for testing and fallback.
 * Not suitable for production LLM use.
 */
public class SimpleTokenizer implements Tokenizer {
    
    private final Map<String, Long> vocab;
    private final Map<Long, String> reverseVocab;
    private long nextId = 0;
    private long eosTokenId;
    private long padTokenId;
    private long unkTokenId;

    public SimpleTokenizer() {
        this.vocab = new HashMap<>();
        this.reverseVocab = new HashMap<>();
        
        // Add special tokens
        this.padTokenId = addToken("<pad>");
        this.unkTokenId = addToken("<unk>");
        this.eosTokenId = addToken("<eos>");
        addToken("<bos>");
    }

    private long addToken(String token) {
        if (!vocab.containsKey(token)) {
            vocab.put(token, nextId);
            reverseVocab.put(nextId, token);
            return nextId++;
        }
        return vocab.get(token);
    }

    @Override
    public long[] encode(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new long[0];
        }
        
        String[] words = text.toLowerCase().split("\\s+");
        List<Long> idsList = new ArrayList<>();
        
        for (String word : words) {
            String cleaned = word.replaceAll("[^a-zA-Z0-9]", "");
            if (cleaned.isEmpty()) continue;
            
            if (vocab.containsKey(cleaned)) {
                idsList.add(vocab.get(cleaned));
            } else {
                long id = addToken(cleaned);
                idsList.add(id);
            }
        }
        
        return idsList.stream().mapToLong(Long::longValue).toArray();
    }

    @Override
    public EncodingResult encodeWithAttention(String text) {
        long[] ids = encode(text);
        long[] attention = new long[ids.length];
        Arrays.fill(attention, 1L);
        return new EncodingResult(ids, attention);
    }

    @Override
    public String decode(long[] tokenIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokenIds.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(reverseVocab.getOrDefault(tokenIds[i], "<unk>"));
        }
        return sb.toString();
    }

    @Override
    public String decode(long tokenId) {
        return reverseVocab.getOrDefault(tokenId, "<unk>");
    }

    @Override
    public int getVocabSize() {
        return vocab.size();
    }

    @Override
    public long getEosTokenId() {
        return eosTokenId;
    }

    @Override
    public long getPadTokenId() {
        return padTokenId;
    }
}

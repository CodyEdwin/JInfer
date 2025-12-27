package com.jinfer.config;

/**
 * Configuration for text generation parameters.
 */
public class GenerationConfig {
    private int maxNewTokens = 256;
    private float temperature = 1.0f;
    private float topP = 0.9f;
    private int topK = 50;
    private String stopSequence = null;
    private boolean doSample = true;
    private long seed = -1;

    public GenerationConfig() {}

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public int getMaxNewTokens() { return maxNewTokens; }
    public float getTemperature() { return temperature; }
    public float getTopP() { return topP; }
    public int getTopK() { return topK; }
    public String getStopSequence() { return stopSequence; }
    public boolean isDoSample() { return doSample; }
    public long getSeed() { return seed; }

    // Setters
    public void setMaxNewTokens(int maxNewTokens) { this.maxNewTokens = maxNewTokens; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    public void setTopP(float topP) { this.topP = topP; }
    public void setTopK(int topK) { this.topK = topK; }
    public void setStopSequence(String stopSequence) { this.stopSequence = stopSequence; }
    public void setDoSample(boolean doSample) { this.doSample = doSample; }
    public void setSeed(long seed) { this.seed = seed; }

    public static class Builder {
        private final GenerationConfig config = new GenerationConfig();

        public Builder maxNewTokens(int maxNewTokens) {
            config.maxNewTokens = maxNewTokens;
            return this;
        }

        public Builder temperature(float temperature) {
            config.temperature = temperature;
            return this;
        }

        public Builder topP(float topP) {
            config.topP = topP;
            return this;
        }

        public Builder topK(int topK) {
            config.topK = topK;
            return this;
        }

        public Builder stopSequence(String stopSequence) {
            config.stopSequence = stopSequence;
            return this;
        }

        public Builder doSample(boolean doSample) {
            config.doSample = doSample;
            return this;
        }

        public Builder seed(long seed) {
            config.seed = seed;
            return this;
        }

        public GenerationConfig build() {
            return config;
        }
    }

    @Override
    public String toString() {
        return "GenerationConfig{" +
                "maxNewTokens=" + maxNewTokens +
                ", temperature=" + temperature +
                ", topP=" + topP +
                ", topK=" + topK +
                ", stopSequence='" + stopSequence + '\'' +
                ", doSample=" + doSample +
                ", seed=" + seed +
                '}';
    }
}

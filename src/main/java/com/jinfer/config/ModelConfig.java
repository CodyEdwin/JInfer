package com.jinfer.config;

import java.nio.file.Path;

/**
 * Configuration for model loading and initialization.
 */
public class ModelConfig {
    private Path modelPath;
    private Path tokenizerPath;
    private String modelFormat = "onnx";
    private int contextLength = 2048;
    private boolean useGpu = false;
    private int gpuDeviceId = 0;

    public ModelConfig() {}

    public ModelConfig(Path modelPath, Path tokenizerPath) {
        this.modelPath = modelPath;
        this.tokenizerPath = tokenizerPath;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Path getModelPath() { return modelPath; }
    public Path getTokenizerPath() { return tokenizerPath; }
    public String getModelFormat() { return modelFormat; }
    public int getContextLength() { return contextLength; }
    public boolean isUseGpu() { return useGpu; }
    public int getGpuDeviceId() { return gpuDeviceId; }

    // Setters
    public void setModelPath(Path modelPath) { this.modelPath = modelPath; }
    public void setTokenizerPath(Path tokenizerPath) { this.tokenizerPath = tokenizerPath; }
    public void setModelFormat(String modelFormat) { this.modelFormat = modelFormat; }
    public void setContextLength(int contextLength) { this.contextLength = contextLength; }
    public void setUseGpu(boolean useGpu) { this.useGpu = useGpu; }
    public void setGpuDeviceId(int gpuDeviceId) { this.gpuDeviceId = gpuDeviceId; }

    public static class Builder {
        private final ModelConfig config = new ModelConfig();

        public Builder modelPath(Path modelPath) {
            config.modelPath = modelPath;
            return this;
        }

        public Builder tokenizerPath(Path tokenizerPath) {
            config.tokenizerPath = tokenizerPath;
            return this;
        }

        public Builder modelFormat(String modelFormat) {
            config.modelFormat = modelFormat;
            return this;
        }

        public Builder contextLength(int contextLength) {
            config.contextLength = contextLength;
            return this;
        }

        public Builder useGpu(boolean useGpu) {
            config.useGpu = useGpu;
            return this;
        }

        public Builder gpuDeviceId(int gpuDeviceId) {
            config.gpuDeviceId = gpuDeviceId;
            return this;
        }

        public ModelConfig build() {
            return config;
        }
    }

    @Override
    public String toString() {
        return "ModelConfig{" +
                "modelPath=" + modelPath +
                ", tokenizerPath=" + tokenizerPath +
                ", modelFormat='" + modelFormat + '\'' +
                ", contextLength=" + contextLength +
                ", useGpu=" + useGpu +
                ", gpuDeviceId=" + gpuDeviceId +
                '}';
    }
}

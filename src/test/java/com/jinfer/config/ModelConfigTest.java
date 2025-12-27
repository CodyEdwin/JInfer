package com.jinfer.config;

import org.junit.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;

public class ModelConfigTest {

    @Test
    public void testDefaultValues() {
        ModelConfig config = new ModelConfig();
        
        assertNull(config.getModelPath());
        assertNull(config.getTokenizerPath());
        assertEquals("onnx", config.getModelFormat());
        assertEquals(2048, config.getContextLength());
        assertFalse(config.isUseGpu());
        assertEquals(0, config.getGpuDeviceId());
    }

    @Test
    public void testConstructorWithPaths() {
        Path modelPath = Paths.get("/models/llama.onnx");
        Path tokenizerPath = Paths.get("/models/tokenizer.json");
        
        ModelConfig config = new ModelConfig(modelPath, tokenizerPath);
        
        assertEquals(modelPath, config.getModelPath());
        assertEquals(tokenizerPath, config.getTokenizerPath());
    }

    @Test
    public void testBuilder() {
        Path modelPath = Paths.get("/models/gpt2.onnx");
        Path tokenizerPath = Paths.get("/models/tokenizer");
        
        ModelConfig config = ModelConfig.builder()
                .modelPath(modelPath)
                .tokenizerPath(tokenizerPath)
                .modelFormat("onnx")
                .contextLength(4096)
                .useGpu(true)
                .gpuDeviceId(1)
                .build();

        assertEquals(modelPath, config.getModelPath());
        assertEquals(tokenizerPath, config.getTokenizerPath());
        assertEquals("onnx", config.getModelFormat());
        assertEquals(4096, config.getContextLength());
        assertTrue(config.isUseGpu());
        assertEquals(1, config.getGpuDeviceId());
    }

    @Test
    public void testSetters() {
        ModelConfig config = new ModelConfig();
        Path path = Paths.get("/test/model.onnx");
        
        config.setModelPath(path);
        config.setModelFormat("pytorch");
        config.setContextLength(1024);
        config.setUseGpu(true);
        config.setGpuDeviceId(2);

        assertEquals(path, config.getModelPath());
        assertEquals("pytorch", config.getModelFormat());
        assertEquals(1024, config.getContextLength());
        assertTrue(config.isUseGpu());
        assertEquals(2, config.getGpuDeviceId());
    }

    @Test
    public void testToString() {
        ModelConfig config = ModelConfig.builder()
                .contextLength(2048)
                .useGpu(false)
                .build();

        String str = config.toString();
        assertTrue(str.contains("contextLength=2048"));
        assertTrue(str.contains("useGpu=false"));
    }
}

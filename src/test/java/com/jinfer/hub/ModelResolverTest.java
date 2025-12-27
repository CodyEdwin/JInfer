package com.jinfer.hub;

import com.jinfer.config.ModelConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ModelResolverTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ModelResolver resolver;
    private Path cacheDir;

    @Before
    public void setUp() throws IOException {
        cacheDir = tempFolder.newFolder("cache").toPath();
        HuggingFaceHub hub = new HuggingFaceHub(cacheDir);
        resolver = new ModelResolver(hub);
    }

    @Test
    public void testResolveLocalPath() throws IOException {
        // Create a local model directory
        Path modelDir = tempFolder.newFolder("local-model").toPath();
        Files.writeString(modelDir.resolve("model.onnx"), "fake model");
        Files.writeString(modelDir.resolve("tokenizer.json"), "{}");
        Files.writeString(modelDir.resolve("config.json"), "{\"max_position_embeddings\": 4096}");

        ModelConfig config = resolver.resolve(modelDir.toString());

        assertNotNull(config);
        assertTrue(config.getModelPath().toString().contains("model.onnx"));
        assertEquals("onnx", config.getModelFormat());
        assertEquals(4096, config.getContextLength());
    }

    @Test
    public void testIsAvailableLocalPath() throws IOException {
        Path modelDir = tempFolder.newFolder("available-model").toPath();
        
        assertTrue(resolver.isAvailable(modelDir.toString()));
    }

    @Test
    public void testIsAvailableNonexistent() {
        assertFalse(resolver.isAvailable("/nonexistent/path"));
    }

    @Test
    public void testIsAvailableCachedHuggingFace() throws IOException {
        // Create fake cached model
        Path modelDir = cacheDir.resolve("user--cached-model");
        Files.createDirectories(modelDir);
        Files.writeString(modelDir.resolve(".jinfer_downloaded"), "test");

        assertTrue(resolver.isAvailable("user/cached-model"));
    }

    @Test
    public void testIsAvailableNotCachedHuggingFace() {
        assertFalse(resolver.isAvailable("user/not-cached"));
    }

    @Test
    public void testDetectOnnxFormat() throws IOException {
        Path modelDir = tempFolder.newFolder("onnx-model").toPath();
        Files.writeString(modelDir.resolve("model.onnx"), "fake");

        ModelConfig config = resolver.resolve(modelDir.toString());

        assertEquals("onnx", config.getModelFormat());
    }

    @Test
    public void testDetectPytorchFormat() throws IOException {
        Path modelDir = tempFolder.newFolder("pytorch-model").toPath();
        Files.writeString(modelDir.resolve("model.bin"), "fake");

        ModelConfig config = resolver.resolve(modelDir.toString());

        assertEquals("pytorch", config.getModelFormat());
    }

    @Test
    public void testDetectSafetensorsFormat() throws IOException {
        Path modelDir = tempFolder.newFolder("safetensors-model").toPath();
        Files.writeString(modelDir.resolve("model.safetensors"), "fake");

        ModelConfig config = resolver.resolve(modelDir.toString());

        assertEquals("safetensors", config.getModelFormat());
    }

    @Test
    public void testReadContextLengthFromConfig() throws IOException {
        Path modelDir = tempFolder.newFolder("config-model").toPath();
        Files.writeString(modelDir.resolve("model.onnx"), "fake");
        Files.writeString(modelDir.resolve("config.json"), 
            "{\"hidden_size\": 768, \"max_position_embeddings\": 8192}");

        ModelConfig config = resolver.resolve(modelDir.toString());

        assertEquals(8192, config.getContextLength());
    }

    @Test
    public void testReadContextLengthNPositions() throws IOException {
        Path modelDir = tempFolder.newFolder("gpt-model").toPath();
        Files.writeString(modelDir.resolve("model.onnx"), "fake");
        Files.writeString(modelDir.resolve("config.json"), 
            "{\"n_positions\": 1024}");

        ModelConfig config = resolver.resolve(modelDir.toString());

        assertEquals(1024, config.getContextLength());
    }

    @Test
    public void testDefaultContextLength() throws IOException {
        Path modelDir = tempFolder.newFolder("no-config-model").toPath();
        Files.writeString(modelDir.resolve("model.onnx"), "fake");

        ModelConfig config = resolver.resolve(modelDir.toString());

        assertEquals(2048, config.getContextLength()); // default
    }

    @Test(expected = IOException.class)
    public void testResolveNonexistent() throws IOException {
        resolver.resolve("/nonexistent/path/to/model");
    }

    @Test
    public void testSetAuthToken() {
        resolver.setAuthToken("hf_token");
        // Should not throw
    }

    @Test
    public void testGetHub() {
        assertNotNull(resolver.getHub());
    }
}

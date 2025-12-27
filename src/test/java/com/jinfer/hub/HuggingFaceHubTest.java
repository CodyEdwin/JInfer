package com.jinfer.hub;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class HuggingFaceHubTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private HuggingFaceHub hub;
    private Path cacheDir;

    @Before
    public void setUp() throws IOException {
        cacheDir = tempFolder.newFolder("cache").toPath();
        hub = new HuggingFaceHub(cacheDir);
    }

    @Test
    public void testGetCacheDir() {
        assertEquals(cacheDir, hub.getCacheDir());
    }

    @Test
    public void testGetModelCachePath() {
        Path path = hub.getModelCachePath("microsoft/DialoGPT-small");
        assertEquals("microsoft--DialoGPT-small", path.getFileName().toString());
    }

    @Test
    public void testIsModelCachedFalseWhenNotDownloaded() {
        assertFalse(hub.isModelCached("user/nonexistent-model"));
    }

    @Test
    public void testIsModelCachedTrueWhenMarkerExists() throws IOException {
        // Create fake cached model
        Path modelDir = cacheDir.resolve("user--test-model");
        Files.createDirectories(modelDir);
        Files.writeString(modelDir.resolve(".jinfer_downloaded"), "test");

        assertTrue(hub.isModelCached("user/test-model"));
    }

    @Test
    public void testListCachedModelsEmpty() throws IOException {
        List<String> models = hub.listCachedModels();
        assertTrue(models.isEmpty());
    }

    @Test
    public void testListCachedModels() throws IOException {
        // Create fake cached models
        Path model1 = cacheDir.resolve("user--model1");
        Path model2 = cacheDir.resolve("org--model2");
        Files.createDirectories(model1);
        Files.createDirectories(model2);
        Files.writeString(model1.resolve(".jinfer_downloaded"), "test");
        Files.writeString(model2.resolve(".jinfer_downloaded"), "test");

        List<String> models = hub.listCachedModels();
        assertEquals(2, models.size());
        assertTrue(models.contains("user/model1"));
        assertTrue(models.contains("org/model2"));
    }

    @Test
    public void testDeleteModel() throws IOException {
        // Create fake cached model
        Path modelDir = cacheDir.resolve("user--to-delete");
        Files.createDirectories(modelDir);
        Files.writeString(modelDir.resolve(".jinfer_downloaded"), "test");
        Files.writeString(modelDir.resolve("model.onnx"), "fake model");

        assertTrue(Files.exists(modelDir));

        boolean result = hub.deleteModel("user/to-delete");
        
        assertTrue(result);
        assertFalse(Files.exists(modelDir));
    }

    @Test
    public void testDeleteNonexistentModel() throws IOException {
        boolean result = hub.deleteModel("user/nonexistent");
        assertFalse(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModelInvalidRepoIdNull() throws IOException {
        hub.getModel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModelInvalidRepoIdEmpty() throws IOException {
        hub.getModel("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModelInvalidRepoIdNoSlash() throws IOException {
        hub.getModel("invalid-repo-id");
    }

    @Test
    public void testSetAuthToken() {
        // Should not throw
        hub.setAuthToken("hf_test_token");
    }

    @Test
    public void testDefaultCacheDir() {
        HuggingFaceHub defaultHub = new HuggingFaceHub();
        Path defaultCache = defaultHub.getCacheDir();
        
        assertTrue(defaultCache.toString().contains(".jinfer"));
        assertTrue(defaultCache.toString().contains("models"));
    }
}

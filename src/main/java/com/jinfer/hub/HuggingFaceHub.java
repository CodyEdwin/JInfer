package com.jinfer.hub;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Downloads and caches models from HuggingFace Hub.
 */
public class HuggingFaceHub {
    
    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceHub.class);
    private static final String HF_API_URL = "https://huggingface.co/api/models/";
    private static final String HF_DOWNLOAD_URL = "https://huggingface.co/%s/resolve/main/%s";
    private static final int BUFFER_SIZE = 8192;
    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 60000;
    
    private final Path cacheDir;
    private final Gson gson;
    private String authToken;

    public HuggingFaceHub() {
        this(getDefaultCacheDir());
    }

    public HuggingFaceHub(Path cacheDir) {
        this.cacheDir = cacheDir;
        this.gson = new Gson();
        ensureCacheDir();
    }

    private static Path getDefaultCacheDir() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".jinfer", "models");
    }

    private void ensureCacheDir() {
        try {
            Files.createDirectories(cacheDir);
            logger.info("Cache directory: {}", cacheDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache directory: " + cacheDir, e);
        }
    }

    /**
     * Set HuggingFace auth token for private/gated models.
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    /**
     * Get local path for a model. Downloads if not cached.
     *
     * @param repoId Model repository ID (e.g., "microsoft/DialoGPT-small")
     * @return Path to the local model directory
     */
    public Path getModel(String repoId) throws IOException {
        return getModel(repoId, false);
    }

    /**
     * Get local path for a model.
     *
     * @param repoId Model repository ID
     * @param forceDownload Force re-download even if cached
     * @return Path to the local model directory
     */
    public Path getModel(String repoId, boolean forceDownload) throws IOException {
        validateRepoId(repoId);
        
        Path modelDir = getModelCachePath(repoId);
        
        if (!forceDownload && isModelCached(modelDir)) {
            logger.info("Model '{}' found in cache: {}", repoId, modelDir);
            return modelDir;
        }
        
        logger.info("Downloading model '{}' from HuggingFace...", repoId);
        downloadModel(repoId, modelDir);
        
        return modelDir;
    }

    /**
     * Check if a model is already downloaded.
     */
    public boolean isModelCached(String repoId) {
        Path modelDir = getModelCachePath(repoId);
        return isModelCached(modelDir);
    }

    private boolean isModelCached(Path modelDir) {
        if (!Files.exists(modelDir)) {
            return false;
        }
        
        // Check for essential files
        Path markerFile = modelDir.resolve(".jinfer_downloaded");
        return Files.exists(markerFile);
    }

    /**
     * Get the cache path for a model.
     */
    public Path getModelCachePath(String repoId) {
        // Replace / with -- for directory name
        String safeName = repoId.replace("/", "--");
        return cacheDir.resolve(safeName);
    }

    private void validateRepoId(String repoId) {
        if (repoId == null || repoId.isEmpty()) {
            throw new IllegalArgumentException("Repository ID cannot be null or empty");
        }
        if (!repoId.contains("/")) {
            throw new IllegalArgumentException("Invalid repository ID format. Expected: 'user/repo' or 'org/repo'");
        }
    }

    private void downloadModel(String repoId, Path modelDir) throws IOException {
        // Create model directory
        Files.createDirectories(modelDir);
        
        // Get list of files from HF API
        List<String> files = getModelFiles(repoId);
        
        if (files.isEmpty()) {
            throw new IOException("No files found in repository: " + repoId);
        }
        
        // Filter for essential files
        List<String> filesToDownload = filterEssentialFiles(files);
        
        logger.info("Found {} files to download", filesToDownload.size());
        
        // Download each file
        int downloaded = 0;
        for (String file : filesToDownload) {
            try {
                downloadFile(repoId, file, modelDir);
                downloaded++;
                logger.info("Downloaded ({}/{}) {}", downloaded, filesToDownload.size(), file);
            } catch (IOException e) {
                logger.warn("Failed to download {}: {}", file, e.getMessage());
            }
        }
        
        if (downloaded == 0) {
            throw new IOException("Failed to download any files from: " + repoId);
        }
        
        // Create marker file
        Path markerFile = modelDir.resolve(".jinfer_downloaded");
        Files.writeString(markerFile, repoId + "\n" + System.currentTimeMillis());
        
        logger.info("Model downloaded successfully to: {}", modelDir);
    }

    private List<String> getModelFiles(String repoId) throws IOException {
        List<String> files = new ArrayList<>();
        
        String apiUrl = HF_API_URL + repoId;
        HttpURLConnection conn = createConnection(apiUrl);
        
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Failed to get model info: HTTP " + responseCode);
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                JsonObject response = gson.fromJson(reader, JsonObject.class);
                
                if (response.has("siblings")) {
                    JsonArray siblings = response.getAsJsonArray("siblings");
                    for (JsonElement elem : siblings) {
                        JsonObject file = elem.getAsJsonObject();
                        if (file.has("rfilename")) {
                            files.add(file.get("rfilename").getAsString());
                        }
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
        
        return files;
    }

    private List<String> filterEssentialFiles(List<String> files) {
        List<String> essential = new ArrayList<>();
        
        for (String file : files) {
            String lower = file.toLowerCase();
            
            // Include model files
            if (lower.endsWith(".onnx") ||
                lower.endsWith(".bin") ||
                lower.endsWith(".safetensors") ||
                lower.endsWith(".pt") ||
                lower.endsWith(".pth")) {
                essential.add(file);
                continue;
            }
            
            // Include tokenizer files
            if (lower.contains("tokenizer") ||
                lower.equals("vocab.json") ||
                lower.equals("merges.txt") ||
                lower.equals("special_tokens_map.json")) {
                essential.add(file);
                continue;
            }
            
            // Include config files
            if (lower.equals("config.json") ||
                lower.equals("generation_config.json") ||
                lower.equals("model_index.json")) {
                essential.add(file);
            }
        }
        
        return essential;
    }

    private void downloadFile(String repoId, String filename, Path modelDir) throws IOException {
        String fileUrl = String.format(HF_DOWNLOAD_URL, repoId, filename);
        Path targetPath = modelDir.resolve(filename);
        
        // Create parent directories for nested files
        Files.createDirectories(targetPath.getParent());
        
        HttpURLConnection conn = createConnection(fileUrl);
        
        try {
            int responseCode = conn.getResponseCode();
            
            // Handle redirects
            if (responseCode == 302 || responseCode == 301) {
                String redirectUrl = conn.getHeaderField("Location");
                conn.disconnect();
                conn = createConnection(redirectUrl);
                responseCode = conn.getResponseCode();
            }
            
            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode);
            }
            
            long totalSize = conn.getContentLengthLong();
            
            try (InputStream in = conn.getInputStream();
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetPath))) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                long downloaded = 0;
                int bytesRead;
                int lastProgress = 0;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;
                    
                    if (totalSize > 0) {
                        int progress = (int) (downloaded * 100 / totalSize);
                        if (progress >= lastProgress + 10) {
                            logger.debug("  {}% ({}/{})", progress, formatSize(downloaded), formatSize(totalSize));
                            lastProgress = progress;
                        }
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
    }

    private HttpURLConnection createConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("User-Agent", "JInfer/1.0");
        
        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        
        return conn;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * List all cached models.
     */
    public List<String> listCachedModels() throws IOException {
        List<String> models = new ArrayList<>();
        
        if (!Files.exists(cacheDir)) {
            return models;
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path) && Files.exists(path.resolve(".jinfer_downloaded"))) {
                    String name = path.getFileName().toString().replace("--", "/");
                    models.add(name);
                }
            }
        }
        
        return models;
    }

    /**
     * Delete a cached model.
     */
    public boolean deleteModel(String repoId) throws IOException {
        Path modelDir = getModelCachePath(repoId);
        
        if (!Files.exists(modelDir)) {
            return false;
        }
        
        // Recursively delete
        Files.walk(modelDir)
             .sorted((a, b) -> b.compareTo(a))
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     logger.warn("Failed to delete: {}", path);
                 }
             });
        
        return true;
    }

    /**
     * Get the cache directory path.
     */
    public Path getCacheDir() {
        return cacheDir;
    }
}

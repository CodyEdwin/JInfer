package com.jinfer.hub;

import com.jinfer.config.ModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

/**
 * Resolves model paths - handles local paths and HuggingFace repo IDs.
 */
public class ModelResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(ModelResolver.class);
    
    private final HuggingFaceHub hub;

    public ModelResolver() {
        this.hub = new HuggingFaceHub();
    }

    public ModelResolver(HuggingFaceHub hub) {
        this.hub = hub;
    }

    /**
     * Set HuggingFace auth token.
     */
    public void setAuthToken(String token) {
        hub.setAuthToken(token);
    }

    /**
     * Resolve a model identifier to a ModelConfig.
     * Supports:
     * - Local paths: /path/to/model or ./model
     * - HuggingFace repos: user/repo or org/model-name
     *
     * @param modelId Local path or HuggingFace repo ID
     * @return Resolved ModelConfig with local paths
     */
    public ModelConfig resolve(String modelId) throws IOException {
        return resolve(modelId, false);
    }

    /**
     * Resolve a model identifier with force download option.
     */
    public ModelConfig resolve(String modelId, boolean forceDownload) throws IOException {
        Path modelPath;
        
        // Check if it's a local path
        Path localPath = Paths.get(modelId);
        if (Files.exists(localPath)) {
            logger.info("Using local model: {}", localPath);
            modelPath = localPath;
        } 
        // Check if it looks like a HuggingFace repo ID
        else if (isHuggingFaceRepoId(modelId)) {
            logger.info("Resolving HuggingFace model: {}", modelId);
            modelPath = hub.getModel(modelId, forceDownload);
        }
        // Try as local path that might not exist yet
        else {
            throw new IOException("Model not found: " + modelId + 
                    ". Provide a valid local path or HuggingFace repo ID (user/repo)");
        }
        
        return buildConfig(modelPath);
    }

    /**
     * Check if a model is available (locally or in cache).
     */
    public boolean isAvailable(String modelId) {
        // Check local path
        if (Files.exists(Paths.get(modelId))) {
            return true;
        }
        
        // Check HuggingFace cache
        if (isHuggingFaceRepoId(modelId)) {
            return hub.isModelCached(modelId);
        }
        
        return false;
    }

    private boolean isHuggingFaceRepoId(String id) {
        // HuggingFace repo IDs are in format: user/repo or org/model-name
        if (id == null || id.isEmpty()) {
            return false;
        }
        
        // Must contain exactly one /
        int slashCount = id.length() - id.replace("/", "").length();
        if (slashCount != 1) {
            return false;
        }
        
        // Should not start with / or . (local paths)
        if (id.startsWith("/") || id.startsWith(".")) {
            return false;
        }
        
        // Should not contain path separators other than single /
        if (id.contains("\\") || id.contains("//")) {
            return false;
        }
        
        return true;
    }

    private ModelConfig buildConfig(Path modelDir) throws IOException {
        ModelConfig.Builder builder = ModelConfig.builder();
        
        if (Files.isDirectory(modelDir)) {
            // Find model file
            Optional<Path> modelFile = findModelFile(modelDir);
            if (modelFile.isPresent()) {
                builder.modelPath(modelFile.get());
                builder.modelFormat(detectFormat(modelFile.get()));
            } else {
                builder.modelPath(modelDir);
            }
            
            // Find tokenizer
            Path tokenizerJson = modelDir.resolve("tokenizer.json");
            if (Files.exists(tokenizerJson)) {
                builder.tokenizerPath(tokenizerJson);
            } else {
                builder.tokenizerPath(modelDir);
            }
            
            // Read context length from config if available
            int contextLength = readContextLength(modelDir);
            builder.contextLength(contextLength);
            
        } else {
            // Single file
            builder.modelPath(modelDir);
            builder.modelFormat(detectFormat(modelDir));
            builder.tokenizerPath(modelDir.getParent());
        }
        
        return builder.build();
    }

    private Optional<Path> findModelFile(Path dir) throws IOException {
        // Priority: ONNX > SafeTensors > PyTorch
        String[] patterns = {"*.onnx", "*.safetensors", "*.bin", "*.pt", "*.pth"};
        
        for (String pattern : patterns) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, pattern)) {
                for (Path path : stream) {
                    // Skip if it's a tokenizer or optimizer file
                    String name = path.getFileName().toString().toLowerCase();
                    if (name.contains("tokenizer") || name.contains("optimizer")) {
                        continue;
                    }
                    return Optional.of(path);
                }
            }
        }
        
        return Optional.empty();
    }

    private String detectFormat(Path modelFile) {
        String name = modelFile.getFileName().toString().toLowerCase();
        
        if (name.endsWith(".onnx")) return "onnx";
        if (name.endsWith(".safetensors")) return "safetensors";
        if (name.endsWith(".pt") || name.endsWith(".pth")) return "pytorch";
        if (name.endsWith(".bin")) return "pytorch";
        
        return "onnx"; // default
    }

    private int readContextLength(Path modelDir) {
        Path configFile = modelDir.resolve("config.json");
        
        if (Files.exists(configFile)) {
            try {
                String content = Files.readString(configFile);
                
                // Simple parsing for common fields
                if (content.contains("\"max_position_embeddings\"")) {
                    return extractInt(content, "max_position_embeddings", 2048);
                }
                if (content.contains("\"n_positions\"")) {
                    return extractInt(content, "n_positions", 2048);
                }
                if (content.contains("\"max_seq_len\"")) {
                    return extractInt(content, "max_seq_len", 2048);
                }
            } catch (IOException e) {
                logger.debug("Could not read config.json: {}", e.getMessage());
            }
        }
        
        return 2048; // default
    }

    private int extractInt(String json, String key, int defaultValue) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // ignore
        }
        return defaultValue;
    }

    /**
     * Get the underlying HuggingFace hub instance.
     */
    public HuggingFaceHub getHub() {
        return hub;
    }
}

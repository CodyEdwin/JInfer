package com.jinfer.cli;

import com.jinfer.config.GenerationConfig;
import com.jinfer.config.ModelConfig;
import com.jinfer.engine.JInferEngine;
import com.jinfer.engine.LLMEngine;
import com.jinfer.hub.HuggingFaceHub;
import com.jinfer.hub.ModelResolver;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command-line interface for JInfer engine.
 */
@Command(
    name = "jinfer",
    mixinStandardHelpOptions = true,
    version = "JInfer 1.0.0",
    description = "Java LLM Inference Engine",
    subcommands = {
        JInferCLI.RunCommand.class,
        JInferCLI.DownloadCommand.class,
        JInferCLI.ListCommand.class,
        JInferCLI.DeleteCommand.class
    }
)
public class JInferCLI implements Callable<Integer> {

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JInferCLI()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Run inference with a model.
     */
    @Command(name = "run", description = "Run inference with a model")
    static class RunCommand implements Callable<Integer> {

        @Option(names = {"-m", "--model"}, description = "Model path or HuggingFace repo (user/repo)", required = true)
        private String model;

        @Option(names = {"-p", "--prompt"}, description = "Input prompt for generation", required = true)
        private String prompt;

        @Option(names = {"--max-tokens"}, description = "Maximum new tokens to generate", defaultValue = "256")
        private int maxTokens;

        @Option(names = {"--temperature"}, description = "Sampling temperature", defaultValue = "0.7")
        private float temperature;

        @Option(names = {"--top-p"}, description = "Top-p (nucleus) sampling", defaultValue = "0.9")
        private float topP;

        @Option(names = {"--top-k"}, description = "Top-k sampling", defaultValue = "50")
        private int topK;

        @Option(names = {"--seed"}, description = "Random seed for reproducibility", defaultValue = "-1")
        private long seed;

        @Option(names = {"--stream"}, description = "Enable streaming output")
        private boolean stream;

        @Option(names = {"--token"}, description = "HuggingFace auth token for private models")
        private String hfToken;

        @Option(names = {"--force-download"}, description = "Force re-download even if cached")
        private boolean forceDownload;

        @Override
        public Integer call() throws Exception {
            System.out.println("JInfer - Java LLM Inference Engine");
            System.out.println("===================================\n");

            // Resolve model (download from HuggingFace if needed)
            ModelResolver resolver = new ModelResolver();
            if (hfToken != null) {
                resolver.setAuthToken(hfToken);
            }

            System.out.println("Resolving model: " + model);
            
            ModelConfig modelConfig;
            try {
                modelConfig = resolver.resolve(model, forceDownload);
                System.out.println("Model path: " + modelConfig.getModelPath());
                System.out.println("Tokenizer path: " + modelConfig.getTokenizerPath());
            } catch (Exception e) {
                System.err.println("Failed to resolve model: " + e.getMessage());
                return 1;
            }

            // Load engine
            LLMEngine engine = new JInferEngine();
            try {
                ((JInferEngine) engine).loadModel(modelConfig);
            } catch (Exception e) {
                System.err.println("Failed to load model: " + e.getMessage());
                return 1;
            }

            // Configure generation
            GenerationConfig genConfig = GenerationConfig.builder()
                    .maxNewTokens(maxTokens)
                    .temperature(temperature)
                    .topP(topP)
                    .topK(topK)
                    .seed(seed)
                    .doSample(temperature > 0.01f)
                    .build();

            System.out.println("\nPrompt: " + prompt);
            System.out.println("Config: " + genConfig);
            System.out.println("\nGenerated Output:");
            System.out.println("-----------------");

            try {
                if (stream) {
                    Iterator<String> tokens = engine.generateStream(prompt, genConfig);
                    while (tokens.hasNext()) {
                        System.out.print(tokens.next());
                        System.out.flush();
                    }
                    System.out.println();
                } else {
                    String output = engine.generate(prompt, genConfig);
                    System.out.println(output);
                }
            } finally {
                engine.close();
            }

            System.out.println("\n-----------------");
            System.out.println("Generation complete.");

            return 0;
        }
    }

    /**
     * Download a model from HuggingFace.
     */
    @Command(name = "download", description = "Download a model from HuggingFace")
    static class DownloadCommand implements Callable<Integer> {

        @Option(names = {"-m", "--model"}, description = "HuggingFace repo ID (user/repo)", required = true)
        private String model;

        @Option(names = {"--token"}, description = "HuggingFace auth token for private/gated models")
        private String hfToken;

        @Option(names = {"--force"}, description = "Force re-download even if cached")
        private boolean force;

        @Override
        public Integer call() throws Exception {
            System.out.println("JInfer Model Downloader");
            System.out.println("=======================\n");

            HuggingFaceHub hub = new HuggingFaceHub();
            if (hfToken != null) {
                hub.setAuthToken(hfToken);
            }

            System.out.println("Downloading: " + model);
            System.out.println("Cache directory: " + hub.getCacheDir());
            System.out.println();

            try {
                Path modelPath = hub.getModel(model, force);
                System.out.println("\nModel downloaded successfully!");
                System.out.println("Location: " + modelPath);
                System.out.println("\nRun with:");
                System.out.println("  jinfer run -m " + model + " -p \"Your prompt here\"");
            } catch (Exception e) {
                System.err.println("Download failed: " + e.getMessage());
                return 1;
            }

            return 0;
        }
    }

    /**
     * List cached models.
     */
    @Command(name = "list", description = "List cached models")
    static class ListCommand implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("JInfer Cached Models");
            System.out.println("====================\n");

            HuggingFaceHub hub = new HuggingFaceHub();
            System.out.println("Cache directory: " + hub.getCacheDir());
            System.out.println();

            List<String> models = hub.listCachedModels();

            if (models.isEmpty()) {
                System.out.println("No models cached.");
                System.out.println("\nDownload a model with:");
                System.out.println("  jinfer download -m user/repo");
            } else {
                System.out.println("Cached models:");
                for (String model : models) {
                    Path path = hub.getModelCachePath(model);
                    System.out.println("  - " + model);
                    System.out.println("    " + path);
                }
            }

            return 0;
        }
    }

    /**
     * Delete a cached model.
     */
    @Command(name = "delete", description = "Delete a cached model")
    static class DeleteCommand implements Callable<Integer> {

        @Option(names = {"-m", "--model"}, description = "Model repo ID to delete", required = true)
        private String model;

        @Override
        public Integer call() throws Exception {
            HuggingFaceHub hub = new HuggingFaceHub();

            if (hub.deleteModel(model)) {
                System.out.println("Deleted: " + model);
            } else {
                System.out.println("Model not found in cache: " + model);
            }

            return 0;
        }
    }
}

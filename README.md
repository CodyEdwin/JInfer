# JInfer

**Java LLM Inference Engine** - A high-performance, offline-capable inference engine for running Large Language Models in Java applications.

## Features

- **Local LLM Inference** - Run models locally without external API dependencies
- **HuggingFace Integration** - Download and cache models directly from HuggingFace Hub
- **Multiple Backends** - Support for ONNX Runtime via Deep Java Library (DJL)
- **Sampling Strategies** - Greedy, Temperature, Top-K, and Top-P (Nucleus) sampling
- **Streaming Output** - Token-by-token generation for responsive applications
- **CLI & Library** - Use as command-line tool or embed in Java applications

## Requirements

- Java 17 or higher
- Gradle 8.5+ (included via wrapper)

## Quick Start

### Installation

```bash
# Clone and build
git clone <repository-url>
cd jinfer

# Install for current user
make install-user

# Add to PATH (add to ~/.bashrc or ~/.zshrc)
export PATH="$HOME/.local/bin:$PATH"
```

### Basic Usage

```bash
# Download a model from HuggingFace
jinfer download -m microsoft/DialoGPT-small

# Run inference
jinfer run -m microsoft/DialoGPT-small -p "Hello, how are you?"

# Stream output
jinfer run -m microsoft/DialoGPT-small -p "Tell me a story" --stream

# List cached models
jinfer list
```

## CLI Commands

### `jinfer run`

Run inference with a model.

```bash
jinfer run -m <model> -p <prompt> [options]

Options:
  -m, --model         Model path or HuggingFace repo (user/repo)
  -p, --prompt        Input prompt for generation
  --max-tokens        Maximum tokens to generate (default: 256)
  --temperature       Sampling temperature (default: 0.7)
  --top-p             Top-p nucleus sampling (default: 0.9)
  --top-k             Top-k sampling (default: 50)
  --seed              Random seed for reproducibility
  --stream            Enable streaming output
  --token             HuggingFace auth token for private models
  --force-download    Force re-download even if cached
```

### `jinfer download`

Download a model from HuggingFace.

```bash
jinfer download -m <repo-id> [--token <hf-token>] [--force]
```

### `jinfer list`

List all cached models.

```bash
jinfer list
```

### `jinfer delete`

Delete a cached model.

```bash
jinfer delete -m <repo-id>
```

## Library Usage

### Maven/Gradle Dependency

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.jinfer:jinfer:1.0.0")
}
```

### Java API

```java
import com.jinfer.engine.JInferEngine;
import com.jinfer.config.GenerationConfig;
import com.jinfer.config.ModelConfig;
import com.jinfer.hub.ModelResolver;

public class Example {
    public static void main(String[] args) throws Exception {
        // Resolve model (downloads from HuggingFace if needed)
        ModelResolver resolver = new ModelResolver();
        ModelConfig modelConfig = resolver.resolve("microsoft/DialoGPT-small");
        
        // Create engine and load model
        try (JInferEngine engine = new JInferEngine()) {
            engine.loadModel(modelConfig);
            
            // Configure generation
            GenerationConfig genConfig = GenerationConfig.builder()
                .maxNewTokens(100)
                .temperature(0.7f)
                .topP(0.9f)
                .build();
            
            // Generate text
            String output = engine.generate("Hello!", genConfig);
            System.out.println(output);
            
            // Or stream tokens
            Iterator<String> stream = engine.generateStream("Hello!", genConfig);
            while (stream.hasNext()) {
                System.out.print(stream.next());
            }
        }
    }
}
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JINFER_HOME` | Installation and cache directory | `~/.jinfer` |
| `JINFER_JAVA_OPTS` | JVM options | `-Xmx4g -Xms512m` |

### Generation Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `maxNewTokens` | Maximum tokens to generate | 256 |
| `temperature` | Sampling temperature (higher = more random) | 1.0 |
| `topP` | Nucleus sampling threshold | 0.9 |
| `topK` | Top-K sampling limit | 50 |
| `doSample` | Enable sampling (false = greedy) | true |
| `seed` | Random seed (-1 = random) | -1 |
| `stopSequence` | Stop generation on this string | null |

## Sampling Strategies

- **Greedy**: Always selects the highest probability token. Deterministic output.
- **Temperature**: Scales logits before sampling. Higher values increase randomness.
- **Top-K**: Samples from the K most probable tokens only.
- **Top-P (Nucleus)**: Samples from smallest set of tokens with cumulative probability >= P.

## Supported Model Formats

| Format | Extension | Status |
|--------|-----------|--------|
| ONNX | `.onnx` | Supported |
| SafeTensors | `.safetensors` | Planned |
| PyTorch | `.pt`, `.bin` | Planned |

## Project Structure

```
jinfer/
├── src/main/java/com/jinfer/
│   ├── cli/           # Command-line interface
│   ├── config/        # Configuration classes
│   ├── engine/        # Core inference engine
│   ├── hub/           # HuggingFace integration
│   ├── sampling/      # Sampling strategies
│   └── tokenization/  # Tokenizer implementations
├── src/test/java/     # JUnit 4 tests
├── bin/jinfer         # Launcher script
├── Makefile           # Build & install automation
└── build.gradle.kts   # Gradle build configuration
```

## Building from Source

```bash
# Build
make build

# Run tests
make test

# Create distribution package
make dist

# Clean
make clean
```

## Installation Options

```bash
# User installation (no sudo required)
make install-user

# System-wide installation
sudo make install

# Custom prefix
sudo make PREFIX=/opt/jinfer install

# Uninstall
make uninstall

# Uninstall and remove all data
make uninstall-all
```

## Dependencies

- [Deep Java Library (DJL)](https://djl.ai/) - Deep learning framework
- [ONNX Runtime](https://onnxruntime.ai/) - Model inference backend
- [HuggingFace Tokenizers](https://huggingface.co/docs/tokenizers/) - Text tokenization
- [Picocli](https://picocli.info/) - CLI framework
- [Gson](https://github.com/google/gson) - JSON parsing
- [SLF4J](https://www.slf4j.org/) - Logging

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- HuggingFace for the model hub and tokenizers
- Amazon DJL team for the deep learning library
- Microsoft for ONNX Runtime

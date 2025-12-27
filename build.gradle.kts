plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.jinfer"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Deep Java Library (DJL) - Core API
    implementation("ai.djl:api:0.28.0")
    
    // ONNX Runtime Engine for DJL
    implementation("ai.djl.onnxruntime:onnxruntime-engine:0.28.0")
    runtimeOnly("com.microsoft.onnxruntime:onnxruntime:1.17.1")
    
    // HuggingFace Tokenizers
    implementation("ai.djl.huggingface:tokenizers:0.28.0")
    
    // CLI parsing
    implementation("info.picocli:picocli:4.7.5")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    
    // JSON parsing for config
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Testing with JUnit 4
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
}

application {
    mainClass.set("com.jinfer.cli.JInferCLI")
}

tasks.test {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.jinfer.cli.JInferCLI"
        )
    }
}

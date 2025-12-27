# JInfer Makefile
# Java LLM Inference Engine

# Configuration
PREFIX ?= /usr/local
JINFER_HOME ?= $(HOME)/.jinfer
JAR_NAME = jinfer-1.0.0-all.jar
INSTALL_JAR = jinfer.jar

# Directories
BUILD_DIR = build/libs
BIN_DIR = bin
LIB_DIR = $(JINFER_HOME)/lib
MODELS_DIR = $(JINFER_HOME)/models

# Colors
GREEN = \033[0;32m
YELLOW = \033[1;33m
NC = \033[0m

.PHONY: all build clean install uninstall test help

# Default target
all: build

# Build the project
build:
	@echo "$(GREEN)Building JInfer...$(NC)"
	./gradlew shadowJar --no-daemon
	@echo "$(GREEN)Build complete: $(BUILD_DIR)/$(JAR_NAME)$(NC)"

# Run tests
test:
	@echo "$(GREEN)Running tests...$(NC)"
	./gradlew test --no-daemon

# Clean build artifacts
clean:
	@echo "$(YELLOW)Cleaning build artifacts...$(NC)"
	./gradlew clean --no-daemon
	@echo "$(GREEN)Clean complete$(NC)"

# Install to system
install: build
	@echo "$(GREEN)Installing JInfer...$(NC)"
	
	# Create directories
	@mkdir -p $(LIB_DIR)
	@mkdir -p $(MODELS_DIR)
	@mkdir -p $(PREFIX)/bin
	
	# Copy JAR
	@cp $(BUILD_DIR)/$(JAR_NAME) $(LIB_DIR)/$(INSTALL_JAR)
	@echo "  Installed JAR to $(LIB_DIR)/$(INSTALL_JAR)"
	
	# Copy launcher script
	@cp $(BIN_DIR)/jinfer $(PREFIX)/bin/jinfer
	@chmod +x $(PREFIX)/bin/jinfer
	@echo "  Installed launcher to $(PREFIX)/bin/jinfer"
	
	@echo ""
	@echo "$(GREEN)Installation complete!$(NC)"
	@echo ""
	@echo "JInfer installed to:"
	@echo "  Executable: $(PREFIX)/bin/jinfer"
	@echo "  Library:    $(LIB_DIR)/$(INSTALL_JAR)"
	@echo "  Models:     $(MODELS_DIR)"
	@echo ""
	@echo "Usage:"
	@echo "  jinfer --help"
	@echo "  jinfer download -m microsoft/DialoGPT-small"
	@echo "  jinfer run -m microsoft/DialoGPT-small -p \"Hello\""

# Install for current user only (no sudo required)
install-user: build
	@echo "$(GREEN)Installing JInfer for current user...$(NC)"
	
	# Create directories
	@mkdir -p $(LIB_DIR)
	@mkdir -p $(MODELS_DIR)
	@mkdir -p $(HOME)/.local/bin
	
	# Copy JAR
	@cp $(BUILD_DIR)/$(JAR_NAME) $(LIB_DIR)/$(INSTALL_JAR)
	@echo "  Installed JAR to $(LIB_DIR)/$(INSTALL_JAR)"
	
	# Copy launcher script
	@cp $(BIN_DIR)/jinfer $(HOME)/.local/bin/jinfer
	@chmod +x $(HOME)/.local/bin/jinfer
	@echo "  Installed launcher to $(HOME)/.local/bin/jinfer"
	
	@echo ""
	@echo "$(GREEN)Installation complete!$(NC)"
	@echo ""
	@echo "Add to your PATH if not already (add to ~/.bashrc or ~/.zshrc):"
	@echo "  export PATH=\"\$$HOME/.local/bin:\$$PATH\""
	@echo ""
	@echo "Then run:"
	@echo "  jinfer --help"

# Uninstall from system
uninstall:
	@echo "$(YELLOW)Uninstalling JInfer...$(NC)"
	
	# Remove executable
	@rm -f $(PREFIX)/bin/jinfer
	@rm -f $(HOME)/.local/bin/jinfer
	@echo "  Removed launcher"
	
	# Remove library (keep models)
	@rm -f $(LIB_DIR)/$(INSTALL_JAR)
	@echo "  Removed JAR"
	
	@echo ""
	@echo "$(GREEN)Uninstall complete$(NC)"
	@echo ""
	@echo "Note: Cached models in $(MODELS_DIR) were preserved."
	@echo "To remove everything: rm -rf $(JINFER_HOME)"

# Uninstall and remove all data
uninstall-all: uninstall
	@echo "$(YELLOW)Removing all JInfer data...$(NC)"
	@rm -rf $(JINFER_HOME)
	@echo "$(GREEN)All JInfer data removed$(NC)"

# Create distribution package
dist: build
	@echo "$(GREEN)Creating distribution package...$(NC)"
	@mkdir -p dist
	@cp $(BUILD_DIR)/$(JAR_NAME) dist/
	@cp $(BIN_DIR)/jinfer dist/
	@cp Makefile dist/
	@cp README.md dist/ 2>/dev/null || echo "# JInfer" > dist/README.md
	@tar -czvf jinfer-1.0.0.tar.gz -C dist .
	@rm -rf dist
	@echo "$(GREEN)Distribution package: jinfer-1.0.0.tar.gz$(NC)"

# Show help
help:
	@echo "JInfer Makefile"
	@echo ""
	@echo "Targets:"
	@echo "  make build         Build the project"
	@echo "  make test          Run tests"
	@echo "  make clean         Clean build artifacts"
	@echo "  make install       Install to system (may need sudo)"
	@echo "  make install-user  Install for current user only"
	@echo "  make uninstall     Remove installation (keep models)"
	@echo "  make uninstall-all Remove installation and all data"
	@echo "  make dist          Create distribution package"
	@echo "  make help          Show this help"
	@echo ""
	@echo "Variables:"
	@echo "  PREFIX       Installation prefix (default: /usr/local)"
	@echo "  JINFER_HOME  JInfer home directory (default: ~/.jinfer)"
	@echo ""
	@echo "Examples:"
	@echo "  make build"
	@echo "  sudo make install"
	@echo "  make install-user"
	@echo "  make PREFIX=/opt/jinfer install"

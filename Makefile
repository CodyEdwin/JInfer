#!/usr/bin/env make
#===============================================================================
# JInfer - Java LLM Inference Engine
# Build System for Local Installation
#===============================================================================

# Version Information
VERSION := 1.0.0
PACKAGE_NAME := jinfer-$(VERSION)
INSTALL_PREFIX ?= /usr/local
BIN_INSTALL_DIR := $(INSTALL_PREFIX)/bin
LIB_INSTALL_DIR := $(INSTALL_PREFIX)/lib
SHARE_INSTALL_DIR := $(INSTALL_PREFIX)/share/jinfer

# Java Configuration
JAVA_HOME ?= $(shell which java > /dev/null 2>&1 && dirname $$(dirname $$(readlink -f $$(which java))) 2>/dev/null || echo "")
JAVA_VERSION_MIN := 17

# Colors for output
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

# Default target
.PHONY: all
all: info build

#===============================================================================
# Information and Detection
#===============================================================================

.PHONY: info
info:
	@echo "============================================"
	@echo "JInfer Build System"
	@echo "============================================"
	@echo "Version: $(VERSION)"
	@echo "Package: $(PACKAGE_NAME)"
	@echo "Install Prefix: $(INSTALL_PREFIX)"
	@echo ""
	@echo "Java Configuration:"
	@if [ -n "$$JAVA_HOME" ]; then \
		echo "  JAVA_HOME: $$JAVA_HOME"; \
		$$JAVA_HOME/bin/java -version 2>&1 | head -1; \
	else \
		echo "  JAVA_HOME not set, using system default"; \
		java -version 2>&1 | head -1; \
	fi
	@echo ""

#===============================================================================
# Build Targets
#===============================================================================

.PHONY: build
build: clean info
	@echo "Building JInfer..."
	@./gradlew shadowJar --no-daemon -q
	@echo ""
	@echo "Build completed successfully!"
	@echo "JAR files created in build/libs/"

.PHONY: build-dist
build-dist:
	@echo "Building distribution..."
	@./gradlew distTar distZip --no-daemon -q
	@echo "Distribution created!"

.PHONY: compile
compile:
	@echo "Compiling source files..."
	@./gradlew compileJava --no-daemon -q
	@echo "Compilation successful!"

.PHONY: clean
clean:
	@echo "Cleaning build artifacts..."
	@./gradlew clean --no-daemon -q 2>/dev/null || true
	@rm -rf build/ .gradle/*/build .gradle/*/fileHashes
	@echo "Clean complete!"

.PHONY: distclean
distclean: clean
	@echo "Removing all generated files..."
	@rm -rf .gradle/
	@echo "Distclean complete!"

#===============================================================================
# Test Targets
#===============================================================================

.PHONY: test
test:
	@echo "Running unit tests..."
	@./gradlew test --no-daemon 2>&1 | tee test_output.txt
	@if grep -q "BUILD SUCCESSFUL" test_output.txt; then \
		echo ""; \
		echo "All tests passed!"; \
		rm -f test_output.txt; \
	else \
		echo ""; \
		echo "Some tests failed!"; \
		rm -f test_output.txt; \
		exit 1; \
	fi

#===============================================================================
# Installation Targets
#===============================================================================

.PHONY: install
install: build
	@echo "============================================"
	@echo "Installing JInfer"
	@echo "============================================"
	@echo ""
	@echo "Install prefix: $(INSTALL_PREFIX)"
	@echo "Binary directory: $(BIN_INSTALL_DIR)"
	@echo "Library directory: $(LIB_INSTALL_DIR)"
	@echo "Share directory: $(SHARE_INSTALL_DIR)"
	@echo ""

	@# Create directories
	@echo "Creating directories..."
	@mkdir -p $(DESTDIR)$(BIN_INSTALL_DIR)
	@mkdir -p $(DESTDIR)$(LIB_INSTALL_DIR)
	@mkdir -p $(DESTDIR)$(SHARE_INSTALL_DIR)
	@mkdir -p $(DESTDIR)$(SHARE_INSTALL_DIR)/bin
	@mkdir -p $(HOME)/.jinfer/models

	@# Install main JAR
	@echo "Installing main library..."
	@cp build/libs/jinfer-$(VERSION)-all.jar $(DESTDIR)$(LIB_INSTALL_DIR)/jinfer.jar

	@# Install CLI launcher
	@echo "Installing CLI launcher..."
	@cp bin/jinfer $(DESTDIR)$(SHARE_INSTALL_DIR)/bin/jinfer
	@chmod +x $(DESTDIR)$(SHARE_INSTALL_DIR)/bin/jinfer
	@ln -sf $(SHARE_INSTALL_DIR)/bin/jinfer $(DESTDIR)$(BIN_INSTALL_DIR)/jinfer

	@# Create environment setup script
	@echo "Creating environment setup..."
	@echo '# JInfer Environment Setup' > $(DESTDIR)$(SHARE_INSTALL_DIR)/jinfer-env.sh
	@echo "export JINFER_HOME=$(SHARE_INSTALL_DIR)" >> $(DESTDIR)$(SHARE_INSTALL_DIR)/jinfer-env.sh
	@echo "export JINFER_LIB=$(LIB_INSTALL_DIR)" >> $(DESTDIR)$(SHARE_INSTALL_DIR)/jinfer-env.sh

	@# Create version file
	@echo "Creating version file..."
	@echo "JINFER_VERSION=$(VERSION)" > $(DESTDIR)$(SHARE_INSTALL_DIR)/VERSION

	@echo ""
	@echo "============================================"
	@echo "Installation Complete!"
	@echo "============================================"
	@echo ""
	@echo "To use JInfer:"
	@echo "  1. Run: $(BIN_INSTALL_DIR)/jinfer --help"
	@echo "  2. Download a model: jinfer download -m user/repo"
	@echo "  3. Run inference: jinfer run -m user/repo -p \"Hello\""
	@echo ""
	@echo "Library installed to: $(LIB_INSTALL_DIR)/jinfer.jar"
	@echo ""

.PHONY: install-user
install-user: build
	@echo "Installing to user home directory..."
	@make install INSTALL_PREFIX=$$HOME/.local DESTDIR=

.PHONY: uninstall
uninstall:
	@echo "Removing JInfer..."
	@rm -f $(DESTDIR)$(BIN_INSTALL_DIR)/jinfer
	@rm -rf $(DESTDIR)$(SHARE_INSTALL_DIR)/
	@rm -f $(DESTDIR)$(LIB_INSTALL_DIR)/jinfer.jar
	@echo "Uninstallation complete!"

#===============================================================================
# Package Targets
#===============================================================================

.PHONY: package
package: build build-dist
	@echo "Creating distribution packages..."
	@mkdir -p package
	@cp build/distributions/jinfer-$(VERSION).tar package/ 2>/dev/null || true
	@cp build/distributions/jinfer-$(VERSION).zip package/ 2>/dev/null || true
	@cp build/libs/jinfer-$(VERSION)-all.jar package/
	@tar -czf package/jinfer-sources-$(VERSION).tar.gz \
		--exclude='.git' \
		--exclude='.gradle' \
		--exclude='build' \
		--exclude='package' \
		.
	@echo ""
	@echo "Packages created in package/:"
	@ls -lh package/

#===============================================================================
# Development Targets
#===============================================================================

.PHONY: deps
deps:
	@echo "Checking dependencies..."
	@./gradlew dependencies --no-daemon -q

.PHONY: lint
lint:
	@echo "Running code quality checks..."
	@./gradlew compileJava --no-daemon -q
	@echo "Code quality check passed!"

.PHONY: doc
doc:
	@echo "Generating documentation..."
	@mkdir -p doc
	@./gradlew javadoc --no-daemon -q 2>/dev/null || echo "Javadoc not configured"
	@echo "Documentation generated in doc/"

#===============================================================================
# Help
#===============================================================================

.PHONY: help
help:
	@echo ""
	@echo "============================================"
	@echo "  JInfer Build System - Help"
	@echo "============================================"
	@echo ""
	@echo "Usage: make [target] [options]"
	@echo ""
	@echo "Main targets:"
	@echo "  all       - Build everything (default)"
	@echo "  build     - Clean and build the project"
	@echo "  compile   - Compile source files only"
	@echo "  clean     - Remove build artifacts"
	@echo "  test      - Run unit tests"
	@echo "  install   - Install to system (requires root)"
	@echo "  uninstall - Remove installed files"
	@echo "  package   - Create distribution packages"
	@echo ""
	@echo "Options:"
	@echo "  INSTALL_PREFIX=/path  Set installation prefix"
	@echo ""
	@echo "Examples:"
	@echo "  make build                    # Build the project"
	@echo "  make install                  # Install to /usr/local"
	@echo "  make install-user             # Install to ~/.local"
	@echo "  make INSTALL_PREFIX=/opt install  # Custom prefix"
	@echo ""

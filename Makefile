.PHONY: build jar test clean help

GRADLE := ./gradlew
VERSION ?= 0.1.3
GRADLE_VERSION := -PpluginVersion=$(VERSION)

build: ## Build, test, and package the plugin JAR.
	$(GRADLE) $(GRADLE_VERSION) build

jar: ## Package the shaded plugin JAR.
	$(GRADLE) $(GRADLE_VERSION) shadowJar

test: ## Run the test suite.
	$(GRADLE) $(GRADLE_VERSION) test

clean: ## Remove generated build files.
	$(GRADLE) clean

help: ## Show available targets.
	@awk 'BEGIN {FS = ":.*##"} /^[a-zA-Z_-]+:.*##/ {printf "%-10s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

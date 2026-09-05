.PHONY: build jar test clean help

GRADLE := ./gradlew

build: ## Build, test, and package the plugin JAR.
	$(GRADLE) build

jar: ## Package the shaded plugin JAR.
	$(GRADLE) shadowJar

test: ## Run the test suite.
	$(GRADLE) test

clean: ## Remove generated build files.
	$(GRADLE) clean

help: ## Show available targets.
	@awk 'BEGIN {FS = ":.*##"} /^[a-zA-Z_-]+:.*##/ {printf "%-10s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

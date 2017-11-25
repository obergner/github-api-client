# Include configuration from config.env
include config.env

# Define application name
APP_NAME := "github-api-client"

# Grep version from project.clj
VERSION := $(shell ./version.sh)

# Default goal 
.DEFAULT_GOAL := build

# LEININGEN TASKS

# Clean output
clean:
	lein clean

# Run tests
.PHONY: test
test:
	lein test

# Create executable uberjar
package:
	lein uberjar

# DOCKER TASKS

# Build the container
build: test package
	docker build \
         --build-arg version=$(VERSION) \
         --build-arg port=$(PORT) \
         -t $(APP_NAME):$(VERSION) .

run: ## Run container
	docker run -i -t --rm \
         --env-file=./config.env \
         --publish=$(PORT):$(PORT) \
         --name="$(APP_NAME)" \
         $(APP_NAME):$(VERSION)

up: build run ## Run container on port configured in `config.env` (Alias to run)

stop: ## Stop and remove a running container
	docker stop $(APP_NAME); docker rm $(APP_NAME)

# HELPERS

version: ## Output the current version
	@echo $(VERSION)

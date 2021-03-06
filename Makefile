# Include configuration from config.env
include config.env

# Define application name
APP_NAME := "github-api-client"

# Grep version from project.clj
VERSION := $(shell ./version.sh)

# Read our secret access token from profiles.clj
ACCESS_TOKEN := $(shell cat profiles.clj | sed -n 's/.*:gh-api-token "\(.*\)".*/\1/p')

# Default goal 
.DEFAULT_GOAL := build

# LEININGEN TASKS

# Clean output
clean:
	@lein clean

# Run tests
.PHONY: test
test:
	@lein test

# Create executable uberjar
package:
	@lein uberjar

# Create documentation
.PHONY: doc
doc:
	@lein doc

# DOCKER TASKS

# Build the container
image: package
	@docker build \
         --build-arg version=$(VERSION) \
         --build-arg port=$(PORT) \
         --build-arg managementApiPort=$(MANAGEMENT_API_PORT) \
         -t $(APP_NAME):$(VERSION) .

# Build the container
build: test container

run: ## Run container
	@docker run -i -t --rm \
         --env-file=./config.env \
         --env=GH_API_TOKEN=$(ACCESS_TOKEN) \
         --publish=$(PORT):$(PORT) \
         --publish=$(MANAGEMENT_API_PORT):$(MANAGEMENT_API_PORT) \
         --name="$(APP_NAME)" \
         $(APP_NAME):$(VERSION)

up: build run ## Run container on port configured in `config.env` (Alias to run)

stop: ## Stop and remove a running container
	@docker stop $(APP_NAME); docker rm $(APP_NAME)

# HELPERS

version: ## Output the current version
	@echo $(VERSION)

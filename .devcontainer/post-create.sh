#!/usr/bin/env zsh
# Runs once after the devcontainer is created.
# Base image's post-create.sh handles: git config, claude symlink, .claude ownership.
set -euo pipefail

# Fix shared-maven-cache volume ownership (may be root-owned from prior sessions)
sudo chown -R dev:dev /home/dev/.m2

# Java dependencies
./mvnw dependency:go-offline -B

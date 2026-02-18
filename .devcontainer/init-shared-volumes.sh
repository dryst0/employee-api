#!/usr/bin/env bash
# Creates shared Docker volumes used across all devcontainer projects.
# Runs on the HOST via initializeCommand — before the container starts.
# Idempotent: "docker volume create" is a no-op if the volume already exists.

set -euo pipefail

for volume in \
    shared-maven-cache \
    shared-npm-cache \
    shared-docker-cache \
    shared-claude-config \
    shared-gemini-config \
    shared-copilot-config \
    shared-zed-data; do
    docker volume create "$volume" 2>/dev/null || true
done

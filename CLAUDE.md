# CLAUDE.md

This file provides operational guidance to Claude Code for this repository. For knowledge about practices, patterns, technologies, and conventions, query the Memory Server.

## Memory Server Integration

At the start of each session:

1. **Load user context**: `mcp__memory__open_nodes(["Franz"])` — retrieves all practices Franz follows (TDD, BDD, SOLID, Object Calisthenics, Expand-Migrate-Contract, etc.) via relations.
2. **Load project context**: `mcp__memory__open_nodes(["Employee API"])` — retrieves project-specific decisions, package structure, and stack details.
3. **Query technologies on demand**: When working with a specific technology, use `mcp__memory__search_nodes("technology name")` to retrieve gotchas and patterns.
4. **Add learnings**: When discovering new patterns or gotchas, update the Memory Server with `mcp__memory__add_observations` so knowledge persists across sessions.

Use Memory Server queries proactively — don't wait to be asked.

## Tool Selection Strategy

### ACP Tools (Default for Code Changes)
Use `mcp__acp__Read`, `mcp__acp__Write`, `mcp__acp__Edit`, `mcp__acp__Bash` for:
- All code changes (diff preview + approval workflow)
- Git operations, build/test/run commands
- Any operation requiring user review

### Filesystem MCP Tools (Read-Only Exploration)
Use `mcp__filesystem__*` tools for:
- Bulk file reads (`read_multiple_files` for 5+ files)
- Directory exploration (`directory_tree`, `list_directory`)
- File metadata queries (`get_file_info`)

### Decision Rule
Will this operation change files or require approval?
- **Yes** → ACP tools
- **No** (read-only) → Filesystem MCP tools

## Build & Run Commands

```bash
# Build
./mvnw clean package

# Run application (starts Netty on port 8080, auto-starts PostgreSQL via Docker Compose)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=<TestClassName>

# Run a single test method
./mvnw test -Dtest=<TestClassName>#<testMethodName>

# Generate OpenAPI docs (runs during integration-test phase)
./mvnw verify

# Build container image (uses Spring Boot Buildpacks, no Dockerfile)
./mvnw spring-boot:build-image

# Run container
docker run -p 8080:8080 ghcr.io/dryst0/employee-api:0.0.1-SNAPSHOT
```

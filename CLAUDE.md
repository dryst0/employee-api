# CLAUDE.md

This file provides operational guidance to Claude Code for this repository.

## Workflow

- **Plan before executing**: Always explore the codebase and present a plan for approval before making any code changes. Never edit files immediately. Understand the scope, identify affected files, and agree on the approach first.

## Memory Server Integration

MEMORY.md contains a compact cache of practices and project context that is always in context. For detailed observations, gotchas, full rule sets, and cross-project knowledge, query the Memory Server.

### Session Bootstrap
1. **Load user context**: `mcp__memory__open_nodes(["Franz"])` — full practice observations beyond the MEMORY.md summary
2. **Load project context**: `mcp__memory__open_nodes(["Employee API"])` — project-specific decisions, stack details, and conventions

### During Work
3. **Query before coding**: Before implementing an approved plan, query MEMORY.md and Memory Server for relevant practices, technology gotchas, and patterns that apply to the work. Use `mcp__memory__search_nodes("technology name")` for specific technologies.
4. **Keep knowledge current**: When discovering new patterns, gotchas, or decisions, update Memory Server with `mcp__memory__add_observations` AND update the MEMORY.md cache if it affects a core practice or project summary. Knowledge should grow with every session.

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

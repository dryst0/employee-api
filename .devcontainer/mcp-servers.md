# MCP Server Reference

Run from **within the devcontainer** where DinD provides the Docker daemon.
All MCP servers use stdio transport (`docker run -i --rm`).
Copy the relevant command into your editor's MCP client config.

Infrastructure services (postgres, tempo, etc.) are started by
`spring-boot-docker-compose` on the same DinD daemon. MCP servers
can reach them by container name on the compose network.

## Filesystem

**Publisher**: MCP Project (Anthropic) — reference implementation

```sh
docker run -i --rm -v /workspace:/workspace mcp/filesystem /workspace
```

## Memory

**Publisher**: MCP Project (Anthropic) — reference implementation

### Option A: Per-project volume (default)

Uses the `mcp-memory` volume mounted at `~/.mcp-memory` inside the devcontainer.
Works out of the box — no configuration needed.

```sh
docker run -i --rm -v /home/developer/.mcp-memory:/data -e MEMORY_FILE_PATH=/data/memory.json mcp/memory
```

### Option B: Host directory (optional)

Bind-mount a host directory for memory that survives volume pruning.
Replace `/path/on/host` with your desired directory.

```sh
docker run -i --rm -v /path/on/host:/data -e MEMORY_FILE_PATH=/data/memory.json mcp/memory
```

## Git

**Publisher**: MCP Project (Anthropic) — reference implementation

```sh
docker run -i --rm -v /workspace:/workspace mcp/git --repository /workspace
```

## Context7

**Publisher**: Upstash — official integration in Docker MCP catalog

Provides up-to-date, version-specific documentation for libraries and frameworks.

```sh
docker run -i --rm mcp/context7
```

## Fetch

**Publisher**: MCP Project (Anthropic) — reference implementation

Fetches web content and converts HTML to markdown for LLM consumption.
Reduces token waste on raw HTML. Supports chunked reading via `start_index`.

```sh
docker run -i --rm mcp/fetch
```

## Grafana

**Publisher**: Grafana Labs — official integration

Requires a service account token. Create one in Grafana UI: Administration > Service Accounts.
First run `./mvnw spring-boot:run` to start infrastructure, then:

```sh
docker run -i --rm --network employee_default -e GRAFANA_URL=http://grafana:3000 -e GRAFANA_SERVICE_ACCOUNT_TOKEN=<token> mcp/grafana -t stdio
```

## Not Included

- **Postgres MCP** (`mcp/postgres`) — archived by MCP project, known SQL injection vulnerability. Use `psql` via Bash.
- **Docker MCP** — no official/verified server exists. Use `docker` CLI via Bash.
- **Sequential Thinking MCP** (`mcp/sequentialthinking`) — redundant for Claude Code which has Plan mode, subagents, and strong native reasoning.

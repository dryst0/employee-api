# CI Pipeline

GitHub Actions workflow triggered on every push to `main`.

```mermaid
flowchart TD
    PUSH[Push to main] --> CHECKOUT[Checkout]
    CHECKOUT --> JAVA[Set up Java 25<br/>Temurin + Maven cache]
    JAVA --> VERSION[Generate build version<br/>YYYYMMDDTHHmmss-runId-sha]
    VERSION --> BUILD[Build and test<br/>mvnw clean package<br/>-Drevision=version]
    BUILD --> REPORTS[Upload test reports<br/>surefire-reports, 7 days]
    BUILD --> LOGIN[Log in to ghcr.io<br/>GITHUB_TOKEN]
    LOGIN --> IMAGE[Build OCI image<br/>Paketo Buildpacks<br/>+ OCI metadata labels]
    IMAGE --> PUSH_IMG[Push to<br/>ghcr.io/dryst0/employee-api:version]

    style PUSH fill:#e3f2fd,stroke:#1565c0
    style BUILD fill:#fff3e0,stroke:#e65100
    style IMAGE fill:#fce4ec,stroke:#c62828
    style PUSH_IMG fill:#e8f5e9,stroke:#2e7d32
```

## Version Pattern

```
<UTC timestamp>-<workflow run ID>-<short commit SHA>
```

Example: `20260220T103045-12345678-abc1234`

This version flows through:
- **JAR filename** — `employee-<version>.jar`
- **JAR manifest** — `Implementation-Version: <version>`
- **OCI image tag** — `ghcr.io/dryst0/employee-api:<version>`
- **OCI label** — `org.opencontainers.image.version`

## OCI Image Labels

| Label | Source |
|-------|--------|
| `org.opencontainers.image.title` | Paketo auto-detection (artifact name) |
| `org.opencontainers.image.version` | Build version (from `-Drevision`) |
| `org.opencontainers.image.source` | Repository URL |
| `org.opencontainers.image.revision` | Full commit SHA (40 chars) |
| `org.opencontainers.image.created` | RFC 3339 build timestamp |

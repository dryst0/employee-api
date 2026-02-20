# CI Pipeline

GitHub Actions workflow with two jobs, triggered on push to `main` and pull requests targeting `main`. Only runs when source, build, or workflow files change.

```mermaid
flowchart TD
    TRIGGER{Push to main<br/>or Pull Request} --> TEST_JOB

    subgraph TEST_JOB [Test Job]
        CHECKOUT_T[Checkout] --> JAVA_T[Set up Java 25<br/>Temurin + Maven cache]
        JAVA_T --> VERSION[Generate build version<br/>timestamp-runId-sha]
        VERSION --> BUILD[Build and test<br/>mvnw clean package]
        BUILD --> REPORTS[Upload test reports<br/>surefire-reports, 7 days]
    end

    TEST_JOB --> GATE{Push only?}
    GATE -- Yes --> PUBLISH_JOB
    GATE -- No / PR --> SKIP[Publish skipped]

    subgraph PUBLISH_JOB [Publish Job]
        CHECKOUT_P[Checkout] --> JAVA_P[Set up Java 25<br/>Temurin + Maven cache]
        JAVA_P --> LOGIN[Log in to ghcr.io]
        LOGIN --> IMAGE[Build and push OCI image<br/>Paketo Buildpacks<br/>+ OCI metadata labels]
    end

    style TRIGGER fill:#e3f2fd,stroke:#1565c0
    style TEST_JOB fill:#fff3e0,stroke:#e65100
    style PUBLISH_JOB fill:#fce4ec,stroke:#c62828
    style GATE fill:#f3e5f5,stroke:#6a1b9a
    style SKIP fill:#eceff1,stroke:#607d8b
```

## Triggers

| Event | Branches | Path filters |
|-------|----------|-------------|
| `push` | `main` | `src/**`, `pom.xml`, `.mvn/**`, `mvnw`, `.github/workflows/ci.yml` |
| `pull_request` | `main` | Same as push |

## Concurrency

One run at a time per branch or PR. New pushes cancel in-progress runs.

- Push runs: grouped by `refs/heads/main`
- PR runs: grouped by PR number (won't cancel main branch runs)

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

## Supply Chain Security

- All GitHub Actions pinned to full commit SHAs (not version tags)
- [Dependabot](../.github/dependabot.yml) updates Maven and GitHub Actions dependencies weekly, grouped into one PR per ecosystem

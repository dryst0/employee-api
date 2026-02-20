# Employee API

A reactive RESTful API built with Spring Boot, created as a Kata to consolidate practices learned from weekly Katas at IONOS — TDD, hexagonal architecture, observability, and clean code disciplines applied end-to-end.

Built through modern pair programming: an AI agent drives (writes the code) while the human navigates (steers direction, reviews decisions, and enforces quality).

## Tech Stack

- Java 25, Spring Boot 3.5.11, Spring WebFlux, Spring Data R2DBC
- PostgreSQL 17.8, Flyway migrations
- Maven 3.9.12, Spring Boot Buildpacks (Paketo)
- Log4j2, Micrometer Tracing, OpenTelemetry, Prometheus, Grafana, Tempo

## Architecture

Hexagonal Architecture (Ports & Adapters), enforced by [ArchUnit tests](src/test/java/com/jfi/api/ApplicationTest.java).

```
com.jfi.api.employee/
├── domain/                          # Domain model
├── port/in/                         # Driving ports
├── port/out/                        # Driven ports
├── usecase/                         # Use case implementations
└── adapter/
    ├── in/rest/                     # REST adapter
    └── out/persistence/             # Persistence adapter

com.jfi.api.infrastructure/          # Cross-cutting concerns
```

See [Architecture diagram](docs/architecture.md) for the full component view and dependency rules.

## Getting Started

**Prerequisites:** Java 25, Docker (for PostgreSQL and observability stack)

```bash
# Run application (starts Netty on port 8080, auto-starts PostgreSQL via Docker Compose)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=EmployeeServiceImplTest

# Build
./mvnw clean package

# Build container image
./mvnw spring-boot:build-image
```

## CI/CD

GitHub Actions workflow with two jobs: **test** and **publish**. Triggered on push to `main` and pull requests, with path filtering so docs-only changes don't trigger builds. PRs only run tests; image publishing is push-to-main only.

Actions are pinned to commit SHAs for supply chain security. [Dependabot](.github/dependabot.yml) keeps Maven and GitHub Actions dependencies up to date weekly.

See [CI pipeline diagram](docs/ci-pipeline.md) for the full workflow and version tagging details.

## Observability

Three-pillar observability: structured logging (Log4j2 + MDC), metrics (Prometheus + Grafana), and distributed tracing (Micrometer Tracing + OTel → Tempo).

Every request is correlated with `requestId`, `traceId`, and `spanId` across the reactive chain. Sampling and log levels are profile-aware (dev/staging/prod).

See [Observability diagram](docs/observability.md) for the full stack view and profile configuration.

## API Documentation

With the application running:

- **Swagger UI:** http://localhost:8080/webjars/swagger-ui/index.html
- **OpenAPI spec:** http://localhost:8080/v3/api-docs

Generate the OpenAPI spec as a file:

```bash
./mvnw verify
# Output: target/openapi.json
```

## Lessons Learned

Gotchas and insights from building this API.

### WebFlux is overkill for CRUD

Spring WebFlux adds significant complexity (reactive types everywhere, context propagation gotchas, MDC workarounds, reactive test utilities) with no real benefit for standard request/response workloads. Spring MVC with virtual threads would have been the pragmatic choice. WebFlux is justified only for streaming use cases — SSE, WebSockets, change streams, or backpressure-sensitive pipelines.

### Reactive context propagation is not automatic

`spring.reactor.context-propagation=auto` must be set explicitly. Without it, `traceId` and `spanId` are not auto-populated into MDC, and log correlation silently breaks. Every reactive operator that needs MDC access must use `Mono.deferContextual()`.

### Log4j2 requires a global Logback exclusion

Excluding `spring-boot-starter-logging` from a single starter is fragile — Maven resolution order determines which starter pulls Logback first. The reliable approach is to declare the bare `spring-boot-starter` with the exclusion, which globally prevents Logback from leaking through any other starter.

### Never use `MDC.clear()` in reactive code

`MDC.clear()` wipes auto-propagated values like `traceId` and `spanId` from context propagation. Use `MDC.remove(key)` for owned keys only.

### Flyway needs JDBC even in an R2DBC stack

Flyway does not support R2DBC. Add the JDBC driver (`org.postgresql:postgresql`) alongside `r2dbc-postgresql`. Spring Boot auto-configures a separate JDBC DataSource for Flyway while the application uses R2DBC.

### Testcontainers R2DBC module is easy to forget

`org.testcontainers:r2dbc` is required in addition to `org.testcontainers:postgresql` for `@ServiceConnection` to wire the R2DBC `ConnectionFactory`. Without it, the test container starts but the application can't connect.

### Testcontainers Ryuk is broken on macOS Docker Desktop

TCP FIN signals don't reach the Ryuk container inside the VM, so containers are never cleaned up. Use Spring Boot's `@TestConfiguration` + `@Bean` + `@ServiceConnection` instead of JUnit's `@Testcontainers`/`@Container` — Spring's lifecycle post-processor calls `container.close()` directly.

### Don't name adapters `*RepositoryImpl`

Spring Data's `*Impl` convention clashes with custom repository implementations. Name outbound adapters `*Adapter` with `@Component` instead of `@Repository`.

### Framework-rejected requests bypass controllers

Requests rejected by the framework (405 Method Not Allowed, unmapped 404s) never reach `@RestController` methods or `@ControllerAdvice`. Only a `WebFilter` sees them — this is why `RequestLoggingFilter` exists at the filter level.

### Grafana Tempo must be pinned for local dev

Tempo v2.10+ defaults to a Kafka-based ingest path, causing `InstancesCount <= 0` errors in single-binary mode. Pin to 2.6.1 and set `ingester.lifecycler.ring.replication_factor=1` for single-instance deployments.

### Paketo CDS is broken on Java 25

The AOT cache step exits with code 1 despite success. Disabled via `BP_JVM_CDS_ENABLED=false` until [paketo-buildpacks/spring-boot#581](https://github.com/paketo-buildpacks/spring-boot/issues/581) is resolved.

### Flyway does not support PostgreSQL 18

Flyway 11.7.2 (managed by Spring Boot 3.5) warns that PostgreSQL 18 is untested. Pinned to PostgreSQL 17.8 — keep docker-compose and Testcontainers config in sync since they are coupled.

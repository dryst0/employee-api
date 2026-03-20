# CLAUDE.md

## Project Overview

Employee API — a RESTful CRUD API built with Java 25, Spring Boot 3.5, Spring WebFlux, Spring Data R2DBC, and PostgreSQL. Packaged as a container image using Spring Boot Buildpacks.

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

# Run load test smoke (single user, validates script runs correctly)
docker run --rm --network=host -v $(pwd)/load-test:/load-test grafana/k6 run /load-test/smoke.js

# Run load test (ramp-up to 50 VUs, ~2 min)
docker run --rm --network=host -v $(pwd)/load-test:/load-test grafana/k6 run /load-test/load.js
```

## Architecture

Hexagonal Architecture (Ports & Adapters), enforced by ArchUnit tests.

### Package Structure

```
com.jfi.api.employee/
  domain/          # Domain model
  port/in/         # Driving ports (interfaces)
  port/out/        # Driven ports (interfaces)
  usecase/         # Use case implementations (@Service)
  adapter/in/rest/ # Inbound REST adapter (@RestController)
  adapter/out/persistence/  # Outbound persistence adapter (@Component)

com.jfi.api.infrastructure/  # Cross-cutting concerns (filters, aspects, logging)
```

### Dependency Rules

Dependencies point inward: adapters → ports/use cases → domain. Inbound adapters must not depend on outbound adapters.

## Coding Conventions

- **Services**: interface + `*Impl` (`@Service`)
- **Outbound adapters**: `*Adapter` (`@Component`, not `@Repository`)
- **DTOs**: Java Records for API request/response objects — decoupled from persistence model
- **Domain exceptions**: `EmployeeException` base class → `EmployeeNotFoundException`, `InvalidEmployeeException`. Mapped to RFC 7807 ProblemDetail in REST adapter exception handler
- **Validation**: REST adapter validates DTO structure (`@NotBlank`, `@NotNull`), use case validates domain rules. Both layers validate independently

## Testing Conventions

- **Unit tests**: Fakes (in-memory implementations) + StepVerifier for reactive streams. No mocks for owned collaborators
- **Integration tests**: `@SpringBootTest` + Testcontainers with `@ServiceConnection`. Pre-populate test data via `DatabaseClient`, test through the port interface. Suffix: `*IT.java`
- **Architecture tests**: ArchUnit enforces hexagonal dependency rules
- **DTO tests**: bean-matchers validates getters/setters/equals/hashCode

## Observability

- **Logging**: Log4j2 + MDC (`requestId`, `traceId`, `spanId`). `RequestIdFilter` (ID lifecycle) + `RequestLoggingFilter` (HTTP logging) + `LoggingAspect` (composable named pointcuts)
- **Metrics**: Prometheus + Grafana. Micrometer registry exposed via Actuator `/metrics`
- **Tracing**: Micrometer Tracing + OTel bridge → Grafana Tempo (OTLP port 4318). `@Observed` on service + persistence layers. Trace hierarchy: HTTP → `employee.service` → `employee.persistence`
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workflow

- **Plan before executing**: Always explore the codebase and present a plan for approval before making any code changes. Never edit files immediately. Understand the scope, identify affected files, and agree on the approach first.

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

## Architecture

This is a **Spring Boot 3.5** reactive microservice using **Java 25**.

### Reactive Stack

The entire stack is non-blocking: **Spring WebFlux** (Netty server) + **Spring Data R2DBC** (PostgreSQL) + **Project Reactor** (`Mono<T>` / `Flux<T>` return types). All service and repository methods must return reactive types.

**When to use WebFlux vs Spring MVC + Virtual Threads**: WebFlux is justified only for streaming use cases — SSE/live feeds, WebSockets, change streams, tailable cursors, backpressure-sensitive pipelines, and high-concurrency gateways (500k+ connections). For standard request/response CRUD APIs, Spring MVC with virtual threads (Java 21+) provides identical scalability with simpler code, readable stack traces, natural MDC propagation, and no reactive ceremony. A reactive database driver (R2DBC, MongoDB Reactive Streams) does not require WebFlux — blocking drivers with virtual threads perform equally well for bounded queries. The web framework and database driver choices are independent decisions.

### Hexagonal Architecture (Ports & Adapters, Enforced by ArchUnit)

Organized by domain entity, then by hexagonal role. Each entity package is self-contained.

Application-level package (`com.jfi.api`):
- `infrastructure/` — Cross-cutting concerns shared across all entities: filters, aspects, logging infrastructure

Package structure per entity (`com.jfi.api.employee`):
- `domain/` — Domain models and enums. No framework dependencies.
- `port/in/` — Driving ports (interfaces called by inbound adapters)
- `port/out/` — Driven ports (interfaces implemented by outbound adapters)
- `usecase/` — Use case implementations. Depends on ports only.
- `adapter/in/rest/` — Inbound adapter: REST controller + DTOs + exception handler
- `adapter/out/persistence/` — Outbound adapter: repository adapter delegates to Spring Data `ReactiveCrudRepository`

Dependency rules (enforced via ArchUnit):
- **Domain** must not depend on adapters or use cases
- **Ports** must not depend on adapters
- **Use cases** depend on ports only, not adapters
- **Inbound adapters** must not depend on outbound adapters (and vice versa)
- Dependencies always point inward: adapters → ports/use cases → domain

### Conventions

- **Interface-based design**: Services define an interface and a separate `*Impl` class annotated with `@Service`. Outbound adapters use `*Adapter` classes annotated with `@Component` (not `@Repository`, which conflicts with Spring Data repository scanning).
- **DTO separation**: API responses use DTOs, not entity classes directly.
- **Lombok**: Use only for data/structure classes (DTOs, entities, value objects) where `@Data`, `@Builder`, `@Value` eliminate pure boilerplate. Do **not** use Lombok to generate constructors in services, controllers, or any class with dependency injection — write explicit constructors so the wiring is visible. Lombok can silently add unintended constructor parameters if non-dependency `final` fields are added.
- **Java Records vs Lombok**: Prefer Java Records for immutable data carriers (DTOs, value objects) that don't need builders, setters, or a no-arg constructor. Records are a language feature with no annotation processor dependency, avoiding JDK compatibility issues. Use Lombok (`@Data`, `@Builder`) when you need mutability, builders, or framework requirements like no-arg constructors (e.g., entities mapped by R2DBC/JPA).
- **Error handling**: Uses Spring Boot's native `ProblemDetail` for RFC 7807 Problem JSON responses (no external library needed).
- **API docs**: SpringDoc OpenAPI auto-generates Swagger UI at `/swagger-ui.html`.
- **Logging**: Log4j2 with Disruptor for async logging (spring-boot-starter-logging is excluded).
- **Log levels**: DEBUG for method-level tracing (e.g., AOP aspect). INFO for successful operations, handled client errors (4xx), and expected failures. WARN for errors the application recovers from. ERROR only for unhandled failures (5xx). Use typed checks like `HttpStatusCode.is5xxServerError()` instead of magic numbers.
- **No magic numbers or strings**: Use framework-provided constants, enums, or typed methods instead of raw numeric literals or string literals (e.g., `status.is5xxServerError()` not `code >= 500`; `RequestIdFilter.REQUEST_ID_KEY` not `"requestId"`).
- **Monitoring**: Spring Boot Actuator is included for health/metrics endpoints.
- **Database**: PostgreSQL with Spring Data R2DBC. The `Employee` entity uses `@Table`/`@Id` annotations. Schema managed via `schema.sql` with `spring.sql.init.mode=always`.
- **Docker Compose**: `compose.yaml` at project root defines the PostgreSQL container. Spring Boot Docker Compose support (`spring-boot-docker-compose`) auto-starts/stops the container with `./mvnw spring-boot:run`.

### Git Conventions

- **Conventional Commits**: Use the format `type(scope): description` (e.g., `feat(employee): add GET endpoints`, `fix(dto): correct default employee type`, `refactor(usecase): extract mapping logic`). Common types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`.
- **Commit messages**: Keep short and concise. The subject line should convey **why** the change was made. Include **what** changed only when not obvious from the diff — if the diff speaks for itself, don't repeat it in the message.
- **Small green commits**: Always commit small, incremental changes that are green (all tests pass). Never commit large changes in one go. Each commit should be a single logical unit — one feature, one refactor step, or one bug fix.
- **Only commit on green**: Never commit failing tests. The TDD RED phase (failing test) is transient local working state — commit only once the test passes (GREEN). The RED-GREEN gap lives in your working tree, not in version control.
- **Semantic Versioning**: Use `MAJOR.MINOR.PATCH` for releases. Use `-SNAPSHOT` suffix during development. Bump MAJOR for breaking changes, MINOR for new features, PATCH for bug fixes. The POM version is used as the container image tag.

### Design Principles

- **Pragmatism**: Prefer officially supported, well-documented solutions over marginal gains. When choosing between tools, libraries, or approaches, weigh the trade-off between benefit and maintenance/compatibility cost. A small improvement isn't worth it if it takes you off the supported path. Validate inputs and assumptions at system boundaries (user input, external APIs, configuration), but trust internal code and framework guarantees once validated. If a check detects an unrecoverable state (invalid configuration, missing critical dependency, corrupted data), fail fast — terminate immediately rather than continue in a broken state.
- **SOLID Principles**: Follow Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion across all code.
- **Object Calisthenics**: Apply these constraints — small methods, minimal indentation levels, first-class collections, no getters/setters exposing internals unnecessarily, wrap primitives that carry domain meaning, keep classes small and focused.
- **Rule of Three**: Only extract duplicate code into a shared abstraction when it appears more than two times. Two occurrences are acceptable — premature extraction adds unnecessary indirection.
- **Transformation Priority Premise**: When refactoring, prefer simpler transformations over complex ones (e.g., constant → scalar → direct replacement → conditional → iteration → recursion). Apply transformations incrementally in order of priority to arrive at cleaner solutions.
- **Expand-Migrate-Contract**: When replacing an implementation, don't modify or delete the existing one in-place. First *expand* by introducing the new implementation alongside the old. Then *migrate* by switching consumers to the new implementation. Finally *contract* by removing the old implementation. Each step is a separate green commit. This applies to code (repository implementations, service swaps) and to database schema migrations (add new column → backfill/migrate data → drop old column).

### Testing Patterns

- **TDD (Red-Green-Refactor)**: Always write a failing test first (Red), then write the minimum production code to make it pass (Green), then refactor. Never introduce new behavior without a failing test driving it.
- **BDD approach**: Tests should describe and verify behaviors, not implementation details. Structure tests around what the system does (given/when/then), not how it does it. Test names should read as behavior specifications.
- **Fakes over mocks**: Default to fakes (in-memory implementations of interfaces) for collaborators you own — they test real behavior and don't break when internals are refactored. Use mocks only for boundaries you don't own (external APIs, third-party libraries) or to simulate failure scenarios.
- **Unit tests**: Fakes + StepVerifier for reactive service/use case tests.
- **Integration tests**: `@SpringBootTest` + Testcontainers with `@ServiceConnection` for real database testing. Pre-populate test data via `DatabaseClient`, test through the port interface.
- **Architecture tests**: ArchUnit enforces hexagonal dependency rules.
- **DTO tests**: `bean-matchers` validates getters/setters/equals/hashCode.

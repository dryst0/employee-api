# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./mvnw clean package

# Run application (starts Netty on port 8080)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=<TestClassName>

# Run a single test method
./mvnw test -Dtest=<TestClassName>#<testMethodName>

# Generate OpenAPI docs (runs during integration-test phase)
./mvnw verify
```

## Architecture

This is a **Spring Boot 3.5** reactive microservice using **Java 25**.

### Reactive Stack

The entire stack is non-blocking: **Spring WebFlux** (Netty server) + **R2DBC** (reactive database) + **Project Reactor** (`Mono<T>` / `Flux<T>` return types). All service and repository methods must return reactive types.

### Hexagonal Architecture (Ports & Adapters, Enforced by ArchUnit)

Organized by domain entity, then by hexagonal role. Each entity package is self-contained.

Package structure per entity:
- `domain/` — Domain models and enums. No framework dependencies.
- `port/in/` — Driving ports (interfaces called by inbound adapters)
- `port/out/` — Driven ports (interfaces implemented by outbound adapters)
- `usecase/` — Use case implementations. Depends on ports only.
- `adapter/in/rest/` — Inbound adapter: REST controller + DTOs
- `adapter/out/persistence/` — Outbound adapter: repository implementations

Dependency rules (enforced via ArchUnit):
- **Domain** must not depend on adapters or use cases
- **Ports** must not depend on adapters
- **Use cases** depend on ports only, not adapters
- **Inbound adapters** must not depend on outbound adapters (and vice versa)
- Dependencies always point inward: adapters → ports/use cases → domain

### Conventions

- **Interface-based design**: Services and repositories define an interface and a separate `*Impl` class annotated with `@Service`/`@Repository`.
- **DTO separation**: API responses use DTOs, not entity classes directly.
- **Lombok**: Use only for data/structure classes (DTOs, entities, value objects) where `@Data`, `@Builder`, `@Value` eliminate pure boilerplate. Do **not** use Lombok to generate constructors in services, controllers, or any class with dependency injection — write explicit constructors so the wiring is visible. Lombok can silently add unintended constructor parameters if non-dependency `final` fields are added.
- **Java Records vs Lombok**: Prefer Java Records for immutable data carriers (DTOs, value objects) that don't need builders, setters, or a no-arg constructor. Records are a language feature with no annotation processor dependency, avoiding JDK compatibility issues. Use Lombok (`@Data`, `@Builder`) when you need mutability, builders, or framework requirements like no-arg constructors (e.g., entities mapped by R2DBC/JPA).
- **Error handling**: Uses Spring Boot's native `ProblemDetail` for RFC 7807 Problem JSON responses (no external library needed).
- **API docs**: SpringDoc OpenAPI auto-generates Swagger UI at `/swagger-ui.html`.
- **Logging**: Log4j2 with Disruptor for async logging (spring-boot-starter-logging is excluded).
- **Monitoring**: Spring Boot Actuator is included for health/metrics endpoints.

### Git Conventions

- **Conventional Commits**: Use the format `type(scope): description` (e.g., `feat(employee): add GET endpoints`, `fix(dto): correct default employee type`, `refactor(usecase): extract mapping logic`). Common types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`.
- **Commit messages**: Keep short and concise. Always explain **why** the change was made. Include **what** only when not obvious from the diff.

### Design Principles

- **SOLID Principles**: Follow Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion across all code.
- **Object Calisthenics**: Apply these constraints — small methods, minimal indentation levels, first-class collections, no getters/setters exposing internals unnecessarily, wrap primitives that carry domain meaning, keep classes small and focused.
- **Transformation Priority Premise**: When refactoring, prefer simpler transformations over complex ones (e.g., constant → scalar → direct replacement → conditional → iteration → recursion). Apply transformations incrementally in order of priority to arrive at cleaner solutions.

### Testing Patterns

- **TDD (Red-Green-Refactor)**: Always write a failing test first (Red), then write the minimum production code to make it pass (Green), then refactor. Never introduce new behavior without a failing test driving it.
- **BDD approach**: Tests should describe and verify behaviors, not implementation details. Structure tests around what the system does (given/when/then), not how it does it. Test names should read as behavior specifications.
- **Fakes over mocks**: Default to fakes (in-memory implementations of interfaces) for collaborators you own — they test real behavior and don't break when internals are refactored. Use mocks only for boundaries you don't own (external APIs, third-party libraries) or to simulate failure scenarios.
- **Unit tests**: Fakes + StepVerifier for reactive service/use case tests.
- **Integration tests**: `@SpringBootTest` for full context loading.
- **Architecture tests**: ArchUnit enforces hexagonal dependency rules.
- **DTO tests**: `bean-matchers` validates getters/setters/equals/hashCode.

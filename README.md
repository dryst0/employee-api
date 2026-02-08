# Employee API

A reactive REST microservice for managing employees, built with Spring Boot 3.5 and Java 25.

## Tech Stack

- **Java 25** with **Spring Boot 3.5.10**
- **Spring WebFlux** (Netty) — non-blocking reactive HTTP
- **R2DBC** — reactive database access
- **Project Reactor** — `Mono<T>` / `Flux<T>` reactive types
- **SpringDoc OpenAPI 2.8** — auto-generated Swagger UI
- **Log4j2 + LMAX Disruptor** — async logging
- **Spring Boot Actuator** — health and metrics endpoints
- **Lombok** — boilerplate reduction for domain entities
- **ArchUnit** — architecture rule enforcement

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.9+ (or use the included Maven wrapper)

### Build

```bash
./mvnw clean package
```

### Run

```bash
./mvnw spring-boot:run
```

The application starts on **http://localhost:8080**.

### Test

```bash
# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=EmployeeServiceImplTests

# Run a single test method
./mvnw test -Dtest=EmployeeServiceImplTests#testMethodName
```

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/employees` | List all employees |
| GET | `/employees/{uuid}` | Get employee by ID |

Error responses use RFC 7807 Problem Detail format (e.g., 404 when employee not found).

### API Documentation

Swagger UI is available at **http://localhost:8080/swagger-ui.html** when the application is running.

To generate the OpenAPI spec as a build artifact:

```bash
./mvnw verify
```

## Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters), organized by domain:

```
com.jfi.api
├── Application.java
└── employee
    ├── domain/                          # Domain models (Employee, EmployeeType)
    ├── port/
    │   ├── in/                          # Driving ports (EmployeeService)
    │   └── out/                         # Driven ports (EmployeeRepository)
    ├── usecase/                         # Use case implementations (EmployeeServiceImpl)
    └── adapter/
        ├── in/rest/                     # REST controller, DTOs, error handling
        └── out/persistence/             # Repository implementations
```

Dependencies always point inward: **adapters → ports/use cases → domain**. This is enforced at build time by ArchUnit tests.

## Testing

Tests follow TDD (Red-Green-Refactor) and BDD conventions (given/when/then), preferring **fakes over mocks** for collaborators the project owns:

- **Unit tests** — Fakes + StepVerifier for reactive assertions
- **Architecture tests** — ArchUnit enforces hexagonal dependency rules
- **DTO tests** — validates record contracts and domain-to-DTO mapping

## Container

Build the container image using Spring Boot Buildpacks (no Dockerfile needed):

```bash
./mvnw spring-boot:build-image
```

Run the container:

```bash
docker run -p 8080:8080 ghcr.io/dryst0/employee-api:0.0.1-SNAPSHOT
```

Push to GitHub Container Registry:

```bash
docker push ghcr.io/dryst0/employee-api:0.0.1-SNAPSHOT
```

## License

This project is for demonstration purposes.

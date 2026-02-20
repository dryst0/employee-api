# Architecture

Hexagonal Architecture (Ports & Adapters), enforced by [ArchUnit tests](../src/test/java/com/jfi/api/ApplicationTest.java).

```mermaid
graph LR
    subgraph Inbound
        HTTP[HTTP Client]
    end

    subgraph Adapters In
        REST[EmployeeRESTController<br/>REST Adapter]
    end

    subgraph Ports In
        SVC_PORT[EmployeeService<br/>Driving Port]
    end

    subgraph Use Cases
        SVC_IMPL[EmployeeServiceImpl<br/>Service]
    end

    subgraph Ports Out
        PERSIST_PORT[EmployeePersistence<br/>Driven Port]
    end

    subgraph Adapters Out
        PERSIST[EmployeePersistenceAdapter<br/>Persistence Adapter]
    end

    subgraph Domain
        EMP[Employee]
        EMP_TYPE[EmployeeType]
        EMP_EX[EmployeeException]
    end

    subgraph Infrastructure
        DB[(PostgreSQL)]
    end

    HTTP --> REST
    REST --> SVC_PORT
    SVC_PORT --> SVC_IMPL
    SVC_IMPL --> PERSIST_PORT
    PERSIST_PORT --> PERSIST
    PERSIST --> DB
    SVC_IMPL --> EMP
    PERSIST --> EMP

    style Domain fill:#e8f5e9,stroke:#2e7d32
    style Ports In fill:#e3f2fd,stroke:#1565c0
    style Ports Out fill:#e3f2fd,stroke:#1565c0
    style Use Cases fill:#fff3e0,stroke:#e65100
    style Adapters In fill:#fce4ec,stroke:#c62828
    style Adapters Out fill:#fce4ec,stroke:#c62828
```

## Package Structure

```
com.jfi.api.employee/
├── domain/                          # Domain model (Employee, EmployeeType, exceptions)
├── port/
│   ├── in/                          # Driving ports (EmployeeService interface)
│   └── out/                         # Driven ports (EmployeePersistence interface)
├── usecase/                         # Use case implementations (EmployeeServiceImpl)
└── adapter/
    ├── in/rest/                     # REST adapter (controller, DTOs, exception handler)
    └── out/persistence/             # Persistence adapter (R2DBC repository)

com.jfi.api.infrastructure/          # Cross-cutting: filters, aspects, observability config
```

## Dependency Rules

Dependencies point **inward**: adapters → ports/use cases → domain.

| Rule | Constraint |
|------|-----------|
| Domain isolation | Domain depends on nothing else |
| Port isolation | Ports do not depend on adapters |
| Use case isolation | Use cases depend on ports, not adapters |
| Adapter separation | Inbound adapters do not depend on outbound adapters |

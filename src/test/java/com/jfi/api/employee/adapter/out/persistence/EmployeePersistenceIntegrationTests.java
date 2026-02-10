package com.jfi.api.employee.adapter.out.persistence;

import com.jfi.api.employee.domain.EmployeeType;
import com.jfi.api.employee.port.out.EmployeePersistence;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class EmployeePersistenceIntegrationTests {

    @Autowired
    private EmployeePersistence employeePersistence;

    @Autowired
    private DatabaseClient databaseClient;

    private final UUID workerUuid = UUID.randomUUID();
    private final UUID managerUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        databaseClient.sql("DELETE FROM employee").then().block();
        databaseClient
            .sql(
                "INSERT INTO employee (uuid, first_name, last_name, employee_type) VALUES (:uuid, :firstName, :lastName, :employeeType)"
            )
            .bind("uuid", workerUuid)
            .bind("firstName", "Juan")
            .bind("lastName", "dela Cruz")
            .bind("employeeType", EmployeeType.WORKER.name())
            .then()
            .block();
        databaseClient
            .sql(
                "INSERT INTO employee (uuid, first_name, last_name, employee_type) VALUES (:uuid, :firstName, :lastName, :employeeType)"
            )
            .bind("uuid", managerUuid)
            .bind("firstName", "Maria")
            .bind("lastName", "Santos")
            .bind("employeeType", EmployeeType.MANAGER.name())
            .then()
            .block();
    }

    @Test
    void givenEmployeesExist_whenGetEmployees_thenReturnsAll() {
        StepVerifier.create(employeePersistence.getEmployees())
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void givenEmployeeExists_whenGetEmployeeById_thenReturnsEmployee() {
        StepVerifier.create(employeePersistence.getEmployeeById(workerUuid))
            .expectNextMatches(
                employee ->
                    employee.getFirstName().equals("Juan") &&
                    employee.getLastName().equals("dela Cruz") &&
                    employee.getEmployeeType() == EmployeeType.WORKER
            )
            .verifyComplete();
    }

    @Test
    void givenNoEmployee_whenGetEmployeeById_thenReturnsEmpty() {
        StepVerifier.create(
            employeePersistence.getEmployeeById(UUID.randomUUID())
        ).verifyComplete();
    }
}

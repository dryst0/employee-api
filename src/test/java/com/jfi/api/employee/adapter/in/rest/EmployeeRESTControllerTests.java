package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EmployeeRESTControllerTests {

    FakeEmployeeService employeeService;
    EmployeeRESTController controller;

    Employee worker;
    Employee manager;

    @BeforeEach
    void setup() {
        employeeService = new FakeEmployeeService();
        controller = new EmployeeRESTController(employeeService);

        worker = Employee.builder()
            .uuid(UUID.randomUUID())
            .firstName("Juan")
            .lastName("dela Cruz")
            .employeeType(EmployeeType.WORKER)
            .build();
        manager = Employee.builder()
            .uuid(UUID.randomUUID())
            .firstName("Maria")
            .lastName("Santos")
            .employeeType(EmployeeType.MANAGER)
            .build();

        employeeService.save(worker);
        employeeService.save(manager);
    }

    @Test
    void givenEmployeesExist_whenGetAllEmployees_thenReturnAllDTOs() {
        // when / then
        StepVerifier.create(controller.getAllEmployees())
            .expectNextMatches(
                dto ->
                    dto.uuid().equals(worker.getUuid()) &&
                    dto.firstName().equals("Juan")
            )
            .expectNextMatches(
                dto ->
                    dto.uuid().equals(manager.getUuid()) &&
                    dto.firstName().equals("Maria")
            )
            .verifyComplete();
    }

    @Test
    void givenEmployeeExists_whenGetById_thenReturnDTO() {
        // given
        UUID workerId = worker.getUuid();

        // when / then
        StepVerifier.create(controller.getEmployeeById(workerId))
            .expectNextMatches(
                dto ->
                    dto.uuid().equals(workerId) &&
                    dto.firstName().equals("Juan") &&
                    dto.lastName().equals("dela Cruz") &&
                    dto.employeeType() == EmployeeType.WORKER
            )
            .verifyComplete();
    }

    @Test
    void givenEmployeeDoesNotExist_whenGetById_thenError() {
        // given
        UUID unknownId = UUID.randomUUID();

        // when / then
        StepVerifier.create(controller.getEmployeeById(unknownId))
            .expectError(EmployeeNotFoundException.class)
            .verify();
    }
}

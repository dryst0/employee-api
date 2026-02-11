package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeNotFoundException;
import com.jfi.api.employee.domain.EmployeeType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

class EmployeeRESTControllerTest {

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
    void givenEmployeesExist_whenGetAllEmployees_thenReturnAllEmployees() {
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
    void givenEmployeeExists_whenGetById_thenReturnEmployee() {
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
    void givenValidEmployee_whenCreated_thenReturnsCreatedEmployee() {
        // given
        EmployeeRequest request = new EmployeeRequest(
            "Pedro",
            "Garcia",
            EmployeeType.WORKER
        );

        // when / then
        StepVerifier.create(controller.createEmployee(request))
            .expectNextMatches(
                response ->
                    response.getStatusCode() == HttpStatus.CREATED &&
                    response.getBody() != null &&
                    response.getBody().firstName().equals("Pedro") &&
                    response.getBody().lastName().equals("Garcia")
            )
            .verifyComplete();
    }

    @Test
    void givenValidEmployee_whenCreated_thenReturnedWithLocation() {
        // given
        EmployeeRequest request = new EmployeeRequest(
            "Pedro",
            "Garcia",
            EmployeeType.WORKER
        );

        // when / then
        StepVerifier.create(controller.createEmployee(request))
            .expectNextMatches(
                response ->
                    response.getHeaders().getLocation() != null &&
                    response
                        .getHeaders()
                        .getLocation()
                        .getPath()
                        .contains("/employees/")
            )
            .verifyComplete();
    }

    @Test
    void givenExistingEmployee_whenUpdate_thenReturnEmployee() {
        // given
        UUID workerId = worker.getUuid();
        EmployeeRequest request = new EmployeeRequest(
            "Pedro",
            "Garcia",
            EmployeeType.MANAGER
        );

        // when / then
        StepVerifier.create(controller.updateEmployee(workerId, request))
            .expectNextMatches(
                dto ->
                    dto.uuid().equals(workerId) &&
                    dto.firstName().equals("Pedro") &&
                    dto.lastName().equals("Garcia") &&
                    dto.employeeType() == EmployeeType.MANAGER
            )
            .verifyComplete();
    }

    @Test
    void givenNonExistentEmployee_whenUpdate_thenReportsNotFound() {
        // given
        UUID unknownId = UUID.randomUUID();
        EmployeeRequest request = new EmployeeRequest(
            "Pedro",
            "Garcia",
            EmployeeType.WORKER
        );

        // when / then
        StepVerifier.create(controller.updateEmployee(unknownId, request))
            .expectError(EmployeeNotFoundException.class)
            .verify();
    }

    @Test
    void givenExistingEmployee_whenPatch_thenReturnEmployee() {
        // given
        UUID workerId = worker.getUuid();
        EmployeePatchRequest request = new EmployeePatchRequest(
            "Maria",
            null,
            null
        );

        // when / then
        StepVerifier.create(controller.patchEmployee(workerId, request))
            .expectNextMatches(
                dto ->
                    dto.uuid().equals(workerId) &&
                    dto.firstName().equals("Maria") &&
                    dto.lastName().equals("dela Cruz") &&
                    dto.employeeType() == EmployeeType.WORKER
            )
            .verifyComplete();
    }

    @Test
    void givenNonExistentEmployee_whenPatch_thenReportsNotFound() {
        // given
        UUID unknownId = UUID.randomUUID();
        EmployeePatchRequest request = new EmployeePatchRequest(
            "Maria",
            null,
            null
        );

        // when / then
        StepVerifier.create(controller.patchEmployee(unknownId, request))
            .expectError(EmployeeNotFoundException.class)
            .verify();
    }

    @Test
    void givenExistingEmployee_whenDelete_thenConfirmsRemoval() {
        // given
        UUID workerId = worker.getUuid();

        // when / then
        StepVerifier.create(controller.deleteEmployee(workerId))
            .expectNextMatches(
                response -> response.getStatusCode() == HttpStatus.NO_CONTENT
            )
            .verifyComplete();
    }

    @Test
    void givenNonExistentEmployee_whenDelete_thenReportsNotFound() {
        // given
        UUID unknownId = UUID.randomUUID();

        // when / then
        StepVerifier.create(controller.deleteEmployee(unknownId))
            .expectError(EmployeeNotFoundException.class)
            .verify();
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

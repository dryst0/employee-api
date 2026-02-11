package com.jfi.api.employee.usecase;

import com.jfi.api.employee.adapter.out.persistence.FakeEmployeePersistence;
import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeNotFoundException;
import com.jfi.api.employee.domain.EmployeeType;
import com.jfi.api.employee.domain.InvalidEmployeeException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EmployeeServiceImplTest {

    FakeEmployeePersistence employeePersistence;
    EmployeeServiceImpl employeeService;

    Employee workerEntity;
    Employee managerEntity;
    Employee financeManagerEntity;

    @BeforeEach
    void setup() {
        employeePersistence = new FakeEmployeePersistence();
        employeeService = new EmployeeServiceImpl(employeePersistence);

        workerEntity = Employee.builder()
            .uuid(UUID.randomUUID())
            .firstName("Juan")
            .lastName("dela Cruz")
            .employeeType(EmployeeType.WORKER)
            .build();
        managerEntity = Employee.builder()
            .uuid(UUID.randomUUID())
            .firstName("Tudis")
            .lastName("dela Cruz")
            .employeeType(EmployeeType.MANAGER)
            .build();
        financeManagerEntity = Employee.builder()
            .uuid(UUID.randomUUID())
            .firstName("Dmitri")
            .lastName("dela Cruz")
            .employeeType(EmployeeType.FINANCE_MANAGER)
            .build();

        employeePersistence.save(workerEntity);
        employeePersistence.save(managerEntity);
        employeePersistence.save(financeManagerEntity);
    }

    @Test
    void givenEmployeeId_whenEmployeeIsLookedUp_thenProvidesTheEmployee() {
        // given
        UUID workerId = workerEntity.getUuid();

        // when / then
        StepVerifier.create(employeeService.findEmployeeById(workerId))
            .expectNextMatches(
                employee ->
                    employee.getUuid().equals(workerId) &&
                    employee.getFirstName().equals("Juan") &&
                    employee.getLastName().equals("dela Cruz") &&
                    employee.getEmployeeType() == EmployeeType.WORKER
            )
            .verifyComplete();
    }

    @Test
    void givenCorrectEmployeeInformation_whenEmployeeIsAdded_thenProvidesTheNewEmployee() {
        // given
        Employee newEmployee = Employee.builder()
            .firstName("Pedro")
            .lastName("Garcia")
            .employeeType(EmployeeType.WORKER)
            .build();

        // when / then
        StepVerifier.create(employeeService.createEmployee(newEmployee))
            .expectNextMatches(
                saved ->
                    saved.getUuid() != null &&
                    saved.getFirstName().equals("Pedro") &&
                    saved.getLastName().equals("Garcia") &&
                    saved.getEmployeeType() == EmployeeType.WORKER
            )
            .verifyComplete();
    }

    @Test
    void givenBlankFirstName_whenCreate_thenRejects() {
        // given
        Employee invalid = Employee.builder()
            .firstName("  ")
            .lastName("Garcia")
            .employeeType(EmployeeType.WORKER)
            .build();

        // when / then
        StepVerifier.create(employeeService.createEmployee(invalid))
            .expectError(InvalidEmployeeException.class)
            .verify();
    }

    @Test
    void givenThatEmployeeTypeIsNotProvided_whenEmployeeIsAdded_thenRejects() {
        // given
        Employee invalid = Employee.builder()
            .firstName("Pedro")
            .lastName("Garcia")
            .employeeType(null)
            .build();

        // when / then
        StepVerifier.create(employeeService.createEmployee(invalid))
            .expectError(InvalidEmployeeException.class)
            .verify();
    }

    @Test
    void givenExistingEmployee_whenUpdate_thenProvidesTheEmployee() {
        // given
        UUID existingId = workerEntity.getUuid();
        Employee updated = Employee.builder()
            .firstName("Maria")
            .lastName("Santos")
            .employeeType(EmployeeType.MANAGER)
            .build();

        // when / then
        StepVerifier.create(employeeService.updateEmployee(existingId, updated))
            .expectNextMatches(
                saved ->
                    saved.getUuid().equals(existingId) &&
                    saved.getFirstName().equals("Maria") &&
                    saved.getLastName().equals("Santos") &&
                    saved.getEmployeeType() == EmployeeType.MANAGER
            )
            .verifyComplete();
    }

    @Test
    void givenBlankFirstName_whenUpdate_thenRejects() {
        // given
        UUID existingId = workerEntity.getUuid();
        Employee invalid = Employee.builder()
            .firstName("  ")
            .lastName("Santos")
            .employeeType(EmployeeType.MANAGER)
            .build();

        // when / then
        StepVerifier.create(employeeService.updateEmployee(existingId, invalid))
            .expectError(InvalidEmployeeException.class)
            .verify();
    }

    @Test
    void givenExistingEmployee_whenPatchFirstName_thenProvidesTheEmployee() {
        // given
        UUID existingId = workerEntity.getUuid();
        Employee patch = Employee.builder().firstName("Maria").build();

        // when / then
        StepVerifier.create(employeeService.patchEmployee(existingId, patch))
            .expectNextMatches(
                saved ->
                    saved.getUuid().equals(existingId) &&
                    saved.getFirstName().equals("Maria") &&
                    saved.getLastName().equals("dela Cruz") &&
                    saved.getEmployeeType() == EmployeeType.WORKER
            )
            .verifyComplete();
    }

    @Test
    void givenBlankFirstName_whenPatch_thenRejects() {
        // given
        UUID existingId = workerEntity.getUuid();
        Employee patch = Employee.builder().firstName("  ").build();

        // when / then
        StepVerifier.create(employeeService.patchEmployee(existingId, patch))
            .expectError(InvalidEmployeeException.class)
            .verify();
    }

    @Test
    void givenExistingEmployee_whenDelete_thenEmployeeIsRemoved() {
        // given
        UUID existingId = workerEntity.getUuid();

        // when
        StepVerifier.create(
            employeeService.deleteEmployee(existingId)
        ).verifyComplete();

        // then
        StepVerifier.create(
            employeeService.findEmployeeById(existingId)
        ).verifyComplete();
    }

    @Test
    void givenNonExistentEmployee_whenDelete_thenEmployeeIsNotFound() {
        // given
        UUID unknownId = UUID.randomUUID();

        // when / then
        StepVerifier.create(employeeService.deleteEmployee(unknownId))
            .expectError(EmployeeNotFoundException.class)
            .verify();
    }

    @Test
    void givenNoParameters_whenFindAll_thenListsAllEmployees() {
        // when / then
        StepVerifier.create(employeeService.findAllEmployees())
            .expectNextMatches(e -> e.getUuid().equals(workerEntity.getUuid()))
            .expectNextMatches(e -> e.getUuid().equals(managerEntity.getUuid()))
            .expectNextMatches(e ->
                e.getUuid().equals(financeManagerEntity.getUuid())
            )
            .verifyComplete();
    }
}

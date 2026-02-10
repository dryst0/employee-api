package com.jfi.api.employee.usecase;

import com.jfi.api.employee.adapter.out.persistence.FakeEmployeePersistence;
import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EmployeeServiceImplTests {

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
    void givenEmployeeId_whenFindById_thenReturnEmployee() {
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
    void givenNoParameters_whenFindAll_thenReturnAllEmployees() {
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

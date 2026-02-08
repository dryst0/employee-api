package com.jfi.api.employee.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmployeeDTOTests {

    @Test
    void givenValues_thenReturnEmployeeDTO() {
        UUID uuid = UUID.randomUUID();
        String firstName = "Juan";
        String lastName = "dela Cruz";
        EmployeeDTO employeeDTO = new EmployeeDTO(
            uuid,
            firstName,
            lastName,
            EmployeeType.WORKER
        );

        assertEquals(uuid, employeeDTO.uuid());
        assertEquals(firstName, employeeDTO.firstName());
        assertEquals(lastName, employeeDTO.lastName());
        assertEquals(EmployeeType.WORKER, employeeDTO.employeeType());
    }

    @Test
    void givenNoEmployeeType_thenDefaultToWorker() {
        UUID uuid = UUID.randomUUID();
        EmployeeDTO employeeDTO = new EmployeeDTO(uuid, "Juan", "dela Cruz");

        assertEquals(EmployeeType.WORKER, employeeDTO.employeeType());
    }

    @Test
    void givenEmployee_whenFromCalled_thenReturnMatchingDTO() {
        // given
        UUID uuid = UUID.randomUUID();
        Employee employee = Employee.builder()
            .uuid(uuid)
            .firstName("Juan")
            .lastName("dela Cruz")
            .employeeType(EmployeeType.MANAGER)
            .build();

        // when
        EmployeeDTO dto = EmployeeDTO.from(employee);

        // then
        assertEquals(uuid, dto.uuid());
        assertEquals("Juan", dto.firstName());
        assertEquals("dela Cruz", dto.lastName());
        assertEquals(EmployeeType.MANAGER, dto.employeeType());
    }
}

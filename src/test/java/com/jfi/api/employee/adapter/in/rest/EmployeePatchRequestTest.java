package com.jfi.api.employee.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.jfi.api.employee.domain.Employee;
import org.junit.jupiter.api.Test;

class EmployeePatchRequestTest {

    @Test
    void givenFirstNameIsProvided_whenEmployeeIsChanged_thenFirstNameIsUpdated() {
        // given
        EmployeePatchRequest request = new EmployeePatchRequest(
            "Maria",
            null,
            null
        );

        // when
        Employee employee = request.toEmployee();

        // then
        assertNull(employee.getUuid());
        assertEquals("Maria", employee.getFirstName());
        assertNull(employee.getLastName());
        assertNull(employee.getEmployeeType());
    }
}

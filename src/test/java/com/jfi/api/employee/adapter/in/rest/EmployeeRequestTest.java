package com.jfi.api.employee.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeType;
import org.junit.jupiter.api.Test;

class EmployeeRequestTest {

    @Test
    void givenEmployeeInformation_whenEmployeeIsAdded_thenGivenAndAddedInformationMustBeTheSame() {
        // given
        EmployeeRequest request = new EmployeeRequest(
            "Pedro",
            "Garcia",
            EmployeeType.MANAGER
        );

        // when
        Employee employee = request.toEmployee();

        // then
        assertNull(employee.getUuid());
        assertEquals("Pedro", employee.getFirstName());
        assertEquals("Garcia", employee.getLastName());
        assertEquals(EmployeeType.MANAGER, employee.getEmployeeType());
    }
}

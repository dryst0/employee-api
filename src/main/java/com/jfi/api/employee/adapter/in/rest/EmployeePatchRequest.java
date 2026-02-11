package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeType;

public record EmployeePatchRequest(
    String firstName,
    String lastName,
    EmployeeType employeeType
) {
    public Employee toEmployee() {
        return Employee.builder()
            .firstName(firstName)
            .lastName(lastName)
            .employeeType(employeeType)
            .build();
    }
}

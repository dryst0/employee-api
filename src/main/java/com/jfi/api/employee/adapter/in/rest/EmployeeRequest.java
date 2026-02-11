package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmployeeRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull EmployeeType employeeType
) {
    public Employee toEmployee() {
        return Employee.builder()
            .firstName(firstName)
            .lastName(lastName)
            .employeeType(employeeType)
            .build();
    }
}

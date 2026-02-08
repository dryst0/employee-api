package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeType;
import java.util.UUID;

public record EmployeeDTO(
    UUID uuid,
    String firstName,
    String lastName,
    EmployeeType employeeType
) {
    public EmployeeDTO(UUID uuid, String firstName, String lastName) {
        this(uuid, firstName, lastName, EmployeeType.WORKER);
    }

    public static EmployeeDTO from(Employee employee) {
        return new EmployeeDTO(
            employee.getUuid(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmployeeType()
        );
    }
}

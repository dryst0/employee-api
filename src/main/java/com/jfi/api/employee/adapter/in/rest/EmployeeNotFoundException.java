package com.jfi.api.employee.adapter.in.rest;

import java.util.UUID;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(UUID uuid) {
        super("Employee not found: " + uuid);
    }
}

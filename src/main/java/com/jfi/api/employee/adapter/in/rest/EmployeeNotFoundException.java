package com.jfi.api.employee.adapter.in.rest;

import java.util.UUID;

public class EmployeeNotFoundException extends RuntimeException {

    static final String MESSAGE_PREFIX = "Employee not found: ";

    public EmployeeNotFoundException(UUID uuid) {
        super(MESSAGE_PREFIX + uuid);
    }
}

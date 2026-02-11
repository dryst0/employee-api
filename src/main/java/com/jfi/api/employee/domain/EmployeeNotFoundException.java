package com.jfi.api.employee.domain;

import java.util.UUID;

public class EmployeeNotFoundException extends EmployeeException {

    public static final String MESSAGE_PREFIX = "Employee not found: ";

    public EmployeeNotFoundException(UUID uuid) {
        super(MESSAGE_PREFIX + uuid);
    }
}

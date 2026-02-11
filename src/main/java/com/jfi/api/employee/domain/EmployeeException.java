package com.jfi.api.employee.domain;

public abstract class EmployeeException extends RuntimeException {

    protected EmployeeException(String message) {
        super(message);
    }
}

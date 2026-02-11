package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.EmployeeNotFoundException;
import com.jfi.api.employee.domain.InvalidEmployeeException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EmployeeExceptionHandler {

    static final String EMPLOYEE_NOT_FOUND_TITLE = "Employee Not Found";
    static final String INVALID_EMPLOYEE_TITLE = "Invalid Employee";
    static final URI PROBLEM_DEFAULT_TYPE = URI.create("about:blank");

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail handleEmployeeNotFound(EmployeeNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problem.setTitle(EMPLOYEE_NOT_FOUND_TITLE);
        problem.setType(PROBLEM_DEFAULT_TYPE);
        return problem;
    }

    @ExceptionHandler(InvalidEmployeeException.class)
    public ProblemDetail handleInvalidEmployee(InvalidEmployeeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problem.setTitle(INVALID_EMPLOYEE_TITLE);
        problem.setType(PROBLEM_DEFAULT_TYPE);
        return problem;
    }
}

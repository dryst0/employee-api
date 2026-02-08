package com.jfi.api.employee.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class EmployeeExceptionHandlerTests {

    EmployeeExceptionHandler handler = new EmployeeExceptionHandler();

    @Test
    void givenEmployeeNotFound_whenHandled_thenReturnProblemDetailWith404() {
        // given
        UUID uuid = UUID.randomUUID();
        EmployeeNotFoundException exception = new EmployeeNotFoundException(
            uuid
        );

        // when
        ProblemDetail problem = handler.handleEmployeeNotFound(exception);

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
    }

    @Test
    void givenEmployeeNotFound_whenHandled_thenReturnCorrectTitle() {
        // given
        UUID uuid = UUID.randomUUID();
        EmployeeNotFoundException exception = new EmployeeNotFoundException(
            uuid
        );

        // when
        ProblemDetail problem = handler.handleEmployeeNotFound(exception);

        // then
        assertEquals(
            EmployeeExceptionHandler.EMPLOYEE_NOT_FOUND_TITLE,
            problem.getTitle()
        );
    }

    @Test
    void givenEmployeeNotFound_whenHandled_thenReturnDetailWithUUID() {
        // given
        UUID uuid = UUID.randomUUID();
        EmployeeNotFoundException exception = new EmployeeNotFoundException(
            uuid
        );

        // when
        ProblemDetail problem = handler.handleEmployeeNotFound(exception);

        // then
        assertEquals(
            EmployeeNotFoundException.MESSAGE_PREFIX + uuid,
            problem.getDetail()
        );
    }

    @Test
    void givenEmployeeNotFound_whenHandled_thenReturnAboutBlankType() {
        // given
        UUID uuid = UUID.randomUUID();
        EmployeeNotFoundException exception = new EmployeeNotFoundException(
            uuid
        );

        // when
        ProblemDetail problem = handler.handleEmployeeNotFound(exception);

        // then
        assertEquals(
            EmployeeExceptionHandler.PROBLEM_DEFAULT_TYPE,
            problem.getType()
        );
    }
}

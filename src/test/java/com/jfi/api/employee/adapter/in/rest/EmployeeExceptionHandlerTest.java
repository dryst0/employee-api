package com.jfi.api.employee.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jfi.api.employee.domain.EmployeeNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class EmployeeExceptionHandlerTest {

    EmployeeExceptionHandler handler = new EmployeeExceptionHandler();

    @Test
    void givenEmployeeNotFound_whenHandled_thenReportsNotFound() {
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
    void givenEmployeeNotFound_whenHandled_thenDescribesTheError() {
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
    void givenEmployeeNotFound_whenHandled_thenIdentifiesTheMissingEmployee() {
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
    void givenEmployeeNotFound_whenHandled_thenUsesStandardErrorFormat() {
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

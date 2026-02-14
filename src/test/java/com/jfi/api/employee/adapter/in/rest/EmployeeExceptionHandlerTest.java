package com.jfi.api.employee.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jfi.api.employee.domain.EmployeeNotFoundException;
import com.jfi.api.employee.domain.InvalidEmployeeException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class EmployeeExceptionHandlerTest {

    EmployeeExceptionHandler handler = new EmployeeExceptionHandler();

    @Test
    void givenTheEmployeeIsNotStored_whenTheProblemIsReported_thenEmployeeIsNotFound() {
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
    void givenTheEmployeeIsNotStored_whenTheProblemIsReported_thenDescribesTheProblem() {
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
    void givenTheEmployeeIsNotStored_whenTheProblemIsReported_thenIdentifiesTheMissingEmployee() {
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
    void givenIncorrectEmployeeInformation_whenTheProblemIsReported_thenEmployeeIsNotAdded() {
        // given
        InvalidEmployeeException exception = new InvalidEmployeeException(
            "First name must not be blank"
        );

        // when
        ProblemDetail problem = handler.handleInvalidEmployee(exception);

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    }

    @Test
    void givenIncorrectEmployeeInformation_whenTheProblemIsReported_thenDescribesTheProblem() {
        // given
        InvalidEmployeeException exception = new InvalidEmployeeException(
            "First name must not be blank"
        );

        // when
        ProblemDetail problem = handler.handleInvalidEmployee(exception);

        // then
        assertEquals(
            EmployeeExceptionHandler.INVALID_EMPLOYEE_TITLE,
            problem.getTitle()
        );
    }

    @Test
    void givenIncorrectEmployeeInformation_whenTheProblemIsReported_thenExplainsWhatIsWrong() {
        // given
        InvalidEmployeeException exception = new InvalidEmployeeException(
            "First name must not be blank"
        );

        // when
        ProblemDetail problem = handler.handleInvalidEmployee(exception);

        // then
        assertEquals("First name must not be blank", problem.getDetail());
    }

    @Test
    void givenTheEmployeeIsNotStored_whenTheProblemIsReported_thenDescribesGenericProblem() {
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

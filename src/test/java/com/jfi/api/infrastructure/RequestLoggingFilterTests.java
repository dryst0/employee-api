package com.jfi.api.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

class RequestLoggingFilterTests {

    RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void givenSuccessfulRequest_thenLogsAtInfo() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get(
            "/employees"
        ).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        CapturingAppender appender = CapturingAppender.attach(
            RequestLoggingFilter.class
        );

        // when
        WebFilterChain chain = e -> Mono.empty();
        StepVerifier.create(filter.filter(exchange, chain))
            .expectComplete()
            .verify();

        // then
        List<String> messages = appender.getMessages();
        appender.detach();
        assertEquals(
            1,
            messages.size(),
            "Expected single log line, got: " + messages
        );
        assertTrue(
            messages.getFirst().contains("200") &&
                messages.getFirst().contains("ms"),
            "Expected status and duration, got: " + messages.getFirst()
        );
        assertEquals(Level.INFO, appender.getLevels().getFirst());
    }

    @Test
    void givenClientError_thenLogsAtInfoWithReason() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get(
            "/employees/bad"
        ).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        CapturingAppender appender = CapturingAppender.attach(
            RequestLoggingFilter.class
        );

        // when
        WebFilterChain chain = e ->
            Mono.error(new RuntimeException("Employee not found"));
        StepVerifier.create(filter.filter(exchange, chain))
            .expectError()
            .verify();

        // then
        List<String> messages = appender.getMessages();
        appender.detach();
        assertEquals(
            1,
            messages.size(),
            "Expected single log line, got: " + messages
        );
        assertTrue(
            messages.getFirst().contains("404"),
            "Expected status code, got: " + messages.getFirst()
        );
        assertTrue(
            messages.getFirst().contains("Employee not found"),
            "Expected error reason, got: " + messages.getFirst()
        );
        assertEquals(Level.INFO, appender.getLevels().getFirst());
    }

    @Test
    void givenServerError_thenLogsAtError() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get(
            "/employees"
        ).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        CapturingAppender appender = CapturingAppender.attach(
            RequestLoggingFilter.class
        );

        // when
        WebFilterChain chain = e ->
            Mono.error(new RuntimeException("Unexpected failure"));
        StepVerifier.create(filter.filter(exchange, chain))
            .expectError()
            .verify();

        // then
        List<String> messages = appender.getMessages();
        appender.detach();
        assertEquals(
            1,
            messages.size(),
            "Expected single log line, got: " + messages
        );
        assertTrue(
            messages.getFirst().contains("500"),
            "Expected status code, got: " + messages.getFirst()
        );
        assertTrue(
            messages.getFirst().contains("Unexpected failure"),
            "Expected error reason, got: " + messages.getFirst()
        );
        assertEquals(Level.ERROR, appender.getLevels().getFirst());
    }

    @Test
    void givenRequestWithContextId_thenMdcContainsRequestId() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get(
            "/employees"
        ).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        CapturingAppender appender = CapturingAppender.attach(
            RequestLoggingFilter.class
        );

        // when
        WebFilterChain chain = e -> Mono.empty();
        StepVerifier.create(
            filter
                .filter(exchange, chain)
                .contextWrite(
                    Context.of(
                        RequestIdFilter.REQUEST_ID_KEY,
                        "test-request-id"
                    )
                )
        )
            .expectComplete()
            .verify();

        // then
        List<Map<String, String>> mdcSnapshots = appender.getMdcSnapshots();
        appender.detach();
        assertEquals(1, mdcSnapshots.size());
        assertEquals(
            "test-request-id",
            mdcSnapshots.getFirst().get(RequestIdFilter.REQUEST_ID_KEY)
        );
    }

    static class CapturingAppender extends AbstractAppender {

        private final List<String> messages = new java.util.ArrayList<>();
        private final List<Level> levels = new java.util.ArrayList<>();
        private final List<Map<String, String>> mdcSnapshots =
            new java.util.ArrayList<>();
        private LoggerConfig loggerConfig;

        CapturingAppender() {
            super("CapturingAppender", null, null, true, Property.EMPTY_ARRAY);
        }

        static CapturingAppender attach(Class<?> loggerClass) {
            CapturingAppender appender = new CapturingAppender();
            appender.start();
            Logger logger = (Logger) LogManager.getLogger(loggerClass);
            appender.loggerConfig = logger.get();
            appender.loggerConfig.setLevel(Level.DEBUG);
            appender.loggerConfig.addAppender(appender, Level.DEBUG, null);
            logger.getContext().updateLoggers();
            return appender;
        }

        void detach() {
            loggerConfig.removeAppender("CapturingAppender");
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
            levels.add(event.getLevel());
            mdcSnapshots.add(
                new java.util.HashMap<>(event.getContextData().toMap())
            );
        }

        List<String> getMessages() {
            return messages;
        }

        List<Level> getLevels() {
            return levels;
        }

        List<Map<String, String>> getMdcSnapshots() {
            return mdcSnapshots;
        }
    }
}

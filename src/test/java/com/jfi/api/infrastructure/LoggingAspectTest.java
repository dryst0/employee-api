package com.jfi.api.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

class LoggingAspectTest {

    LoggingAspect loggingAspect;

    @BeforeEach
    void setup() {
        loggingAspect = new LoggingAspect();
    }

    @Test
    void givenFluxMethod_whenIntercepted_thenReturnsOriginalElements()
        throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "getAllEmployees",
            "EmployeeRESTController",
            new Object[] {},
            Flux.just("a", "b", "c")
        );

        // when
        Object result = loggingAspect.logAround(joinPoint);

        // then
        @SuppressWarnings("unchecked")
        Flux<String> flux = (Flux<String>) result;
        StepVerifier.create(
            flux.contextWrite(
                Context.of(RequestIdFilter.REQUEST_ID_KEY, "test-id")
            )
        )
            .expectNext("a", "b", "c")
            .verifyComplete();
    }

    @Test
    void givenMonoMethod_whenIntercepted_thenReturnsOriginalElement()
        throws Throwable {
        // given
        UUID uuid = UUID.randomUUID();
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "getEmployeeById",
            "EmployeeRESTController",
            new Object[] { uuid },
            Mono.just("employee")
        );

        // when
        Object result = loggingAspect.logAround(joinPoint);

        // then
        @SuppressWarnings("unchecked")
        Mono<String> mono = (Mono<String>) result;
        StepVerifier.create(
            mono.contextWrite(
                Context.of(RequestIdFilter.REQUEST_ID_KEY, "test-id")
            )
        )
            .expectNext("employee")
            .verifyComplete();
    }

    @Test
    void givenMonoError_whenIntercepted_thenPropagatesError() throws Throwable {
        // given
        RuntimeException error = new RuntimeException("not found");
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "getEmployeeById",
            "EmployeeRESTController",
            new Object[] { UUID.randomUUID() },
            Mono.error(error)
        );

        // when
        Object result = loggingAspect.logAround(joinPoint);

        // then
        @SuppressWarnings("unchecked")
        Mono<String> mono = (Mono<String>) result;
        StepVerifier.create(
            mono.contextWrite(
                Context.of(RequestIdFilter.REQUEST_ID_KEY, "test-id")
            )
        )
            .expectErrorMatches(e -> e.getMessage().equals("not found"))
            .verify();
    }

    @Test
    void givenNonReactiveMethod_whenIntercepted_thenReturnsOriginalValue()
        throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "someMethod",
            "SomeClass",
            new Object[] { "arg1" },
            "plain result"
        );

        // when
        Object result = loggingAspect.logAround(joinPoint);

        // then
        assertEquals("plain result", result);
    }

    @Test
    void givenMonoMethod_whenCompleted_thenLogsAtInfoLevel() throws Throwable {
        // given
        CapturingAppender appender = CapturingAppender.attach();
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "createEmployee",
            "EmployeeServiceImpl",
            new Object[] {},
            Mono.just("created")
        );

        // when
        @SuppressWarnings("unchecked")
        Mono<String> mono = (Mono<String>) loggingAspect.logAround(joinPoint);
        StepVerifier.create(
            mono.contextWrite(
                Context.of(RequestIdFilter.REQUEST_ID_KEY, "test-id")
            )
        )
            .expectNext("created")
            .verifyComplete();

        // then
        appender.detach();
        assertThat(appender.getEvents()).anyMatch(
            e ->
                e.getLevel() == Level.INFO &&
                e.getMessage().getFormattedMessage().contains("Completed")
        );
    }

    @Test
    void givenFluxMethod_whenCompleted_thenLogsAtInfoLevel() throws Throwable {
        // given
        CapturingAppender appender = CapturingAppender.attach();
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "getAllEmployees",
            "EmployeeRESTController",
            new Object[] {},
            Flux.just("a", "b")
        );

        // when
        @SuppressWarnings("unchecked")
        Flux<String> flux = (Flux<String>) loggingAspect.logAround(joinPoint);
        StepVerifier.create(
            flux.contextWrite(
                Context.of(RequestIdFilter.REQUEST_ID_KEY, "test-id")
            )
        )
            .expectNext("a", "b")
            .verifyComplete();

        // then
        appender.detach();
        assertThat(appender.getEvents()).anyMatch(
            e ->
                e.getLevel() == Level.INFO &&
                e.getMessage().getFormattedMessage().contains("Completed")
        );
    }

    @Test
    void givenNonReactiveMethod_whenCompleted_thenLogsAtInfoLevel()
        throws Throwable {
        // given
        CapturingAppender appender = CapturingAppender.attach();
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "someMethod",
            "SomeClass",
            new Object[] {},
            "result"
        );

        // when
        loggingAspect.logAround(joinPoint);

        // then
        appender.detach();
        assertThat(appender.getEvents()).anyMatch(
            e ->
                e.getLevel() == Level.INFO &&
                e.getMessage().getFormattedMessage().contains("Executed")
        );
    }

    static class CapturingAppender extends AbstractAppender {

        private final List<LogEvent> events = new ArrayList<>();
        private LoggerConfig loggerConfig;

        CapturingAppender() {
            super("CapturingAppender", null, null, true, Property.EMPTY_ARRAY);
        }

        static CapturingAppender attach() {
            CapturingAppender appender = new CapturingAppender();
            appender.start();
            Logger logger = (Logger) LogManager.getLogger(LoggingAspect.class);
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
            events.add(event.toImmutable());
        }

        List<LogEvent> getEvents() {
            return events;
        }
    }

    static class FakeJoinPoint implements ProceedingJoinPoint {

        private final String methodName;
        private final String className;
        private final Object[] args;
        private final Object returnValue;

        FakeJoinPoint(
            String methodName,
            String className,
            Object[] args,
            Object returnValue
        ) {
            this.methodName = methodName;
            this.className = className;
            this.args = args;
            this.returnValue = returnValue;
        }

        @Override
        public Object proceed() throws Throwable {
            return returnValue;
        }

        @Override
        public Object proceed(Object[] args) throws Throwable {
            return returnValue;
        }

        @Override
        public Signature getSignature() {
            return new Signature() {
                @Override
                public String toShortString() {
                    return className + "." + methodName;
                }

                @Override
                public String toLongString() {
                    return className + "." + methodName;
                }

                @Override
                public String getName() {
                    return methodName;
                }

                @Override
                public int getModifiers() {
                    return 0;
                }

                @Override
                public Class<?> getDeclaringType() {
                    return Object.class;
                }

                @Override
                public String getDeclaringTypeName() {
                    return className;
                }
            };
        }

        @Override
        public Object[] getArgs() {
            return args;
        }

        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public Object getThis() {
            return null;
        }

        @Override
        public String toShortString() {
            return className + "." + methodName;
        }

        @Override
        public String toLongString() {
            return className + "." + methodName;
        }

        @Override
        public String toString() {
            return className + "." + methodName;
        }

        @Override
        public void set$AroundClosure(
            org.aspectj.runtime.internal.AroundClosure arc
        ) {}

        @Override
        public String getKind() {
            return "method-execution";
        }

        @Override
        public org.aspectj.lang.reflect.SourceLocation getSourceLocation() {
            return null;
        }

        @Override
        public StaticPart getStaticPart() {
            return null;
        }
    }
}

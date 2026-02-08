package com.jfi.api.employee.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

class LoggingAspectTests {

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

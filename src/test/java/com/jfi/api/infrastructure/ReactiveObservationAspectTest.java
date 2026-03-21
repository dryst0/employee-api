package com.jfi.api.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveObservationAspectTest {

    ObservationRegistry registry;
    RecordingObservationHandler handler;
    ReactiveObservationAspect aspect;

    @BeforeEach
    void setup() {
        handler = new RecordingObservationHandler();
        registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        aspect = new ReactiveObservationAspect(registry);
    }

    @Test
    void givenMonoMethod_whenSubscribed_thenObservationStartsBeforeAndStopsAfterExecution()
        throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "getEmployeeById",
            "EmployeePersistenceAdapter",
            new Object[] {},
            Mono.just("result"),
            new AnnotatedTarget()
        );

        // when
        @SuppressWarnings("unchecked")
        Mono<String> result = (Mono<String>) aspect.observe(joinPoint);
        StepVerifier.create(result).expectNext("result").verifyComplete();

        // then
        assertThat(handler.events)
            .containsExactly("onStart:test.observation", "onStop:test.observation");
    }

    @Test
    void givenFluxMethod_whenSubscribed_thenObservationStartsBeforeAndStopsAfterExecution()
        throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "getEmployees",
            "EmployeePersistenceAdapter",
            new Object[] {},
            Flux.just("a", "b"),
            new AnnotatedTarget()
        );

        // when
        @SuppressWarnings("unchecked")
        Flux<String> result = (Flux<String>) aspect.observe(joinPoint);
        StepVerifier.create(result).expectNext("a", "b").verifyComplete();

        // then
        assertThat(handler.events)
            .containsExactly("onStart:test.observation", "onStop:test.observation");
    }

    @Test
    void givenMonoError_whenSubscribed_thenErrorIsRecordedOnObservation()
        throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "getEmployeeById",
            "EmployeePersistenceAdapter",
            new Object[] {},
            Mono.error(new RuntimeException("connection refused")),
            new AnnotatedTarget()
        );

        // when
        @SuppressWarnings("unchecked")
        Mono<String> result = (Mono<String>) aspect.observe(joinPoint);
        StepVerifier.create(result).expectError(RuntimeException.class).verify();

        // then
        assertThat(handler.events)
            .containsExactly(
                "onStart:test.observation",
                "onError:test.observation",
                "onStop:test.observation"
            );
    }

    @Test
    void givenNonReactiveMethod_whenInvoked_thenObservationWrapsExecution()
        throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = new FakeJoinPoint(
            "toString",
            "AnnotatedTarget",
            new Object[] {},
            "plain result",
            new AnnotatedTarget()
        );

        // when
        Object result = aspect.observe(joinPoint);

        // then
        assertThat(result).isEqualTo("plain result");
        assertThat(handler.events)
            .containsExactly("onStart:test.observation", "onStop:test.observation");
    }

    @Observed(contextualName = "test.observation")
    static class AnnotatedTarget {}

    static class RecordingObservationHandler
        implements ObservationHandler<Observation.Context> {

        final List<String> events = new ArrayList<>();

        @Override
        public void onStart(Observation.Context context) {
            events.add("onStart:" + context.getContextualName());
        }

        @Override
        public void onError(Observation.Context context) {
            events.add("onError:" + context.getContextualName());
        }

        @Override
        public void onStop(Observation.Context context) {
            events.add("onStop:" + context.getContextualName());
        }

        @Override
        public boolean supportsContext(Observation.Context context) {
            return true;
        }
    }

    static class FakeJoinPoint implements ProceedingJoinPoint {

        private final String methodName;
        private final String className;
        private final Object[] args;
        private final Object returnValue;
        private final Object target;

        FakeJoinPoint(
            String methodName,
            String className,
            Object[] args,
            Object returnValue,
            Object target
        ) {
            this.methodName = methodName;
            this.className = className;
            this.args = args;
            this.returnValue = returnValue;
            this.target = target;
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
        public Object getTarget() {
            return target;
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
        public Object getThis() {
            return target;
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

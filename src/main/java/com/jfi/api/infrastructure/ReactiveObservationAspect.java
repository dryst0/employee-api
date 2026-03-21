package com.jfi.api.infrastructure;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class ReactiveObservationAspect {

    private static final String DEFAULT_OBSERVATION_NAME = "method.observed";

    private final ObservationRegistry registry;

    public ReactiveObservationAspect(ObservationRegistry registry) {
        this.registry = registry;
    }

    @Around(
        "@within(io.micrometer.observation.annotation.Observed) || @annotation(io.micrometer.observation.annotation.Observed)"
    )
    public Object observe(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof Mono<?> mono) {
            return wrapMono(mono, joinPoint);
        }

        if (result instanceof Flux<?> flux) {
            return wrapFlux(flux, joinPoint);
        }

        return observeSync(result, joinPoint);
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> wrapMono(Mono<T> mono, ProceedingJoinPoint joinPoint) {
        return Mono.defer(() -> {
            Observation observation = createObservation(joinPoint);
            observation.start();
            return mono
                .doOnError(observation::error)
                .doFinally(signal -> observation.stop())
                .contextWrite(context ->
                    context.put(
                        ObservationThreadLocalAccessor.KEY,
                        observation
                    )
                );
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Flux<T> wrapFlux(Flux<T> flux, ProceedingJoinPoint joinPoint) {
        return Flux.defer(() -> {
            Observation observation = createObservation(joinPoint);
            observation.start();
            return flux
                .doOnError(observation::error)
                .doFinally(signal -> observation.stop())
                .contextWrite(context ->
                    context.put(
                        ObservationThreadLocalAccessor.KEY,
                        observation
                    )
                );
        });
    }

    private Object observeSync(Object result, ProceedingJoinPoint joinPoint) {
        Observation observation = createObservation(joinPoint);
        observation.start();
        observation.stop();
        return result;
    }

    private Observation createObservation(ProceedingJoinPoint joinPoint) {
        Observed observed = resolveAnnotation(joinPoint);
        String contextualName = observed.contextualName().isEmpty()
            ? joinPoint.getSignature().toShortString()
            : observed.contextualName();

        return Observation.createNotStarted(DEFAULT_OBSERVATION_NAME, registry)
            .contextualName(contextualName)
            .lowCardinalityKeyValue(
                "class",
                joinPoint.getSignature().getDeclaringTypeName()
            )
            .lowCardinalityKeyValue(
                "method",
                joinPoint.getSignature().getName()
            );
    }

    private Observed resolveAnnotation(ProceedingJoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getAnnotation(Observed.class);
    }
}

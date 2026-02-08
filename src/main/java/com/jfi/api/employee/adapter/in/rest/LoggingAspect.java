package com.jfi.api.employee.adapter.in.rest;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut(
        "(within(com.jfi.api.employee.adapter.in.rest..*) || within(com.jfi.api.employee.usecase..*) || within(com.jfi.api.employee.adapter.out.persistence..*)) && !within(com.jfi.api.employee.adapter.in.rest.LoggingAspect) && !within(com.jfi.api.employee.adapter.in.rest.EmployeeExceptionHandler) && !within(com.jfi.api.employee.adapter.in.rest.RequestIdFilter) && !within(com.jfi.api.employee.adapter.in.rest.RequestLoggingFilter)"
    )
    public void applicationMethods() {}

    @Around("applicationMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        String args = Arrays.toString(joinPoint.getArgs());

        Object result = joinPoint.proceed();

        if (result instanceof Mono<?> mono) {
            return Mono.deferContextual(ctx -> {
                setMdc(ctx);
                log.debug("Entering {} with args {}", method, args);
                return mono
                    .doOnNext(r -> {
                        setMdc(ctx);
                        log.debug("Completed {} with result", method);
                    })
                    .doOnError(e -> {
                        setMdc(ctx);
                        log.debug(
                            "Failed {} with error: {}",
                            method,
                            e.getMessage()
                        );
                    })
                    .doFinally(s -> MDC.clear());
            });
        }

        if (result instanceof Flux<?> flux) {
            return Flux.deferContextual(ctx -> {
                setMdc(ctx);
                log.debug("Entering {} with args {}", method, args);
                return flux
                    .doOnComplete(() -> {
                        setMdc(ctx);
                        log.debug("Completed {}", method);
                    })
                    .doOnError(e -> {
                        setMdc(ctx);
                        log.debug(
                            "Failed {} with error: {}",
                            method,
                            e.getMessage()
                        );
                    })
                    .doFinally(s -> MDC.clear());
            });
        }

        log.debug(
            "Executed {} with args {} returning {}",
            method,
            args,
            result
        );
        return result;
    }

    private void setMdc(ContextView ctx) {
        ctx
            .getOrEmpty(RequestIdFilter.REQUEST_ID_KEY)
            .ifPresent(id ->
                MDC.put(RequestIdFilter.REQUEST_ID_KEY, id.toString())
            );
    }
}

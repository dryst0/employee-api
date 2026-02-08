package com.jfi.api.employee.adapter.in.rest;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

// HTTP-level logging lives here, not in the LoggingAspect, because the aspect only
// intercepts our own methods. Requests rejected by the framework (405, unmapped paths)
// never reach a controller, so only a WebFilter sees every request/response.
@Slf4j
@Component
public class RequestIdFilter implements WebFilter {

    static final String REQUEST_ID_KEY = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        ServerHttpRequest request = exchange.getRequest();
        long start = System.currentTimeMillis();
        AtomicReference<Throwable> error = new AtomicReference<>();
        exchange.getResponse().getHeaders().add("X-Request-Id", requestId);
        return chain
            .filter(exchange)
            .doOnError(error::set)
            .doFinally(s -> {
                MDC.put(REQUEST_ID_KEY, requestId);
                long duration = System.currentTimeMillis() - start;
                HttpStatusCode status = exchange.getResponse().getStatusCode();
                Throwable cause = error.get();
                if (
                    cause != null && status != null && status.is5xxServerError()
                ) {
                    log.error(
                        "{} {} {} in {}ms - {}",
                        request.getMethod(),
                        request.getPath(),
                        status.value(),
                        duration,
                        cause.getMessage()
                    );
                } else if (cause != null) {
                    log.info(
                        "{} {} {} in {}ms - {}",
                        request.getMethod(),
                        request.getPath(),
                        status != null ? status.value() : "unknown",
                        duration,
                        cause.getMessage()
                    );
                } else {
                    log.info(
                        "{} {} {} in {}ms",
                        request.getMethod(),
                        request.getPath(),
                        status != null ? status.value() : "unknown",
                        duration
                    );
                }
                MDC.clear();
            })
            .contextWrite(Context.of(REQUEST_ID_KEY, requestId));
    }
}

package com.jfi.api.infrastructure;

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

// HTTP-level logging lives here, not in the LoggingAspect, because the aspect only
// intercepts our own methods. Requests rejected by the framework (405, unmapped paths)
// never reach a controller, so only a WebFilter sees every request/response.
@Slf4j
@Component
public class RequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long start = System.currentTimeMillis();
        AtomicReference<Throwable> error = new AtomicReference<>();
        return Mono.deferContextual(ctx ->
            chain
                .filter(exchange)
                .doOnError(error::set)
                .doFinally(s -> {
                    ctx
                        .getOrEmpty(RequestIdFilter.REQUEST_ID_KEY)
                        .ifPresent(id ->
                            MDC.put(
                                RequestIdFilter.REQUEST_ID_KEY,
                                id.toString()
                            )
                        );
                    long duration = System.currentTimeMillis() - start;
                    HttpStatusCode status = exchange
                        .getResponse()
                        .getStatusCode();
                    Throwable cause = error.get();
                    if (
                        cause != null &&
                        status != null &&
                        status.is5xxServerError()
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
                    MDC.remove(RequestIdFilter.REQUEST_ID_KEY);
                })
        );
    }
}

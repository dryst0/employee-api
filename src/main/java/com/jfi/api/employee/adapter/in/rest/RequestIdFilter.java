package com.jfi.api.employee.adapter.in.rest;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
public class RequestIdFilter implements WebFilter {

    static final String REQUEST_ID_KEY = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().add("X-Request-Id", requestId);
        MDC.put(REQUEST_ID_KEY, requestId);
        return chain
            .filter(exchange)
            .doFinally(s -> MDC.clear())
            .contextWrite(Context.of(REQUEST_ID_KEY, requestId));
    }
}

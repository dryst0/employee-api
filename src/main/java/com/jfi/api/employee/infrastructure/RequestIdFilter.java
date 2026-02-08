package com.jfi.api.employee.infrastructure;

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
    static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);
        MDC.put(REQUEST_ID_KEY, requestId);
        return chain
            .filter(exchange)
            .doFinally(s -> MDC.clear())
            .contextWrite(Context.of(REQUEST_ID_KEY, requestId));
    }
}

package com.jfi.api.infrastructure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RequestIdFilterTests {

    RequestIdFilter filter = new RequestIdFilter();

    @Test
    void givenRequest_whenFiltered_thenRequestIdAddedToContext() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get(
            "/employees"
        ).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // when
        Mono<Void> result = filter.filter(
            exchange,
            filterChainCapturingContext(exchange)
        );

        // then
        StepVerifier.create(result).expectComplete().verify();
    }

    @Test
    void givenRequest_whenFiltered_thenRequestIdIsValidUuid() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get(
            "/employees"
        ).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // when / then
        WebFilterChain chain = e ->
            Mono.deferContextual(ctx -> {
                String requestId = ctx.get(RequestIdFilter.REQUEST_ID_KEY);
                assertDoesNotThrow(() -> UUID.fromString(requestId));
                return Mono.empty();
            });

        StepVerifier.create(filter.filter(exchange, chain))
            .expectComplete()
            .verify();
    }

    @Test
    void givenRequest_whenFiltered_thenContextContainsRequestIdKey() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get(
            "/employees/123"
        ).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // when / then
        WebFilterChain chain = e ->
            Mono.deferContextual(ctx -> {
                assertNotNull(
                    ctx.getOrDefault(RequestIdFilter.REQUEST_ID_KEY, null)
                );
                return Mono.empty();
            });

        StepVerifier.create(filter.filter(exchange, chain))
            .expectComplete()
            .verify();
    }

    @Test
    void givenRequest_whenFiltered_thenResponseContainsRequestIdHeader() {
        // given
        MockServerHttpRequest request = MockServerHttpRequest.get(
            "/employees"
        ).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // when
        WebFilterChain chain = e -> Mono.empty();
        StepVerifier.create(filter.filter(exchange, chain))
            .expectComplete()
            .verify();

        // then
        String header = exchange
            .getResponse()
            .getHeaders()
            .getFirst(RequestIdFilter.REQUEST_ID_HEADER);
        assertDoesNotThrow(() -> UUID.fromString(header));
    }

    private WebFilterChain filterChainCapturingContext(
        MockServerWebExchange exchange
    ) {
        return e ->
            Mono.deferContextual(ctx -> {
                assertNotNull(
                    ctx.getOrDefault(RequestIdFilter.REQUEST_ID_KEY, null)
                );
                return Mono.empty();
            });
    }
}

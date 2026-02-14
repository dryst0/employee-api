package com.jfi.api.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jfi.api.employee.adapter.out.persistence.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ActuatorEndpointsIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void givenApplicationIsRunning_whenMetricsAreScraped_thenProvidesPrometheusMetrics() {
        webTestClient
            .get()
            .uri("/actuator/prometheus")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .value(body -> {
                assert body.contains("jvm_memory_used_bytes");
            });
    }

    @Test
    void givenApplicationIsRunning_whenHealthIsChecked_thenProvidesHealthDetails() {
        webTestClient
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .value(body -> {
                assert body.contains("components");
            });
    }

    @Test
    void givenDatabaseIsAvailable_whenHealthIsChecked_thenShowsDatabaseStatus() {
        webTestClient
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .value(body -> {
                assert body.contains("r2dbc");
            });
    }

    @Test
    void givenDatabaseIsAvailable_whenMetricsAreScraped_thenExposesDatabasePoolMetrics() {
        webTestClient
            .get()
            .uri("/actuator/prometheus")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .value(body -> {
                assert body.contains("r2dbc_pool");
            });
    }

    @Test
    void givenApplicationIsRunning_whenMetricsAreScraped_thenProvidesHttpRequestMetrics() {
        webTestClient
            .get()
            .uri("/actuator/prometheus")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .value(body -> {
                assert body.contains("http_server_requests");
            });
    }

    @Test
    void givenApplicationIsRunning_whenLivenessIsChecked_thenApplicationIsAlive() {
        webTestClient
            .get()
            .uri("/actuator/health/liveness")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void givenApplicationIsRunning_whenReadinessIsChecked_thenApplicationIsReady() {
        webTestClient
            .get()
            .uri("/actuator/health/readiness")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void givenApplicationIsRunning_whenTracingIsChecked_thenDistributedTracingIsAutoConfigured() {
        try {
            Class<?> tracerClass = Class.forName(
                "io.micrometer.tracing.Tracer"
            );
            String[] tracerBeans = applicationContext.getBeanNamesForType(
                tracerClass
            );
            assertTrue(
                tracerBeans.length > 0,
                "Expected a distributed tracing Tracer bean to be auto-configured"
            );
        } catch (ClassNotFoundException e) {
            throw new AssertionError(
                "Expected io.micrometer.tracing.Tracer on classpath for distributed tracing",
                e
            );
        }
    }
}

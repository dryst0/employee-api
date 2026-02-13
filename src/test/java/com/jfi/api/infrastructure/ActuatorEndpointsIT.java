package com.jfi.api.infrastructure;

import com.jfi.api.employee.adapter.out.persistence.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ActuatorEndpointsIT {

    @Autowired
    private WebTestClient webTestClient;

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
}

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
}

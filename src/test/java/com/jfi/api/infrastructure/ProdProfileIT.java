package com.jfi.api.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.jfi.api.employee.adapter.out.persistence.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("prod")
@TestPropertySource(properties = {
    "DATABASE_HOST=localhost",
    "DATABASE_USERNAME=test",
    "DATABASE_PASSWORD=test"
})
@Import(TestcontainersConfiguration.class)
class ProdProfileIT {

    @Autowired
    private Environment environment;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void givenProdProfile_whenApplicationStarts_thenActuatorExposesMinimalEndpoints() {
        assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("health,prometheus");
    }

    @Test
    void givenProdProfile_whenApplicationStarts_thenHealthDetailsAreHidden() {
        assertThat(environment.getProperty("management.endpoint.health.show-details"))
                .isEqualTo("never");
    }

    @Test
    void givenProdProfile_whenApplicationStarts_thenTracingSamplesTenPercent() {
        assertThat(environment.getProperty("management.tracing.sampling.probability"))
                .isEqualTo("0.1");
    }

    @Test
    void givenProdProfile_whenNonExposedActuatorEndpointIsAccessed_thenNotFound() {
        webTestClient.get().uri("/actuator/beans")
                .exchange()
                .expectStatus().isNotFound();
    }
}

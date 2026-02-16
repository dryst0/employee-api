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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("staging")
@TestPropertySource(properties = {
    "DATABASE_HOST=localhost",
    "DATABASE_USERNAME=test",
    "DATABASE_PASSWORD=test"
})
@Import(TestcontainersConfiguration.class)
class StagingProfileIT {

    @Autowired
    private Environment environment;

    @Test
    void givenStagingProfile_whenApplicationStarts_thenDatabaseConnectionUsesEnvironmentVariables() {
        assertThat(environment.getProperty("spring.r2dbc.url"))
                .contains("localhost")
                .contains("5432")
                .contains("employee");
    }

    @Test
    void givenStagingProfile_whenApplicationStarts_thenActuatorExposesEssentialEndpoints() {
        assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("health,info,prometheus");
    }

    @Test
    void givenStagingProfile_whenApplicationStarts_thenTracingSamplesHalfOfRequests() {
        assertThat(environment.getProperty("management.tracing.sampling.probability"))
                .isEqualTo("0.5");
    }
}

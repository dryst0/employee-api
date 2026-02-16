package com.jfi.api.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.jfi.api.employee.adapter.out.persistence.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SpringProfilesIT {

    @Autowired
    private Environment environment;

    @Test
    void givenNoExplicitProfile_whenApplicationStarts_thenDevProfileIsActive() {
        assertThat(environment.acceptsProfiles(Profiles.of("dev"))).isTrue();
    }
}

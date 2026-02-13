package com.jfi.api.employee.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;

@SpringBootTest
@Import(
    {
        TestcontainersConfiguration.class,
        FlywayBaselineOnMigrateIT.NoOpFlywayMigrationConfig.class,
    }
)
class FlywayBaselineOnMigrateIT {

    @TestConfiguration
    static class NoOpFlywayMigrationConfig {

        @Bean
        org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy flywayMigrationStrategy() {
            return flyway -> {};
        }
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        databaseClient
            .sql("DROP TABLE IF EXISTS flyway_schema_history")
            .then()
            .block();
        databaseClient.sql("DROP TABLE IF EXISTS employee").then().block();
        databaseClient
            .sql(
                """
                CREATE TABLE employee (
                    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    first_name VARCHAR(255),
                    last_name VARCHAR(255),
                    employee_type VARCHAR(50) NOT NULL DEFAULT 'WORKER'
                )
                """
            )
            .then()
            .block();
        databaseClient
            .sql(
                """
                INSERT INTO employee (uuid, first_name, last_name, employee_type)
                VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Juan', 'dela Cruz', 'WORKER'),
                       ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Maria', 'Santos', 'MANAGER'),
                       ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Pedro', 'Reyes', 'FINANCE_MANAGER')
                """
            )
            .then()
            .block();
    }

    @Test
    void givenPreExistingDatabase_whenMigrate_thenBaselinesAtConfiguredVersion() {
        flyway.migrate();

        MigrationInfo[] applied = flyway.info().applied();
        assertThat(applied).anyMatch(
            migration ->
                migration.getVersion().getVersion().equals("20260210190100") &&
                migration.getState() == MigrationState.BASELINE
        );
    }

    @Test
    void givenPreExistingDatabase_whenMigrate_thenNoPendingMigrations() {
        flyway.migrate();

        MigrationInfo[] pending = flyway.info().pending();
        assertThat(pending).isEmpty();
    }

    @Test
    void givenPreExistingDatabase_whenMigrate_thenExistingDataPreserved() {
        flyway.migrate();

        Long count = databaseClient
            .sql("SELECT COUNT(*) FROM employee")
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        assertThat(count).isEqualTo(3);
    }
}

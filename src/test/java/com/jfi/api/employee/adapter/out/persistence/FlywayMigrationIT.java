package com.jfi.api.employee.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlywayMigrationIT {

    @Autowired
    private Flyway flyway;

    @Test
    void givenMigrations_whenApplicationStarts_thenAllMigrationsApplied() {
        MigrationInfo[] applied = flyway.info().applied();

        assertThat(applied).hasSize(2);
        assertThat(applied).allSatisfy(migration ->
            assertThat(migration.getState()).isEqualTo(MigrationState.SUCCESS)
        );
    }

    @Test
    void givenMigrations_whenApplicationStarts_thenNoPendingMigrations() {
        MigrationInfo[] pending = flyway.info().pending();

        assertThat(pending).isEmpty();
    }

    @Test
    void givenSeedMigration_whenApplicationStarts_thenSeedMigrationRecorded() {
        MigrationInfo[] applied = flyway.info().applied();

        assertThat(applied).anyMatch(
            migration ->
                migration.getDescription().equals("seed employees") &&
                migration.getState() == MigrationState.SUCCESS
        );
    }
}

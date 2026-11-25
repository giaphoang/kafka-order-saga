package dev.kafka.orders;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test (requires Docker). Excluded from the default unit run via @Disabled;
 * enable by removing @Disabled when Docker is available, e.g.
 *   mvn -pl order-service test -Dtest=OrderPostgresIntegrationTest
 */
@Tag("integration")
@Disabled("Requires Docker; Testcontainers cannot run in the CI unit-test job")
@Testcontainers
class OrderPostgresIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Test
    void postgresContainerAcceptsConnections() throws Exception {
        try (var conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            assertTrue(conn.isValid(5));
        }
    }
}

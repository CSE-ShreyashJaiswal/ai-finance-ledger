package com.financeledger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring context loads successfully.
 *
 * <p>Note: This test requires a running Postgres instance (Docker Compose)
 * because Flyway will attempt to run migrations on startup.
 * In Week 3, we'll add Testcontainers so tests spin up their own Postgres.
 */
@SpringBootTest
class AiFinanceLedgerApplicationTests {

    @Test
    void contextLoads() {
        // If this passes, Spring Boot started, Flyway ran migrations,
        // JPA validated entities against the schema, and security is configured.
    }
}

package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration smoke test — verifies the full Spring context loads correctly,
 * including DataSource and JPA configuration.
 *
 * Requires MySQL running at the coordinates defined in application-local.properties
 * (or via DB_URL / DB_USERNAME / DB_PASSWORD environment variables).
 */
@SpringBootTest
@ActiveProfiles("local")
class VmsApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, the Spring context (including DataSource + JPA) started successfully.
    }
}


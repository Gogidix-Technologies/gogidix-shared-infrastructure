package com.exalt.ecosystem.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for Config Server Application.
 * 
 * Tests the basic startup and configuration loading functionality
 * of the configuration server.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConfigServerApplicationTest {

    /**
     * Test that the Spring Boot application context loads successfully.
     * This validates that all configuration and dependencies are properly set up.
     */
    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
        // No additional assertions needed for basic smoke test
    }
}

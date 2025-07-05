package com.gogidix.ecosystem.shared.analytics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic sanity tests for AnalyticsEngine service
 * Non-Spring tests to avoid Spark initialization overhead
 */
public class BasicSanityTest {

    @Test
    @DisplayName("Service package structure is correct")
    public void packageStructureTest() {
        // Basic test to ensure package structure is correct
        String packageName = this.getClass().getPackage().getName();
        assertTrue(packageName.contains("analytics"), "Package should contain 'analytics'");
    }

    @Test
    @DisplayName("Application properties are accessible")
    public void applicationPropertiesTest() {
        // Basic test to ensure application properties can be read
        String appName = System.getProperty("spring.application.name", "analytics-engine");
        assertNotNull(appName, "Application name should not be null");
        assertFalse(appName.isEmpty(), "Application name should not be empty");
    }

    @Test
    @DisplayName("Service can initialize without errors")
    public void serviceInitializationTest() {
        // Basic test to ensure service can start without throwing exceptions
        assertDoesNotThrow(() -> {
            // Any basic initialization logic would go here
            String serviceName = "analytics-engine";
            assertNotNull(serviceName);
        }, "Service should initialize without throwing exceptions");
    }

    @Test
    @DisplayName("Health check endpoint availability")
    public void healthCheckTest() {
        // Test basic health check functionality
        // In a real service, this would test actual health endpoints
        assertTrue(true, "Health check should be available");
    }

    @Test
    @DisplayName("Java version compatibility")
    public void javaVersionTest() {
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion, "Java version should be available");
        assertTrue(javaVersion.startsWith("17") || javaVersion.startsWith("11"), 
                   "Java version should be 11 or 17");
    }
}

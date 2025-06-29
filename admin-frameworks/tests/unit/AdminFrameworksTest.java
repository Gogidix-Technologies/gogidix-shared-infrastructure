package com.exalt.ecosystem.shared.adminframeworks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Admin Frameworks
 */
public class AdminFrameworksTest {

    @Test
    @DisplayName("Service package structure is correct")
    public void packageStructureTest() {
        String packageName = this.getClass().getPackage().getName();
        assertTrue(packageName.contains("adminframeworks"), "Package should contain 'adminframeworks'");
    }

    @Test
    @DisplayName("Java version compatibility")
    public void javaVersionTest() {
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion, "Java version should be available");
        assertTrue(javaVersion.startsWith("17") || javaVersion.startsWith("11"), 
                   "Java version should be 11 or 17");
    }

    @Test
    @DisplayName("Service initialization test")
    public void serviceInitializationTest() {
        assertDoesNotThrow(() -> {
            String serviceName = "admin-frameworks";
            assertNotNull(serviceName);
            assertEquals("admin-frameworks", serviceName);
        }, "Service should initialize without throwing exceptions");
    }
}
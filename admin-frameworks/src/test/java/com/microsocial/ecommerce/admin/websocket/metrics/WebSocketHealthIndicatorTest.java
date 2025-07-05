package com.gogidix.ecommerce.admin.websocket.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class WebSocketHealthIndicatorTest {

    private WebSocketMetricsCollector metricsCollector;
    private WebSocketHealthIndicator healthIndicator;
    private final long testStartTime = System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        metricsCollector = new WebSocketMetricsCollector(new SimpleMeterRegistry());
        healthIndicator = new WebSocketHealthIndicator(metricsCollector);
    }

    @Test
    void health_shouldBeUp_whenNoConnections() {
        // When
        var health = healthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus(), "Health should be UP with no connections");
        assertEquals(0, health.getDetails().get("activeConnections"), "No active connections expected");
    }

    @Test
    void health_shouldBeUp_whenRecentMessageReceived() {
        // Given
        metricsCollector.incrementConnections("test-session");
        
        // When
        var health = healthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus(), "Health should be UP with recent message");
        assertEquals(1, health.getDetails().get("activeConnections"), "One active connection expected");
    }

    @Test
    void health_shouldBeDown_whenStaleConnection() {
        // Given
        metricsCollector.incrementConnections("stale-session");
        
        // Simulate time passing beyond the threshold (5 minutes + 1 second)
        long oldTime = testStartTime - TimeUnit.MINUTES.toMillis(5) - 1000;
        try (var timeTraveller = new FixedTimeTraveller(oldTime)) {
            // When
            var health = healthIndicator.health();
            
            // Then
            assertEquals(Status.DOWN, health.getStatus(), "Health should be DOWN with stale connection");
            assertEquals(1, health.getDetails().get("activeConnections"), "One active connection expected");
            assertTrue(health.getDetails().get("reason").toString().contains("No messages received"), 
                     "Should indicate no recent messages");
        }
    }
    
    @Test
    void updateLastMessageTimestamp_shouldAffectHealth() {
        // Given
        metricsCollector.incrementConnections("test-session");
        
        // When
        healthIndicator.updateLastMessageTimestamp();
        var health = healthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus(), "Health should be UP after updating timestamp");
    }
    
    // Helper class to simulate time passing for testing
    private static class FixedTimeTraveller implements AutoCloseable {
        private final long originalTime;
        
        public FixedTimeTraveller(long fixedTime) {
            this.originalTime = System.currentTimeMillis();
            System.setProperty("java.lang.System.currentTimeMillis", String.valueOf(fixedTime));
        }
        
        @Override
        public void close() {
            System.setProperty("java.lang.System.currentTimeMillis", String.valueOf(originalTime));
        }
    }
}

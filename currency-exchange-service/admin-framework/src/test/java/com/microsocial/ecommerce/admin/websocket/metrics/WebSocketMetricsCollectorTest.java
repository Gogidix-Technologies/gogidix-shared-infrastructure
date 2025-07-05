package com.gogidix.shared.ecommerce.admin.websocket.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@ContextConfiguration(classes = WebSocketMetricsTestConfig.class)
public class WebSocketMetricsCollectorTest {

    private WebSocketMetricsCollector metricsCollector;
    private MeterRegistry meterRegistry;
    
    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsCollector = new WebSocketMetricsCollector(meterRegistry);
    }
    
    @Test
    void testIncrementConnections() {
        // When
        metricsCollector.incrementConnections("session1");
        
        // Then
        double connections = meterRegistry.counter("websocket.connections.total").count();
        assertEquals(1.0, connections, "Connection count should be incremented");
        
        int activeConnections = metricsCollector.getActiveConnectionCount();
        assertEquals(1, activeConnections, "Active connections should be 1");
    }
    
    @Test
    void testDecrementConnections() {
        // Given
        String sessionId = "session1";
        metricsCollector.incrementConnections(sessionId);
        
        // When
        metricsCollector.decrementConnections(sessionId);
        
        // Then
        int activeConnections = metricsCollector.getActiveConnectionCount();
        assertEquals(0, activeConnections, "Active connections should be 0 after decrement");
    }
    
    @Test
    void testRecordMessageProcessed() {
        // When
        metricsCollector.recordMessageProcessed("test.message", 100_000_000L); // 100ms
        
        // Then
        double messageCount = meterRegistry.counter("websocket.messages.total").count();
        assertEquals(1.0, messageCount, "Message count should be 1");
        
        double messageTypeCount = meterRegistry.counter(
            "websocket.messages.by.type", 
            "type", "test.message"
        ).count();
        assertEquals(1.0, messageTypeCount, "Message type count should be 1");
    }
    
    @Test
    void testRecordError() {
        // When
        metricsCollector.recordError("test.error");
        
        // Then
        double errorCount = meterRegistry.counter("websocket.errors.by.type", "type", "test.error").count();
        assertEquals(1.0, errorCount, "Error count should be 1");
    }
}

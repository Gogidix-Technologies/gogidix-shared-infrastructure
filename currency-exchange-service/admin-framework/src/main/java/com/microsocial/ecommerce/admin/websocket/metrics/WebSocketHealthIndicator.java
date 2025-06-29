package com.exalt.shared.ecommerce.admin.websocket.metrics;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for WebSocket connections.
 */
@Component("websocket")
public class WebSocketHealthIndicator implements HealthIndicator {
    
    private static final long MAX_INACTIVE_THRESHOLD_MS = 5 * 60 * 1000; // 5 minutes
    private final WebSocketMetricsCollector metricsCollector;
    private long lastMessageTimestamp = System.currentTimeMillis();
    
    public WebSocketHealthIndicator(WebSocketMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }
    
    @Override
    public Health health() {
        int activeConnections = metricsCollector.getActiveConnectionCount();
        long timeSinceLastMessage = System.currentTimeMillis() - lastMessageTimestamp;
        
        boolean isUp = true;
        Health.Builder builder = Health.up();
        
        // Check if we haven't seen messages for too long but have active connections
        if (activeConnections > 0 && timeSinceLastMessage > MAX_INACTIVE_THRESHOLD_MS) {
            isUp = false;
            builder = Health.down()
                .withDetail("reason", "No messages received in " + (timeSinceLastMessage / 1000) + " seconds");
        }
        
        return builder
            .withDetail("activeConnections", activeConnections)
            .withDetail("lastMessageAgeMs", timeSinceLastMessage)
            .withDetail("status", isUp ? "UP" : "DEGRADED")
            .build();
    }
    
    public void updateLastMessageTimestamp() {
        this.lastMessageTimestamp = System.currentTimeMillis();
    }
}

package com.exalt.shared.ecommerce.admin.websocket.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for WebSocket rate limiting.
 */
@Component
@ConfigurationProperties(prefix = "websocket.rate-limit")
public class RateLimitProperties {
    
    /**
     * Whether rate limiting is enabled.
     */
    private boolean enabled = true;
    
    /**
     * Maximum number of messages allowed per second per connection.
     */
    private int messagesPerSecond = 100;
    
    /**
     * Maximum number of connections allowed per IP address.
     */
    private int connectionsPerIp = 10;
    
    /**
     * Time window in seconds for rate limiting.
     */
    private int timeWindowSeconds = 60;
    
    /**
     * Whether to block connections that exceed the rate limit.
     * If false, messages will be dropped but connections remain open.
     */
    private boolean blockOnLimitExceeded = true;
    
    // Getters and Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getMessagesPerSecond() {
        return messagesPerSecond;
    }
    
    public void setMessagesPerSecond(int messagesPerSecond) {
        this.messagesPerSecond = messagesPerSecond;
    }
    
    public int getConnectionsPerIp() {
        return connectionsPerIp;
    }
    
    public void setConnectionsPerIp(int connectionsPerIp) {
        this.connectionsPerIp = connectionsPerIp;
    }
    
    public int getTimeWindowSeconds() {
        return timeWindowSeconds;
    }
    
    public void setTimeWindowSeconds(int timeWindowSeconds) {
        this.timeWindowSeconds = timeWindowSeconds;
    }
    
    public boolean isBlockOnLimitExceeded() {
        return blockOnLimitExceeded;
    }
    
    public void setBlockOnLimitExceeded(boolean blockOnLimitExceeded) {
        this.blockOnLimitExceeded = blockOnLimitExceeded;
    }
    
    /**
     * Monitoring configuration for rate limiting.
     */
    @NestedConfigurationProperty
    private MonitoringProperties monitoring = new MonitoringProperties();
    
    @Data
    public static class MonitoringProperties {
        /**
         * Whether monitoring is enabled.
         */
        private boolean enabled = true;
        
        /**
         * Number of days to retain rate limiting data.
         */
        private int retentionDays = 30;
        
        /**
         * Whether to enable detailed logging of rate limit events.
         */
        private boolean detailedLogging = false;
    }
    
    public MonitoringProperties getMonitoring() {
        return monitoring;
    }
    
    public void setMonitoring(MonitoringProperties monitoring) {
        this.monitoring = monitoring;
    }
}

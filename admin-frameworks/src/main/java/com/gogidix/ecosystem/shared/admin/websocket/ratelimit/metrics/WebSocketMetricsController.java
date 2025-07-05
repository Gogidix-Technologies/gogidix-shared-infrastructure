package com.gogidix.ecosystem.shared.admin.websocket.ratelimit.metrics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gogidix.ecosystem.shared.admin.websocket.ratelimit.RateLimitProperties;

import java.util.Map;

/**
 * Controller for exposing WebSocket metrics.
 */
@RestController
@RequestMapping("/api/websocket/metrics")
public class WebSocketMetricsController {
    
    private final RateLimitProperties rateLimitProperties;
    private final RateLimitMetrics rateLimitMetrics;

    public WebSocketMetricsController(RateLimitProperties rateLimitProperties, 
                                    RateLimitMetrics rateLimitMetrics) {
        this.rateLimitProperties = rateLimitProperties;
        this.rateLimitMetrics = rateLimitMetrics;
    }

    @GetMapping("/rate-limits")
    public Map<String, Object> getRateLimitMetrics() {
        return Map.of(
            "rateLimits", Map.of(
                "enabled", rateLimitProperties.isEnabled(),
                "messagesPerSecond", rateLimitProperties.getMessagesPerSecond(),
                "connectionsPerIp", rateLimitProperties.getConnectionsPerIp(),
                "timeWindowSeconds", rateLimitProperties.getTimeWindowSeconds(),
                "blockOnLimitExceeded", rateLimitProperties.isBlockOnLimitExceeded()
            )
        );
    }
}

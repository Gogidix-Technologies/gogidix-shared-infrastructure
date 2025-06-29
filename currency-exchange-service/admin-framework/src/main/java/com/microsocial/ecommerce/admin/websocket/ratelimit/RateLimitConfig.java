package com.exalt.shared.ecommerce.admin.websocket.ratelimit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Configuration for WebSocket rate limiting.
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class RateLimitConfig implements WebSocketMessageBrokerConfigurer {

    private final RateLimitProperties rateLimitProperties;
    private final RateLimitMetrics rateLimitMetrics;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    public RateLimitConfig(RateLimitProperties rateLimitProperties, RateLimitMetrics rateLimitMetrics) {
        this.rateLimitProperties = rateLimitProperties;
        this.rateLimitMetrics = rateLimitMetrics;
        this.rateLimitInterceptor = new RateLimitInterceptor(rateLimitProperties, rateLimitMetrics);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        if (rateLimitProperties.isEnabled()) {
            registration.interceptors(rateLimitInterceptor);
        }
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        if (rateLimitProperties.isEnabled()) {
            registration.addDecoratorFactory(handler -> new RateLimitWebSocketHandlerDecorator(handler, rateLimitInterceptor));
        }
    }

    @Bean
    public HandshakeInterceptor rateLimitHandshakeInterceptor() {
        return new RateLimitHandshakeInterceptor(rateLimitProperties, rateLimitMetrics);
    }
    
    /**
     * Clean up old rate limit counters to prevent memory leaks.
     * Runs every hour and at midnight.
     */
    @Scheduled(fixedRate = 3_600_000) // Every hour
    @Scheduled(cron = "0 0 0 * * *")  // Also run at midnight
    public void cleanupOldRateLimits() {
        if (!rateLimitProperties.getMonitoring().isEnabled()) {
            return;
        }
        
        long now = System.currentTimeMillis();
        long windowMs = rateLimitProperties.getTimeWindowSeconds() * 1000L;
        
        // Clean up old connection counters
        rateLimitInterceptor.cleanupOldCounters(now - windowMs);
        
        if (rateLimitProperties.getMonitoring().isDetailedLogging()) {
            logger.debug("Cleaned up old rate limit counters");
        }
    }
}

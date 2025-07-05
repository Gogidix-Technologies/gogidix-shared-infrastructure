package com.gogidix.ecommerce.admin.websocket.ratelimit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Test configuration for rate limiting tests.
 */
@TestConfiguration
@EnableWebSocketMessageBroker
public class TestRateLimitConfig implements WebSocketMessageBrokerConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public TestRateLimitConfig() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMessagesPerSecond(10);
        properties.setConnectionsPerIp(5);
        this.rateLimitInterceptor = new RateLimitInterceptor(properties);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
               .setAllowedOriginPatterns("*")
               .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(rateLimitInterceptor);
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return rateLimitInterceptor;
    }

    @Bean
    public RateLimitProperties rateLimitProperties() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMessagesPerSecond(10);
        properties.setConnectionsPerIp(5);
        return properties;
    }
}

package com.exalt.shared.ecommerce.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Test configuration for WebSocket testing.
 */
@Configuration
@EnableWebSocketMessageBroker
public class TestWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String WS_ENDPOINT = "/test-ws";
    public static final String TOPIC_PREFIX = "/topic";
    public static final String QUEUE_PREFIX = "/queue";
    public static final String USER_PREFIX = "/user";
    public static final String APP_PREFIX = "/app";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(TOPIC_PREFIX, QUEUE_PREFIX, USER_PREFIX);
        config.setApplicationDestinationPrefixes(APP_PREFIX);
        config.setUserDestinationPrefix(USER_PREFIX);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Bean
    @Primary
    public TestWebSocketEventListener testWebSocketEventListener() {
        return new TestWebSocketEventListener();
    }
}

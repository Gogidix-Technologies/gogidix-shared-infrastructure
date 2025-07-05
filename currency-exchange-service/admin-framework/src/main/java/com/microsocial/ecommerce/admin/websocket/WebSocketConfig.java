package com.gogidix.shared.ecommerce.admin.websocket;

import com.microsocial.ecommerce.admin.websocket.acknowledge.AcknowledgmentChannelInterceptor;
import com.microsocial.ecommerce.admin.websocket.metrics.WebSocketMetricsInterceptor;
import com.microsocial.ecommerce.admin.websocket.ratelimit.RateLimitInterceptor;
import com.microsocial.ecommerce.admin.websocket.security.WebSocketAuthChannelInterceptor;
import com.microsocial.ecommerce.admin.websocket.security.WebSocketConnectInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration class that enables STOMP messaging.
 */
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 98) // Run after security
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketMetricsInterceptor metricsInterceptor;
    private final WebSocketAuthChannelInterceptor authChannelInterceptor;
    private final WebSocketConnectInterceptor connectInterceptor;
    private final AcknowledgmentChannelInterceptor acknowledgmentChannelInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    public WebSocketConfig(WebSocketMetricsInterceptor metricsInterceptor,
                         WebSocketAuthChannelInterceptor authChannelInterceptor,
                         WebSocketConnectInterceptor connectInterceptor,
                         AcknowledgmentChannelInterceptor acknowledgmentChannelInterceptor,
                         RateLimitInterceptor rateLimitInterceptor) {
        this.metricsInterceptor = metricsInterceptor;
        this.authChannelInterceptor = authChannelInterceptor;
        this.connectInterceptor = connectInterceptor;
        this.acknowledgmentChannelInterceptor = acknowledgmentChannelInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    public static final String WS_ENDPOINT = "/ws";
    public static final String APP_PREFIX = "/app";
    public static final String TOPIC_PREFIX = "/topic";
    public static final String QUEUE_PREFIX = "/queue";
    public static final String USER_PREFIX = "/user";
    public static final String DESTINATION_PREFIX = "/admin-framework";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        config.enableSimpleBroker(TOPIC_PREFIX, QUEUE_PREFIX, USER_PREFIX);
        
        // Set the application destination prefix
        config.setApplicationDestinationPrefixes(APP_PREFIX);
        
        // Set the user destination prefix
        config.setUserDestinationPrefix(USER_PREFIX);
        
        // Set destination prefixes for handling messages
        config.setApplicationDestinationPrefixes(APP_PREFIX);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the /ws endpoint, enabling SockJS fallback
        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns("*")
                .addInterceptors(connectInterceptor)
                .withSockJS()
                .setHeartbeatTime(60_000) // 60 seconds heartbeat
                .setSessionCookieNeeded(true);
                
        // Register secure endpoint
        registry.addEndpoint("/ws/secure")
                .setAllowedOriginPatterns("*")
                .addInterceptors(connectInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add interceptors in order of execution
        registration.interceptors(
            authChannelInterceptor,        // First authenticate
            rateLimitInterceptor,          // Then apply rate limiting
            acknowledgmentChannelInterceptor, // Then handle acknowledgments
            metricsInterceptor             // Finally collect metrics
        );
    }
    
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Add interceptors for outbound messages
        registration.interceptors(
            acknowledgmentChannelInterceptor, // Handle outbound acknowledgments
            metricsInterceptor               // Collect metrics
        );
    }
}

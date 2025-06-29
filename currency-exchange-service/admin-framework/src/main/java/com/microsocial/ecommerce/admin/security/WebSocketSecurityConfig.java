package com.exalt.shared.ecommerce.admin.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * WebSocket security configuration.
 */
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Configure message broker in the parent class
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoints are registered in WebSocketConfig
    }
    
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .nullDestMatcher().authenticated()
            .simpSubscribeDestMatchers(
                "/user/queue/errors",
                "/topic/events"
            ).permitAll()
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/user/**", "/topic/**").authenticated()
            .anyMessage().denyAll();
    }
    
    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSockets for now
        // In production, you should implement proper CSRF protection
        return true;
    }
    
    @Override
    protected void customizeClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
            new WebSocketChannelInterceptor(),
            new WebSocketConnectInterceptor()
        );
    }
}

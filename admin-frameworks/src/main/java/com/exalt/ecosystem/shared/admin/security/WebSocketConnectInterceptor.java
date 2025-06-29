package com.exalt.ecosystem.shared.admin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * Interceptor for WebSocket connection events.
 */
public class WebSocketConnectInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectInterceptor.class);
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            switch (accessor.getCommand()) {
                case CONNECT:
                    handleConnect(accessor);
                    break;
                case DISCONNECT:
                    handleDisconnect(accessor);
                    break;
                case SUBSCRIBE:
                    handleSubscribe(accessor);
                    break;
                case UNSUBSCRIBE:
                    handleUnsubscribe(accessor);
                    break;
                default:
                    break;
            }
        }
        
        return message;
    }
    
    private void handleConnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";
        logger.info("WebSocket CONNECT: session={}, user={}", sessionId, user);
    }
    
    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";
        logger.info("WebSocket DISCONNECT: session={}, user={}", sessionId, user);
    }
    
    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";
        logger.debug("WebSocket SUBSCRIBE: destination={}, session={}, user={}", 
            destination, sessionId, user);
    }
    
    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";
        logger.debug("WebSocket UNSUBSCRIBE: destination={}, session={}, user={}", 
            destination, sessionId, user);
    }
}

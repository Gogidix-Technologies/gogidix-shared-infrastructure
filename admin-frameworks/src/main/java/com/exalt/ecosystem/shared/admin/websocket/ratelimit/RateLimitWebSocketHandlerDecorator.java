package com.exalt.ecosystem.shared.admin.websocket.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * WebSocket handler decorator that enforces rate limits on WebSocket connections.
 */
public class RateLimitWebSocketHandlerDecorator extends WebSocketHandlerDecorator {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitWebSocketHandlerDecorator.class);
    
    private final RateLimitInterceptor rateLimitInterceptor;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    public RateLimitWebSocketHandlerDecorator(WebSocketHandler delegate, RateLimitInterceptor rateLimitInterceptor) {
        super(delegate);
        this.rateLimitInterceptor = rateLimitInterceptor;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        super.afterConnectionEstablished(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        try {
            rateLimitInterceptor.afterConnectionClosed(session, closeStatus);
        } finally {
            sessions.remove(session.getId());
            super.afterConnectionClosed(session, closeStatus);
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            super.handleMessage(session, message);
        } catch (RateLimitInterceptor.RateLimitExceededException e) {
            logger.warn("Rate limit exceeded for session: {}", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Rate limit exceeded"));
        }
    }
}

package com.gogidix.shared.ecommerce.admin.websocket.ratelimit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
public class RateLimitInterceptorTest {

    @Mock
    private RateLimitProperties properties;
    
    @Mock
    private MessageChannel channel;
    
    @Mock
    private WebSocketSession session;
    
    @Mock
    private Principal principal;
    
    private RateLimitInterceptor interceptor;
    
    @BeforeEach
    void setUp() {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getMessagesPerSecond()).thenReturn(10);
        when(properties.getConnectionsPerIp()).thenReturn(5);
        when(properties.getTimeWindowSeconds()).thenReturn(60);
        when(properties.isBlockOnLimitExceeded()).thenReturn(true);
        
        interceptor = new RateLimitInterceptor(properties);
        
        when(session.getId()).thenReturn("test-session");
        when(session.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
    }
    
    @Test
    void testMessageRateLimit() {
        // Create a test message
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setSessionId("test-session");
        accessor.setDestination("/topic/test");
        
        // Set up session attributes
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(WebSocketSession.class.getName(), session);
        accessor.setSessionAttributes(sessionAttrs);
        
        Message<String> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());
        
        // Send messages up to the limit
        for (int i = 0; i < 10; i++) {
            assertNotNull(interceptor.preSend(message, channel));
        }
        
        // Next message should be rate limited
        assertThrows(RateLimitInterceptor.RateLimitExceededException.class, 
            () -> interceptor.preSend(message, channel));
    }
    
    @Test
    void testConnectionRateLimit() {
        // Test connection limit per IP
        for (int i = 0; i < 5; i++) {
            WebSocketSession newSession = mock(WebSocketSession.class);
            when(newSession.getId()).thenReturn("session-" + i);
            when(newSession.getRemoteAddress())
                .thenReturn(new InetSocketAddress("192.168.1.1", 12345 + i));
            
            interceptor.afterConnectionEstablished(newSession);
        }
        
        // Next connection from same IP should be rejected
        WebSocketSession newSession = mock(WebSocketSession.class);
        when(newSession.getId()).thenReturn("session-5");
        when(newSession.getRemoteAddress())
            .thenReturn(new InetSocketAddress("192.168.1.1", 12350));
        
        assertThrows(RateLimitInterceptor.RateLimitExceededException.class, 
            () -> interceptor.afterConnectionEstablished(newSession));
    }
    
    @Test
    void testConnectionCleanup() {
        // Add a connection
        interceptor.afterConnectionEstablished(session);
        
        // Close the connection
        interceptor.afterConnectionClosed(session, null);
        
        // Should be able to add a new connection
        WebSocketSession newSession = mock(WebSocketSession.class);
        when(newSession.getId()).thenReturn("new-session");
        when(newSession.getRemoteAddress())
            .thenReturn(new InetSocketAddress("127.0.0.1", 12346));
            
        assertDoesNotThrow(() -> interceptor.afterConnectionEstablished(newSession));
    }
    
    @Test
    void testRateLimitDisabled() {
        when(properties.isEnabled()).thenReturn(false);
        
        // Create a test message
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setSessionId("test-session");
        accessor.setDestination("/topic/test");
        
        // Set up session attributes
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(WebSocketSession.class.getName(), session);
        accessor.setSessionAttributes(sessionAttrs);
        
        Message<String> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());
        
        // Should not throw even if over the limit
        for (int i = 0; i < 20; i++) {
            assertNotNull(interceptor.preSend(message, channel));
        }
    }
    
    @Test
    void testGetClientIpAddress() {
        // Test with X-Forwarded-For header
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Map<String, Object> sessionAttrs = new HashMap<>();
        
        WebSocketSession testSession = mock(WebSocketSession.class);
        when(testSession.getRemoteAddress())
            .thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        
        sessionAttrs.put(WebSocketSession.class.getName(), testSession);
        accessor.setSessionAttributes(sessionAttrs);
        
        // Add X-Forwarded-For header
        accessor.setNativeHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");
        
        Message<String> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());
        
        // Should use the first IP from X-Forwarded-For
        assertNotNull(interceptor.preSend(message, channel));
    }
}

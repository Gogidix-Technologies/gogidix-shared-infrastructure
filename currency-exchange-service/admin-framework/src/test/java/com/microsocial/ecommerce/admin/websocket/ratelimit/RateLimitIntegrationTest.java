package com.exalt.shared.ecommerce.admin.websocket.ratelimit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@ExtendWith(MockitoExtension.class)
public class RateLimitIntegrationTest {

    private WebSocketStompClient stompClient;
    private BlockingQueue<String> messageQueue;
    
    @Mock
    private RateLimitProperties properties;
    
    @Captor
    private ArgumentCaptor<StompHeaders> headersCaptor;
    
    @BeforeEach
    void setUp() {
        messageQueue = new LinkedBlockingDeque<>();
        
        // Set up WebSocket client for testing
        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        SockJsClient sockJsClient = new SockJsClient(webSocketTransport);
        stompClient = new WebSocketStompClient(sockJsClient);
        
        // Configure test properties
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getMessagesPerSecond()).thenReturn(10);
        when(properties.getConnectionsPerIp()).thenReturn(5);
        when(properties.getTimeWindowSeconds()).thenReturn(1); // Short window for testing
        when(properties.isBlockOnLimitExceeded()).thenReturn(true);
    }
    
    @AfterEach
    void tearDown() {
        if (stompClient != null) {
            stompClient.stop();
        }
    }
    
    @Test
    void testMessageRateLimiting() throws Exception {
        // Create a test session
        StompSession session = createSession("test-session");
        
        // Subscribe to a test destination
        String destination = "/topic/test";
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((String) payload);
            }
        });
        
        // Send messages up to the limit
        for (int i = 0; i < 10; i++) {
            session.send(destination, "Test message " + i);
        }
        
        // Wait for messages to be processed
        await().atMost(2, TimeUnit.SECONDS).until(() -> messageQueue.size() >= 10);
        
        // Next send should be rate limited
        assertThrows(Exception.class, () -> {
            session.send(destination, "This should be rate limited");
        });
        
        // Wait for rate limit window to reset
        Thread.sleep(1100);
        
        // Should be able to send messages again after window resets
        session.send(destination, "New message after reset");
        
        // Verify the new message was received
        await().atMost(1, TimeUnit.SECONDS).until(() -> messageQueue.size() == 11);
    }
    
    @Test
    void testConnectionLimiting() throws Exception {
        // Create maximum allowed connections
        StompSession[] sessions = new StompSession[5];
        for (int i = 0; i < 5; i++) {
            sessions[i] = createSession("session-" + i);
        }
        
        // Next connection attempt should be rejected
        assertThrows(Exception.class, () -> {
            createSession("should-fail");
        });
        
        // Close one connection
        sessions[0].disconnect();
        
        // Should be able to create a new connection now
        StompSession newSession = createSession("new-session");
        assertTrue(newSession.isConnected());
        newSession.disconnect();
    }
    
    private StompSession createSession(String sessionId) throws Exception {
        return stompClient.connect("ws://localhost:8080/ws", new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                // Session connected
            }
        }).get(5, TimeUnit.SECONDS);
    }
}

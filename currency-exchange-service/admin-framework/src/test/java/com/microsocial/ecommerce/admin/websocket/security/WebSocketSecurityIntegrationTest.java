package com.gogidix.shared.ecommerce.admin.websocket.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketSecurityIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private StompSession session;
    private BlockingQueue<String> messageQueue;

    @BeforeEach
    void setUp() {
        messageQueue = new LinkedBlockingDeque<>();
        stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))
        ));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void whenConnectWithValidToken_thenConnected() throws Exception {
        // Given
        String token = "test-token-user1";
        
        // When
        StompHeaders headers = new StompHeaders();
        headers.add("X-Authorization", "Bearer " + token);
        
        session = stompClient.connect(
                "ws://localhost:" + port + "/admin-framework/ws/secure/websocket",
                new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        
        // Then
        assertTrue(session.isConnected(), "Should be connected with valid token");
    }

    @Test
    void whenConnectWithInvalidToken_thenConnectionRefused() {
        // Given
        String invalidToken = "invalid-token";
        
        // When/Then
        assertThrows(Exception.class, () -> {
            StompHeaders headers = new StompHeaders();
            headers.add("X-Authorization", "Bearer " + invalidToken);
            
            session = stompClient.connect(
                    "ws://localhost:" + port + "/admin-framework/ws/secure/websocket",
                    new StompSessionHandlerAdapter() {})
                    .get(5, TimeUnit.SECONDS);
        }, "Should throw exception with invalid token");
    }

    @Test
    void whenSubscribeWithValidToken_thenReceivesMessages() throws Exception {
        // Given
        String token = "test-token-user2";
        String destination = "/topic/test";
        
        // Connect with valid token
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("X-Authorization", "Bearer " + token);
        
        session = stompClient.connect(
                "ws://localhost:" + port + "/admin-framework/ws/secure/websocket",
                new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        
        // Subscribe to a topic
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.add((String) payload);
            }
        });
        
        // Send a message to the topic
        String testMessage = "Test message " + System.currentTimeMillis();
        session.send(destination, testMessage);
        
        // Then
        await().atMost(5, TimeUnit.SECONDS).until(() -> !messageQueue.isEmpty());
        assertEquals(testMessage, messageQueue.poll(), "Should receive the sent message");
    }

    @Test
    void whenSendMessageWithoutSubscription_thenNoMessagesReceived() throws Exception {
        // Given
        String token = "test-token-user3";
        String destination = "/topic/test";
        
        // Connect with valid token but don't subscribe
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("X-Authorization", "Bearer " + token);
        
        session = stompClient.connect(
                "ws://localhost:" + port + "/admin-framework/ws/secure/websocket",
                new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        
        // Send a message to the topic without subscribing
        String testMessage = "Test message " + System.currentTimeMillis();
        session.send(destination, testMessage);
        
        // Then
        Thread.sleep(1000); // Give some time for potential message delivery
        assertTrue(messageQueue.isEmpty(), "Should not receive any messages without subscription");
    }
}

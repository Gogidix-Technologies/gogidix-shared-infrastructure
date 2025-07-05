package com.gogidix.shared.ecommerce.admin.websocket;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsocial.ecommerce.admin.config.TestWebSocketConfig;
import com.microsocial.ecommerce.admin.events.Event;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private BlockingQueue<String> messageQueue;
    
    @BeforeEach
    public void setup() throws Exception {
        messageQueue = new LinkedBlockingQueue<>();
        
        // Configure WebSocket client
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        Transport webSocketTransport = new WebSocketTransport(webSocketClient);
        SockJsClient sockJsClient = new SockJsClient(List.of(webSocketTransport));
        
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        
        // Connect to the WebSocket server
        String url = "ws://localhost:" + port + TestWebSocketConfig.WS_ENDPOINT;
        stompSession = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        
        // Subscribe to the test topic
        stompSession.subscribe(TestWebSocketConfig.TOPIC_PREFIX + "/test", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((String) payload);
            }
        });
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }
    
    @Test
    public void testWebSocketConnection() throws Exception {
        assertTrue(stompSession.isConnected(), "WebSocket session should be connected");
    }
    
    @Test
    public void testSendAndReceiveMessage() throws Exception {
        // Send a test message
        String testMessage = "Test message " + System.currentTimeMillis();
        stompSession.send("/app/test", testMessage);
        
        // Wait for the message to be received
        String received = messageQueue.poll(5, TimeUnit.SECONDS);
        
        assertNotNull(received, "No message received");
        assertTrue(received.contains(testMessage), "Received message doesn't contain the test message");
    }
    
    @Test
    public void testEventBroadcast() throws Exception {
        // Create a test event
        Event testEvent = new Event("test.event", "test-source") {
            @Override
            public String getEventType() {
                return "test.event";
            }
        };
        
        // Convert event to JSON
        String eventJson = objectMapper.writeValueAsString(testEvent);
        
        // Send the event
        stompSession.send("/app/events", eventJson);
        
        // Wait for the event to be broadcast
        String received = messageQueue.poll(5, TimeUnit.SECONDS);
        
        assertNotNull(received, "No event received");
        assertTrue(received.contains(testEvent.getEventId()), "Received event doesn't match the sent event");
    }
    
    @Test
    public void testAuthentication() throws Exception {
        // Test authentication endpoint
        stompSession.send("/app/auth/status", "");
        
        // Wait for the response
        String response = messageQueue.poll(5, TimeUnit.SECONDS);
        
        assertNotNull(response, "No authentication status received");
        assertTrue(response.contains("authenticated"), "Response should contain authentication status");
    }
}

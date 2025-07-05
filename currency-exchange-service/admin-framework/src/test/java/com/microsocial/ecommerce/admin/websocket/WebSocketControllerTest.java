package com.gogidix.shared.ecommerce.admin.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.security.Principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsocial.ecommerce.admin.websocket.acknowledge.AcknowledgmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
public class WebSocketControllerTest {

    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private AcknowledgmentService acknowledgmentService;
    
    @Mock
    private Principal principal;
    
    private WebSocketController controller;
    
    @BeforeEach
    void setUp() {
        controller = new WebSocketController(objectMapper, acknowledgmentService);
        when(principal.getName()).thenReturn("testuser");
    }
    
    @Test
    void testHandleEvent_WithAck() throws Exception {
        // Given
        String messageId = "msg-123";
        String sessionId = "sess-123";
        String payload = "test message";
        
        // Mock the acknowledgment service to return our expected result
        String expectedResponse = "{\"status\":\"received\",\"message\":\"test\"}";
        when(acknowledgmentService.processWithAck(any(Message.class), any())).thenReturn(expectedResponse);
        
        // Create a message with ack header
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setHeader("message-id", messageId);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setUser(principal);
        
        Message<String> message = MessageBuilder.createMessage(payload, headerAccessor.getMessageHeaders());
        
        // When
        String result = controller.handleEvent(message, principal);
        
        // Then
        assertEquals(expectedResponse, result);
        
        // Verify the acknowledgment service was called with the right parameters
        ArgumentCaptor<AcknowledgmentService.MessageProcessor<String, String>> processorCaptor = 
            ArgumentCaptor.forClass(AcknowledgmentService.MessageProcessor.class);
            
        verify(acknowledgmentService).processWithAck(eq(message), processorCaptor.capture());
        
        // Verify the processor does what we expect
        String processed = processorCaptor.getValue().process(payload);
        assertNotNull(processed);
        assertTrue(processed.contains("\"status\":\"received\""));
    }
    
    @Test
    void testHandleAuthStatus_WithAck() throws Exception {
        // Given
        String messageId = "auth-123";
        String sessionId = "sess-123";
        
        // Mock the acknowledgment service to return our expected result
        String expectedResponse = "{\"authenticated\":true,\"username\":\"testuser\"}";
        when(acknowledgmentService.processWithAck(any(Message.class), any())).thenReturn(expectedResponse);
        
        // Create a message with ack header
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setHeader("message-id", messageId);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setUser(principal);
        
        Message<String> message = MessageBuilder.createMessage("", headerAccessor.getMessageHeaders());
        
        // When
        String result = controller.handleAuthStatus(message, principal);
        
        // Then
        assertEquals(expectedResponse, result);
        
        // Verify the acknowledgment service was called with the right parameters
        ArgumentCaptor<AcknowledgmentService.MessageProcessor<Object, String>> processorCaptor = 
            ArgumentCaptor.forClass(AcknowledgmentService.MessageProcessor.class);
            
        verify(acknowledgmentService).processWithAck(eq(message), processorCaptor.capture());
        
        // Verify the processor does what we expect
        String processed = processorCaptor.getValue().process(null);
        assertNotNull(processed);
        assertTrue(processed.contains("\"authenticated\":true"));
        assertTrue(processed.contains("\"username\":\"testuser\""));
    }
    
    @Test
    void testHandleSubscribe() {
        // When
        String response = controller.handleSubscribe();
        
        // Then
        assertEquals("Subscribed to general events", response);
    }
}

package com.exalt.shared.ecommerce.admin.websocket.acknowledge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
public class AcknowledgmentServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private Principal principal;
    
    private AcknowledgmentService acknowledgmentService;
    
    @BeforeEach
    void setUp() {
        acknowledgmentService = new AcknowledgmentService(messagingTemplate);
        when(principal.getName()).thenReturn("testuser");
    }
    
    @Test
    void testRegisterAndAcknowledge() {
        // Given
        String messageId = "test-message-123";
        String destination = "/topic/test";
        String sessionId = "session-123";
        
        // When
        acknowledgmentService.registerForAck(messageId, destination, sessionId, principal, 3, 1000);
        boolean result = acknowledgmentService.acknowledge(messageId);
        
        // Then
        assertTrue(result, "Message should be acknowledged successfully");
        
        // Verify the message was removed after acknowledgment
        assertFalse(acknowledgmentService.acknowledge(messageId), "Message should not exist after acknowledgment");
    }
    
    @Test
    void testProcessWithAck_Success() {
        // Given
        String messageId = "test-message-123";
        String sessionId = "session-123";
        
        Message<String> message = createTestMessage(messageId, sessionId, "test-payload");
        
        // When
        String result = acknowledgmentService.processWithAck(
            message,
            payload -> "processed: " + payload
        );
        
        // Then
        assertEquals("processed: test-payload", result);
        
        // Verify acknowledgment was sent
        ArgumentCaptor<AcknowledgmentService.Acknowledgment> ackCaptor = 
            ArgumentCaptor.forClass(AcknowledgmentService.Acknowledgment.class);
            
        verify(messagingTemplate).convertAndSendToUser(
            eq(sessionId),
            eq("/queue/ack"),
            ackCaptor.capture(),
            any()
        );
        
        AcknowledgmentService.Acknowledgment ack = ackCaptor.getValue();
        assertEquals(messageId, ack.getMessageId());
        assertTrue(ack.isSuccess());
        assertEquals("Message processed successfully", ack.getMessage());
    }
    
    @Test
    void testProcessWithAck_NoAckRequired() {
        // Given - message with no message-id header
        Message<String> message = createTestMessage(null, "session-123", "test-payload");
        
        // When
        String result = acknowledgmentService.processWithAck(
            message,
            payload -> "processed: " + payload
        );
        
        // Then
        assertEquals("processed: test-payload", result);
        
        // Verify no acknowledgment was sent
        verifyNoInteractions(messagingTemplate);
    }
    
    @Test
    void testProcessWithAck_Exception() {
        // Given
        String messageId = "test-message-123";
        String sessionId = "session-123";
        
        Message<String> message = createTestMessage(messageId, sessionId, "test-payload");
        
        // When/Then
        assertThrows(RuntimeException.class, () -> 
            acknowledgmentService.processWithAck(
                message,
                payload -> { throw new RuntimeException("Test error"); }
            )
        );
        
        // Verify error acknowledgment was sent
        ArgumentCaptor<AcknowledgmentService.Acknowledgment> ackCaptor = 
            ArgumentCaptor.forClass(AcknowledgmentService.Acknowledgment.class);
            
        verify(messagingTemplate).convertAndSendToUser(
            eq(sessionId),
            eq("/queue/ack"),
            ackCaptor.capture(),
            any()
        );
        
        AcknowledgmentService.Acknowledgment ack = ackCaptor.getValue();
        assertEquals(messageId, ack.getMessageId());
        assertFalse(ack.isSuccess());
        assertTrue(ack.getMessage().contains("Test error"));
    }
    
    private <T> Message<T> createTestMessage(String messageId, String sessionId, T payload) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        if (messageId != null) {
            headerAccessor.setHeader("message-id", messageId);
        }
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        
        return MessageBuilder.createMessage(payload, headerAccessor.getMessageHeaders());
    }
}

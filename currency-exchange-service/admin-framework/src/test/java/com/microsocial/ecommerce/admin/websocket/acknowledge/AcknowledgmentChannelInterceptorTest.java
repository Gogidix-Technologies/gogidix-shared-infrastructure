package com.gogidix.shared.ecommerce.admin.websocket.acknowledge;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
public class AcknowledgmentChannelInterceptorTest {

    @Mock
    private AcknowledgmentService acknowledgmentService;
    
    @Mock
    private MessageChannel channel;
    
    @Mock
    private Principal principal;
    
    private AcknowledgmentChannelInterceptor interceptor;
    
    @BeforeEach
    void setUp() {
        interceptor = new AcknowledgmentChannelInterceptor(acknowledgmentService);
        when(principal.getName()).thenReturn("testuser");
    }
    
    @Test
    void testPreSend_MessageWithAckHeader() {
        // Given
        String messageId = "msg-123";
        String destination = "/topic/test";
        String sessionId = "sess-123";
        
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination(destination);
        accessor.setSessionId(sessionId);
        accessor.setUser(principal);
        accessor.setHeader("message-id", messageId);
        
        Message<String> message = MessageBuilder.createMessage("test payload", accessor.getMessageHeaders());
        
        // When
        interceptor.preSend(message, channel);
        
        // Then
        verify(acknowledgmentService).registerForAck(
            eq(messageId),
            eq(destination),
            eq(sessionId),
            eq(principal),
            eq(3),    // default maxRetries
            eq(5000L) // default retryDelay
        );
    }
    
    @Test
    void testPreSend_MessageWithReceipt() {
        // Given
        String receiptId = "receipt-123";
        String destination = "/topic/test";
        String sessionId = "sess-123";
        
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination(destination);
        accessor.setSessionId(sessionId);
        accessor.setUser(principal);
        accessor.setReceipt(receiptId);
        
        Message<String> message = MessageBuilder.createMessage("test payload", accessor.getMessageHeaders());
        
        // When
        interceptor.preSend(message, channel);
        
        // Then - should use receipt ID as message ID
        verify(acknowledgmentService).registerForAck(
            eq(receiptId),
            eq(destination),
            eq(sessionId),
            eq(principal),
            eq(3),    // default maxRetries
            eq(5000L) // default retryDelay
        );
    }
    
    @Test
    void testPreSend_AckMessage() {
        // Given
        String messageId = "msg-123";
        
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ACK);
        accessor.setAck(messageId);
        accessor.setSessionId("sess-123");
        
        Message<String> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());
        
        // When
        interceptor.preSend(message, channel);
        
        // Then
        verify(acknowledgmentService).acknowledge(messageId);
    }
    
    @Test
    void testPreSend_NackMessage() {
        // Given
        String messageId = "msg-123";
        
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.NACK);
        accessor.setAck(messageId);
        accessor.setSessionId("sess-123");
        
        Message<String> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());
        
        // When
        interceptor.preSend(message, channel);
        
        // Then - should log but not call acknowledge
        verify(acknowledgmentService, never()).acknowledge(any());
    }
    
    @Test
    void testPreSend_ReceiptMessage() {
        // Given
        String receiptId = "receipt-123";
        
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
        accessor.setReceiptId(receiptId);
        accessor.setSessionId("sess-123");
        
        Message<String> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());
        
        // When
        interceptor.preSend(message, channel);
        
        // Then - should log but not call acknowledge
        verify(acknowledgmentService, never()).acknowledge(any());
    }
}

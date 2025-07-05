package com.gogidix.ecosystem.shared.admin.websocket.acknowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Channel interceptor that handles acknowledgment of WebSocket messages.
 */
@Component
public class AcknowledgmentChannelInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AcknowledgmentChannelInterceptor.class);
    
    private static final String ACK_HEADER = "message-id";
    private static final String ACK_HEADER_RECEIPT = "receipt";
    
    private final AcknowledgmentService acknowledgmentService;
    
    public AcknowledgmentChannelInterceptor(AcknowledgmentService acknowledgmentService) {
        this.acknowledgmentService = acknowledgmentService;
    }
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }
        
        StompCommand command = accessor.getCommand();
        
        try {
            switch (command) {
                case SEND:
                    handleSendMessage(message, accessor);
                    break;
                    
                case ACK:
                    handleAckMessage(accessor);
                    break;
                    
                case NACK:
                    handleNackMessage(accessor);
                    break;
                    
                case RECEIPT:
                    handleReceiptMessage(accessor);
                    break;
                    
                default:
                    // Ignore other commands
                    break;
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: {}", e.getMessage(), e);
        }
        
        return message;
    }
    
    private void handleSendMessage(Message<?> message, StompHeaderAccessor accessor) {
        String messageId = accessor.getFirstNativeHeader(ACK_HEADER);
        String receiptId = accessor.getReceipt();
        
        if (messageId == null && receiptId == null) {
            // No acknowledgment requested
            return;
        }
        
        // Use receipt ID as message ID if no explicit message ID is provided
        if (messageId == null) {
            messageId = receiptId;
        }
        
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();
        
        // Register the message for acknowledgment
        if (messageId != null && sessionId != null) {
            acknowledgmentService.registerForAck(
                messageId,
                accessor.getDestination(),
                sessionId,
                principal,
                3, // maxRetries
                5000 // retryDelay in ms
            );
            
            logger.debug("Registered message for acknowledgment - ID: {}, Destination: {}", 
                messageId, accessor.getDestination());
        }
    }
    
    private void handleAckMessage(StompHeaderAccessor accessor) {
        String messageId = accessor.getAck();
        if (messageId != null) {
            boolean acknowledged = acknowledgmentService.acknowledge(messageId);
            logger.debug("Received ACK for message: {} - {}", messageId, 
                acknowledged ? "Found and processed" : "Not found");
        }
    }
    
    private void handleNackMessage(StompHeaderAccessor accessor) {
        String messageId = accessor.getAck();
        if (messageId != null) {
            logger.warn("Received NACK for message: {}", messageId);
            // TODO: Handle negative acknowledgment (e.g., retry logic)
        }
    }
    
    private void handleReceiptMessage(StompHeaderAccessor accessor) {
        String receiptId = accessor.getReceiptId();
        if (receiptId != null) {
            logger.debug("Received RECEIPT for: {}", receiptId);
            // Receipts are handled automatically by the STOMP protocol
        }
    }
}

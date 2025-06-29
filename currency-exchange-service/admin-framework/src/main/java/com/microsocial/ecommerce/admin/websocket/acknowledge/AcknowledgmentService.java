package com.exalt.shared.ecommerce.admin.websocket.acknowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling WebSocket message acknowledgment.
 */
@Service
public class AcknowledgmentService {
    private static final Logger logger = LoggerFactory.getLogger(AcknowledgmentService.class);
    
    private static final String ACK_HEADER = "message-id";
    private static final String ACK_DESTINATION = "/queue/ack";
    
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, PendingAck> pendingAcks = new ConcurrentHashMap<>();
    
    public AcknowledgmentService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Register a message for acknowledgment.
     * 
     * @param messageId The message ID
     * @param destination The destination to send the acknowledgment to
     * @param sessionId The WebSocket session ID
     * @param principal The authenticated user principal
     * @param maxRetries Maximum number of retry attempts
     * @param retryDelay Delay between retries in milliseconds
     */
    public void registerForAck(String messageId, String destination, String sessionId, 
                              Principal principal, int maxRetries, long retryDelay) {
        if (messageId == null || messageId.isEmpty()) {
            throw new IllegalArgumentException("Message ID cannot be null or empty");
        }
        
        PendingAck pendingAck = new PendingAck(
            messageId, 
            destination, 
            sessionId, 
            principal,
            maxRetries,
            retryDelay,
            System.currentTimeMillis()
        );
        
        pendingAcks.put(messageId, pendingAck);
        logger.debug("Registered message for acknowledgment: {}", messageId);
    }
    
    /**
     * Acknowledge a message was received and processed.
     * 
     * @param messageId The message ID to acknowledge
     * @return true if the message was found and acknowledged, false otherwise
     */
    public boolean acknowledge(String messageId) {
        PendingAck ack = pendingAcks.remove(messageId);
        if (ack != null) {
            logger.debug("Acknowledged message: {}", messageId);
            return true;
        }
        logger.warn("Received acknowledgment for unknown message: {}", messageId);
        return false;
    }
    
    /**
     * Process a message that requires acknowledgment.
     * 
     * @param message The message to process
     * @param processor The message processor
     * @return The result of processing the message
     */
    public <T, R> R processWithAck(Message<T> message, MessageProcessor<T, R> processor) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(message);
        String messageId = headers.getFirstNativeHeader(ACK_HEADER);
        
        if (messageId == null || messageId.isEmpty()) {
            // No acknowledgment required, just process the message
            return processor.process(message.getPayload());
        }
        
        try {
            // Process the message
            R result = processor.process(message.getPayload());
            
            // Send acknowledgment
            sendAck(messageId, headers.getSessionId(), true, "Message processed successfully");
            
            return result;
        } catch (Exception e) {
            // Send error acknowledgment
            sendAck(messageId, headers.getSessionId(), false, "Error processing message: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Send an acknowledgment message to the client.
     */
    private void sendAck(String messageId, String sessionId, boolean success, String message) {
        if (messageId == null || sessionId == null) {
            return;
        }
        
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        
        Acknowledgment ack = new Acknowledgment(messageId, success, message, System.currentTimeMillis());
        
        messagingTemplate.convertAndSendToUser(
            sessionId,
            ACK_DESTINATION,
            ack,
            headerAccessor.getMessageHeaders()
        );
        
        logger.debug("Sent acknowledgment for message {}: {}", messageId, success ? "SUCCESS" : "FAILURE");
    }
    
    /**
     * Interface for message processors that support acknowledgment.
     */
    @FunctionalInterface
    public interface MessageProcessor<T, R> {
        R process(T message) throws Exception;
    }
    
    /**
     * Represents a pending acknowledgment.
     */
    private static class PendingAck {
        final String messageId;
        final String destination;
        final String sessionId;
        final Principal principal;
        final int maxRetries;
        final long retryDelay;
        final long timestamp;
        int retryCount = 0;
        
        PendingAck(String messageId, String destination, String sessionId, 
                  Principal principal, int maxRetries, long retryDelay, long timestamp) {
            this.messageId = messageId;
            this.destination = destination;
            this.sessionId = sessionId;
            this.principal = principal;
            this.maxRetries = maxRetries;
            this.retryDelay = retryDelay;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Acknowledgment message DTO.
     */
    public static class Acknowledgment {
        private final String messageId;
        private final boolean success;
        private final String message;
        private final long timestamp;
        
        public Acknowledgment(String messageId, boolean success, String message, long timestamp) {
            this.messageId = messageId;
            this.success = success;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getMessageId() { return messageId; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}

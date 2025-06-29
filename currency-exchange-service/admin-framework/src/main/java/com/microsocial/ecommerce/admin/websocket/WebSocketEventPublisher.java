package com.exalt.shared.ecommerce.admin.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsocial.ecommerce.admin.events.Event;
import com.microsocial.ecommerce.admin.events.EventSubscriber;

/**
 * WebSocket implementation of EventSubscriber that broadcasts events to WebSocket clients.
 */
@Component
public class WebSocketEventPublisher implements EventSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventPublisher.class);
    
    private static final String WS_EVENTS_TOPIC = "/topic/events";
    private static final String WS_USER_EVENTS_QUEUE = "/queue/events";
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    public WebSocketEventPublisher(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public boolean supportsEvent(Event event) {
        // Support all event types by default
        return true;
    }
    
    @Override
    public boolean onEvent(Event event) {
        try {
            String destination = WS_EVENTS_TOPIC;
            String payload = objectMapper.writeValueAsString(convertToWsEvent(event));
            
            // Broadcast to all subscribers
            messagingTemplate.convertAndSend(destination, payload);
            
            // If event has a specific user target, send it to their personal queue
            if (event.getTargetUserId() != null) {
                String userDestination = String.format("%s%s-%s", 
                    WebSocketEventPublisher.WS_USER_EVENTS_QUEUE, 
                    event.getTargetUserId(),
                    event.getEventType().toLowerCase().replace('.', '-'));
                messagingTemplate.convertAndToUser(
                    event.getTargetUserId(), 
                    userDestination, 
                    payload);
            }
            
            return true;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event for WebSocket: " + event, e);
            return false;
        } catch (Exception e) {
            logger.error("Error publishing event to WebSocket: " + event, e);
            return false;
        }
    }
    
    private WebSocketEvent convertToWsEvent(Event event) {
        WebSocketEvent wsEvent = new WebSocketEvent();
        wsEvent.setId(event.getEventId());
        wsEvent.setType(event.getEventType());
        wsEvent.setTimestamp(event.getTimestamp().toString());
        wsEvent.setSource(event.getSource());
        wsEvent.setPayload(event);
        return wsEvent;
    }
    
    /**
     * WebSocket event DTO for consistent event structure over the wire.
     */
    public static class WebSocketEvent {
        private String id;
        private String type;
        private String timestamp;
        private String source;
        private Object payload;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }
    }
}

package com.exalt.ecosystem.shared.admin.websocket;

import com.exalt.ecosystem.shared.admin.websocket.acknowledge.AcknowledgmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.security.Principal;

/**
 * WebSocket controller for handling real-time messaging.
 */
@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    private final ObjectMapper objectMapper;
    private final AcknowledgmentService acknowledgmentService;
    
    public WebSocketController(ObjectMapper objectMapper, AcknowledgmentService acknowledgmentService) {
        this.objectMapper = objectMapper;
        this.acknowledgmentService = acknowledgmentService;
    }
    
    /**
     * Handle subscription to the general events topic.
     */
    @SubscribeMapping(WebSocketConfig.TOPIC_PREFIX + "/events")
    public String handleSubscribe() {
        return "Subscribed to general events";
    }
    
    /**
     * Handle incoming WebSocket messages with acknowledgment support.
     * 
     * Clients can include a 'message-id' header to request acknowledgment.
     * The acknowledgment will be sent to '/queue/ack' with the same message ID.
     */
    @MessageMapping("/events")
    @SendToUser(WebSocketConfig.QUEUE_PREFIX + "/events/response")
    public String handleEvent(Message<String> message, Principal principal) {
        return acknowledgmentService.processWithAck(
            message,
            payload -> {
                logger.info("Processing WebSocket message from {}: {}", 
                    principal != null ? principal.getName() : "anonymous", 
                    payload);
                
                // Process the message (in this case, just echo it back with a timestamp)
                return objectMapper.createObjectNode()
                    .put("status", "received")
                    .put("message", "Message processed successfully")
                    .put("originalMessage", payload)
                    .put("timestamp", System.currentTimeMillis())
                    .toString();
            }
        );
    }
    
    /**
     * Handle authentication status requests with acknowledgment support.
     */
    @MessageMapping("/auth/status")
    @SendToUser(WebSocketConfig.QUEUE_PREFIX + "/auth/status")
    public String handleAuthStatus(Message<?> message, Principal principal) {
        return acknowledgmentService.processWithAck(
            message,
            payload -> objectMapper.createObjectNode()
                .put("authenticated", principal != null)
                .put("username", principal != null ? principal.getName() : "anonymous")
                .put("timestamp", System.currentTimeMillis())
                .toString()
        );
    }
}

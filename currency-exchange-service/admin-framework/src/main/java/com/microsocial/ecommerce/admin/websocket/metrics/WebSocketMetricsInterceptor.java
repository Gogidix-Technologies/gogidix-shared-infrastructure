package com.exalt.shared.ecommerce.admin.websocket.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Interceptor for collecting WebSocket metrics.
 */
@Component
public class WebSocketMetricsInterceptor implements ChannelInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketMetricsInterceptor.class);
    private final WebSocketMetricsCollector metricsCollector;
    
    public WebSocketMetricsInterceptor(WebSocketMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            String sessionId = accessor.getSessionId();
            
            switch (accessor.getCommand()) {
                case CONNECT:
                    metricsCollector.incrementConnections(sessionId);
                    logger.debug("WebSocket connection established: {}", sessionId);
                    break;
                    
                case DISCONNECT:
                    metricsCollector.decrementConnections(sessionId);
                    logger.debug("WebSocket connection closed: {}", sessionId);
                    break;
                    
                case SEND:
                case SUBSCRIBE:
                case UNSUBSCRIBE:
                    String destination = accessor.getDestination();
                    logger.trace("WebSocket {}: {} -> {}", 
                        accessor.getCommand(), 
                        sessionId, 
                        destination);
                    break;
                    
                case ERROR:
                    String errorMessage = "Unknown error";
                    if (message.getPayload() instanceof byte[]) {
                        errorMessage = new String((byte[]) message.getPayload());
                    }
                    metricsCollector.recordError("websocket_error");
                    logger.error("WebSocket error [{}]: {}", sessionId, errorMessage);
                    break;
                    
                default:
                    break;
            }
        }
        
        return message;
    }
    
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            metricsCollector.recordError("send_error");
            logger.error("Error sending WebSocket message: {}", ex.getMessage(), ex);
        }
    }
}

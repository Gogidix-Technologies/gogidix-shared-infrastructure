package com.gogidix.ecosystem.shared.admin.websocket.ratelimit;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import com.gogidix.ecosystem.shared.admin.websocket.ratelimit.metrics.RateLimitMetrics;

/**
 * Interceptor that enforces rate limiting on WebSocket connections and messages.
 */
public class RateLimitInterceptor implements ChannelInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private static final String RATE_LIMIT_EXCEEDED = "rate_limit_exceeded";
    
    private final RateLimitProperties properties;
    private final RateLimitMetrics metrics;
    private final Map<String, RateLimitCounter> connectionCounts = new ConcurrentHashMap<>();
    private final Map<String, RateLimitCounter> messageCounts = new ConcurrentHashMap<>();
    
    public RateLimitInterceptor(RateLimitProperties properties, RateLimitMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
    }
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (!properties.isEnabled()) {
            return message;
        }
        
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        
        String sessionId = accessor.getSessionId();
        if (sessionId == null) {
            return message;
        }
        
        // Handle connection limits
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return handleConnect(message, accessor, sessionId);
        }
        
        // Handle message limits
        if (StompCommand.SEND.equals(accessor.getCommand()) || 
            StompCommand.SUBSCRIBE.equals(accessor.getCommand()) ||
            StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            return handleMessage(message, accessor, sessionId);
        }
        
        return message;
    }
    
    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor, String sessionId) {
        String ipAddress = getClientIpAddress(accessor);
        if (ipAddress == null) {
            logger.warn("Could not determine IP address for session: {}", sessionId);
            return message;
        }
        
        // Check connection limit per IP
        RateLimitCounter ipCounter = connectionCounts.computeIfAbsent(
            ipAddress, 
            k -> new RateLimitCounter(properties.getConnectionsPerIp(), properties.getTimeWindowSeconds())
        );
        
        if (!ipCounter.incrementAndCheck()) {
            logger.warn("Connection limit exceeded for IP: {}", ipAddress);
            metrics.incrementConnectionLimitExceeded();
            if (properties.isBlockOnLimitExceeded()) {
                throw new RateLimitExceededException("Connection limit exceeded for IP: " + ipAddress);
            }
        } else if (properties.getMonitoring().isDetailedLogging()) {
            logger.debug("Connection allowed for IP: {}, current count: {}", 
                ipAddress, ipCounter.getCurrentCount());
        }
        
        return message;
    }
    
    private Message<?> handleMessage(Message<?> message, StompHeaderAccessor accessor, String sessionId) {
        RateLimitCounter counter = messageCounts.computeIfAbsent(
            sessionId,
            k -> new RateLimitCounter(properties.getMessagesPerSecond(), 1) // Per second window
        );
        
        if (!counter.incrementAndCheck()) {
            logger.warn("Message rate limit exceeded for session: {}", sessionId);
            metrics.incrementRateLimitExceeded();
            metrics.incrementMessagesBlocked();
            if (properties.isBlockOnLimitExceeded()) {
                throw new RateLimitExceededException("Message rate limit exceeded");
            }
            return null; // Drop the message
        } else {
            metrics.incrementMessagesAllowed();
            if (properties.getMonitoring().isDetailedLogging()) {
                logger.debug("Message allowed for session: {}, current count: {}", 
                    sessionId, counter.getCurrentCount());
            }
        }
        
        return message;
    }
    
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        messageCounts.remove(sessionId);
        
        // Clean up connection counters for the IP if no more connections
        String ipAddress = getClientIpAddress(session);
        if (ipAddress != null) {
            RateLimitCounter counter = connectionCounts.get(ipAddress);
            if (counter != null && counter.decrementAndGet() <= 0) {
                connectionCounts.remove(ipAddress);
            }
        }
    }
    
    private String getClientIpAddress(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            WebSocketSession session = (WebSocketSession) sessionAttributes.get(WebSocketSession.class.getName());
            if (session != null) {
                return getClientIpAddress(session);
            }
        }
        return null;
    }
    
    private String getClientIpAddress(WebSocketSession session) {
        InetSocketAddress remoteAddress = session.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : null;
    }
    
    /**
     * Simple counter with time-based expiration.
     */
    private static class RateLimitCounter {
        private final int limit;
        private final int windowSeconds;
        private volatile Instant windowStart;
        private final AtomicInteger count;
        
        public RateLimitCounter(int limit, int windowSeconds) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
            this.windowStart = Instant.now();
            this.count = new AtomicInteger(0);
        }
        
        public synchronized boolean incrementAndCheck() {
            Instant now = Instant.now();
            
            // Reset counter if window has passed
            if (now.isAfter(windowStart.plusSeconds(windowSeconds))) {
                windowStart = now;
                count.set(0);
            }
            
            // Check limit
            return count.incrementAndGet() <= limit;
        }
        
        public synchronized int decrementAndGet() {
            return count.decrementAndGet();
        }
        
        public int getCurrentCount() {
            return count.get();
        }
    }
    
    /**
     * Clean up old counters that are beyond the time window
     */
    public void cleanupOldCounters(long cutoffTime) {
        // Clean up connection counters
        connectionCounts.entrySet().removeIf(entry -> {
            RateLimitCounter counter = entry.getValue();
            return counter.windowStart.toEpochMilli() < cutoffTime;
        });
        
        // Clean up message counters  
        messageCounts.entrySet().removeIf(entry -> {
            RateLimitCounter counter = entry.getValue();
            return counter.windowStart.toEpochMilli() < cutoffTime;
        });
    }
    
    /**
     * Exception thrown when rate limits are exceeded.
     */
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}

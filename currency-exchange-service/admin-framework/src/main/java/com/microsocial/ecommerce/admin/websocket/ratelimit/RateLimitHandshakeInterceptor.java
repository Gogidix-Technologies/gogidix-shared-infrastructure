package com.exalt.shared.ecommerce.admin.websocket.ratelimit;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import com.microsocial.ecommerce.admin.websocket.ratelimit.metrics.RateLimitMetrics;

/**
 * Interceptor that enforces rate limits during the WebSocket handshake.
 */
public class RateLimitHandshakeInterceptor implements HandshakeInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitHandshakeInterceptor.class);
    private final RateLimitProperties properties;
    private final RateLimitMetrics metrics;
    
    public RateLimitHandshakeInterceptor(RateLimitProperties properties, RateLimitMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
    }
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!properties.isEnabled()) {
            return true;
        }
        
        // Get client IP address
        String ipAddress = getClientIpAddress(request);
        if (ipAddress == null) {
            logger.warn("Could not determine client IP address");
            return true;
        }
        
        // Store IP address for later use
        attributes.put("client.ip", ipAddress);
        
        if (properties.getMonitoring().isDetailedLogging()) {
            logger.info("New WebSocket connection from IP: {}", ipAddress);
        }
        
        return true;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                              WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
    
    private String getClientIpAddress(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String ipAddress = servletRequest.getServletRequest().getHeader("X-Forwarded-For");
            
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = servletRequest.getServletRequest().getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = servletRequest.getServletRequest().getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = servletRequest.getServletRequest().getRemoteAddr();
            }
            
            // If multiple IPs are in the header, take the first one
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
            
            return ipAddress;
        }
        return null;
    }
}

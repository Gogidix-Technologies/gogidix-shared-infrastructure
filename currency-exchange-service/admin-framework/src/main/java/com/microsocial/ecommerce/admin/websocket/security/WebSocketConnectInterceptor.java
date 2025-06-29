package com.exalt.shared.ecommerce.admin.websocket.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Intercepts WebSocket connection attempts to perform pre-connect validation.
 */
@Component
public class WebSocketConnectInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectInterceptor.class);
    private static final String TOKEN_HEADER = "X-Authorization";
    
    private final JwtTokenProvider tokenProvider;

    public WebSocketConnectInterceptor(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
        
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // Extract token from query parameters or headers
            String token = extractToken(servletRequest);
            
            if (token != null && tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                logger.info("WebSocket connection attempt by user: {}", username);
                attributes.put("username", username);
                return true;
            } else {
                logger.warn("WebSocket connection rejected: Invalid or missing token");
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                              WebSocketHandler wsHandler, Exception exception) {
        // Cleanup if needed
    }
    
    private String extractToken(ServletServerHttpRequest request) {
        // Check query parameter first
        String token = request.getServletRequest().getParameter("token");
        
        // Then check headers
        if (token == null) {
            String authHeader = request.getHeaders().getFirst(TOKEN_HEADER);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        return token;
    }
}

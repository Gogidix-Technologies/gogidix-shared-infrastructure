package com.exalt.shared.ecommerce.admin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.StringUtils;

/**
 * Intercepts WebSocket messages to handle authentication and authorization.
 */
public class WebSocketChannelInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketChannelInterceptor.class);
    
    private static final String TOKEN_HEADER = "X-Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    private final JwtDecoder jwtDecoder;
    
    public WebSocketChannelInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            
            if (token != null) {
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        jwt.getSubject(), 
                        null,
                        jwt.getClaimAsStringList("scope")
                            .stream()
                            .map(s -> (String) s)
                            .map(scope -> "SCOPE_" + scope)
                            .map(scope -> (String) scope)
                            .collect(java.util.stream.Collectors.toList())
                    );
                    
                    accessor.setUser(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                } catch (JwtException e) {
                    logger.warn("Failed to authenticate WebSocket connection: {}", e.getMessage());
                    throw new SecurityException("Authentication failed: " + e.getMessage());
                }
            } else {
                logger.warn("No authentication token found in WebSocket connection");
                throw new SecurityException("Authentication required");
            }
        }
        
        return message;
    }
    
    private String extractToken(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader(TOKEN_HEADER);
        
        if (StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }
        
        return null;
    }
}

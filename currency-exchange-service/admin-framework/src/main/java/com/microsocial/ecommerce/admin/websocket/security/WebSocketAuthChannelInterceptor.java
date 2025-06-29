package com.exalt.shared.ecommerce.admin.websocket.security;

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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Intercepts WebSocket messages to authenticate and authorize requests.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);
    private static final String TOKEN_HEADER = "X-Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    private final JwtTokenProvider tokenProvider;

    public WebSocketAuthChannelInterceptor(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }

        // Handle CONNECT and SUBSCRIBE messages
        if (StompCommand.CONNECT.equals(accessor.getCommand()) || 
            StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            
            String token = resolveToken(accessor);
            
            if (token != null && tokenProvider.validateToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                if (authentication != null) {
                    accessor.setUser(authentication);
                    logger.debug("Authenticated WebSocket connection for user: {}", authentication.getName());
                }
            } else {
                logger.warn("WebSocket connection rejected: Invalid or missing token");
                throw new SecurityException("Authentication required");
            }
        }
        
        return message;
    }
    
    private String resolveToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader(TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}

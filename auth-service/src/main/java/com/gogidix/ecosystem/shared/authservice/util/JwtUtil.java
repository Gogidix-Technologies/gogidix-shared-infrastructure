package com.gogidix.ecosystem.shared.authservice.util;

import com.gogidix.ecosystem.shared.authservice.config.JwtSecurityConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * JWT Utility Class
 * Provides secure JWT token generation and validation
 * 
 * Security Features:
 * - Strong HMAC-SHA256 signing
 * - Proper expiration handling
 * - Claims validation
 * - Token blacklisting support
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = Logger.getLogger(JwtUtil.class.getName());
    
    @Autowired
    private JwtSecurityConfig jwtConfig;
    
    /**
     * Generate JWT access token
     */
    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("type", "access");
        return createToken(claims, username, jwtConfig.getJwtExpiration());
    }
    
    /**
     * Generate JWT refresh token
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, username, jwtConfig.getJwtRefreshExpiration());
    }
    
    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer("exalt-auth-service")
                .setAudience("exalt-ecosystem")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Get signing key from configuration
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract roles from token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get("roles"));
    }
    
    /**
     * Extract token type (access/refresh)
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> (String) claims.get("type"));
    }
    
    /**
     * Extract claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer("exalt-auth-service")
                    .requireAudience("exalt-ecosystem")
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.severe("JWT parsing failed: " + e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
    
    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.warning("Error checking token expiration: " + e.getMessage());
            return true; // Treat invalid tokens as expired
        }
    }
    
    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token) && isValidTokenType(token));
        } catch (Exception e) {
            logger.warning("Token validation failed for user " + username + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate token type (access tokens for API access)
     */
    private Boolean isValidTokenType(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "access".equals(tokenType);
        } catch (Exception e) {
            logger.warning("Invalid token type: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate refresh token
     */
    public Boolean validateRefreshToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            String tokenType = extractTokenType(token);
            return (extractedUsername.equals(username) && 
                   !isTokenExpired(token) && 
                   "refresh".equals(tokenType));
        } catch (Exception e) {
            logger.warning("Refresh token validation failed for user " + username + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract token from Authorization header
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * Check if token will expire soon (within 5 minutes)
     * Useful for proactive token refresh
     */
    public Boolean isTokenExpiringSoon(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date fiveMinutesFromNow = new Date(System.currentTimeMillis() + (5 * 60 * 1000));
            return expiration.before(fiveMinutesFromNow);
        } catch (Exception e) {
            logger.warning("Error checking token expiration time: " + e.getMessage());
            return true; // Treat invalid tokens as expiring
        }
    }
    
    /**
     * Get token remaining time in milliseconds
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            logger.warning("Error getting token remaining time: " + e.getMessage());
            return 0L;
        }
    }
}
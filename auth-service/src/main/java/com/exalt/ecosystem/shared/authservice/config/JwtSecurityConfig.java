package com.exalt.ecosystem.shared.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * JWT Security Configuration
 * Implements secure JWT secret management with environment variable validation
 * 
 * Security Fix: Replaces hardcoded JWT secret (CVSS 9.8)
 * Implementation: Environment variable based secret management
 */
@Component
@Configuration
public class JwtSecurityConfig {
    
    private static final Logger logger = Logger.getLogger(JwtSecurityConfig.class.getName());
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:3600000}")
    private Long jwtExpiration;
    
    @Value("${jwt.refresh-expiration:7200000}")
    private Long jwtRefreshExpiration;
    
    /**
     * Validate JWT configuration on startup
     * Ensures secure secret configuration
     */
    @PostConstruct
    public void validateConfiguration() {
        validateJwtSecret();
        validateExpirationTimes();
        logger.info("JWT Security Configuration validated successfully");
    }
    
    /**
     * Validate JWT secret meets security requirements
     */
    private void validateJwtSecret() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret is not configured. Please set JWT_SECRET environment variable. " +
                "Generate secure secret using: openssl rand -base64 64"
            );
        }
        
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 characters long for security. " +
                "Current length: " + jwtSecret.length() + ". " +
                "Generate secure secret using: openssl rand -base64 64"
            );
        }
        
        // Check for common weak secrets
        String[] weakSecrets = {"mySecretKey", "secret", "password", "test", "default"};
        for (String weakSecret : weakSecrets) {
            if (jwtSecret.toLowerCase().contains(weakSecret.toLowerCase())) {
                throw new IllegalStateException(
                    "JWT secret appears to contain weak/default values. " +
                    "Use a cryptographically secure random secret."
                );
            }
        }
        
        logger.info("JWT secret validation passed - using secure environment variable");
    }
    
    /**
     * Validate expiration time configurations
     */
    private void validateExpirationTimes() {
        if (jwtExpiration <= 0) {
            throw new IllegalStateException("JWT expiration must be positive");
        }
        
        if (jwtRefreshExpiration <= jwtExpiration) {
            throw new IllegalStateException("JWT refresh expiration must be greater than access token expiration");
        }
        
        // Warn if expiration times are too long for security
        long maxRecommendedExpiration = 2 * 60 * 60 * 1000; // 2 hours
        if (jwtExpiration > maxRecommendedExpiration) {
            logger.warning("JWT expiration time is longer than recommended 2 hours. " +
                          "Consider shorter expiration for better security.");
        }
        
        logger.info("JWT expiration configuration validated - Access: " + 
                   (jwtExpiration / 1000 / 60) + " minutes, Refresh: " + 
                   (jwtRefreshExpiration / 1000 / 60) + " minutes");
    }
    
    /**
     * Generate a secure JWT secret for development/testing
     * Production should use proper secrets management
     */
    public static String generateSecureSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] secretBytes = new byte[64]; // 512-bit key
        secureRandom.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
    }
    
    /**
     * Get the validated JWT secret
     */
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    /**
     * Get JWT expiration time
     */
    public Long getJwtExpiration() {
        return jwtExpiration;
    }
    
    /**
     * Get JWT refresh expiration time
     */
    public Long getJwtRefreshExpiration() {
        return jwtRefreshExpiration;
    }
    
    /**
     * Implement JWT secret rotation capability
     * This would be called during scheduled secret rotation
     */
    public void rotateSecret(String newSecret) {
        if (newSecret == null || newSecret.length() < 32) {
            throw new IllegalArgumentException("New JWT secret must be at least 32 characters");
        }
        
        // In production, this would:
        // 1. Validate new secret
        // 2. Update secrets manager
        // 3. Trigger service restart or hot reload
        // 4. Invalidate existing tokens if required
        
        logger.info("JWT secret rotation requested - implement with secrets manager");
    }
}
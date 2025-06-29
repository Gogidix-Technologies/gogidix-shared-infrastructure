package com.exalt.shared.ecommerce.admin.core;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for admin applications.
 * This class provides security settings and configurations
 * for admin applications, including authentication, authorization,
 * and access control.
 */
public class SecurityConfig {
    private List<String> allowedOrigins;
    private boolean csrfEnabled;
    private String tokenExpirationSeconds;
    private String jwtSecret;
    
    /**
     * Default constructor with secure defaults
     */
    public SecurityConfig() {
        this.allowedOrigins = Arrays.asList("https://admin.microsocial-ecommerce.com");
        this.csrfEnabled = true;
        this.tokenExpirationSeconds = "3600";
        this.jwtSecret = generateRandomSecret();
    }
    
    /**
     * Generate a random secret for JWT signing
     * 
     * @return A random secret string
     */
    private String generateRandomSecret() {
        return java.util.UUID.randomUUID().toString();
    }
    
    // Getters and setters
    
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
    
    public boolean isCsrfEnabled() {
        return csrfEnabled;
    }
    
    public void setCsrfEnabled(boolean csrfEnabled) {
        this.csrfEnabled = csrfEnabled;
    }
    
    public String getTokenExpirationSeconds() {
        return tokenExpirationSeconds;
    }
    
    public void setTokenExpirationSeconds(String tokenExpirationSeconds) {
        this.tokenExpirationSeconds = tokenExpirationSeconds;
    }
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
}

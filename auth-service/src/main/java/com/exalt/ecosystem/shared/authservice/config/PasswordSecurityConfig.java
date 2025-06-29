package com.exalt.ecosystem.shared.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import java.util.logging.Logger;

/**
 * Password Security Configuration
 * 
 * SECURITY FIX: Password Encryption Implementation (CVSS 9.1)
 * - Implements BCrypt password hashing with configurable strength
 * - Provides secure password encoding and validation
 * - Configures password policy enforcement
 * - Supports password security auditing
 */
@Configuration
public class PasswordSecurityConfig {
    
    private static final Logger logger = Logger.getLogger(PasswordSecurityConfig.class.getName());
    
    // BCrypt configuration
    @Value("${security.password.bcrypt.strength:12}")
    private int bcryptStrength;
    
    // Password policy configuration
    @Value("${security.password.min-length:12}")
    private int minPasswordLength;
    
    @Value("${security.password.require-uppercase:true}")
    private boolean requireUppercase;
    
    @Value("${security.password.require-lowercase:true}")
    private boolean requireLowercase;
    
    @Value("${security.password.require-numbers:true}")
    private boolean requireNumbers;
    
    @Value("${security.password.require-special-chars:true}")
    private boolean requireSpecialChars;
    
    @Value("${security.password.history-count:5}")
    private int passwordHistoryCount;
    
    @Value("${security.password.expiry-days:90}")
    private int passwordExpiryDays;
    
    @Value("${security.password.max-failed-attempts:5}")
    private int maxFailedAttempts;
    
    @Value("${security.password.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;
    
    /**
     * BCrypt Password Encoder Bean
     * Uses configurable strength (default: 12 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Configuring BCrypt password encoder with strength: " + bcryptStrength);
        return new BCryptPasswordEncoder(bcryptStrength);
    }
    
    /**
     * Validate password security configuration on startup
     */
    @PostConstruct
    public void validatePasswordConfiguration() {
        validateBCryptStrength();
        validatePasswordPolicy();
        logger.info("Password security configuration validated successfully");
    }
    
    /**
     * Validate BCrypt strength configuration
     */
    private void validateBCryptStrength() {
        if (bcryptStrength < 10) {
            logger.warning("BCrypt strength is set to " + bcryptStrength + 
                          ". Minimum recommended strength is 10 for security.");
        }
        
        if (bcryptStrength > 15) {
            logger.warning("BCrypt strength is set to " + bcryptStrength + 
                          ". This may cause performance issues. Maximum recommended is 15.");
        }
        
        if (bcryptStrength < 4 || bcryptStrength > 31) {
            throw new IllegalStateException(
                "BCrypt strength must be between 4 and 31. Current value: " + bcryptStrength
            );
        }
        
        logger.info("BCrypt strength configuration is valid: " + bcryptStrength + " rounds");
    }
    
    /**
     * Validate password policy configuration
     */
    private void validatePasswordPolicy() {
        if (minPasswordLength < 8) {
            throw new IllegalStateException(
                "Minimum password length must be at least 8 characters. Current value: " + minPasswordLength
            );
        }
        
        if (passwordHistoryCount < 1) {
            throw new IllegalStateException(
                "Password history count must be at least 1. Current value: " + passwordHistoryCount
            );
        }
        
        if (passwordExpiryDays < 1) {
            throw new IllegalStateException(
                "Password expiry days must be at least 1. Current value: " + passwordExpiryDays
            );
        }
        
        if (maxFailedAttempts < 1) {
            throw new IllegalStateException(
                "Max failed attempts must be at least 1. Current value: " + maxFailedAttempts
            );
        }
        
        if (lockoutDurationMinutes < 1) {
            throw new IllegalStateException(
                "Lockout duration must be at least 1 minute. Current value: " + lockoutDurationMinutes
            );
        }
        
        logger.info("Password policy configuration validated - " +
                   "Min length: " + minPasswordLength + ", " +
                   "History count: " + passwordHistoryCount + ", " +
                   "Expiry days: " + passwordExpiryDays + ", " +
                   "Max failed attempts: " + maxFailedAttempts + ", " +
                   "Lockout duration: " + lockoutDurationMinutes + " minutes");
    }
    
    // =============================================
    // GETTER METHODS FOR CONFIGURATION VALUES
    // =============================================
    
    public int getBcryptStrength() {
        return bcryptStrength;
    }
    
    public int getMinPasswordLength() {
        return minPasswordLength;
    }
    
    public boolean isRequireUppercase() {
        return requireUppercase;
    }
    
    public boolean isRequireLowercase() {
        return requireLowercase;
    }
    
    public boolean isRequireNumbers() {
        return requireNumbers;
    }
    
    public boolean isRequireSpecialChars() {
        return requireSpecialChars;
    }
    
    public int getPasswordHistoryCount() {
        return passwordHistoryCount;
    }
    
    public int getPasswordExpiryDays() {
        return passwordExpiryDays;
    }
    
    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }
    
    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
}
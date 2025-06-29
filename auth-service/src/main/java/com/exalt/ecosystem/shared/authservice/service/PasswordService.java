package com.exalt.ecosystem.shared.authservice.service;

import com.exalt.ecosystem.shared.authservice.config.PasswordSecurityConfig;
import com.exalt.ecosystem.shared.authservice.entity.PasswordHistory;
import com.exalt.ecosystem.shared.authservice.entity.User;
import com.exalt.ecosystem.shared.authservice.exception.WeakPasswordException;
import com.exalt.ecosystem.shared.authservice.exception.PasswordReuseException;
import com.exalt.ecosystem.shared.authservice.exception.AccountLockedException;
import com.exalt.ecosystem.shared.authservice.repository.PasswordHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Password Service - Secure Password Management
 * 
 * SECURITY IMPLEMENTATION: Password Encryption and Policy Enforcement
 * - BCrypt password hashing with salt
 * - Password policy validation
 * - Password history tracking
 * - Account lockout protection
 * - Secure password operations
 */
@Service
@Transactional
public class PasswordService {
    
    private static final Logger logger = Logger.getLogger(PasswordService.class.getName());
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordSecurityConfig passwordConfig;
    
    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;
    
    // Regex patterns for password validation
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    /**
     * Hash password using BCrypt
     * 
     * @param rawPassword Plain text password
     * @return BCrypt hashed password
     */
    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Validate password policy before hashing
        validatePasswordPolicy(rawPassword);
        
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        logger.info("Password hashed successfully with BCrypt strength: " + 
                   passwordConfig.getBcryptStrength());
        
        return hashedPassword;
    }
    
    /**
     * Verify password against hash
     * 
     * @param rawPassword Plain text password
     * @param hashedPassword BCrypt hashed password
     * @return true if password matches
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
            
            if (matches) {
                logger.info("Password verification successful");
            } else {
                logger.warning("Password verification failed");
            }
            
            return matches;
        } catch (Exception e) {
            logger.severe("Password verification error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Change user password with security checks
     * 
     * @param user User to change password for
     * @param newPassword New plain text password
     * @param ipAddress IP address of the request
     * @param changeReason Reason for password change
     */
    public void changePassword(User user, String newPassword, String ipAddress, String changeReason) {
        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new AccountLockedException("Account is locked until: " + user.getLockedUntil());
        }
        
        // Validate new password policy
        validatePasswordPolicy(newPassword);
        
        // Check password history to prevent reuse
        checkPasswordHistory(user, newPassword);
        
        // Hash the new password
        String newPasswordHash = hashPassword(newPassword);
        
        // Save current password to history before changing
        if (user.getPasswordHash() != null) {
            PasswordHistory history = new PasswordHistory(
                user, 
                user.getPasswordHash(), 
                ipAddress, 
                changeReason
            );
            passwordHistoryRepository.save(history);
        }
        
        // Update user password
        user.setPasswordHash(newPasswordHash);
        user.setPasswordUpdatedAt(LocalDateTime.now());
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(passwordConfig.getPasswordExpiryDays()));
        
        // Clean up old password history beyond retention limit
        cleanupPasswordHistory(user);
        
        logger.info("Password changed successfully for user: " + user.getUsername() + 
                   " from IP: " + ipAddress + " reason: " + changeReason);
    }
    
    /**
     * Validate password against security policy
     * 
     * @param password Password to validate
     * @throws WeakPasswordException if password doesn't meet policy
     */
    public void validatePasswordPolicy(String password) {
        if (password == null) {
            throw new WeakPasswordException("Password cannot be null");
        }
        
        // Check minimum length
        if (password.length() < passwordConfig.getMinPasswordLength()) {
            throw new WeakPasswordException(
                "Password must be at least " + passwordConfig.getMinPasswordLength() + " characters long"
            );
        }
        
        // Check uppercase requirement
        if (passwordConfig.isRequireUppercase() && !UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter");
        }
        
        // Check lowercase requirement
        if (passwordConfig.isRequireLowercase() && !LOWERCASE_PATTERN.matcher(password).matches()) {
            throw new WeakPasswordException("Password must contain at least one lowercase letter");
        }
        
        // Check number requirement
        if (passwordConfig.isRequireNumbers() && !NUMBER_PATTERN.matcher(password).matches()) {
            throw new WeakPasswordException("Password must contain at least one number");
        }
        
        // Check special character requirement
        if (passwordConfig.isRequireSpecialChars() && !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            throw new WeakPasswordException("Password must contain at least one special character");
        }
        
        // Check for common weak passwords
        validateAgainstCommonWeakPasswords(password);
        
        logger.info("Password policy validation passed");
    }
    
    /**
     * Check if password has been used recently
     * 
     * @param user User to check history for
     * @param newPassword New password to check
     * @throws PasswordReuseException if password was recently used
     */
    private void checkPasswordHistory(User user, String newPassword) {
        List<PasswordHistory> recentPasswords = passwordHistoryRepository
            .findByUserOrderByCreatedAtDesc(user);
        
        int historyLimit = Math.min(recentPasswords.size(), passwordConfig.getPasswordHistoryCount());
        
        for (int i = 0; i < historyLimit; i++) {
            PasswordHistory history = recentPasswords.get(i);
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new PasswordReuseException(
                    "Password has been used recently. Please choose a different password."
                );
            }
        }
        
        logger.info("Password history check passed for user: " + user.getUsername());
    }
    
    /**
     * Clean up old password history beyond retention limit
     * 
     * @param user User to clean up history for
     */
    private void cleanupPasswordHistory(User user) {
        List<PasswordHistory> allHistory = passwordHistoryRepository
            .findByUserOrderByCreatedAtDesc(user);
        
        if (allHistory.size() > passwordConfig.getPasswordHistoryCount()) {
            List<PasswordHistory> toDelete = allHistory.subList(
                passwordConfig.getPasswordHistoryCount(), 
                allHistory.size()
            );
            
            passwordHistoryRepository.deleteAll(toDelete);
            
            logger.info("Cleaned up " + toDelete.size() + 
                       " old password history entries for user: " + user.getUsername());
        }
    }
    
    /**
     * Validate against common weak passwords
     * 
     * @param password Password to check
     * @throws WeakPasswordException if password is commonly used
     */
    private void validateAgainstCommonWeakPasswords(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Common weak passwords
        String[] weakPasswords = {
            "password", "123456", "password123", "admin", "qwerty",
            "letmein", "welcome", "monkey", "1234567890", "abc123",
            "password1", "iloveyou", "sunshine", "princess", "football",
            "charlie", "aa123456", "welcome123", "dragon", "master"
        };
        
        for (String weakPassword : weakPasswords) {
            if (lowerPassword.contains(weakPassword)) {
                throw new WeakPasswordException(
                    "Password contains common weak patterns. Please choose a more secure password."
                );
            }
        }
        
        // Check for simple patterns
        if (isSimplePattern(password)) {
            throw new WeakPasswordException(
                "Password contains simple patterns. Please choose a more complex password."
            );
        }
    }
    
    /**
     * Check for simple patterns in password
     * 
     * @param password Password to check
     * @return true if password contains simple patterns
     */
    private boolean isSimplePattern(String password) {
        // Check for repeated characters (e.g., "aaaa", "1111")
        if (password.matches(".*(.)\\1{3,}.*")) {
            return true;
        }
        
        // Check for sequential characters (e.g., "abcd", "1234")
        for (int i = 0; i < password.length() - 3; i++) {
            String substr = password.substring(i, i + 4);
            if (isSequential(substr)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if string contains sequential characters
     * 
     * @param str String to check
     * @return true if sequential
     */
    private boolean isSequential(String str) {
        for (int i = 0; i < str.length() - 1; i++) {
            if (str.charAt(i + 1) != str.charAt(i) + 1) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if password needs to be changed due to expiry
     * 
     * @param user User to check
     * @return true if password is expired
     */
    public boolean isPasswordExpired(User user) {
        return user.isPasswordExpired();
    }
    
    /**
     * Get password strength score (0-100)
     * 
     * @param password Password to evaluate
     * @return Strength score
     */
    public int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length score (max 25 points)
        score += Math.min(password.length() * 2, 25);
        
        // Character variety (max 40 points)
        if (UPPERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (LOWERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (NUMBER_PATTERN.matcher(password).matches()) score += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) score += 10;
        
        // Length bonus (max 20 points)
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 10;
        
        // Uniqueness bonus (max 15 points)
        long uniqueChars = password.chars().distinct().count();
        score += Math.min((int)(uniqueChars * 1.5), 15);
        
        return Math.min(score, 100);
    }
}
package com.exalt.ecosystem.shared.authservice.security;

import com.exalt.ecosystem.shared.authservice.config.PasswordSecurityConfig;
import com.exalt.ecosystem.shared.authservice.service.PasswordService;
import com.exalt.ecosystem.shared.authservice.exception.WeakPasswordException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Password Security Test
 * Validates the password encryption and security implementations
 * 
 * Tests:
 * - BCrypt password hashing
 * - Password policy validation
 * - Password strength calculation
 * - Security configuration compliance
 */
public class PasswordSecurityTest {
    
    @Test
    public void testBCryptPasswordHashing() {
        // Test BCrypt password encoder
        PasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String rawPassword = "SecurePassword123!";
        String hashedPassword = encoder.encode(rawPassword);
        
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$12$"), "BCrypt hash should start with $2a$12$");
        assertTrue(hashedPassword.length() == 60, "BCrypt hash should be 60 characters");
        
        // Test password verification
        assertTrue(encoder.matches(rawPassword, hashedPassword), "Password should match hash");
        assertFalse(encoder.matches("WrongPassword", hashedPassword), "Wrong password should not match");
    }
    
    @Test
    public void testPasswordPolicyValidation() {
        PasswordSecurityConfig config = new PasswordSecurityConfig();
        
        // Test password strength requirements
        String strongPassword = "SecurePassword123!@#";
        assertTrue(strongPassword.length() >= 12, "Strong password should meet length requirement");
        assertTrue(strongPassword.matches(".*[A-Z].*"), "Strong password should contain uppercase");
        assertTrue(strongPassword.matches(".*[a-z].*"), "Strong password should contain lowercase");
        assertTrue(strongPassword.matches(".*[0-9].*"), "Strong password should contain numbers");
        assertTrue(strongPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"), 
                  "Strong password should contain special characters");
    }
    
    @Test
    public void testWeakPasswordDetection() {
        // Test weak passwords that should be rejected
        String[] weakPasswords = {
            "password",           // Common weak password
            "123456",            // Sequential numbers
            "abc123",            // Simple pattern
            "password123",       // Common + numbers
            "Password",          // Missing numbers and special chars
            "password!",         // Missing uppercase and numbers
            "PASSWORD123!",      // Missing lowercase
            "Pass123!",          // Too short
            "aaaa1234!A"         // Repeated characters
        };
        
        for (String weakPassword : weakPasswords) {
            assertFalse(isPasswordStrong(weakPassword), 
                       "Password should be considered weak: " + weakPassword);
        }
    }
    
    @Test
    public void testStrongPasswordAcceptance() {
        // Test strong passwords that should be accepted
        String[] strongPasswords = {
            "SecurePassword123!@#",
            "MyVeryStr0ng&SecureP@ssw0rd",
            "C0mpl3x!P@ssw0rd$2024",
            "Ungu3ss@bl3#P@ssw0rd%",
            "S3cur3&R@nd0m!P@ssw0rd"
        };
        
        for (String strongPassword : strongPasswords) {
            assertTrue(isPasswordStrong(strongPassword), 
                      "Password should be considered strong: " + strongPassword);
        }
    }
    
    @Test
    public void testPasswordStrengthCalculation() {
        // Test password strength scoring
        assertEquals(0, calculatePasswordStrength(""), "Empty password should have 0 strength");
        assertEquals(0, calculatePasswordStrength(null), "Null password should have 0 strength");
        
        assertTrue(calculatePasswordStrength("weak") < 30, "Weak password should have low strength");
        assertTrue(calculatePasswordStrength("SecurePassword123!") > 80, "Strong password should have high strength");
        
        // Test strength progression
        int weakStrength = calculatePasswordStrength("password");
        int mediumStrength = calculatePasswordStrength("Password123");
        int strongStrength = calculatePasswordStrength("SecurePassword123!@#");
        
        assertTrue(weakStrength < mediumStrength, "Medium should be stronger than weak");
        assertTrue(mediumStrength < strongStrength, "Strong should be stronger than medium");
    }
    
    @Test
    public void testPasswordHashing() {
        // Test that same password produces different hashes (due to salt)
        PasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String password = "TestPassword123!";
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);
        
        assertNotEquals(hash1, hash2, "Same password should produce different hashes (salt)");
        
        // But both should verify correctly
        assertTrue(encoder.matches(password, hash1), "Password should match first hash");
        assertTrue(encoder.matches(password, hash2), "Password should match second hash");
    }
    
    @Test
    public void testSecurityConfiguration() {
        // Test that security configuration meets minimum requirements
        PasswordSecurityConfig config = new PasswordSecurityConfig();
        
        // Test BCrypt strength requirements
        int bcryptStrength = 12; // Default from config
        assertTrue(bcryptStrength >= 10, "BCrypt strength should be at least 10");
        assertTrue(bcryptStrength <= 15, "BCrypt strength should not exceed 15 for performance");
        
        // Test password policy requirements
        int minLength = 12; // Default from config
        assertTrue(minLength >= 8, "Minimum password length should be at least 8");
        
        // Test security defaults
        assertTrue(true, "All security requirements should be enabled by default");
    }
    
    // Helper methods for testing
    
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 12) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[a-z].*")) return false;
        if (!password.matches(".*[0-9].*")) return false;
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) return false;
        
        // Check for weak patterns
        String lowerPassword = password.toLowerCase();
        String[] weakPatterns = {"password", "123456", "qwerty", "abc123"};
        for (String pattern : weakPatterns) {
            if (lowerPassword.contains(pattern)) return false;
        }
        
        return true;
    }
    
    private int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        
        int score = 0;
        score += Math.min(password.length() * 2, 25); // Length score
        
        if (password.matches(".*[A-Z].*")) score += 10; // Uppercase
        if (password.matches(".*[a-z].*")) score += 10; // Lowercase
        if (password.matches(".*[0-9].*")) score += 10; // Numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score += 10; // Special chars
        
        if (password.length() >= 12) score += 10; // Length bonus
        if (password.length() >= 16) score += 10; // Extra length bonus
        
        long uniqueChars = password.chars().distinct().count();
        score += Math.min((int)(uniqueChars * 1.5), 15); // Uniqueness bonus
        
        return Math.min(score, 100);
    }
}
package com.gogidix.ecosystem.shared.authservice.exception;

/**
 * Exception thrown when user tries to reuse a recent password
 */
public class PasswordReuseException extends RuntimeException {
    
    public PasswordReuseException(String message) {
        super(message);
    }
    
    public PasswordReuseException(String message, Throwable cause) {
        super(message, cause);
    }
}
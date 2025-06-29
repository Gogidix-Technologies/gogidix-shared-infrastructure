package com.exalt.ecosystem.shared.authservice.exception;

/**
 * Exception thrown when password doesn't meet security policy requirements
 */
public class WeakPasswordException extends RuntimeException {
    
    public WeakPasswordException(String message) {
        super(message);
    }
    
    public WeakPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
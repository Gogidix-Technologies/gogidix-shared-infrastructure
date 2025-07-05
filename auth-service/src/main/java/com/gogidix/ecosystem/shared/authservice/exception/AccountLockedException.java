package com.gogidix.ecosystem.shared.authservice.exception;

/**
 * Exception thrown when account is locked due to security policy
 */
public class AccountLockedException extends RuntimeException {
    
    public AccountLockedException(String message) {
        super(message);
    }
    
    public AccountLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
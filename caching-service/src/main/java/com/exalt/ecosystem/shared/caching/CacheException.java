package com.exalt.ecosystem.shared.caching;

/**
 * Exception thrown by cache operations.
 */
public class CacheException extends Exception {
    
    /**
     * Error codes for common cache errors
     */
    public enum ErrorCode {
        CONNECTION_ERROR,
        SERIALIZATION_ERROR,
        TIMEOUT_ERROR,
        KEY_NOT_FOUND,
        TYPE_MISMATCH,
        CAPACITY_EXCEEDED,
        AUTHENTICATION_ERROR,
        UNKNOWN_ERROR
    }
    
    private final ErrorCode errorCode;
    
    /**
     * Constructs a new CacheException with the specified error code.
     *
     * @param errorCode The error code
     */
    public CacheException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new CacheException with the specified error code and detail message.
     *
     * @param errorCode The error code
     * @param message   The detail message
     */
    public CacheException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new CacheException with the specified error code, detail message, and cause.
     *
     * @param errorCode The error code
     * @param message   The detail message
     * @param cause     The cause
     */
    public CacheException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new CacheException with the specified error code and cause.
     *
     * @param errorCode The error code
     * @param cause     The cause
     */
    public CacheException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the error code associated with this exception.
     *
     * @return The error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

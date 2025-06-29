package com.exalt.shared.ecommerce.admin.export;

/**
 * Exception thrown when an error occurs during export operations.
 */
public class ExportException extends RuntimeException {

    /**
     * Constructs a new export exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ExportException(String message) {
        super(message);
    }

    /**
     * Constructs a new export exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new export exception with the specified cause.
     *
     * @param cause the cause
     */
    public ExportException(Throwable cause) {
        super(cause);
    }
}

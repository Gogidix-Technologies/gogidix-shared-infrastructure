package com.exalt.ecosystem.shared.filestorage.exception;

/**
 * Exception thrown when file validation fails
 */
public class InvalidFileException extends FileStorageException {
    
    public InvalidFileException(String message) {
        super("Invalid file: " + message);
    }
    
    public InvalidFileException(String message, Throwable cause) {
        super("Invalid file: " + message, cause);
    }
}

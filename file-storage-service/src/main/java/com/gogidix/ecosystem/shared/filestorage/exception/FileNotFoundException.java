package com.gogidix.ecosystem.shared.filestorage.exception;

/**
 * Exception thrown when a file is not found
 */
public class FileNotFoundException extends FileStorageException {
    
    public FileNotFoundException(String fileId) {
        super("File not found with ID: " + fileId);
    }
    
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

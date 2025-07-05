package com.gogidix.shared.ecommerce.admin.export.exception;

/**
 * Exception thrown when there is an error with export templates.
 */
public class TemplateException extends RuntimeException {

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}

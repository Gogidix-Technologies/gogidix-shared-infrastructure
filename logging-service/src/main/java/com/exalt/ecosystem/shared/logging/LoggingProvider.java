package com.exalt.ecosystem.shared.logging;

/**
 * Interface for logging providers.
 * Allows for pluggable logging implementations.
 */
public interface LoggingProvider {
    /**
     * Get a logger for the specified class.
     * @param clazz The class to log for
     * @return A logger instance
     */
    Logger getLogger(Class<?> clazz);
    
    /**
     * Get a logger with the specified name.
     * @param name The logger name
     * @return A logger instance
     */
    Logger getLogger(String name);
}

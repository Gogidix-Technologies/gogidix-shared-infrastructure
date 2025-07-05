package com.gogidix.ecosystem.shared.logging;

/**
 * Factory for creating Logger instances.
 * Abstracts away the underlying logging implementation.
 */
public class LoggerFactory {
    
    private static LoggingProvider provider = new DefaultLoggingProvider();
    
    /**
     * Get a logger for the specified class.
     * @param clazz The class to log for
     * @return A logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return provider.getLogger(clazz);
    }
    
    /**
     * Get a logger with the specified name.
     * @param name The logger name
     * @return A logger instance
     */
    public static Logger getLogger(String name) {
        return provider.getLogger(name);
    }
    
    /**
     * Set the logging provider implementation.
     * @param newProvider The provider to use
     */
    public static void setProvider(LoggingProvider newProvider) {
        provider = newProvider;
    }
}

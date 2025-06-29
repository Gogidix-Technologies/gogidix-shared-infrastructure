package com.exalt.ecosystem.shared.logging;

/**
 * Core logging interface for the microservices ecosystem.
 * Provides standardized logging methods across all services.
 */
public interface Logger {
    void debug(String message);
    void debug(String message, Object... args);
    void debug(String message, Throwable throwable);
    
    void info(String message);
    void info(String message, Object... args);
    void info(String message, Throwable throwable);
    
    void warn(String message);
    void warn(String message, Object... args);
    void warn(String message, Throwable throwable);
    
    void error(String message);
    void error(String message, Object... args);
    void error(String message, Throwable throwable);
    
    boolean isDebugEnabled();
    boolean isInfoEnabled();
    boolean isWarnEnabled();
    boolean isErrorEnabled();
}

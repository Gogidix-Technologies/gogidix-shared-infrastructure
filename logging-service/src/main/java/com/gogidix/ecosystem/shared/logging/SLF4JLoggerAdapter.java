package com.gogidix.ecosystem.shared.logging;

/**
 * Adapter that converts SLF4J Logger to our custom Logger interface.
 */
public class SLF4JLoggerAdapter implements Logger {
    
    private final org.slf4j.Logger logger;
    
    public SLF4JLoggerAdapter(org.slf4j.Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public void debug(String message) {
        logger.debug(message);
    }
    
    @Override
    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }
    
    @Override
    public void debug(String message, Throwable throwable) {
        logger.debug(message, throwable);
    }
    
    @Override
    public void info(String message) {
        logger.info(message);
    }
    
    @Override
    public void info(String message, Object... args) {
        logger.info(message, args);
    }
    
    @Override
    public void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }
    
    @Override
    public void warn(String message) {
        logger.warn(message);
    }
    
    @Override
    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }
    
    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }
    
    @Override
    public void error(String message) {
        logger.error(message);
    }
    
    @Override
    public void error(String message, Object... args) {
        logger.error(message, args);
    }
    
    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
    
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
    
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }
    
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }
    
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }
}

package com.exalt.shared.ecommerce.admin.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger utility for logging messages.
 * This class provides logging functionality for the admin framework.
 */
public class Logger {
    
    /**
     * Log level enum
     */
    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }
    
    private final Class<?> loggerClass;
    private Level minimumLevel;
    
    /**
     * Constructor with logger class
     * 
     * @param loggerClass The class to log for
     */
    public Logger(Class<?> loggerClass) {
        this.loggerClass = loggerClass;
        this.minimumLevel = Level.INFO;
    }
    
    /**
     * Get a logger for a class
     * 
     * @param clazz The class to get a logger for
     * @return The logger for the class
     */
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }
    
    /**
     * Set the minimum log level
     * 
     * @param level The minimum log level
     */
    public void setMinimumLevel(Level level) {
        this.minimumLevel = level;
    }
    
    /**
     * Log a debug message
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    /**
     * Log an info message
     * 
     * @param message The message to log
     */
    public void info(String message) {
        log(Level.INFO, message);
    }
    
    /**
     * Log a warning message
     * 
     * @param message The message to log
     */
    public void warn(String message) {
        log(Level.WARN, message);
    }
    
    /**
     * Log an error message
     * 
     * @param message The message to log
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }
    
    /**
     * Log an error message with an exception
     * 
     * @param message The message to log
     * @param e The exception to log
     */
    public void error(String message, Exception e) {
        log(Level.ERROR, message + ": " + e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * Log a message with a level
     * 
     * @param level The log level
     * @param message The message to log
     */
    private void log(Level level, String message) {
        if (level.ordinal() < minimumLevel.ordinal()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        System.out.println("[" + timestamp + "] [" + level + "] [" + loggerClass.getSimpleName() + "] " + message);
    }
}

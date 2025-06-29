package com.exalt.ecosystem.shared.logging;

import java.util.Map;

/**
 * Interface for log aggregation functionality.
 * Provides methods to collect and forward logs to a centralized system.
 */
public interface LogAggregator {
    /**
     * Send a log event to the aggregation system.
     * 
     * @param level The log level
     * @param message The log message
     * @param metadata Additional metadata for the log event
     */
    void aggregate(String level, String message, Map<String, Object> metadata);
    
    /**
     * Initialize the aggregator with the given configuration.
     * 
     * @param config The configuration properties
     */
    void initialize(Map<String, String> config);
    
    /**
     * Shut down the aggregator, releasing any resources.
     */
    void shutdown();
}

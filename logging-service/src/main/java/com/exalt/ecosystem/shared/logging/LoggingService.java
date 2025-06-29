package com.exalt.ecosystem.shared.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point for the Logging Service.
 * Manages logging configuration and aggregation.
 */
public class LoggingService {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingService.class);
    private static final Map<String, LogAggregator> aggregators = new ConcurrentHashMap<>();
    private static final Map<String, String> config = new ConcurrentHashMap<>();
    
    private static boolean initialized = false;
    
    /**
     * Initialize the logging service with the given configuration.
     * 
     * @param configuration Configuration properties
     */
    public static synchronized void initialize(Map<String, String> configuration) {
        if (initialized) {
            log.warn("LoggingService already initialized");
            return;
        }
        
        config.putAll(configuration);
        
        // Set up default log aggregator if enabled
        if (Boolean.parseBoolean(config.getOrDefault("logging.aggregation.enabled", "true"))) {
            String aggregatorType = config.getOrDefault("logging.aggregator.type", "elasticsearch");
            
            LogAggregator aggregator;
            if ("elasticsearch".equalsIgnoreCase(aggregatorType)) {
                aggregator = new ElasticSearchAggregator();
            } else {
                log.warn("Unknown aggregator type: {}. Using ElasticSearchAggregator as default.", aggregatorType);
                aggregator = new ElasticSearchAggregator();
            }
            
            aggregator.initialize(config);
            aggregators.put(aggregatorType, aggregator);
        }
        
        initialized = true;
        log.info("LoggingService initialized successfully");
    }
    
    /**
     * Submit a log event to all configured aggregators.
     * 
     * @param level The log level
     * @param message The log message
     * @param metadata Additional metadata for the log event
     */
    public static void submitLogEvent(String level, String message, Map<String, Object> metadata) {
        if (!initialized) {
            System.err.println("LoggingService not initialized");
            return;
        }
        
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        
        // Add standard metadata
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("service", config.getOrDefault("service.name", "unknown"));
        
        for (LogAggregator aggregator : aggregators.values()) {
            try {
                aggregator.aggregate(level, message, metadata);
            } catch (Exception e) {
                log.error("Failed to aggregate log", e);
            }
        }
    }
    
    /**
     * Shut down the logging service, releasing any resources.
     */
    public static synchronized void shutdown() {
        if (!initialized) {
            return;
        }
        
        for (LogAggregator aggregator : aggregators.values()) {
            try {
                aggregator.shutdown();
            } catch (Exception e) {
                log.error("Failed to shut down aggregator", e);
            }
        }
        
        aggregators.clear();
        initialized = false;
        log.info("LoggingService shut down successfully");
    }
}

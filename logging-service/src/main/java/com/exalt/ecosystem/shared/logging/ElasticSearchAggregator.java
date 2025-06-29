package com.exalt.ecosystem.shared.logging;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of LogAggregator that sends logs to Elasticsearch.
 */
public class ElasticSearchAggregator implements LogAggregator {
    
    private String elasticUrl;
    private String indexName;
    private ExecutorService executor;
    private boolean initialized = false;
    
    @Override
    public void initialize(Map<String, String> config) {
        elasticUrl = config.getOrDefault("elastic.url", "http://localhost:9200");
        indexName = config.getOrDefault("elastic.index", "microecommerce-logs");
        int threadPoolSize = Integer.parseInt(config.getOrDefault("elastic.threadpool.size", "2"));
        
        executor = Executors.newFixedThreadPool(threadPoolSize);
        initialized = true;
    }
    
    @Override
    public void aggregate(String level, String message, Map<String, Object> metadata) {
        if (!initialized) {
            throw new IllegalStateException("ElasticSearchAggregator not initialized");
        }
        
        executor.submit(() -> {
            try {
                // In a real implementation, this would use the Elasticsearch Java client
                // to send the log data to Elasticsearch
                System.out.println("Sending log to Elasticsearch: " + level + " - " + message);
            } catch (Exception e) {
                System.err.println("Failed to send log to Elasticsearch: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}

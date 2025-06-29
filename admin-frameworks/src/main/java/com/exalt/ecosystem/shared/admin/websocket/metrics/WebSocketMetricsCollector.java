package com.exalt.ecosystem.shared.admin.websocket.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Collects metrics for WebSocket connections and message handling.
 */
@Component
public class WebSocketMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicInteger> sessionGauges = new ConcurrentHashMap<>();
    private Counter connectionCounter;
    private Counter messageCounter;
    private Counter errorCounter;
    private Timer messageProcessingTimer;
    
    public WebSocketMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeCounters();
    }
    
    private void initializeCounters() {
        // Connection metrics
        connectionCounter = Counter.builder("websocket.connections.total")
                .description("Total WebSocket connections")
                .register(meterRegistry);
        
        // Message metrics
        messageCounter = Counter.builder("websocket.messages.total")
                .description("Total WebSocket messages processed")
                .register(meterRegistry);
        
        // Error metrics
        errorCounter = Counter.builder("websocket.errors.total")
                .description("Total WebSocket errors")
                .tag("type", "general")
                .register(meterRegistry);
        
        // Processing time metrics
        messageProcessingTimer = Timer.builder("websocket.message.processing.time")
                .description("Time taken to process WebSocket messages")
                .publishPercentiles(0.5, 0.95, 0.99) // median and 95th percentile
                .register(meterRegistry);
    }
    
    public void incrementConnections(String sessionId) {
        connectionCounter.increment();
        sessionGauges.computeIfAbsent(sessionId, 
            id -> {
                AtomicInteger gauge = new AtomicInteger(0);
                meterRegistry.gauge("websocket.active.connections", 
                    gauge, AtomicInteger::doubleValue);
                return gauge;
            }).incrementAndGet();
    }
    
    public void decrementConnections(String sessionId) {
        sessionGauges.computeIfPresent(sessionId, (id, gauge) -> {
            gauge.decrementAndGet();
            return gauge.get() <= 0 ? null : gauge;
        });
    }
    
    public void recordMessageProcessed(String messageType, long durationNanos) {
        messageCounter.increment();
        messageProcessingTimer.record(durationNanos, TimeUnit.NANOSECONDS);
        
        // Record message type distribution
        Counter.builder("websocket.messages.by.type")
                .tag("type", messageType)
                .register(meterRegistry)
                .increment();
    }
    
    public void recordError(String errorType) {
        errorCounter.increment();
        Counter.builder("websocket.errors.by.type")
                .tag("type", errorType)
                .register(meterRegistry)
                .increment();
    }
    
    public int getActiveConnectionCount() {
        return sessionGauges.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }
}

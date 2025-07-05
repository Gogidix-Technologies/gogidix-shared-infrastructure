package com.gogidix.shared.ecommerce.admin.websocket.ratelimit.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.stereotype.Service;

/**
 * Service for tracking WebSocket rate limiting metrics.
 */
@Service
public class RateLimitMetrics {
    private static final String RATE_LIMIT_METRIC = "websocket.rate_limit";
    private static final String TYPE_TAG = "type";
    private static final String RESULT_TAG = "result";
    
    private final Counter rateLimitExceededCounter;
    private final Counter connectionLimitExceededCounter;
    private final Counter messagesAllowedCounter;
    private final Counter messagesBlockedCounter;

    public RateLimitMetrics(MeterRegistry registry) {
        this.rateLimitExceededCounter = Counter.builder(RATE_LIMIT_METRIC)
                .description("Number of rate limit exceeded events")
                .tags(TYPE_TAG, "message", RESULT_TAG, "exceeded")
                .register(registry);
                
        this.connectionLimitExceededCounter = Counter.builder(RATE_LIMIT_METRIC)
                .description("Number of connection limit exceeded events")
                .tags(TYPE_TAG, "connection", RESULT_TAG, "exceeded")
                .register(registry);
                
        this.messagesAllowedCounter = Counter.builder(RATE_LIMIT_METRIC)
                .description("Number of messages allowed")
                .tags(TYPE_TAG, "message", RESULT_TAG, "allowed")
                .register(registry);
                
        this.messagesBlockedCounter = Counter.builder(RATE_LIMIT_METRIC)
                .description("Number of messages blocked by rate limiting")
                .tags(TYPE_TAG, "message", RESULT_TAG, "blocked")
                .register(registry);
    }

    public void incrementRateLimitExceeded() {
        rateLimitExceededCounter.increment();
    }

    public void incrementConnectionLimitExceeded() {
        connectionLimitExceededCounter.increment();
    }

    public void incrementMessagesAllowed() {
        messagesAllowedCounter.increment();
    }

    public void incrementMessagesBlocked() {
        messagesBlockedCounter.increment();
    }
}

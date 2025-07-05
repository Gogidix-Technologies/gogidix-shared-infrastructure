package com.gogidix.shared.ecommerce.admin.websocket.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for WebSocket metrics testing.
 */
@Configuration
public class WebSocketMetricsTestConfig {
    
    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
    
    @Bean
    @Primary
    public WebSocketMetricsCollector webSocketMetricsCollector(MeterRegistry meterRegistry) {
        return new WebSocketMetricsCollector(meterRegistry);
    }
    
    @Bean
    @Primary
    public WebSocketMetricsInterceptor webSocketMetricsInterceptor(WebSocketMetricsCollector metricsCollector) {
        return new WebSocketMetricsInterceptor(metricsCollector);
    }
    
    @Bean
    @Primary
    public WebSocketHealthIndicator webSocketHealthIndicator(WebSocketMetricsCollector metricsCollector) {
        return new WebSocketHealthIndicator(metricsCollector);
    }
}

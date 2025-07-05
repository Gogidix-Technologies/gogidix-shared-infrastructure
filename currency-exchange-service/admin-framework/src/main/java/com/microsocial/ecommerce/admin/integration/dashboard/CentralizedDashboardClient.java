package com.gogidix.shared.ecommerce.admin.integration.dashboard;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsocial.ecommerce.admin.integration.RestClient;
import com.microsocial.ecommerce.admin.util.Logger;

/**
 * Client for interacting with the Centralized Dashboard service.
 * Handles all communication with the dashboard microservice.
 */
@Component
public class CentralizedDashboardClient extends RestClient {
    
    private static final Logger logger = Logger.getLogger(CentralizedDashboardClient.class);
    
    @Value("${dashboard.service.url:http://localhost:8081}")
    private String dashboardBaseUrl;
    
    private final ObjectMapper objectMapper;
    
    public CentralizedDashboardClient(ObjectMapper objectMapper) {
        super(15); // 15 seconds timeout for dashboard operations
        this.objectMapper = objectMapper;
    }
    
    /**
     * Sends metrics data to the centralized dashboard
     * 
     * @param metricsData The metrics data to send (will be converted to JSON)
     * @return The response body as a string
     * @throws Exception if the request fails
     */
    public String sendMetrics(Object metricsData) throws Exception {
        String url = dashboardBaseUrl + "/api/v1/metrics";
        String jsonPayload = objectMapper.writeValueAsString(metricsData);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
                
        return executeRequest(request);
    }
    
    /**
     * Fetches dashboard configuration for a specific user
     * 
     * @param userId The ID of the user
     * @return The dashboard configuration as a string (JSON)
     * @throws Exception if the request fails
     */
    public String getDashboardConfig(String userId) throws Exception {
        String url = String.format("%s/api/v1/dashboard/config?userId=%s", dashboardBaseUrl, userId);
        return get(url, Map.of("Accept", "application/json"));
    }
    
    /**
     * Updates dashboard configuration for a specific user
     * 
     * @param userId The ID of the user
     * @param config The new configuration (will be converted to JSON)
     * @return The response body as a string
     * @throws Exception if the request fails
     */
    public String updateDashboardConfig(String userId, Object config) throws Exception {
        String url = dashboardBaseUrl + "/api/v1/dashboard/config/" + userId;
        String jsonPayload = objectMapper.writeValueAsString(config);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
                
        return executeRequest(request);
    }
    
    /**
     * Subscribes to dashboard events
     * 
     * @param callbackUrl The URL to receive event callbacks
     * @return The subscription ID
     * @throws Exception if the request fails
     */
    public String subscribeToEvents(String callbackUrl) throws Exception {
        String url = dashboardBaseUrl + "/api/v1/events/subscribe";
        String jsonPayload = String.format("{\"callbackUrl\":\"%s\"}", callbackUrl);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
                
        return executeRequest(request);
    }
    
    /**
     * Unsubscribes from dashboard events
     * 
     * @param subscriptionId The subscription ID to unsubscribe
     * @return true if unsubscribed successfully, false otherwise
     */
    public boolean unsubscribeFromEvents(String subscriptionId) {
        try {
            String url = dashboardBaseUrl + "/api/v1/events/unsubscribe/" + subscriptionId;
            delete(url, Map.of("Accept", "application/json"));
            return true;
        } catch (Exception e) {
            logger.error("Failed to unsubscribe from events: " + e.getMessage(), e);
            return false;
        }
    }
    
    private String executeRequest(HttpRequest request) throws Exception {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(e -> {
                    logger.error("Dashboard request failed: " + e.getMessage(), e);
                    return null;
                })
                .get();
    }
}

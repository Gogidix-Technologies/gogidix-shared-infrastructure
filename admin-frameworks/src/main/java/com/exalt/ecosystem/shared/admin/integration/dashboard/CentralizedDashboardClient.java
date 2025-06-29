package com.exalt.ecosystem.shared.admin.integration.dashboard;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Client for interacting with the Centralized Dashboard service.
 * Handles all communication with the dashboard microservice.
 */
@Component
public class CentralizedDashboardClient {
    
    private static final Logger logger = LoggerFactory.getLogger(CentralizedDashboardClient.class);
    
    @Value("${dashboard.service.url:http://localhost:8081}")
    private String dashboardBaseUrl;
    
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    public CentralizedDashboardClient(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
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
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(metricsData, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to send metrics to dashboard: " + e.getMessage(), e);
            throw e;
        }
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
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to get dashboard config for user " + userId + ": " + e.getMessage(), e);
            throw e;
        }
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
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(config, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to update dashboard config for user " + userId + ": " + e.getMessage(), e);
            throw e;
        }
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
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> payload = Map.of("callbackUrl", callbackUrl);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to subscribe to dashboard events: " + e.getMessage(), e);
            throw e;
        }
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
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            return true;
        } catch (Exception e) {
            logger.error("Failed to unsubscribe from events: " + e.getMessage(), e);
            return false;
        }
    }
}

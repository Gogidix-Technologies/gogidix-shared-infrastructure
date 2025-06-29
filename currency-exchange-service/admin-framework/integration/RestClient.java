package com.exalt.shared.ecommerce.admin.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsocial.ecommerce.admin.util.Logger;

/**
 * REST client for making HTTP requests.
 * This class provides functionality for making HTTP requests to external services.
 */
public class RestClient {
    
    private static final Logger logger = Logger.getLogger(RestClient.class);
    
    private final HttpClient httpClient;
    
    /**
     * Default constructor
     */
    public RestClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    /**
     * Constructor with timeout
     * 
     * @param timeoutSeconds The timeout in seconds
     */
    public RestClient(int timeoutSeconds) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }
    
    /**
     * Make a GET request
     * 
     * @param url The URL to request
     * @return The response body as a string
     * @throws Exception if an error occurs
     */
    public String get(String url) throws Exception {
        return get(url, Map.of());
    }
    
    /**
     * Make a GET request with headers
     * 
     * @param url The URL to request
     * @param headers The headers to include
     * @return The response body as a string
     * @throws Exception if an error occurs
     */
    public String get(String url, Map<String, String> headers) throws Exception {
        logger.debug("Making GET request to: " + url);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url));
        
        // Add headers
        headers.forEach(requestBuilder::header);
        
        HttpRequest request = requestBuilder.build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.debug("Response status code: " + response.statusCode());
        
        if (response.statusCode() >= 400) {
            throw new Exception("HTTP error: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }
    
    /**
     * Make a POST request
     * 
     * @param url The URL to request
     * @param body The request body
     * @return The response body as a string
     * @throws Exception if an error occurs
     */
    public String post(String url, String body) throws Exception {
        return post(url, body, Map.of());
    }
    
    /**
     * Make a POST request with headers
     * 
     * @param url The URL to request
     * @param body The request body
     * @param headers The headers to include
     * @return The response body as a string
     * @throws Exception if an error occurs
     */
    public String post(String url, String body, Map<String, String> headers) throws Exception {
        logger.debug("Making POST request to: " + url);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(url));
        
        // Add headers
        headers.forEach(requestBuilder::header);
        
        HttpRequest request = requestBuilder.build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.debug("Response status code: " + response.statusCode());
        
        if (response.statusCode() >= 400) {
            throw new Exception("HTTP error: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }
    
    /**
     * Make an asynchronous GET request
     * 
     * @param url The URL to request
     * @return A CompletableFuture for the response body as a string
     */
    public CompletableFuture<String> getAsync(String url) {
        return getAsync(url, Map.of());
    }
    
    /**
     * Make an asynchronous GET request with headers
     * 
     * @param url The URL to request
     * @param headers The headers to include
     * @return A CompletableFuture for the response body as a string
     */
    public CompletableFuture<String> getAsync(String url, Map<String, String> headers) {
        logger.debug("Making async GET request to: " + url);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url));
        
        // Add headers
        headers.forEach(requestBuilder::header);
        
        HttpRequest request = requestBuilder.build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    logger.debug("Response status code: " + response.statusCode());
                    
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("HTTP error: " + response.statusCode() + " - " + response.body());
                    }
                    
                    return response.body();
                });
    }
    
    /**
     * Make an asynchronous POST request
     * 
     * @param url The URL to request
     * @param body The request body
     * @return A CompletableFuture for the response body as a string
     */
    public CompletableFuture<String> postAsync(String url, String body) {
        return postAsync(url, body, Map.of());
    }
    
    /**
     * Make an asynchronous POST request with headers
     * 
     * @param url The URL to request
     * @param body The request body
     * @param headers The headers to include
     * @return A CompletableFuture for the response body as a string
     */
    public CompletableFuture<String> postAsync(String url, String body, Map<String, String> headers) {
        logger.debug("Making async POST request to: " + url);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(url));
        
        // Add headers
        headers.forEach(requestBuilder::header);
        
        HttpRequest request = requestBuilder.build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    logger.debug("Response status code: " + response.statusCode());
                    
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("HTTP error: " + response.statusCode() + " - " + response.body());
                    }
                    
                    return response.body();
                });
    }
}

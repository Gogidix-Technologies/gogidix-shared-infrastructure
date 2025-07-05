package com.gogidix.shared.apigateway.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.UUID;

/**
 * Utility class for API Gateway integration tests.
 * Provides helper methods for common testing operations.
 */
public final class TestUtils {

    private TestUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a mock JWT token for testing.
     * 
     * @param subject the subject (user ID)
     * @param roles the user roles
     * @return a mock JWT token
     */
    public static String generateMockJwtToken(String subject, String... roles) {
        // In a real implementation, this would generate a proper JWT token
        // For testing purposes, we just return a fixed token string
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
               "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
               "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    /**
     * Creates a WebTestClient.RequestHeadersSpec with common headers for testing.
     * 
     * @param webTestClient the WebTestClient to use
     * @param method the HTTP method
     * @param uri the URI to request
     * @param authenticated whether to include authentication headers
     * @return a WebTestClient.RequestHeadersSpec with common headers
     */
    public static WebTestClient.RequestHeadersSpec<?> createRequest(
            WebTestClient webTestClient, 
            HttpMethod method, 
            String uri, 
            boolean authenticated) {
        
        WebTestClient.RequestHeadersSpec<?> request;
        
        switch (method) {
            case GET:
                request = webTestClient.get().uri(uri);
                break;
            case POST:
                request = webTestClient.post().uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{}");
                break;
            case PUT:
                request = webTestClient.put().uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{}");
                break;
            case DELETE:
                request = webTestClient.delete().uri(uri);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        
        // Add common headers
        request = request.header("X-Request-ID", UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON);
        
        // Add authentication if needed
        if (authenticated) {
            request = request.header(HttpHeaders.AUTHORIZATION, 
                    "Bearer " + generateMockJwtToken("test-user", "USER"));
        }
        
        return request;
    }

    /**
     * HTTP method enum for test requests.
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    /**
     * Validates common response headers.
     * 
     * @param headers the response headers
     * @return true if all required headers are present, false otherwise
     */
    public static boolean validateResponseHeaders(HttpHeaders headers) {
        return headers.containsKey("X-Request-ID") &&
                headers.containsKey("X-Response-Time") &&
                headers.containsKey("X-Content-Type-Options");
    }

    /**
     * Creates a test request body.
     * 
     * @param fields map of field names to values
     * @return JSON string representing the request body
     */
    public static String createRequestBody(Map<String, Object> fields) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!first) {
                json.append(",");
            }
            
            json.append("\"").append(entry.getKey()).append("\":");
            
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else if (entry.getValue() instanceof Number) {
                json.append(entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                json.append(entry.getValue());
            } else if (entry.getValue() == null) {
                json.append("null");
            } else {
                json.append("\"").append(entry.getValue().toString()).append("\"");
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
}

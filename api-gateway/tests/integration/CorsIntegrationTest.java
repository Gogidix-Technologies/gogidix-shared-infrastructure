import java.util.Map;
package com.exalt.shared.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for API Gateway CORS configuration.
 * Tests that CORS headers are properly set for cross-origin requests.
 */
@TestPropertySource(properties = {
    "spring.cloud.gateway.globalcors.add-to-simple-url-handler-mapping=true",
    "spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowed-origins=*",
    "spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowed-methods=GET,POST,PUT,DELETE,OPTIONS",
    "spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowed-headers=*",
    "spring.cloud.gateway.globalcors.corsConfigurations.[/**].max-age=3600"
})
public class CorsIntegrationTest extends BaseIntegrationTest {

    @Test
    void returnsCorsHeadersForPreflightRequests() {
        webTestClient.options()
                .uri("/users/1")
                .header(HttpHeaders.ORIGIN, "http://example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, 
                        "GET,POST,PUT,DELETE,OPTIONS")
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    }
    
    @Test
    void includesCorsHeadersInActualResponses() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(HttpHeaders.ORIGIN, "http://example.com")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    }
    
    @Test
    void allowsRequestsFromVariousOrigins() {
        String[] origins = {
            "http://localhost:3000",
            "https://app.example.com",
            "https://admin.example.com"
        };
        
        for (String origin : origins) {
            webTestClient.get()
                    .uri("/actuator/health")
                    .header(HttpHeaders.ORIGIN, origin)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        }
    }
    
    @Test
    void supportsVariousHttpMethods() {
        HttpMethod[] methods = {
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.DELETE
        };
        
        for (HttpMethod method : methods) {
            webTestClient.options()
                    .uri("/users/1")
                    .header(HttpHeaders.ORIGIN, "http://example.com")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method.name())
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
        }
    }
}


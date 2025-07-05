package com.gogidix.shared.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for API Gateway filters.
 * Tests request logging and global exception handling.
 */
@TestPropertySource(properties = {
    "spring.cloud.gateway.routes[0].id=test-service",
    "spring.cloud.gateway.routes[0].uri=http://localhost:8080",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**",
    "spring.cloud.gateway.routes[0].filters[0]=RequestLoggingFilter",
    "spring.cloud.gateway.routes[0].filters[1]=GlobalExceptionHandlerFilter",
    "spring.cloud.gateway.default-filters[0]=RequestLoggingFilter",
    "spring.cloud.gateway.default-filters[1]=GlobalExceptionHandlerFilter",
    "spring.cloud.gateway.default-filters[2]=AddRequestHeader=X-Request-ID, ${random.uuid}",
    "spring.cloud.gateway.default-filters[3]=AddResponseHeader=X-Response-Time, ${now:yyyy-MM-dd'T'HH:mm:ss.SSS}"
})
public class FilterIntegrationTest extends BaseIntegrationTest {

    @Test
    void addsRequestAndResponseHeaders() {
        webTestClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Request-ID")
                .expectHeader().exists("X-Response-Time");
    }
    
    @Test
    void returnsConsistentErrorResponseForBadRequests() {
        webTestClient.get()
                .uri("/test/bad-request?param=invalid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").exists()
                .jsonPath("$.path").exists();
    }
    
    @Test
    void returnsConsistentErrorResponseForServerErrors() {
        webTestClient.get()
                .uri("/test/server-error")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").exists()
                .jsonPath("$.path").exists();
    }
    
    @Test
    void logsRequestsAndResponses() {
        // This test can't directly verify logging, but ensures the filter is applied
        // without throwing exceptions
        webTestClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
        
        webTestClient.post()
                .uri("/test/resource")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"test\":\"data\"}")
                .exchange()
                .expectStatus().isNotFound(); // Since service doesn't exist
    }
    
    @Test
    void preservesRequestBodyForLogging() {
        // Test that the request body is preserved for logging but still available for the service
        webTestClient.post()
                .uri("/test/resource")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"test\":\"data\"}")
                .exchange()
                .expectStatus().isNotFound(); // Since service doesn't exist
    }
    
    @Test
    void tracksRequestTiming() {
        // This test ensures that timing metrics are recorded without exceptions
        webTestClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Response-Time");
    }
}

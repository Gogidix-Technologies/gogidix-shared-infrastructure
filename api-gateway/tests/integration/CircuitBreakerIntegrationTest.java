package com.gogidix.shared.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for API Gateway circuit breaker functionality.
 * Tests that circuit breakers properly handle service failures and provide fallback responses.
 */
@TestPropertySource(properties = {
    "spring.cloud.gateway.routes[0].id=circuit-breaker-service",
    "spring.cloud.gateway.routes[0].uri=http://localhost:8099", // Non-existent service to trigger circuit breaker
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/circuit-test/**",
    "spring.cloud.gateway.routes[0].filters[0]=name=CircuitBreaker,args.name=testCircuitBreaker,args.fallbackUri=forward:/fallback",
    "resilience4j.circuitbreaker.configs.default.slidingWindowSize=10",
    "resilience4j.circuitbreaker.configs.default.failureRateThreshold=50",
    "resilience4j.circuitbreaker.configs.default.waitDurationInOpenState=1000",
    "resilience4j.circuitbreaker.instances.testCircuitBreaker.baseConfig=default"
})
public class CircuitBreakerIntegrationTest extends BaseIntegrationTest {

    @Test
    void activatesCircuitBreakerWhenServiceIsDown() {
        // Send request to a non-existent service through a route with circuit breaker
        // This should trigger the fallback response
        webTestClient.get()
                .uri("/circuit-test/resource")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE) // Assuming fallback returns 503
                .expectBody()
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }

    @Test
    void returnsServiceUnavailableAfterCircuitBreaks() {
        // Send multiple requests to trigger the circuit breaker to open
        for (int i = 0; i < 5; i++) {
            webTestClient.get()
                    .uri("/circuit-test/resource")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }
        
        // After multiple failures, circuit should be open
        // Additional requests should fail fast without trying to call the service
        webTestClient.get()
                .uri("/circuit-test/resource")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }

    @Test
    void doesNotAffectHealthyServicesWhenOneServiceIsDown() {
        // First, verify that the circuit breaker route returns an error
        webTestClient.get()
                .uri("/circuit-test/resource")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        
        // Then, verify that the health endpoint still works
        webTestClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }
}

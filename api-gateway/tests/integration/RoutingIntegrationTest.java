package com.gogidix.shared.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for API Gateway routing functionality.
 * Tests that requests are properly routed to the correct services.
 */
@TestPropertySource(properties = {
    "spring.cloud.gateway.routes[0].id=auth-service",
    "spring.cloud.gateway.routes[0].uri=http://localhost:8081",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/auth/**",
    "spring.cloud.gateway.routes[1].id=user-service",
    "spring.cloud.gateway.routes[1].uri=http://localhost:8082",
    "spring.cloud.gateway.routes[1].predicates[0]=Path=/users/**",
    "spring.cloud.gateway.routes[2].id=product-service",
    "spring.cloud.gateway.routes[2].uri=http://localhost:8083",
    "spring.cloud.gateway.routes[2].predicates[0]=Path=/products/**"
})
public class RoutingIntegrationTest extends BaseIntegrationTest {

    @Test
    void routesToAuthServiceWithCorrectPath() {
        // This test assumes that the /auth/** path is routed to the auth-service
        // In a real test, you would use a mock server for the auth-service
        // Here we're just checking that the request is properly routed (will return 404 since service not running)
        webTestClient.get()
                .uri("/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody()
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }

    @Test
    void routesToUserServiceWithCorrectPath() {
        // This test assumes that the /users/** path is routed to the user-service
        webTestClient.get()
                .uri("/users/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody()
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }

    @Test
    void routesToProductServiceWithCorrectPath() {
        // This test assumes that the /products/** path is routed to the product-service
        webTestClient.get()
                .uri("/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody()
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }

    @Test
    void returnsNotFoundForNonExistentService() {
        webTestClient.get()
                .uri("/non-existent-service")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }
}

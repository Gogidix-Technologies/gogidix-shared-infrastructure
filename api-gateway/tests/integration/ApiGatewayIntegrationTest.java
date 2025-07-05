package com.gogidix.shared.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic integration tests for the API Gateway application.
 * Ensures that the application context loads successfully and basic endpoints are accessible.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApiGatewayIntegrationTest extends BaseIntegrationTest {

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }

    @Test
    void actuatorHealthEndpointShouldBeAccessible() {
        webTestClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }
    
    @Test
    void actuatorInfoEndpointShouldBeAccessible() {
        webTestClient.get()
                .uri("/actuator/info")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.app").exists();
    }
    
    @Test
    void actuatorMetricsEndpointShouldBeAccessible() {
        webTestClient.get()
                .uri("/actuator/metrics")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.names").isArray();
    }
    
    @Test
    void nonExistentEndpointShouldReturn404() {
        webTestClient.get()
                .uri("/non-existent-endpoint")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    void httpMethodNotAllowedShouldReturn405() {
        webTestClient.post()
                .uri("/actuator/health")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().is4xxClientError();
    }
}

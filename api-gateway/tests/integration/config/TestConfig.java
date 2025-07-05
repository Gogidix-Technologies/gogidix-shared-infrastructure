package com.gogidix.shared.apigateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test configuration for API Gateway integration tests.
 * Provides necessary beans for testing the gateway functionality.
 */
@TestConfiguration
public class TestConfig {

    /**
     * Creates a WebTestClient with default configuration.
     * This client is used to make HTTP requests to the API Gateway during tests.
     * 
     * @return the configured WebTestClient
     */
    @Bean
    public WebTestClient webTestClient() {
        return WebTestClient.bindToServer()
                .baseUrl("http://localhost:${local.server.port}")
                .build();
    }
}

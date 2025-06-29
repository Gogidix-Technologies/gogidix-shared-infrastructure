package com.exalt.shared.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for API Gateway security functionality.
 * Tests authentication, authorization, and other security features.
 */
@TestPropertySource(properties = {
    "spring.cloud.gateway.routes[0].id=secured-route",
    "spring.cloud.gateway.routes[0].uri=http://localhost:8080",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/secured/**",
    "spring.cloud.gateway.routes[0].filters[0]=name=SecurityFilter",
    "spring.cloud.gateway.routes[1].id=public-route",
    "spring.cloud.gateway.routes[1].uri=http://localhost:8080",
    "spring.cloud.gateway.routes[1].predicates[0]=Path=/api/public/**",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/auth",
    "security.ignored-paths=/actuator/**,/api/public/**,/api/auth/**"
})
public class SecurityIntegrationTest extends BaseIntegrationTest {

    // Mock JWT token for testing
    private static final String MOCK_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    @Test
    void allowsAccessToPublicEndpoints() {
        webTestClient.get()
                .uri("/api/public/resources")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // Since the actual service doesn't exist, but gateway should allow access
    }

    @Test
    void blocksUnauthenticatedAccessToSecuredEndpoints() {
        webTestClient.get()
                .uri("/api/secured/resources")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED) // 401 Unauthorized expected
                .expectBody()
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }

    @Test
    void allowsAuthenticatedAccessToSecuredEndpoints() {
        // In a real test, we would use a valid JWT token
        // For this test, we'll check that the Gateway forwards the request with the Authorization header
        webTestClient.get()
                .uri("/api/secured/resources")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_JWT_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // Since the actual service doesn't exist, but gateway should forward request
    }

    @Test
    void protectsAgainstHeaderInjection() {
        webTestClient.get()
                .uri("/api/public/resources")
                .header("X-Forwarded-Host", "malicious-host.com")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN) // Gateway should block suspicious headers
                .expectBody()
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }

    @Test
    void setsCacheAndSecurityHeaders() {
        webTestClient.get()
                .uri("/api/public/resources")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().exists("X-Content-Type-Options")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().exists("X-XSS-Protection")
                .expectHeader().valueEquals("X-XSS-Protection", "1; mode=block")
                .expectHeader().exists("Cache-Control")
                .expectHeader().valueEquals("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
                .expectHeader().exists("Pragma")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().exists("Expires")
                .expectHeader().valueEquals("Expires", "0")
                .expectHeader().exists("X-Frame-Options")
                .expectHeader().valueEquals("X-Frame-Options", "DENY");
    }

    @Test
    void blocksInvalidContentTypes() {
        webTestClient.post()
                .uri("/api/public/resources")
                .contentType(MediaType.APPLICATION_XML) // Assuming XML is not supported
                .bodyValue("<test>data</test>")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
}

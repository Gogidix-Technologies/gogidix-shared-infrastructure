# API Gateway Integration Testing

This document describes the comprehensive integration testing approach implemented for the API Gateway component of the Social E-commerce Ecosystem.

## Test Coverage

The integration tests cover the following aspects of the API Gateway functionality:

1. **Basic Gateway Functionality**
   - Application context loading
   - Health and information endpoints
   - Basic routing and error handling

2. **Routing**
   - Service discovery and routing to appropriate microservices
   - Handling of non-existent services or endpoints

3. **Rate Limiting**
   - Enforcement of rate limits on configured endpoints
   - Proper handling of requests exceeding the limit
   - Rate limit reset behavior

4. **Circuit Breaking**
   - Detection of failing services
   - Activation of circuit breakers after threshold is exceeded
   - Fallback responses when circuits are open
   - Isolation of failures to maintain overall system health

5. **CORS (Cross-Origin Resource Sharing)**
   - Proper handling of preflight requests
   - Setting appropriate CORS headers in responses
   - Support for multiple origins, methods, and headers

6. **Security**
   - Authentication and authorization checks
   - Handling of secure vs. public endpoints
   - Protection against header injection and other security threats
   - Setting of security-related headers

7. **Request Filtering**
   - Request logging
   - Request ID generation and tracking
   - Global exception handling
   - Response timing and metrics

## Test Structure

The tests are organized as follows:

- `BaseIntegrationTest`: Base class that provides common functionality for all tests
- `TestConfig`: Test-specific configuration
- `ApiGatewayIntegrationTest`: Basic API Gateway functionality tests
- `RoutingIntegrationTest`: Tests for routing capabilities
- `RateLimitingIntegrationTest`: Tests for rate limiting functionality
- `CircuitBreakerIntegrationTest`: Tests for circuit breaker patterns
- `CorsIntegrationTest`: Tests for CORS configuration
- `SecurityIntegrationTest`: Tests for security features
- `FilterIntegrationTest`: Tests for request/response filters

## Test Utilities

The `TestUtils` class provides helper methods for common testing operations:

- Generation of mock JWT tokens
- Creation of test requests with common headers
- Validation of response headers
- Creation of test request bodies

## Test Configuration

The tests use a custom `application-test.yml` configuration file that sets up:

- Random server port for testing
- Mock routes and services
- Security configuration
- Circuit breaker configuration
- Rate limiting configuration
- CORS settings
- Logging configuration

## Running the Tests

To run the integration tests:

```bash
# Run all tests
./gradlew integrationTest

# Run a specific test
./gradlew integrationTest --tests "com.socialecommerceecosystem.apigateway.SecurityIntegrationTest"
```

or with Maven:

```bash
# Run all tests
mvn verify -P integration-test

# Run a specific test
mvn verify -P integration-test -Dtest=SecurityIntegrationTest
```

## Test Environment

The tests run with the following environment:

- Spring Boot test environment with a random port
- Mock services for downstream dependencies
- Test profile activated (`@ActiveProfiles("test")`)
- Eureka client disabled for testing

## Best Practices

The integration tests follow these best practices:

1. **Independent Tests**: Each test is independent and does not rely on the state from previous tests
2. **Clear Assertions**: Each test has clear, specific assertions about expected behavior
3. **Comprehensive Coverage**: Tests cover both happy paths and error scenarios
4. **Performance**: Tests are designed to run quickly to enable fast feedback
5. **Readability**: Test methods have descriptive names that explain what is being tested
6. **Maintainability**: Common functionality is extracted to utility methods and base classes

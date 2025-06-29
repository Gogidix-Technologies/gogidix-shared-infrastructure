# API Gateway Documentation

## Overview

The API Gateway serves as the single entry point for all client requests in the Social E-commerce Ecosystem. It provides centralized routing, authentication, authorization, rate limiting, monitoring, and security for all microservices across the platform.

## Components

### Core Components
- **GatewayRouter**: Central routing component that directs requests to appropriate microservices
- **AuthenticationFilter**: Handles JWT token validation and user authentication
- **AuthorizationFilter**: Manages role-based access control (RBAC) and permission checks
- **RateLimitingFilter**: Implements rate limiting and throttling mechanisms

### Security Components
- **SecurityConfig**: Comprehensive security configuration for the gateway
- **JWTProcessor**: JWT token processing, validation, and generation
- **CORSConfig**: Cross-Origin Resource Sharing configuration
- **APIKeyValidator**: API key validation for external integrations

### Routing Components
- **ServiceDiscovery**: Dynamic service discovery integration with Eureka
- **LoadBalancer**: Intelligent load balancing across service instances
- **CircuitBreaker**: Circuit breaker pattern implementation for fault tolerance
- **HealthChecker**: Health checking for backend services

### Monitoring Components
- **RequestLogger**: Comprehensive request/response logging
- **MetricsCollector**: API metrics collection and reporting
- **PerformanceMonitor**: Performance monitoring and alerting
- **AuditLogger**: Security audit logging

## Getting Started

To use the API Gateway, follow these steps:

1. Configure the gateway with service discovery settings
2. Set up authentication and authorization rules
3. Configure routing rules for your microservices
4. Enable rate limiting and monitoring
5. Set up SSL/TLS certificates for production

## Examples

### Configuring the API Gateway

```java
import com.exalt.gateway.core.GatewayRouter;
import com.exalt.gateway.core.SecurityConfig;
import com.exalt.gateway.filters.AuthenticationFilter;
import com.exalt.gateway.filters.AuthorizationFilter;
import com.exalt.gateway.filters.RateLimitingFilter;
import com.exalt.gateway.discovery.ServiceDiscovery;

@SpringBootApplication
@EnableGatewayApplication
public class ApiGatewayApplication {
    private final GatewayRouter gatewayRouter;
    private final SecurityConfig securityConfig;
    private final ServiceDiscovery serviceDiscovery;
    
    public ApiGatewayApplication() {
        this.gatewayRouter = new GatewayRouter("API Gateway", "Main gateway for social e-commerce platform");
        this.securityConfig = new SecurityConfig();
        this.serviceDiscovery = new ServiceDiscovery();
    }
    
    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Social Commerce routes
            .route("social-commerce-api", r -> r
                .path("/api/v1/social-commerce/**")
                .filters(f -> f
                    .filter(new AuthenticationFilter())
                    .filter(new AuthorizationFilter("USER"))
                    .filter(new RateLimitingFilter(1000, Duration.ofMinutes(1)))
                    .stripPrefix(3))
                .uri("lb://social-commerce-service"))
            
            // Warehousing routes
            .route("warehousing-api", r -> r
                .path("/api/v1/warehousing/**")
                .filters(f -> f
                    .filter(new AuthenticationFilter())
                    .filter(new AuthorizationFilter("WAREHOUSE_ADMIN"))
                    .stripPrefix(3))
                .uri("lb://warehousing-service"))
            
            // Analytics routes
            .route("analytics-api", r -> r
                .path("/api/v1/analytics/**")
                .filters(f -> f
                    .filter(new AuthenticationFilter())
                    .filter(new AuthorizationFilter("ANALYTICS_USER"))
                    .filter(new RateLimitingFilter(500, Duration.ofMinutes(1)))
                    .stripPrefix(3))
                .uri("lb://analytics-engine"))
            
            .build();
    }
}
```

### Custom Authentication Filter

```java
import com.exalt.gateway.filters.AuthenticationFilter;
import com.exalt.gateway.security.JWTProcessor;

@Component
public class CustomAuthenticationFilter extends AuthenticationFilter {
    private final JWTProcessor jwtProcessor;
    
    public CustomAuthenticationFilter(JWTProcessor jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Extract JWT token from Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange);
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Validate JWT token
            JWTValidationResult validation = jwtProcessor.validateToken(token);
            if (!validation.isValid()) {
                return handleUnauthorized(exchange);
            }
            
            // Add user context to request
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-ID", validation.getUserId())
                .header("X-User-Roles", String.join(",", validation.getRoles()))
                .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
            
            return chain.filter(modifiedExchange);
            
        } catch (Exception e) {
            return handleUnauthorized(exchange);
        }
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Invalid or missing authentication token\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        
        return response.writeWith(Mono.just(buffer));
    }
}
```

### Rate Limiting Configuration

```java
import com.exalt.gateway.filters.RateLimitingFilter;
import com.exalt.gateway.ratelimit.RateLimitConfig;

@Configuration
public class RateLimitingConfiguration {
    
    @Bean
    public RateLimitConfig defaultRateLimit() {
        return RateLimitConfig.builder()
            .requestsPerSecond(100)
            .burstCapacity(200)
            .keyResolver(exchange -> {
                // Rate limit by user ID if authenticated, otherwise by IP
                String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
                if (userId != null) {
                    return Mono.just(userId);
                }
                return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            })
            .build();
    }
    
    @Bean
    public RateLimitConfig premiumUserRateLimit() {
        return RateLimitConfig.builder()
            .requestsPerSecond(500)
            .burstCapacity(1000)
            .keyResolver(exchange -> {
                String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
                String userType = exchange.getRequest().getHeaders().getFirst("X-User-Type");
                
                if ("PREMIUM".equals(userType)) {
                    return Mono.just("premium:" + userId);
                }
                return Mono.just("regular:" + userId);
            })
            .build();
    }
}
```

### Circuit Breaker Implementation

```java
import com.exalt.gateway.resilience.CircuitBreaker;
import com.exalt.gateway.resilience.CircuitBreakerConfig;

@Component
public class ServiceCircuitBreaker {
    private final Map<String, CircuitBreaker> circuitBreakers;
    
    public ServiceCircuitBreaker() {
        this.circuitBreakers = new ConcurrentHashMap<>();
        initializeCircuitBreakers();
    }
    
    private void initializeCircuitBreakers() {
        // Circuit breaker for social commerce service
        circuitBreakers.put("social-commerce", CircuitBreaker.builder()
            .config(CircuitBreakerConfig.builder()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build())
            .build());
        
        // Circuit breaker for analytics service
        circuitBreakers.put("analytics", CircuitBreaker.builder()
            .config(CircuitBreakerConfig.builder()
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .build())
            .build());
    }
    
    public <T> Mono<T> executeWithCircuitBreaker(String serviceName, Supplier<Mono<T>> operation) {
        CircuitBreaker circuitBreaker = circuitBreakers.get(serviceName);
        if (circuitBreaker == null) {
            return operation.get();
        }
        
        return circuitBreaker.execute(operation);
    }
}
```

### Service Discovery Integration

```java
import com.exalt.gateway.discovery.ServiceDiscovery;
import com.exalt.gateway.discovery.ServiceInstance;

@Service
public class DynamicRoutingService {
    private final ServiceDiscovery serviceDiscovery;
    private final LoadBalancer loadBalancer;
    
    public DynamicRoutingService(ServiceDiscovery serviceDiscovery, LoadBalancer loadBalancer) {
        this.serviceDiscovery = serviceDiscovery;
        this.loadBalancer = loadBalancer;
    }
    
    public Mono<URI> resolveServiceURI(String serviceName) {
        return serviceDiscovery.getServiceInstances(serviceName)
            .flatMap(instances -> {
                if (instances.isEmpty()) {
                    return Mono.error(new ServiceUnavailableException("No instances available for service: " + serviceName));
                }
                
                ServiceInstance selectedInstance = loadBalancer.select(instances);
                return Mono.just(URI.create(selectedInstance.getUri()));
            })
            .doOnError(error -> {
                // Log error and potentially trigger circuit breaker
                log.error("Failed to resolve service URI for {}: {}", serviceName, error.getMessage());
            });
    }
    
    public Mono<List<ServiceInstance>> getHealthyInstances(String serviceName) {
        return serviceDiscovery.getServiceInstances(serviceName)
            .flatMap(instances -> {
                List<Mono<ServiceInstance>> healthChecks = instances.stream()
                    .map(this::checkInstanceHealth)
                    .collect(Collectors.toList());
                
                return Flux.merge(healthChecks)
                    .collectList();
            });
    }
    
    private Mono<ServiceInstance> checkInstanceHealth(ServiceInstance instance) {
        return WebClient.builder()
            .baseUrl(instance.getUri())
            .build()
            .get()
            .uri("/actuator/health")
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(5))
            .map(response -> instance)
            .onErrorReturn(null)
            .filter(Objects::nonNull);
    }
}
```

## API Reference

### Core Gateway API

#### GatewayRouter
- `GatewayRouter(String name, String description)`: Initialize gateway router
- `void addRoute(RouteDefinition route)`: Add new route definition
- `boolean removeRoute(String routeId)`: Remove route definition
- `List<RouteDefinition> getRoutes()`: Get all configured routes
- `RouteMatch matchRoute(ServerHttpRequest request)`: Match request to route
- `void updateRoute(String routeId, RouteDefinition route)`: Update existing route

#### SecurityConfig
- `SecurityConfig()`: Initialize security configuration
- `void setJwtSecret(String secret)`: Set JWT signing secret
- `void setCorsSettings(CorsSettings settings)`: Configure CORS settings
- `void addSecurityFilter(SecurityFilter filter)`: Add security filter
- `boolean removeSecurityFilter(String filterId)`: Remove security filter
- `List<SecurityFilter> getSecurityFilters()`: Get all security filters

### Authentication and Authorization API

#### AuthenticationFilter
- `AuthenticationFilter()`: Default constructor
- `AuthenticationFilter(AuthConfig config)`: Constructor with configuration
- `Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)`: Process authentication
- `boolean isAuthenticationRequired(ServerHttpRequest request)`: Check if auth required
- `Mono<AuthenticationResult> authenticate(String token)`: Authenticate token
- `void setAuthenticationProvider(AuthenticationProvider provider)`: Set auth provider

#### JWTProcessor
- `JWTProcessor(String secret)`: Initialize with signing secret
- `String generateToken(UserPrincipal user)`: Generate JWT token
- `JWTValidationResult validateToken(String token)`: Validate JWT token
- `boolean isTokenExpired(String token)`: Check token expiration
- `Claims extractClaims(String token)`: Extract token claims
- `void revokeToken(String token)`: Revoke token

#### AuthorizationFilter
- `AuthorizationFilter(String requiredRole)`: Constructor with required role
- `AuthorizationFilter(List<String> requiredRoles)`: Constructor with role list
- `Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)`: Process authorization
- `boolean hasRequiredRole(List<String> userRoles, List<String> requiredRoles)`: Check role permissions
- `void setPermissionEvaluator(PermissionEvaluator evaluator)`: Set permission evaluator

### Rate Limiting API

#### RateLimitingFilter
- `RateLimitingFilter(int requestsPerSecond, Duration window)`: Constructor with basic config
- `RateLimitingFilter(RateLimitConfig config)`: Constructor with detailed config
- `Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)`: Apply rate limiting
- `boolean isRateLimited(String key)`: Check if key is rate limited
- `void resetRateLimit(String key)`: Reset rate limit for key

#### RateLimitConfig
- `RateLimitConfig.builder()`: Create configuration builder
- `RateLimitConfig requestsPerSecond(int rps)`: Set requests per second
- `RateLimitConfig burstCapacity(int capacity)`: Set burst capacity
- `RateLimitConfig keyResolver(Function<ServerWebExchange, Mono<String>> resolver)`: Set key resolver
- `RateLimitConfig skipSuccessfulRequests(boolean skip)`: Configure success request handling

### Service Discovery API

#### ServiceDiscovery
- `ServiceDiscovery()`: Default constructor with Eureka integration
- `ServiceDiscovery(DiscoveryConfig config)`: Constructor with custom config
- `Mono<List<ServiceInstance>> getServiceInstances(String serviceName)`: Get service instances
- `Mono<ServiceInstance> getServiceInstance(String serviceName, String instanceId)`: Get specific instance
- `void registerService(ServiceRegistration registration)`: Register service
- `void deregisterService(String serviceId)`: Deregister service

#### LoadBalancer
- `LoadBalancer(LoadBalancingStrategy strategy)`: Constructor with strategy
- `ServiceInstance select(List<ServiceInstance> instances)`: Select instance
- `void updateInstanceHealth(String instanceId, HealthStatus status)`: Update health status
- `LoadBalancingStrategy getStrategy()`: Get current strategy
- `void setStrategy(LoadBalancingStrategy strategy)`: Set load balancing strategy

### Monitoring and Logging API

#### RequestLogger
- `RequestLogger()`: Default constructor
- `RequestLogger(LoggingConfig config)`: Constructor with configuration
- `void logRequest(ServerHttpRequest request)`: Log incoming request
- `void logResponse(ServerHttpResponse response, Duration duration)`: Log response
- `void setLogLevel(LogLevel level)`: Set logging level
- `LoggingConfig getConfig()`: Get logging configuration

#### MetricsCollector
- `MetricsCollector()`: Default constructor
- `void recordRequest(String path, String method, int statusCode, Duration duration)`: Record request metrics
- `void recordError(String path, String method, String errorType)`: Record error metrics
- `Counter getRequestCounter(String path, String method)`: Get request counter
- `Timer getRequestTimer(String path, String method)`: Get request timer
- `Gauge getActiveRequestsGauge()`: Get active requests gauge

## Best Practices

1. **Security**: Always validate and sanitize input at the gateway level
2. **Performance**: Implement caching for frequently accessed routes
3. **Monitoring**: Enable comprehensive logging and metrics collection
4. **Resilience**: Use circuit breakers and timeouts for backend services
5. **Rate Limiting**: Implement appropriate rate limits to prevent abuse
6. **SSL/TLS**: Always use HTTPS in production environments
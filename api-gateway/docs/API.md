# API Gateway API Documentation

## Core API

### GatewayRouter
- `GatewayRouter()`: Default constructor
- `GatewayRouter(String name, String description)`: Constructor with name and description
- `UUID getId()`: Get the gateway ID
- `String getName()`: Get the gateway name
- `void setName(String name)`: Set the gateway name
- `String getDescription()`: Get the gateway description
- `void setDescription(String description)`: Set the gateway description
- `void addRoute(RouteDefinition route)`: Add new route definition
- `boolean removeRoute(String routeId)`: Remove route definition
- `List<RouteDefinition> getRoutes()`: Get all configured routes
- `RouteMatch matchRoute(ServerHttpRequest request)`: Match request to route
- `void updateRoute(String routeId, RouteDefinition route)`: Update existing route
- `void enableRoute(String routeId)`: Enable specific route
- `void disableRoute(String routeId)`: Disable specific route
- `RouteStatistics getRouteStatistics(String routeId)`: Get route performance statistics
- `LocalDateTime getCreatedAt()`: Get creation timestamp
- `LocalDateTime getUpdatedAt()`: Get last update timestamp

### RouteDefinition
- `RouteDefinition()`: Default constructor
- `RouteDefinition(String id, String path, String serviceUri)`: Constructor with basic routing info
- `UUID getId()`: Get the route ID
- `String getPath()`: Get the route path pattern
- `void setPath(String path)`: Set the route path pattern
- `String getServiceUri()`: Get the target service URI
- `void setServiceUri(String serviceUri)`: Set the target service URI
- `List<String> getMethods()`: Get allowed HTTP methods
- `void setMethods(List<String> methods)`: Set allowed HTTP methods
- `Map<String, String> getHeaders()`: Get required headers
- `void addHeader(String name, String value)`: Add required header
- `String removeHeader(String name)`: Remove required header
- `List<GatewayFilter> getFilters()`: Get applied filters
- `void addFilter(GatewayFilter filter)`: Add gateway filter
- `boolean removeFilter(String filterId)`: Remove gateway filter
- `boolean isEnabled()`: Check if route is enabled
- `void setEnabled(boolean enabled)`: Set route enabled status
- `LocalDateTime getCreatedAt()`: Get creation timestamp
- `LocalDateTime getUpdatedAt()`: Get last update timestamp

### SecurityConfig
- `SecurityConfig()`: Default constructor with secure defaults
- `void setJwtSecret(String secret)`: Set JWT signing secret
- `String getJwtSecret()`: Get JWT signing secret
- `void setJwtExpirationTime(Duration expiration)`: Set JWT expiration time
- `Duration getJwtExpirationTime()`: Get JWT expiration time
- `void setCorsSettings(CorsSettings settings)`: Configure CORS settings
- `CorsSettings getCorsSettings()`: Get CORS settings
- `void addSecurityFilter(SecurityFilter filter)`: Add security filter
- `boolean removeSecurityFilter(String filterId)`: Remove security filter
- `List<SecurityFilter> getSecurityFilters()`: Get all security filters
- `void setAuthenticationProvider(AuthenticationProvider provider)`: Set authentication provider
- `AuthenticationProvider getAuthenticationProvider()`: Get authentication provider
- `void addExcludedPath(String path)`: Add path to exclude from security
- `boolean removeExcludedPath(String path)`: Remove path from security exclusions
- `List<String> getExcludedPaths()`: Get all excluded paths

## Authentication and Authorization API

### AuthenticationFilter
- `AuthenticationFilter()`: Default constructor
- `AuthenticationFilter(AuthConfig config)`: Constructor with configuration
- `Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)`: Process authentication
- `boolean isAuthenticationRequired(ServerHttpRequest request)`: Check if auth required
- `Mono<AuthenticationResult> authenticate(String token)`: Authenticate token
- `void setAuthenticationProvider(AuthenticationProvider provider)`: Set auth provider
- `AuthenticationProvider getAuthenticationProvider()`: Get auth provider
- `void setTokenExtractor(TokenExtractor extractor)`: Set token extractor
- `TokenExtractor getTokenExtractor()`: Get token extractor
- `void addAuthenticationListener(AuthenticationListener listener)`: Add auth listener
- `boolean removeAuthenticationListener(String listenerId)`: Remove auth listener
- `List<AuthenticationListener> getAuthenticationListeners()`: Get all auth listeners

### AuthenticationResult
- `AuthenticationResult(boolean success, UserPrincipal user)`: Constructor with success status and user
- `AuthenticationResult(boolean success, String errorMessage)`: Constructor with failure info
- `boolean isSuccess()`: Check if authentication succeeded
- `UserPrincipal getUser()`: Get authenticated user
- `String getErrorMessage()`: Get error message if failed
- `List<String> getRoles()`: Get user roles
- `Map<String, Object> getAttributes()`: Get additional user attributes
- `Instant getAuthenticationTime()`: Get authentication timestamp
- `Duration getValidityDuration()`: Get token validity duration

### JWTProcessor
- `JWTProcessor(String secret)`: Initialize with signing secret
- `JWTProcessor(JWTConfig config)`: Initialize with detailed configuration
- `String generateToken(UserPrincipal user)`: Generate JWT token
- `String generateToken(UserPrincipal user, Duration expiration)`: Generate token with custom expiration
- `JWTValidationResult validateToken(String token)`: Validate JWT token
- `boolean isTokenExpired(String token)`: Check token expiration
- `Claims extractClaims(String token)`: Extract token claims
- `String extractUserId(String token)`: Extract user ID from token
- `List<String> extractRoles(String token)`: Extract roles from token
- `void revokeToken(String token)`: Revoke token
- `boolean isTokenRevoked(String token)`: Check if token is revoked
- `String refreshToken(String token)`: Refresh existing token
- `JWTConfig getConfig()`: Get JWT configuration
- `void setConfig(JWTConfig config)`: Set JWT configuration

### AuthorizationFilter
- `AuthorizationFilter(String requiredRole)`: Constructor with required role
- `AuthorizationFilter(List<String> requiredRoles)`: Constructor with role list
- `AuthorizationFilter(AuthorizationConfig config)`: Constructor with detailed config
- `Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)`: Process authorization
- `boolean hasRequiredRole(List<String> userRoles, List<String> requiredRoles)`: Check role permissions
- `boolean hasPermission(UserPrincipal user, String resource, String action)`: Check specific permission
- `void setPermissionEvaluator(PermissionEvaluator evaluator)`: Set permission evaluator
- `PermissionEvaluator getPermissionEvaluator()`: Get permission evaluator
- `void addRoleHierarchy(String higherRole, String lowerRole)`: Add role hierarchy
- `Map<String, List<String>> getRoleHierarchy()`: Get role hierarchy
- `AuthorizationConfig getConfig()`: Get authorization configuration

### UserPrincipal
- `UserPrincipal(String userId, String username, List<String> roles)`: Constructor with basic info
- `String getUserId()`: Get user ID
- `String getUsername()`: Get username
- `String getEmail()`: Get user email
- `void setEmail(String email)`: Set user email
- `List<String> getRoles()`: Get user roles
- `void addRole(String role)`: Add user role
- `boolean removeRole(String role)`: Remove user role
- `boolean hasRole(String role)`: Check if user has role
- `Map<String, Object> getAttributes()`: Get additional attributes
- `void addAttribute(String key, Object value)`: Add user attribute
- `Object getAttribute(String key)`: Get user attribute
- `boolean isEnabled()`: Check if user is enabled
- `void setEnabled(boolean enabled)`: Set user enabled status
- `LocalDateTime getLastLoginTime()`: Get last login timestamp
- `void setLastLoginTime(LocalDateTime lastLogin)`: Set last login timestamp

## Rate Limiting API

### RateLimitingFilter
- `RateLimitingFilter(int requestsPerSecond, Duration window)`: Constructor with basic config
- `RateLimitingFilter(RateLimitConfig config)`: Constructor with detailed config
- `Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)`: Apply rate limiting
- `boolean isRateLimited(String key)`: Check if key is rate limited
- `void resetRateLimit(String key)`: Reset rate limit for key
- `RateLimitStatus getRateLimitStatus(String key)`: Get current rate limit status
- `void setRateLimitConfig(RateLimitConfig config)`: Set rate limit configuration
- `RateLimitConfig getRateLimitConfig()`: Get rate limit configuration
- `void addRateLimitListener(RateLimitListener listener)`: Add rate limit listener
- `boolean removeRateLimitListener(String listenerId)`: Remove rate limit listener

### RateLimitConfig
- `RateLimitConfig.builder()`: Create configuration builder
- `RateLimitConfig requestsPerSecond(int rps)`: Set requests per second
- `RateLimitConfig burstCapacity(int capacity)`: Set burst capacity
- `RateLimitConfig keyResolver(Function<ServerWebExchange, Mono<String>> resolver)`: Set key resolver
- `RateLimitConfig skipSuccessfulRequests(boolean skip)`: Configure success request handling
- `RateLimitConfig skipIfHeaderPresent(String headerName)`: Skip rate limiting if header present
- `RateLimitConfig customResponseStatus(HttpStatus status)`: Set custom response status for rate limited requests
- `RateLimitConfig customResponseBody(String body)`: Set custom response body for rate limited requests
- `RateLimitConfig enableMetrics(boolean enable)`: Enable rate limiting metrics
- `RateLimitConfig slidingWindow(Duration window)`: Set sliding window duration

### RateLimitStatus
- `RateLimitStatus(String key, int remaining, int limit, Duration resetTime)`: Constructor with status info
- `String getKey()`: Get rate limit key
- `int getRemaining()`: Get remaining requests
- `int getLimit()`: Get total request limit
- `Duration getResetTime()`: Get time until reset
- `boolean isRateLimited()`: Check if currently rate limited
- `double getRequestRate()`: Get current request rate
- `Instant getLastRequestTime()`: Get last request timestamp

## Service Discovery API

### ServiceDiscovery
- `ServiceDiscovery()`: Default constructor with Eureka integration
- `ServiceDiscovery(DiscoveryConfig config)`: Constructor with custom config
- `Mono<List<ServiceInstance>> getServiceInstances(String serviceName)`: Get service instances
- `Mono<ServiceInstance> getServiceInstance(String serviceName, String instanceId)`: Get specific instance
- `Mono<List<String>> getServiceNames()`: Get all registered service names
- `void registerService(ServiceRegistration registration)`: Register service
- `void deregisterService(String serviceId)`: Deregister service
- `void subscribeToServiceChanges(String serviceName, ServiceChangeListener listener)`: Subscribe to service changes
- `boolean unsubscribeFromServiceChanges(String serviceName, String listenerId)`: Unsubscribe from service changes
- `ServiceHealth getServiceHealth(String serviceName)`: Get service health status
- `Map<String, ServiceMetadata> getServiceMetadata(String serviceName)`: Get service metadata

### ServiceInstance
- `ServiceInstance(String serviceId, String host, int port)`: Constructor with basic info
- `ServiceInstance(String serviceId, String host, int port, Map<String, String> metadata)`: Constructor with metadata
- `String getServiceId()`: Get service ID
- `String getHost()`: Get service host
- `int getPort()`: Get service port
- `String getUri()`: Get complete service URI
- `Map<String, String> getMetadata()`: Get service metadata
- `void addMetadata(String key, String value)`: Add metadata
- `String getMetadata(String key)`: Get specific metadata
- `boolean isHealthy()`: Check if instance is healthy
- `void setHealthy(boolean healthy)`: Set health status
- `HealthStatus getHealthStatus()`: Get detailed health status
- `Instant getRegistrationTime()`: Get registration timestamp
- `Instant getLastHeartbeat()`: Get last heartbeat timestamp

### LoadBalancer
- `LoadBalancer(LoadBalancingStrategy strategy)`: Constructor with strategy
- `ServiceInstance select(List<ServiceInstance> instances)`: Select instance
- `ServiceInstance select(List<ServiceInstance> instances, LoadBalancingContext context)`: Select with context
- `void updateInstanceHealth(String instanceId, HealthStatus status)`: Update health status
- `LoadBalancingStrategy getStrategy()`: Get current strategy
- `void setStrategy(LoadBalancingStrategy strategy)`: Set load balancing strategy
- `LoadBalancingStatistics getStatistics()`: Get load balancing statistics
- `void addLoadBalancingListener(LoadBalancingListener listener)`: Add load balancing listener
- `boolean removeLoadBalancingListener(String listenerId)`: Remove load balancing listener

### LoadBalancingStrategy
- `ROUND_ROBIN`: Round-robin load balancing
- `WEIGHTED_ROUND_ROBIN`: Weighted round-robin load balancing
- `LEAST_CONNECTIONS`: Route to instance with least connections
- `LEAST_RESPONSE_TIME`: Route to instance with fastest response time
- `RANDOM`: Random instance selection
- `IP_HASH`: Hash-based routing using client IP
- `CUSTOM`: Custom load balancing algorithm

## Circuit Breaker API

### CircuitBreaker
- `CircuitBreaker(String name, CircuitBreakerConfig config)`: Constructor with name and config
- `<T> Mono<T> execute(Supplier<Mono<T>> operation)`: Execute operation with circuit breaker
- `<T> T executeSync(Supplier<T> operation)`: Execute synchronous operation
- `CircuitBreakerState getState()`: Get current circuit breaker state
- `void forceOpen()`: Force circuit breaker to open state
- `void forceClose()`: Force circuit breaker to closed state
- `void reset()`: Reset circuit breaker to closed state
- `CircuitBreakerStatistics getStatistics()`: Get circuit breaker statistics
- `void addStateChangeListener(StateChangeListener listener)`: Add state change listener
- `boolean removeStateChangeListener(String listenerId)`: Remove state change listener
- `CircuitBreakerConfig getConfig()`: Get circuit breaker configuration
- `void updateConfig(CircuitBreakerConfig config)`: Update configuration

### CircuitBreakerConfig
- `CircuitBreakerConfig.builder()`: Create configuration builder
- `CircuitBreakerConfig failureRateThreshold(float threshold)`: Set failure rate threshold
- `CircuitBreakerConfig waitDurationInOpenState(Duration duration)`: Set wait duration in open state
- `CircuitBreakerConfig slidingWindowSize(int size)`: Set sliding window size
- `CircuitBreakerConfig minimumNumberOfCalls(int calls)`: Set minimum calls for evaluation
- `CircuitBreakerConfig slowCallRateThreshold(float threshold)`: Set slow call rate threshold
- `CircuitBreakerConfig slowCallDurationThreshold(Duration duration)`: Set slow call duration threshold
- `CircuitBreakerConfig permittedNumberOfCallsInHalfOpenState(int calls)`: Set permitted calls in half-open state

### CircuitBreakerState
- `CLOSED`: Circuit breaker is closed, allowing requests
- `OPEN`: Circuit breaker is open, rejecting requests
- `HALF_OPEN`: Circuit breaker is half-open, testing if service recovered

## Monitoring and Logging API

### RequestLogger
- `RequestLogger()`: Default constructor
- `RequestLogger(LoggingConfig config)`: Constructor with configuration
- `void logRequest(ServerHttpRequest request)`: Log incoming request
- `void logResponse(ServerHttpResponse response, Duration duration)`: Log response
- `void logRequest(ServerHttpRequest request, String correlationId)`: Log request with correlation ID
- `void logError(ServerHttpRequest request, Throwable error)`: Log request error
- `void setLogLevel(LogLevel level)`: Set logging level
- `LogLevel getLogLevel()`: Get current logging level
- `LoggingConfig getConfig()`: Get logging configuration
- `void setConfig(LoggingConfig config)`: Set logging configuration
- `void addLogFilter(LogFilter filter)`: Add log filter
- `boolean removeLogFilter(String filterId)`: Remove log filter

### MetricsCollector
- `MetricsCollector()`: Default constructor
- `void recordRequest(String path, String method, int statusCode, Duration duration)`: Record request metrics
- `void recordError(String path, String method, String errorType)`: Record error metrics
- `void recordRateLimitEvent(String key, boolean rateLimited)`: Record rate limiting event
- `void recordCircuitBreakerEvent(String serviceName, CircuitBreakerState state)`: Record circuit breaker event
- `Counter getRequestCounter(String path, String method)`: Get request counter
- `Timer getRequestTimer(String path, String method)`: Get request timer
- `Gauge getActiveRequestsGauge()`: Get active requests gauge
- `Histogram getRequestSizeHistogram()`: Get request size histogram
- `Histogram getResponseSizeHistogram()`: Get response size histogram
- `Map<String, Metric> getAllMetrics()`: Get all collected metrics

### PerformanceMonitor
- `PerformanceMonitor()`: Default constructor
- `PerformanceMonitor(PerformanceConfig config)`: Constructor with configuration
- `void startRequest(String requestId)`: Start monitoring request
- `void endRequest(String requestId, int statusCode)`: End monitoring request
- `RequestPerformance getRequestPerformance(String requestId)`: Get request performance data
- `List<RequestPerformance> getSlowRequests(Duration threshold)`: Get requests exceeding threshold
- `PerformanceStatistics getStatistics()`: Get overall performance statistics
- `void addPerformanceListener(PerformanceListener listener)`: Add performance listener
- `boolean removePerformanceListener(String listenerId)`: Remove performance listener

## Health Check API

### HealthChecker
- `HealthChecker()`: Default constructor
- `HealthChecker(HealthCheckConfig config)`: Constructor with configuration
- `Mono<HealthStatus> checkHealth(ServiceInstance instance)`: Check single instance health
- `Mono<Map<String, HealthStatus>> checkAllServices()`: Check all registered services
- `void addHealthCheckListener(HealthCheckListener listener)`: Add health check listener
- `boolean removeHealthCheckListener(String listenerId)`: Remove health check listener
- `void setHealthCheckInterval(Duration interval)`: Set health check interval
- `Duration getHealthCheckInterval()`: Get health check interval
- `void enableHealthCheck(String serviceName)`: Enable health checking for service
- `void disableHealthCheck(String serviceName)`: Disable health checking for service

### HealthStatus
- `HealthStatus(boolean healthy, String message)`: Constructor with status and message
- `HealthStatus(boolean healthy, String message, Map<String, Object> details)`: Constructor with details
- `boolean isHealthy()`: Check if healthy
- `String getMessage()`: Get status message
- `Map<String, Object> getDetails()`: Get health details
- `void addDetail(String key, Object value)`: Add health detail
- `Object getDetail(String key)`: Get specific health detail
- `Instant getCheckTime()`: Get health check timestamp
- `Duration getResponseTime()`: Get health check response time

## Configuration API

### GatewayConfig
- `GatewayConfig()`: Default constructor
- `GatewayConfig(String name, int port)`: Constructor with basic config
- `String getName()`: Get gateway name
- `void setName(String name)`: Set gateway name
- `int getPort()`: Get gateway port
- `void setPort(int port)`: Set gateway port
- `Duration getRequestTimeout()`: Get request timeout
- `void setRequestTimeout(Duration timeout)`: Set request timeout
- `int getMaxConnections()`: Get maximum connections
- `void setMaxConnections(int maxConnections)`: Set maximum connections
- `boolean isSslEnabled()`: Check if SSL is enabled
- `void setSslEnabled(boolean sslEnabled)`: Enable/disable SSL
- `SslConfig getSslConfig()`: Get SSL configuration
- `void setSslConfig(SslConfig sslConfig)`: Set SSL configuration
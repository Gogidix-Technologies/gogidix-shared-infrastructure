# Architecture Documentation - API Gateway

## Overview

The API Gateway serves as the single entry point for all client requests in the Social E-commerce Ecosystem. It provides centralized routing, authentication, authorization, rate limiting, monitoring, and security for all microservices across the platform.

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Component Overview](#component-overview)
3. [Security Architecture](#security-architecture)
4. [Routing Architecture](#routing-architecture)
5. [Resilience Patterns](#resilience-patterns)
6. [Performance Architecture](#performance-architecture)
7. [Monitoring Architecture](#monitoring-architecture)
8. [Integration Points](#integration-points)

## System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        API Gateway                          │
├─────────────────────────┬───────────────────────────────────┤
│   Security Layer       │        Routing Layer              │
│ - Authentication        │ - Service Discovery               │
│ - Authorization         │ - Load Balancing                  │
│ - Rate Limiting         │ - Circuit Breaker                 │
├─────────────────────────┼───────────────────────────────────┤
│   Monitoring Layer      │        Resilience Layer           │
│ - Request Logging       │ - Retry Logic                     │
│ - Metrics Collection    │ - Timeout Handling                │
│ - Performance Tracking  │ - Fallback Mechanisms             │
└─────────────────────────┴───────────────────────────────────┘
```

### Request Flow Architecture

```
Client Request → SSL Termination → Authentication → Authorization → Rate Limiting → Routing → Backend Service
      ↓               ↓                ↓              ↓              ↓           ↓            ↓
   Validate      JWT Validation   Role Check    Quota Check    Load Balance   Service Call  Response
      ↓               ↓                ↓              ↓              ↓           ↓            ↓
   Log Request   Security Audit   Access Log    Rate Metrics   Route Metrics  Call Metrics  Log Response
```

### Architecture Principles

1. **Single Entry Point**: All external requests flow through the gateway
2. **Security-First**: Authentication and authorization at the gateway level
3. **Resilience**: Circuit breakers, retries, and fallbacks
4. **Observability**: Comprehensive logging, metrics, and tracing
5. **Scalability**: Horizontal scaling with load balancing

## Component Overview

### Core Gateway Components

#### Gateway Router
- **GatewayRouter**: Central routing engine
- **RouteDefinition**: Route configuration and matching
- **RouteResolver**: Dynamic route resolution
- **PathMatcher**: URL pattern matching engine

#### Security Components
- **AuthenticationFilter**: JWT token validation
- **AuthorizationFilter**: Role-based access control
- **SecurityConfig**: Security policy configuration
- **JWTProcessor**: Token processing and validation

#### Traffic Management
- **RateLimitingFilter**: Request throttling and quota management
- **LoadBalancer**: Instance selection and distribution
- **CircuitBreaker**: Fault tolerance and service protection
- **RetryHandler**: Request retry logic

### Supporting Components

| Component | Purpose | Technology |
|-----------|---------|------------|
| ServiceDiscovery | Dynamic service location | Eureka Client |
| MetricsCollector | Performance monitoring | Micrometer |
| RequestLogger | Request/response logging | Logback |
| HealthChecker | Service health monitoring | Spring Boot Actuator |

### Filter Chain Architecture

```
Incoming Request
       ↓
┌─────────────────┐
│ Security Filters │
│ - Authentication │
│ - Authorization  │
│ - CORS          │
└─────────────────┘
       ↓
┌─────────────────┐
│Traffic Filters  │
│ - Rate Limiting │
│ - Request Size  │
│ - Compression   │
└─────────────────┘
       ↓
┌─────────────────┐
│Routing Filters  │
│ - Path Rewrite  │
│ - Header Inject │
│ - Load Balance  │
└─────────────────┘
       ↓
┌─────────────────┐
│Backend Service  │
│ - Circuit Break │
│ - Retry Logic   │
│ - Timeout       │
└─────────────────┘
```

## Security Architecture

### Authentication Flow

```
Client → API Gateway → JWT Validation → User Context → Backend Service
  ↓         ↓              ↓              ↓              ↓
Request   Extract JWT    Validate      Add Headers    Authenticated
          from Header    Signature     (User-ID,      Request
                        & Claims       Roles)
```

### Authorization Matrix

| Service | Public Endpoints | User Endpoints | Admin Endpoints |
|---------|------------------|----------------|-----------------|
| Social Commerce | /api/v1/products | /api/v1/orders | /api/v1/admin/* |
| Warehousing | /api/v1/locations | /api/v1/shipments | /api/v1/warehouse/* |
| Analytics | - | /api/v1/reports | /api/v1/analytics/* |
| User Profile | - | /api/v1/profile | /api/v1/users/* |

### Security Policies

1. **JWT Token Validation**
   - Signature verification
   - Expiration checking
   - Issuer validation
   - Audience verification

2. **Role-Based Access Control**
   - Hierarchical role structure
   - Resource-based permissions
   - Dynamic role evaluation

3. **Request Validation**
   - Input sanitization
   - Size limitations
   - Content type validation

## Routing Architecture

### Service Discovery Integration

```
API Gateway → Eureka Server → Service Registry
     ↓              ↓              ↓
Route Request   Get Instances   Load Balance
     ↓              ↓              ↓
Select Instance Backend Service Health Check
```

### Dynamic Routing

- **Path-based Routing**: `/api/v1/social-commerce/**` → Social Commerce Service
- **Header-based Routing**: `X-API-Version: v2` → Version 2 Services
- **Parameter-based Routing**: `?region=eu` → European Data Center

### Load Balancing Strategies

1. **Round Robin**: Even distribution across instances
2. **Weighted Round Robin**: Distribution based on instance capacity
3. **Least Connections**: Route to instance with fewest active connections
4. **Least Response Time**: Route to fastest responding instance
5. **IP Hash**: Consistent routing based on client IP

## Resilience Patterns

### Circuit Breaker Pattern

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     CLOSED      │───▶│    OPEN         │───▶│   HALF-OPEN     │
│ (Normal Flow)   │    │ (Fail Fast)     │    │ (Testing)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         ▲                                              │
         │                                              │
         └──────────────────────────────────────────────┘
                        (Success)
```

#### Circuit Breaker Configuration

```yaml
circuit-breaker:
  social-commerce:
    failure-rate-threshold: 50%
    wait-duration-in-open-state: 60s
    sliding-window-size: 10
    minimum-number-of-calls: 5
  
  analytics:
    failure-rate-threshold: 30%
    wait-duration-in-open-state: 30s
    sliding-window-size: 20
    minimum-number-of-calls: 10
```

### Retry Logic

- **Exponential Backoff**: 100ms, 200ms, 400ms, 800ms
- **Jitter**: Random delay to prevent thundering herd
- **Max Attempts**: Configurable per service
- **Idempotent Operations**: Only retry safe operations

### Timeout Management

| Service Type | Connect Timeout | Read Timeout | Total Timeout |
|--------------|-----------------|--------------|---------------|
| Social Commerce | 5s | 30s | 35s |
| Analytics | 3s | 60s | 63s |
| Warehousing | 5s | 20s | 25s |
| Courier Services | 5s | 15s | 20s |

## Performance Architecture

### Caching Strategy

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Browser Cache  │    │  CDN Cache      │    │ Gateway Cache   │
│  (Client-side)  │    │  (Edge)         │    │ (Application)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  │
                         ┌─────────────────┐
                         │ Backend Service │
                         │    (Origin)     │
                         └─────────────────┘
```

### Connection Pooling

```yaml
connection-pool:
  max-connections: 1000
  max-connections-per-route: 100
  connection-timeout: 5s
  socket-timeout: 30s
  connection-request-timeout: 10s
  keep-alive-duration: 20s
  max-idle-time: 60s
```

### Compression

- **Request Compression**: Gzip for request bodies > 1KB
- **Response Compression**: Automatic compression for responses > 1KB
- **Content Types**: JSON, XML, HTML, CSS, JavaScript

## Monitoring Architecture

### Metrics Collection

```
API Gateway → Prometheus → Grafana Dashboard
     ↓             ↓            ↓
 Custom Metrics  Storage    Visualization
     ↓             ↓            ↓
Business KPIs   Time Series   Alerting
```

### Key Metrics

#### Request Metrics
- **Request Rate**: Requests per second
- **Response Time**: P50, P95, P99 percentiles
- **Error Rate**: 4xx and 5xx error percentages
- **Throughput**: Bytes per second

#### Security Metrics
- **Authentication Failures**: Failed login attempts
- **Authorization Denials**: Access denied events
- **Rate Limit Events**: Throttled requests
- **Security Violations**: Suspicious activities

#### Performance Metrics
- **Circuit Breaker Events**: State changes and trip events
- **Retry Attempts**: Retry counts and success rates
- **Cache Hit Ratio**: Cache effectiveness
- **Connection Pool Usage**: Pool utilization

### Distributed Tracing

```
Client Request → API Gateway → Service A → Service B → Database
       ↓              ↓           ↓           ↓          ↓
   Trace Start    Add Span    Add Span    Add Span   Add Span
       ↓              ↓           ↓           ↓          ↓
   Correlation   Gateway Span  Service    Service   Database
      ID         (routing)     Span       Span       Span
```

### Logging Strategy

1. **Access Logs**: All requests and responses
2. **Security Logs**: Authentication and authorization events
3. **Error Logs**: Application and system errors
4. **Audit Logs**: Administrative actions and configuration changes

## Integration Points

### Internal Service Integration

| Service | Integration Method | Purpose |
|---------|-------------------|---------|
| Auth Service | REST API | User authentication |
| Analytics Engine | REST/Kafka | Request metrics |
| Notification Service | Kafka | Security alerts |
| Config Server | HTTP | Dynamic configuration |

### External Integration

1. **Service Discovery**
   - Eureka Server integration
   - Health check endpoints
   - Instance registration

2. **Security Providers**
   - OAuth2/OpenID Connect
   - LDAP/Active Directory
   - Custom authentication providers

3. **Monitoring Systems**
   - Prometheus metrics export
   - Grafana dashboard integration
   - Alertmanager notifications

## Deployment Architecture

### Container Strategy

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/api-gateway.jar app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    spec:
      containers:
      - name: api-gateway
        image: api-gateway:latest
        ports:
        - containerPort: 8080
        - containerPort: 9090
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
```

### High Availability

- **Multiple Instances**: Minimum 3 replicas for production
- **Health Checks**: Liveness and readiness probes
- **Rolling Updates**: Zero-downtime deployments
- **Auto-scaling**: HPA based on CPU and memory usage

## Security Considerations

### SSL/TLS Configuration

- **TLS 1.2+**: Minimum TLS version
- **Perfect Forward Secrecy**: ECDHE cipher suites
- **HSTS**: HTTP Strict Transport Security
- **Certificate Management**: Automated renewal

### Rate Limiting

```yaml
rate-limiting:
  global:
    requests-per-second: 1000
    burst-capacity: 2000
  
  per-user:
    requests-per-second: 100
    burst-capacity: 200
  
  per-api:
    requests-per-second: 500
    burst-capacity: 1000
```

### CORS Configuration

```yaml
cors:
  allowed-origins:
    - https://app.exalt.com
    - https://admin.exalt.com
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
  allowed-headers:
    - Authorization
    - Content-Type
    - X-Requested-With
  max-age: 3600
```

## Future Considerations

1. **GraphQL Support**: Unified API interface
2. **gRPC Integration**: High-performance RPC support
3. **Service Mesh**: Istio integration for advanced traffic management
4. **API Versioning**: Advanced versioning strategies
5. **Serverless Integration**: AWS Lambda and Azure Functions support

## References

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [API Gateway Patterns](https://microservices.io/patterns/apigateway.html)
- [OAuth 2.0 Specification](https://tools.ietf.org/html/rfc6749)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
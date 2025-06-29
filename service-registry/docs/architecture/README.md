# Architecture Documentation - Service Registry (Eureka)

## Overview

The Service Registry (Eureka) provides centralized service discovery and registration for the Social E-commerce Ecosystem. It enables microservices to dynamically discover and communicate with each other, ensuring high availability, load balancing, and fault tolerance across the distributed system.

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Component Architecture](#component-architecture)
3. [Service Discovery Pattern](#service-discovery-pattern)
4. [Registration & Health Checking](#registration--health-checking)
5. [Load Balancing Strategy](#load-balancing-strategy)
6. [High Availability Design](#high-availability-design)
7. [Security Architecture](#security-architecture)
8. [Data Architecture](#data-architecture)
9. [Integration Architecture](#integration-architecture)
10. [Deployment Architecture](#deployment-architecture)

## System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Service Registry (Eureka)                    │
├─────────────────┬───────────────────┬───────────────────────────┤
│ Service         │   Health Check    │      Load Balancing       │
│ Discovery       │      Engine       │        Engine             │
├─────────────────┼───────────────────┼───────────────────────────┤
│ - Registration  │ - Health Monitor  │ - Round Robin             │
│ - Deregistration│ - Status Tracking │ - Weighted Distribution   │
│ - Lookup        │ - Failure Detect  │ - Availability Zone       │
├─────────────────┴───────────────────┴───────────────────────────┤
│                    Registry & Metadata Store                    │
│ - Service Metadata    - Instance Status    - Network Topology  │
└─────────────────────────────────────────────────────────────────┘
```

### Service Ecosystem Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Auth Service   │    │ Order Service   │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │Eureka Client│ │    │ │Eureka Client│ │    │ │Eureka Client│ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          │              ┌───────▼───────┐              │
          └─────────────►│ Eureka Server │◄─────────────┘
                         │               │
                         │ Service       │
                         │ Registry      │
                         └───────────────┘
```

### Architecture Principles

1. **Self-Service Discovery**: Automatic service registration and discovery
2. **Resilience**: Graceful degradation under network partitions
3. **Scalability**: Horizontal scaling of registry clusters
4. **Consistency**: Eventually consistent service registry
5. **Simplicity**: Minimal configuration and maintenance
6. **Performance**: Fast service lookup and registration

## Component Architecture

### Core Components

#### Eureka Server Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Eureka Server                            │
├─────────────────┬─────────────────┬─────────────────────────┤
│ Registry Core   │ Health Monitor  │    Management           │
│ - Service Store │ - Heartbeat     │    - Admin Interface    │
│ - Metadata Mgmt │ - Status Check  │    - Metrics Export     │
│ - Lease Mgmt    │ - Eviction      │    - Configuration      │
├─────────────────┼─────────────────┼─────────────────────────┤
│ Replication     │ REST API        │    Security             │
│ - Peer Sync     │ - Registration  │    - Authentication     │
│ - Cluster Mgmt  │ - Discovery     │    - Authorization      │
│ - Consistency   │ - Health Check  │    - TLS Termination    │
└─────────────────┴─────────────────┴─────────────────────────┘
```

#### Eureka Client Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Eureka Client                            │
├─────────────────┬─────────────────┬─────────────────────────┤
│ Registration    │ Discovery       │    Load Balancing       │
│ - Self Register │ - Service Cache │    - Client-side LB     │
│ - Heartbeat     │ - Cache Refresh │    - Ribbon Integration │
│ - Deregister    │ - Fetch Delta   │    - Retry Logic        │
├─────────────────┼─────────────────┼─────────────────────────┤
│ Health Check    │ Circuit Breaker │    Configuration        │
│ - Health Status │ - Hystrix       │    - Auto Config        │
│ - Custom Checks │ - Fallback      │    - Property Binding   │
│ - Indicator     │ - Monitoring    │    - Profile Support    │
└─────────────────┴─────────────────┴─────────────────────────┘
```

### Supporting Infrastructure

| Component | Purpose | Technology |
|-----------|---------|------------|
| Service Store | Registry persistence | In-memory + backup |
| Health Monitor | Service availability | HTTP/TCP health checks |
| Load Balancer | Traffic distribution | Netflix Ribbon |
| Cache Layer | Performance optimization | Local caching |
| Admin Dashboard | Management interface | Spring Boot Admin |
| Metrics Export | Monitoring integration | Micrometer + Prometheus |

## Service Discovery Pattern

### Service Discovery Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                Service Discovery Flow                       │
├─────────────────────────────────────────────────────────────┤
│ 1. Service Registration                                     │
│    Client → Register → Eureka Server → Store Metadata      │
│                                                             │
│ 2. Service Discovery                                        │
│    Client → Query → Eureka Server → Return Service List    │
│                                                             │
│ 3. Load Balancing                                           │
│    Client → Select Instance → Direct Communication         │
│                                                             │
│ 4. Health Monitoring                                        │
│    Client → Heartbeat → Eureka Server → Update Status      │
└─────────────────────────────────────────────────────────────┘
```

### Registration Flow

```
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Service   │    │ Eureka Server   │    │   Registry      │
│   Instance  │    │                 │    │   Database      │
└──────┬──────┘    └─────────┬───────┘    └─────────┬───────┘
       │                     │                      │
       │ 1. Register Request │                      │
       ├────────────────────►│                      │
       │                     │ 2. Store Instance    │
       │                     ├─────────────────────►│
       │                     │ 3. Confirm Storage   │
       │                     │◄─────────────────────┤
       │ 4. Registration OK  │                      │
       │◄────────────────────┤                      │
       │                     │                      │
       │ 5. Heartbeat (30s)  │                      │
       ├────────────────────►│                      │
       │                     │ 6. Update Timestamp  │
       │                     ├─────────────────────►│
```

### Discovery Flow

```
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client    │    │ Eureka Server   │    │   Target        │
│   Service   │    │                 │    │   Service       │
└──────┬──────┘    └─────────┬───────┘    └─────────┬───────┘
       │                     │                      │
       │ 1. Discover Request │                      │
       ├────────────────────►│                      │
       │                     │                      │
       │ 2. Service List     │                      │
       │◄────────────────────┤                      │
       │                     │                      │
       │ 3. Select Instance  │                      │
       │ (Load Balancing)    │                      │
       │                     │                      │
       │ 4. Direct Call      │                      │
       ├─────────────────────┼─────────────────────►│
       │                     │ 5. Response          │
       │◄────────────────────┼──────────────────────┤
```

## Registration & Health Checking

### Service Registration

```yaml
# Service Registration Configuration
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
    registry-fetch-interval-seconds: 30
    
  instance:
    hostname: ${spring.application.name}
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    health-check-url-path: /actuator/health
    status-page-url-path: /actuator/info
    metadata-map:
      version: ${project.version}
      environment: ${spring.profiles.active}
```

### Health Check Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Health Check System                       │
├─────────────────┬─────────────────┬─────────────────────────┤
│ Health Endpoint │ Custom Checks   │    Status Aggregation   │
│ - Spring Boot   │ - Database      │    - UP/DOWN/UNKNOWN    │
│ - /health       │ - External APIs │    - Component Status   │
│ - JSON Response │ - Business Logic│    - Overall Health     │
├─────────────────┼─────────────────┼─────────────────────────┤
│ Heartbeat Mgmt  │ Failure Detect  │    Recovery Process     │
│ - 30s Interval  │ - Missed Beats  │    - Auto Registration  │
│ - HTTP/TCP      │ - Threshold     │    - Status Update      │
│ - Retry Logic   │ - Eviction      │    - Cache Invalidation │
└─────────────────┴─────────────────┴─────────────────────────┘
```

### Custom Health Indicators

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down().build();
    }
}

@Component
public class ExternalServiceHealthIndicator implements ReactiveHealthIndicator {
    
    @Autowired
    private WebClient webClient;
    
    @Override
    public Mono<Health> health() {
        return webClient.get()
            .uri("/health")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> Health.up()
                .withDetail("external-service", "Available")
                .build())
            .onErrorReturn(Health.down()
                .withDetail("external-service", "Unavailable")
                .build());
    }
}
```

## Load Balancing Strategy

### Client-Side Load Balancing

```
┌─────────────────────────────────────────────────────────────┐
│                Load Balancing Strategies                    │
├─────────────────┬─────────────────┬─────────────────────────┤
│ Round Robin     │ Weighted Round  │    Availability Zone    │
│ - Equal Dist    │ Robin           │    - Zone Preference    │
│ - Simple Logic  │ - Instance Load │    - Cross-Zone Backup │
│ - Default Mode  │ - CPU/Memory    │    - Latency Optimize   │
├─────────────────┼─────────────────┼─────────────────────────┤
│ Random          │ Least Conn      │    Response Time        │
│ - Random Select │ - Active Conn   │    - Latency Based      │
│ - Good Spread   │ - Connection    │    - Performance Opt    │
│ - Stateless     │   Pool Mgmt     │    - Adaptive           │
└─────────────────┴─────────────────┴─────────────────────────┘
```

### Netflix Ribbon Configuration

```yaml
# Ribbon Load Balancer Configuration
ribbon:
  NFLoadBalancerRuleClassName: com.netflix.loadbalancer.WeightedResponseTimeRule
  NFLoadBalancerPingClassName: com.netflix.loadbalancer.PingUrl
  NIWSServerListClassName: com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList
  NIWSServerListFilterClassName: com.netflix.loadbalancer.ServerListSubsetFilter
  
  # Connection settings
  ConnectTimeout: 3000
  ReadTimeout: 60000
  MaxAutoRetries: 1
  MaxAutoRetriesNextServer: 3
  
  # Health check
  PingInterval: 30
  HealthCheckEnabled: true
```

### Load Balancing Implementation

```java
@Configuration
@RibbonClient(name = "order-service", configuration = OrderServiceRibbonConfig.class)
public class RibbonConfiguration {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public IRule ribbonRule() {
        return new WeightedResponseTimeRule();
    }
    
    @Bean
    public IPing ribbonPing() {
        return new PingUrl(false, "/actuator/health");
    }
    
    @Bean
    public ServerListSubsetFilter serverListFilter() {
        ServerListSubsetFilter filter = new ServerListSubsetFilter();
        filter.setSize(20);
        return filter;
    }
}

// Service call with load balancing
@Service
public class OrderServiceClient {
    
    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;
    
    public Order getOrder(UUID orderId) {
        String url = "http://order-service/api/orders/" + orderId;
        return restTemplate.getForObject(url, Order.class);
    }
}
```

## High Availability Design

### Multi-Zone Deployment

```
┌─────────────────────────────────────────────────────────────┐
│              Multi-Zone Eureka Deployment                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Zone A    │  │   Zone B    │  │   Zone C    │        │
│  │             │  │             │  │             │        │
│  │ ┌─────────┐ │  │ ┌─────────┐ │  │ ┌─────────┐ │        │
│  │ │ Eureka  │ │  │ │ Eureka  │ │  │ │ Eureka  │ │        │
│  │ │ Server  │ │  │ │ Server  │ │  │ │ Server  │ │        │
│  │ │    1    │ │  │ │    2    │ │  │ │    3    │ │        │
│  │ └─────────┘ │  │ └─────────┘ │  │ └─────────┘ │        │
│  │             │  │             │  │             │        │
│  │ Services    │  │ Services    │  │ Services    │        │
│  │ - Auth      │  │ - Order     │  │ - Payment   │        │
│  │ - User      │  │ - Product   │  │ - Shipping  │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
│                                                             │
│  ◄────────────── Peer Replication ──────────────►        │
└─────────────────────────────────────────────────────────────┘
```

### Peer-to-Peer Replication

```yaml
# Eureka Server Cluster Configuration
spring:
  profiles:
    active: peer1
    
---
spring:
  profiles: peer1
  application:
    name: eureka-server
server:
  port: 8761
eureka:
  instance:
    hostname: eureka-peer1
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka-peer2:8762/eureka/,http://eureka-peer3:8763/eureka/

---
spring:
  profiles: peer2
  application:
    name: eureka-server
server:
  port: 8762
eureka:
  instance:
    hostname: eureka-peer2
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka-peer1:8761/eureka/,http://eureka-peer3:8763/eureka/
```

### Failover Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Failover Strategy                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 1. Primary Eureka Server Failure                           │
│    Client → Detect Failure → Switch to Backup Server       │
│                                                             │
│ 2. Network Partition                                        │
│    Client → Use Cached Registry → Gradual Degradation      │
│                                                             │
│ 3. Complete Registry Failure                               │
│    Client → Fallback to Static Configuration               │
│                                                             │
│ 4. Service Instance Failure                                │
│    Client → Remove from Cache → Retry with Next Instance   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Security Architecture

### Authentication & Authorization

```yaml
# Security Configuration
security:
  basic:
    enabled: true
    username: admin
    password: ${EUREKA_PASSWORD:admin123}
    
eureka:
  server:
    enable-self-preservation: false
    
  client:
    register-with-eureka: false
    fetch-registry: false
    
management:
  security:
    enabled: true
    roles: ADMIN
```

### TLS Configuration

```yaml
# HTTPS Configuration
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.jks
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: JKS
    trust-store: classpath:truststore.jks
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD}
    
eureka:
  client:
    service-url:
      defaultZone: https://eureka-server:8443/eureka/
```

### Network Security

```yaml
# Network Security Configuration
management:
  endpoints:
    web:
      base-path: /actuator
      exposure:
        include: health,info,metrics
        exclude: shutdown,env
  
  endpoint:
    health:
      show-details: when-authorized
      roles: ADMIN
```

## Data Architecture

### Service Registry Schema

```json
{
  "application": "order-service",
  "instance": {
    "instanceId": "order-service:192.168.1.100:8080",
    "hostName": "order-service-pod-1",
    "app": "ORDER-SERVICE",
    "ipAddr": "192.168.1.100",
    "port": {
      "$": 8080,
      "@enabled": true
    },
    "securePort": {
      "$": 8443,
      "@enabled": true
    },
    "status": "UP",
    "overriddenStatus": "UNKNOWN",
    "leaseInfo": {
      "renewalIntervalInSecs": 30,
      "durationInSecs": 90,
      "registrationTimestamp": 1640995200000,
      "lastRenewalTimestamp": 1640995800000,
      "evictionTimestamp": 0,
      "serviceUpTimestamp": 1640995200000
    },
    "metadata": {
      "version": "1.0.0",
      "environment": "production",
      "zone": "us-east-1a",
      "management.port": "8081"
    },
    "healthCheckUrl": "http://192.168.1.100:8080/actuator/health",
    "statusPageUrl": "http://192.168.1.100:8080/actuator/info",
    "homePageUrl": "http://192.168.1.100:8080/"
  }
}
```

### Registry Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                   Registry Data Flow                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Service Registration → Validation → Storage → Replication  │
│                                                             │
│ Health Check → Status Update → Cache Invalidation          │
│                                                             │
│ Service Discovery → Cache Lookup → Response Generation     │
│                                                             │
│ Lease Expiration → Eviction → Cleanup → Notification      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Integration Architecture

### Service Integration Points

```
┌─────────────────────────────────────────────────────────────┐
│                Service Integration Matrix                   │
├─────────────────┬─────────────────┬─────────────────────────┤
│ API Gateway     │ Config Server   │    Monitoring           │
│ - Route Discovery│ - Service Config│    - Health Metrics    │
│ - Load Balancing│ - Dynamic Props │    - Registration Stats │
│ - Service Mesh  │ - Env Variables │    - Performance Data   │
├─────────────────┼─────────────────┼─────────────────────────┤
│ Circuit Breaker │ Admin Dashboard │    Distributed Tracing │
│ - Hystrix       │ - Service Status│    - Zipkin Integration │
│ - Resilience4j  │ - Health Check  │    - Trace Context     │
│ - Failover      │ - Metrics View  │    - Performance Track  │
└─────────────────┴─────────────────┴─────────────────────────┘
```

### Event-Driven Architecture

```yaml
events:
  service-lifecycle:
    - service.registered
    - service.deregistered
    - service.status.changed
    - service.health.failed
    
  registry-management:
    - registry.server.started
    - registry.server.stopped
    - registry.peer.connected
    - registry.peer.disconnected
    
  monitoring:
    - registry.metrics.collected
    - registry.alert.triggered
    - registry.threshold.exceeded
```

## Deployment Architecture

### Container Strategy

```dockerfile
FROM openjdk:17-jdk-slim

# Add application user
RUN groupadd -r eureka && useradd -r -g eureka eureka

# Copy application
COPY target/service-registry.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

# Security
USER eureka

# Expose ports
EXPOSE 8761 8762

# Run application
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
  namespace: infrastructure
spec:
  replicas: 3
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
      - name: eureka-server
        image: exalt/service-registry:latest
        ports:
        - containerPort: 8761
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: EUREKA_INSTANCE_PREFER_IP_ADDRESS
          value: "true"
        - name: EUREKA_SERVER_ENABLE_SELF_PRESERVATION
          value: "false"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8761
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8761
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
  namespace: infrastructure
spec:
  selector:
    app: eureka-server
  ports:
  - name: http
    port: 8761
    targetPort: 8761
  type: ClusterIP
```

### Production Considerations

```yaml
# Production Configuration
eureka:
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 60000
    renewal-percent-threshold: 0.85
    renewal-threshold-update-interval-ms: 900000
    
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

## Performance Optimization

### Caching Strategy

```yaml
# Client-side caching
eureka:
  client:
    cache-refresh-task-time-interval-seconds: 30
    cache-refresh-task-initial-delay-seconds: 30
    registry-fetch-interval-seconds: 30
    disable-delta: false
    
# Server-side optimization
eureka:
  server:
    response-cache-auto-expiration-in-seconds: 180
    response-cache-update-interval-ms: 30000
    use-read-only-response-cache: true
```

### Monitoring & Metrics

```yaml
# Metrics Configuration
management:
  metrics:
    tags:
      service: eureka-server
      environment: ${spring.profiles.active}
    export:
      prometheus:
        enabled: true
        step: 30s
      
# Custom metrics
eureka:
  metrics:
    enabled: true
    export-interval: 30s
```

## Future Enhancements

1. **Service Mesh Integration**: Istio/Linkerd integration
2. **Advanced Load Balancing**: ML-based traffic routing
3. **Multi-Cloud Discovery**: Cross-cloud service discovery
4. **Security Enhancements**: mTLS, RBAC, service-to-service auth
5. **Performance Optimization**: Improved caching, batch operations
6. **Event Streaming**: Real-time service topology updates

## References

- [Netflix Eureka Documentation](https://github.com/Netflix/eureka)
- [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix)
- [Microservices Patterns](https://microservices.io/patterns/service-registry.html)
- [Service Discovery Best Practices](https://www.consul.io/docs/architecture)

---

*Last Updated: 2024-06-25*
*Document Version: 1.0*
*Review Schedule: Quarterly*
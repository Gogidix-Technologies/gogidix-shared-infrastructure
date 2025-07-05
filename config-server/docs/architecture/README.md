# Architecture Documentation - Config Server

## Overview

The Config Server is a centralized configuration management service built on Spring Cloud Config, designed to provide externalized configuration for all microservices in the Social E-commerce Ecosystem. It implements the configuration server pattern, enabling dynamic configuration management, environment-specific deployments, and secure configuration distribution across the entire platform.

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Component Architecture](#component-architecture)
3. [Configuration Management](#configuration-management)
4. [Storage Architecture](#storage-architecture)
5. [Security Architecture](#security-architecture)
6. [Client Integration](#client-integration)
7. [Performance Architecture](#performance-architecture)
8. [Deployment Architecture](#deployment-architecture)

## System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Config Server                             │
├─────────────────────┬─────────────────────┬───────────────────┤
│  Configuration API  │   Management Layer  │   Security Layer  │
│  - REST Endpoints   │   - Profile Manager │   - Authentication│
│  - GraphQL API     │   - Label Manager   │   - Authorization │
│  - gRPC Interface  │   - Backup Manager  │   - Encryption    │
├─────────────────────┼─────────────────────┼───────────────────┤
│  Storage Backends   │   Change Management │   Client Support  │
│  - Git Repository  │   - Version Control │   - Discovery     │
│  - Vault Store     │   - Audit Logging   │   - Refresh API   │
│  - Database Store  │   - Event Stream    │   - Circuit Break │
└─────────────────────┴─────────────────────┴───────────────────┘
```

### Service Interaction Flow

```
Client Services → Config Server → Storage Backend → Configuration Repository
     ↓                 ↓              ↓                    ↓
  Bootstrap        Retrieve       Read/Write           Git/Vault/DB
     ↓                 ↓              ↓                    ↓
  Application      Process        Cache Layer          Version Control
     ↓                 ↓              ↓                    ↓
  Runtime Ops      Respond        Security Check       Change Tracking
```

### Architecture Principles

1. **Centralization**: Single source of truth for all configuration
2. **Externalization**: Configuration separated from deployment artifacts
3. **Environment Promotion**: Consistent configuration across environments
4. **Security**: Encryption and access control for sensitive data
5. **Versioning**: Full audit trail and rollback capabilities
6. **Scalability**: Support for thousands of client services

## Component Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────────────┐
│                    Core Configuration Components                │
├─────────────────────┬─────────────────────┬───────────────────┤
│ ConfigController    │ EnvironmentRepo     │ PropertyLocator   │
│ - REST API          │ - Storage Access    │ - Source Location │
│ - Request Handling  │ - CRUD Operations   │ - Priority Order  │
│ - Response Format   │ - Query Interface   │ - Fallback Logic  │
├─────────────────────┼─────────────────────┼───────────────────┤
│ EncryptionService   │ ProfileManager      │ RefreshManager    │
│ - Encrypt/Decrypt   │ - Profile Logic     │ - Change Detection│
│ - Key Management    │ - Environment Map   │ - Event Publishing│
│ - Algorithm Config  │ - Default Handling  │ - Client Notify   │
└─────────────────────┴─────────────────────┴───────────────────┘
```

### Storage Layer Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Multi-Backend Storage                      │
├─────────────────────┬─────────────────────┬───────────────────┤
│ Git Repository      │ HashiCorp Vault     │ Database Store    │
│ - Version Control   │ - Secret Management │ - Relational DB   │
│ - Branch Strategy   │ - Dynamic Secrets   │ - ACID Properties │
│ - Merge Conflicts   │ - Lease Management  │ - Query Interface │
├─────────────────────┼─────────────────────┼───────────────────┤
│ Composite Backend   │ Cache Layer         │ File System       │
│ - Multiple Sources  │ - Redis/Hazelcast   │ - Local Files     │
│ - Priority Rules    │ - TTL Management    │ - Watch Service   │
│ - Failover Logic    │ - Invalidation      │ - Hot Reload      │
└─────────────────────┴─────────────────────┴───────────────────┘
```

### Configuration Resolution Flow

```
Client Request → Authentication → Authorization → Profile Resolution
      ↓               ↓               ↓                ↓
Property Lookup → Source Priority → Placeholder → Environment
      ↓               ↓               ↓           Resolution
Encryption Check → Decryption → Value Assembly → Response
      ↓               ↓               ↓           Formatting
Audit Logging → Metrics Update → Cache Update → Client Response
```

## Configuration Management

### Configuration Hierarchy

```
┌─────────────────────────────────────────────────────────────────┐
│                Configuration Resolution Order                   │
├─────────────────────┬─────────────────────┬───────────────────┤
│ 1. Command Line     │ 2. JVM Properties   │ 3. Environment    │
│ - Program Args      │ - System Properties │ - OS Variables    │
│ - Override All      │ - -D Parameters     │ - Docker/K8s      │
├─────────────────────┼─────────────────────┼───────────────────┤
│ 4. Config Server    │ 5. Application      │ 6. Default        │
│ - Remote Config     │ - Local Files       │ - Built-in        │
│ - Profile Specific  │ - Classpath         │ - Framework       │
└─────────────────────┴─────────────────────┴───────────────────┘
```

### Profile and Label Strategy

```yaml
# Configuration File Naming Convention
application.yml              # Default configuration for all services
application-{profile}.yml    # Profile-specific configuration
{application}.yml            # Application-specific configuration
{application}-{profile}.yml  # Application and profile specific

# Git Repository Structure
config-repo/
├── application.yml          # Global defaults
├── application-dev.yml      # Development environment
├── application-staging.yml  # Staging environment
├── application-prod.yml     # Production environment
├── api-gateway.yml          # API Gateway specific
├── api-gateway-dev.yml      # API Gateway dev config
├── user-service.yml         # User Service specific
├── user-service-prod.yml    # User Service prod config
└── shared/
    ├── database.yml         # Shared database config
    ├── security.yml         # Shared security config
    └── monitoring.yml       # Shared monitoring config
```

### Configuration Schema

```java
// Configuration Entity Model
public class ConfigEntity {
    private String application;     // Service name
    private String profile;         // Environment profile
    private String label;           // Version/branch label
    private String key;             // Property key
    private String value;           // Property value
    private boolean encrypted;      // Encryption flag
    private Instant createdAt;      // Creation timestamp
    private String createdBy;       // Creator user
    private Instant modifiedAt;     // Last modification
    private String modifiedBy;      // Last modifier
    private String version;         // Configuration version
    private Map<String, Object> metadata; // Additional metadata
}

// Environment Resolution
public class Environment {
    private String name;                          // Environment name
    private String[] profiles;                    // Active profiles
    private String label;                         // Version label
    private List<PropertySource> propertySources; // Property sources
    private String version;                       // Overall version
    private String state;                         // Environment state
}
```

### Configuration Validation

```java
@Configuration
public class ConfigValidationConfig {
    
    @Bean
    public ConfigValidator configValidator() {
        return ConfigValidator.builder()
            .addRule(new RequiredPropertyRule("spring.application.name"))
            .addRule(new DatabaseUrlFormatRule())
            .addRule(new EncryptedSecretRule())
            .addRule(new PortRangeRule(1024, 65535))
            .addRule(new ProfileDependencyRule())
            .enableSchemaValidation(true)
            .enableTypeValidation(true)
            .build();
    }
    
    @Bean
    public PropertyConstraintValidator propertyValidator() {
        return PropertyConstraintValidator.builder()
            .constraint("spring.datasource.url", 
                       Pattern.compile("jdbc:[a-zA-Z0-9]+://[a-zA-Z0-9.-]+:[0-9]+/[a-zA-Z0-9_]+"))
            .constraint("server.port", 
                       value -> Integer.parseInt(value) >= 1024 && Integer.parseInt(value) <= 65535)
            .constraint("logging.level.root", 
                       Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR"))
            .build();
    }
}
```

## Storage Architecture

### Git Repository Backend

```java
@Configuration
public class GitRepositoryConfig {
    
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.config.server.git.uri")
    public GitEnvironmentRepository gitEnvironmentRepository() {
        return GitEnvironmentRepository.builder()
            .uri(configProperties.getGit().getUri())
            .searchPaths(configProperties.getGit().getSearchPaths())
            .username(configProperties.getGit().getUsername())
            .password(configProperties.getGit().getPassword())
            .basedir(configProperties.getGit().getBasedir())
            .cloneOnStart(true)
            .deleteUntrackedBranches(true)
            .refreshRate(Duration.ofMinutes(5))
            .timeout(Duration.ofSeconds(30))
            .build();
    }
    
    @Bean
    public GitOperations gitOperations() {
        return GitOperations.builder()
            .enableAutoCommit(true)
            .commitMessage("Config update via Config Server")
            .authorName("Config Server")
            .authorEmail("config-server@gogidix.com")
            .enableMergeStrategy(MergeStrategy.RECURSIVE)
            .conflictResolution(ConflictResolution.MANUAL)
            .build();
    }
}
```

### Vault Integration

```java
@Configuration
@ConditionalOnProperty(name = "spring.cloud.config.server.vault.host")
public class VaultConfig {
    
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.create(vaultHost, vaultPort);
        endpoint.setScheme(vaultScheme);
        
        ClientAuthentication authentication = vaultAuthMethod.equals("token")
            ? new TokenAuthentication(vaultToken)
            : new AppRoleAuthentication(
                AppRoleAuthenticationOptions.builder()
                    .roleId(roleId)
                    .secretId(secretId)
                    .build());
        
        return new VaultTemplate(endpoint, authentication);
    }
    
    @Bean
    public VaultEnvironmentRepository vaultEnvironmentRepository(VaultTemplate vaultTemplate) {
        return VaultEnvironmentRepository.builder()
            .vaultTemplate(vaultTemplate)
            .backend(vaultBackend)
            .defaultKey("value")
            .profileSeparator("-")
            .kvVersion(VaultKvVersion.V2)
            .enableSecretVersioning(true)
            .secretTtl(Duration.ofHours(24))
            .build();
    }
}
```

### Database Backend

```java
@Configuration
@ConditionalOnProperty(name = "spring.cloud.config.server.database.enabled", havingValue = "true")
public class DatabaseRepositoryConfig {
    
    @Bean
    public DatabaseEnvironmentRepository databaseEnvironmentRepository(JdbcTemplate jdbcTemplate) {
        return DatabaseEnvironmentRepository.builder()
            .jdbcTemplate(jdbcTemplate)
            .tableName("config_properties")
            .applicationColumn("application")
            .profileColumn("profile")
            .labelColumn("label")
            .keyColumn("property_key")
            .valueColumn("property_value")
            .enableCaching(true)
            .cacheExpiry(Duration.ofMinutes(30))
            .build();
    }
    
    @Bean
    public ConfigDatabaseSchema configSchema() {
        return ConfigDatabaseSchema.builder()
            .createTable(CREATE_CONFIG_TABLE_SQL)
            .createIndex(CREATE_INDEX_SQL)
            .enableMigration(true)
            .versionControl(true)
            .build();
    }
}

// Database Schema
private static final String CREATE_CONFIG_TABLE_SQL = """
    CREATE TABLE IF NOT EXISTS config_properties (
        id BIGSERIAL PRIMARY KEY,
        application VARCHAR(255) NOT NULL,
        profile VARCHAR(255) NOT NULL,
        label VARCHAR(255) NOT NULL DEFAULT 'master',
        property_key VARCHAR(1000) NOT NULL,
        property_value TEXT,
        encrypted BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255),
        modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        modified_by VARCHAR(255),
        version VARCHAR(255),
        UNIQUE(application, profile, label, property_key)
    )
    """;
```

## Security Architecture

### Authentication and Authorization

```
┌─────────────────────────────────────────────────────────────────┐
│                      Security Layers                           │
├─────────────────────┬─────────────────────┬───────────────────┤
│ Transport Security  │ Authentication      │ Authorization     │
│ - TLS 1.3          │ - Basic Auth        │ - RBAC            │
│ - mTLS             │ - JWT Tokens        │ - Resource ACL    │
│ - Certificate Mgmt  │ - OAuth2/OIDC       │ - Method Security │
├─────────────────────┼─────────────────────┼───────────────────┤
│ Data Security       │ Audit & Logging     │ Network Security  │
│ - Encryption        │ - Access Logs       │ - IP Whitelisting │
│ - Key Management    │ - Change Tracking   │ - Rate Limiting   │
│ - Secret Handling   │ - Compliance        │ - DDoS Protection │
└─────────────────────┴─────────────────────┴───────────────────┘
```

### Configuration Encryption

```java
@Configuration
@EnableEncryption
public class EncryptionConfig {
    
    @Bean
    public TextEncryptor textEncryptor() {
        return new AesTextEncryptor(encryptionKey, salt);
    }
    
    @Bean
    @ConditionalOnProperty(name = "encrypt.keystore.location")
    public RsaTextEncryptor rsaTextEncryptor() {
        return RsaTextEncryptor.builder()
            .keyStoreLocation(encryptProperties.getKeyStore().getLocation())
            .keyStorePassword(encryptProperties.getKeyStore().getPassword())
            .keyStoreAlias(encryptProperties.getKeyStore().getAlias())
            .algorithm(encryptProperties.getKeyStore().getAlgorithm())
            .keyLength(2048)
            .build();
    }
    
    @Bean
    public EncryptionService encryptionService(TextEncryptor encryptor) {
        return EncryptionService.builder()
            .encryptor(encryptor)
            .prefix("{cipher}")
            .enableAutoDecryption(true)
            .enablePropertyEncryption(true)
            .supportedAlgorithms(Set.of("AES", "RSA"))
            .keyRotationInterval(Duration.ofDays(90))
            .build();
    }
}
```

### Access Control

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ConfigSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/{application}/{profile}").hasRole("CONFIG_READ")
                .requestMatchers(HttpMethod.POST, "/refresh").hasRole("CONFIG_REFRESH")
                .requestMatchers("/admin/**").hasRole("CONFIG_ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable())
            .build();
    }
    
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(new ConfigPermissionEvaluator());
        return handler;
    }
}
```

## Client Integration

### Spring Cloud Config Client

```java
// Bootstrap Configuration
@Configuration
public class ConfigClientBootstrap {
    
    @Bean
    public ConfigServicePropertySourceLocator configServicePropertySourceLocator(
            ConfigClientProperties properties,
            ConfigurableEnvironment environment) {
        
        return ConfigServicePropertySourceLocator.builder()
            .configClientProperties(properties)
            .environment(environment)
            .enableFailFast(true)
            .retryConfig(RetryConfig.builder()
                .maxAttempts(6)
                .initialInterval(Duration.ofSeconds(1))
                .maxInterval(Duration.ofSeconds(30))
                .multiplier(1.5)
                .build())
            .enableDiscovery(true)
            .enableHealthCheck(true)
            .build();
    }
}

// Configuration Properties
@ConfigurationProperties(prefix = "spring.cloud.config")
public class ConfigClientProperties {
    private String uri = "http://localhost:8888";
    private String name = "${spring.application.name}";
    private String profile = "default";
    private String label = "master";
    private boolean enabled = true;
    private boolean failFast = false;
    private String username;
    private String password;
    private String token;
    private boolean discovery = false;
    
    // Request configuration
    private int requestConnectTimeout = 3000;
    private int requestReadTimeout = 5000;
    
    // Retry configuration
    private RetryProperties retry = new RetryProperties();
    
    public static class RetryProperties {
        private boolean enabled = false;
        private int maxAttempts = 6;
        private long initialInterval = 1000;
        private double multiplier = 1.1;
        private long maxInterval = 2000;
    }
}
```

### Dynamic Refresh

```java
@Component
@RefreshScope
public class DynamicConfigService {
    
    @Value("${app.feature.enabled:false}")
    private boolean featureEnabled;
    
    @Value("${app.timeout:5000}")
    private int timeout;
    
    @EventListener(RefreshRemoteApplicationEvent.class)
    public void handleRefresh(RefreshRemoteApplicationEvent event) {
        log.info("Configuration refreshed for service: {}", event.getDestinationService());
        // Trigger any necessary reinitialization
        reinitializeComponents();
    }
    
    private void reinitializeComponents() {
        // Reinitialize components that depend on configuration
        if (featureEnabled) {
            enableFeature();
        } else {
            disableFeature();
        }
    }
}

@RestController
public class RefreshController {
    
    @Autowired
    private ContextRefresher contextRefresher;
    
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('CONFIG_REFRESH')")
    public Set<String> refresh() {
        return contextRefresher.refresh();
    }
}
```

## Performance Architecture

### Caching Strategy

```java
@Configuration
@EnableCaching
public class ConfigCacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration())
            .transactionAware()
            .build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();
    }
    
    @Bean
    @Primary
    public EnvironmentRepository cachedEnvironmentRepository(
            EnvironmentRepository delegate,
            CacheManager cacheManager) {
        
        return CachedEnvironmentRepository.builder()
            .delegate(delegate)
            .cacheManager(cacheManager)
            .cacheName("config-environment")
            .enableMetrics(true)
            .evictionPolicy(EvictionPolicy.LRU)
            .maxSize(10000)
            .build();
    }
}
```

### Performance Monitoring

```java
@Component
public class ConfigPerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Timer configRequestTimer;
    private final Counter configCacheHit;
    private final Counter configCacheMiss;
    
    public ConfigPerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.configRequestTimer = Timer.builder("config.request.duration")
            .description("Configuration request duration")
            .register(meterRegistry);
        this.configCacheHit = Counter.builder("config.cache.hits")
            .description("Configuration cache hits")
            .register(meterRegistry);
        this.configCacheMiss = Counter.builder("config.cache.misses")
            .description("Configuration cache misses")
            .register(meterRegistry);
    }
    
    @EventListener(ConfigRequestEvent.class)
    public void handleConfigRequest(ConfigRequestEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(configRequestTimer);
        
        if (event.isCacheHit()) {
            configCacheHit.increment();
        } else {
            configCacheMiss.increment();
        }
    }
}
```

## Deployment Architecture

### Container Configuration

```dockerfile
# Multi-stage Dockerfile for Config Server
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim
RUN addgroup --system config && adduser --system --group config

# Install git for git repository support
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/config-server.jar app.jar
RUN chown config:config app.jar

USER config
EXPOSE 8404

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8404/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: config-server
  namespace: config-system
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: config-server
  template:
    metadata:
      labels:
        app: config-server
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8404"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: config-server
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: config-server
        image: gogidix/config-server:1.0.0
        ports:
        - containerPort: 8404
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: SPRING_CLOUD_CONFIG_SERVER_GIT_URI
          valueFrom:
            secretKeyRef:
              name: config-git-secret
              key: uri
        - name: SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME
          valueFrom:
            secretKeyRef:
              name: config-git-secret
              key: username
        - name: SPRING_CLOUD_CONFIG_SERVER_GIT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: config-git-secret
              key: password
        - name: ENCRYPT_KEY
          valueFrom:
            secretKeyRef:
              name: config-encryption-secret
              key: key
        volumeMounts:
        - name: config
          mountPath: /etc/config
        - name: git-repos
          mountPath: /var/lib/config-repos
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8404
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8404
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
      volumes:
      - name: config
        configMap:
          name: config-server-config
      - name: git-repos
        emptyDir: {}

---
apiVersion: v1
kind: Service
metadata:
  name: config-server
  namespace: config-system
spec:
  selector:
    app: config-server
  ports:
  - name: http
    port: 8404
    targetPort: 8404
  type: ClusterIP
```

### High Availability Setup

```yaml
# Redis for caching and session storage
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: config-redis
  namespace: config-system
spec:
  serviceName: config-redis
  replicas: 3
  selector:
    matchLabels:
      app: config-redis
  template:
    spec:
      containers:
      - name: redis
        image: redis:7.0.11
        ports:
        - containerPort: 6379
        volumeMounts:
        - name: redis-data
          mountPath: /data
  volumeClaimTemplates:
  - metadata:
      name: redis-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 10Gi

---
# PostgreSQL for audit and metadata
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: config-postgres
  namespace: config-system
spec:
  serviceName: config-postgres
  replicas: 3
  selector:
    matchLabels:
      app: config-postgres
  template:
    spec:
      containers:
      - name: postgres
        image: postgres:15
        env:
        - name: POSTGRES_DB
          value: config_server
        - name: POSTGRES_USER
          value: config_user
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
        volumeMounts:
        - name: postgres-data
          mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:
  - metadata:
      name: postgres-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 50Gi
```

## Integration Patterns

### Service Discovery Integration

```java
@Configuration
@ConditionalOnProperty(name = "spring.cloud.config.discovery.enabled", havingValue = "true")
public class DiscoveryConfig {
    
    @Bean
    public DiscoveryClientConfigServiceBootstrapConfiguration discoveryConfig() {
        return new DiscoveryClientConfigServiceBootstrapConfiguration();
    }
    
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Event-Driven Configuration Updates

```java
@Component
public class ConfigChangeEventPublisher {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private MessageTemplate messageTemplate;
    
    @EventListener(ConfigurationUpdateEvent.class)
    public void publishConfigChange(ConfigurationUpdateEvent event) {
        // Publish to message queue for distributed systems
        ConfigChangeMessage message = ConfigChangeMessage.builder()
            .application(event.getApplication())
            .profile(event.getProfile())
            .label(event.getLabel())
            .changedProperties(event.getChangedProperties())
            .timestamp(event.getTimestamp())
            .build();
            
        messageTemplate.send("config.changes", message);
        
        // Publish local event
        eventPublisher.publishEvent(new LocalConfigChangeEvent(event));
    }
}
```

## Future Considerations

1. **Multi-Cloud Support**: Configuration federation across cloud providers
2. **AI-Driven Configuration**: Intelligent configuration optimization and recommendations
3. **Blockchain Audit**: Immutable configuration change audit trail
4. **Edge Configuration**: Configuration distribution to edge computing nodes
5. **GraphQL API**: Advanced querying capabilities for configuration data
6. **Configuration Templates**: Reusable configuration templates and inheritance

## References

- [Spring Cloud Config Documentation](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [HashiCorp Vault Integration](https://www.vaultproject.io/docs)
- [12-Factor App Configuration](https://12factor.net/config)
- [GitOps Configuration Management](https://www.weave.works/technologies/gitops/)

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Quarterly*
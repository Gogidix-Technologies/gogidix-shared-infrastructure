# Config Server Documentation

## Overview

The Config Server is a centralized configuration management service built on Java and Spring Cloud Config, designed to provide dynamic configuration management for all services in the Social E-commerce Ecosystem. It implements externalized configuration patterns, version control integration, dynamic refresh capabilities, and environment-specific configuration management.

## Components

### Core Components
- **ConfigServerApplication**: Main Spring Boot application with Spring Cloud Config Server
- **GitConfigRepository**: Git-based configuration repository management
- **EnvironmentController**: Configuration retrieval and management endpoints
- **PropertySourceLocator**: Custom property source location and resolution
- **ConfigClientSupport**: Client-side configuration retrieval and caching

### Configuration Sources
- **GitPropertySource**: Git repository-based configuration storage
- **VaultPropertySource**: HashiCorp Vault integration for secrets management
- **DatabasePropertySource**: Database-backed configuration storage
- **CompositePropertySource**: Multi-source configuration composition
- **EncryptedPropertySource**: Encrypted configuration values handling

### Client Integration
- **ConfigClientBootstrap**: Bootstrap configuration for client services
- **RefreshEndpoint**: Dynamic configuration refresh capabilities
- **ConfigurationProperties**: Type-safe configuration binding
- **ProfileResolver**: Environment and profile-based configuration resolution
- **ConfigRetryTemplate**: Resilient configuration retrieval with retry logic

### Security Components
- **ConfigSecurityConfig**: Security configuration for config endpoints
- **EncryptionService**: Configuration value encryption and decryption
- **AuthenticationProvider**: Client authentication and authorization
- **AuditEventPublisher**: Configuration access and change auditing
- **SecurityPropertySource**: Security-related configuration management

## Getting Started

To use the Config Server, follow these steps:

1. Set up the configuration repository (Git or other supported backends)
2. Configure the Config Server with repository settings
3. Set up client services for configuration retrieval
4. Configure environment-specific properties
5. Enable dynamic refresh capabilities

## Examples

### Configuring the Config Server

```java
import com.exalt.config.core.ConfigServerApplication;
import com.exalt.config.repository.GitConfigRepository;
import com.exalt.config.security.ConfigSecurityConfig;
import com.exalt.config.encryption.EncryptionService;

@SpringBootApplication
@EnableConfigServer
@EnableEncryption
public class ConfigServerApplication {
    
    @Bean
    public GitConfigRepository gitConfigRepository(ConfigServerProperties properties) {
        return GitConfigRepository.builder()
            .uri(properties.getGit().getUri())
            .searchPaths(properties.getGit().getSearchPaths())
            .username(properties.getGit().getUsername())
            .password(properties.getGit().getPassword())
            .cloneOnStart(true)
            .refreshRate(Duration.ofMinutes(5))
            .build();
    }
    
    @Bean
    public EncryptionService encryptionService(ConfigServerProperties properties) {
        return EncryptionService.builder()
            .keyStoreLocation(properties.getEncrypt().getKeyStore().getLocation())
            .keyStorePassword(properties.getEncrypt().getKeyStore().getPassword())
            .keyStoreAlias(properties.getEncrypt().getKeyStore().getAlias())
            .algorithm("AES")
            .build();
    }
    
    @Bean
    public ConfigSecurityConfig configSecurityConfig() {
        return ConfigSecurityConfig.builder()
            .enableBasicAuth(true)
            .enableJwtAuth(true)
            .enableMTLS(true)
            .auditEnabled(true)
            .build();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

### Configuration Repository Structure

```yaml
# config-repo/application.yml - Default configuration for all services
spring:
  application:
    name: default-config
  datasource:
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000

server:
  port: 8080
  compression:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    root: INFO
    com.exalt: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n"

---
# config-repo/api-gateway.yml - API Gateway specific configuration
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: user-service-cb
                fallbackUri: forward:/fallback/user-service
        
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/v1/products/**
          filters:
            - name: RateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200

server:
  port: 8080

---
# config-repo/user-service.yml - User Service specific configuration
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:user_service_db}
    username: ${DB_USER:user_service_user}
    password: ${DB_PASSWORD:secure_password}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: 8081

# Service-specific configuration
user:
  registration:
    email-verification-required: true
    auto-activation: false
  authentication:
    jwt:
      expiration: 86400
      refresh-expiration: 604800
  password:
    min-length: 8
    require-special-chars: true
    require-numbers: true
```

### Environment-Specific Configuration

```yaml
# config-repo/application-development.yml
spring:
  datasource:
    url: jdbc:h2:mem:devdb
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

logging:
  level:
    root: DEBUG
    com.exalt: DEBUG
    org.springframework: DEBUG

---
# config-repo/application-staging.yml
spring:
  datasource:
    url: jdbc:postgresql://staging-db:5432/staging_db
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: INFO
    com.exalt: DEBUG

---
# config-repo/application-production.yml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/prod_db
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: INFO
    com.exalt: WARN

security:
  require-ssl: true
  strict-transport-security: true
```

### Client Configuration

```java
import com.exalt.config.client.ConfigClientBootstrap;
import com.exalt.config.client.RefreshScope;

@SpringBootApplication
@EnableConfigurationProperties
@RefreshScope
public class ClientApplication {
    
    @Autowired
    private UserServiceProperties userProperties;
    
    @Bean
    @RefreshScope
    public ConfigClientBootstrap configClient() {
        return ConfigClientBootstrap.builder()
            .configServerUrl("http://config-server:8404")
            .applicationName("user-service")
            .profile("${spring.profiles.active:default}")
            .label("${config.label:main}")
            .failFast(true)
            .retryAttempts(3)
            .retryInterval(Duration.ofSeconds(10))
            .enableAutoRefresh(true)
            .refreshInterval(Duration.ofMinutes(5))
            .build();
    }
    
    @ConfigurationProperties(prefix = "user")
    @RefreshScope
    public static class UserServiceProperties {
        private Registration registration = new Registration();
        private Authentication authentication = new Authentication();
        private Password password = new Password();
        
        // Getters and setters
        public static class Registration {
            private boolean emailVerificationRequired = true;
            private boolean autoActivation = false;
            // Getters and setters
        }
        
        public static class Authentication {
            private Jwt jwt = new Jwt();
            
            public static class Jwt {
                private long expiration = 86400;
                private long refreshExpiration = 604800;
                // Getters and setters
            }
        }
        
        public static class Password {
            private int minLength = 8;
            private boolean requireSpecialChars = true;
            private boolean requireNumbers = true;
            // Getters and setters
        }
    }
    
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}
```

### Dynamic Configuration Refresh

```java
import com.exalt.config.refresh.ConfigRefreshService;
import com.exalt.config.client.ConfigurationUpdateEvent;

@Service
public class DynamicConfigurationService {
    private final ConfigRefreshService configRefreshService;
    private final ApplicationEventPublisher eventPublisher;
    
    public DynamicConfigurationService(ConfigRefreshService configRefreshService, 
                                     ApplicationEventPublisher eventPublisher) {
        this.configRefreshService = configRefreshService;
        this.eventPublisher = eventPublisher;
    }
    
    @EventListener(ConfigurationUpdateEvent.class)
    public void handleConfigurationUpdate(ConfigurationUpdateEvent event) {
        log.info("Configuration updated for service: {}, properties: {}", 
                event.getServiceName(), event.getUpdatedProperties());
        
        // Trigger refresh for specific properties
        if (event.getUpdatedProperties().contains("user.registration")) {
            eventPublisher.publishEvent(new UserRegistrationConfigChangedEvent(event));
        }
        
        if (event.getUpdatedProperties().contains("database")) {
            eventPublisher.publishEvent(new DatabaseConfigChangedEvent(event));
        }
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void refreshConfigurations() {
        try {
            Set<String> refreshedServices = configRefreshService.refreshAll();
            log.info("Refreshed configurations for services: {}", refreshedServices);
        } catch (Exception e) {
            log.error("Failed to refresh configurations", e);
        }
    }
    
    public void refreshServiceConfiguration(String serviceName) {
        try {
            boolean refreshed = configRefreshService.refresh(serviceName);
            if (refreshed) {
                log.info("Successfully refreshed configuration for service: {}", serviceName);
                eventPublisher.publishEvent(new ServiceConfigRefreshedEvent(serviceName));
            } else {
                log.warn("No configuration changes detected for service: {}", serviceName);
            }
        } catch (Exception e) {
            log.error("Failed to refresh configuration for service: {}", serviceName, e);
        }
    }
}
```

### Configuration Encryption

```java
import com.exalt.config.encryption.PropertyEncryption;
import com.exalt.config.encryption.ConfigEncryptionService;

@Service
public class ConfigurationEncryptionService {
    private final ConfigEncryptionService encryptionService;
    
    public ConfigurationEncryptionService(ConfigEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }
    
    @PropertyEncryption
    public String encryptProperty(String plainText) {
        return encryptionService.encrypt(plainText);
    }
    
    public String decryptProperty(String encryptedText) {
        return encryptionService.decrypt(encryptedText);
    }
    
    // Usage in configuration files:
    // spring:
    //   datasource:
    //     password: '{cipher}AQA3FxvjYQPR9+9Q9Q9Q9Q9Q9Q9Q9Q9Q'
    
    @PostConstruct
    public void initializeEncryption() {
        // Encrypt sensitive properties during startup
        Map<String, String> sensitiveProperties = Map.of(
            "spring.datasource.password", "actual_password",
            "spring.security.oauth2.client.client-secret", "oauth_secret",
            "app.external-api.api-key", "api_key_value"
        );
        
        sensitiveProperties.forEach((key, value) -> {
            String encrypted = encryptProperty(value);
            log.info("Encrypted property {}: {}", key, encrypted);
        });
    }
}
```

### Configuration Profiles Management

```java
import com.exalt.config.profile.ProfileResolver;
import com.exalt.config.profile.EnvironmentConfigProvider;

@Service
public class ConfigProfileService {
    private final ProfileResolver profileResolver;
    private final EnvironmentConfigProvider environmentProvider;
    
    public ConfigProfileService(ProfileResolver profileResolver, 
                               EnvironmentConfigProvider environmentProvider) {
        this.profileResolver = profileResolver;
        this.environmentProvider = environmentProvider;
    }
    
    public ConfigurationProfile resolveProfile(String serviceName, String environment) {
        return ConfigurationProfile.builder()
            .serviceName(serviceName)
            .environment(environment)
            .profiles(profileResolver.resolveProfiles(serviceName, environment))
            .properties(environmentProvider.getEnvironmentProperties(environment))
            .build();
    }
    
    public void activateProfile(String serviceName, String profile) {
        try {
            profileResolver.activateProfile(serviceName, profile);
            log.info("Activated profile {} for service {}", profile, serviceName);
            
            // Trigger configuration refresh
            configRefreshService.refresh(serviceName);
        } catch (Exception e) {
            log.error("Failed to activate profile {} for service {}", profile, serviceName, e);
            throw new ProfileActivationException("Failed to activate profile", e);
        }
    }
    
    public List<String> getActiveProfiles(String serviceName) {
        return profileResolver.getActiveProfiles(serviceName);
    }
    
    public void createEnvironmentProfile(String environment, Map<String, Object> properties) {
        EnvironmentProfile profile = EnvironmentProfile.builder()
            .name(environment)
            .properties(properties)
            .active(true)
            .createdAt(Instant.now())
            .build();
            
        environmentProvider.saveProfile(profile);
        log.info("Created environment profile: {}", environment);
    }
}
```

### Configuration Monitoring and Auditing

```java
import com.exalt.config.audit.ConfigAuditService;
import com.exalt.config.monitoring.ConfigMonitoringService;

@Service
public class ConfigAuditAndMonitoringService {
    private final ConfigAuditService auditService;
    private final ConfigMonitoringService monitoringService;
    
    public ConfigAuditAndMonitoringService(ConfigAuditService auditService, 
                                         ConfigMonitoringService monitoringService) {
        this.auditService = auditService;
        this.monitoringService = monitoringService;
    }
    
    @EventListener(ConfigurationAccessEvent.class)
    public void auditConfigurationAccess(ConfigurationAccessEvent event) {
        ConfigAuditEntry auditEntry = ConfigAuditEntry.builder()
            .serviceName(event.getServiceName())
            .operation(event.getOperation())
            .userId(event.getUserId())
            .clientIp(event.getClientIp())
            .timestamp(event.getTimestamp())
            .properties(event.getAccessedProperties())
            .success(event.isSuccess())
            .build();
            
        auditService.recordAuditEntry(auditEntry);
    }
    
    @EventListener(ConfigurationChangeEvent.class)
    public void auditConfigurationChange(ConfigurationChangeEvent event) {
        ConfigChangeAuditEntry changeEntry = ConfigChangeAuditEntry.builder()
            .serviceName(event.getServiceName())
            .propertyKey(event.getPropertyKey())
            .oldValue(event.getOldValue())
            .newValue(event.getNewValue())
            .changedBy(event.getChangedBy())
            .timestamp(event.getTimestamp())
            .changeReason(event.getChangeReason())
            .build();
            
        auditService.recordConfigurationChange(changeEntry);
        monitoringService.trackConfigurationChange(changeEntry);
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void collectConfigurationMetrics() {
        Map<String, Object> metrics = monitoringService.collectMetrics();
        
        // Record configuration server metrics
        metrics.put("config.requests.total", getConfigRequestCount());
        metrics.put("config.refresh.total", getConfigRefreshCount());
        metrics.put("config.errors.total", getConfigErrorCount());
        metrics.put("config.active.services", getActiveServiceCount());
        
        monitoringService.recordMetrics(metrics);
    }
    
    public ConfigurationUsageReport generateUsageReport(String serviceName, Duration period) {
        return ConfigurationUsageReport.builder()
            .serviceName(serviceName)
            .period(period)
            .totalRequests(auditService.getRequestCount(serviceName, period))
            .uniqueProperties(auditService.getUniquePropertiesAccessed(serviceName, period))
            .errorRate(auditService.getErrorRate(serviceName, period))
            .averageResponseTime(monitoringService.getAverageResponseTime(serviceName, period))
            .build();
    }
}
```

## API Reference

### Core Configuration API

#### ConfigServerController
- `ConfigServerController()`: Initialize config server controller
- `Environment getConfiguration(String application, String profile, String label)`: Get configuration for application
- `PropertySource getPropertySource(String application, String profile, String label, String path)`: Get specific property source
- `void refreshConfiguration(String application)`: Refresh configuration for application
- `ConfigStatus getConfigStatus(String application)`: Get configuration status
- `List<String> getApplications()`: Get all registered applications
- `List<String> getProfiles(String application)`: Get available profiles for application
- `Map<String, Object> getProperties(String application, String profile)`: Get all properties

#### EnvironmentRepository
- `Environment findOne(String application, String profile, String label)`: Find environment configuration
- `Environment findOne(String application, String profile, String label, boolean includeOrigin)`: Find with origin details
- `void save(String application, String profile, String label, Properties properties)`: Save configuration
- `void delete(String application, String profile, String label)`: Delete configuration
- `List<String> getApplicationNames()`: Get all application names
- `boolean exists(String application, String profile, String label)`: Check if configuration exists

#### PropertySourceLocator
- `PropertySource<?> locate(Environment environment)`: Locate property source
- `Collection<PropertySource<?>> locateCollection(Environment environment)`: Locate multiple sources
- `PropertySource<?> locate(String name, Environment environment)`: Locate named property source
- `int getOrder()`: Get locator order
- `boolean supports(Environment environment)`: Check if environment is supported

### Configuration Management API

#### ConfigurationManager
- `ConfigurationManager(EnvironmentRepository repository)`: Initialize with repository
- `void updateConfiguration(String application, String profile, Map<String, Object> properties)`: Update configuration
- `void deleteProperty(String application, String profile, String key)`: Delete specific property
- `void backupConfiguration(String application, String profile)`: Backup configuration
- `void restoreConfiguration(String application, String profile, String backupId)`: Restore from backup
- `ConfigurationHistory getHistory(String application, String profile)`: Get configuration history
- `void validateConfiguration(String application, String profile)`: Validate configuration

#### ProfileManager
- `ProfileManager()`: Default constructor
- `void createProfile(String application, String profile, String baseProfile)`: Create new profile
- `void deleteProfile(String application, String profile)`: Delete profile
- `void copyProfile(String application, String sourceProfile, String targetProfile)`: Copy profile
- `List<String> getProfiles(String application)`: Get application profiles
- `ProfileMetadata getProfileMetadata(String application, String profile)`: Get profile metadata
- `void setDefaultProfile(String application, String profile)`: Set default profile

#### EncryptionController
- `EncryptionController(TextEncryptor encryptor)`: Initialize with encryptor
- `String encrypt(String text)`: Encrypt text value
- `String decrypt(String encryptedText)`: Decrypt encrypted value
- `Map<String, String> encryptProperties(Map<String, String> properties)`: Encrypt property map
- `Map<String, String> decryptProperties(Map<String, String> encryptedProperties)`: Decrypt property map
- `EncryptionStatus getEncryptionStatus()`: Get encryption configuration status
- `void rotateEncryptionKey()`: Rotate encryption key

### Client Configuration API

#### ConfigServicePropertySourceLocator
- `ConfigServicePropertySourceLocator(ConfigClientProperties properties)`: Initialize with client properties
- `PropertySource<?> locate(Environment environment)`: Locate config server property source
- `void setRequestInterceptors(List<RequestInterceptor> interceptors)`: Set request interceptors
- `void setErrorHandler(ConfigServerErrorHandler errorHandler)`: Set error handler
- `PropertySource<?> getRemotePropertySource(String name, String profile, String label)`: Get remote source

#### RefreshEndpoint
- `RefreshEndpoint(ContextRefresher contextRefresher)`: Initialize with context refresher
- `Collection<String> refresh()`: Refresh all configuration
- `Collection<String> refresh(String application)`: Refresh specific application
- `boolean isRefreshEnabled()`: Check if refresh is enabled
- `void setRefreshEnabled(boolean enabled)`: Enable/disable refresh
- `RefreshStatus getRefreshStatus()`: Get current refresh status

#### ConfigWatchService
- `ConfigWatchService(ConfigClientProperties properties)`: Initialize config watcher
- `void startWatching()`: Start configuration watching
- `void stopWatching()`: Stop configuration watching
- `void addChangeListener(ConfigChangeListener listener)`: Add change listener
- `void removeChangeListener(ConfigChangeListener listener)`: Remove change listener
- `boolean isWatching()`: Check if currently watching
- `Duration getWatchInterval()`: Get watch interval

### Security and Audit API

#### ConfigSecurityManager
- `ConfigSecurityManager(ConfigSecurityProperties properties)`: Initialize with security properties
- `boolean authenticate(String username, String password)`: Authenticate user
- `boolean authorize(String user, String application, String operation)`: Authorize operation
- `void auditAccess(String user, String application, String operation, boolean success)`: Audit access
- `List<AccessLog> getAccessLogs(String application, Duration period)`: Get access logs
- `void revokeAccess(String user, String application)`: Revoke user access

#### EncryptionService
- `EncryptionService(TextEncryptor encryptor)`: Initialize with text encryptor
- `String encrypt(String plaintext)`: Encrypt plaintext
- `String decrypt(String ciphertext)`: Decrypt ciphertext
- `boolean canDecrypt(String ciphertext)`: Check if text can be decrypted
- `void setKeyStore(KeyStore keyStore)`: Set encryption key store
- `KeyStore getKeyStore()`: Get current key store

### Configuration Repository API

#### GitEnvironmentRepository
- `GitEnvironmentRepository(Environment environment)`: Initialize with environment
- `Environment findOne(String application, String profile, String label)`: Find configuration in Git
- `void refresh()`: Refresh Git repository
- `void setUri(String uri)`: Set Git repository URI
- `void setSearchPaths(String[] searchPaths)`: Set search paths
- `void setUsername(String username)`: Set Git username
- `void setPassword(String password)`: Set Git password
- `boolean isCloneOnStart()`: Check if clone on start is enabled

#### VaultEnvironmentRepository
- `VaultEnvironmentRepository(VaultTemplate vaultTemplate)`: Initialize with Vault template
- `Environment findOne(String application, String profile, String label)`: Find configuration in Vault
- `void setBackend(String backend)`: Set Vault backend
- `void setDefaultKey(String defaultKey)`: Set default key
- `void setProfileSeparator(String profileSeparator)`: Set profile separator
- `Map<String, String> findProperties(String path)`: Find properties at path

## Best Practices

1. **Repository Organization**: Structure configuration files hierarchically by application and environment
2. **Security**: Always encrypt sensitive configuration values
3. **Versioning**: Use Git labels for configuration versioning and rollback capabilities
4. **Caching**: Implement client-side caching for improved performance
5. **Monitoring**: Set up comprehensive monitoring and alerting for configuration changes
6. **Validation**: Validate configuration changes before applying them
7. **Backup**: Regularly backup configuration data

## Related Documentation

- [API Specification](../api-docs/openapi.yaml)
- [Architecture Documentation](./architecture/README.md)
- [Setup Guide](./setup/README.md)
- [Operations Guide](./operations/README.md)
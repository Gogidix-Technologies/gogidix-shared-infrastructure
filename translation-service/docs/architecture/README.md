# Translation Service - Architecture Documentation

## Table of Contents

- [Overview](#overview)
- [Internationalization Design](#internationalization-design)
- [Multi-Language Support](#multi-language-support)
- [Translation Providers](#translation-providers)
- [Caching Strategies](#caching-strategies)
- [Data Architecture](#data-architecture)
- [Security Architecture](#security-architecture)
- [Integration Patterns](#integration-patterns)
- [Performance Considerations](#performance-considerations)
- [Scalability Design](#scalability-design)

## Overview

The Translation Service is a foundational microservice within the Exalt Social E-commerce Ecosystem's shared infrastructure domain. It provides enterprise-grade translation capabilities supporting the platform's international expansion across multiple markets.

### Core Principles

- **Provider Agnostic**: Support for multiple translation providers with failover capabilities
- **High Performance**: Advanced caching strategies for optimal response times
- **Quality Focused**: Translation quality management and human review workflows
- **Scalable**: Horizontally scalable design for high-volume translation workloads
- **Cost Optimized**: Intelligent provider selection based on cost and quality metrics

### Service Characteristics

- **Language**: Java 17
- **Framework**: Spring Boot 3.1.5
- **Architecture Style**: Microservice
- **Communication**: REST API + WebSocket for real-time updates
- **Data Storage**: PostgreSQL with Redis caching
- **Message Queuing**: Apache Kafka for asynchronous processing

## Internationalization Design

### Supported Languages

The service currently supports the following languages based on market priorities:

| Language | Code | Market Priority | Status |
|----------|------|----------------|---------|
| English | en | Primary | Active |
| Spanish | es | High | Active |
| French | fr | High | Active |
| German | de | High | Active |
| Arabic | ar | Medium | Active |

### Locale Management

```yaml
Locale Structure:
  language_code: ISO 639-1 (2-letter)
  country_code: ISO 3166-1 alpha-2 (2-letter)
  format: {language}_{country} (e.g., en_US, fr_CA)
```

### Text Processing Pipeline

```
Input Text → Language Detection → Content Analysis → Provider Selection → Translation → Quality Check → Caching → Response
```

#### Language Detection

- **Primary**: Optimaize Language Detector
- **Confidence Threshold**: 85%
- **Fallback**: User-specified source language
- **Context Aware**: Leverages domain-specific dictionaries

#### Content Analysis

- **Text Classification**: Technical, marketing, legal, general
- **Complexity Scoring**: Simple, moderate, complex
- **Domain Detection**: E-commerce, legal, technical, marketing
- **Format Preservation**: HTML, Markdown, plain text handling

## Multi-Language Support

### Translation Strategies

#### 1. Static Content Translation

```java
@Component
public class StaticContentTranslator {
    
    @Cacheable("static-translations")
    public TranslationResult translateStaticContent(
            String content, 
            Language source, 
            Language target) {
        // Implementation for UI labels, error messages, etc.
    }
}
```

#### 2. Dynamic Content Translation

```java
@Component
public class DynamicContentTranslator {
    
    @Async
    public CompletableFuture<TranslationResult> translateDynamicContent(
            String content, 
            Language source, 
            Language target,
            TranslationContext context) {
        // Implementation for product descriptions, reviews, etc.
    }
}
```

#### 3. Batch Translation Processing

```java
@Component
public class BatchTranslationProcessor {
    
    @KafkaListener(topics = "translation.batch.requests")
    public void processBatchTranslation(BatchTranslationRequest request) {
        // Implementation for large-scale translation jobs
    }
}
```

### Content Categorization

#### E-commerce Specific Categories

- **Product Catalog**: Names, descriptions, specifications
- **User Generated**: Reviews, comments, Q&A
- **Marketing**: Campaigns, promotions, banners
- **Legal**: Terms, policies, disclaimers
- **Support**: Help content, FAQs, guides

#### Translation Quality Levels

| Level | Use Case | Provider | Human Review |
|-------|----------|----------|--------------|
| High | Legal documents | Premium AI + Human | Required |
| Medium | Product descriptions | Premium AI | Optional |
| Standard | User reviews | Standard AI | Automated |
| Basic | System messages | Basic AI | None |

## Translation Providers

### Provider Architecture

```java
public interface TranslationProvider {
    
    TranslationResult translate(TranslationRequest request);
    
    ProviderCapabilities getCapabilities();
    
    ProviderCostModel getCostModel();
    
    QualityMetrics getQualityMetrics();
}
```

### Supported Providers

#### 1. Google Cloud Translation API

```yaml
Provider: Google Translate
Features:
  - Neural Machine Translation (NMT)
  - 100+ language pairs
  - Automatic language detection
  - Custom models support
  - Glossary management
Pricing: Pay-per-character
Quality: High
Latency: Low
```

#### 2. AWS Translate

```yaml
Provider: AWS Translate
Features:
  - Neural Machine Translation
  - Custom terminology
  - Batch translation jobs
  - Real-time translation
  - Active custom translation
Pricing: Pay-per-character
Quality: High
Latency: Medium
```

#### 3. Microsoft Translator

```yaml
Provider: Microsoft Translator
Features:
  - Neural Machine Translation
  - Custom translator
  - Document translation
  - Conversation translation
  - Dictionary lookup
Pricing: Tiered pricing model
Quality: High
Latency: Medium
```

#### 4. DeepL API

```yaml
Provider: DeepL
Features:
  - High-quality neural translation
  - Formality control
  - Document translation
  - Glossary support
  - Translation alternatives
Pricing: Subscription-based
Quality: Very High
Latency: Medium
```

### Provider Selection Strategy

```java
@Component
public class ProviderSelectionStrategy {
    
    public TranslationProvider selectProvider(TranslationRequest request) {
        return ProviderSelector.builder()
            .withQualityRequirement(request.getQualityLevel())
            .withCostConstraint(request.getBudget())
            .withLanguagePair(request.getSourceLang(), request.getTargetLang())
            .withContentType(request.getContentType())
            .withLatencyRequirement(request.getMaxLatency())
            .select();
    }
}
```

### Failover and Circuit Breaker

```java
@Component
public class TranslationOrchestrator {
    
    @CircuitBreaker(name = "translation-service")
    @Retry(name = "translation-service")
    @TimeLimiter(name = "translation-service")
    public TranslationResult translateWithFailover(TranslationRequest request) {
        // Primary provider attempt
        // Fallback to secondary provider
        // Final fallback to cached/default translation
    }
}
```

## Caching Strategies

### Multi-Level Caching Architecture

```
L1 Cache (Application) → L2 Cache (Redis) → L3 Cache (Database) → Translation Provider
```

#### L1 Cache - Application Level

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        return CacheManager.builder()
            .withCache("translation-results", 
                CachePolicy.builder()
                    .maxSize(10000)
                    .expireAfterWrite(Duration.ofHours(1))
                    .build())
            .withCache("language-detection",
                CachePolicy.builder()
                    .maxSize(5000)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .build())
            .build();
    }
}
```

#### L2 Cache - Redis Distributed Cache

```yaml
Cache Strategy:
  Key Pattern: "translation:{source_lang}:{target_lang}:{content_hash}"
  TTL Strategy:
    - Static content: 30 days
    - Dynamic content: 7 days
    - User-generated: 1 day
  Eviction Policy: LRU
  Memory Limit: 2GB per instance
```

#### Cache Invalidation Policies

```java
@Component
public class CacheInvalidationManager {
    
    @EventListener
    public void handleTranslationUpdate(TranslationUpdatedEvent event) {
        // Invalidate specific cache entries
        cacheManager.evict("translation-results", 
            generateCacheKey(event.getSourceLang(), 
                           event.getTargetLang(), 
                           event.getContentHash()));
    }
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredEntries() {
        // Proactive cache cleanup
    }
}
```

### Cache Warming Strategies

```java
@Component
public class CacheWarmupService {
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCriticalTranslations() {
        // Preload common UI elements
        // Preload frequently accessed product categories
        // Preload legal text translations
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void scheduledCacheWarmup() {
        // Refresh expiring cache entries
        // Preload trending content translations
    }
}
```

## Data Architecture

### Database Schema Design

#### Core Tables

```sql
-- Translation Projects
CREATE TABLE translation_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    source_language VARCHAR(5) NOT NULL,
    target_languages TEXT[] NOT NULL,
    project_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Translation Requests
CREATE TABLE translation_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID REFERENCES translation_projects(id),
    source_content TEXT NOT NULL,
    source_language VARCHAR(5) NOT NULL,
    target_language VARCHAR(5) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    quality_level VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_used VARCHAR(50),
    cost_amount DECIMAL(10,4),
    processing_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Translation Results
CREATE TABLE translation_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES translation_requests(id),
    translated_content TEXT NOT NULL,
    confidence_score DECIMAL(5,4),
    quality_score DECIMAL(5,4),
    human_reviewed BOOLEAN DEFAULT FALSE,
    reviewer_id UUID,
    review_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Custom Dictionaries
CREATE TABLE custom_dictionaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domain VARCHAR(100) NOT NULL,
    source_language VARCHAR(5) NOT NULL,
    target_language VARCHAR(5) NOT NULL,
    source_term VARCHAR(255) NOT NULL,
    target_term VARCHAR(255) NOT NULL,
    context TEXT,
    confidence_level VARCHAR(20) DEFAULT 'HIGH',
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Translation Cache
CREATE TABLE translation_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_hash VARCHAR(64) NOT NULL,
    source_language VARCHAR(5) NOT NULL,
    target_language VARCHAR(5) NOT NULL,
    source_content TEXT NOT NULL,
    translated_content TEXT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    quality_score DECIMAL(5,4),
    access_count INTEGER DEFAULT 1,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_translation_cache_hash_lang ON translation_cache(content_hash, source_language, target_language);
CREATE INDEX idx_translation_requests_project_status ON translation_requests(project_id, status);
CREATE INDEX idx_custom_dictionaries_domain_lang ON custom_dictionaries(domain, source_language, target_language);
```

#### Data Partitioning Strategy

```sql
-- Partition translation_requests by month
CREATE TABLE translation_requests_y2024m01 PARTITION OF translation_requests
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- Partition translation_cache by language pair
CREATE TABLE translation_cache_en_es PARTITION OF translation_cache
    FOR VALUES IN (('en', 'es'), ('es', 'en'));
```

### Document Storage for Large Content

```java
@Entity
@Table(name = "document_translations")
public class DocumentTranslation {
    
    @Id
    private UUID id;
    
    @Column(name = "document_path")
    private String documentPath; // S3/MinIO path
    
    @Column(name = "original_format")
    private String originalFormat; // PDF, DOCX, HTML
    
    @Column(name = "translation_path")
    private String translationPath;
    
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;
    
    // Additional fields...
}
```

## Security Architecture

### Authentication and Authorization

#### API Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/translate/public").hasRole("PUBLIC_API")
                .requestMatchers("/api/v1/translate/**").hasRole("TRANSLATOR")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .build();
    }
}
```

#### Role-Based Access Control

```yaml
Roles:
  PUBLIC_API:
    - Basic translation requests
    - Language detection
    - Public content translation
  
  TRANSLATOR:
    - Full translation API access
    - Custom dictionary management
    - Translation history access
  
  ADMIN:
    - Provider configuration
    - System monitoring
    - User management
    - Cost analytics
  
  REVIEWER:
    - Translation quality review
    - Human approval workflows
    - Quality scoring
```

### Data Security

#### Encryption at Rest

```yaml
Database Encryption:
  - Transparent Data Encryption (TDE)
  - Encrypted tablespaces for sensitive content
  - Key rotation every 90 days

Cache Encryption:
  - Redis encryption in transit and at rest
  - Encrypted connection strings
  - Regular key rotation
```

#### Encryption in Transit

```yaml
TLS Configuration:
  - TLS 1.3 for all external communications
  - mTLS for service-to-service communication
  - Certificate-based authentication
  - Regular certificate rotation
```

### Privacy and Compliance

#### Data Retention Policies

```java
@Component
public class DataRetentionManager {
    
    @Scheduled(cron = "0 0 1 * * ?") // Monthly cleanup
    public void cleanupExpiredTranslations() {
        // Remove translations older than retention period
        // Anonymize sensitive user data
        // Archive historical data for compliance
    }
}
```

#### GDPR Compliance

```java
@RestController
@RequestMapping("/api/v1/gdpr")
public class GdprController {
    
    @DeleteMapping("/user/{userId}/translations")
    public ResponseEntity<Void> deleteUserTranslations(@PathVariable UUID userId) {
        // Implementation for right to erasure
    }
    
    @GetMapping("/user/{userId}/translations")
    public ResponseEntity<UserTranslationData> exportUserTranslations(@PathVariable UUID userId) {
        // Implementation for data portability
    }
}
```

## Integration Patterns

### Synchronous Integration

#### REST API Design

```java
@RestController
@RequestMapping("/api/v1/translate")
@Validated
public class TranslationController {
    
    @PostMapping("/text")
    public ResponseEntity<TranslationResponse> translateText(
            @Valid @RequestBody TranslationRequest request) {
        // Synchronous translation for real-time needs
    }
    
    @PostMapping("/batch")
    public ResponseEntity<BatchTranslationResponse> translateBatch(
            @Valid @RequestBody BatchTranslationRequest request) {
        // Asynchronous batch processing
    }
}
```

### Asynchronous Integration

#### Event-Driven Architecture

```java
@Component
public class TranslationEventPublisher {
    
    @EventListener
    public void handleTranslationCompleted(TranslationCompletedEvent event) {
        // Publish to Kafka for downstream services
        kafkaTemplate.send("translation.completed", event);
    }
    
    @EventListener
    public void handleTranslationFailed(TranslationFailedEvent event) {
        // Handle failures and retry logic
        kafkaTemplate.send("translation.failed", event);
    }
}
```

#### WebSocket for Real-Time Updates

```java
@Controller
public class TranslationWebSocketController {
    
    @MessageMapping("/translate")
    @SendTo("/topic/translations/{userId}")
    public TranslationUpdate handleTranslationRequest(TranslationRequest request) {
        // Real-time translation updates
    }
}
```

### Circuit Breaker Pattern

```java
@Component
public class ResilientTranslationService {
    
    @CircuitBreaker(name = "google-translate", fallbackMethod = "fallbackTranslation")
    @Retry(name = "google-translate")
    @TimeLimiter(name = "google-translate")
    public CompletableFuture<TranslationResult> translateWithGoogleProvider(TranslationRequest request) {
        // Primary translation logic
    }
    
    public CompletableFuture<TranslationResult> fallbackTranslation(TranslationRequest request, Exception ex) {
        // Fallback to alternative provider or cached translation
    }
}
```

## Performance Considerations

### Response Time Optimization

#### Performance Targets

```yaml
Performance SLAs:
  Cached Translations: < 100ms (p95)
  Simple Text Translation: < 2s (p95)
  Complex Document Translation: < 30s (p95)
  Batch Processing: < 5 minutes per 1000 items
```

#### Connection Pooling

```java
@Configuration
public class HttpClientConfiguration {
    
    @Bean
    public WebClient googleTranslateClient() {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .connectionProvider(ConnectionProvider
                        .builder("google-translate")
                        .maxConnections(100)
                        .maxIdleTime(Duration.ofSeconds(30))
                        .build())))
            .build();
    }
}
```

### Memory Management

#### JVM Tuning

```bash
# Production JVM settings
-Xms2g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
```

#### Memory Monitoring

```java
@Component
public class MemoryMonitor {
    
    @Scheduled(fixedRate = 60000)
    public void monitorMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        double usagePercentage = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        
        if (usagePercentage > 80) {
            // Trigger cache cleanup or scale-out
        }
    }
}
```

## Scalability Design

### Horizontal Scaling Strategy

#### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Database Scaling

#### Read Replicas

```java
@Configuration
public class DatabaseConfiguration {
    
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        // Master database for writes
    }
    
    @Bean
    public DataSource readOnlyDataSource() {
        // Read replica for translations cache queries
    }
}
```

#### Sharding Strategy

```sql
-- Shard by language pair
CREATE TABLE translation_cache_shard_1 (
    LIKE translation_cache INCLUDING ALL
) INHERITS (translation_cache);

-- Check constraint for shard distribution
ALTER TABLE translation_cache_shard_1 
ADD CONSTRAINT translation_cache_shard_1_check 
CHECK (hashtext(source_language || target_language) % 4 = 0);
```

### Caching Scalability

#### Redis Cluster Configuration

```yaml
Redis Cluster:
  Nodes: 6 (3 masters, 3 replicas)
  Memory per node: 4GB
  Eviction policy: allkeys-lru
  Persistence: RDB + AOF
  Clustering: Hash slots distribution
```

This architecture documentation provides a comprehensive foundation for understanding and implementing the Translation Service within the Exalt Social E-commerce Ecosystem. The design emphasizes scalability, performance, and reliability while maintaining flexibility for future enhancements and integrations.
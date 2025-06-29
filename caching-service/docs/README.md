# Caching Service Documentation

## Overview

The Caching Service is a high-performance Java-based microservice that provides distributed caching capabilities for the Social E-commerce Ecosystem. It implements advanced caching strategies, cache warming, cache invalidation patterns, and provides cache analytics to improve overall system performance and reduce database load.

## Components

### Core Components
- **CacheManager**: Central cache management and coordination
- **RedisClusterManager**: Redis cluster management and failover
- **CacheKeyGenerator**: Intelligent cache key generation and namespacing
- **CacheEvictionPolicy**: Cache eviction strategies and policies
- **CacheMetricsCollector**: Cache performance metrics and analytics

### Caching Strategies
- **CacheAsideStrategy**: Cache-aside pattern implementation
- **WriteThroughStrategy**: Write-through cache strategy
- **WriteBehindStrategy**: Write-behind cache pattern
- **RefreshAheadStrategy**: Proactive cache refresh mechanism
- **CacheWarmingService**: Intelligent cache warming and pre-loading

### Data Management
- **CacheSerializer**: Object serialization for cache storage
- **CachePartitioner**: Data partitioning across cache nodes
- **CacheReplicationManager**: Cross-region cache replication
- **CacheConsistencyManager**: Cache consistency and synchronization
- **CacheVersionManager**: Cache versioning and migration

### Integration Components
- **SpringCacheProvider**: Spring Cache abstraction integration
- **HibernateCacheProvider**: Hibernate L2 cache integration
- **WebCacheManager**: HTTP cache headers and CDN integration
- **SessionCacheManager**: Distributed session storage
- **ConfigCacheManager**: Application configuration caching

## Getting Started

To use the Caching Service, follow these steps:

1. Configure the caching service with Redis cluster settings
2. Set up cache regions and TTL policies
3. Configure cache warming strategies
4. Initialize cache metrics and monitoring
5. Integrate with application services

## Examples

### Configuring the Caching Service

```java
import com.exalt.caching.core.CacheManager;
import com.exalt.caching.core.CacheConfiguration;
import com.exalt.caching.strategy.CacheAsideStrategy;
import com.exalt.caching.redis.RedisClusterManager;

@Configuration
@EnableCaching
public class CachingServiceConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        CacheConfiguration config = CacheConfiguration.builder()
            .redisCluster(redisClusterConfig())
            .defaultTtl(Duration.ofHours(1))
            .maxMemoryPolicy(MaxMemoryPolicy.ALLKEYS_LRU)
            .enableCompression(true)
            .enableEncryption(true)
            .meticsEnabled(true)
            .build();
            
        return new CacheManager(config);
    }
    
    @Bean
    public RedisClusterConfig redisClusterConfig() {
        return RedisClusterConfig.builder()
            .nodes(Arrays.asList(
                "redis-node-1:6379",
                "redis-node-2:6379",
                "redis-node-3:6379"
            ))
            .password(cacheProperties.getRedisPassword())
            .connectionTimeout(Duration.ofSeconds(2))
            .socketTimeout(Duration.ofSeconds(5))
            .maxAttempts(3)
            .enableSsl(true)
            .build();
    }
    
    @Bean
    public CacheKeyGenerator cacheKeyGenerator() {
        return CacheKeyGenerator.builder()
            .namespace("exalt:cache")
            .version("v1")
            .includeVersion(true)
            .hashLongKeys(true)
            .build();
    }
}
```

### Cache-Aside Pattern Implementation

```java
import com.exalt.caching.annotation.Cacheable;
import com.exalt.caching.annotation.CacheEvict;
import com.exalt.caching.annotation.CachePut;

@Service
public class ProductCacheService {
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;
    
    public ProductCacheService(ProductRepository productRepository, CacheManager cacheManager) {
        this.productRepository = productRepository;
        this.cacheManager = cacheManager;
    }
    
    @Cacheable(
        region = "products", 
        key = "#productId",
        ttl = "1h",
        condition = "#productId != null"
    )
    public Product getProduct(UUID productId) {
        log.debug("Loading product from database: {}", productId);
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    @CachePut(
        region = "products",
        key = "#product.id",
        ttl = "1h"
    )
    public Product updateProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        
        // Invalidate related caches
        cacheManager.evictByPattern("products:category:" + product.getCategoryId() + ":*");
        cacheManager.evictByPattern("search:*");
        
        return savedProduct;
    }
    
    @CacheEvict(
        region = "products",
        key = "#productId",
        beforeInvocation = false
    )
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
            
        productRepository.delete(product);
        
        // Invalidate related caches
        cacheManager.evictByPattern("products:category:" + product.getCategoryId() + ":*");
        cacheManager.evictByPattern("search:*");
    }
    
    @Cacheable(
        region = "product-lists",
        key = "'category:' + #categoryId + ':page:' + #page + ':size:' + #size",
        ttl = "30m"
    )
    public Page<Product> getProductsByCategory(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryId(categoryId, pageable);
    }
}
```

### Advanced Caching Patterns

```java
import com.exalt.caching.strategy.RefreshAheadStrategy;
import com.exalt.caching.warming.CacheWarmingService;

@Service
public class AdvancedCachingService {
    private final CacheManager cacheManager;
    private final RefreshAheadStrategy refreshAheadStrategy;
    private final CacheWarmingService cacheWarmingService;
    
    // Refresh-ahead pattern for critical data
    @Cacheable(
        region = "critical-data",
        key = "#dataId",
        ttl = "2h",
        refreshAhead = true,
        refreshThreshold = 0.8 // Refresh when 80% of TTL elapsed
    )
    public CriticalData getCriticalData(String dataId) {
        return loadCriticalDataFromSource(dataId);
    }
    
    // Write-through pattern
    @CachePut(
        region = "user-preferences",
        key = "#userId",
        writeThrough = true,
        ttl = "24h"
    )
    public UserPreferences updateUserPreferences(UUID userId, UserPreferences preferences) {
        return userPreferencesRepository.save(preferences);
    }
    
    // Cache warming on application startup
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCaches() {
        cacheWarmingService.warmup(CacheWarmupConfig.builder()
            .region("products")
            .strategy(WarmupStrategy.POPULAR_ITEMS)
            .dataSource(() -> productRepository.findPopularProducts(1000))
            .concurrency(10)
            .build());
            
        cacheWarmingService.warmup(CacheWarmupConfig.builder()
            .region("categories")
            .strategy(WarmupStrategy.ALL_ITEMS)
            .dataSource(() -> categoryRepository.findAll())
            .build());
    }
    
    // Distributed cache invalidation
    @CacheEvict(region = "products", allEntries = true)
    @EventListener(ProductCatalogUpdatedEvent.class)
    public void onProductCatalogUpdated(ProductCatalogUpdatedEvent event) {
        log.info("Product catalog updated, invalidating product caches");
        
        // Publish cache invalidation event to other nodes
        cacheManager.publishInvalidationEvent(
            CacheInvalidationEvent.builder()
                .region("products")
                .pattern("*")
                .reason("catalog_updated")
                .timestamp(Instant.now())
                .build()
        );
    }
}
```

### Cache Metrics and Monitoring

```java
import com.exalt.caching.metrics.CacheMetricsCollector;
import com.exalt.caching.analytics.CacheAnalytics;

@Service
public class CacheMonitoringService {
    private final CacheMetricsCollector metricsCollector;
    private final CacheAnalytics cacheAnalytics;
    
    public CacheMonitoringService(CacheMetricsCollector metricsCollector, CacheAnalytics cacheAnalytics) {
        this.metricsCollector = metricsCollector;
        this.cacheAnalytics = cacheAnalytics;
    }
    
    public CacheStatistics getCacheStatistics(String region) {
        return metricsCollector.getStatistics(region);
    }
    
    public CachePerformanceReport getPerformanceReport(String region, Duration period) {
        return CachePerformanceReport.builder()
            .region(region)
            .period(period)
            .hitRate(metricsCollector.getHitRate(region, period))
            .missRate(metricsCollector.getMissRate(region, period))
            .averageLoadTime(metricsCollector.getAverageLoadTime(region, period))
            .evictionCount(metricsCollector.getEvictionCount(region, period))
            .memoryUsage(metricsCollector.getMemoryUsage(region))
            .topKeys(cacheAnalytics.getTopAccessedKeys(region, 100))
            .build();
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void collectMetrics() {
        cacheManager.getAllRegions().forEach(region -> {
            CacheStatistics stats = getCacheStatistics(region);
            
            // Record metrics for monitoring
            Metrics.gauge("cache.hit.rate", Tags.of("region", region), stats.getHitRate());
            Metrics.gauge("cache.miss.rate", Tags.of("region", region), stats.getMissRate());
            Metrics.gauge("cache.memory.usage", Tags.of("region", region), stats.getMemoryUsage());
            Metrics.gauge("cache.entry.count", Tags.of("region", region), stats.getEntryCount());
            
            // Check for performance issues
            if (stats.getHitRate() < 0.8) {
                log.warn("Low cache hit rate in region {}: {}", region, stats.getHitRate());
                alertService.sendAlert(AlertLevel.WARNING, 
                    "Low cache hit rate in region " + region);
            }
            
            if (stats.getMemoryUsage() > 0.9) {
                log.warn("High memory usage in cache region {}: {}", region, stats.getMemoryUsage());
                alertService.sendAlert(AlertLevel.CRITICAL, 
                    "High memory usage in cache region " + region);
            }
        });
    }
}
```

### Multi-Level Caching

```java
import com.exalt.caching.multilevel.MultiLevelCache;
import com.exalt.caching.local.L1Cache;
import com.exalt.caching.distributed.L2Cache;

@Service
public class MultiLevelCachingService {
    private final MultiLevelCache multiLevelCache;
    
    public MultiLevelCachingService() {
        this.multiLevelCache = MultiLevelCache.builder()
            .l1Cache(L1Cache.builder()
                .implementation(L1CacheType.CAFFEINE)
                .maxSize(10000)
                .ttl(Duration.ofMinutes(5))
                .recordStats(true)
                .build())
            .l2Cache(L2Cache.builder()
                .implementation(L2CacheType.REDIS_CLUSTER)
                .ttl(Duration.ofHours(1))
                .compression(CompressionType.GZIP)
                .serialization(SerializationType.JSON)
                .build())
            .build();
    }
    
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        return multiLevelCache.get(key, type, loader);
    }
    
    public void put(String key, Object value, Duration ttl) {
        multiLevelCache.put(key, value, ttl);
    }
    
    public void evict(String key) {
        multiLevelCache.evict(key);
    }
    
    // Example usage with search results
    @Cacheable(
        region = "search-results",
        key = "'search:' + #query + ':' + #filters.hashCode()",
        multilevel = true,
        l1Ttl = "5m",
        l2Ttl = "1h"
    )
    public SearchResults search(String query, SearchFilters filters) {
        return searchService.performSearch(query, filters);
    }
}
```

### Cache Synchronization and Consistency

```java
import com.exalt.caching.sync.CacheReplicationManager;
import com.exalt.caching.consistency.CacheConsistencyManager;

@Service
public class CacheSynchronizationService {
    private final CacheReplicationManager replicationManager;
    private final CacheConsistencyManager consistencyManager;
    
    // Cross-region cache replication
    @EventListener(CacheUpdateEvent.class)
    public void handleCacheUpdate(CacheUpdateEvent event) {
        // Replicate cache updates to other regions
        replicationManager.replicate(
            ReplicationRequest.builder()
                .sourceRegion(event.getRegion())
                .key(event.getKey())
                .value(event.getValue())
                .operation(event.getOperation())
                .timestamp(event.getTimestamp())
                .targetRegions(Arrays.asList("us-west", "eu-central", "ap-southeast"))
                .build()
        );
    }
    
    // Eventual consistency handling
    @EventListener(DataInconsistencyDetectedEvent.class)
    public void handleInconsistency(DataInconsistencyDetectedEvent event) {
        log.warn("Cache inconsistency detected for key: {}", event.getKey());
        
        // Resolve inconsistency
        consistencyManager.resolveInconsistency(
            InconsistencyResolution.builder()
                .key(event.getKey())
                .strategy(ResolutionStrategy.LATEST_TIMESTAMP)
                .forceRefresh(true)
                .notifyNodes(true)
                .build()
        );
    }
    
    // Cache validation and repair
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void validateAndRepairCache() {
        ValidationReport report = consistencyManager.validateConsistency(
            ValidationConfig.builder()
                .regions(Arrays.asList("products", "users", "orders"))
                .samplePercentage(0.1) // Validate 10% of entries
                .repairInconsistencies(true)
                .build()
        );
        
        if (report.hasInconsistencies()) {
            log.warn("Cache inconsistencies found and repaired: {}", report);
            alertService.sendAlert(AlertLevel.WARNING, 
                "Cache inconsistencies detected and repaired", report);
        }
    }
}
```

## API Reference

### Core Caching API

#### CacheManager
- `CacheManager(CacheConfiguration config)`: Initialize cache manager with configuration
- `<T> T get(String key, Class<T> type)`: Get cached value by key
- `<T> T get(String key, Class<T> type, Supplier<T> loader)`: Get with fallback loader
- `void put(String key, Object value)`: Store value in cache
- `void put(String key, Object value, Duration ttl)`: Store with custom TTL
- `boolean exists(String key)`: Check if key exists in cache
- `void evict(String key)`: Remove specific key from cache
- `void evictAll()`: Clear all cache entries
- `void evictByPattern(String pattern)`: Remove keys matching pattern
- `Set<String> getKeys(String pattern)`: Get keys matching pattern
- `CacheStatistics getStatistics()`: Get cache performance statistics

#### CacheRegion
- `CacheRegion(String name, CacheConfig config)`: Create cache region
- `String getName()`: Get region name
- `CacheConfig getConfig()`: Get region configuration
- `long size()`: Get number of entries in region
- `long estimateSize()`: Get estimated memory usage
- `void clear()`: Clear all entries in region
- `Map<String, Object> getAll(Set<String> keys)`: Get multiple values
- `void putAll(Map<String, Object> entries)`: Store multiple values
- `Set<String> getAllKeys()`: Get all keys in region

### Cache Configuration API

#### CacheConfiguration
- `CacheConfiguration.builder()`: Create configuration builder
- `CacheConfiguration redisCluster(RedisClusterConfig config)`: Set Redis cluster config
- `CacheConfiguration defaultTtl(Duration ttl)`: Set default time-to-live
- `CacheConfiguration maxMemoryPolicy(MaxMemoryPolicy policy)`: Set eviction policy
- `CacheConfiguration enableCompression(boolean enable)`: Enable/disable compression
- `CacheConfiguration enableEncryption(boolean enable)`: Enable/disable encryption
- `CacheConfiguration metricsEnabled(boolean enable)`: Enable/disable metrics
- `CacheConfiguration serializer(CacheSerializer serializer)`: Set serializer
- `CacheConfiguration keyGenerator(CacheKeyGenerator generator)`: Set key generator

#### RedisClusterConfig
- `RedisClusterConfig.builder()`: Create Redis cluster configuration
- `RedisClusterConfig nodes(List<String> nodes)`: Set cluster nodes
- `RedisClusterConfig password(String password)`: Set authentication password
- `RedisClusterConfig connectionTimeout(Duration timeout)`: Set connection timeout
- `RedisClusterConfig socketTimeout(Duration timeout)`: Set socket timeout
- `RedisClusterConfig maxAttempts(int attempts)`: Set max retry attempts
- `RedisClusterConfig enableSsl(boolean ssl)`: Enable SSL/TLS

### Cache Annotations

#### @Cacheable
- `region`: Cache region name
- `key`: Cache key expression (SpEL)
- `condition`: Caching condition (SpEL)
- `unless`: Skip caching condition (SpEL)
- `ttl`: Time-to-live duration
- `refreshAhead`: Enable refresh-ahead strategy
- `writeThrough`: Enable write-through caching
- `multilevel`: Enable multi-level caching

#### @CachePut
- `region`: Cache region name
- `key`: Cache key expression (SpEL)
- `condition`: Caching condition (SpEL)
- `ttl`: Time-to-live duration

#### @CacheEvict
- `region`: Cache region name
- `key`: Cache key expression (SpEL)
- `allEntries`: Evict all entries in region
- `beforeInvocation`: Evict before method execution

### Cache Metrics API

#### CacheMetricsCollector
- `CacheMetricsCollector()`: Default constructor
- `CacheStatistics getStatistics(String region)`: Get region statistics
- `double getHitRate(String region, Duration period)`: Get hit rate for period
- `double getMissRate(String region, Duration period)`: Get miss rate for period
- `Duration getAverageLoadTime(String region, Duration period)`: Get average load time
- `long getEvictionCount(String region, Duration period)`: Get eviction count
- `double getMemoryUsage(String region)`: Get memory usage percentage
- `void recordHit(String region, String key)`: Record cache hit
- `void recordMiss(String region, String key)`: Record cache miss
- `void recordLoadTime(String region, String key, Duration loadTime)`: Record load time

#### CacheAnalytics
- `CacheAnalytics(CacheMetricsCollector collector)`: Initialize with metrics collector
- `List<String> getTopAccessedKeys(String region, int limit)`: Get most accessed keys
- `List<String> getBottomAccessedKeys(String region, int limit)`: Get least accessed keys
- `Map<String, Double> getKeyHitRates(String region)`: Get hit rates by key
- `CacheEfficiencyReport generateEfficiencyReport(String region)`: Generate efficiency report
- `List<String> identifyHotspots(String region, double threshold)`: Identify hotspot keys
- `List<String> identifyColdKeys(String region, Duration period)`: Identify unused keys

## Best Practices

1. **Cache Key Design**: Use consistent, hierarchical key naming conventions
2. **TTL Strategy**: Set appropriate TTL values based on data volatility
3. **Memory Management**: Monitor memory usage and configure proper eviction policies
4. **Cache Warming**: Implement intelligent cache warming for critical data
5. **Invalidation Strategy**: Design efficient cache invalidation patterns
6. **Monitoring**: Implement comprehensive cache metrics and alerting
7. **Testing**: Test cache behavior under various load conditions

## Related Documentation

- [API Specification](../api-docs/openapi.yaml)
- [Architecture Documentation](./architecture/README.md)
- [Setup Guide](./setup/README.md)
- [Operations Guide](./operations/README.md)
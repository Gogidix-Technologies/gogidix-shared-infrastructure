# Caching Service API Documentation

## Core Caching API

### CacheManager
- `CacheManager()`: Default constructor with standard configuration
- `CacheManager(CacheConfiguration config)`: Initialize cache manager with custom configuration
- `<T> T get(String key, Class<T> type)`: Get cached value by key
- `<T> T get(String key, Class<T> type, Supplier<T> loader)`: Get with fallback loader function
- `<T> Optional<T> getOptional(String key, Class<T> type)`: Get optional cached value
- `void put(String key, Object value)`: Store value with default TTL
- `void put(String key, Object value, Duration ttl)`: Store value with custom TTL
- `void putIfAbsent(String key, Object value)`: Store only if key doesn't exist
- `void putIfAbsent(String key, Object value, Duration ttl)`: Store with TTL if absent
- `boolean exists(String key)`: Check if key exists in cache
- `void evict(String key)`: Remove specific key from cache
- `void evictAll()`: Clear all cache entries
- `void evictAll(String region)`: Clear all entries in specific region
- `void evictByPattern(String pattern)`: Remove keys matching pattern
- `void evictByTags(Set<String> tags)`: Remove entries with matching tags
- `Set<String> getKeys()`: Get all cache keys
- `Set<String> getKeys(String pattern)`: Get keys matching pattern
- `Set<String> getKeys(String region)`: Get all keys in region
- `long size()`: Get total number of cached entries
- `long size(String region)`: Get number of entries in region
- `CacheStatistics getStatistics()`: Get overall cache performance statistics
- `CacheStatistics getStatistics(String region)`: Get region-specific statistics
- `void refresh(String key)`: Force refresh of cached entry
- `void refreshAsync(String key)`: Asynchronously refresh cached entry
- `CompletableFuture<Void> refreshAllAsync()`: Refresh all entries asynchronously

### CacheRegion
- `CacheRegion(String name)`: Create cache region with default configuration
- `CacheRegion(String name, CacheConfig config)`: Create region with custom config
- `String getName()`: Get region name
- `CacheConfig getConfig()`: Get region configuration
- `void setConfig(CacheConfig config)`: Update region configuration
- `<T> T get(String key, Class<T> type)`: Get value from this region
- `<T> T get(String key, Class<T> type, Supplier<T> loader)`: Get with loader
- `void put(String key, Object value)`: Store value in this region
- `void put(String key, Object value, Duration ttl)`: Store with custom TTL
- `boolean containsKey(String key)`: Check if key exists in region
- `void remove(String key)`: Remove key from region
- `void clear()`: Clear all entries in region
- `Set<String> keySet()`: Get all keys in region
- `Collection<Object> values()`: Get all values in region
- `Map<String, Object> getAll(Set<String> keys)`: Get multiple values
- `void putAll(Map<String, Object> entries)`: Store multiple entries
- `void putAll(Map<String, Object> entries, Duration ttl)`: Store with TTL
- `long size()`: Get number of entries in region
- `long estimateSize()`: Get estimated memory usage in bytes
- `boolean isEmpty()`: Check if region is empty
- `CacheStatistics getStatistics()`: Get region statistics

### CacheEntry
- `CacheEntry(String key, Object value)`: Create cache entry
- `CacheEntry(String key, Object value, Duration ttl)`: Create with TTL
- `String getKey()`: Get entry key
- `Object getValue()`: Get entry value
- `<T> T getValue(Class<T> type)`: Get typed value
- `Duration getTtl()`: Get time-to-live
- `void setTtl(Duration ttl)`: Set time-to-live
- `Instant getCreatedAt()`: Get creation timestamp
- `Instant getLastAccessedAt()`: Get last access timestamp
- `Instant getExpiresAt()`: Get expiration timestamp
- `boolean isExpired()`: Check if entry is expired
- `long getAccessCount()`: Get access count
- `void incrementAccessCount()`: Increment access counter
- `Set<String> getTags()`: Get entry tags
- `void addTag(String tag)`: Add tag to entry
- `void removeTag(String tag)`: Remove tag from entry
- `Map<String, Object> getMetadata()`: Get entry metadata
- `void setMetadata(String key, Object value)`: Set metadata property

### CacheConfiguration
- `CacheConfiguration()`: Default configuration constructor
- `CacheConfiguration.builder()`: Create configuration builder
- `String getProviderType()`: Get cache provider type (REDIS, CAFFEINE, etc.)
- `CacheConfiguration setProviderType(String type)`: Set cache provider
- `Duration getDefaultTtl()`: Get default time-to-live
- `CacheConfiguration setDefaultTtl(Duration ttl)`: Set default TTL
- `long getMaxSize()`: Get maximum cache size
- `CacheConfiguration setMaxSize(long size)`: Set maximum size
- `EvictionPolicy getEvictionPolicy()`: Get eviction policy
- `CacheConfiguration setEvictionPolicy(EvictionPolicy policy)`: Set eviction policy
- `boolean isCompressionEnabled()`: Check if compression is enabled
- `CacheConfiguration enableCompression(boolean enable)`: Enable/disable compression
- `boolean isEncryptionEnabled()`: Check if encryption is enabled
- `CacheConfiguration enableEncryption(boolean enable)`: Enable/disable encryption
- `boolean isMetricsEnabled()`: Check if metrics collection is enabled
- `CacheConfiguration enableMetrics(boolean enable)`: Enable/disable metrics
- `CacheSerializer getSerializer()`: Get value serializer
- `CacheConfiguration setSerializer(CacheSerializer serializer)`: Set serializer
- `CacheKeyGenerator getKeyGenerator()`: Get key generator
- `CacheConfiguration setKeyGenerator(CacheKeyGenerator generator)`: Set key generator
- `RedisConfiguration getRedisConfig()`: Get Redis configuration
- `CacheConfiguration setRedisConfig(RedisConfiguration config)`: Set Redis config

### RedisConfiguration
- `RedisConfiguration()`: Default Redis configuration
- `RedisConfiguration.builder()`: Create Redis configuration builder
- `String getHost()`: Get Redis host
- `RedisConfiguration setHost(String host)`: Set Redis host
- `int getPort()`: Get Redis port
- `RedisConfiguration setPort(int port)`: Set Redis port
- `String getPassword()`: Get Redis password
- `RedisConfiguration setPassword(String password)`: Set Redis password
- `int getDatabase()`: Get Redis database number
- `RedisConfiguration setDatabase(int database)`: Set database number
- `Duration getConnectionTimeout()`: Get connection timeout
- `RedisConfiguration setConnectionTimeout(Duration timeout)`: Set connection timeout
- `Duration getSocketTimeout()`: Get socket timeout
- `RedisConfiguration setSocketTimeout(Duration timeout)`: Set socket timeout
- `boolean isSslEnabled()`: Check if SSL is enabled
- `RedisConfiguration enableSsl(boolean ssl)`: Enable/disable SSL
- `List<String> getClusterNodes()`: Get cluster node addresses
- `RedisConfiguration setClusterNodes(List<String> nodes)`: Set cluster nodes
- `int getMaxTotal()`: Get maximum connections
- `RedisConfiguration setMaxTotal(int max)`: Set max connections
- `int getMaxIdle()`: Get maximum idle connections
- `RedisConfiguration setMaxIdle(int maxIdle)`: Set max idle connections
- `int getMinIdle()`: Get minimum idle connections
- `RedisConfiguration setMinIdle(int minIdle)`: Set min idle connections

## Cache Operations API

### CacheOperations
- `CacheOperations(CacheManager cacheManager)`: Initialize with cache manager
- `<T> T executeWithCache(String key, Class<T> type, Supplier<T> loader)`: Execute with cache
- `<T> T executeWithCache(String key, Class<T> type, Supplier<T> loader, Duration ttl)`: Execute with TTL
- `void executeWithInvalidation(String pattern, Runnable operation)`: Execute and invalidate
- `<T> List<T> executeBatch(List<String> keys, Class<T> type, Function<List<String>, List<T>> loader)`: Batch operation
- `void executeBulkInvalidation(Set<String> patterns)`: Bulk cache invalidation
- `CompletableFuture<Void> executeAsyncInvalidation(String pattern)`: Async invalidation
- `void executeConditionalInvalidation(String pattern, Predicate<CacheEntry> condition)`: Conditional invalidation
- `CacheTransaction beginTransaction()`: Start cache transaction
- `void commitTransaction(CacheTransaction transaction)`: Commit transaction
- `void rollbackTransaction(CacheTransaction transaction)`: Rollback transaction

### CacheTransaction
- `CacheTransaction()`: Create new cache transaction
- `String getTransactionId()`: Get transaction identifier
- `void put(String key, Object value)`: Add put operation to transaction
- `void put(String key, Object value, Duration ttl)`: Add put with TTL
- `void evict(String key)`: Add evict operation to transaction
- `void evictPattern(String pattern)`: Add pattern eviction
- `List<CacheOperation> getOperations()`: Get all operations in transaction
- `void commit()`: Commit all operations atomically
- `void rollback()`: Rollback all operations
- `boolean isCommitted()`: Check if transaction is committed
- `boolean isRolledBack()`: Check if transaction is rolled back
- `Instant getStartTime()`: Get transaction start time
- `Duration getDuration()`: Get transaction duration

### CacheEventListener
- `CacheEventListener()`: Default constructor
- `void onCacheHit(CacheHitEvent event)`: Handle cache hit event
- `void onCacheMiss(CacheMissEvent event)`: Handle cache miss event
- `void onCachePut(CachePutEvent event)`: Handle cache put event
- `void onCacheEvict(CacheEvictEvent event)`: Handle cache evict event
- `void onCacheExpire(CacheExpireEvent event)`: Handle cache expire event
- `void onCacheError(CacheErrorEvent event)`: Handle cache error event
- `void onRegionCleared(RegionClearedEvent event)`: Handle region clear event
- `void addEventListener(CacheEventType type, Consumer<CacheEvent> listener)`: Add event listener
- `void removeEventListener(CacheEventType type, Consumer<CacheEvent> listener)`: Remove listener

## Cache Strategies API

### CacheAsideStrategy
- `CacheAsideStrategy(CacheManager cacheManager)`: Initialize cache-aside strategy
- `<T> T get(String key, Class<T> type, Supplier<T> loader)`: Get with cache-aside pattern
- `void put(String key, Object value)`: Put value using cache-aside
- `void evict(String key)`: Evict key using cache-aside
- `boolean isEnabled()`: Check if strategy is enabled
- `void setEnabled(boolean enabled)`: Enable/disable strategy
- `CacheAsideConfig getConfig()`: Get strategy configuration
- `void setConfig(CacheAsideConfig config)`: Set strategy configuration

### WriteThoughStrategy
- `WriteThroughStrategy(CacheManager cacheManager, DataSource dataSource)`: Initialize write-through
- `<T> T put(String key, T value)`: Put value with write-through
- `<T> T putIfAbsent(String key, T value)`: Put if absent with write-through
- `void remove(String key)`: Remove with write-through
- `void removeAll(Set<String> keys)`: Remove multiple with write-through
- `void flush()`: Flush pending writes to data source
- `boolean isWritePending(String key)`: Check if write is pending
- `int getPendingWriteCount()`: Get number of pending writes
- `void configureBatching(int batchSize, Duration flushInterval)`: Configure write batching

### WriteBehindStrategy
- `WriteBehindStrategy(CacheManager cacheManager, DataSource dataSource)`: Initialize write-behind
- `<T> T put(String key, T value)`: Put value with write-behind
- `void remove(String key)`: Remove with write-behind
- `void queueWrite(WriteOperation operation)`: Queue write operation
- `void processQueue()`: Process write queue
- `List<WriteOperation> getPendingWrites()`: Get pending write operations
- `void configureBatching(int batchSize, Duration delay)`: Configure write batching
- `void configureRetry(int maxRetries, Duration backoff)`: Configure retry policy
- `void flush()`: Force flush all pending writes
- `void shutdown()`: Gracefully shutdown write-behind processor

### RefreshAheadStrategy
- `RefreshAheadStrategy(CacheManager cacheManager)`: Initialize refresh-ahead strategy
- `<T> T get(String key, Class<T> type, Supplier<T> loader)`: Get with refresh-ahead
- `void scheduleRefresh(String key, Supplier<Object> loader, Duration refreshTime)`: Schedule refresh
- `void scheduleRefresh(String key, Supplier<Object> loader, double refreshThreshold)`: Schedule by threshold
- `void cancelRefresh(String key)`: Cancel scheduled refresh
- `boolean isRefreshScheduled(String key)`: Check if refresh is scheduled
- `Set<String> getScheduledRefreshKeys()`: Get all keys with scheduled refresh
- `void configureThreadPool(int corePoolSize, int maxPoolSize)`: Configure refresh thread pool
- `RefreshStatistics getRefreshStatistics()`: Get refresh statistics

## Cache Analytics API

### CacheAnalytics
- `CacheAnalytics(CacheManager cacheManager)`: Initialize analytics with cache manager
- `CacheAnalytics(CacheMetricsCollector metricsCollector)`: Initialize with metrics collector
- `AnalyticsReport generateReport()`: Generate comprehensive analytics report
- `AnalyticsReport generateReport(String region)`: Generate region-specific report
- `AnalyticsReport generateReport(AnalyticsFilter filter)`: Generate filtered report
- `List<String> getTopAccessedKeys(int limit)`: Get most frequently accessed keys
- `List<String> getTopAccessedKeys(String region, int limit)`: Get top keys in region
- `List<String> getBottomAccessedKeys(int limit)`: Get least accessed keys
- `Map<String, Long> getKeyAccessCounts()`: Get access counts for all keys
- `Map<String, Long> getKeyAccessCounts(String region)`: Get access counts in region
- `Map<String, Double> getKeyHitRates()`: Get hit rates for all keys
- `Map<String, Double> getKeyHitRates(String region)`: Get hit rates in region
- `List<String> identifyHotspots(double threshold)`: Identify hotspot keys above threshold
- `List<String> identifyHotspots(String region, double threshold)`: Identify region hotspots
- `List<String> identifyColdKeys(Duration period)`: Identify unused keys in period
- `List<String> identifyColdKeys(String region, Duration period)`: Identify cold keys in region
- `CacheEfficiencyReport analyzeEfficiency()`: Analyze cache efficiency
- `CacheEfficiencyReport analyzeEfficiency(String region)`: Analyze region efficiency
- `MemoryUsageReport analyzeMemoryUsage()`: Analyze memory usage patterns
- `MemoryUsageReport analyzeMemoryUsage(String region)`: Analyze region memory usage
- `PerformanceReport analyzePerformance(Duration period)`: Analyze performance over period
- `TrendAnalysis analyzeTrends(Duration period)`: Analyze cache usage trends

### CacheMetricsCollector
- `CacheMetricsCollector()`: Default constructor
- `CacheMetricsCollector(MetricsConfiguration config)`: Initialize with configuration
- `void recordHit(String key)`: Record cache hit
- `void recordHit(String region, String key)`: Record hit in specific region
- `void recordMiss(String key)`: Record cache miss
- `void recordMiss(String region, String key)`: Record miss in specific region
- `void recordPut(String key, long size)`: Record cache put operation
- `void recordPut(String region, String key, long size)`: Record put in region
- `void recordEviction(String key, EvictionReason reason)`: Record eviction
- `void recordEviction(String region, String key, EvictionReason reason)`: Record region eviction
- `void recordLoadTime(String key, Duration loadTime)`: Record data load time
- `void recordLoadTime(String region, String key, Duration loadTime)`: Record region load time
- `void recordError(String key, Throwable error)`: Record cache error
- `void recordError(String region, String key, Throwable error)`: Record region error
- `CacheStatistics getStatistics()`: Get overall statistics
- `CacheStatistics getStatistics(String region)`: Get region statistics
- `double getHitRate()`: Get overall hit rate
- `double getHitRate(String region)`: Get region hit rate
- `double getMissRate()`: Get overall miss rate
- `double getMissRate(String region)`: Get region miss rate
- `long getTotalHits()`: Get total cache hits
- `long getTotalHits(String region)`: Get total hits in region
- `long getTotalMisses()`: Get total cache misses
- `long getTotalMisses(String region)`: Get total misses in region
- `Duration getAverageLoadTime()`: Get average load time
- `Duration getAverageLoadTime(String region)`: Get average region load time
- `long getEvictionCount()`: Get total evictions
- `long getEvictionCount(String region)`: Get region evictions
- `Map<EvictionReason, Long> getEvictionsByReason()`: Get evictions by reason
- `Map<EvictionReason, Long> getEvictionsByReason(String region)`: Get region evictions by reason

### CacheHealthChecker
- `CacheHealthChecker(CacheManager cacheManager)`: Initialize health checker
- `HealthStatus checkHealth()`: Check overall cache health
- `HealthStatus checkRegionHealth(String region)`: Check specific region health
- `List<HealthIssue> identifyIssues()`: Identify potential health issues
- `List<HealthIssue> identifyIssues(String region)`: Identify region-specific issues
- `HealthReport generateHealthReport()`: Generate comprehensive health report
- `HealthReport generateHealthReport(String region)`: Generate region health report
- `boolean isHealthy()`: Check if cache is healthy
- `boolean isRegionHealthy(String region)`: Check if region is healthy
- `void addHealthCheck(String name, HealthCheck check)`: Add custom health check
- `void removeHealthCheck(String name)`: Remove custom health check
- `Map<String, HealthCheckResult> runAllHealthChecks()`: Run all registered health checks
- `HealthCheckResult runHealthCheck(String name)`: Run specific health check
- `void configureAlerts(HealthAlertConfig config)`: Configure health alerts
- `void enableContinuousMonitoring(Duration interval)`: Enable continuous health monitoring

## Serialization API

### CacheSerializer
- `CacheSerializer()`: Default constructor
- `byte[] serialize(Object object)`: Serialize object to byte array
- `<T> T deserialize(byte[] bytes, Class<T> type)`: Deserialize bytes to object
- `boolean canSerialize(Class<?> type)`: Check if type can be serialized
- `String getContentType()`: Get serializer content type
- `void configure(SerializerConfig config)`: Configure serializer
- `SerializerConfig getConfig()`: Get serializer configuration

### JsonCacheSerializer
- `JsonCacheSerializer()`: Default JSON serializer
- `JsonCacheSerializer(ObjectMapper objectMapper)`: Initialize with custom ObjectMapper
- `byte[] serialize(Object object)`: Serialize to JSON bytes
- `<T> T deserialize(byte[] bytes, Class<T> type)`: Deserialize from JSON
- `<T> T deserialize(byte[] bytes, TypeReference<T> typeRef)`: Deserialize with type reference
- `ObjectMapper getObjectMapper()`: Get underlying ObjectMapper
- `void setObjectMapper(ObjectMapper objectMapper)`: Set custom ObjectMapper

### BinaryCacheSerializer
- `BinaryCacheSerializer()`: Default binary serializer
- `BinaryCacheSerializer(SerializationConfig config)`: Initialize with configuration
- `byte[] serialize(Object object)`: Serialize to binary format
- `<T> T deserialize(byte[] bytes, Class<T> type)`: Deserialize from binary
- `boolean isCompressionEnabled()`: Check if compression is enabled
- `void enableCompression(boolean enable)`: Enable/disable compression
- `CompressionType getCompressionType()`: Get compression algorithm
- `void setCompressionType(CompressionType type)`: Set compression algorithm

## Key Generation API

### CacheKeyGenerator
- `CacheKeyGenerator()`: Default key generator
- `CacheKeyGenerator(KeyGeneratorConfig config)`: Initialize with configuration
- `String generateKey(Object... keyParts)`: Generate cache key from parts
- `String generateKey(String prefix, Object... keyParts)`: Generate with prefix
- `String generateVersionedKey(String baseKey, String version)`: Generate versioned key
- `String generateHashedKey(String longKey)`: Generate hashed key for long keys
- `String generateTaggedKey(String baseKey, Set<String> tags)`: Generate key with tags
- `boolean isValidKey(String key)`: Validate cache key format
- `String normalizeKey(String key)`: Normalize key format
- `KeyGeneratorConfig getConfig()`: Get generator configuration
- `void setConfig(KeyGeneratorConfig config)`: Set generator configuration

### HierarchicalKeyGenerator
- `HierarchicalKeyGenerator(String separator)`: Initialize with key separator
- `HierarchicalKeyGenerator(String namespace, String separator)`: Initialize with namespace
- `String generateKey(String... pathSegments)`: Generate hierarchical key
- `String generateKey(String namespace, String... pathSegments)`: Generate with namespace
- `List<String> parseKey(String key)`: Parse key into segments
- `String getParentKey(String key)`: Get parent key in hierarchy
- `List<String> getChildKeys(String parentKey)`: Get child keys
- `String getNamespace(String key)`: Extract namespace from key
- `boolean isChildOf(String childKey, String parentKey)`: Check parent-child relationship

## Error Handling

### CacheException
- `CacheException(String message)`: Create with error message
- `CacheException(String message, Throwable cause)`: Create with message and cause
- `String getCacheRegion()`: Get cache region where error occurred
- `String getCacheKey()`: Get cache key that caused error
- `CacheOperation getOperation()`: Get cache operation that failed
- `ErrorSeverity getSeverity()`: Get error severity level

### CacheTimeoutException
- `CacheTimeoutException(String message, Duration timeout)`: Create with timeout duration
- `Duration getTimeout()`: Get timeout duration that was exceeded
- `String getOperation()`: Get operation that timed out

### CacheKeyNotFoundException
- `CacheKeyNotFoundException(String key)`: Create with missing key
- `CacheKeyNotFoundException(String region, String key)`: Create with region and key
- `String getKey()`: Get the key that was not found
- `String getRegion()`: Get the region where key was not found

### CacheSerializationException
- `CacheSerializationException(String message, Class<?> type)`: Create with type info
- `Class<?> getTargetType()`: Get type that failed to serialize/deserialize
- `boolean isSerializationError()`: Check if error occurred during serialization
- `boolean isDeserializationError()`: Check if error occurred during deserialization
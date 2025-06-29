# Feature Flag Service API Documentation

## Core Flag Management API

### FeatureFlagService
- `FeatureFlagService(config)`: Initialize feature flag service with configuration
- `FeatureFlagService(store, cache, ruleEngine)`: Initialize with specific components
- `createFlag(flagDefinition)`: Create new feature flag with targeting rules
- `updateFlag(flagKey, updates)`: Update existing feature flag configuration
- `deleteFlag(flagKey, options)`: Delete feature flag with safety checks
- `archiveFlag(flagKey, archiveOptions)`: Archive flag instead of deletion
- `getFlag(flagKey)`: Get complete flag configuration and metadata
- `getFlag(flagKey, includeHistory)`: Get flag with change history
- `getAllFlags(filters)`: Get all flags with optional filtering
- `getAllFlags(environment, status)`: Get flags by environment and status
- `searchFlags(searchCriteria)`: Search flags by name, tags, or owner
- `cloneFlag(sourceFlagKey, targetFlagKey, options)`: Clone flag configuration
- `validateFlag(flagDefinition)`: Validate flag configuration before creation
- `enableFlag(flagKey)`: Enable flag evaluation
- `disableFlag(flagKey)`: Disable flag evaluation
- `getFlagHistory(flagKey, dateRange)`: Get flag change history
- `rollbackFlag(flagKey, version)`: Rollback flag to previous version
- `bulkUpdateFlags(flagUpdates)`: Update multiple flags in batch operation

### FlagEvaluationService
- `FlagEvaluationService(flagService, ruleEngine)`: Initialize evaluation service
- `evaluateFlag(flagKey, context)`: Evaluate single flag for given context
- `evaluateFlag(flagKey, context, defaultValue)`: Evaluate with fallback value
- `evaluateFlags(flagKeys, context)`: Evaluate multiple flags efficiently
- `evaluateBatchFlags(contexts, flagKeys)`: Batch evaluate for multiple contexts
- `getBooleanFlag(flagKey, context, defaultValue)`: Get boolean flag value
- `getStringFlag(flagKey, context, defaultValue)`: Get string flag value
- `getNumericFlag(flagKey, context, defaultValue)`: Get numeric flag value
- `getJSONFlag(flagKey, context, defaultValue)`: Get JSON object flag value
- `getEvaluationResult(flagKey, context)`: Get detailed evaluation result
- `precomputeFlags(context, flagKeys)`: Precompute flags for performance
- `warmupCache(flagKeys, contexts)`: Warm up evaluation cache
- `invalidateEvaluationCache(flagKey)`: Invalidate cached evaluations
- `getEvaluationMetrics(flagKey, dateRange)`: Get evaluation performance metrics

### RuleEngine
- `RuleEngine(config)`: Initialize rule evaluation engine
- `evaluateRule(rule, context)`: Evaluate single targeting rule
- `evaluateRules(rules, context)`: Evaluate multiple rules with precedence
- `validateRule(rule)`: Validate rule syntax and logic
- `optimizeRules(rules)`: Optimize rule evaluation performance
- `addCustomOperator(name, operatorFunction)`: Add custom rule operator
- `removeCustomOperator(name)`: Remove custom operator
- `getAvailableOperators()`: Get list of available operators
- `testRule(rule, testContexts)`: Test rule against multiple contexts
- `analyzeRulePerformance(rule, metrics)`: Analyze rule evaluation performance
- `generateRuleDocumentation(rules)`: Generate human-readable rule docs
- `convertRule(rule, targetFormat)`: Convert rule to different format
- `mergeRules(rules, mergeStrategy)`: Merge multiple rules intelligently

## Experimentation and A/B Testing API

### ExperimentManager
- `ExperimentManager(flagService, analyticsService)`: Initialize experiment manager
- `createExperiment(experimentDefinition)`: Create new A/B test experiment
- `updateExperiment(experimentKey, updates)`: Update experiment configuration
- `startExperiment(experimentKey, options)`: Start experiment with validation
- `pauseExperiment(experimentKey, reason)`: Pause running experiment
- `stopExperiment(experimentKey, conclusion)`: Stop experiment and lock results
- `deleteExperiment(experimentKey, confirmation)`: Delete experiment permanently
- `getExperiment(experimentKey)`: Get experiment configuration and status
- `getAllExperiments(filters)`: Get all experiments with filtering
- `getActiveExperiments(context)`: Get experiments active for given context
- `assignVariant(experimentKey, context)`: Assign user to experiment variant
- `getVariantAssignment(experimentKey, userId)`: Get user's variant assignment
- `trackConversion(experimentKey, userId, conversionData)`: Track conversion event
- `getExperimentResults(experimentKey)`: Get statistical results and analysis
- `calculateStatisticalSignificance(experimentKey)`: Calculate significance
- `generateExperimentReport(experimentKey, format)`: Generate experiment report

### VariantManager
- `VariantManager(experimentManager)`: Initialize variant management
- `createVariant(experimentKey, variantDefinition)`: Create experiment variant
- `updateVariant(experimentKey, variantKey, updates)`: Update variant configuration
- `deleteVariant(experimentKey, variantKey)`: Delete experiment variant
- `getVariant(experimentKey, variantKey)`: Get variant configuration
- `getAllVariants(experimentKey)`: Get all variants for experiment
- `allocateTraffic(experimentKey, trafficAllocation)`: Set traffic allocation
- `rebalanceTraffic(experimentKey, newAllocations)`: Rebalance variant traffic
- `getVariantMetrics(experimentKey, variantKey)`: Get variant performance metrics
- `compareVariants(experimentKey, variantKeys)`: Compare variant performance
- `optimizeTrafficAllocation(experimentKey, optimization)`: Auto-optimize allocation

### StatisticalAnalyzer
- `StatisticalAnalyzer(config)`: Initialize statistical analysis engine
- `calculateSampleSize(effectSize, power, significance)`: Calculate required sample size
- `calculatePower(sampleSize, effectSize, significance)`: Calculate statistical power
- `calculateSignificance(experimentData)`: Calculate statistical significance
- `calculateConfidenceInterval(data, confidence)`: Calculate confidence intervals
- `performTTest(control, treatment)`: Perform t-test for mean comparison
- `performChiSquareTest(contingencyTable)`: Perform chi-square test
- `performMannWhitneyTest(control, treatment)`: Perform Mann-Whitney U test
- `calculateEffectSize(control, treatment)`: Calculate effect size (Cohen's d)
- `detectEarlySignificance(experimentKey)`: Detect early statistical significance
- `recommendExperimentDuration(experimentKey)`: Recommend experiment duration
- `validateExperimentDesign(experimentDefinition)`: Validate experiment design
- `generateStatisticalReport(experimentKey)`: Generate detailed statistical report

## Segmentation and Targeting API

### SegmentManager
- `SegmentManager(userService, contextService)`: Initialize segment manager
- `createSegment(segmentDefinition)`: Create user segment with criteria
- `updateSegment(segmentKey, updates)`: Update segment definition
- `deleteSegment(segmentKey)`: Delete user segment
- `getSegment(segmentKey)`: Get segment configuration and stats
- `getAllSegments(filters)`: Get all segments with filtering
- `evaluateSegment(segmentKey, context)`: Check if context matches segment
- `getUserSegments(userId)`: Get all segments for specific user
- `getSegmentUsers(segmentKey, pagination)`: Get users in segment
- `calculateSegmentSize(segmentKey)`: Calculate current segment size
- `previewSegment(segmentDefinition)`: Preview segment without saving
- `exportSegment(segmentKey, format)`: Export segment data
- `importSegment(segmentData, validation)`: Import segment from data
- `mergeSegments(segmentKeys, mergeOptions)`: Merge multiple segments

### TargetingEngine
- `TargetingEngine(segmentManager, ruleEngine)`: Initialize targeting engine
- `evaluateTargeting(targetingRules, context)`: Evaluate targeting rules
- `createTargetingRule(ruleDefinition)`: Create new targeting rule
- `updateTargetingRule(ruleId, updates)`: Update targeting rule
- `deleteTargetingRule(ruleId)`: Delete targeting rule
- `validateTargetingRule(rule)`: Validate rule syntax and logic
- `testTargeting(rules, testContexts)`: Test targeting against contexts
- `optimizeTargeting(rules)`: Optimize targeting performance
- `getTargetingInsights(flagKey, dateRange)`: Get targeting performance insights
- `calculateReachEstimate(targetingRules)`: Estimate targeting reach
- `suggestTargetingImprovements(flagKey)`: Suggest targeting optimizations

### ContextBuilder
- `ContextBuilder()`: Initialize context builder
- `setUser(userAttributes)`: Set user attributes in context
- `setDevice(deviceAttributes)`: Set device attributes
- `setLocation(locationAttributes)`: Set geographic attributes
- `setSession(sessionAttributes)`: Set session attributes
- `setCustomAttribute(key, value)`: Add custom context attribute
- `removeAttribute(key)`: Remove attribute from context
- `getContext()`: Get complete context object
- `validateContext(context)`: Validate context completeness
- `enrichContext(context, enrichmentSources)`: Enrich context with external data
- `normalizeContext(context)`: Normalize context format
- `hashContext(context, algorithm)`: Generate context hash for caching
- `compareContexts(context1, context2)`: Compare two contexts

## Configuration Management API

### ConfigurationService
- `ConfigurationService(store, cache)`: Initialize configuration service
- `getConfiguration(key, environment)`: Get configuration value
- `getConfiguration(key, environment, defaultValue)`: Get with default
- `setConfiguration(key, value, environment)`: Set configuration value
- `updateConfiguration(key, updates, environment)`: Update configuration
- `deleteConfiguration(key, environment)`: Delete configuration
- `getAllConfigurations(environment)`: Get all configurations for environment
- `getConfigurationHistory(key, dateRange)`: Get configuration change history
- `rollbackConfiguration(key, version, environment)`: Rollback to previous version
- `validateConfiguration(key, value, schema)`: Validate against schema
- `encryptConfiguration(key, value)`: Encrypt sensitive configuration
- `decryptConfiguration(key)`: Decrypt sensitive configuration
- `bulkUpdateConfigurations(updates, environment)`: Bulk update configurations
- `exportConfigurations(environment, format)`: Export configurations
- `importConfigurations(configData, environment)`: Import configurations

### EnvironmentManager
- `EnvironmentManager(configService)`: Initialize environment manager
- `createEnvironment(environmentDefinition)`: Create new environment
- `updateEnvironment(environmentKey, updates)`: Update environment settings
- `deleteEnvironment(environmentKey)`: Delete environment
- `getEnvironment(environmentKey)`: Get environment configuration
- `getAllEnvironments()`: Get all available environments
- `cloneEnvironment(sourceEnv, targetEnv)`: Clone environment configuration
- `promoteConfigurations(sourceEnv, targetEnv, keys)`: Promote configs between envs
- `getEnvironmentDiff(env1, env2)`: Compare configurations between environments
- `lockEnvironment(environmentKey, reason)`: Lock environment for changes
- `unlockEnvironment(environmentKey)`: Unlock environment
- `getEnvironmentMetrics(environmentKey)`: Get environment usage metrics

## Cache and Performance API

### CacheManager
- `CacheManager(cacheConfig)`: Initialize cache manager with configuration
- `get(key)`: Get cached value by key
- `get(key, defaultValue)`: Get with default value if not found
- `set(key, value, ttl)`: Set cached value with TTL
- `setex(key, value, ttl)`: Set with explicit expiration time
- `delete(key)`: Remove key from cache
- `deletePattern(pattern)`: Remove all keys matching pattern
- `exists(key)`: Check if key exists in cache
- `expire(key, ttl)`: Update TTL for existing key
- `ttl(key)`: Get remaining TTL for key
- `clear()`: Clear all cached entries
- `getStats()`: Get cache performance statistics
- `getSize()`: Get cache size metrics
- `warmup(keys, data)`: Warm up cache with data
- `preload(dataSource, keys)`: Preload cache from data source
- `enableStatistics(enabled)`: Enable/disable cache statistics

### PerformanceOptimizer
- `PerformanceOptimizer(services)`: Initialize performance optimizer
- `optimizeFlagEvaluation(flagKey)`: Optimize flag evaluation performance
- `optimizeRuleExecution(rules)`: Optimize rule execution order
- `optimizeCacheStrategy(usage patterns)`: Optimize caching strategy
- `identifyBottlenecks(performanceData)`: Identify performance bottlenecks
- `suggestOptimizations(metrics)`: Suggest performance improvements
- `benchmarkEvaluation(flagKeys, contexts)`: Benchmark evaluation performance
- `profileRuleExecution(rules, contexts)`: Profile rule execution performance
- `generatePerformanceReport(dateRange)`: Generate performance analysis report
- `setPerformanceThresholds(thresholds)`: Set performance alert thresholds
- `monitorPerformance(interval)`: Start performance monitoring

## Analytics and Metrics API

### AnalyticsService
- `AnalyticsService(metricsStore, aggregator)`: Initialize analytics service
- `trackFlagEvaluation(flagKey, context, result)`: Track flag evaluation event
- `trackExperimentEvent(experimentKey, userId, event)`: Track experiment event
- `trackConversion(conversionData)`: Track conversion event
- `getEvaluationMetrics(flagKey, dateRange)`: Get flag evaluation metrics
- `getExperimentMetrics(experimentKey)`: Get experiment performance metrics
- `getUserMetrics(userId, dateRange)`: Get user-specific metrics
- `getSegmentMetrics(segmentKey, dateRange)`: Get segment performance metrics
- `calculateFlagImpact(flagKey, metrics)`: Calculate flag business impact
- `generateAnalyticsReport(reportConfig)`: Generate custom analytics report
- `exportAnalyticsData(query, format)`: Export analytics data
- `createDashboard(dashboardConfig)`: Create analytics dashboard
- `updateDashboard(dashboardId, updates)`: Update dashboard configuration
- `getDashboard(dashboardId)`: Get dashboard data and configuration

### MetricsCollector
- `MetricsCollector(config)`: Initialize metrics collection
- `collectFlagMetrics(flagKey)`: Collect comprehensive flag metrics
- `collectSystemMetrics()`: Collect system performance metrics
- `collectUserMetrics(userId)`: Collect user-specific metrics
- `collectSegmentMetrics(segmentKey)`: Collect segment metrics
- `aggregateMetrics(rawMetrics, aggregationType)`: Aggregate raw metrics
- `scheduleMetricsCollection(schedule)`: Schedule automatic collection
- `exportMetrics(query, format)`: Export metrics data
- `getMetricsSchema()`: Get available metrics schema
- `validateMetrics(metricsData)`: Validate metrics data integrity
- `archiveMetrics(retentionPolicy)`: Archive old metrics data

### ReportGenerator
- `ReportGenerator(analyticsService, templater)`: Initialize report generator
- `generateFlagReport(flagKey, reportConfig)`: Generate flag performance report
- `generateExperimentReport(experimentKey, format)`: Generate experiment report
- `generateSegmentReport(segmentKey, dateRange)`: Generate segment analysis
- `generatePerformanceReport(serviceMetrics)`: Generate system performance report
- `generateComplianceReport(auditData)`: Generate compliance audit report
- `createCustomReport(reportDefinition)`: Create custom report template
- `scheduleReport(reportConfig, schedule)`: Schedule automated reports
- `getReportHistory(reportId)`: Get report generation history
- `exportReport(reportId, format)`: Export report in various formats

## SDK and Integration API

### SDKManager
- `SDKManager(configService, authService)`: Initialize SDK manager
- `generateSDKKey(application, environment)`: Generate SDK authentication key
- `revokeSDKKey(keyId, reason)`: Revoke SDK key
- `getSDKKey(keyId)`: Get SDK key details and permissions
- `getAllSDKKeys(filters)`: Get all SDK keys with filtering
- `updateSDKPermissions(keyId, permissions)`: Update SDK permissions
- `getSDKUsageMetrics(keyId, dateRange)`: Get SDK usage statistics
- `validateSDKRequest(request, keyId)`: Validate SDK request authenticity
- `getSDKConfiguration(keyId)`: Get SDK-specific configuration
- `trackSDKUsage(keyId, usageData)`: Track SDK usage events
- `generateSDKDocumentation(sdkConfig)`: Generate SDK documentation
- `createSDKExample(language, useCase)`: Generate SDK code examples

### WebhookService
- `WebhookService(deliveryService, retryHandler)`: Initialize webhook service
- `createWebhook(webhookDefinition)`: Create new webhook endpoint
- `updateWebhook(webhookId, updates)`: Update webhook configuration
- `deleteWebhook(webhookId)`: Delete webhook endpoint
- `getWebhook(webhookId)`: Get webhook configuration
- `getAllWebhooks(filters)`: Get all webhooks with filtering
- `testWebhook(webhookId, testPayload)`: Test webhook delivery
- `sendWebhook(webhookId, eventData)`: Send webhook notification
- `retryWebhook(deliveryId)`: Retry failed webhook delivery
- `getWebhookDeliveries(webhookId, dateRange)`: Get delivery history
- `getWebhookMetrics(webhookId)`: Get webhook performance metrics
- `validateWebhookSignature(payload, signature, secret)`: Validate webhook signature

### EventStreamingService
- `EventStreamingService(streamConfig)`: Initialize event streaming
- `createStream(streamDefinition)`: Create new event stream
- `subscribeToStream(streamId, callback)`: Subscribe to event stream
- `unsubscribeFromStream(streamId, subscriptionId)`: Unsubscribe from stream
- `publishEvent(streamId, eventData)`: Publish event to stream
- `getStreamMetrics(streamId)`: Get stream performance metrics
- `filterEvents(streamId, filterCriteria)`: Apply event filtering
- `pauseStream(streamId)`: Pause event stream
- `resumeStream(streamId)`: Resume paused event stream
- `getStreamHistory(streamId, dateRange)`: Get stream event history
- `configureStreamRetention(streamId, retentionPolicy)`: Configure retention

## REST API Endpoints

### Flag Management Endpoints
- `GET /api/v1/flags`: Get all feature flags
- `POST /api/v1/flags`: Create new feature flag
- `GET /api/v1/flags/{flagKey}`: Get specific flag configuration
- `PUT /api/v1/flags/{flagKey}`: Update flag configuration
- `DELETE /api/v1/flags/{flagKey}`: Delete feature flag
- `POST /api/v1/flags/{flagKey}/enable`: Enable flag
- `POST /api/v1/flags/{flagKey}/disable`: Disable flag
- `GET /api/v1/flags/{flagKey}/history`: Get flag change history
- `POST /api/v1/flags/{flagKey}/rollback`: Rollback flag to previous version
- `POST /api/v1/flags/bulk`: Bulk update multiple flags

### Flag Evaluation Endpoints
- `POST /api/v1/evaluate`: Evaluate flags for given context
- `POST /api/v1/evaluate/batch`: Batch evaluate flags for multiple contexts
- `GET /api/v1/evaluate/{flagKey}`: Evaluate specific flag
- `POST /api/v1/evaluate/{flagKey}`: Evaluate flag with POST context
- `GET /api/v1/flags/{flagKey}/value`: Get flag value for context
- `POST /api/v1/precompute`: Precompute flags for performance

### Experiment Management Endpoints
- `GET /api/v1/experiments`: Get all experiments
- `POST /api/v1/experiments`: Create new experiment
- `GET /api/v1/experiments/{experimentKey}`: Get experiment details
- `PUT /api/v1/experiments/{experimentKey}`: Update experiment
- `DELETE /api/v1/experiments/{experimentKey}`: Delete experiment
- `POST /api/v1/experiments/{experimentKey}/start`: Start experiment
- `POST /api/v1/experiments/{experimentKey}/pause`: Pause experiment
- `POST /api/v1/experiments/{experimentKey}/stop`: Stop experiment
- `GET /api/v1/experiments/{experimentKey}/results`: Get experiment results
- `POST /api/v1/experiments/{experimentKey}/track`: Track experiment event

### Segment Management Endpoints
- `GET /api/v1/segments`: Get all user segments
- `POST /api/v1/segments`: Create new segment
- `GET /api/v1/segments/{segmentKey}`: Get segment details
- `PUT /api/v1/segments/{segmentKey}`: Update segment
- `DELETE /api/v1/segments/{segmentKey}`: Delete segment
- `POST /api/v1/segments/{segmentKey}/evaluate`: Evaluate segment membership
- `GET /api/v1/segments/{segmentKey}/users`: Get users in segment
- `GET /api/v1/segments/{segmentKey}/size`: Get segment size

### Configuration Management Endpoints
- `GET /api/v1/config`: Get all configurations
- `GET /api/v1/config/{key}`: Get specific configuration
- `PUT /api/v1/config/{key}`: Set configuration value
- `DELETE /api/v1/config/{key}`: Delete configuration
- `GET /api/v1/config/{key}/history`: Get configuration history
- `POST /api/v1/config/bulk`: Bulk update configurations
- `POST /api/v1/config/import`: Import configurations
- `GET /api/v1/config/export`: Export configurations

### Analytics and Metrics Endpoints
- `GET /api/v1/analytics/flags/{flagKey}`: Get flag analytics
- `GET /api/v1/analytics/experiments/{experimentKey}`: Get experiment analytics
- `GET /api/v1/analytics/segments/{segmentKey}`: Get segment analytics
- `GET /api/v1/analytics/users/{userId}`: Get user analytics
- `POST /api/v1/analytics/report`: Generate custom analytics report
- `GET /api/v1/metrics/system`: Get system performance metrics
- `GET /api/v1/metrics/evaluation`: Get evaluation performance metrics

### SDK and Integration Endpoints
- `GET /api/v1/sdk/keys`: Get all SDK keys
- `POST /api/v1/sdk/keys`: Create new SDK key
- `DELETE /api/v1/sdk/keys/{keyId}`: Revoke SDK key
- `GET /api/v1/sdk/keys/{keyId}/usage`: Get SDK usage metrics
- `GET /api/v1/webhooks`: Get all webhooks
- `POST /api/v1/webhooks`: Create new webhook
- `PUT /api/v1/webhooks/{webhookId}`: Update webhook
- `DELETE /api/v1/webhooks/{webhookId}`: Delete webhook
- `POST /api/v1/webhooks/{webhookId}/test`: Test webhook

### Health and Monitoring Endpoints
- `GET /health`: Service health status
- `GET /health/live`: Liveness probe for Kubernetes
- `GET /health/ready`: Readiness probe for Kubernetes
- `GET /metrics`: Prometheus metrics endpoint
- `GET /api/v1/status`: Service status and uptime
- `GET /api/v1/info`: Service information and version

## WebSocket API

### Real-time Flag Updates
- `ws://host:port/ws/flags`: Real-time flag value updates
- `ws://host:port/ws/flags/{flagKey}`: Updates for specific flag
- `ws://host:port/ws/experiments`: Real-time experiment updates
- `ws://host:port/ws/events`: All service events stream

### WebSocket Message Types
- `flag_updated`: Flag configuration or value changed
- `flag_evaluation`: Flag evaluation result
- `experiment_started`: Experiment started notification
- `experiment_stopped`: Experiment stopped notification
- `segment_updated`: User segment updated
- `system_alert`: System-wide alerts and notifications

## Error Handling

### FeatureFlagError
- `FeatureFlagError(message)`: Create generic feature flag error
- `FeatureFlagError(message, code)`: Create error with specific code
- `getErrorCode()`: Get error code
- `getErrorDetails()`: Get detailed error information

### FlagNotFoundError
- `FlagNotFoundError(flagKey)`: Create flag not found error
- `getFlagKey()`: Get flag key from error

### EvaluationError
- `EvaluationError(flagKey, context, message)`: Create evaluation error
- `getFlagKey()`: Get flag key that failed evaluation
- `getContext()`: Get evaluation context
- `getEvaluationDetails()`: Get evaluation error details

### ExperimentError
- `ExperimentError(experimentKey, message)`: Create experiment error
- `getExperimentKey()`: Get experiment key from error
- `getExperimentPhase()`: Get experiment phase when error occurred

### TargetingError
- `TargetingError(rule, context, message)`: Create targeting rule error
- `getRule()`: Get failed targeting rule
- `getContext()`: Get evaluation context
- `getValidationErrors()`: Get rule validation errors

### ConfigurationError
- `ConfigurationError(configKey, message)`: Create configuration error
- `getConfigKey()`: Get configuration key from error
- `getConfigValue()`: Get invalid configuration value

### CacheError
- `CacheError(operation, key, message)`: Create cache operation error
- `getOperation()`: Get failed cache operation
- `getKey()`: Get cache key that caused error

### ValidationError
- `ValidationError(field, value, message)`: Create validation error
- `getField()`: Get field that failed validation
- `getValue()`: Get invalid value
- `getValidationRule()`: Get violated validation rule

### AuthorizationError
- `AuthorizationError(resource, action, message)`: Create authorization error
- `getResource()`: Get protected resource
- `getAction()`: Get attempted action
- `getRequiredPermissions()`: Get required permissions
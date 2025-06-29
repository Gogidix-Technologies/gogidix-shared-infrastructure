# Feature Flag Service Documentation

## Overview

The Feature Flag Service is a high-performance Node.js-based microservice that provides dynamic feature flag management, A/B testing capabilities, and configuration management for the Social E-commerce Ecosystem. It enables teams to safely deploy features, control feature rollouts, and perform experiments without requiring code deployments.

## Components

### Core Components
- **FeatureFlagManager**: Main service for managing feature flags and configurations
- **RuleEngine**: Sophisticated rule evaluation engine for complex targeting
- **ExperimentManager**: A/B testing and multivariate testing management
- **ConfigurationService**: Dynamic configuration and settings management
- **SegmentManager**: User and audience segmentation for targeted releases

### Flag Types
- **BooleanFlag**: Simple on/off feature toggles
- **NumericFlag**: Numeric configuration values and thresholds
- **StringFlag**: Text-based configuration and content flags
- **JSONFlag**: Complex configuration objects and structures
- **PercentageFlag**: Gradual rollout and sampling flags

### Targeting and Segmentation
- **UserTargeting**: Individual user-based targeting
- **GroupTargeting**: Team, organization, or role-based targeting
- **GeographicTargeting**: Location-based feature control
- **DeviceTargeting**: Platform and device-specific features
- **TimeBasedTargeting**: Scheduled feature activation and deactivation

### Integration Components
- **SDKManager**: Client SDK management and distribution
- **WebhookService**: Real-time flag change notifications
- **EventStreamingService**: Flag evaluation event streaming
- **CacheManager**: High-performance flag value caching
- **AuditService**: Flag change tracking and compliance logging

## Getting Started

To use the Feature Flag Service, follow these steps:

1. Configure the service with flag storage and caching
2. Set up user segmentation and targeting rules
3. Create and deploy feature flags
4. Integrate client SDKs for flag evaluation
5. Set up monitoring and analytics

## Examples

### Basic Feature Flag Service Setup

```javascript
import { FeatureFlagService } from '@exalt/feature-flag-service';
import { RedisCache } from '@exalt/cache-manager';
import { PostgreSQLStore } from '@exalt/data-store';
import { RuleEngine } from '@exalt/rule-engine';

class FeatureFlagServiceApplication {
    constructor() {
        this.app = express();
        this.setupStorage();
        this.setupCache();
        this.setupRuleEngine();
        this.setupFeatureFlagService();
        this.setupRoutes();
        this.setupMiddleware();
    }
    
    setupStorage() {
        this.flagStore = new PostgreSQLStore({
            host: process.env.DB_HOST,
            port: process.env.DB_PORT,
            database: process.env.DB_NAME,
            username: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            ssl: process.env.NODE_ENV === 'production',
            pool: {
                min: 5,
                max: 20,
                acquireTimeoutMillis: 30000,
                idleTimeoutMillis: 600000
            }
        });
        
        this.configStore = new PostgreSQLStore({
            host: process.env.CONFIG_DB_HOST,
            port: process.env.CONFIG_DB_PORT,
            database: process.env.CONFIG_DB_NAME,
            schema: 'configurations',
            connectionPool: 'shared'
        });
    }
    
    setupCache() {
        this.cache = new RedisCache({
            host: process.env.REDIS_HOST,
            port: process.env.REDIS_PORT,
            password: process.env.REDIS_PASSWORD,
            db: 1, // Dedicated database for feature flags
            keyPrefix: 'ff:',
            ttl: 300, // 5 minutes default TTL
            cluster: {
                enabled: process.env.NODE_ENV === 'production',
                nodes: process.env.REDIS_CLUSTER_NODES?.split(',') || []
            }
        });
        
        this.cacheStrategy = {
            flagValues: { ttl: 60 }, // 1 minute for flag values
            flagConfigs: { ttl: 300 }, // 5 minutes for configurations
            userSegments: { ttl: 900 }, // 15 minutes for user segments
            experimentResults: { ttl: 1800 } // 30 minutes for experiment data
        };
    }
    
    setupRuleEngine() {
        this.ruleEngine = new RuleEngine({
            operators: [
                'equals', 'not_equals', 'contains', 'not_contains',
                'starts_with', 'ends_with', 'regex_match',
                'greater_than', 'less_than', 'greater_equal', 'less_equal',
                'in_list', 'not_in_list', 'between', 'not_between',
                'is_true', 'is_false', 'is_null', 'is_not_null'
            ],
            customOperators: {
                'geo_within_radius': this.geoWithinRadiusOperator,
                'version_compare': this.versionCompareOperator,
                'time_window': this.timeWindowOperator,
                'percentage_bucket': this.percentageBucketOperator
            },
            optimizations: {
                enableCaching: true,
                enableShortCircuit: true,
                enableBatchEvaluation: true
            }
        });
    }
    
    setupFeatureFlagService() {
        this.featureFlagService = new FeatureFlagService({
            flagStore: this.flagStore,
            configStore: this.configStore,
            cache: this.cache,
            ruleEngine: this.ruleEngine,
            
            // Flag evaluation settings
            evaluation: {
                defaultTimeout: 100, // 100ms timeout
                enableAsyncEvaluation: true,
                enableBatchEvaluation: true,
                cacheResults: true,
                trackEvaluations: true
            },
            
            // Segmentation settings
            segmentation: {
                enableUserTargeting: true,
                enableGroupTargeting: true,
                enableGeoTargeting: true,
                enableDeviceTargeting: true,
                enableTimeTargeting: true,
                maxSegmentSize: 1000000 // 1M users per segment
            },
            
            // A/B testing settings
            experimentation: {
                enableExperiments: true,
                defaultSampleSize: 10000,
                significanceLevel: 0.05,
                powerLevel: 0.8,
                minimumDetectableEffect: 0.02 // 2%
            },
            
            // Audit and compliance
            audit: {
                enableChangeTracking: true,
                enableEvaluationLogging: process.env.NODE_ENV !== 'production',
                retentionPeriod: '90 days',
                encryptSensitiveData: true
            }
        });
    }
}
```

### Feature Flag Creation and Management

```javascript
// Create a boolean feature flag
const newUserOnboardingFlag = await featureFlagService.createFlag({
    key: 'new_user_onboarding_v2',
    name: 'New User Onboarding V2',
    description: 'Enable the redesigned user onboarding flow',
    type: 'boolean',
    defaultValue: false,
    
    // Targeting rules
    targeting: {
        enabled: true,
        rules: [
            {
                id: 'beta_users',
                description: 'Enable for beta users',
                conditions: [
                    {
                        attribute: 'user.group',
                        operator: 'in_list',
                        values: ['beta_testers', 'internal_users']
                    }
                ],
                percentage: 100,
                value: true
            },
            {
                id: 'gradual_rollout',
                description: 'Gradual rollout to all users',
                conditions: [
                    {
                        attribute: 'user.registration_date',
                        operator: 'greater_than',
                        value: '2024-01-01'
                    }
                ],
                percentage: 25, // 25% of matching users
                value: true
            }
        ],
        defaultRule: {
            percentage: 0,
            value: false
        }
    },
    
    // Metadata
    tags: ['onboarding', 'ui', 'experiment'],
    owner: 'product-team',
    environment: 'production',
    expirationDate: '2024-12-31'
});

// Create a configuration flag
const checkoutConfigFlag = await featureFlagService.createFlag({
    key: 'checkout_configuration',
    name: 'Checkout Configuration',
    description: 'Configuration settings for checkout process',
    type: 'json',
    defaultValue: {
        maxItems: 50,
        enableExpressCheckout: false,
        paymentMethods: ['credit_card', 'paypal'],
        shippingOptions: ['standard', 'express'],
        taxCalculation: 'inclusive'
    },
    
    targeting: {
        enabled: true,
        rules: [
            {
                id: 'premium_users',
                description: 'Enhanced checkout for premium users',
                conditions: [
                    {
                        attribute: 'user.subscription_tier',
                        operator: 'in_list',
                        values: ['premium', 'enterprise']
                    }
                ],
                percentage: 100,
                value: {
                    maxItems: 100,
                    enableExpressCheckout: true,
                    paymentMethods: ['credit_card', 'paypal', 'apple_pay', 'google_pay'],
                    shippingOptions: ['standard', 'express', 'overnight'],
                    taxCalculation: 'exclusive'
                }
            }
        ]
    }
});
```

### A/B Testing and Experimentation

```javascript
// Create an A/B test experiment
const checkoutButtonExperiment = await featureFlagService.createExperiment({
    key: 'checkout_button_color_test',
    name: 'Checkout Button Color A/B Test',
    description: 'Test different button colors for checkout conversion',
    
    // Experiment configuration
    type: 'ab_test',
    status: 'running',
    startDate: '2024-06-01',
    endDate: '2024-07-01',
    
    // Traffic allocation
    trafficAllocation: 0.2, // 20% of eligible users
    
    // Targeting criteria
    targeting: {
        conditions: [
            {
                attribute: 'user.country',
                operator: 'in_list',
                values: ['US', 'CA', 'GB', 'AU']
            },
            {
                attribute: 'session.pages_viewed',
                operator: 'greater_than',
                value: 3
            }
        ]
    },
    
    // Experiment variants
    variants: [
        {
            key: 'control',
            name: 'Control (Green Button)',
            description: 'Current green checkout button',
            allocation: 0.5, // 50% of experiment traffic
            flagOverrides: {
                'checkout_button_color': 'green',
                'checkout_button_text': 'Complete Purchase'
            }
        },
        {
            key: 'treatment',
            name: 'Treatment (Orange Button)',
            description: 'New orange checkout button',
            allocation: 0.5, // 50% of experiment traffic
            flagOverrides: {
                'checkout_button_color': 'orange',
                'checkout_button_text': 'Buy Now'
            }
        }
    ],
    
    // Success metrics
    metrics: [
        {
            key: 'checkout_conversion',
            name: 'Checkout Conversion Rate',
            type: 'conversion',
            primary: true,
            targetImprovement: 0.05 // 5% improvement target
        },
        {
            key: 'revenue_per_visitor',
            name: 'Revenue Per Visitor',
            type: 'numeric',
            primary: false
        }
    ],
    
    // Statistical settings
    statistics: {
        confidenceLevel: 0.95,
        minimumSampleSize: 1000,
        minimumDetectableEffect: 0.02,
        powerLevel: 0.8
    }
});
```

### Real-time Flag Evaluation

```javascript
// Evaluate flags for a user context
const evaluateUserFlags = async (userId, userContext = {}) => {
    const context = {
        user: {
            id: userId,
            email: userContext.email,
            group: userContext.group || 'default',
            country: userContext.country || 'US',
            registrationDate: userContext.registrationDate,
            subscriptionTier: userContext.subscriptionTier || 'free',
            ...userContext.customAttributes
        },
        device: {
            type: userContext.deviceType || 'web',
            platform: userContext.platform || 'unknown',
            version: userContext.appVersion
        },
        session: {
            id: userContext.sessionId,
            timestamp: new Date().toISOString(),
            pagesViewed: userContext.pagesViewed || 0
        }
    };
    
    // Evaluate multiple flags efficiently
    const flagKeys = [
        'new_user_onboarding_v2',
        'checkout_configuration',
        'product_recommendations_enabled',
        'advanced_search_features',
        'mobile_app_redesign'
    ];
    
    const flagValues = await featureFlagService.evaluateFlags(flagKeys, context);
    
    return {
        userId,
        timestamp: context.session.timestamp,
        flags: flagValues,
        experiments: await featureFlagService.getActiveExperiments(context)
    };
};

// Batch evaluation for multiple users
const evaluateBatchFlags = async (userContexts, flagKeys) => {
    const results = await featureFlagService.evaluateBatchFlags(
        userContexts.map(ctx => ({
            user: { id: ctx.userId, ...ctx },
            timestamp: new Date().toISOString()
        })),
        flagKeys
    );
    
    return results;
};
```

### Flag Management and Operations

```javascript
// Update flag targeting rules
const updateFlagTargeting = async (flagKey, newRules) => {
    await featureFlagService.updateFlag(flagKey, {
        targeting: {
            enabled: true,
            rules: newRules,
            lastModified: new Date().toISOString(),
            modifiedBy: 'admin@exalt.com'
        }
    });
    
    // Invalidate cache for immediate effect
    await featureFlagService.invalidateCache(flagKey);
    
    // Send webhook notification
    await featureFlagService.notifyFlagChange(flagKey, 'targeting_updated');
};

// Gradual rollout management
const updateRolloutPercentage = async (flagKey, newPercentage) => {
    const flag = await featureFlagService.getFlag(flagKey);
    
    if (flag.targeting?.rules?.length > 0) {
        // Update the main rollout rule
        const rolloutRule = flag.targeting.rules.find(rule => 
            rule.id === 'gradual_rollout'
        );
        
        if (rolloutRule) {
            rolloutRule.percentage = newPercentage;
            
            await featureFlagService.updateFlag(flagKey, {
                targeting: flag.targeting
            });
            
            console.log(`Updated ${flagKey} rollout to ${newPercentage}%`);
        }
    }
};

// Flag lifecycle management
const archiveExpiredFlags = async () => {
    const expiredFlags = await featureFlagService.getExpiredFlags();
    
    for (const flag of expiredFlags) {
        await featureFlagService.archiveFlag(flag.key, {
            reason: 'expired',
            archivedBy: 'system',
            archivedAt: new Date().toISOString()
        });
        
        console.log(`Archived expired flag: ${flag.key}`);
    }
};
```

### Analytics and Reporting

```javascript
// Flag evaluation analytics
const getFlagAnalytics = async (flagKey, dateRange) => {
    const analytics = await featureFlagService.getFlagAnalytics(flagKey, {
        startDate: dateRange.start,
        endDate: dateRange.end,
        groupBy: 'day',
        includeSegmentation: true
    });
    
    return {
        totalEvaluations: analytics.evaluationCount,
        uniqueUsers: analytics.uniqueUserCount,
        trueEvaluations: analytics.trueCount,
        falseEvaluations: analytics.falseCount,
        conversionRate: analytics.trueCount / analytics.evaluationCount,
        
        segmentBreakdown: analytics.segments.map(segment => ({
            segmentName: segment.name,
            evaluations: segment.count,
            percentage: segment.count / analytics.evaluationCount
        })),
        
        dailyTrend: analytics.dailyBreakdown.map(day => ({
            date: day.date,
            evaluations: day.count,
            uniqueUsers: day.uniqueUsers,
            trueRate: day.trueCount / day.count
        }))
    };
};

// Experiment results analysis
const getExperimentResults = async (experimentKey) => {
    const experiment = await featureFlagService.getExperiment(experimentKey);
    const results = await featureFlagService.getExperimentResults(experimentKey);
    
    return {
        experiment: {
            key: experiment.key,
            name: experiment.name,
            status: experiment.status,
            duration: experiment.endDate - experiment.startDate
        },
        
        variants: results.variants.map(variant => ({
            key: variant.key,
            name: variant.name,
            sampleSize: variant.sampleSize,
            metrics: variant.metrics.map(metric => ({
                key: metric.key,
                value: metric.value,
                confidenceInterval: metric.confidenceInterval,
                pValue: metric.pValue,
                statisticalSignificance: metric.pValue < 0.05
            }))
        })),
        
        recommendations: results.recommendations,
        confidence: results.overallConfidence,
        status: results.statisticalStatus
    };
};
```

## Integration Examples

### Client SDK Integration

```javascript
// Browser SDK integration
import { FeatureFlagClient } from '@exalt/feature-flag-client';

const flagClient = new FeatureFlagClient({
    apiKey: 'your-client-api-key',
    baseUrl: 'https://feature-flags.exalt.com',
    environment: 'production',
    
    // User context
    user: {
        id: 'user-123',
        email: 'user@example.com',
        group: 'premium_users'
    },
    
    // Caching and performance
    enableCaching: true,
    cacheTimeout: 60000, // 1 minute
    enableStreaming: true, // Real-time updates
    
    // Error handling
    defaultValues: {
        'new_user_onboarding_v2': false,
        'checkout_configuration': {
            maxItems: 50,
            enableExpressCheckout: false
        }
    }
});

// Initialize and start receiving updates
await flagClient.initialize();

// Evaluate flags
const showNewOnboarding = flagClient.getBooleanFlag(
    'new_user_onboarding_v2', 
    false
);

const checkoutConfig = flagClient.getJSONFlag(
    'checkout_configuration',
    { maxItems: 50 }
);

// Track events for experiments
flagClient.track('checkout_started', {
    orderId: 'order-456',
    amount: 99.99
});

flagClient.track('checkout_completed', {
    orderId: 'order-456',
    amount: 99.99,
    conversionTime: 120 // seconds
});
```

### Server-side Integration

```javascript
// Express.js middleware
const featureFlagMiddleware = (req, res, next) => {
    const userContext = {
        user: {
            id: req.user?.id,
            email: req.user?.email,
            group: req.user?.group,
            country: req.headers['cf-ipcountry'] || 'US'
        },
        device: {
            type: req.headers['user-agent']?.includes('Mobile') ? 'mobile' : 'desktop',
            platform: req.headers['user-agent']
        },
        session: {
            id: req.sessionID,
            timestamp: new Date().toISOString()
        }
    };
    
    req.featureFlags = flagClient.createEvaluationContext(userContext);
    next();
};

// Route handler using feature flags
app.get('/api/products', featureFlagMiddleware, async (req, res) => {
    const enableAdvancedSearch = await req.featureFlags.getBooleanFlag(
        'advanced_search_features',
        false
    );
    
    const productRecommendations = await req.featureFlags.getBooleanFlag(
        'product_recommendations_enabled',
        false
    );
    
    const searchConfig = await req.featureFlags.getJSONFlag(
        'search_configuration',
        { maxResults: 20, enableFilters: true }
    );
    
    // Implement logic based on flag values
    const products = await productService.searchProducts({
        query: req.query.q,
        enableAdvancedSearch,
        enableRecommendations: productRecommendations,
        config: searchConfig
    });
    
    res.json(products);
});
```

## Best Practices

### Flag Naming Conventions
- Use descriptive, hierarchical naming: `feature.component.variant`
- Include version numbers for major changes: `checkout_flow_v2`
- Use consistent prefixes for categories: `exp_` for experiments, `config_` for configurations

### Targeting and Segmentation
- Keep targeting rules simple and maintainable
- Use percentage rollouts for gradual deployment
- Combine multiple conditions carefully to avoid overly complex logic
- Test targeting rules in staging environments first

### Performance Optimization
- Enable caching for frequently evaluated flags
- Use batch evaluation for multiple flags
- Implement proper timeout and fallback mechanisms
- Monitor flag evaluation performance metrics

### Security and Compliance
- Implement proper access controls for flag management
- Audit all flag changes and evaluations
- Use encrypted storage for sensitive configuration data
- Follow data privacy regulations for user targeting

## Monitoring and Alerting

### Key Metrics to Monitor
- Flag evaluation latency and throughput
- Cache hit rates and performance
- Error rates for flag evaluations
- Flag change frequency and impact
- Experiment statistical power and significance

### Alerting Scenarios
- Flag evaluation errors or timeouts
- Unexpected changes in flag evaluation patterns
- Experiment reaching statistical significance
- Cache performance degradation
- Service availability issues
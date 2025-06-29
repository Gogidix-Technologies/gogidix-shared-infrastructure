# Currency Exchange Service Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the Currency Exchange Service in production environments.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Monitoring and Alerting](#monitoring-and-alerting)
3. [Performance Management](#performance-management)
4. [Backup and Recovery](#backup-and-recovery)
5. [Security Operations](#security-operations)
6. [Troubleshooting](#troubleshooting)
7. [Maintenance Procedures](#maintenance-procedures)
8. [Disaster Recovery](#disaster-recovery)

## Service Operations

### Service Management

#### Starting the Service

**Production Environment:**
```bash
# Using systemd
sudo systemctl start currency-exchange-service

# Using Docker
docker-compose -f docker-compose.prod.yml up -d currency-exchange-service

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n shared-infrastructure
```

#### Stopping the Service

```bash
# Graceful shutdown (allows 30s for cleanup)
sudo systemctl stop currency-exchange-service

# Force stop if needed
sudo systemctl kill currency-exchange-service

# Kubernetes
kubectl delete deployment currency-exchange-service -n shared-infrastructure
```

#### Service Status Checks

```bash
# System status
sudo systemctl status currency-exchange-service

# Health endpoints
curl http://localhost:3402/health
curl http://localhost:3402/health/ready
curl http://localhost:3402/health/live

# Provider status
curl http://localhost:3402/api/v1/providers/status
```

### Configuration Management

#### Environment-Specific Configurations

**Production (`config/production.js`):**
```javascript
module.exports = {
  server: {
    port: 3402,
    host: '0.0.0.0'
  },
  database: {
    mongodb: {
      uri: process.env.MONGODB_URI,
      options: {
        maxPoolSize: 20,
        minPoolSize: 5,
        maxIdleTimeMS: 300000,
        serverSelectionTimeoutMS: 5000
      }
    }
  },
  cache: {
    redis: {
      host: process.env.REDIS_HOST,
      port: process.env.REDIS_PORT,
      maxRetriesPerRequest: 3,
      retryDelayOnFailover: 100,
      lazyConnect: true
    }
  },
  logging: {
    level: 'info',
    file: '/var/log/currency-exchange/service.log',
    maxSize: '100m',
    maxFiles: 10
  }
};
```

#### Runtime Configuration Updates

```bash
# Update rate refresh interval
curl -X PUT http://localhost:3402/api/v1/config/rate-refresh-interval \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"interval": "*/10 * * * *"}'

# Update provider configuration
curl -X PUT http://localhost:3402/api/v1/config/providers \
  -H "Content-Type: application/json" \
  -d '{"primary": "open-exchange-rates", "fallback": "fixer-io"}'
```

## Monitoring and Alerting

### Key Performance Indicators (KPIs)

#### Service Health Metrics

| Metric | Normal Range | Alert Threshold | Critical Threshold |
|--------|--------------|-----------------|-------------------|
| Response Time | < 100ms | > 300ms | > 500ms |
| CPU Usage | < 60% | > 80% | > 90% |
| Memory Usage | < 70% | > 85% | > 95% |
| Conversion Rate | > 99% | < 98% | < 95% |
| Provider Uptime | > 99% | < 98% | < 95% |

#### Business Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Conversion Requests/min | 100-1000 | > 2000 |
| Rate Update Frequency | Every 15min | > 30min |
| Cache Hit Rate | > 90% | < 80% |
| Provider Response Time | < 2s | > 5s |

### Custom Metrics

#### Prometheus Metrics Configuration

```javascript
// metrics/prometheus.js
const prometheus = require('prom-client');

// Currency conversion metrics
const conversionCounter = new prometheus.Counter({
  name: 'currency_conversions_total',
  help: 'Total number of currency conversions',
  labelNames: ['from_currency', 'to_currency', 'status']
});

const conversionDuration = new prometheus.Histogram({
  name: 'currency_conversion_duration_seconds',
  help: 'Duration of currency conversion operations',
  buckets: [0.001, 0.01, 0.1, 0.5, 1, 2, 5]
});

// Rate provider metrics
const providerResponseTime = new prometheus.Histogram({
  name: 'currency_provider_response_duration_seconds',
  help: 'Response time of currency providers',
  labelNames: ['provider'],
  buckets: [0.1, 0.5, 1, 2, 5, 10]
});

const rateUpdateCounter = new prometheus.Counter({
  name: 'currency_rate_updates_total',
  help: 'Total number of rate updates',
  labelNames: ['provider', 'status']
});
```

#### Key Metrics to Monitor

```promql
# Conversion request rate
rate(currency_conversions_total[5m])

# Average conversion response time
histogram_quantile(0.95, currency_conversion_duration_seconds_bucket)

# Provider availability
up{job="currency-exchange-service"}

# Cache hit rate
currency_cache_hits_total / (currency_cache_hits_total + currency_cache_misses_total)

# Rate update success rate
rate(currency_rate_updates_total{status="success"}[5m])
```

### Grafana Dashboards

#### Dashboard Configuration

```json
{
  "dashboard": {
    "title": "Currency Exchange Service - Overview",
    "panels": [
      {
        "title": "Conversion Requests/min",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(currency_conversions_total[1m]) * 60"
          }
        ]
      },
      {
        "title": "Provider Response Times",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, currency_provider_response_duration_seconds_bucket)"
          }
        ]
      },
      {
        "title": "Cache Performance",
        "type": "stat",
        "targets": [
          {
            "expr": "currency_cache_hits_total / (currency_cache_hits_total + currency_cache_misses_total) * 100"
          }
        ]
      }
    ]
  }
}
```

### Alert Rules

#### Prometheus Alert Configuration

```yaml
# alert-rules.yml
groups:
  - name: currency-exchange-service
    rules:
      - alert: CurrencyServiceHighResponseTime
        expr: histogram_quantile(0.95, currency_conversion_duration_seconds_bucket) > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time for currency conversions"
          description: "95th percentile response time is {{ $value }}s"

      - alert: CurrencyProviderDown
        expr: up{job="currency-exchange-service"} == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Currency provider is down"
          description: "Provider {{ $labels.provider }} is not responding"

      - alert: CurrencyRateUpdateFailed
        expr: increase(currency_rate_updates_total{status="error"}[15m]) > 3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Currency rate update failures"
          description: "{{ $value }} rate update failures in the last 15 minutes"
```

## Performance Management

### Performance Optimization

#### Node.js Tuning

```bash
# Production Node.js settings
export NODE_OPTIONS="
  --max-old-space-size=2048
  --max-semi-space-size=128
  --optimize-for-size
"

# Enable V8 profiling
export NODE_OPTIONS="$NODE_OPTIONS --prof"

# Enable garbage collection logging
export NODE_OPTIONS="$NODE_OPTIONS --trace-gc"
```

#### Database Connection Optimization

```javascript
// config/database.js
const mongoOptions = {
  maxPoolSize: 20,
  minPoolSize: 5,
  maxIdleTimeMS: 300000,
  serverSelectionTimeoutMS: 5000,
  socketTimeoutMS: 45000,
  bufferMaxEntries: 0,
  useNewUrlParser: true,
  useUnifiedTopology: true
};
```

#### Redis Cache Optimization

```javascript
// config/cache.js
const redisConfig = {
  host: process.env.REDIS_HOST,
  port: process.env.REDIS_PORT,
  maxRetriesPerRequest: 3,
  retryDelayOnFailover: 100,
  enableReadyCheck: false,
  maxLoadingTimeout: 1000,
  lazyConnect: true,
  keepAlive: 30000
};
```

### Load Testing

#### Performance Test Scripts

```bash
# Artillery.js load test
artillery run currency-exchange-load-test.yml

# k6 performance test
k6 run currency-exchange-k6-test.js

# Apache Bench test
ab -n 10000 -c 100 http://localhost:3402/api/v1/rates/USD
```

## Backup and Recovery

### Database Backup Procedures

#### Automated Backup Script

```bash
#!/bin/bash
# backup-mongodb.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/currency-exchange"
DB_NAME="currency_exchange_db"

# Create backup
mongodump --uri="$MONGODB_URI" --out="$BACKUP_DIR/mongodb_$DATE"

# Compress backup
tar -czf "$BACKUP_DIR/mongodb_$DATE.tar.gz" -C "$BACKUP_DIR" "mongodb_$DATE"
rm -rf "$BACKUP_DIR/mongodb_$DATE"

# Remove backups older than 7 days
find "$BACKUP_DIR" -name "*.tar.gz" -mtime +7 -delete

echo "MongoDB backup completed: mongodb_$DATE.tar.gz"
```

#### Cache Backup

```bash
#!/bin/bash
# backup-redis.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/currency-exchange"

# Create Redis snapshot
redis-cli --rdb "$BACKUP_DIR/redis_$DATE.rdb"

# Compress snapshot
gzip "$BACKUP_DIR/redis_$DATE.rdb"

echo "Redis backup completed: redis_$DATE.rdb.gz"
```

#### Backup Schedule

```cron
# Crontab entries for automated backups
# MongoDB backup every 6 hours
0 */6 * * * /opt/currency-exchange/scripts/backup-mongodb.sh >> /var/log/backup.log 2>&1

# Redis backup every 2 hours
0 */2 * * * /opt/currency-exchange/scripts/backup-redis.sh >> /var/log/backup.log 2>&1
```

### Recovery Procedures

#### Database Recovery

```bash
# Stop the service
sudo systemctl stop currency-exchange-service

# Restore MongoDB
tar -xzf mongodb_20231201_020000.tar.gz
mongorestore --uri="$MONGODB_URI" --drop mongodb_20231201_020000/

# Restore Redis
redis-cli FLUSHALL
redis-cli --rdb redis_20231201_020000.rdb

# Restart service
sudo systemctl start currency-exchange-service
```

## Security Operations

### Security Monitoring

#### Access Log Analysis

```bash
# Monitor API access patterns
tail -f /var/log/currency-exchange/access.log | \
  grep -E "(suspicious|error|blocked)"

# Track rate limiting
grep "RATE_LIMIT" /var/log/currency-exchange/service.log | tail -20
```

#### Security Audit Commands

```bash
# Check for unusual conversion patterns
grep "LARGE_CONVERSION" /var/log/currency-exchange/audit.log

# Monitor provider key usage
grep "API_KEY_USAGE" /var/log/currency-exchange/security.log
```

### API Key Management

```bash
# Rotate provider API keys
curl -X PUT http://localhost:3402/api/v1/admin/providers/rotate-key \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"provider": "open-exchange-rates", "newKey": "new-api-key"}'

# Check key expiration
curl http://localhost:3402/api/v1/admin/providers/key-status \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Memory Usage

**Symptoms:**
- Memory usage consistently above 85%
- Slow response times
- Out of memory errors

**Investigation:**
```bash
# Generate heap snapshot
kill -USR2 <process_id>

# Analyze memory usage
node --inspect index.js
```

**Solutions:**
- Increase Node.js heap size: `--max-old-space-size=4096`
- Optimize cache TTL settings
- Review for memory leaks in custom code

#### 2. Provider API Issues

**Symptoms:**
- Rate update failures
- Stale exchange rates
- Provider timeout errors

**Investigation:**
```bash
# Check provider status
curl http://localhost:3402/api/v1/providers/status

# Test provider connectivity
curl -X POST http://localhost:3402/api/v1/admin/test-provider \
  -d '{"provider": "open-exchange-rates"}'
```

**Solutions:**
- Switch to backup provider
- Check API key validity
- Increase timeout settings

#### 3. Cache Performance Issues

**Symptoms:**
- Low cache hit rate
- Slow conversion responses
- Redis connection errors

**Investigation:**
```bash
# Check Redis status
redis-cli INFO memory
redis-cli INFO stats

# Monitor cache hit rate
curl http://localhost:3402/api/v1/cache/stats
```

### Log Analysis

#### Important Log Patterns

```bash
# Conversion errors
grep -E "CONVERSION_ERROR|INVALID_CURRENCY" /var/log/currency-exchange/service.log

# Provider issues
grep -E "PROVIDER_ERROR|TIMEOUT|RATE_LIMIT" /var/log/currency-exchange/service.log

# Performance issues
grep -E "SLOW_RESPONSE|HIGH_MEMORY" /var/log/currency-exchange/service.log
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-maintenance.sh

echo "=== Currency Exchange Daily Report ===" > /tmp/maintenance.log

# Check service health
curl -f http://localhost:3402/health >> /tmp/maintenance.log

# Check provider status
curl http://localhost:3402/api/v1/providers/status >> /tmp/maintenance.log

# Check cache performance
curl http://localhost:3402/api/v1/cache/stats >> /tmp/maintenance.log

# Check recent errors
tail -100 /var/log/currency-exchange/service.log | grep ERROR >> /tmp/maintenance.log

# Send report
mail -s "Currency Exchange Daily Report" currency-team@exalt.com < /tmp/maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-maintenance.sh

# Update dependencies
npm audit fix --force

# Cleanup old logs
find /var/log/currency-exchange -name "*.log.*" -mtime +7 -delete

# Optimize database
mongo --eval "db.exchangeRates.reIndex()" currency_exchange_db

# Clear old cache entries
redis-cli EVAL "return redis.call('del', unpack(redis.call('keys', 'old:*')))" 0
```

### Update Procedures

#### Rolling Updates (Kubernetes)

```bash
# Update deployment with new image
kubectl set image deployment/currency-exchange-service \
  currency-exchange-service=currency-exchange-service:v1.2.0 \
  -n shared-infrastructure

# Monitor rollout
kubectl rollout status deployment/currency-exchange-service -n shared-infrastructure

# Rollback if necessary
kubectl rollout undo deployment/currency-exchange-service -n shared-infrastructure
```

## Disaster Recovery

### Recovery Time Objectives (RTO)

| Component | RTO Target | Recovery Procedure |
|-----------|------------|-------------------|
| Service Instance | 2 minutes | Auto-scaling/Health check restart |
| Database | 10 minutes | Restore from backup |
| Cache | 5 minutes | Rebuild from providers |
| Complete System | 15 minutes | Full disaster recovery |

### Recovery Point Objectives (RPO)

| Data Type | RPO Target | Backup Frequency |
|-----------|------------|------------------|
| Exchange Rates | 15 minutes | Continuous caching |
| Historical Data | 6 hours | Every 6 hours |
| Configuration | 1 hour | Real-time Git sync |

### Emergency Procedures

1. **Provider Failure**: Automatic failover to backup providers
2. **Database Failure**: Restore from latest backup
3. **Cache Failure**: Rebuild cache from primary providers
4. **Complete Service Failure**: Full disaster recovery

### Emergency Contacts

- **Primary On-Call**: +1-555-0125 (currency-team-primary@exalt.com)
- **Secondary On-Call**: +1-555-0126 (currency-team-secondary@exalt.com)
- **DevOps Team**: devops-emergency@exalt.com
- **External Provider Support**: provider-support@exalt.com

## Compliance and Auditing

### Financial Data Compliance

```javascript
// Audit configuration
const auditConfig = {
  enabled: true,
  events: [
    'CONVERSION_REQUEST',
    'RATE_UPDATE',
    'PROVIDER_SWITCH',
    'CACHE_MISS',
    'ERROR_RESPONSE'
  ],
  retentionDays: 365,
  encryptLogs: true
};
```

### Compliance Checks

```bash
# Financial compliance audit
./scripts/financial-compliance-check.sh

# Provider agreement verification
./scripts/verify-provider-agreements.sh

# Data retention audit
./scripts/data-retention-audit.sh
```

## Support and Escalation

### Support Levels

1. **L1 Support**: Basic service restart, health checks
2. **L2 Support**: Configuration changes, provider issues
3. **L3 Support**: Code changes, architecture decisions

### Escalation Matrix

| Issue Severity | Initial Response | Resolution Target | Escalation Path |
|----------------|------------------|-------------------|-----------------|
| Critical | 5 minutes | 30 minutes | L1 → L2 → L3 → Management |
| High | 15 minutes | 2 hours | L1 → L2 → L3 |
| Medium | 1 hour | 8 hours | L1 → L2 |
| Low | 4 hours | 48 hours | L1 |

---

*Last Updated: 2024-06-25*
*Document Version: 1.0*
*Review Schedule: Monthly*
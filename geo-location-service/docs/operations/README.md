# Geo-Location Service Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the Geo-Location Service in production environments.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Monitoring and Alerting](#monitoring-and-alerting)
3. [Performance Management](#performance-management)
4. [Provider Management](#provider-management)
5. [Cache Management](#cache-management)
6. [Backup and Recovery](#backup-recovery)
7. [Security Operations](#security-operations)
8. [Troubleshooting](#troubleshooting)
9. [Maintenance Procedures](#maintenance-procedures)
10. [Disaster Recovery](#disaster-recovery)

## Service Operations

### Service Management

#### Starting the Service

**Production Environment:**
```bash
# Using systemd
sudo systemctl start geo-location-service

# Using Docker
docker-compose -f docker-compose.prod.yml up -d geo-location-service

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n shared-infrastructure
```

#### Stopping the Service

```bash
# Graceful shutdown (allows 30s for processing completion)
sudo systemctl stop geo-location-service

# Force stop if needed
sudo systemctl kill geo-location-service

# Kubernetes
kubectl delete deployment geo-location-service -n shared-infrastructure
```

#### Service Status Checks

```bash
# System status
sudo systemctl status geo-location-service

# Health endpoints
curl http://localhost:8407/actuator/health
curl http://localhost:8407/actuator/health/readiness
curl http://localhost:8407/actuator/health/liveness

# Provider status
curl http://localhost:8407/api/v1/admin/providers/status

# Cache status
curl http://localhost:8407/api/v1/admin/cache/status
```

### Configuration Management

#### Environment-Specific Configurations

**Production (`application-prod.yml`):**
```yaml
spring:
  profiles: prod
  datasource:
    hikari:
      maximum-pool-size: 25
      minimum-idle: 10
      idle-timeout: 300000
      max-lifetime: 1200000
  redis:
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 8

geo-location:
  providers:
    google-maps:
      rate-limit: 50000
      timeout: 5s
      retry-attempts: 3
    openstreetmap:
      rate-limit: 100000
      timeout: 10s
      retry-attempts: 2
  
  cache:
    geocode-ttl: 86400s
    route-ttl: 3600s
    boundary-ttl: 604800s

logging:
  level:
    root: INFO
    com.exalt.geolocation: INFO
  file:
    name: /var/log/geo-location/application.log
    max-file-size: 100MB
    max-history: 30
```

#### Runtime Configuration Updates

```bash
# Update provider settings
curl -X PUT http://localhost:8407/api/v1/admin/providers/config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "primaryProvider": "google-maps",
    "fallbackProviders": ["openstreetmap", "here-maps"],
    "rateLimits": {"google-maps": 45000}
  }'

# Update cache settings
curl -X PUT http://localhost:8407/api/v1/admin/cache/config \
  -H "Content-Type: application/json" \
  -d '{"geocodeTTL": "24h", "routeTTL": "2h"}'
```

## Monitoring and Alerting

### Key Performance Indicators (KPIs)

#### Service Health Metrics

| Metric | Normal Range | Alert Threshold | Critical Threshold |
|--------|--------------|-----------------|-------------------|
| Response Time | < 200ms | > 500ms | > 1s |
| CPU Usage | < 70% | > 85% | > 95% |
| Memory Usage | < 75% | > 90% | > 95% |
| Request Success Rate | > 99% | < 95% | < 90% |
| Provider Uptime | > 99% | < 98% | < 95% |

#### Geographic Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Geocoding Accuracy | > 95% | < 90% |
| Route Calculation Time | < 500ms | > 2s |
| Cache Hit Rate | > 80% | < 60% |
| Provider Error Rate | < 1% | > 5% |

### Custom Metrics

#### Prometheus Metrics Configuration

```java
// metrics/GeoLocationMetrics.java
@Component
public class GeoLocationMetrics {
    
    private final Counter geocodeRequests = Counter.builder("geocode_requests_total")
            .description("Total geocoding requests")
            .labelNames("provider", "status", "region")
            .register(Metrics.globalRegistry);
    
    private final Timer geocodeDuration = Timer.builder("geocode_duration_seconds")
            .description("Geocoding request duration")
            .labelNames("provider", "cache_hit")
            .register(Metrics.globalRegistry);
    
    private final Counter routeCalculations = Counter.builder("route_calculations_total")
            .description("Total route calculations")
            .labelNames("travel_mode", "status")
            .register(Metrics.globalRegistry);
    
    private final Gauge cacheHitRate = Gauge.builder("cache_hit_rate_percentage")
            .description("Cache hit rate")
            .labelNames("cache_type")
            .register(Metrics.globalRegistry);
    
    private final Counter providerErrors = Counter.builder("provider_errors_total")
            .description("Provider API errors")
            .labelNames("provider", "error_type")
            .register(Metrics.globalRegistry);
}
```

#### Key Metrics to Monitor

```promql
# Geocoding request rate
rate(geocode_requests_total[5m])

# Geocoding latency
histogram_quantile(0.95, geocode_duration_seconds_bucket)

# Provider error rate
rate(provider_errors_total[5m]) / rate(geocode_requests_total[5m]) * 100

# Cache performance
cache_hit_rate_percentage{cache_type="geocode"}

# Route calculation performance
histogram_quantile(0.95, route_calculation_duration_seconds_bucket)
```

### Grafana Dashboards

#### Dashboard Configuration

```json
{
  "dashboard": {
    "title": "Geo-Location Service - Operations",
    "panels": [
      {
        "title": "Request Volume",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(geocode_requests_total[5m]) * 60"
          }
        ]
      },
      {
        "title": "Provider Performance",
        "type": "table",
        "targets": [
          {
            "expr": "avg_over_time(geocode_duration_seconds{provider=\"google-maps\"}[5m])"
          }
        ]
      },
      {
        "title": "Cache Hit Rates",
        "type": "stat",
        "targets": [
          {
            "expr": "cache_hit_rate_percentage"
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
  - name: geo-location-service
    rules:
      - alert: GeoLocationHighLatency
        expr: histogram_quantile(0.95, geocode_duration_seconds_bucket) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High geocoding latency"
          description: "95th percentile latency is {{ $value }}s"

      - alert: GeoLocationProviderDown
        expr: up{job="geo-location-service"} == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Geo-location provider is down"

      - alert: GeoLocationLowCacheHitRate
        expr: cache_hit_rate_percentage < 60
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Low cache hit rate"
          description: "Cache hit rate is {{ $value }}%"
```

## Performance Management

### Performance Optimization

#### JVM Tuning

```bash
# Production JVM settings
export JAVA_OPTS="
  -Xmx4g
  -Xms2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/geo-location/heapdumps/
"
```

#### Database Optimization

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 25
      minimum-idle: 10
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      validation-timeout: 5000
```

#### Cache Optimization

```yaml
geo-location:
  cache:
    redis:
      connection-pool-size: 20
      max-total: 100
      max-idle: 20
      min-idle: 5
    levels:
      l1-ttl: 300s
      l2-ttl: 3600s
      l3-ttl: 86400s
```

## Provider Management

### Provider Health Monitoring

```bash
# Check all provider status
curl -X GET http://localhost:8407/api/v1/admin/providers/health \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Test specific provider
curl -X POST http://localhost:8407/api/v1/admin/providers/test \
  -H "Content-Type: application/json" \
  -d '{"provider": "google-maps", "testType": "geocode"}'

# Get provider usage statistics
curl -X GET http://localhost:8407/api/v1/admin/providers/usage \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Provider Failover Management

```bash
# Manual provider failover
curl -X POST http://localhost:8407/api/v1/admin/providers/failover \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "fromProvider": "google-maps",
    "toProvider": "openstreetmap",
    "reason": "planned_maintenance"
  }'

# Check failover status
curl -X GET http://localhost:8407/api/v1/admin/providers/failover-status \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Rate Limit Management

```bash
# Check rate limit status
curl -X GET http://localhost:8407/api/v1/admin/rate-limits \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update rate limits
curl -X PUT http://localhost:8407/api/v1/admin/rate-limits \
  -H "Content-Type: application/json" \
  -d '{
    "google-maps": {"dailyLimit": 45000, "perSecondLimit": 50},
    "openstreetmap": {"dailyLimit": 90000, "perSecondLimit": 100}
  }'
```

## Cache Management

### Cache Operations

```bash
# Check cache statistics
curl -X GET http://localhost:8407/api/v1/admin/cache/stats \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Clear specific cache
curl -X DELETE http://localhost:8407/api/v1/admin/cache/clear \
  -H "Content-Type: application/json" \
  -d '{"cacheType": "geocode", "pattern": "city:*"}'

# Warm cache with common requests
curl -X POST http://localhost:8407/api/v1/admin/cache/warm \
  -H "Content-Type: application/json" \
  -d '{"regions": ["US", "EU"], "requestTypes": ["geocode", "route"]}'
```

### Cache Performance Monitoring

```bash
# Monitor cache hit rates
redis-cli INFO stats | grep hits

# Check cache memory usage
redis-cli INFO memory

# Monitor cache key expiration
redis-cli --scan --pattern "*geo:*" | wc -l
```

## Backup and Recovery

### Database Backup Procedures

#### Automated Backup Script

```bash
#!/bin/bash
# backup-geo-location.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/geo-location"
DB_NAME="geo_location_db"

# Create database backup
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME > \
  $BACKUP_DIR/db_$DATE.sql

# Backup spatial data separately
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME -t boundaries > \
  $BACKUP_DIR/boundaries_$DATE.sql

# Compress backups
gzip $BACKUP_DIR/db_$DATE.sql
gzip $BACKUP_DIR/boundaries_$DATE.sql

echo "Backup completed: db_$DATE.sql.gz"
```

### Cache Backup

```bash
#!/bin/bash
# backup-cache.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/geo-location"

# Create Redis snapshot
redis-cli BGSAVE
cp /var/lib/redis/dump.rdb $BACKUP_DIR/cache_$DATE.rdb
gzip $BACKUP_DIR/cache_$DATE.rdb

echo "Cache backup completed: cache_$DATE.rdb.gz"
```

## Security Operations

### API Security Monitoring

```bash
# Monitor API key usage
grep "API_KEY_USAGE" /var/log/geo-location/audit.log | \
  tail -20

# Check for suspicious location requests
grep "SUSPICIOUS_PATTERN" /var/log/geo-location/security.log

# Monitor rate limit violations
grep "RATE_LIMIT_EXCEEDED" /var/log/geo-location/application.log
```

### Location Data Privacy

```bash
# Generate privacy compliance report
curl -X GET http://localhost:8407/api/v1/admin/privacy/report \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Anonymize location data
curl -X POST http://localhost:8407/api/v1/admin/privacy/anonymize \
  -H "Content-Type: application/json" \
  -d '{"olderThan": "30d", "level": "high"}'
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Response Times

**Symptoms:**
- Slow geocoding responses
- Route calculation timeouts
- User complaints about performance

**Investigation:**
```bash
# Check provider response times
curl http://localhost:8407/api/v1/admin/providers/performance

# Monitor database query performance
grep "SLOW_QUERY" /var/log/geo-location/application.log

# Check cache performance
curl http://localhost:8407/api/v1/admin/cache/performance
```

**Solutions:**
- Scale up service instances
- Optimize database queries
- Improve cache hit rates
- Switch to faster providers

#### 2. Provider API Failures

**Symptoms:**
- Geocoding failures
- Provider timeout errors
- High error rates

**Investigation:**
```bash
# Test provider connectivity
curl -X POST http://localhost:8407/api/v1/admin/providers/test-all

# Check provider error logs
grep "PROVIDER_ERROR" /var/log/geo-location/application.log

# Monitor provider quotas
curl http://localhost:8407/api/v1/admin/providers/quotas
```

**Solutions:**
- Switch to backup providers
- Increase timeout values
- Check API credentials
- Contact provider support

#### 3. Cache Performance Issues

**Symptoms:**
- Low cache hit rates
- High memory usage
- Frequent cache misses

**Investigation:**
```bash
# Analyze cache patterns
redis-cli --hotkeys

# Check cache memory usage
redis-cli INFO memory

# Monitor cache TTL settings
redis-cli --scan --pattern "*" | head -10 | xargs redis-cli TTL
```

### Log Analysis

#### Important Log Patterns

```bash
# Geocoding errors
grep -E "GEOCODE_ERROR|INVALID_ADDRESS" /var/log/geo-location/application.log

# Provider issues
grep -E "PROVIDER_TIMEOUT|API_ERROR|QUOTA_EXCEEDED" /var/log/geo-location/application.log

# Performance issues
grep -E "SLOW_RESPONSE|HIGH_LATENCY" /var/log/geo-location/application.log

# Cache issues
grep -E "CACHE_MISS|CACHE_ERROR|EVICTION" /var/log/geo-location/application.log
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-maintenance.sh

echo "=== Geo-Location Daily Report ===" > /tmp/maintenance.log

# Check service health
curl -f http://localhost:8407/actuator/health >> /tmp/maintenance.log

# Check provider status
curl http://localhost:8407/api/v1/admin/providers/status >> /tmp/maintenance.log

# Check cache performance
curl http://localhost:8407/api/v1/admin/cache/stats >> /tmp/maintenance.log

# Clean expired cache entries
redis-cli EVAL "return redis.call('del', unpack(redis.call('keys', 'expired:*')))" 0

# Send report
mail -s "Geo-Location Daily Report" geo-team@exalt.com < /tmp/maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-maintenance.sh

# Optimize database
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "VACUUM ANALYZE;"

# Update provider configurations
curl -X POST http://localhost:8407/api/v1/admin/providers/refresh-config \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Generate usage report
curl -X GET http://localhost:8407/api/v1/admin/reports/weekly \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Clean old logs
find /var/log/geo-location -name "*.log.*" -mtime +7 -delete
```

### Update Procedures

#### Rolling Updates (Kubernetes)

```bash
# Update deployment
kubectl set image deployment/geo-location-service \
  geo-location-service=geo-location-service:v1.5.0 \
  -n shared-infrastructure

# Monitor rollout
kubectl rollout status deployment/geo-location-service -n shared-infrastructure

# Rollback if necessary
kubectl rollout undo deployment/geo-location-service -n shared-infrastructure
```

## Disaster Recovery

### Recovery Time Objectives (RTO)

| Component | RTO Target | Recovery Procedure |
|-----------|------------|-------------------|
| Service Instance | 3 minutes | Auto-scaling/Health check restart |
| Database | 15 minutes | Restore from backup |
| Cache | 5 minutes | Rebuild from providers |
| Provider Failover | 1 minute | Automatic failover |

### Recovery Point Objectives (RPO)

| Data Type | RPO Target | Backup Frequency |
|-----------|------------|------------------|
| Location Data | 1 hour | Hourly snapshots |
| Cache Data | 15 minutes | Continuous replication |
| Configuration | 5 minutes | Real-time sync |

### Emergency Procedures

1. **Provider Outage**: Automatic failover to backup providers
2. **Database Failure**: Restore from latest backup
3. **Cache Failure**: Rebuild cache from providers
4. **Service Outage**: Auto-scaling and health checks

### Emergency Contacts

- **Primary On-Call**: +1-555-0131 (geo-team-primary@exalt.com)
- **Secondary On-Call**: +1-555-0132 (geo-team-secondary@exalt.com)
- **Provider Support**: Google Maps, OpenStreetMap support
- **DevOps Team**: devops-emergency@exalt.com

## Compliance and Auditing

### Location Data Compliance

```bash
# Generate compliance report
curl -X GET http://localhost:8407/api/v1/admin/compliance/report \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Check data retention
curl -X GET http://localhost:8407/api/v1/admin/compliance/retention \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Audit Trail Management

```yaml
# Audit configuration
audit:
  enabled: true
  events:
    - GEOCODE_REQUEST
    - ROUTE_CALCULATION
    - PROVIDER_SWITCH
    - CACHE_OPERATION
    - ADMIN_ACTION
  retention-days: 365
  encryption: true
```

## Support and Escalation

### Support Levels

1. **L1 Support**: Basic service restart, health checks
2. **L2 Support**: Configuration changes, provider management
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
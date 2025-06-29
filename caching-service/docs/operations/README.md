# Caching Service Operations Guide

## Overview

This comprehensive operations guide provides detailed procedures for managing, monitoring, and maintaining the Caching Service in production environments. It covers daily operations, performance optimization, incident response, maintenance procedures, and troubleshooting guidelines.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Cache Management](#cache-management)
3. [Performance Monitoring](#performance-monitoring)
4. [Maintenance Procedures](#maintenance-procedures)
5. [Incident Response](#incident-response)
6. [Backup and Recovery](#backup-and-recovery)
7. [Security Operations](#security-operations)
8. [Troubleshooting](#troubleshooting)
9. [Best Practices](#best-practices)

## Service Operations

### Service Management

#### Starting the Caching Service

**Production Environment:**
```bash
# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n caching-system
kubectl scale deployment caching-service --replicas=3 -n caching-system

# Using Docker Compose
docker-compose -f docker-compose.prod.yml up -d caching-service

# Using systemd
sudo systemctl start caching-service
sudo systemctl enable caching-service

# Verify startup
kubectl get pods -l app=caching-service -n caching-system
curl http://caching-service:8403/actuator/health
```

#### Graceful Shutdown

```bash
# Kubernetes graceful shutdown
kubectl scale deployment caching-service --replicas=0 -n caching-system

# Wait for cache flush
kubectl exec -it caching-service-xxx -n caching-system -- \
  curl -X POST http://localhost:8403/admin/cache/flush-and-shutdown

# Docker graceful shutdown
docker-compose -f docker-compose.prod.yml stop caching-service

# systemd shutdown
sudo systemctl stop caching-service
```

#### Service Status Monitoring

```bash
# Real-time service status
kubectl get pods -l app=caching-service -n caching-system -w

# Detailed pod information
kubectl describe pod caching-service-xxx -n caching-system

# Service health endpoints
curl http://caching-service:8403/actuator/health
curl http://caching-service:8403/actuator/health/liveness
curl http://caching-service:8403/actuator/health/readiness

# Component health checks
curl http://caching-service:8403/actuator/health/redis
curl http://caching-service:8403/actuator/health/database
curl http://caching-service:8403/actuator/health/diskSpace
```

### Configuration Management

#### Dynamic Configuration Updates

```bash
# View current configuration
curl http://caching-service:8404/actuator/configprops | jq .

# Reload configuration without restart
curl -X POST http://caching-service:8404/actuator/refresh \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update cache region settings
curl -X PATCH http://caching-service:8403/admin/cache/regions/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultTtl": "PT2H",
    "maxSize": 150000,
    "evictionPolicy": "LRU"
  }'

# Update Redis connection settings
curl -X PUT http://caching-service:8403/admin/redis/connection \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "maxTotal": 300,
    "maxIdle": 100,
    "minIdle": 20,
    "testOnBorrow": true
  }'
```

#### Feature Flag Management

```bash
# List all feature flags
curl http://caching-service:8404/admin/features \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Enable/disable cache compression
curl -X PUT http://caching-service:8404/admin/features/cache-compression \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"enabled": true}'

# Enable write-through caching for specific regions
curl -X PUT http://caching-service:8404/admin/features/write-through \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "regions": ["products", "users"],
    "rolloutPercentage": 25
  }'
```

## Cache Management

### Cache Operations

#### Cache Region Management

```bash
# List all cache regions
curl http://caching-service:8403/api/v1/cache/regions \
  -H "Authorization: Bearer $API_TOKEN"

# Get region statistics
curl http://caching-service:8403/api/v1/cache/regions/products/stats \
  -H "Authorization: Bearer $API_TOKEN"

# Create new cache region
curl -X POST http://caching-service:8403/api/v1/cache/regions \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "search-results",
    "defaultTtl": "PT15M",
    "maxSize": 25000,
    "evictionPolicy": "LFU",
    "enableCompression": true,
    "enableEncryption": false
  }'

# Update region configuration
curl -X PUT http://caching-service:8403/api/v1/cache/regions/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultTtl": "PT3H",
    "maxSize": 200000,
    "evictionPolicy": "LRU"
  }'

# Delete cache region
curl -X DELETE http://caching-service:8403/api/v1/cache/regions/old-region \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Cache Entry Operations

```bash
# Get cache entry
curl http://caching-service:8403/api/v1/cache/regions/products/keys/product:123 \
  -H "Authorization: Bearer $API_TOKEN"

# Set cache entry with TTL
curl -X PUT http://caching-service:8403/api/v1/cache/regions/products/keys/product:456 \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "value": {
      "id": 456,
      "name": "Premium Product",
      "price": 199.99
    },
    "ttl": "PT4H"
  }'

# Delete cache entry
curl -X DELETE http://caching-service:8403/api/v1/cache/regions/products/keys/product:789 \
  -H "Authorization: Bearer $API_TOKEN"

# Check if key exists
curl -I http://caching-service:8403/api/v1/cache/regions/products/keys/product:123 \
  -H "Authorization: Bearer $API_TOKEN"

# Get multiple keys
curl -X POST http://caching-service:8403/api/v1/cache/regions/products/keys/batch \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "keys": ["product:123", "product:456", "product:789"]
  }'
```

#### Cache Invalidation

```bash
# Clear entire cache region
curl -X DELETE http://caching-service:8403/api/v1/cache/regions/products/clear \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Invalidate by pattern
curl -X POST http://caching-service:8403/api/v1/cache/invalidate/pattern \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "pattern": "user:*:preferences",
    "regions": ["users", "sessions"]
  }'

# Invalidate by tags
curl -X POST http://caching-service:8403/api/v1/cache/invalidate/tags \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tags": ["category:electronics", "brand:apple"],
    "regions": ["products"]
  }'

# Bulk invalidation
curl -X POST http://caching-service:8403/api/v1/cache/invalidate/bulk \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "invalidations": [
      {
        "type": "key",
        "region": "products",
        "identifier": "product:123"
      },
      {
        "type": "pattern",
        "region": "users",
        "identifier": "user:*:cart"
      }
    ]
  }'
```

### Cache Warming

#### Manual Cache Warming

```bash
# Warm specific cache region
curl -X POST http://caching-service:8403/api/v1/cache/warm/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "strategy": "popular-items",
    "concurrency": 10,
    "batchSize": 100,
    "maxItems": 10000
  }'

# Warm from data source
curl -X POST http://caching-service:8403/api/v1/cache/warm/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "strategy": "all-items",
    "dataSource": "database",
    "query": "SELECT * FROM categories WHERE active = true"
  }'

# Check warming status
curl http://caching-service:8403/api/v1/cache/warm/status \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Cancel warming operation
curl -X DELETE http://caching-service:8403/api/v1/cache/warm/products \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Scheduled Cache Warming

```bash
# Create warming schedule
curl -X POST http://caching-service:8403/admin/cache/warm/schedule \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "nightly-product-warm",
    "schedule": "0 2 * * *",
    "region": "products",
    "strategy": "popular-items",
    "enabled": true
  }'

# List warming schedules
curl http://caching-service:8403/admin/cache/warm/schedules \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update warming schedule
curl -X PUT http://caching-service:8403/admin/cache/warm/schedule/nightly-product-warm \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "schedule": "0 1 * * *",
    "enabled": true
  }'
```

## Performance Monitoring

### Key Performance Indicators

#### Cache Performance Metrics

| Metric | Target | Warning | Critical | Description |
|--------|--------|---------|----------|-------------|
| Hit Rate | > 85% | < 80% | < 70% | Percentage of cache hits |
| Miss Rate | < 15% | > 20% | > 30% | Percentage of cache misses |
| Response Time | < 5ms | > 10ms | > 25ms | Average cache operation time |
| Memory Usage | < 80% | > 85% | > 95% | Redis memory utilization |
| Eviction Rate | < 5% | > 10% | > 20% | Percentage of evicted entries |

#### System Performance Metrics

| Metric | Target | Warning | Critical | Description |
|--------|--------|---------|----------|-------------|
| CPU Usage | < 70% | > 80% | > 90% | Service CPU utilization |
| Memory Usage | < 80% | > 85% | > 95% | JVM memory usage |
| Network I/O | < 70% | > 80% | > 90% | Network bandwidth usage |
| Disk I/O | < 50% | > 70% | > 85% | Disk operation load |

### Real-time Monitoring

#### Performance Dashboard Queries

```bash
# Cache hit rate over time
curl http://caching-service:8404/actuator/metrics/cache.requests \
  -H "Authorization: Bearer $MONITOR_TOKEN" | \
  jq '.measurements[] | select(.statistic == "COUNT") | .value'

# Memory usage by region
curl http://caching-service:8404/actuator/metrics/cache.size \
  -H "Authorization: Bearer $MONITOR_TOKEN" | \
  jq '.availableTags[] | select(.tag == "cache") | .values[]'

# Response time percentiles
curl http://caching-service:8404/actuator/metrics/cache.gets \
  -H "Authorization: Bearer $MONITOR_TOKEN" | \
  jq '.measurements[] | select(.statistic | contains("percentile"))'

# Active connections
curl http://caching-service:8404/actuator/metrics/redis.connections.active \
  -H "Authorization: Bearer $MONITOR_TOKEN"
```

#### Automated Performance Reports

```bash
#!/bin/bash
# scripts/performance-report.sh

REPORT_DATE=$(date +"%Y-%m-%d")
REPORT_FILE="/tmp/cache-performance-${REPORT_DATE}.json"

echo "Generating cache performance report for $REPORT_DATE"

# Collect metrics
curl -s http://caching-service:8404/actuator/metrics/cache.requests > /tmp/cache_requests.json
curl -s http://caching-service:8404/actuator/metrics/cache.size > /tmp/cache_size.json
curl -s http://caching-service:8404/actuator/metrics/jvm.memory.used > /tmp/memory_usage.json

# Generate report
cat > $REPORT_FILE << EOF
{
  "reportDate": "$REPORT_DATE",
  "cacheMetrics": {
    "hitRate": $(jq '.measurements[] | select(.statistic == "COUNT") | .value' /tmp/cache_requests.json),
    "totalSize": $(jq '.measurements[] | select(.statistic == "VALUE") | .value' /tmp/cache_size.json),
    "memoryUsage": $(jq '.measurements[] | select(.statistic == "VALUE") | .value' /tmp/memory_usage.json)
  },
  "topRegions": $(curl -s http://caching-service:8403/api/v1/analytics/top-regions),
  "recommendations": $(curl -s http://caching-service:8403/api/v1/analytics/recommendations)
}
EOF

# Send report
curl -X POST http://monitoring-service:8080/reports/cache \
  -H "Content-Type: application/json" \
  -d @$REPORT_FILE

echo "Performance report generated: $REPORT_FILE"
```

### Performance Optimization

#### Cache Configuration Tuning

```bash
# Optimize Redis memory settings
curl -X PUT http://caching-service:8403/admin/redis/optimize-memory \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "maxMemoryPolicy": "allkeys-lru",
    "maxMemorySamples": 10,
    "compressionThreshold": 1024
  }'

# Adjust connection pool settings
curl -X PUT http://caching-service:8403/admin/redis/pool-config \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "maxTotal": 500,
    "maxIdle": 100,
    "minIdle": 50,
    "testOnBorrow": true,
    "testWhileIdle": true,
    "timeBetweenEvictionRuns": 30000
  }'

# Enable advanced caching strategies
curl -X PUT http://caching-service:8403/admin/cache/strategies \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "products": "refresh-ahead",
    "sessions": "write-through",
    "search": "cache-aside"
  }'
```

#### JVM Performance Tuning

```bash
# Get current JVM settings
curl http://caching-service:8404/actuator/metrics/jvm.memory.max

# Update JVM parameters (requires restart)
kubectl patch deployment caching-service -n caching-system --patch '
spec:
  template:
    spec:
      containers:
      - name: caching-service
        env:
        - name: JAVA_OPTS
          value: "-Xms2g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler"
'

# Monitor GC performance
curl http://caching-service:8404/actuator/metrics/jvm.gc.pause | \
  jq '.measurements[] | select(.statistic == "TOTAL_TIME")'
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Maintenance Tasks

```bash
#!/bin/bash
# scripts/daily-maintenance.sh

echo "=== Daily Cache Service Maintenance - $(date) ===" | tee -a /var/log/cache-maintenance.log

# Health check
HEALTH=$(curl -s http://caching-service:8403/actuator/health)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    echo "✅ Service health check passed" | tee -a /var/log/cache-maintenance.log
else
    echo "❌ Service health check failed: $HEALTH" | tee -a /var/log/cache-maintenance.log
    exit 1
fi

# Redis cluster health
REDIS_HEALTH=$(kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli cluster info | grep cluster_state:ok)
if [ "$REDIS_HEALTH" ]; then
    echo "✅ Redis cluster health check passed" | tee -a /var/log/cache-maintenance.log
else
    echo "❌ Redis cluster health check failed" | tee -a /var/log/cache-maintenance.log
fi

# Memory usage check
MEMORY_USAGE=$(curl -s http://caching-service:8404/actuator/metrics/redis.memory.used | jq '.measurements[0].value')
MEMORY_MAX=$(curl -s http://caching-service:8404/actuator/metrics/redis.memory.max | jq '.measurements[0].value')
MEMORY_PERCENT=$(echo "scale=2; $MEMORY_USAGE / $MEMORY_MAX * 100" | bc)

echo "Memory usage: ${MEMORY_PERCENT}%" | tee -a /var/log/cache-maintenance.log

if (( $(echo "$MEMORY_PERCENT > 85" | bc -l) )); then
    echo "⚠️ High memory usage detected" | tee -a /var/log/cache-maintenance.log
    # Send alert
    curl -X POST $SLACK_WEBHOOK -d "{\"text\":\"Cache service high memory usage: ${MEMORY_PERCENT}%\"}"
fi

# Clear expired entries
curl -X POST http://caching-service:8403/admin/cache/cleanup-expired \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Log rotation
find /var/log/caching-service -name "*.log" -size +100M -exec gzip {} \;
find /var/log/caching-service -name "*.gz" -mtime +30 -delete

echo "Daily maintenance completed successfully" | tee -a /var/log/cache-maintenance.log
```

#### Weekly Maintenance Tasks

```bash
#!/bin/bash
# scripts/weekly-maintenance.sh

echo "=== Weekly Cache Service Maintenance - $(date) ==="

# Performance analysis
curl -X POST http://caching-service:8403/api/v1/analytics/weekly-report \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -o /tmp/weekly-cache-report.json

# Cache optimization
curl -X POST http://caching-service:8403/admin/cache/optimize \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Update cache warming schedules based on usage patterns
curl -X POST http://caching-service:8403/admin/cache/warm/optimize-schedules \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Backup cache configuration
kubectl get configmap caching-service-config -n caching-system -o yaml > \
    /backup/cache-config-$(date +%Y%m%d).yaml

# Security audit
npm run security:audit
kubectl run security-scan --image=aquasec/trivy --rm -i --restart=Never -- \
    image exalt/caching-service:latest

echo "Weekly maintenance completed"
```

### Updates and Upgrades

#### Rolling Updates

```bash
# Pre-update validation
kubectl run pre-update-test --image=exalt/caching-service:new-version --rm -i --restart=Never -- \
    /app/scripts/compatibility-check.sh

# Perform rolling update
kubectl set image deployment/caching-service \
    caching-service=exalt/caching-service:new-version \
    -n caching-system

# Monitor rollout
kubectl rollout status deployment/caching-service -n caching-system --timeout=600s

# Post-update validation
kubectl run post-update-test --image=exalt/caching-service:new-version --rm -i --restart=Never -- \
    /app/scripts/smoke-test.sh

# Rollback if needed
if [ $? -ne 0 ]; then
    kubectl rollout undo deployment/caching-service -n caching-system
    echo "Rollback completed due to validation failure"
fi
```

#### Configuration Updates

```bash
# Update ConfigMap
kubectl patch configmap caching-service-config -n caching-system --patch '
data:
  application.yml: |
    cache:
      redis:
        pool:
          maxTotal: 300
          maxIdle: 100
      regions:
        products:
          defaultTtl: PT3H
          maxSize: 200000
'

# Trigger configuration reload
kubectl rollout restart deployment/caching-service -n caching-system
```

## Incident Response

### Incident Classification

| Severity | Response Time | Description | Examples |
|----------|---------------|-------------|----------|
| Critical | 15 minutes | Service completely down | Redis cluster failure, service crash |
| High | 30 minutes | Significant performance degradation | High latency, memory leaks |
| Medium | 2 hours | Moderate issues | Cache misses spike, slow responses |
| Low | 24 hours | Minor issues | Configuration warnings, logs errors |

### Common Incident Procedures

#### Redis Cluster Failure

1. **Immediate Response**
```bash
# Check cluster status
kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli cluster info

# Identify failed nodes
kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli cluster nodes | grep fail

# Attempt automatic failover
kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli cluster failover

# If failover fails, restart failed nodes
kubectl delete pod redis-cluster-X -n caching-system
```

2. **Enable Fallback Mode**
```bash
# Switch to local cache mode
curl -X POST http://caching-service:8403/admin/emergency/local-cache-mode \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Notify dependent services
curl -X POST http://notification-service:8080/alerts/cache-degraded
```

#### High Memory Usage

1. **Immediate Mitigation**
```bash
# Clear least important cache regions
curl -X DELETE http://caching-service:8403/api/v1/cache/regions/search-results/clear \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Trigger memory optimization
curl -X POST http://caching-service:8403/admin/cache/memory/optimize \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Temporarily reduce cache sizes
curl -X PUT http://caching-service:8403/admin/cache/regions/products \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"maxSize": 50000}'
```

2. **Scale Resources**
```bash
# Scale up Redis memory
kubectl patch statefulset redis-cluster -n caching-system --patch '
spec:
  template:
    spec:
      containers:
      - name: redis
        resources:
          limits:
            memory: "8Gi"
'

# Scale up service instances
kubectl scale deployment caching-service --replicas=5 -n caching-system
```

#### Performance Degradation

1. **Identify Root Cause**
```bash
# Check slow queries
kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli slowlog get 20

# Analyze cache patterns
curl http://caching-service:8403/api/v1/analytics/performance-issues \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Monitor system resources
kubectl top pods -n caching-system
kubectl top nodes
```

2. **Apply Fixes**
```bash
# Optimize queries
curl -X POST http://caching-service:8403/admin/cache/optimize-patterns \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Adjust cache strategies
curl -X PUT http://caching-service:8403/admin/cache/strategies \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"products": "write-behind", "sessions": "cache-aside"}'
```

## Backup and Recovery

### Cache Data Backup

#### Redis Backup Procedures

```bash
#!/bin/bash
# scripts/backup-redis.sh

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/redis/$BACKUP_DATE"
S3_BUCKET="exalt-cache-backups"

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup each Redis node
for i in {0..5}; do
    echo "Backing up redis-cluster-$i..."
    
    # Create RDB backup
    kubectl exec redis-cluster-$i -n caching-system -- redis-cli BGSAVE
    
    # Wait for backup to complete
    while [ "$(kubectl exec redis-cluster-$i -n caching-system -- redis-cli LASTSAVE)" = "$(kubectl exec redis-cluster-$i -n caching-system -- redis-cli LASTSAVE)" ]; do
        sleep 5
    done
    
    # Copy backup file
    kubectl cp caching-system/redis-cluster-$i:/var/lib/redis/dump.rdb \
        $BACKUP_DIR/redis-cluster-$i-dump.rdb
    
    # Compress backup
    gzip $BACKUP_DIR/redis-cluster-$i-dump.rdb
done

# Upload to S3
aws s3 sync $BACKUP_DIR s3://$S3_BUCKET/redis/$BACKUP_DATE/

# Cleanup old local backups
find /backup/redis -type d -mtime +7 -exec rm -rf {} \;

echo "Redis backup completed: $BACKUP_DATE"
```

#### Configuration Backup

```bash
#!/bin/bash
# scripts/backup-config.sh

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
CONFIG_BACKUP_DIR="/backup/config/$BACKUP_DATE"

mkdir -p $CONFIG_BACKUP_DIR

# Backup Kubernetes configurations
kubectl get configmap caching-service-config -n caching-system -o yaml > \
    $CONFIG_BACKUP_DIR/configmap.yaml

kubectl get secret caching-service-secret -n caching-system -o yaml > \
    $CONFIG_BACKUP_DIR/secrets.yaml

kubectl get deployment caching-service -n caching-system -o yaml > \
    $CONFIG_BACKUP_DIR/deployment.yaml

# Backup Redis configuration
kubectl exec redis-cluster-0 -n caching-system -- cat /etc/redis/redis.conf > \
    $CONFIG_BACKUP_DIR/redis.conf

# Upload to S3
aws s3 sync $CONFIG_BACKUP_DIR s3://exalt-cache-backups/config/$BACKUP_DATE/

echo "Configuration backup completed: $BACKUP_DATE"
```

### Recovery Procedures

#### Redis Data Recovery

```bash
#!/bin/bash
# scripts/recover-redis.sh

BACKUP_DATE=$1
if [ -z "$BACKUP_DATE" ]; then
    echo "Usage: $0 <backup-date>"
    exit 1
fi

echo "Recovering Redis data from backup: $BACKUP_DATE"

# Stop current Redis cluster
kubectl scale statefulset redis-cluster --replicas=0 -n caching-system

# Download backup
aws s3 sync s3://exalt-cache-backups/redis/$BACKUP_DATE/ /tmp/redis-restore/

# Restore each node
for i in {0..5}; do
    # Start single node
    kubectl scale statefulset redis-cluster --replicas=$((i+1)) -n caching-system
    kubectl wait --for=condition=ready pod/redis-cluster-$i -n caching-system --timeout=120s
    
    # Stop Redis process
    kubectl exec redis-cluster-$i -n caching-system -- redis-cli SHUTDOWN NOSAVE
    
    # Restore data file
    gunzip -c /tmp/redis-restore/redis-cluster-$i-dump.rdb.gz | \
        kubectl exec -i redis-cluster-$i -n caching-system -- tee /var/lib/redis/dump.rdb
    
    # Restart Redis
    kubectl delete pod redis-cluster-$i -n caching-system
    kubectl wait --for=condition=ready pod/redis-cluster-$i -n caching-system --timeout=120s
done

# Verify cluster
kubectl exec redis-cluster-0 -n caching-system -- redis-cli cluster info

echo "Redis recovery completed"
```

#### Service Recovery

```bash
#!/bin/bash
# scripts/recover-service.sh

# Stop caching service
kubectl scale deployment caching-service --replicas=0 -n caching-system

# Restore configuration
kubectl apply -f /backup/config/latest/configmap.yaml
kubectl apply -f /backup/config/latest/secrets.yaml

# Clear any corrupted local cache
kubectl exec -it caching-service-xxx -n caching-system -- \
    rm -rf /var/cache/*

# Restart service
kubectl scale deployment caching-service --replicas=3 -n caching-system

# Verify service health
kubectl wait --for=condition=available deployment/caching-service -n caching-system --timeout=300s

# Run smoke tests
kubectl run recovery-test --image=exalt/caching-service:latest --rm -i --restart=Never -- \
    /app/scripts/smoke-test.sh

echo "Service recovery completed"
```

## Security Operations

### Security Monitoring

#### Access Control Monitoring

```bash
# Monitor authentication attempts
curl http://caching-service:8404/admin/security/auth-events \
    -H "Authorization: Bearer $ADMIN_TOKEN" | \
    jq '.events[] | select(.success == false)'

# Check for unauthorized access patterns
curl http://caching-service:8404/admin/security/access-patterns \
    -H "Authorization: Bearer $ADMIN_TOKEN" | \
    jq '.patterns[] | select(.risk_level == "high")'

# Review API key usage
curl http://caching-service:8404/admin/security/api-keys/usage \
    -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Encryption Status

```bash
# Check TLS certificate status
curl http://caching-service:8404/admin/security/tls-status \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Verify cache encryption
curl http://caching-service:8404/admin/security/encryption-status \
    -H "Authorization: Bearer $ADMIN_TOKEN"

# Check Redis AUTH status
kubectl exec redis-cluster-0 -n caching-system -- redis-cli AUTH $REDIS_PASSWORD INFO
```

### Security Maintenance

#### Certificate Rotation

```bash
#!/bin/bash
# scripts/rotate-certificates.sh

echo "Starting certificate rotation..."

# Generate new certificates
openssl genrsa -out new-server.key 2048
openssl req -new -key new-server.key -out new-server.csr -subj "/CN=caching-service"
openssl x509 -req -days 365 -in new-server.csr -signkey new-server.key -out new-server.crt

# Update Kubernetes secret
kubectl create secret tls caching-service-tls-new \
    --cert=new-server.crt \
    --key=new-server.key \
    -n caching-system

# Update deployment to use new certificates
kubectl patch deployment caching-service -n caching-system --patch '
spec:
  template:
    spec:
      volumes:
      - name: tls-certs
        secret:
          secretName: caching-service-tls-new
'

# Wait for rollout
kubectl rollout status deployment/caching-service -n caching-system

# Verify new certificates
curl -k https://caching-service:8443/actuator/health

# Remove old certificates
kubectl delete secret caching-service-tls -n caching-system
kubectl patch secret caching-service-tls-new -n caching-system --patch '
metadata:
  name: caching-service-tls
'

echo "Certificate rotation completed"
```

#### Security Audit

```bash
#!/bin/bash
# scripts/security-audit.sh

echo "=== Cache Service Security Audit - $(date) ==="

# Check for security vulnerabilities
trivy image exalt/caching-service:latest

# Audit Kubernetes security
kube-bench run --targets node,policies,managedservices

# Check Redis security configuration
kubectl exec redis-cluster-0 -n caching-system -- redis-cli CONFIG GET "*"

# Review network policies
kubectl get networkpolicy -n caching-system

# Check RBAC permissions
kubectl auth can-i --list --as=system:serviceaccount:caching-system:caching-service

# Generate security report
cat > /tmp/security-audit-$(date +%Y%m%d).json << EOF
{
  "auditDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "service": "caching-service",
  "vulnerabilities": $(trivy image --format json exalt/caching-service:latest),
  "networkPolicies": $(kubectl get networkpolicy -n caching-system -o json),
  "rbacConfig": $(kubectl get rolebinding -n caching-system -o json)
}
EOF

echo "Security audit completed"
```

## Troubleshooting

### Common Issues

#### Issue: High Cache Miss Rate

**Symptoms:**
```
Cache hit rate below 70%
Increased database load
Slow response times
```

**Diagnosis:**
```bash
# Check cache statistics
curl http://caching-service:8403/api/v1/analytics/hit-rate-by-region

# Analyze access patterns
curl http://caching-service:8403/api/v1/analytics/access-patterns

# Check TTL configurations
curl http://caching-service:8403/api/v1/cache/regions | jq '.[] | {name, defaultTtl}'
```

**Solutions:**
```bash
# Increase TTL for stable data
curl -X PUT http://caching-service:8403/api/v1/cache/regions/products \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"defaultTtl": "PT6H"}'

# Implement cache warming
curl -X POST http://caching-service:8403/api/v1/cache/warm/products \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"strategy": "popular-items"}'

# Optimize cache strategies
curl -X PUT http://caching-service:8403/admin/cache/strategies \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"products": "refresh-ahead"}'
```

#### Issue: Redis Connection Timeout

**Symptoms:**
```
ERROR: Redis connection timeout
Connection pool exhausted
Service unavailable errors
```

**Diagnosis:**
```bash
# Check Redis cluster status
kubectl exec redis-cluster-0 -n caching-system -- redis-cli cluster info

# Monitor connection pool
curl http://caching-service:8404/actuator/metrics/redis.connections

# Check network latency
kubectl exec -it caching-service-xxx -n caching-system -- \
    ping redis-cluster-0.redis-cluster.caching-system.svc.cluster.local
```

**Solutions:**
```bash
# Increase connection timeout
curl -X PUT http://caching-service:8403/admin/redis/connection-config \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"connectionTimeout": 10000, "socketTimeout": 15000}'

# Increase pool size
curl -X PUT http://caching-service:8403/admin/redis/pool-config \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"maxTotal": 500, "maxIdle": 150}'

# Restart problematic Redis nodes
kubectl delete pod redis-cluster-X -n caching-system
```

#### Issue: Memory Pressure

**Symptoms:**
```
High JVM memory usage
Frequent GC events
OutOfMemoryError
```

**Diagnosis:**
```bash
# Check memory usage
curl http://caching-service:8404/actuator/metrics/jvm.memory.used

# Analyze heap dumps
kubectl exec -it caching-service-xxx -n caching-system -- \
    jcmd 1 GC.run_finalization

# Check cache sizes
curl http://caching-service:8403/api/v1/cache/regions | \
    jq '.[] | {name, size, memoryUsage}'
```

**Solutions:**
```bash
# Increase JVM memory
kubectl patch deployment caching-service -n caching-system --patch '
spec:
  template:
    spec:
      containers:
      - name: caching-service
        resources:
          limits:
            memory: "8Gi"
        env:
        - name: JAVA_OPTS
          value: "-Xmx6g -XX:+UseG1GC"
'

# Reduce cache sizes
curl -X PUT http://caching-service:8403/api/v1/cache/regions/products \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"maxSize": 75000}'

# Enable compression
curl -X PUT http://caching-service:8403/admin/cache/compression \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"enabled": true, "threshold": 1024}'
```

### Diagnostic Tools

#### Health Check Script

```bash
#!/bin/bash
# scripts/health-check.sh

echo "=== Caching Service Health Check ==="

# Service health
HEALTH=$(curl -s http://caching-service:8403/actuator/health)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    echo "✅ Service health: UP"
else
    echo "❌ Service health: DOWN"
    echo "$HEALTH"
fi

# Redis connectivity
REDIS_HEALTH=$(curl -s http://caching-service:8403/actuator/health/redis)
if echo "$REDIS_HEALTH" | grep -q '"status":"UP"'; then
    echo "✅ Redis connectivity: UP"
else
    echo "❌ Redis connectivity: DOWN"
    echo "$REDIS_HEALTH"
fi

# Performance metrics
HIT_RATE=$(curl -s http://caching-service:8404/actuator/metrics/cache.requests | \
    jq '.measurements[] | select(.statistic == "COUNT") | .value')
echo "📊 Cache hit rate: ${HIT_RATE}%"

MEMORY_USAGE=$(curl -s http://caching-service:8404/actuator/metrics/jvm.memory.used | \
    jq '.measurements[0].value')
echo "📊 Memory usage: ${MEMORY_USAGE} bytes"

# Redis cluster status
CLUSTER_INFO=$(kubectl exec redis-cluster-0 -n caching-system -- redis-cli cluster info 2>/dev/null)
if echo "$CLUSTER_INFO" | grep -q "cluster_state:ok"; then
    echo "✅ Redis cluster: OK"
else
    echo "❌ Redis cluster: NOT OK"
    echo "$CLUSTER_INFO"
fi

echo "=== Health Check Complete ==="
```

## Best Practices

### Operational Best Practices

1. **Monitoring and Alerting**
   - Set up comprehensive monitoring for all KPIs
   - Configure alerts for critical thresholds
   - Implement automated incident response

2. **Performance Optimization**
   - Regularly review and optimize cache configurations
   - Monitor cache hit rates and adjust TTL values
   - Implement appropriate cache warming strategies

3. **Security**
   - Regularly rotate certificates and credentials
   - Monitor access patterns for anomalies
   - Keep security patches up to date

4. **Capacity Planning**
   - Monitor resource usage trends
   - Plan for growth and scale proactively
   - Regular load testing

5. **Documentation**
   - Keep operational procedures up to date
   - Document all configuration changes
   - Maintain incident response playbooks

### Contact Information

- **Primary On-Call**: +1-555-0200 (cache-ops@exalt.com)
- **Secondary On-Call**: +1-555-0201 (infrastructure-ops@exalt.com)
- **Database Team**: dba-emergency@exalt.com
- **Security Team**: security-ops@exalt.com
- **Platform Team**: platform-ops@exalt.com

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Monthly*
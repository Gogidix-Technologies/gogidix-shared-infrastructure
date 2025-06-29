# User Profile Service - Operations Guide

## Overview

This operations guide provides comprehensive instructions for monitoring, maintaining, and troubleshooting the User Profile Service in production environments. It covers day-to-day operational procedures, incident response, and data privacy management.

## Service Health Monitoring

### Health Check Endpoints

#### Primary Health Checks

```bash
# Basic health check
curl -f http://user-profile-service:8095/actuator/health

# Detailed health check (with components)
curl -f http://user-profile-service:8095/actuator/health/detail

# Readiness probe
curl -f http://user-profile-service:8095/actuator/health/readiness

# Liveness probe
curl -f http://user-profile-service:8095/actuator/health/liveness
```

#### Component Health Checks

```bash
# Database health
curl http://user-profile-service:8095/actuator/health/db

# Redis health
curl http://user-profile-service:8095/actuator/health/redis

# Elasticsearch health
curl http://user-profile-service:8095/actuator/health/elasticsearch

# External service health
curl http://user-profile-service:8095/actuator/health/external-services
```

### Application Metrics

#### Key Performance Indicators (KPIs)

```bash
# Request metrics
curl http://user-profile-service:8095/actuator/metrics/http.server.requests

# JVM metrics
curl http://user-profile-service:8095/actuator/metrics/jvm.memory.used
curl http://user-profile-service:8095/actuator/metrics/jvm.gc.pause

# Database connection pool
curl http://user-profile-service:8095/actuator/metrics/hikaricp.connections.active

# Cache metrics
curl http://user-profile-service:8095/actuator/metrics/cache.gets
curl http://user-profile-service:8095/actuator/metrics/cache.puts
```

#### Custom Business Metrics

```bash
# Profile operations
curl http://user-profile-service:8095/actuator/metrics/profile.created.count
curl http://user-profile-service:8095/actuator/metrics/profile.updated.count
curl http://user-profile-service:8095/actuator/metrics/profile.deleted.count

# Privacy operations
curl http://user-profile-service:8095/actuator/metrics/gdpr.export.requests
curl http://user-profile-service:8095/actuator/metrics/ccpa.optout.requests

# Social login metrics
curl http://user-profile-service:8095/actuator/metrics/oauth2.login.success
curl http://user-profile-service:8095/actuator/metrics/oauth2.login.failure
```

### Prometheus Monitoring

#### Prometheus Configuration

Create `monitoring/prometheus-rules.yml`:

```yaml
groups:
  - name: user-profile-service.rules
    rules:
      # Application availability
      - alert: UserProfileServiceDown
        expr: up{job="user-profile-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "User Profile Service is down"
          description: "User Profile Service has been down for more than 1 minute"

      # High error rate
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} requests per second"

      # High response time
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is {{ $value }} seconds"

      # Memory usage
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage"
          description: "Memory usage is {{ $value | humanizePercentage }}"

      # Database connection pool
      - alert: DatabaseConnectionPoolExhaustion
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool near exhaustion"
          description: "Connection pool usage is {{ $value | humanizePercentage }}"

      # Redis connectivity
      - alert: RedisConnectionFailure
        expr: redis_connected_clients == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Redis connection failure"
          description: "Unable to connect to Redis cache"
```

#### Grafana Dashboard

Create `monitoring/user-profile-dashboard.json`:

```json
{
  "dashboard": {
    "id": null,
    "title": "User Profile Service Dashboard",
    "tags": ["user-profile", "microservice", "exalt"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "Request Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "sum(rate(http_server_requests_seconds_count{job=\"user-profile-service\"}[5m]))",
            "legendFormat": "Requests/sec"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "reqps"
          }
        }
      },
      {
        "id": 2,
        "title": "Response Time Distribution",
        "type": "heatmap",
        "targets": [
          {
            "expr": "sum(rate(http_server_requests_seconds_bucket{job=\"user-profile-service\"}[5m])) by (le)",
            "format": "heatmap",
            "legendFormat": "{{le}}"
          }
        ]
      },
      {
        "id": 3,
        "title": "Error Rate by Status",
        "type": "graph",
        "targets": [
          {
            "expr": "sum(rate(http_server_requests_seconds_count{job=\"user-profile-service\",status=~\"4..\"}[5m]))",
            "legendFormat": "4xx Errors"
          },
          {
            "expr": "sum(rate(http_server_requests_seconds_count{job=\"user-profile-service\",status=~\"5..\"}[5m]))",
            "legendFormat": "5xx Errors"
          }
        ]
      },
      {
        "id": 4,
        "title": "JVM Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{job=\"user-profile-service\",area=\"heap\"}",
            "legendFormat": "Heap Used"
          },
          {
            "expr": "jvm_memory_max_bytes{job=\"user-profile-service\",area=\"heap\"}",
            "legendFormat": "Heap Max"
          }
        ]
      },
      {
        "id": 5,
        "title": "Database Connection Pool",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active{job=\"user-profile-service\"}",
            "legendFormat": "Active Connections"
          },
          {
            "expr": "hikaricp_connections_idle{job=\"user-profile-service\"}",
            "legendFormat": "Idle Connections"
          }
        ]
      },
      {
        "id": 6,
        "title": "Cache Hit Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(cache_gets_total{job=\"user-profile-service\",result=\"hit\"}[5m]) / rate(cache_gets_total{job=\"user-profile-service\"}[5m])",
            "legendFormat": "Hit Rate"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percentunit"
          }
        }
      }
    ]
  }
}
```

## Log Management

### Log Configuration

#### Structured Logging

Configure `logback-spring.xml`:

```xml
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <springProfile name="!production">
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="production">
        <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/user-profile-service.json</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/user-profile-service.%d{yyyy-MM-dd}.%i.json.gz</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>10GB</totalSizeCap>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                    <pattern>
                        <pattern>
                            {
                                "service": "user-profile-service",
                                "version": "${BUILD_VERSION:-unknown}",
                                "environment": "${ENVIRONMENT:-unknown}"
                            }
                        </pattern>
                    </pattern>
                </providers>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="JSON_FILE"/>
        </root>
    </springProfile>
    
    <!-- Audit logging -->
    <appender name="AUDIT_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/audit.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/audit.%d{yyyy-MM-dd}.log.gz</fileNamePattern>
            <maxHistory>2555</maxHistory> <!-- 7 years for compliance -->
        </rollingPolicy>
        <encoder>
            <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="AUDIT" level="INFO" additivity="false">
        <appender-ref ref="AUDIT_FILE"/>
    </logger>
</configuration>
```

#### Log Analysis Queries

```bash
# Search for errors in the last hour
kubectl logs deployment/user-profile-service --since=1h | grep -i error

# Search for specific user operations
kubectl logs deployment/user-profile-service | grep "userId=12345"

# Monitor authentication failures
kubectl logs deployment/user-profile-service | grep "authentication failed"

# Track GDPR export requests
kubectl logs deployment/user-profile-service | grep "GDPR export request"
```

### Centralized Logging with ELK Stack

#### Elasticsearch Index Template

```json
{
  "index_patterns": ["user-profile-logs-*"],
  "template": {
    "settings": {
      "number_of_shards": 2,
      "number_of_replicas": 1,
      "index.lifecycle.name": "user-profile-logs-policy",
      "index.lifecycle.rollover_alias": "user-profile-logs"
    },
    "mappings": {
      "properties": {
        "@timestamp": {"type": "date"},
        "level": {"type": "keyword"},
        "logger": {"type": "keyword"},
        "message": {"type": "text"},
        "service": {"type": "keyword"},
        "version": {"type": "keyword"},
        "environment": {"type": "keyword"},
        "userId": {"type": "keyword"},
        "sessionId": {"type": "keyword"},
        "requestId": {"type": "keyword"}
      }
    }
  }
}
```

#### Kibana Dashboard Configuration

```json
{
  "objects": [
    {
      "type": "dashboard",
      "id": "user-profile-service-logs",
      "attributes": {
        "title": "User Profile Service Logs",
        "type": "dashboard",
        "description": "Operational dashboard for User Profile Service logs",
        "panelsJSON": "[{\"version\":\"8.0.0\",\"type\":\"visualization\",\"gridData\":{\"x\":0,\"y\":0,\"w\":24,\"h\":15,\"i\":\"1\"},\"panelIndex\":\"1\",\"embeddableConfig\":{},\"panelRefName\":\"panel_1\"}]"
      }
    }
  ]
}
```

## Database Operations

### Database Maintenance

#### Routine Maintenance Tasks

```sql
-- Update table statistics
ANALYZE user_profile;
ANALYZE social_profile;
ANALYZE privacy_consent;
ANALYZE profile_audit;

-- Check for unused indexes
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
WHERE idx_scan = 0;

-- Monitor table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Check for bloated tables
SELECT 
    schemaname, 
    tablename, 
    n_dead_tup, 
    n_live_tup,
    ROUND(n_dead_tup::float / (n_live_tup + n_dead_tup) * 100, 2) AS dead_percentage
FROM pg_stat_user_tables 
WHERE n_dead_tup > 1000
ORDER BY dead_percentage DESC;
```

#### Database Performance Optimization

```sql
-- Create performance monitoring function
CREATE OR REPLACE FUNCTION monitor_slow_queries()
RETURNS TABLE(
    query_text text,
    total_time numeric,
    mean_time numeric,
    calls bigint
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        query,
        total_exec_time,
        mean_exec_time,
        calls
    FROM pg_stat_statements
    WHERE mean_exec_time > 100  -- queries taking more than 100ms
    ORDER BY mean_exec_time DESC
    LIMIT 20;
END;
$$ LANGUAGE plpgsql;

-- Monitor active connections
SELECT 
    state,
    count(*) as connection_count,
    max(now() - state_change) as max_duration
FROM pg_stat_activity
WHERE state IS NOT NULL
GROUP BY state;

-- Check for blocking queries
SELECT 
    blocked_locks.pid AS blocked_pid,
    blocked_activity.usename AS blocked_user,
    blocking_locks.pid AS blocking_pid,
    blocking_activity.usename AS blocking_user,
    blocked_activity.query AS blocked_statement,
    blocking_activity.query AS blocking_statement
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.granted AND blocking_locks.granted;
```

### Database Backup and Recovery

#### Automated Backup Script

Create `scripts/database-backup.sh`:

```bash
#!/bin/bash

set -e

# Configuration
BACKUP_DIR="/backups/user-profile-service"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-user_profile_prod}"
DB_USER="${DB_USER:-postgres}"
RETENTION_DAYS=30

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Generate backup filename
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/user_profile_backup_$TIMESTAMP.sql"

# Perform backup
echo "Starting database backup..."
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
    --no-password --verbose --clean --create --if-exists \
    --format=custom --compress=9 \
    --file="$BACKUP_FILE.pgdump"

# Create SQL backup as well
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
    --no-password --verbose --clean --create --if-exists \
    --format=plain \
    --file="$BACKUP_FILE"

# Compress SQL backup
gzip "$BACKUP_FILE"

# Calculate checksums
sha256sum "$BACKUP_FILE.pgdump" > "$BACKUP_FILE.pgdump.sha256"
sha256sum "$BACKUP_FILE.gz" > "$BACKUP_FILE.gz.sha256"

# Cleanup old backups
find "$BACKUP_DIR" -name "user_profile_backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name "user_profile_backup_*.pgdump" -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name "*.sha256" -mtime +$RETENTION_DAYS -delete

echo "Database backup completed: $BACKUP_FILE.pgdump"
echo "Database backup completed: $BACKUP_FILE.gz"
```

#### Point-in-Time Recovery

```bash
#!/bin/bash

# Point-in-time recovery script
RECOVERY_TARGET_TIME="2024-06-25 14:30:00"
BACKUP_FILE="/backups/user-profile-service/user_profile_backup_20240625_120000.pgdump"

# Stop the application
kubectl scale deployment user-profile-service --replicas=0

# Create recovery database
createdb user_profile_recovery

# Restore from backup
pg_restore -h $DB_HOST -U $DB_USER -d user_profile_recovery -v "$BACKUP_FILE"

# Apply WAL files for point-in-time recovery (if available)
# This requires continuous archiving to be set up

echo "Point-in-time recovery completed to: $RECOVERY_TARGET_TIME"
```

## Cache Management

### Redis Operations

#### Cache Monitoring

```bash
# Connect to Redis
kubectl exec -it deployment/redis -- redis-cli

# Monitor Redis operations
kubectl exec -it deployment/redis -- redis-cli monitor

# Check Redis info
kubectl exec -it deployment/redis -- redis-cli info

# Check memory usage
kubectl exec -it deployment/redis -- redis-cli info memory

# List keys by pattern
kubectl exec -it deployment/redis -- redis-cli keys "user-profile:*"

# Check cache hit ratio
kubectl exec -it deployment/redis -- redis-cli info stats | grep keyspace
```

#### Cache Maintenance

```bash
# Flush specific cache patterns
kubectl exec -it deployment/redis -- redis-cli eval "
    for i, name in ipairs(redis.call('KEYS', ARGV[1])) do
        redis.call('DEL', name)
    end
" 0 "user-profile:session:*"

# Clear expired keys
kubectl exec -it deployment/redis -- redis-cli eval "
    local keys = redis.call('keys', '*')
    local expired = 0
    for i=1,#keys do
        if redis.call('ttl', keys[i]) == -1 then
            redis.call('del', keys[i])
            expired = expired + 1
        end
    end
    return expired
" 0

# Monitor slow operations
kubectl exec -it deployment/redis -- redis-cli config set slowlog-log-slower-than 10000
kubectl exec -it deployment/redis -- redis-cli slowlog get 10
```

### Cache Warm-up

Create `scripts/cache-warmup.sh`:

```bash
#!/bin/bash

# Cache warm-up script for User Profile Service
SERVICE_URL="http://user-profile-service:8095/api/v1"
ADMIN_TOKEN="$ADMIN_JWT_TOKEN"

echo "Starting cache warm-up..."

# Warm up frequently accessed profiles
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
     "$SERVICE_URL/profiles/popular" | jq -r '.[].id' | while read profile_id; do
    curl -H "Authorization: Bearer $ADMIN_TOKEN" \
         "$SERVICE_URL/profiles/$profile_id" > /dev/null 2>&1
    echo "Warmed up profile: $profile_id"
done

# Warm up social provider configurations
for provider in facebook google twitter linkedin; do
    curl -H "Authorization: Bearer $ADMIN_TOKEN" \
         "$SERVICE_URL/oauth2/providers/$provider/config" > /dev/null 2>&1
    echo "Warmed up provider config: $provider"
done

echo "Cache warm-up completed"
```

## Data Privacy Management

### GDPR Compliance Operations

#### Data Subject Rights Management

```bash
# Create GDPR operations script
cat > scripts/gdpr-operations.sh << 'EOF'
#!/bin/bash

SERVICE_URL="http://user-profile-service:8095/api/v1"
ADMIN_TOKEN="$ADMIN_JWT_TOKEN"

case "$1" in
    export)
        USER_ID="$2"
        echo "Initiating GDPR data export for user: $USER_ID"
        curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
             -H "Content-Type: application/json" \
             -d "{\"userId\":\"$USER_ID\",\"format\":\"json\"}" \
             "$SERVICE_URL/privacy/gdpr/export"
        ;;
    delete)
        USER_ID="$2"
        echo "Initiating GDPR data deletion for user: $USER_ID"
        curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
             -H "Content-Type: application/json" \
             -d "{\"userId\":\"$USER_ID\",\"reason\":\"user_request\"}" \
             "$SERVICE_URL/privacy/gdpr/delete"
        ;;
    anonymize)
        USER_ID="$2"
        echo "Initiating data anonymization for user: $USER_ID"
        curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
             -H "Content-Type: application/json" \
             -d "{\"userId\":\"$USER_ID\"}" \
             "$SERVICE_URL/privacy/anonymize"
        ;;
    consent-status)
        USER_ID="$2"
        echo "Checking consent status for user: $USER_ID"
        curl -H "Authorization: Bearer $ADMIN_TOKEN" \
             "$SERVICE_URL/privacy/consent/$USER_ID"
        ;;
    *)
        echo "Usage: $0 {export|delete|anonymize|consent-status} <user_id>"
        exit 1
        ;;
esac
EOF

chmod +x scripts/gdpr-operations.sh
```

#### Automated Data Retention

Create `scripts/data-retention.sh`:

```bash
#!/bin/bash

# Automated data retention policy enforcement
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-user_profile_prod}"
DB_USER="${DB_USER:-postgres}"

# Configuration
RETENTION_DAYS=2555  # 7 years for GDPR compliance
ANONYMIZATION_DAYS=2190  # 6 years before anonymization

echo "Starting data retention policy enforcement..."

# Find profiles eligible for anonymization
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" << EOF
-- Find profiles older than anonymization threshold
SELECT 
    id, 
    user_id, 
    email, 
    created_at,
    EXTRACT(days FROM NOW() - created_at) as days_old
FROM user_profile 
WHERE 
    created_at < NOW() - INTERVAL '$ANONYMIZATION_DAYS days'
    AND is_active = false
    AND last_login < NOW() - INTERVAL '365 days'
ORDER BY created_at;
EOF

# Find profiles eligible for deletion
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" << EOF
-- Find profiles older than retention threshold
SELECT 
    id, 
    user_id, 
    created_at,
    EXTRACT(days FROM NOW() - created_at) as days_old
FROM user_profile 
WHERE 
    created_at < NOW() - INTERVAL '$RETENTION_DAYS days'
ORDER BY created_at;
EOF

echo "Data retention policy check completed"
```

### CCPA Compliance Operations

```bash
# CCPA operations script
cat > scripts/ccpa-operations.sh << 'EOF'
#!/bin/bash

SERVICE_URL="http://user-profile-service:8095/api/v1"
ADMIN_TOKEN="$ADMIN_JWT_TOKEN"

case "$1" in
    opt-out)
        USER_ID="$2"
        echo "Processing CCPA opt-out request for user: $USER_ID"
        curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
             -H "Content-Type: application/json" \
             -d "{\"userId\":\"$USER_ID\",\"optOut\":true}" \
             "$SERVICE_URL/privacy/ccpa/opt-out"
        ;;
    data-categories)
        USER_ID="$2"
        echo "Retrieving data categories for user: $USER_ID"
        curl -H "Authorization: Bearer $ADMIN_TOKEN" \
             "$SERVICE_URL/privacy/ccpa/data-categories/$USER_ID"
        ;;
    third-parties)
        USER_ID="$2"
        echo "Retrieving third-party data sharing info for user: $USER_ID"
        curl -H "Authorization: Bearer $ADMIN_TOKEN" \
             "$SERVICE_URL/privacy/ccpa/third-parties/$USER_ID"
        ;;
    *)
        echo "Usage: $0 {opt-out|data-categories|third-parties} <user_id>"
        exit 1
        ;;
esac
EOF

chmod +x scripts/ccpa-operations.sh
```

## Incident Response

### Incident Response Playbook

#### Severity Levels

1. **Critical (P0)**: Service completely unavailable
2. **High (P1)**: Major functionality impaired
3. **Medium (P2)**: Minor functionality impaired
4. **Low (P3)**: Cosmetic issues or minor improvements

#### Response Procedures

##### P0 - Critical Incidents

```bash
# Immediate response checklist
cat > playbooks/p0-response.md << 'EOF'
# P0 Critical Incident Response

## Immediate Actions (0-5 minutes)
1. [ ] Acknowledge incident in monitoring system
2. [ ] Start incident bridge/war room
3. [ ] Notify on-call engineering team
4. [ ] Check service status dashboard
5. [ ] Verify if issue is widespread or isolated

## Investigation (5-15 minutes)
1. [ ] Check recent deployments
2. [ ] Review error logs and metrics
3. [ ] Verify external dependencies
4. [ ] Check infrastructure status

## Mitigation (15-30 minutes)
1. [ ] Implement immediate workaround if available
2. [ ] Consider rollback if recent deployment
3. [ ] Scale resources if capacity issue
4. [ ] Disable non-critical features if needed

## Communication
1. [ ] Update status page
2. [ ] Notify stakeholders
3. [ ] Provide regular updates every 15 minutes
EOF
```

##### P1 - High Priority Incidents

```bash
# High priority incident response
cat > playbooks/p1-response.md << 'EOF'
# P1 High Priority Incident Response

## Initial Response (0-15 minutes)
1. [ ] Assess impact and affected users
2. [ ] Gather initial information
3. [ ] Determine if escalation to P0 needed
4. [ ] Start investigation

## Investigation (15-60 minutes)
1. [ ] Identify root cause
2. [ ] Determine fix complexity
3. [ ] Plan mitigation strategy
4. [ ] Implement fix or workaround

## Communication
1. [ ] Update internal stakeholders
2. [ ] Consider customer communication if widespread
EOF
```

### Incident Response Commands

#### Quick Diagnostics

```bash
# Service health check
kubectl get pods -n exalt-shared -l app=user-profile-service
kubectl describe pod -n exalt-shared -l app=user-profile-service

# Check recent events
kubectl get events -n exalt-shared --sort-by='.lastTimestamp' | tail -20

# Application logs
kubectl logs -n exalt-shared deployment/user-profile-service --tail=100

# Resource usage
kubectl top pods -n exalt-shared -l app=user-profile-service
kubectl top nodes

# Database connectivity
kubectl exec -it deployment/user-profile-service -- sh -c '
  curl -f http://localhost:8095/actuator/health/db || echo "DB check failed"'

# Cache connectivity
kubectl exec -it deployment/user-profile-service -- sh -c '
  curl -f http://localhost:8095/actuator/health/redis || echo "Redis check failed"'
```

#### Service Recovery

```bash
# Restart service
kubectl rollout restart deployment/user-profile-service -n exalt-shared

# Scale service
kubectl scale deployment/user-profile-service --replicas=5 -n exalt-shared

# Check rollout status
kubectl rollout status deployment/user-profile-service -n exalt-shared

# Rollback deployment
kubectl rollout undo deployment/user-profile-service -n exalt-shared

# Emergency circuit breaker activation
curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
     "http://user-profile-service:8095/actuator/circuitbreaker/disable"
```

### Post-Incident Review

#### RCA Template

Create `templates/rca-template.md`:

```markdown
# Root Cause Analysis - [Incident ID]

## Incident Summary
- **Date**: 
- **Duration**: 
- **Severity**: 
- **Impact**: 
- **Services Affected**: 
- **Users Affected**: 

## Timeline
| Time | Event | Action Taken |
|------|-------|--------------|
|      |       |              |

## Root Cause
### What Happened
- Description of the incident

### Why It Happened
- Technical root cause
- Process failures
- Contributing factors

## Resolution
### Immediate Actions
- What was done to resolve the incident

### Preventive Measures
- [ ] Action item 1
- [ ] Action item 2
- [ ] Action item 3

## Lessons Learned
### What Went Well
- Positive aspects of the response

### What Could Be Improved
- Areas for improvement

## Follow-up Actions
| Action | Owner | Due Date | Status |
|--------|-------|----------|--------|
|        |       |          |        |
```

## Maintenance Windows

### Planned Maintenance

#### Maintenance Checklist

```bash
# Pre-maintenance checklist
cat > playbooks/maintenance-checklist.md << 'EOF'
# Maintenance Window Checklist

## Pre-Maintenance (T-1 week)
- [ ] Schedule maintenance window
- [ ] Notify stakeholders
- [ ] Update status page
- [ ] Prepare rollback plan
- [ ] Test procedures in staging

## Pre-Maintenance (T-1 day)
- [ ] Verify backup completion
- [ ] Confirm maintenance window
- [ ] Prepare monitoring alerts
- [ ] Review procedures with team

## During Maintenance
- [ ] Start maintenance window
- [ ] Update status page
- [ ] Execute planned procedures
- [ ] Monitor system health
- [ ] Verify functionality

## Post-Maintenance
- [ ] Complete system verification
- [ ] Update status page
- [ ] Notify stakeholders
- [ ] Document results
- [ ] Schedule follow-up review
EOF
```

#### Maintenance Scripts

```bash
# Database maintenance
cat > scripts/database-maintenance.sh << 'EOF'
#!/bin/bash

echo "Starting database maintenance..."

# Vacuum and analyze
psql -h $DB_HOST -U $DB_USER -d $DB_NAME << SQL
VACUUM ANALYZE user_profile;
VACUUM ANALYZE social_profile;
VACUUM ANALYZE privacy_consent;
VACUUM ANALYZE profile_audit;
SQL

# Update statistics
psql -h $DB_HOST -U $DB_USER -d $DB_NAME << SQL
ANALYZE;
SQL

# Reindex if needed
psql -h $DB_HOST -U $DB_USER -d $DB_NAME << SQL
REINDEX INDEX CONCURRENTLY idx_user_profile_email;
REINDEX INDEX CONCURRENTLY idx_user_profile_user_id;
SQL

echo "Database maintenance completed"
EOF
```

```bash
# Cache maintenance
cat > scripts/cache-maintenance.sh << 'EOF'
#!/bin/bash

echo "Starting cache maintenance..."

# Clear expired sessions
kubectl exec deployment/redis -- redis-cli eval "
    local keys = redis.call('keys', 'session:*')
    local expired = 0
    for i=1,#keys do
        if redis.call('ttl', keys[i]) < 3600 then
            redis.call('del', keys[i])
            expired = expired + 1
        end
    end
    return expired
" 0

# Optimize memory
kubectl exec deployment/redis -- redis-cli memory purge

echo "Cache maintenance completed"
EOF
```

## Performance Optimization

### Application Performance Tuning

#### JVM Tuning

```bash
# JVM performance flags
JAVA_OPTS="
-Xms2g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+PrintGC
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/app/logs/
-XX:+ExitOnOutOfMemoryError
-Djava.security.egd=file:/dev/./urandom
"
```

#### Database Performance Tuning

```sql
-- Performance optimization queries
-- Update configuration for better performance
ALTER SYSTEM SET shared_buffers = '2GB';
ALTER SYSTEM SET effective_cache_size = '6GB';
ALTER SYSTEM SET maintenance_work_mem = '512MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Reload configuration
SELECT pg_reload_conf();

-- Create missing indexes based on slow query analysis
CREATE INDEX CONCURRENTLY idx_user_profile_last_login 
ON user_profile(last_login) WHERE is_active = true;

CREATE INDEX CONCURRENTLY idx_social_profile_provider_user 
ON social_profile(provider, provider_user_id);

CREATE INDEX CONCURRENTLY idx_privacy_consent_expires 
ON privacy_consent(expires_at) WHERE expires_at IS NOT NULL;
```

### Load Testing

#### Performance Testing Script

```bash
# Load testing with k6
cat > load-test/profile-operations.js << 'EOF'
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '5m', target: 200 },
    { duration: '10m', target: 300 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.1'],
    errors: ['rate<0.1'],
  },
};

const BASE_URL = 'http://user-profile-service:8095/api/v1';
const AUTH_TOKEN = 'Bearer ' + __ENV.TEST_TOKEN;

export default function() {
  let params = {
    headers: {
      'Authorization': AUTH_TOKEN,
      'Content-Type': 'application/json',
    },
  };

  // Test profile retrieval
  let profileResponse = http.get(`${BASE_URL}/profiles/me`, params);
  check(profileResponse, {
    'profile retrieval status is 200': (r) => r.status === 200,
    'profile retrieval response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  // Test profile update
  let updateData = {
    displayName: `Test User ${Math.random()}`,
    bio: 'Updated bio for load testing'
  };
  
  let updateResponse = http.put(`${BASE_URL}/profiles/me`, JSON.stringify(updateData), params);
  check(updateResponse, {
    'profile update status is 200': (r) => r.status === 200,
    'profile update response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(1);
}
EOF
```

---

**Document Version**: 1.0.0  
**Last Updated**: 2024-06-25  
**Document Owner**: Exalt Application Limited - Operations Team  
**Review Cycle**: Monthly
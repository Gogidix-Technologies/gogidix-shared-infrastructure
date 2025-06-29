# Admin Frameworks Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the Admin Frameworks service in production environments.

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
sudo systemctl start admin-frameworks

# Using Docker
docker-compose -f docker-compose.prod.yml up -d admin-frameworks

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n shared-infrastructure
```

#### Stopping the Service

```bash
# Graceful shutdown (allows 30s for cleanup)
sudo systemctl stop admin-frameworks

# Force stop if needed
sudo systemctl kill admin-frameworks

# Kubernetes
kubectl delete deployment admin-frameworks -n shared-infrastructure
```

#### Service Status Checks

```bash
# System status
sudo systemctl status admin-frameworks

# Health endpoints
curl http://localhost:8400/actuator/health
curl http://localhost:8400/actuator/health/readiness
curl http://localhost:8400/actuator/health/liveness

# Detailed metrics
curl http://localhost:8400/actuator/metrics
```

### Configuration Management

#### Environment-Specific Configurations

**Production (`application-prod.yml`):**
```yaml
spring:
  profiles: prod
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
  redis:
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 16
        max-idle: 8

logging:
  level:
    root: INFO
    com.exalt.admin: INFO
  file:
    name: /var/log/admin-frameworks/application.log
    max-file-size: 100MB
    max-history: 30
```

#### Runtime Configuration Updates

```bash
# Update configuration without restart
curl -X POST http://localhost:8400/actuator/refresh \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update specific property
curl -X POST http://localhost:8400/actuator/env \
  -H "Content-Type: application/json" \
  -d '{"name": "admin.dashboard.refresh-interval", "value": "60s"}'
```

## Monitoring and Alerting

### Key Performance Indicators (KPIs)

#### Service Health Metrics

| Metric | Normal Range | Alert Threshold | Critical Threshold |
|--------|--------------|-----------------|-------------------|
| Response Time | < 200ms | > 500ms | > 1000ms |
| CPU Usage | < 70% | > 80% | > 90% |
| Memory Usage | < 75% | > 85% | > 95% |
| Disk I/O | < 80% | > 90% | > 95% |
| Active Connections | < 100 | > 150 | > 200 |

#### Business Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Dashboard Load Time | < 2s | > 5s |
| Policy Evaluation Time | < 100ms | > 300ms |
| Report Generation Time | < 30s | > 60s |
| Component Error Rate | < 0.1% | > 1% |

### Prometheus Metrics

#### Custom Metrics Configuration

```yaml
# prometheus.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      service: admin-frameworks
      environment: ${SPRING_PROFILES_ACTIVE}
```

#### Key Metrics to Monitor

```promql
# Response time percentiles
histogram_quantile(0.95, http_request_duration_seconds_bucket{service="admin-frameworks"})

# Error rate
rate(http_requests_total{service="admin-frameworks", status=~"5.."}[5m])

# Dashboard component health
admin_component_health{component="dashboard"}

# Policy evaluation performance
admin_policy_evaluation_duration_seconds_bucket
```

### Grafana Dashboards

#### Dashboard Configuration

```json
{
  "dashboard": {
    "title": "Admin Frameworks - Service Overview",
    "panels": [
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, http_request_duration_seconds_bucket{service=\"admin-frameworks\"})"
          }
        ]
      },
      {
        "title": "Component Health",
        "type": "stat",
        "targets": [
          {
            "expr": "admin_component_health"
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
  - name: admin-frameworks
    rules:
      - alert: AdminFrameworksHighResponseTime
        expr: histogram_quantile(0.95, http_request_duration_seconds_bucket{service="admin-frameworks"}) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is {{ $value }}s"

      - alert: AdminFrameworksComponentDown
        expr: admin_component_health == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Component {{ $labels.component }} is down"
```

## Performance Management

### Performance Optimization

#### JVM Tuning

```bash
# Production JVM settings
export JAVA_OPTS="
  -Xmx2g
  -Xms2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/admin-frameworks/heapdumps/
  -Dcom.sun.management.jmxremote=true
  -Dcom.sun.management.jmxremote.port=9999
  -Dcom.sun.management.jmxremote.authenticate=false
  -Dcom.sun.management.jmxremote.ssl=false
"
```

#### Database Connection Pool Tuning

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      validation-timeout: 5000
      leak-detection-threshold: 60000
```

#### Cache Configuration

```yaml
spring:
  cache:
    type: redis
    redis:
      cache-names:
        - dashboardData
        - policyCache
        - regionCache
      time-to-live: 300s
      key-prefix: "admin-frameworks:"
```

### Load Testing

#### Performance Test Scripts

```bash
# Apache Bench test
ab -n 1000 -c 10 http://localhost:8400/api/v1/admin/dashboard

# Artillery.js load test
artillery run --config load-test-config.yml load-test-scenarios.yml

# JMeter test plan
jmeter -n -t admin-frameworks-load-test.jmx -l results.jtl
```

## Backup and Recovery

### Database Backup Procedures

#### Automated Backup Script

```bash
#!/bin/bash
# backup-database.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/admin-frameworks"
DB_NAME="admin_frameworks_db"

# Create backup
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME > \
  $BACKUP_DIR/admin_frameworks_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/admin_frameworks_$DATE.sql

# Remove backups older than 30 days
find $BACKUP_DIR -name "*.gz" -mtime +30 -delete

echo "Backup completed: admin_frameworks_$DATE.sql.gz"
```

#### Backup Schedule

```cron
# Crontab entry for daily backups at 2 AM
0 2 * * * /opt/admin-frameworks/scripts/backup-database.sh >> /var/log/backup.log 2>&1
```

### Configuration Backup

```bash
# Backup configuration files
tar -czf config-backup-$(date +%Y%m%d).tar.gz \
  /opt/admin-frameworks/config/ \
  /opt/admin-frameworks/k8s/ \
  /opt/admin-frameworks/scripts/
```

### Recovery Procedures

#### Database Recovery

```bash
# Stop the service
sudo systemctl stop admin-frameworks

# Restore database
gunzip -c admin_frameworks_20231201_020000.sql.gz | \
  psql -h $DB_HOST -U $DB_USER -d $DB_NAME

# Verify data integrity
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "SELECT COUNT(*) FROM admin_applications;"

# Restart service
sudo systemctl start admin-frameworks
```

## Security Operations

### Security Monitoring

#### Access Log Analysis

```bash
# Monitor authentication failures
tail -f /var/log/admin-frameworks/application.log | \
  grep "Authentication failed"

# Track admin operations
grep "ADMIN_OPERATION" /var/log/admin-frameworks/audit.log | \
  tail -20
```

#### Security Audit Commands

```bash
# Check for suspicious activities
grep -E "(SQL|XSS|CSRF)" /var/log/admin-frameworks/security.log

# Monitor privilege escalation attempts
grep "PRIVILEGE_ESCALATION" /var/log/admin-frameworks/audit.log
```

### Certificate Management

```bash
# Check certificate expiry
openssl x509 -in /etc/ssl/certs/admin-frameworks.crt -noout -dates

# Renew certificates (Let's Encrypt)
certbot renew --nginx

# Update keystore
keytool -import -alias admin-frameworks \
  -file /etc/ssl/certs/admin-frameworks.crt \
  -keystore /opt/admin-frameworks/keystore.jks
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Memory Usage

**Symptoms:**
- OutOfMemoryError exceptions
- Slow response times
- GC overhead warnings

**Investigation:**
```bash
# Generate heap dump
jcmd <PID> GC.run_finalization
jcmd <PID> VM.dump_heap /tmp/heapdump.hprof

# Analyze with Eclipse MAT or JVisualVM
```

**Solutions:**
- Increase heap size: `-Xmx4g`
- Tune GC: `-XX:+UseG1GC -XX:MaxGCPauseMillis=200`
- Check for memory leaks in custom code

#### 2. Database Connection Issues

**Symptoms:**
- Connection timeout errors
- Pool exhausted exceptions

**Investigation:**
```bash
# Check active connections
SELECT count(*) FROM pg_stat_activity 
WHERE datname = 'admin_frameworks_db';

# Monitor connection pool
curl http://localhost:8400/actuator/metrics/hikaricp.connections.active
```

**Solutions:**
- Increase connection pool size
- Check for connection leaks
- Optimize long-running queries

#### 3. Component Initialization Failures

**Symptoms:**
- Service starts but components not available
- 503 errors on component endpoints

**Investigation:**
```bash
# Check component status
curl http://localhost:8400/api/v1/admin/components/status

# Review initialization logs
grep "Component initialization" /var/log/admin-frameworks/application.log
```

### Log Analysis

#### Important Log Patterns

```bash
# Component errors
grep -E "Component.*ERROR" /var/log/admin-frameworks/application.log

# Performance issues
grep -E "SLOW_QUERY|TIMEOUT" /var/log/admin-frameworks/application.log

# Security events
grep -E "AUTH|SECURITY|VIOLATION" /var/log/admin-frameworks/security.log
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-maintenance.sh

echo "=== Daily Maintenance Report ===" > /tmp/maintenance.log

# Check service health
curl -f http://localhost:8400/actuator/health >> /tmp/maintenance.log

# Check disk space
df -h >> /tmp/maintenance.log

# Check memory usage
free -h >> /tmp/maintenance.log

# Check recent errors
tail -100 /var/log/admin-frameworks/application.log | grep ERROR >> /tmp/maintenance.log

# Send report
mail -s "Admin Frameworks Daily Report" admin-team@exalt.com < /tmp/maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-maintenance.sh

# Update dependencies (in staging first)
mvn versions:display-dependency-updates

# Review security alerts
npm audit --audit-level=moderate

# Cleanup old logs
find /var/log/admin-frameworks -name "*.log.*" -mtime +7 -delete

# Vacuum database
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "VACUUM ANALYZE;"
```

### Update Procedures

#### Rolling Updates (Kubernetes)

```bash
# Update deployment with new image
kubectl set image deployment/admin-frameworks \
  admin-frameworks=admin-frameworks:v1.2.0 \
  -n shared-infrastructure

# Monitor rollout
kubectl rollout status deployment/admin-frameworks -n shared-infrastructure

# Rollback if necessary
kubectl rollout undo deployment/admin-frameworks -n shared-infrastructure
```

#### Blue-Green Deployment

```bash
# Deploy to green environment
kubectl apply -f k8s/deployment-green.yaml

# Run smoke tests
./scripts/smoke-test.sh green

# Switch traffic
kubectl patch service admin-frameworks \
  -p '{"spec":{"selector":{"version":"green"}}}'

# Monitor and rollback if needed
kubectl patch service admin-frameworks \
  -p '{"spec":{"selector":{"version":"blue"}}}'
```

## Disaster Recovery

### Recovery Time Objectives (RTO)

| Component | RTO Target | Recovery Procedure |
|-----------|------------|-------------------|
| Service Instance | 5 minutes | Auto-scaling/Health check restart |
| Database | 15 minutes | Restore from backup |
| Complete System | 30 minutes | Full disaster recovery |

### Recovery Point Objectives (RPO)

| Data Type | RPO Target | Backup Frequency |
|-----------|------------|------------------|
| Configuration | 1 hour | Real-time Git sync |
| Application Data | 4 hours | Every 6 hours |
| Audit Logs | 1 hour | Continuous replication |

### Disaster Recovery Plan

1. **Assess Damage**: Determine scope of outage
2. **Isolate Issues**: Prevent further damage
3. **Restore Service**: Follow recovery procedures
4. **Verify Operations**: Run health checks
5. **Monitor**: Watch for recurring issues
6. **Post-Mortem**: Document lessons learned

### Emergency Contacts

- **Primary On-Call**: +1-555-0123 (admin-team-primary@exalt.com)
- **Secondary On-Call**: +1-555-0124 (admin-team-secondary@exalt.com)
- **DevOps Team**: devops-emergency@exalt.com
- **Database Team**: dba-team@exalt.com

## Compliance and Auditing

### Audit Trail Configuration

```yaml
admin:
  audit:
    enabled: true
    log-level: INFO
    events:
      - LOGIN
      - LOGOUT
      - POLICY_CHANGE
      - COMPONENT_ACCESS
      - ADMIN_OPERATION
    retention-days: 365
```

### Compliance Checks

```bash
# PCI DSS compliance check
./scripts/pci-compliance-check.sh

# GDPR data processing audit
./scripts/gdpr-audit.sh

# Security vulnerability scan
./scripts/security-scan.sh
```

## Support and Escalation

### Support Levels

1. **L1 Support**: Basic troubleshooting, service restarts
2. **L2 Support**: Configuration changes, performance tuning
3. **L3 Support**: Code changes, architecture decisions

### Escalation Matrix

| Issue Severity | Initial Response | Resolution Target | Escalation Path |
|----------------|------------------|-------------------|-----------------|
| Critical | 15 minutes | 2 hours | L1 → L2 → L3 → Management |
| High | 1 hour | 4 hours | L1 → L2 → L3 |
| Medium | 4 hours | 24 hours | L1 → L2 |
| Low | 24 hours | 1 week | L1 |

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Monthly*

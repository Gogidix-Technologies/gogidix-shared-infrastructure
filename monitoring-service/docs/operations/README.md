# Monitoring Service - Operations Guide

## Overview

This guide provides comprehensive operational procedures for managing the Exalt Monitoring Service in production environments. It covers daily operations, monitoring, maintenance, troubleshooting, and incident response procedures.

## Service Overview

### Service Characteristics
- **Service Name**: monitoring-service
- **Namespace**: monitoring
- **Technology Stack**: Java 17 + Spring Boot 3.x
- **High Availability**: Multi-node deployment
- **Recovery Time Objective (RTO)**: 15 minutes
- **Recovery Point Objective (RPO)**: 5 minutes

### Key Components
- **Prometheus**: Metrics collection and storage
- **Grafana**: Visualization and dashboards
- **AlertManager**: Alert management and routing
- **Jaeger**: Distributed tracing
- **Elasticsearch**: Log aggregation and search

## Daily Operations

### Morning Health Checks

#### 1. Service Status Verification
```bash
#!/bin/bash
# daily-health-check.sh

echo "=== Daily Monitoring Service Health Check ==="
echo "Date: $(date)"
echo ""

# Check Kubernetes pods
echo "1. Checking Kubernetes pods..."
kubectl get pods -n monitoring -o wide

# Check service endpoints
echo "2. Checking service endpoints..."
kubectl get endpoints -n monitoring

# Health check endpoints
echo "3. Testing health endpoints..."
for service in monitoring-service prometheus grafana alertmanager jaeger elasticsearch; do
    echo "  Checking $service..."
    kubectl exec -n monitoring deployment/$service -- curl -f http://localhost:8080/health 2>/dev/null && echo "  ✅ $service healthy" || echo "  ❌ $service unhealthy"
done

# Check resource usage
echo "4. Checking resource usage..."
kubectl top pods -n monitoring

# Check persistent volumes
echo "5. Checking storage..."
kubectl get pvc -n monitoring

echo "=== Health Check Complete ==="
```

#### 2. Metrics Validation
```bash
# Verify key metrics are being collected
curl -s "http://prometheus:9090/api/v1/query?query=up" | jq '.data.result[] | select(.metric.job=="monitoring-service") | .value[1]'

# Check metric cardinality
curl -s "http://prometheus:9090/api/v1/label/__name__/values" | jq '.data | length'

# Verify recent data ingestion
curl -s "http://prometheus:9090/api/v1/query?query=prometheus_tsdb_symbol_table_size_bytes" | jq '.data.result[0].value[0]'
```

#### 3. Dashboard Validation
```bash
# Check Grafana API
curl -s -u admin:${GRAFANA_PASSWORD} "http://grafana:3000/api/health"

# List dashboards
curl -s -u admin:${GRAFANA_PASSWORD} "http://grafana:3000/api/search?type=dash-db" | jq '.[].title'

# Check data source connectivity
curl -s -u admin:${GRAFANA_PASSWORD} "http://grafana:3000/api/datasources" | jq '.[].access'
```

### Performance Monitoring

#### 1. Key Performance Indicators (KPIs)
```yaml
# KPI Thresholds
availability_threshold: 99.9%          # Service uptime
response_time_p95: 500ms              # 95th percentile response time
error_rate_threshold: 0.1%            # Error rate limit
memory_usage_threshold: 80%           # Memory usage limit
cpu_usage_threshold: 70%              # CPU usage limit
disk_usage_threshold: 85%             # Disk usage limit
prometheus_ingestion_rate: 50000      # Samples per second
grafana_concurrent_users: 100         # Concurrent dashboard users
```

#### 2. Performance Queries
```promql
# Service availability
(sum(up{job="monitoring-service"}) / count(up{job="monitoring-service"})) * 100

# Response time 95th percentile
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket{job="monitoring-service"}[5m]))

# Error rate
rate(http_requests_total{job="monitoring-service",status=~"5.."}[5m]) / rate(http_requests_total{job="monitoring-service"}[5m]) * 100

# Memory usage
(process_resident_memory_bytes{job="monitoring-service"} / node_memory_MemTotal_bytes) * 100

# CPU usage
rate(process_cpu_seconds_total{job="monitoring-service"}[5m]) * 100

# Disk usage
(1 - (node_filesystem_avail_bytes / node_filesystem_size_bytes)) * 100
```

### Operational Dashboards

#### 1. Service Overview Dashboard
```json
{
  "dashboard": {
    "title": "Monitoring Service Operations",
    "panels": [
      {
        "title": "Service Health",
        "type": "stat",
        "targets": [{
          "expr": "up{job='monitoring-service'}",
          "legendFormat": "{{instance}}"
        }]
      },
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [{
          "expr": "rate(http_requests_total{job='monitoring-service'}[5m])",
          "legendFormat": "Requests/sec"
        }]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [{
          "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket{job='monitoring-service'}[5m]))",
          "legendFormat": "95th percentile"
        }]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [{
          "expr": "rate(http_requests_total{job='monitoring-service',status=~'5..'}[5m])",
          "legendFormat": "Errors/sec"
        }]
      }
    ]
  }
}
```

#### 2. Infrastructure Dashboard
```json
{
  "dashboard": {
    "title": "Monitoring Infrastructure",
    "panels": [
      {
        "title": "Prometheus Ingestion Rate",
        "type": "graph",
        "targets": [{
          "expr": "rate(prometheus_tsdb_samples_appended_total[5m])",
          "legendFormat": "Samples/sec"
        }]
      },
      {
        "title": "Grafana Active Sessions",
        "type": "stat",
        "targets": [{
          "expr": "grafana_stat_active_sessions",
          "legendFormat": "Active Users"
        }]
      },
      {
        "title": "AlertManager Alerts",
        "type": "graph",
        "targets": [{
          "expr": "alertmanager_alerts",
          "legendFormat": "{{state}}"
        }]
      }
    ]
  }
}
```

## Maintenance Procedures

### Scheduled Maintenance

#### 1. Weekly Maintenance Tasks
```bash
#!/bin/bash
# weekly-maintenance.sh

echo "=== Weekly Monitoring Service Maintenance ==="

# 1. Update Prometheus rules
echo "1. Updating Prometheus alert rules..."
kubectl apply -f prometheus/rules/ -n monitoring

# 2. Backup Grafana dashboards
echo "2. Backing up Grafana dashboards..."
./scripts/backup-dashboards.sh

# 3. Clean up old metrics data
echo "3. Cleaning old metrics (retention policy)..."
curl -X POST http://prometheus:9090/api/v1/admin/tsdb/delete_series?match[]={__name__=~".+",job="deprecated-service"}

# 4. Verify data sources
echo "4. Verifying Grafana data sources..."
curl -s -u admin:${GRAFANA_PASSWORD} "http://grafana:3000/api/datasources/proxy/1/api/v1/query?query=up" > /dev/null

# 5. Update alert notification channels
echo "5. Testing alert notification channels..."
./scripts/test-alert-channels.sh

# 6. Performance optimization
echo "6. Running performance optimization..."
kubectl exec -n monitoring deployment/prometheus -- \
  promtool query instant 'prometheus_config_last_reload_successful'

echo "=== Weekly Maintenance Complete ==="
```

#### 2. Monthly Maintenance Tasks
```bash
#!/bin/bash
# monthly-maintenance.sh

echo "=== Monthly Monitoring Service Maintenance ==="

# 1. Full backup
echo "1. Creating full backup..."
./scripts/full-backup.sh

# 2. Security updates
echo "2. Applying security updates..."
kubectl set image deployment/monitoring-service monitoring-service=registry.exalt.com/monitoring-service:latest -n monitoring

# 3. Certificate renewal
echo "3. Checking SSL certificate expiration..."
kubectl get certificates -n monitoring

# 4. Capacity planning review
echo "4. Generating capacity planning report..."
./scripts/capacity-report.sh

# 5. Performance tuning
echo "5. Reviewing performance metrics..."
./scripts/performance-analysis.sh

echo "=== Monthly Maintenance Complete ==="
```

### Configuration Management

#### 1. Prometheus Configuration Updates
```bash
# Update Prometheus configuration
kubectl create configmap prometheus-config --from-file=prometheus.yml --dry-run=client -o yaml | kubectl apply -n monitoring -f -

# Reload Prometheus configuration
curl -X POST http://prometheus:9090/-/reload

# Verify configuration
curl http://prometheus:9090/api/v1/status/config
```

#### 2. Grafana Dashboard Management
```bash
#!/bin/bash
# manage-dashboards.sh

GRAFANA_URL="http://grafana:3000"
GRAFANA_USER="admin"
GRAFANA_PASS="${GRAFANA_PASSWORD}"

# Export dashboard
export_dashboard() {
    local dashboard_uid=$1
    curl -s -u "${GRAFANA_USER}:${GRAFANA_PASS}" \
        "${GRAFANA_URL}/api/dashboards/uid/${dashboard_uid}" | \
        jq '.dashboard' > "dashboards/${dashboard_uid}.json"
}

# Import dashboard
import_dashboard() {
    local dashboard_file=$1
    curl -X POST -H "Content-Type: application/json" \
        -u "${GRAFANA_USER}:${GRAFANA_PASS}" \
        "${GRAFANA_URL}/api/dashboards/db" \
        -d @"${dashboard_file}"
}

# Bulk export all dashboards
export_all_dashboards() {
    curl -s -u "${GRAFANA_USER}:${GRAFANA_PASS}" \
        "${GRAFANA_URL}/api/search?type=dash-db" | \
        jq -r '.[].uid' | \
        while read uid; do
            export_dashboard "$uid"
        done
}
```

### Alert Management

#### 1. Alert Rule Updates
```yaml
# alert-rules.yml
groups:
  - name: monitoring-service.rules
    rules:
    - alert: MonitoringServiceDown
      expr: up{job="monitoring-service"} == 0
      for: 1m
      labels:
        severity: critical
        team: infrastructure
        service: monitoring-service
      annotations:
        summary: "Monitoring Service is down"
        description: "Monitoring Service on {{ $labels.instance }} has been down for more than 1 minute"
        runbook_url: "https://runbooks.exalt.com/monitoring-service-down"

    - alert: HighMemoryUsage
      expr: (process_resident_memory_bytes{job="monitoring-service"} / 1024 / 1024 / 1024) > 2
      for: 5m
      labels:
        severity: warning
        team: infrastructure
        service: monitoring-service
      annotations:
        summary: "High memory usage detected"
        description: "Memory usage is {{ $value }}GB on {{ $labels.instance }}"

    - alert: HighErrorRate
      expr: rate(http_requests_total{job="monitoring-service",status=~"5.."}[5m]) > 0.01
      for: 2m
      labels:
        severity: critical
        team: infrastructure
        service: monitoring-service
      annotations:
        summary: "High error rate detected"
        description: "Error rate is {{ $value }} errors/sec on {{ $labels.instance }}"
```

#### 2. Alert Testing
```bash
#!/bin/bash
# test-alerts.sh

echo "Testing alert rules..."

# Test alert rule syntax
promtool check rules prometheus/rules/*.yml

# Test alert evaluation
curl -X POST http://prometheus:9090/api/v1/admin/tsdb/snapshot

# Test notification channels
curl -X POST http://alertmanager:9093/api/v1/alerts \
  -H "Content-Type: application/json" \
  -d '[{
    "labels": {
      "alertname": "TestAlert",
      "service": "monitoring-service",
      "severity": "warning"
    },
    "annotations": {
      "summary": "Test alert for monitoring-service"
    }
  }]'
```

## Incident Response

### Incident Classification

#### Severity Levels
| Severity | Description | Response Time | Example |
|----------|-------------|---------------|---------|
| **P0 - Critical** | Complete service outage | 15 minutes | All monitoring down |
| **P1 - High** | Major functionality impacted | 30 minutes | Prometheus down |
| **P2 - Medium** | Minor functionality impacted | 2 hours | Single dashboard broken |
| **P3 - Low** | Cosmetic or minor issues | 1 business day | Typo in dashboard |

### Incident Response Procedures

#### 1. Service Outage Response (P0)
```bash
#!/bin/bash
# incident-response-p0.sh

echo "=== P0 Incident Response: Service Outage ==="

# 1. Immediate assessment
echo "1. Assessing service status..."
kubectl get pods -n monitoring
kubectl get svc -n monitoring
kubectl get ingress -n monitoring

# 2. Check recent changes
echo "2. Checking recent deployments..."
kubectl rollout history deployment/monitoring-service -n monitoring

# 3. Check resource constraints
echo "3. Checking resource usage..."
kubectl top pods -n monitoring
kubectl describe nodes | grep -A 5 "Non-terminated Pods"

# 4. Check logs for errors
echo "4. Checking error logs..."
kubectl logs deployment/monitoring-service -n monitoring --tail=100 | grep -i error

# 5. Initiate rollback if needed
echo "5. Checking rollback options..."
kubectl rollout history deployment/monitoring-service -n monitoring

# 6. Notify stakeholders
echo "6. Sending incident notification..."
./scripts/send-incident-notification.sh "P0" "Monitoring Service Outage"

echo "=== P0 Response Initiated ==="
```

#### 2. Performance Degradation Response (P1)
```bash
#!/bin/bash
# incident-response-p1.sh

echo "=== P1 Incident Response: Performance Degradation ==="

# 1. Identify bottlenecks
echo "1. Identifying performance bottlenecks..."
kubectl exec -n monitoring deployment/prometheus -- \
  promtool query instant 'rate(http_request_duration_seconds_sum{job="monitoring-service"}[5m]) / rate(http_request_duration_seconds_count{job="monitoring-service"}[5m])'

# 2. Check database performance
echo "2. Checking database performance..."
kubectl exec -n infrastructure deployment/postgres -- \
  psql -U monitoring_service_user -d monitoring_service_db -c "SELECT * FROM pg_stat_activity WHERE state = 'active';"

# 3. Check memory and CPU usage
echo "3. Analyzing resource usage..."
kubectl top pods -n monitoring --sort-by=memory
kubectl top pods -n monitoring --sort-by=cpu

# 4. Scale up if needed
echo "4. Scaling up services..."
kubectl scale deployment monitoring-service --replicas=5 -n monitoring

# 5. Monitor improvement
echo "5. Monitoring performance improvement..."
watch kubectl top pods -n monitoring

echo "=== P1 Response Initiated ==="
```

### Recovery Procedures

#### 1. Disaster Recovery
```bash
#!/bin/bash
# disaster-recovery.sh

echo "=== Disaster Recovery Procedure ==="

# 1. Assess damage
echo "1. Assessing system state..."
kubectl get all -n monitoring

# 2. Restore from backup
echo "2. Restoring from backup..."
./scripts/restore-backup.sh "latest"

# 3. Recreate persistent volumes if needed
echo "3. Checking persistent volumes..."
kubectl get pv,pvc -n monitoring

# 4. Restart services in order
echo "4. Restarting services..."
kubectl delete pods -l app=prometheus -n monitoring
kubectl delete pods -l app=grafana -n monitoring
kubectl delete pods -l app=monitoring-service -n monitoring

# 5. Verify recovery
echo "5. Verifying service recovery..."
./scripts/health-check-all.sh

# 6. Update incident log
echo "6. Updating incident log..."
./scripts/log-incident.sh "Disaster recovery completed"

echo "=== Disaster Recovery Complete ==="
```

#### 2. Data Recovery
```bash
#!/bin/bash
# data-recovery.sh

echo "=== Data Recovery Procedure ==="

# 1. Stop services to prevent data corruption
echo "1. Stopping services..."
kubectl scale deployment monitoring-service --replicas=0 -n monitoring
kubectl scale deployment prometheus --replicas=0 -n monitoring

# 2. Restore database from backup
echo "2. Restoring database..."
kubectl exec -n infrastructure deployment/postgres -- \
  pg_restore -U postgres -d monitoring_service_db /backup/monitoring_service_db_$(date +%Y%m%d).dump

# 3. Restore Prometheus data
echo "3. Restoring Prometheus data..."
kubectl cp backup/prometheus-data.tar.gz monitoring/prometheus-0:/prometheus/
kubectl exec -n monitoring prometheus-0 -- tar -xzf /prometheus/prometheus-data.tar.gz -C /prometheus/

# 4. Restore Grafana dashboards
echo "4. Restoring Grafana dashboards..."
for dashboard in backup/dashboards/*.json; do
    curl -X POST -H "Content-Type: application/json" \
        -u admin:${GRAFANA_PASSWORD} \
        "http://grafana:3000/api/dashboards/db" \
        -d "@$dashboard"
done

# 5. Restart services
echo "5. Restarting services..."
kubectl scale deployment prometheus --replicas=2 -n monitoring
kubectl scale deployment monitoring-service --replicas=3 -n monitoring

# 6. Verify data integrity
echo "6. Verifying data integrity..."
./scripts/verify-data-integrity.sh

echo "=== Data Recovery Complete ==="
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Memory Usage
```bash
# Diagnose memory issues
kubectl exec -n monitoring deployment/monitoring-service -- \
  jcmd 1 VM.summary

# Check heap dump
kubectl exec -n monitoring deployment/monitoring-service -- \
  jcmd 1 GC.run_finalization

# Increase memory limits
kubectl patch deployment monitoring-service -n monitoring -p '{"spec":{"template":{"spec":{"containers":[{"name":"monitoring-service","resources":{"limits":{"memory":"4Gi"}}}]}}}}'
```

#### 2. Database Connection Pool Exhaustion
```bash
# Check active connections
kubectl exec -n infrastructure deployment/postgres -- \
  psql -U postgres -c "SELECT count(*) FROM pg_stat_activity WHERE datname='monitoring_service_db';"

# Increase connection pool size
kubectl patch deployment monitoring-service -n monitoring -p '{"spec":{"template":{"spec":{"containers":[{"name":"monitoring-service","env":[{"name":"SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE","value":"20"}]}]}}}}'

# Monitor connection usage
watch kubectl exec -n infrastructure deployment/postgres -- \
  psql -U postgres -c "SELECT state, count(*) FROM pg_stat_activity WHERE datname='monitoring_service_db' GROUP BY state;"
```

#### 3. Prometheus Storage Issues
```bash
# Check Prometheus storage usage
kubectl exec -n monitoring deployment/prometheus -- \
  du -sh /prometheus

# Clean up old data
kubectl exec -n monitoring deployment/prometheus -- \
  find /prometheus -name "*.tmp" -delete

# Increase storage if needed
kubectl patch pvc prometheus-pvc -n monitoring -p '{"spec":{"resources":{"requests":{"storage":"100Gi"}}}}'
```

#### 4. Grafana Dashboard Loading Issues
```bash
# Check Grafana logs
kubectl logs deployment/grafana -n monitoring | grep -i error

# Restart Grafana
kubectl delete pods -l app=grafana -n monitoring

# Clear Grafana cache
kubectl exec -n monitoring deployment/grafana -- \
  rm -rf /var/lib/grafana/cache/*
```

### Performance Tuning

#### 1. JVM Performance Tuning
```yaml
# JVM optimization environment variables
env:
  - name: JAVA_OPTS
    value: >-
      -Xms2g -Xmx4g
      -XX:+UseG1GC
      -XX:MaxGCPauseMillis=200
      -XX:+UseStringDeduplication
      -XX:+OptimizeStringConcat
      -Djava.awt.headless=true
      -Dfile.encoding=UTF-8
      -Dspring.profiles.active=production
```

#### 2. Database Performance Tuning
```sql
-- Optimize database queries
CREATE INDEX CONCURRENTLY idx_metrics_timestamp ON metrics(timestamp);
CREATE INDEX CONCURRENTLY idx_alerts_status ON alerts(status, created_at);

-- Update table statistics
ANALYZE metrics;
ANALYZE alerts;

-- Check slow queries
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

#### 3. Kubernetes Resource Optimization
```yaml
# Resource requests and limits optimization
resources:
  requests:
    memory: "2Gi"
    cpu: "1000m"
  limits:
    memory: "4Gi"
    cpu: "2000m"

# Pod disruption budget
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: monitoring-service-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: monitoring-service
```

## Backup and Recovery

### Backup Strategy

#### 1. Automated Daily Backups
```bash
#!/bin/bash
# daily-backup.sh

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_PATH="/backup/monitoring-service"

echo "Starting daily backup: $BACKUP_DATE"

# 1. Database backup
kubectl exec -n infrastructure deployment/postgres -- \
  pg_dump -U monitoring_service_user monitoring_service_db | \
  gzip > "$BACKUP_PATH/db_$BACKUP_DATE.sql.gz"

# 2. Prometheus data backup
kubectl exec -n monitoring deployment/prometheus -- \
  tar -czf /tmp/prometheus_$BACKUP_DATE.tar.gz /prometheus/data
kubectl cp monitoring/prometheus-0:/tmp/prometheus_$BACKUP_DATE.tar.gz \
  "$BACKUP_PATH/prometheus_$BACKUP_DATE.tar.gz"

# 3. Grafana dashboards backup
curl -s -u admin:${GRAFANA_PASSWORD} \
  "http://grafana:3000/api/search?type=dash-db" | \
  jq -r '.[].uid' | \
  while read uid; do
    curl -s -u admin:${GRAFANA_PASSWORD} \
      "http://grafana:3000/api/dashboards/uid/$uid" | \
      jq '.dashboard' > "$BACKUP_PATH/dashboards/dashboard_$uid.json"
  done

# 4. Configuration backup
kubectl get configmaps -n monitoring -o yaml > "$BACKUP_PATH/configmaps_$BACKUP_DATE.yaml"
kubectl get secrets -n monitoring -o yaml > "$BACKUP_PATH/secrets_$BACKUP_DATE.yaml"

# 5. Upload to cloud storage
aws s3 sync "$BACKUP_PATH" s3://exalt-monitoring-backups/

echo "Backup completed: $BACKUP_DATE"
```

#### 2. Backup Verification
```bash
#!/bin/bash
# verify-backup.sh

BACKUP_DATE=$1
BACKUP_PATH="/backup/monitoring-service"

echo "Verifying backup: $BACKUP_DATE"

# Verify database backup
if zcat "$BACKUP_PATH/db_$BACKUP_DATE.sql.gz" | head -1 | grep -q "PostgreSQL database dump"; then
    echo "✅ Database backup verified"
else
    echo "❌ Database backup verification failed"
fi

# Verify Prometheus backup
if tar -tzf "$BACKUP_PATH/prometheus_$BACKUP_DATE.tar.gz" >/dev/null 2>&1; then
    echo "✅ Prometheus backup verified"
else
    echo "❌ Prometheus backup verification failed"
fi

# Verify Grafana dashboards
dashboard_count=$(find "$BACKUP_PATH/dashboards" -name "*.json" | wc -l)
if [ "$dashboard_count" -gt 0 ]; then
    echo "✅ Grafana dashboards backup verified ($dashboard_count dashboards)"
else
    echo "❌ Grafana dashboards backup verification failed"
fi

echo "Backup verification completed"
```

### Recovery Testing

#### 1. Monthly Recovery Test
```bash
#!/bin/bash
# recovery-test.sh

echo "=== Monthly Recovery Test ==="

# 1. Create test namespace
kubectl create namespace monitoring-recovery-test

# 2. Deploy monitoring stack to test namespace
kubectl apply -f k8s/ -n monitoring-recovery-test

# 3. Restore from backup
./scripts/restore-backup.sh "latest" "monitoring-recovery-test"

# 4. Verify functionality
./scripts/health-check-all.sh "monitoring-recovery-test"

# 5. Run validation tests
kubectl run recovery-test --rm -it --image=curlimages/curl -n monitoring-recovery-test -- \
  curl -f http://monitoring-service:8090/actuator/health

# 6. Clean up test environment
kubectl delete namespace monitoring-recovery-test

echo "=== Recovery Test Complete ==="
```

## Security Operations

### Security Monitoring
```bash
# Monitor security events
kubectl logs -f deployment/monitoring-service -n monitoring | grep -i "authentication\|authorization\|security"

# Check for failed login attempts
curl -s -u admin:${GRAFANA_PASSWORD} \
  "http://grafana:3000/api/admin/stats" | jq '.failed_logins'

# Audit configuration changes
kubectl get events -n monitoring --field-selector reason=ConfigMapChanged
```

### Certificate Management
```bash
# Check certificate expiration
kubectl get certificates -n monitoring -o custom-columns=NAME:.metadata.name,READY:.status.conditions[-1].status,SECRET:.spec.secretName,EXPIRATION:.status.notAfter

# Renew certificates
cert-manager-controller renew --all --namespace monitoring

# Verify certificate validity
openssl x509 -in <(kubectl get secret monitoring-service-tls -n monitoring -o jsonpath='{.data.tls\.crt}' | base64 -d) -text -noout
```

## Metrics and KPIs

### Operational Metrics
```promql
# Service Level Indicators (SLIs)
monitoring_service_availability = up{job="monitoring-service"}
monitoring_service_latency_p99 = histogram_quantile(0.99, rate(http_request_duration_seconds_bucket{job="monitoring-service"}[5m]))
monitoring_service_error_rate = rate(http_requests_total{job="monitoring-service",status=~"5.."}[5m]) / rate(http_requests_total{job="monitoring-service"}[5m])

# Infrastructure Metrics
prometheus_ingestion_rate = rate(prometheus_tsdb_samples_appended_total[5m])
grafana_active_users = grafana_stat_active_sessions
alertmanager_notifications_sent = rate(alertmanager_notifications_total[5m])

# Business Metrics
monitoring_dashboards_count = count(grafana_dashboard_versions)
monitoring_alerts_count = count(ALERTS)
monitoring_services_monitored = count(up)
```

### Reporting
```bash
#!/bin/bash
# generate-operational-report.sh

REPORT_DATE=$(date +%Y-%m-%d)
REPORT_FILE="operational-report-$REPORT_DATE.html"

cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Monitoring Service Operational Report - $REPORT_DATE</title>
</head>
<body>
    <h1>Monitoring Service Operational Report</h1>
    <h2>Service Health Summary</h2>
    <p>Availability: $(curl -s "http://prometheus:9090/api/v1/query?query=avg(up{job='monitoring-service'})" | jq -r '.data.result[0].value[1]')%</p>
    
    <h2>Performance Metrics</h2>
    <p>Average Response Time: $(curl -s "http://prometheus:9090/api/v1/query?query=avg(rate(http_request_duration_seconds_sum{job='monitoring-service'}[24h])/rate(http_request_duration_seconds_count{job='monitoring-service'}[24h]))" | jq -r '.data.result[0].value[1]')s</p>
    
    <h2>Resource Usage</h2>
    <p>Memory Usage: $(kubectl top pods -n monitoring --no-headers | awk '{sum += $3} END {print sum}')Mi</p>
    <p>CPU Usage: $(kubectl top pods -n monitoring --no-headers | awk '{sum += $2} END {print sum}')m</p>
    
    <h2>Alert Summary</h2>
    <p>Active Alerts: $(curl -s "http://alertmanager:9093/api/v1/alerts?active=true" | jq '.data | length')</p>
</body>
</html>
EOF

echo "Operational report generated: $REPORT_FILE"
```

## Related Documentation

- [Architecture Documentation](../architecture/README.md) - System design and components
- [Setup Guide](../setup/README.md) - Installation and configuration
- [API Documentation](../../api-docs/openapi.yaml) - REST API specification
- [Runbooks](./runbooks/) - Detailed incident response procedures
- [Security Guide](./security.md) - Security operations and procedures

---

**Document Version**: 1.0
**Last Updated**: June 25, 2024
**Author**: Exalt Infrastructure Team
**Review Cycle**: Monthly
**On-Call Rotation**: infrastructure-team@exalt.com
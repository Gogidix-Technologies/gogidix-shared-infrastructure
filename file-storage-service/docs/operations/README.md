# File Storage Service Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the File Storage Service in production environments.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Monitoring and Alerting](#monitoring-and-alerting)
3. [Performance Management](#performance-management)
4. [Storage Management](#storage-management)
5. [Backup and Recovery](#backup-and-recovery)
6. [Security Operations](#security-operations)
7. [CDN Operations](#cdn-operations)
8. [Troubleshooting](#troubleshooting)
9. [Maintenance Procedures](#maintenance-procedures)
10. [Disaster Recovery](#disaster-recovery)

## Service Operations

### Service Management

#### Starting the Service

**Production Environment:**
```bash
# Using systemd
sudo systemctl start file-storage-service

# Using Docker
docker-compose -f docker-compose.prod.yml up -d file-storage-service

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n shared-infrastructure
```

#### Stopping the Service

```bash
# Graceful shutdown (allows 120s for upload completion)
sudo systemctl stop file-storage-service

# Force stop if needed
sudo systemctl kill file-storage-service

# Kubernetes
kubectl delete deployment file-storage-service -n shared-infrastructure
```

#### Service Status Checks

```bash
# System status
sudo systemctl status file-storage-service

# Health endpoints
curl http://localhost:8406/actuator/health
curl http://localhost:8406/actuator/health/readiness
curl http://localhost:8406/actuator/health/liveness

# Storage backend status
curl http://localhost:8406/api/v1/admin/storage/status

# Processing capacity
curl http://localhost:8406/api/v1/admin/processing/capacity
```

### Configuration Management

#### Environment-Specific Configurations

**Production (`application-prod.yml`):**
```yaml
spring:
  profiles: prod
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      idle-timeout: 300000
      max-lifetime: 1200000
  redis:
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 25
        max-idle: 10

storage:
  backends:
    s3:
      connection-pool-size: 50
      connection-timeout: 30s
      socket-timeout: 60s
      max-error-retry: 3
  
  processing:
    max-concurrent-uploads: 25
    max-concurrent-processing: 15
    timeout: 600s
    temp-cleanup-interval: 1800s

logging:
  level:
    root: INFO
    com.exalt.storage: INFO
  file:
    name: /var/log/file-storage/application.log
    max-file-size: 100MB
    max-history: 30
```

#### Runtime Configuration Updates

```bash
# Update upload limits
curl -X PUT http://localhost:8406/api/v1/admin/config/upload-limits \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"maxFileSize": "200MB", "maxConcurrentUploads": 20}'

# Update processing settings
curl -X PUT http://localhost:8406/api/v1/admin/config/processing \
  -H "Content-Type: application/json" \
  -d '{"enableThumbnails": true, "imageQuality": 90}'
```

## Monitoring and Alerting

### Key Performance Indicators (KPIs)

#### Service Health Metrics

| Metric | Normal Range | Alert Threshold | Critical Threshold |
|--------|--------------|-----------------|-------------------|
| Response Time | < 200ms | > 1s | > 3s |
| CPU Usage | < 70% | > 85% | > 95% |
| Memory Usage | < 75% | > 90% | > 95% |
| Disk I/O | < 80% | > 95% | > 98% |
| Upload Success Rate | > 99% | < 95% | < 90% |

#### Storage Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Storage Utilization | < 80% | > 90% |
| CDN Hit Rate | > 85% | < 70% |
| Average File Size | 1-50MB | > 100MB |
| Processing Queue Length | < 10 | > 50 |

### Custom Metrics

#### Prometheus Metrics Configuration

```java
// metrics/FileStorageMetrics.java
@Component
public class FileStorageMetrics {
    
    private final Counter filesUploaded = Counter.builder("files_uploaded_total")
            .description("Total number of files uploaded")
            .labelNames("file_type", "backend", "status")
            .register(Metrics.globalRegistry);
    
    private final Timer uploadDuration = Timer.builder("file_upload_duration")
            .description("Time taken to upload files")
            .labelNames("file_type", "size_category")
            .register(Metrics.globalRegistry);
    
    private final Gauge storageUtilization = Gauge.builder("storage_utilization_percentage")
            .description("Storage backend utilization")
            .labelNames("backend")
            .register(Metrics.globalRegistry);
    
    private final Counter processingErrors = Counter.builder("file_processing_errors_total")
            .description("Total file processing errors")
            .labelNames("operation", "error_type")
            .register(Metrics.globalRegistry);
    
    private final Gauge cdnHitRate = Gauge.builder("cdn_hit_rate_percentage")
            .description("CDN cache hit rate")
            .register(Metrics.globalRegistry);
}
```

#### Key Metrics to Monitor

```promql
# File upload rate
rate(files_uploaded_total[5m])

# Upload duration by file type
histogram_quantile(0.95, file_upload_duration_bucket{file_type="image"})

# Storage utilization
storage_utilization_percentage{backend="s3"}

# Processing error rate
rate(file_processing_errors_total[5m]) / rate(files_uploaded_total[5m]) * 100

# CDN performance
cdn_hit_rate_percentage

# Queue depth
file_processing_queue_size
```

### Grafana Dashboards

#### Dashboard Configuration

```json
{
  "dashboard": {
    "title": "File Storage Service - Operations",
    "panels": [
      {
        "title": "Upload Volume",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(files_uploaded_total[5m]) * 60"
          }
        ]
      },
      {
        "title": "Storage Utilization",
        "type": "gauge",
        "targets": [
          {
            "expr": "storage_utilization_percentage"
          }
        ]
      },
      {
        "title": "CDN Hit Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "cdn_hit_rate_percentage"
          }
        ]
      },
      {
        "title": "Processing Queue",
        "type": "graph",
        "targets": [
          {
            "expr": "file_processing_queue_size"
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
  - name: file-storage-service
    rules:
      - alert: FileStorageHighUploadLatency
        expr: histogram_quantile(0.95, file_upload_duration_bucket) > 5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High file upload latency"
          description: "95th percentile upload time is {{ $value }}s"

      - alert: FileStorageHighErrorRate
        expr: rate(file_processing_errors_total[5m]) / rate(files_uploaded_total[5m]) * 100 > 5
        for: 3m
        labels:
          severity: critical
        annotations:
          summary: "High file processing error rate"
          description: "Error rate is {{ $value }}%"

      - alert: FileStorageHighStorageUtilization
        expr: storage_utilization_percentage > 90
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "High storage utilization"
          description: "Storage utilization is {{ $value }}%"

      - alert: FileStorageLowCDNHitRate
        expr: cdn_hit_rate_percentage < 70
        for: 15m
        labels:
          severity: warning
        annotations:
          summary: "Low CDN hit rate"
          description: "CDN hit rate is {{ $value }}%"
```

## Performance Management

### Performance Optimization

#### JVM Tuning for File Operations

```bash
# Production JVM settings
export JAVA_OPTS="
  -Xmx6g
  -Xms3g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/file-storage/heapdumps/
  -Djava.io.tmpdir=/var/tmp/file-processing
  -XX:+UnlockExperimentalVMOptions
  -XX:+UseCGroupMemoryLimitForHeap
"
```

#### Storage Backend Optimization

```yaml
# Storage performance configuration
storage:
  backends:
    s3:
      multipart-threshold: 16MB
      multipart-part-size: 8MB
      transfer-acceleration: true
      connection-pool-size: 50
      max-connections: 200
    
  processing:
    thread-pool-size: 20
    queue-capacity: 100
    keep-alive-time: 60s
    
  caching:
    metadata-ttl: 3600s
    thumbnail-ttl: 86400s
    content-ttl: 604800s
```

#### Database Connection Pool Tuning

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      validation-timeout: 5000
      leak-detection-threshold: 60000
```

### Load Testing

#### Performance Test Scripts

```bash
# Artillery.js load test for file uploads
artillery run file-storage-load-test.yml

# k6 performance test
k6 run file-storage-performance.js

# Custom concurrent upload test
./scripts/concurrent-upload-test.sh --files 100 --concurrent 10
```

## Storage Management

### Storage Backend Operations

#### S3 Storage Management

```bash
# Monitor S3 storage usage
aws s3api list-objects-v2 --bucket your-file-storage-bucket \
  --query 'Contents[].Size' --output text | \
  awk '{s+=$1} END {print "Total Size: " s/1024/1024/1024 " GB"}'

# List large files
aws s3api list-objects-v2 --bucket your-file-storage-bucket \
  --query 'Contents[?Size > `104857600`].[Key,Size]' --output table

# Check storage class distribution
aws s3api list-objects-v2 --bucket your-file-storage-bucket \
  --query 'Contents[].StorageClass' --output text | sort | uniq -c
```

#### Storage Lifecycle Management

```bash
# Configure lifecycle policies
curl -X PUT http://localhost:8406/api/v1/admin/storage/lifecycle \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "rules": [
      {
        "name": "archive_old_files",
        "daysToArchive": 90,
        "daysToDelete": 2555
      }
    ]
  }'

# Monitor lifecycle transitions
curl -X GET http://localhost:8406/api/v1/admin/storage/lifecycle-status \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Capacity Management

```bash
# Get storage capacity report
curl -X GET http://localhost:8406/api/v1/admin/storage/capacity \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Set storage quotas
curl -X PUT http://localhost:8406/api/v1/admin/storage/quotas \
  -H "Content-Type: application/json" \
  -d '{
    "userQuota": "10GB",
    "organizationQuota": "1TB",
    "globalQuota": "100TB"
  }'
```

## Backup and Recovery

### Storage Backup Procedures

#### Database Backup

```bash
#!/bin/bash
# backup-file-metadata.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/file-storage"
DB_NAME="file_storage_db"

# Create metadata backup
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME > \
  $BACKUP_DIR/metadata_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/metadata_$DATE.sql

echo "Metadata backup completed: metadata_$DATE.sql.gz"
```

#### S3 Cross-Region Replication

```bash
#!/bin/bash
# setup-s3-replication.sh

# Enable versioning on source bucket
aws s3api put-bucket-versioning \
  --bucket your-file-storage-bucket \
  --versioning-configuration Status=Enabled

# Create replication rule
aws s3api put-bucket-replication \
  --bucket your-file-storage-bucket \
  --replication-configuration file://replication-config.json

echo "Cross-region replication configured"
```

#### Backup Verification

```bash
#!/bin/bash
# verify-backups.sh

# Check backup integrity
aws s3api head-object --bucket backup-bucket --key latest-backup.tar.gz

# Test restore procedure
./scripts/test-restore.sh --backup-date $(date -d "1 day ago" +%Y%m%d)

echo "Backup verification completed"
```

## Security Operations

### File Security Monitoring

#### Security Audit Commands

```bash
# Monitor file access patterns
grep "FILE_ACCESS" /var/log/file-storage/audit.log | \
  awk '{print $4, $7, $9}' | sort | uniq -c

# Check for suspicious upload patterns
grep "LARGE_UPLOAD\|BULK_UPLOAD" /var/log/file-storage/security.log

# Monitor virus scan results
grep "VIRUS_DETECTED\|MALWARE" /var/log/file-storage/security.log
```

#### Access Control Management

```bash
# Review file permissions
curl -X GET http://localhost:8406/api/v1/admin/security/permissions \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update access policies
curl -X PUT http://localhost:8406/api/v1/admin/security/policies \
  -H "Content-Type: application/json" \
  -d '{
    "defaultPolicy": "private",
    "publicAccess": false,
    "encryptionRequired": true
  }'
```

### Encryption Management

```bash
# Rotate encryption keys
curl -X POST http://localhost:8406/api/v1/admin/security/rotate-keys \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Check encryption status
curl -X GET http://localhost:8406/api/v1/admin/security/encryption-status \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Re-encrypt files with new keys
curl -X POST http://localhost:8406/api/v1/admin/security/re-encrypt \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## CDN Operations

### CDN Management

#### Cache Operations

```bash
# Invalidate CDN cache
aws cloudfront create-invalidation \
  --distribution-id YOUR_DISTRIBUTION_ID \
  --paths "/*"

# Get invalidation status
aws cloudfront get-invalidation \
  --distribution-id YOUR_DISTRIBUTION_ID \
  --id INVALIDATION_ID

# Monitor CDN metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/CloudFront \
  --metric-name Requests \
  --dimensions Name=DistributionId,Value=YOUR_DISTRIBUTION_ID \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Sum
```

#### CDN Configuration Updates

```bash
# Update cache behaviors
curl -X PUT http://localhost:8406/api/v1/admin/cdn/cache-policies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "imageCacheTTL": 31536000,
    "documentCacheTTL": 86400,
    "defaultCacheTTL": 3600
  }'

# Get CDN performance metrics
curl -X GET http://localhost:8406/api/v1/admin/cdn/metrics \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Upload Latency

**Symptoms:**
- Slow file upload times
- User complaints about performance
- High response times

**Investigation:**
```bash
# Check upload performance by file type
curl http://localhost:8406/api/v1/admin/performance/uploads

# Monitor storage backend response times
curl http://localhost:8406/api/v1/admin/storage/performance

# Check network connectivity
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8406/api/v1/health
```

**Solutions:**
- Scale up service instances
- Optimize storage backend connections
- Implement upload resumption
- Use multipart uploads for large files

#### 2. Storage Backend Failures

**Symptoms:**
- S3 connection errors
- Upload failures
- Service unavailable errors

**Investigation:**
```bash
# Test S3 connectivity
aws s3 ls s3://your-file-storage-bucket

# Check service credentials
aws sts get-caller-identity

# Review error logs
grep "S3\|STORAGE_ERROR" /var/log/file-storage/application.log
```

**Solutions:**
- Check AWS credentials and permissions
- Verify network connectivity
- Implement retry mechanisms
- Use backup storage backends

#### 3. CDN Performance Issues

**Symptoms:**
- Low cache hit rates
- Slow content delivery
- High bandwidth costs

**Investigation:**
```bash
# Check CDN metrics
curl http://localhost:8406/api/v1/admin/cdn/metrics

# Analyze cache performance
aws logs filter-log-events \
  --log-group-name /aws/cloudfront/distribution-logs \
  --filter-pattern "MISS"
```

### Log Analysis

#### Important Log Patterns

```bash
# Upload errors
grep -E "UPLOAD_ERROR|TIMEOUT|FAILED" /var/log/file-storage/application.log

# Storage backend issues
grep -E "S3_ERROR|CONNECTION_FAILED|TIMEOUT" /var/log/file-storage/application.log

# Security events
grep -E "VIRUS_DETECTED|BLOCKED|UNAUTHORIZED" /var/log/file-storage/security.log

# Performance issues
grep -E "SLOW_UPLOAD|HIGH_MEMORY|QUEUE_FULL" /var/log/file-storage/application.log
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-maintenance.sh

echo "=== File Storage Daily Report ===" > /tmp/maintenance.log

# Check service health
curl -f http://localhost:8406/actuator/health >> /tmp/maintenance.log

# Check storage utilization
curl http://localhost:8406/api/v1/admin/storage/utilization >> /tmp/maintenance.log

# Clean up temporary files
find /var/tmp/file-processing -name "*.tmp" -mtime +1 -delete

# Check recent errors
tail -100 /var/log/file-storage/application.log | grep ERROR >> /tmp/maintenance.log

# Send report
mail -s "File Storage Daily Report" storage-team@exalt.com < /tmp/maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-maintenance.sh

# Optimize database
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "VACUUM ANALYZE;"

# Clean up old metadata
curl -X POST http://localhost:8406/api/v1/admin/cleanup/metadata \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update CDN cache policies
curl -X POST http://localhost:8406/api/v1/admin/cdn/optimize \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Generate storage report
curl -X GET http://localhost:8406/api/v1/admin/reports/storage \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Update Procedures

#### Rolling Updates (Kubernetes)

```bash
# Update deployment with new image
kubectl set image deployment/file-storage-service \
  file-storage-service=file-storage-service:v1.4.0 \
  -n shared-infrastructure

# Monitor rollout
kubectl rollout status deployment/file-storage-service -n shared-infrastructure

# Rollback if necessary
kubectl rollout undo deployment/file-storage-service -n shared-infrastructure
```

## Disaster Recovery

### Recovery Time Objectives (RTO)

| Component | RTO Target | Recovery Procedure |
|-----------|------------|-------------------|
| Service Instance | 3 minutes | Auto-scaling/Health check restart |
| Database | 15 minutes | Restore from backup |
| S3 Storage | 0 minutes | Cross-region replication |
| CDN | 5 minutes | DNS failover |
| Complete System | 20 minutes | Full disaster recovery |

### Recovery Point Objectives (RPO)

| Data Type | RPO Target | Backup Frequency |
|-----------|------------|------------------|
| File Content | 0 minutes | Real-time replication |
| File Metadata | 15 minutes | Continuous backup |
| Configuration | 1 hour | Git versioning |
| Access Logs | 5 minutes | Real-time streaming |

### Emergency Procedures

1. **Storage Outage**: Automatic failover to backup region
2. **CDN Failure**: DNS switch to backup CDN
3. **Data Corruption**: Restore from cross-region backup
4. **Security Breach**: Immediate access suspension

### Emergency Contacts

- **Primary On-Call**: +1-555-0129 (storage-team-primary@exalt.com)
- **Secondary On-Call**: +1-555-0130 (storage-team-secondary@exalt.com)
- **AWS Support**: aws-support@exalt.com
- **Security Team**: security-emergency@exalt.com

## Compliance and Auditing

### Data Retention Compliance

```bash
# Configure retention policies
curl -X PUT http://localhost:8406/api/v1/admin/compliance/retention \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "defaultRetention": "7y",
    "legalHold": true,
    "autoDelete": false
  }'

# Generate compliance report
curl -X GET http://localhost:8406/api/v1/admin/compliance/report \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Audit Trail Management

```yaml
# Audit configuration
audit:
  enabled: true
  events:
    - FILE_UPLOAD
    - FILE_DOWNLOAD
    - FILE_DELETE
    - ACCESS_GRANTED
    - ACCESS_DENIED
    - ADMIN_ACTION
  retention-days: 2555  # 7 years
  encryption: true
  real-time-streaming: true
```

## Support and Escalation

### Support Levels

1. **L1 Support**: Basic service restart, health checks
2. **L2 Support**: Configuration changes, storage management
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
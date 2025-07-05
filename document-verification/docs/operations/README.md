# Document Verification Service Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the Document Verification Service in production environments.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Monitoring and Alerting](#monitoring-and-alerting)
3. [Performance Management](#performance-management)
4. [Backup and Recovery](#backup-and-recovery)
5. [Security Operations](#security-operations)
6. [ML Model Management](#ml-model-management)
7. [Troubleshooting](#troubleshooting)
8. [Maintenance Procedures](#maintenance-procedures)
9. [Disaster Recovery](#disaster-recovery)

## Service Operations

### Service Management

#### Starting the Service

**Production Environment:**
```bash
# Using systemd
sudo systemctl start document-verification

# Using Docker
docker-compose -f docker-compose.prod.yml up -d document-verification

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n shared-infrastructure
```

#### Stopping the Service

```bash
# Graceful shutdown (allows 60s for document processing completion)
sudo systemctl stop document-verification

# Force stop if needed
sudo systemctl kill document-verification

# Kubernetes
kubectl delete deployment document-verification -n shared-infrastructure
```

#### Service Status Checks

```bash
# System status
sudo systemctl status document-verification

# Health endpoints
curl http://localhost:8405/actuator/health
curl http://localhost:8405/actuator/health/readiness
curl http://localhost:8405/actuator/health/liveness

# ML model status
curl http://localhost:8405/api/v1/models/status

# Processing capacity
curl http://localhost:8405/api/v1/processing/capacity
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
        max-idle: 10

document:
  processing:
    max-concurrent: 20
    timeout: 600s
    temp-directory: /var/tmp/document-processing
    cleanup-interval: 3600s
  
  ml:
    gpu-enabled: true
    batch-size: 16
    model-cache-size: 5

logging:
  level:
    root: INFO
    com.gogidix.verification: INFO
  file:
    name: /var/log/document-verification/application.log
    max-file-size: 100MB
    max-history: 30
```

#### Runtime Configuration Updates

```bash
# Update processing limits
curl -X PUT http://localhost:8405/api/v1/admin/config/processing \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"maxConcurrent": 15, "timeout": "300s"}'

# Update ML model settings
curl -X PUT http://localhost:8405/api/v1/admin/config/ml \
  -H "Content-Type: application/json" \
  -d '{"confidenceThreshold": 0.85, "batchSize": 8}'
```

## Monitoring and Alerting

### Key Performance Indicators (KPIs)

#### Service Health Metrics

| Metric | Normal Range | Alert Threshold | Critical Threshold |
|--------|--------------|-----------------|-------------------|
| Response Time | < 500ms | > 2s | > 5s |
| CPU Usage | < 70% | > 85% | > 95% |
| Memory Usage | < 80% | > 90% | > 95% |
| GPU Usage | < 85% | > 95% | > 98% |
| Processing Success Rate | > 95% | < 90% | < 80% |

#### Business Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Document Processing Time | < 30s | > 60s |
| OCR Accuracy Rate | > 95% | < 90% |
| Fraud Detection Rate | 2-5% | > 10% |
| External API Response Time | < 3s | > 10s |
| Queue Length | < 50 | > 100 |

### Custom Metrics

#### Prometheus Metrics Configuration

```java
// metrics/DocumentVerificationMetrics.java
@Component
public class DocumentVerificationMetrics {
    
    private final Counter documentsProcessed = Counter.builder("documents_processed_total")
            .description("Total number of documents processed")
            .labelNames("document_type", "status", "source")
            .register(Metrics.globalRegistry);
    
    private final Timer processingDuration = Timer.builder("document_processing_duration")
            .description("Time taken to process documents")
            .labelNames("document_type", "stage")
            .register(Metrics.globalRegistry);
    
    private final Gauge ocrAccuracy = Gauge.builder("ocr_accuracy_percentage")
            .description("OCR accuracy percentage")
            .register(Metrics.globalRegistry);
    
    private final Counter fraudDetected = Counter.builder("fraud_detected_total")
            .description("Total number of fraud cases detected")
            .labelNames("document_type", "fraud_type")
            .register(Metrics.globalRegistry);
    
    private final Gauge mlModelConfidence = Gauge.builder("ml_model_confidence")
            .description("ML model prediction confidence")
            .labelNames("model_name", "document_type")
            .register(Metrics.globalRegistry);
}
```

#### Key Metrics to Monitor

```promql
# Document processing rate
rate(documents_processed_total[5m])

# Processing duration by type
histogram_quantile(0.95, document_processing_duration_bucket{document_type="passport"})

# OCR accuracy trend
avg_over_time(ocr_accuracy_percentage[1h])

# Fraud detection rate
rate(fraud_detected_total[5m]) / rate(documents_processed_total[5m]) * 100

# ML model performance
avg(ml_model_confidence{model_name="fraud_detector"})

# Queue depth
document_processing_queue_size
```

### Grafana Dashboards

#### Dashboard Configuration

```json
{
  "dashboard": {
    "title": "Document Verification Service - Operations",
    "panels": [
      {
        "title": "Processing Volume",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(documents_processed_total[5m]) * 60"
          }
        ]
      },
      {
        "title": "Processing Duration by Type",
        "type": "heatmap",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, document_processing_duration_bucket)"
          }
        ]
      },
      {
        "title": "ML Model Accuracy",
        "type": "stat",
        "targets": [
          {
            "expr": "avg(ml_model_confidence)"
          }
        ]
      },
      {
        "title": "Fraud Detection Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(fraud_detected_total[5m]) / rate(documents_processed_total[5m]) * 100"
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
  - name: document-verification
    rules:
      - alert: DocumentVerificationHighProcessingTime
        expr: histogram_quantile(0.95, document_processing_duration_bucket) > 60
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High document processing time"
          description: "95th percentile processing time is {{ $value }}s"

      - alert: DocumentVerificationLowOCRAccuracy
        expr: avg_over_time(ocr_accuracy_percentage[10m]) < 85
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "Low OCR accuracy detected"
          description: "OCR accuracy is {{ $value }}%"

      - alert: DocumentVerificationHighFraudRate
        expr: rate(fraud_detected_total[10m]) / rate(documents_processed_total[10m]) * 100 > 15
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Unusually high fraud detection rate"
          description: "Fraud rate is {{ $value }}%"

      - alert: DocumentVerificationQueueBacklog
        expr: document_processing_queue_size > 100
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Document processing queue backlog"
          description: "Queue size is {{ $value }} documents"
```

## Performance Management

### Performance Optimization

#### JVM Tuning for ML Workloads

```bash
# Production JVM settings
export JAVA_OPTS="
  -Xmx8g
  -Xms4g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/document-verification/heapdumps/
  -XX:+UnlockExperimentalVMOptions
  -XX:+UseCGroupMemoryLimitForHeap
"
```

#### ML Model Optimization

```yaml
# ML performance configuration
ml:
  optimization:
    gpu-memory-growth: true
    mixed-precision: true
    model-caching: true
    batch-inference: true
    async-prediction: true
  
  inference:
    thread-pool-size: 4
    queue-capacity: 50
    timeout: 30s
    
  models:
    fraud-detection:
      batch-size: 16
      confidence-threshold: 0.8
    classification:
      batch-size: 32
      confidence-threshold: 0.7
```

#### Database Connection Pool Tuning

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
      leak-detection-threshold: 60000
```

### Load Testing

#### Performance Test Scripts

```bash
# Artillery.js load test for document upload
artillery run document-verification-load-test.yml

# k6 performance test
k6 run document-verification-performance.js

# Custom load test script
./scripts/load-test-documents.sh --concurrent 20 --duration 300s
```

## Backup and Recovery

### Database Backup Procedures

#### Automated Backup Script

```bash
#!/bin/bash
# backup-document-verification.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/document-verification"
DB_NAME="document_verification_db"

# Create database backup
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME > \
  $BACKUP_DIR/db_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/db_$DATE.sql

# Backup document metadata (not actual documents)
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME -t document_metadata > \
  $BACKUP_DIR/metadata_$DATE.sql

# Remove backups older than 30 days
find $BACKUP_DIR -name "*.gz" -mtime +30 -delete

echo "Database backup completed: db_$DATE.sql.gz"
```

### ML Model Backup

```bash
#!/bin/bash
# backup-ml-models.sh

DATE=$(date +%Y%m%d_%H%M%S)
MODEL_DIR="/opt/models/document-verification"
BACKUP_DIR="/backup/document-verification/models"

# Create model backup
tar -czf $BACKUP_DIR/models_$DATE.tar.gz -C $MODEL_DIR .

# Backup model metadata
cp $MODEL_DIR/model-registry.json $BACKUP_DIR/registry_$DATE.json

echo "Model backup completed: models_$DATE.tar.gz"
```

### Document Storage Backup

```bash
#!/bin/bash
# backup-documents.sh (S3 to S3 backup)

aws s3 sync s3://document-verification-bucket \
  s3://document-verification-backup \
  --storage-class GLACIER

echo "Document storage backup completed"
```

## Security Operations

### Document Security Monitoring

#### Security Audit Commands

```bash
# Monitor document access patterns
grep "DOCUMENT_ACCESS" /var/log/document-verification/audit.log | \
  awk '{print $4, $7}' | sort | uniq -c

# Check for unusual processing patterns
grep "SUSPICIOUS_PATTERN" /var/log/document-verification/security.log

# Monitor fraud detection trends
grep "FRAUD_DETECTED" /var/log/document-verification/audit.log | \
  grep $(date +%Y-%m-%d) | wc -l
```

#### Data Privacy Compliance

```bash
# Check data retention compliance
curl -X GET http://localhost:8405/api/v1/admin/compliance/retention \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Execute GDPR deletion requests
curl -X DELETE http://localhost:8405/api/v1/admin/gdpr/delete-user \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123", "reason": "gdpr_request"}'

# Generate privacy compliance report
curl -X GET http://localhost:8405/api/v1/admin/compliance/report \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Encryption Management

```bash
# Rotate encryption keys
curl -X POST http://localhost:8405/api/v1/admin/security/rotate-keys \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Check encryption status
curl -X GET http://localhost:8405/api/v1/admin/security/encryption-status \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## ML Model Management

### Model Deployment and Updates

#### Model Version Management

```bash
# Deploy new model version
curl -X POST http://localhost:8405/api/v1/admin/models/deploy \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "modelName": "fraud_detector",
    "version": "v1.2.0",
    "modelPath": "/models/fraud_detector_v1.2.0.model",
    "rolloutStrategy": "canary"
  }'

# Monitor model performance
curl -X GET http://localhost:8405/api/v1/admin/models/performance \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Rollback model if needed
curl -X POST http://localhost:8405/api/v1/admin/models/rollback \
  -H "Content-Type: application/json" \
  -d '{"modelName": "fraud_detector", "targetVersion": "v1.1.0"}'
```

#### Model A/B Testing

```bash
# Start A/B test
curl -X POST http://localhost:8405/api/v1/admin/models/ab-test \
  -H "Content-Type: application/json" \
  -d '{
    "modelA": "fraud_detector_v1.1.0",
    "modelB": "fraud_detector_v1.2.0",
    "trafficSplit": 0.1,
    "duration": "7d"
  }'

# Get A/B test results
curl -X GET http://localhost:8405/api/v1/admin/models/ab-test/results \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Model Performance Monitoring

```bash
# Monitor model drift
curl -X GET http://localhost:8405/api/v1/admin/models/drift \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Generate model performance report
curl -X GET http://localhost:8405/api/v1/admin/models/performance-report \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Processing Times

**Symptoms:**
- Document processing takes longer than 60 seconds
- Queue backlog building up
- User complaints about slow verification

**Investigation:**
```bash
# Check processing stages performance
curl http://localhost:8405/api/v1/admin/processing/stage-performance

# Monitor GPU utilization
nvidia-smi

# Check document complexity
grep "COMPLEX_DOCUMENT" /var/log/document-verification/processing.log
```

**Solutions:**
- Scale up processing capacity
- Optimize ML model inference
- Implement document pre-processing
- Add more GPU resources

#### 2. OCR Accuracy Issues

**Symptoms:**
- Low confidence scores
- Extraction errors
- User reports of incorrect data

**Investigation:**
```bash
# Check OCR performance by document type
curl http://localhost:8405/api/v1/admin/ocr/accuracy-by-type

# Review failed extractions
grep "OCR_FAILED" /var/log/document-verification/processing.log | tail -20
```

**Solutions:**
- Update Tesseract version
- Improve image preprocessing
- Add language-specific models
- Implement manual review workflow

#### 3. ML Model Performance Degradation

**Symptoms:**
- Declining accuracy
- Increased false positives/negatives
- Model drift alerts

**Investigation:**
```bash
# Check model metrics
curl http://localhost:8405/api/v1/admin/models/metrics

# Review prediction confidence distribution
curl http://localhost:8405/api/v1/admin/models/confidence-distribution
```

**Solutions:**
- Retrain models with recent data
- Implement model ensemble
- Update feature engineering
- Increase training data quality

### Log Analysis

#### Important Log Patterns

```bash
# Processing errors
grep -E "PROCESSING_ERROR|TIMEOUT|FAILED" /var/log/document-verification/application.log

# Security events
grep -E "FRAUD_DETECTED|SUSPICIOUS|BLOCKED" /var/log/document-verification/security.log

# Performance issues
grep -E "SLOW_PROCESSING|HIGH_MEMORY|GPU_ERROR" /var/log/document-verification/application.log

# External API issues
grep -E "EXTERNAL_API|TIMEOUT|RATE_LIMIT" /var/log/document-verification/application.log
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-maintenance.sh

echo "=== Document Verification Daily Report ===" > /tmp/maintenance.log

# Check service health
curl -f http://localhost:8405/actuator/health >> /tmp/maintenance.log

# Check processing statistics
curl http://localhost:8405/api/v1/admin/statistics/daily >> /tmp/maintenance.log

# Check model performance
curl http://localhost:8405/api/v1/admin/models/daily-performance >> /tmp/maintenance.log

# Clean up temporary files
find /var/tmp/document-processing -name "*.tmp" -mtime +1 -delete

# Check recent errors
tail -100 /var/log/document-verification/application.log | grep ERROR >> /tmp/maintenance.log

# Send report
mail -s "Document Verification Daily Report" verification-team@gogidix.com < /tmp/maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-maintenance.sh

# Update ML models if new versions available
./scripts/check-model-updates.sh

# Optimize database
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "VACUUM ANALYZE;"

# Clean up old processed documents (metadata only)
curl -X POST http://localhost:8405/api/v1/admin/cleanup/old-documents \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Generate compliance report
curl -X GET http://localhost:8405/api/v1/admin/compliance/weekly-report \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Update Procedures

#### Rolling Updates (Kubernetes)

```bash
# Update deployment with new image
kubectl set image deployment/document-verification \
  document-verification=document-verification:v1.3.0 \
  -n shared-infrastructure

# Monitor rollout
kubectl rollout status deployment/document-verification -n shared-infrastructure

# Rollback if necessary
kubectl rollout undo deployment/document-verification -n shared-infrastructure
```

## Disaster Recovery

### Recovery Time Objectives (RTO)

| Component | RTO Target | Recovery Procedure |
|-----------|------------|-------------------|
| Service Instance | 5 minutes | Auto-scaling/Health check restart |
| Database | 20 minutes | Restore from backup |
| ML Models | 10 minutes | Load from backup |
| Document Storage | 30 minutes | S3 replication |
| Complete System | 45 minutes | Full disaster recovery |

### Recovery Point Objectives (RPO)

| Data Type | RPO Target | Backup Frequency |
|-----------|------------|------------------|
| Document Metadata | 15 minutes | Continuous replication |
| Processing Results | 1 hour | Hourly snapshots |
| ML Models | 24 hours | Daily backups |
| Audit Logs | 5 minutes | Real-time streaming |

### Emergency Procedures

1. **Service Outage**: Automatic failover to standby region
2. **Data Corruption**: Restore from backup
3. **Security Breach**: Isolate service, audit access
4. **Model Failure**: Rollback to previous version

### Emergency Contacts

- **Primary On-Call**: +1-555-0127 (verification-team-primary@gogidix.com)
- **Secondary On-Call**: +1-555-0128 (verification-team-secondary@gogidix.com)
- **ML Team**: ml-team@gogidix.com
- **Security Team**: security-emergency@gogidix.com
- **Data Protection Officer**: dpo@gogidix.com

## Compliance and Auditing

### GDPR Compliance Operations

```bash
# Handle data subject access requests
curl -X GET http://localhost:8405/api/v1/admin/gdpr/user-data \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"userId": "user123"}'

# Process deletion requests
curl -X DELETE http://localhost:8405/api/v1/admin/gdpr/delete-user \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"userId": "user123", "reason": "user_request"}'

# Generate data processing report
curl -X GET http://localhost:8405/api/v1/admin/gdpr/processing-report \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Audit Trail Management

```yaml
# Audit configuration
audit:
  enabled: true
  events:
    - DOCUMENT_UPLOAD
    - VERIFICATION_COMPLETE
    - FRAUD_DETECTED
    - DATA_ACCESS
    - MODEL_PREDICTION
    - ADMIN_ACTION
  retention-days: 2555  # 7 years
  encryption: true
  real-time-streaming: true
```

## Support and Escalation

### Support Levels

1. **L1 Support**: Basic service restart, health checks
2. **L2 Support**: Configuration changes, model updates
3. **L3 Support**: Code changes, ML model development

### Escalation Matrix

| Issue Severity | Initial Response | Resolution Target | Escalation Path |
|----------------|------------------|-------------------|-----------------|
| Critical | 10 minutes | 1 hour | L1 → L2 → L3 → Management |
| High | 30 minutes | 4 hours | L1 → L2 → L3 |
| Medium | 2 hours | 24 hours | L1 → L2 |
| Low | 24 hours | 1 week | L1 |

---

*Last Updated: 2024-06-25*
*Document Version: 1.0*
*Review Schedule: Monthly*
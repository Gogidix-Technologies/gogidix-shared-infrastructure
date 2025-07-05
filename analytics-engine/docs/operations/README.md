# Analytics Engine Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the Analytics Engine service in production environments.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Data Pipeline Operations](#data-pipeline-operations)
3. [Monitoring and Alerting](#monitoring-and-alerting)
4. [Performance Management](#performance-management)
5. [Data Management](#data-management)
6. [Machine Learning Operations](#machine-learning-operations)
7. [Backup and Recovery](#backup-and-recovery)
8. [Security Operations](#security-operations)
9. [Troubleshooting](#troubleshooting)
10. [Maintenance Procedures](#maintenance-procedures)

## Service Operations

### Service Management

#### Starting the Analytics Engine

**Production Environment:**
```bash
# Using systemd
sudo systemctl start analytics-engine

# Using Docker
docker-compose -f docker-compose.prod.yml up -d analytics-engine

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n analytics
```

#### Stopping the Service

```bash
# Graceful shutdown (allows data processing to complete)
sudo systemctl stop analytics-engine

# Force stop if needed
sudo systemctl kill analytics-engine

# Kubernetes
kubectl delete deployment analytics-engine -n analytics
```

#### Service Status Checks

```bash
# System status
sudo systemctl status analytics-engine

# Health endpoints
curl http://localhost:8200/actuator/health
curl http://localhost:8200/actuator/health/readiness
curl http://localhost:8200/actuator/health/liveness

# Analytics-specific health
curl http://localhost:8200/api/v1/analytics/health
curl http://localhost:8200/api/v1/analytics/pipelines/status
curl http://localhost:8200/api/v1/analytics/ml/models/status
```

### Configuration Management

#### Environment-Specific Configurations

**Production (`application-prod.yml`):**
```yaml
spring:
  profiles: prod
  kafka:
    consumer:
      max-poll-records: 1000
      session-timeout-ms: 30000
    producer:
      batch-size: 65536
      linger-ms: 5

analytics:
  clickhouse:
    connection-pool:
      max-size: 50
      min-idle: 10
  spark:
    executor-memory: "8g"
    executor-cores: 4
    max-executors: 20
  ml:
    training:
      batch-size: 10000
      max-iterations: 5000

logging:
  level:
    root: INFO
    com.gogidix.analytics: INFO
    org.apache.spark: WARN
  file:
    name: /var/log/analytics-engine/application.log
    max-file-size: 100MB
    max-history: 30
```

#### Runtime Configuration Updates

```bash
# Update analytics configuration
curl -X POST http://localhost:8200/actuator/refresh \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update pipeline configuration
curl -X PUT http://localhost:8200/api/v1/analytics/pipelines/config \
  -H "Content-Type: application/json" \
  -d '{"batchSize": 5000, "windowSize": "2m"}'

# Update ML model configuration
curl -X PUT http://localhost:8200/api/v1/analytics/ml/config \
  -H "Content-Type: application/json" \
  -d '{"modelTimeout": "10s", "maxConcurrent": 200}'
```

## Data Pipeline Operations

### Pipeline Management

#### Pipeline Status Monitoring

```bash
# Check all pipeline statuses
curl http://localhost:8200/api/v1/analytics/pipelines/status

# Check specific pipeline
curl http://localhost:8200/api/v1/analytics/pipelines/sales-analytics/status

# View pipeline metrics
curl http://localhost:8200/api/v1/analytics/pipelines/metrics
```

#### Pipeline Control Operations

```bash
# Start pipeline
curl -X POST http://localhost:8200/api/v1/analytics/pipelines/sales-analytics/start

# Stop pipeline
curl -X POST http://localhost:8200/api/v1/analytics/pipelines/sales-analytics/stop

# Restart pipeline
curl -X POST http://localhost:8200/api/v1/analytics/pipelines/sales-analytics/restart

# Pause pipeline
curl -X POST http://localhost:8200/api/v1/analytics/pipelines/sales-analytics/pause
```

#### Pipeline Error Handling

```bash
# View pipeline errors
curl http://localhost:8200/api/v1/analytics/pipelines/sales-analytics/errors

# Retry failed batches
curl -X POST http://localhost:8200/api/v1/analytics/pipelines/sales-analytics/retry \
  -H "Content-Type: application/json" \
  -d '{"batchIds": ["batch-123", "batch-124"]}'

# Clear error state
curl -X POST http://localhost:8200/api/v1/analytics/pipelines/sales-analytics/clear-errors
```

### Airflow Pipeline Operations

```bash
# Check Airflow DAG status
airflow dags list

# Trigger DAG manually
airflow dags trigger analytics_daily_pipeline

# View DAG run status
airflow dags state analytics_daily_pipeline 2024-06-24

# Check task status
airflow tasks state analytics_daily_pipeline extract_sales_data 2024-06-24
```

## Monitoring and Alerting

### Key Performance Indicators (KPIs)

#### Service Health Metrics

| Metric | Normal Range | Alert Threshold | Critical Threshold |
|--------|--------------|-----------------|-------------------|
| Response Time | < 500ms | > 1s | > 2s |
| CPU Usage | < 70% | > 80% | > 90% |
| Memory Usage | < 75% | > 85% | > 95% |
| Disk I/O | < 80% | > 90% | > 95% |
| JVM Heap Usage | < 80% | > 90% | > 95% |

#### Data Pipeline Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Pipeline Success Rate | > 99% | < 95% |
| Data Processing Lag | < 5 minutes | > 15 minutes |
| Event Processing Rate | > 1000/sec | < 500/sec |
| Data Quality Score | > 95% | < 90% |

#### ML Model Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Model Accuracy | > 85% | < 80% |
| Prediction Latency | < 100ms | > 300ms |
| Model Drift Score | < 0.1 | > 0.3 |
| Feature Availability | > 99% | < 95% |

### Prometheus Metrics

#### Custom Analytics Metrics

```yaml
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
      service: analytics-engine
      environment: ${SPRING_PROFILES_ACTIVE}
    distribution:
      percentiles-histogram:
        http.server.requests: true
        analytics.query.duration: true
```

#### Key Metrics to Monitor

```promql
# Query processing time
histogram_quantile(0.95, analytics_query_duration_seconds_bucket)

# Event processing rate
rate(analytics_events_processed_total[5m])

# Pipeline success rate
rate(analytics_pipeline_success_total[5m]) / rate(analytics_pipeline_total[5m])

# ML model accuracy
analytics_ml_model_accuracy{model="churn_prediction"}

# Data freshness
time() - analytics_data_last_update_timestamp
```

### Grafana Dashboards

#### Analytics Overview Dashboard

```json
{
  "dashboard": {
    "title": "Analytics Engine - Overview",
    "panels": [
      {
        "title": "Event Processing Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(analytics_events_processed_total[5m])"
          }
        ]
      },
      {
        "title": "Pipeline Health",
        "type": "stat",
        "targets": [
          {
            "expr": "analytics_pipeline_status"
          }
        ]
      },
      {
        "title": "Data Quality Score",
        "type": "gauge",
        "targets": [
          {
            "expr": "analytics_data_quality_score"
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
groups:
  - name: analytics-engine
    rules:
      - alert: AnalyticsHighResponseTime
        expr: histogram_quantile(0.95, analytics_query_duration_seconds_bucket) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High analytics query response time"
          description: "95th percentile query time is {{ $value }}s"

      - alert: AnalyticsPipelineFailure
        expr: rate(analytics_pipeline_failure_total[5m]) > 0.1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Analytics pipeline failures detected"
          description: "Pipeline failure rate: {{ $value }}/min"

      - alert: AnalyticsDataLag
        expr: time() - analytics_data_last_update_timestamp > 900
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Analytics data is stale"
          description: "Data last updated {{ $value }}s ago"

      - alert: MLModelDrift
        expr: analytics_ml_model_drift_score > 0.3
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "ML model drift detected"
          description: "Model {{ $labels.model }} drift score: {{ $value }}"
```

## Performance Management

### Performance Optimization

#### JVM Tuning for Analytics Workloads

```bash
# Production JVM settings
export JAVA_OPTS="
  -Xmx16g
  -Xms16g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+OptimizeStringConcat
  -XX:+UseCompressedOops
  -XX:+UseCompressedClassPointers
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/analytics-engine/heapdumps/
  -Dcom.sun.management.jmxremote=true
  -Dcom.sun.management.jmxremote.port=9999
  -Dcom.sun.management.jmxremote.authenticate=false
  -Dcom.sun.management.jmxremote.ssl=false
"
```

#### Spark Performance Tuning

```bash
# Spark configuration for analytics workloads
export SPARK_CONF="
  --conf spark.sql.adaptive.enabled=true
  --conf spark.sql.adaptive.coalescePartitions.enabled=true
  --conf spark.sql.adaptive.skewJoin.enabled=true
  --conf spark.serializer=org.apache.spark.serializer.KryoSerializer
  --conf spark.sql.execution.arrow.pyspark.enabled=true
  --conf spark.executor.memory=8g
  --conf spark.executor.cores=4
  --conf spark.executor.instances=10
  --conf spark.driver.memory=4g
  --conf spark.driver.maxResultSize=2g
"
```

#### ClickHouse Optimization

```sql
-- Optimize ClickHouse for analytics queries
SET max_memory_usage = 20000000000;
SET max_bytes_before_external_group_by = 20000000000;
SET max_bytes_before_external_sort = 20000000000;
SET max_threads = 16;
SET max_execution_time = 600;

-- Create materialized views for common queries
CREATE MATERIALIZED VIEW daily_sales_mv
ENGINE = SummingMergeTree()
ORDER BY (date, product_id)
AS SELECT
  toDate(order_date) as date,
  product_id,
  sum(order_total) as total_sales,
  count() as order_count
FROM orders
GROUP BY date, product_id;
```

### Load Testing

#### Analytics Load Test Scripts

```bash
# Apache Bench for API endpoints
ab -n 10000 -c 100 http://localhost:8200/api/v1/analytics/metrics

# Custom load test for analytics queries
python scripts/load_test_analytics.py \
  --concurrent-users 50 \
  --test-duration 600 \
  --query-types sales,customer,inventory

# Kafka load test for event ingestion
kafka-producer-perf-test.sh \
  --topic analytics-events \
  --num-records 1000000 \
  --record-size 1000 \
  --throughput 10000 \
  --producer-props bootstrap.servers=localhost:9092
```

## Data Management

### Data Quality Monitoring

#### Data Quality Checks

```bash
# Run data quality validation
curl -X POST http://localhost:8200/api/v1/analytics/data-quality/validate \
  -H "Content-Type: application/json" \
  -d '{
    "dataset": "orders",
    "checks": ["completeness", "uniqueness", "validity"]
  }'

# Check data freshness
curl http://localhost:8200/api/v1/analytics/data-quality/freshness

# View data quality report
curl http://localhost:8200/api/v1/analytics/data-quality/report
```

#### Data Lineage Tracking

```bash
# View data lineage for a dataset
curl http://localhost:8200/api/v1/analytics/lineage/orders

# Track data transformations
curl http://localhost:8200/api/v1/analytics/lineage/transformations/sales_summary
```

### Data Retention Management

```bash
# Configure data retention policies
curl -X PUT http://localhost:8200/api/v1/analytics/retention \
  -H "Content-Type: application/json" \
  -d '{
    "rawEvents": "90d",
    "aggregatedMetrics": "1y",
    "mlModels": "2y"
  }'

# Run data cleanup
curl -X POST http://localhost:8200/api/v1/analytics/cleanup \
  -H "Content-Type: application/json" \
  -d '{"dryRun": false}'
```

## Machine Learning Operations

### ML Model Management

#### Model Deployment

```bash
# Deploy new model version
curl -X POST http://localhost:8200/api/v1/analytics/ml/models/deploy \
  -H "Content-Type: application/json" \
  -d '{
    "modelName": "churn_prediction",
    "modelVersion": "v2.1",
    "deploymentStrategy": "canary"
  }'

# Check model status
curl http://localhost:8200/api/v1/analytics/ml/models/churn_prediction/status

# Rollback model
curl -X POST http://localhost:8200/api/v1/analytics/ml/models/churn_prediction/rollback
```

#### Model Performance Monitoring

```bash
# Check model accuracy
curl http://localhost:8200/api/v1/analytics/ml/models/churn_prediction/metrics

# Detect model drift
curl http://localhost:8200/api/v1/analytics/ml/models/churn_prediction/drift

# View prediction distribution
curl http://localhost:8200/api/v1/analytics/ml/models/churn_prediction/predictions/distribution
```

#### Model Retraining

```bash
# Trigger model retraining
curl -X POST http://localhost:8200/api/v1/analytics/ml/models/churn_prediction/retrain \
  -H "Content-Type: application/json" \
  -d '{
    "trainingData": "2024-01-01/2024-06-01",
    "autoDeployIfBetter": true
  }'

# Check training status
curl http://localhost:8200/api/v1/analytics/ml/training/status
```

## Backup and Recovery

### Data Backup Procedures

#### Analytics Database Backup

```bash
#!/bin/bash
# backup-clickhouse.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/analytics-engine"
DB_NAME="analytics_db"

# Create ClickHouse backup
clickhouse-client --query "BACKUP DATABASE $DB_NAME TO Disk('backup', '$DATE/clickhouse_backup.zip')"

# Export analytics configurations
curl http://localhost:8200/api/v1/analytics/export/config > $BACKUP_DIR/analytics_config_$DATE.json

# Backup ML models
mlflow artifacts download --run-id $(mlflow runs list --experiment-id 1 --max-results 1 --order-by "created DESC" --output-format json | jq -r '.[0].run_id') --artifact-path models --dst-path $BACKUP_DIR/models_$DATE/

echo "Analytics backup completed: $DATE"
```

#### Backup Schedule

```cron
# Crontab entries
# Full backup daily at 2 AM
0 2 * * * /opt/analytics-engine/scripts/backup-clickhouse.sh >> /var/log/backup.log 2>&1

# Incremental backup every 6 hours
0 */6 * * * /opt/analytics-engine/scripts/backup-incremental.sh >> /var/log/backup.log 2>&1

# ML model backup weekly
0 3 * * 0 /opt/analytics-engine/scripts/backup-ml-models.sh >> /var/log/backup.log 2>&1
```

### Recovery Procedures

#### Data Recovery

```bash
# Stop analytics engine
sudo systemctl stop analytics-engine

# Restore ClickHouse database
clickhouse-client --query "RESTORE DATABASE analytics_db FROM Disk('backup', '20240624_020000/clickhouse_backup.zip')"

# Restore analytics configuration
curl -X POST http://localhost:8200/api/v1/analytics/import/config \
  -H "Content-Type: application/json" \
  -d @/backup/analytics-engine/analytics_config_20240624_020000.json

# Restore ML models
mlflow artifacts upload --run-id new-run-id --artifact-path models --src-path /backup/analytics-engine/models_20240624_020000/

# Restart service
sudo systemctl start analytics-engine
```

## Security Operations

### Security Monitoring

#### Access Log Analysis

```bash
# Monitor analytics API access
tail -f /var/log/analytics-engine/access.log | grep "POST /api/v1/analytics"

# Track data export activities
grep "EXPORT" /var/log/analytics-engine/audit.log | tail -20

# Monitor ML model access
grep "ML_MODEL_ACCESS" /var/log/analytics-engine/audit.log
```

#### Data Privacy Compliance

```bash
# GDPR data deletion request
curl -X DELETE http://localhost:8200/api/v1/analytics/data/customer/123 \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Anonymize customer data
curl -X POST http://localhost:8200/api/v1/analytics/data/anonymize \
  -H "Content-Type: application/json" \
  -d '{"customerId": "123", "retentionPeriod": "7y"}'

# Generate privacy report
curl http://localhost:8200/api/v1/analytics/privacy/report \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Memory Usage in Spark Jobs

**Symptoms:**
- OutOfMemoryError in Spark executors
- Job failures with memory-related errors

**Investigation:**
```bash
# Check Spark UI for memory usage
open http://localhost:4040

# Monitor memory usage
free -h
jstat -gc $(pgrep -f spark)
```

**Solutions:**
- Increase executor memory: `--executor-memory 16g`
- Optimize data partitioning: `df.repartition(200)`
- Use broadcast joins for small tables

#### 2. ClickHouse Query Performance Issues

**Symptoms:**
- Slow query execution
- High CPU usage on ClickHouse server

**Investigation:**
```bash
# Check running queries
clickhouse-client --query "SELECT * FROM system.processes"

# Analyze query execution plan
clickhouse-client --query "EXPLAIN SYNTAX SELECT ..."
```

**Solutions:**
- Add appropriate indexes
- Optimize table structure with proper ORDER BY
- Use materialized views for complex aggregations

#### 3. Kafka Consumer Lag

**Symptoms:**
- Increasing consumer lag
- Delayed data processing

**Investigation:**
```bash
# Check consumer lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group analytics-consumer-group

# Monitor topic metrics
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic analytics-events
```

**Solutions:**
- Increase consumer parallelism
- Optimize consumer configuration
- Scale up consumer instances

#### 4. ML Model Performance Degradation

**Symptoms:**
- Decreasing model accuracy
- Model drift alerts

**Investigation:**
```bash
# Check model metrics
curl http://localhost:8200/api/v1/analytics/ml/models/metrics

# Analyze feature distribution
python scripts/analyze_feature_drift.py
```

**Solutions:**
- Retrain model with recent data
- Update feature engineering pipeline
- Implement online learning

### Performance Debugging

```bash
# Generate heap dump for analysis
jcmd $(pgrep -f analytics-engine) GC.run_finalization
jcmd $(pgrep -f analytics-engine) VM.dump_heap /tmp/analytics-heap.hprof

# Analyze with Eclipse MAT or JVisualVM
# Profile Spark application
spark-submit --conf spark.sql.execution.arrow.pyspark.enabled=true --conf spark.python.profile=true your_script.py
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-maintenance.sh

echo "=== Analytics Engine Daily Maintenance ===" > /tmp/analytics-maintenance.log

# Check service health
curl -f http://localhost:8200/actuator/health >> /tmp/analytics-maintenance.log

# Check data pipeline status
curl http://localhost:8200/api/v1/analytics/pipelines/status >> /tmp/analytics-maintenance.log

# Check ML model performance
curl http://localhost:8200/api/v1/analytics/ml/models/metrics >> /tmp/analytics-maintenance.log

# Check disk space
df -h >> /tmp/analytics-maintenance.log

# Check recent errors
tail -100 /var/log/analytics-engine/application.log | grep ERROR >> /tmp/analytics-maintenance.log

# Send report
mail -s "Analytics Engine Daily Report" analytics-team@gogidix.com < /tmp/analytics-maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-maintenance.sh

# Optimize ClickHouse tables
clickhouse-client --query "OPTIMIZE TABLE events FINAL"
clickhouse-client --query "OPTIMIZE TABLE metrics FINAL"

# Clean up old Spark logs
find /opt/spark/logs -name "*.log" -mtime +7 -delete

# Update ML model performance baselines
python scripts/update_model_baselines.py

# Run data quality checks
python scripts/data_quality_report.py

# Cleanup temporary files
find /tmp -name "analytics-*" -mtime +1 -delete
```

### Update Procedures

#### Rolling Updates (Kubernetes)

```bash
# Update deployment with new image
kubectl set image deployment/analytics-engine \
  analytics-engine=analytics-engine:v2.1.0 \
  -n analytics

# Monitor rollout
kubectl rollout status deployment/analytics-engine -n analytics

# Rollback if necessary
kubectl rollout undo deployment/analytics-engine -n analytics
```

#### Blue-Green Deployment

```bash
# Deploy to green environment
kubectl apply -f k8s/deployment-green.yaml

# Run validation tests
./scripts/validate-analytics-deployment.sh green

# Switch traffic
kubectl patch service analytics-engine \
  -p '{"spec":{"selector":{"version":"green"}}}'

# Monitor and rollback if needed
kubectl patch service analytics-engine \
  -p '{"spec":{"selector":{"version":"blue"}}}'
```

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Monthly*
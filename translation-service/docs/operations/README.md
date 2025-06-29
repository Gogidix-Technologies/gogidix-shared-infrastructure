# Translation Service - Operations Guide

## Table of Contents

- [Overview](#overview)
- [Monitoring and Observability](#monitoring-and-observability)
- [Maintenance Procedures](#maintenance-procedures)
- [Troubleshooting](#troubleshooting)
- [Translation Quality Management](#translation-quality-management)
- [Performance Optimization](#performance-optimization)
- [Security Operations](#security-operations)
- [Backup and Recovery](#backup-and-recovery)
- [Scaling Operations](#scaling-operations)
- [Incident Response](#incident-response)

## Overview

This operations guide provides comprehensive procedures for monitoring, maintaining, and troubleshooting the Translation Service in production environments. The guide covers day-to-day operations, performance optimization, quality management, and incident response procedures.

### Operational Responsibilities

- **Platform Team**: Infrastructure, monitoring, scaling, security
- **Translation Team**: Quality management, provider optimization, language support
- **DevOps Team**: Deployment, CI/CD, automation, incident response
- **Security Team**: Security monitoring, compliance, access management

### Service Level Objectives (SLOs)

```yaml
Translation Service SLOs:
  Availability: 99.9% (8.76 hours downtime/year)
  Performance:
    - Cached translations: < 100ms (p95)
    - Simple text translation: < 2s (p95)
    - Complex document translation: < 30s (p95)
  Quality:
    - Translation accuracy: > 95% for premium content
    - Human review coverage: 100% for legal/financial content
  Capacity:
    - Peak load: 10,000 requests/minute
    - Concurrent users: 5,000
    - Translation throughput: 1M characters/hour
```

## Monitoring and Observability

### Metrics and KPIs

#### Application Metrics

```yaml
Core Metrics:
  - translation.requests.total (Counter)
  - translation.requests.duration (Histogram)
  - translation.cache.hit.ratio (Gauge)
  - translation.provider.success.rate (Gauge)
  - translation.quality.score (Histogram)
  - translation.cost.per.request (Gauge)
  - translation.queue.size (Gauge)
  - translation.processing.time (Histogram)

Business Metrics:
  - translation.languages.supported (Gauge)
  - translation.daily.volume (Counter)
  - translation.cost.daily (Gauge)
  - translation.accuracy.rate (Gauge)
  - translation.human.review.pending (Gauge)
```

#### System Metrics

```yaml
Infrastructure Metrics:
  - jvm.memory.used (Gauge)
  - jvm.gc.pause (Timer)
  - system.cpu.usage (Gauge)
  - system.memory.usage (Gauge)
  - hikaricp.connections.active (Gauge)
  - redis.connected.slaves (Gauge)
  - kafka.consumer.lag (Gauge)
```

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "translation-service-rules.yml"

scrape_configs:
  - job_name: 'translation-service'
    static_configs:
      - targets: ['translation-service:8094']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    scrape_timeout: 10s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### Alerting Rules

```yaml
# translation-service-rules.yml
groups:
- name: translation-service-alerts
  rules:
  
  # Availability Alerts
  - alert: TranslationServiceDown
    expr: up{job="translation-service"} == 0
    for: 1m
    labels:
      severity: critical
      service: translation-service
    annotations:
      summary: "Translation Service is down"
      description: "Translation Service has been down for more than 1 minute"
      runbook_url: "https://docs.exalt.com/runbooks/translation-service-down"

  - alert: TranslationServiceHighErrorRate
    expr: rate(translation_requests_total{status="error"}[5m]) / rate(translation_requests_total[5m]) > 0.05
    for: 2m
    labels:
      severity: warning
      service: translation-service
    annotations:
      summary: "High error rate in Translation Service"
      description: "Error rate is {{ $value | humanizePercentage }} over the last 5 minutes"

  # Performance Alerts
  - alert: TranslationServiceHighLatency
    expr: histogram_quantile(0.95, rate(translation_requests_duration_seconds_bucket[5m])) > 2
    for: 5m
    labels:
      severity: warning
      service: translation-service
    annotations:
      summary: "High latency in Translation Service"
      description: "95th percentile latency is {{ $value }}s over the last 5 minutes"

  - alert: TranslationCacheLowHitRate
    expr: translation_cache_hit_ratio < 0.8
    for: 10m
    labels:
      severity: warning
      service: translation-service
    annotations:
      summary: "Low cache hit rate"
      description: "Cache hit rate is {{ $value | humanizePercentage }}"

  # Resource Alerts
  - alert: TranslationServiceHighMemoryUsage
    expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
    for: 5m
    labels:
      severity: warning
      service: translation-service
    annotations:
      summary: "High memory usage"
      description: "Memory usage is {{ $value | humanizePercentage }}"

  - alert: TranslationServiceHighCPUUsage
    expr: system_cpu_usage > 0.8
    for: 5m
    labels:
      severity: warning
      service: translation-service
    annotations:
      summary: "High CPU usage"
      description: "CPU usage is {{ $value | humanizePercentage }}"

  # Provider Alerts
  - alert: TranslationProviderFailure
    expr: translation_provider_success_rate < 0.95
    for: 5m
    labels:
      severity: warning
      service: translation-service
    annotations:
      summary: "Translation provider failure rate high"
      description: "Provider {{ $labels.provider }} success rate is {{ $value | humanizePercentage }}"

  # Quality Alerts
  - alert: TranslationQualityDegradation
    expr: avg_over_time(translation_quality_score[1h]) < 0.8
    for: 10m
    labels:
      severity: warning
      service: translation-service
    annotations:
      summary: "Translation quality degradation"
      description: "Average quality score is {{ $value }} over the last hour"
```

### Grafana Dashboard

```json
{
  "dashboard": {
    "title": "Translation Service Dashboard",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(translation_requests_total[5m])",
            "legendFormat": "Requests/sec"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.50, rate(translation_requests_duration_seconds_bucket[5m]))",
            "legendFormat": "50th percentile"
          },
          {
            "expr": "histogram_quantile(0.95, rate(translation_requests_duration_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      },
      {
        "title": "Cache Hit Rate",
        "type": "singlestat",
        "targets": [
          {
            "expr": "translation_cache_hit_ratio",
            "legendFormat": "Hit Rate"
          }
        ]
      },
      {
        "title": "Provider Success Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "translation_provider_success_rate",
            "legendFormat": "{{ provider }}"
          }
        ]
      },
      {
        "title": "Translation Quality Score",
        "type": "graph",
        "targets": [
          {
            "expr": "avg_over_time(translation_quality_score[1h])",
            "legendFormat": "Average Quality"
          }
        ]
      }
    ]
  }
}
```

### Logging Configuration

#### Structured Logging

```yaml
# logback-spring.xml
<configuration>
    <springProfile name="production">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/translation-service.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/translation-service.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
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
                    <arguments/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="STDOUT"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

#### Log Aggregation with ELK Stack

```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /app/logs/*.log
  fields:
    service: translation-service
    environment: production
  fields_under_root: true
  json.keys_under_root: true
  json.overwrite_keys: true

output.logstash:
  hosts: ["logstash:5044"]

processors:
- add_host_metadata:
    when.not.contains.tags: forwarded
- add_docker_metadata: ~
- add_kubernetes_metadata: ~
```

### Distributed Tracing

#### Jaeger Configuration

```yaml
# Jaeger tracing configuration
opentracing:
  jaeger:
    service-name: translation-service
    sampler:
      type: probabilistic
      param: 0.1
    reporter:
      log-spans: false
      sender:
        endpoint: http://jaeger-collector:14268/api/traces
```

#### Custom Tracing

```java
@Component
public class TranslationTracing {
    
    @Autowired
    private Tracer tracer;
    
    public void traceTranslationRequest(TranslationRequest request) {
        Span span = tracer.nextSpan()
            .name("translation-request")
            .tag("source.language", request.getSourceLanguage())
            .tag("target.language", request.getTargetLanguage())
            .tag("provider", request.getProvider())
            .tag("content.type", request.getContentType())
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Translation logic
        } finally {
            span.end();
        }
    }
}
```

## Maintenance Procedures

### Daily Maintenance Tasks

#### Automated Health Checks

```bash
#!/bin/bash
# scripts/daily-health-check.sh

LOG_FILE="/var/log/translation-service/daily-health-$(date +%Y%m%d).log"

{
    echo "=== Daily Health Check - $(date) ==="
    
    # Check service status
    echo "1. Service Status Check"
    systemctl status translation-service
    
    # Check resource usage
    echo "2. Resource Usage Check"
    free -h
    df -h
    
    # Check database connections
    echo "3. Database Connection Check"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "SELECT count(*) FROM translation_requests WHERE created_at >= current_date;"
    
    # Check cache status
    echo "4. Cache Status Check"
    redis-cli -h $REDIS_HOST -p $REDIS_PORT info stats
    
    # Check translation provider status
    echo "5. Provider Status Check"
    curl -s http://localhost:8094/api/v1/providers/status | jq .
    
    # Check error logs
    echo "6. Error Log Check"
    grep -c "ERROR" /var/log/translation-service/translation-service.log
    
    echo "=== Health Check Complete ==="
    
} >> $LOG_FILE 2>&1

# Send notification if errors found
if grep -q "ERROR\|FAILED\|DOWN" $LOG_FILE; then
    echo "Health check found issues. Check $LOG_FILE for details." | \
    mail -s "Translation Service Health Check Alert" ops-team@exalt.com
fi
```

#### Cache Cleanup

```bash
#!/bin/bash
# scripts/cache-cleanup.sh

echo "Starting cache cleanup..."

# Remove expired cache entries
redis-cli -h $REDIS_HOST -p $REDIS_PORT --scan --pattern "translation:*" | \
while read key; do
    ttl=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT TTL "$key")
    if [ "$ttl" -eq -1 ]; then
        echo "Setting TTL for key: $key"
        redis-cli -h $REDIS_HOST -p $REDIS_PORT EXPIRE "$key" 604800  # 7 days
    fi
done

# Clean up old translation results
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
DELETE FROM translation_results 
WHERE created_at < current_date - interval '90 days' 
AND human_reviewed = false;
"

echo "Cache cleanup completed."
```

### Weekly Maintenance Tasks

#### Database Maintenance

```sql
-- weekly-db-maintenance.sql

-- Update table statistics
ANALYZE translation_requests;
ANALYZE translation_results;
ANALYZE translation_cache;

-- Reindex tables
REINDEX TABLE translation_requests;
REINDEX TABLE translation_results;

-- Vacuum tables
VACUUM ANALYZE translation_requests;
VACUUM ANALYZE translation_results;

-- Clean up old partitions
DROP TABLE IF EXISTS translation_requests_old;
DROP TABLE IF EXISTS translation_results_old;

-- Check for slow queries
SELECT query, mean_time, calls, total_time
FROM pg_stat_statements
WHERE query LIKE '%translation%'
ORDER BY total_time DESC
LIMIT 10;
```

#### Performance Review

```bash
#!/bin/bash
# scripts/weekly-performance-review.sh

REPORT_DATE=$(date +%Y%m%d)
REPORT_FILE="/var/log/translation-service/performance-report-$REPORT_DATE.txt"

{
    echo "=== Weekly Performance Report - $(date) ==="
    
    # Translation volume metrics
    echo "1. Translation Volume Metrics"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        DATE(created_at) as date,
        COUNT(*) as total_requests,
        AVG(processing_time_ms) as avg_processing_time,
        AVG(cost_amount) as avg_cost
    FROM translation_requests 
    WHERE created_at >= current_date - interval '7 days'
    GROUP BY DATE(created_at)
    ORDER BY date;
    "
    
    # Provider performance comparison
    echo "2. Provider Performance Comparison"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        provider_used,
        COUNT(*) as requests,
        AVG(processing_time_ms) as avg_time,
        AVG(cost_amount) as avg_cost,
        COUNT(CASE WHEN error_message IS NOT NULL THEN 1 END) as errors
    FROM translation_requests 
    WHERE created_at >= current_date - interval '7 days'
    GROUP BY provider_used
    ORDER BY requests DESC;
    "
    
    # Quality metrics
    echo "3. Quality Metrics"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        AVG(quality_score) as avg_quality,
        COUNT(CASE WHEN human_reviewed = true THEN 1 END) as human_reviewed,
        COUNT(*) as total_translations
    FROM translation_results
    WHERE created_at >= current_date - interval '7 days';
    "
    
    # Cache performance
    echo "4. Cache Performance"
    redis-cli -h $REDIS_HOST -p $REDIS_PORT info stats | grep -E "keyspace_hits|keyspace_misses"
    
} > $REPORT_FILE

# Send report to team
mail -s "Weekly Translation Service Performance Report" \
     -a $REPORT_FILE \
     ops-team@exalt.com < $REPORT_FILE
```

### Monthly Maintenance Tasks

#### Capacity Planning

```bash
#!/bin/bash
# scripts/monthly-capacity-planning.sh

MONTH=$(date +%Y-%m)
REPORT_FILE="/var/log/translation-service/capacity-report-$MONTH.txt"

{
    echo "=== Monthly Capacity Planning Report - $(date) ==="
    
    # Resource utilization trends
    echo "1. Resource Utilization Trends"
    curl -s "http://prometheus:9090/api/v1/query_range?query=avg(system_cpu_usage)&start=$(date -d '30 days ago' +%s)&end=$(date +%s)&step=3600" | \
    jq -r '.data.result[0].values[] | "\(.[0]) \(.[1])"' | \
    awk '{print strftime("%Y-%m-%d %H:%M:%S", $1), $2}' > cpu_usage.tmp
    
    # Translation volume growth
    echo "2. Translation Volume Growth"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        DATE_TRUNC('week', created_at) as week,
        COUNT(*) as total_requests,
        SUM(LENGTH(source_content)) as total_characters
    FROM translation_requests 
    WHERE created_at >= current_date - interval '90 days'
    GROUP BY DATE_TRUNC('week', created_at)
    ORDER BY week;
    "
    
    # Cost analysis
    echo "3. Cost Analysis"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        provider_used,
        SUM(cost_amount) as total_cost,
        COUNT(*) as total_requests,
        AVG(cost_amount) as avg_cost_per_request
    FROM translation_requests 
    WHERE created_at >= current_date - interval '30 days'
    AND cost_amount IS NOT NULL
    GROUP BY provider_used
    ORDER BY total_cost DESC;
    "
    
    # Recommendations
    echo "4. Scaling Recommendations"
    if [ $(cat cpu_usage.tmp | awk '{sum+=$2} END {print sum/NR}' | cut -d. -f1) -gt 70 ]; then
        echo "- Consider scaling up CPU resources"
    fi
    
    rm -f cpu_usage.tmp
    
} > $REPORT_FILE

# Send to management
mail -s "Monthly Translation Service Capacity Planning Report" \
     -a $REPORT_FILE \
     management@exalt.com < $REPORT_FILE
```

### Translation Provider Management

#### Provider Health Monitoring

```bash
#!/bin/bash
# scripts/provider-health-check.sh

check_google_translate() {
    echo "Checking Google Translate API..."
    response=$(curl -s -X POST \
        "https://translation.googleapis.com/language/translate/v2?key=$GOOGLE_TRANSLATE_API_KEY" \
        -H "Content-Type: application/json" \
        -d '{"q": "Hello", "target": "es"}')
    
    if echo "$response" | grep -q "translatedText"; then
        echo "✅ Google Translate API: OK"
        return 0
    else
        echo "❌ Google Translate API: Failed"
        echo "$response"
        return 1
    fi
}

check_aws_translate() {
    echo "Checking AWS Translate..."
    response=$(aws translate translate-text \
        --text "Hello" \
        --source-language-code en \
        --target-language-code es \
        --output json 2>/dev/null)
    
    if echo "$response" | grep -q "TranslatedText"; then
        echo "✅ AWS Translate: OK"
        return 0
    else
        echo "❌ AWS Translate: Failed"
        return 1
    fi
}

check_microsoft_translator() {
    echo "Checking Microsoft Translator..."
    response=$(curl -s -X POST \
        "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&to=es" \
        -H "Ocp-Apim-Subscription-Key: $MICROSOFT_TRANSLATOR_API_KEY" \
        -H "Content-Type: application/json" \
        -d '[{"Text": "Hello"}]')
    
    if echo "$response" | grep -q "translations"; then
        echo "✅ Microsoft Translator: OK"
        return 0
    else
        echo "❌ Microsoft Translator: Failed"
        echo "$response"
        return 1
    fi
}

check_deepl() {
    echo "Checking DeepL API..."
    response=$(curl -s -X POST \
        "https://api-free.deepl.com/v2/translate" \
        -H "Authorization: DeepL-Auth-Key $DEEPL_API_KEY" \
        -d "text=Hello&target_lang=ES")
    
    if echo "$response" | grep -q "translations"; then
        echo "✅ DeepL API: OK"
        return 0
    else
        echo "❌ DeepL API: Failed"
        echo "$response"
        return 1
    fi
}

# Run all checks
failed_providers=()

check_google_translate || failed_providers+=("Google Translate")
check_aws_translate || failed_providers+=("AWS Translate")
check_microsoft_translator || failed_providers+=("Microsoft Translator")
check_deepl || failed_providers+=("DeepL")

if [ ${#failed_providers[@]} -gt 0 ]; then
    echo "❌ Failed providers: ${failed_providers[*]}"
    # Send alert
    echo "Translation provider health check failed for: ${failed_providers[*]}" | \
    mail -s "Translation Provider Alert" ops-team@exalt.com
    exit 1
else
    echo "✅ All translation providers are healthy"
    exit 0
fi
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Response Times

**Symptoms:**
- Response times > 5 seconds
- Timeout errors
- User complaints about slow translations

**Diagnosis:**
```bash
# Check current response times
curl -s http://localhost:8094/actuator/metrics/translation.requests.duration | jq .

# Check database performance
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
SELECT query, mean_time, calls, total_time
FROM pg_stat_statements
WHERE query LIKE '%translation%'
ORDER BY mean_time DESC
LIMIT 5;
"

# Check cache hit rate
redis-cli -h $REDIS_HOST -p $REDIS_PORT info stats | grep -E "keyspace_hits|keyspace_misses"
```

**Solutions:**
```bash
# Optimize database queries
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
CREATE INDEX CONCURRENTLY idx_translation_requests_hash_status 
ON translation_requests(content_hash, status);
"

# Increase cache size
redis-cli -h $REDIS_HOST -p $REDIS_PORT CONFIG SET maxmemory 4gb

# Scale up application instances
kubectl scale deployment translation-service --replicas=6 -n exalt-shared
```

#### 2. Translation Quality Issues

**Symptoms:**
- Low quality scores
- User complaints about translation accuracy
- Inconsistent translations

**Diagnosis:**
```bash
# Check quality metrics
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
SELECT 
    provider_used,
    AVG(quality_score) as avg_quality,
    COUNT(*) as total_translations
FROM translation_results tr
JOIN translation_requests req ON tr.request_id = req.id
WHERE tr.created_at >= current_date - interval '7 days'
GROUP BY provider_used
ORDER BY avg_quality DESC;
"

# Check for provider-specific issues
curl -s http://localhost:8094/api/v1/providers/status | jq .
```

**Solutions:**
```bash
# Update custom dictionaries
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
INSERT INTO custom_dictionaries (domain, source_language, target_language, source_term, target_term)
VALUES ('ecommerce', 'en', 'es', 'checkout', 'finalizar compra');
"

# Adjust provider selection logic
# Edit configuration to prioritize higher quality providers
kubectl edit configmap translation-service-config -n exalt-shared
```

#### 3. Memory Leaks

**Symptoms:**
- Increasing memory usage over time
- OutOfMemoryError exceptions
- Frequent garbage collection

**Diagnosis:**
```bash
# Check memory usage trends
curl -s http://localhost:8094/actuator/metrics/jvm.memory.used | jq .

# Generate heap dump
kubectl exec -it translation-service-pod -- jcmd 1 GC.run_finalization
kubectl exec -it translation-service-pod -- jcmd 1 VM.gc

# Check for memory leaks
kubectl logs translation-service-pod | grep -i "outofmemory\|gc"
```

**Solutions:**
```bash
# Increase heap size
kubectl patch deployment translation-service -p '
{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "translation-service",
          "env": [{
            "name": "JAVA_OPTS",
            "value": "-Xmx4g -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions"
          }]
        }]
      }
    }
  }
}' -n exalt-shared

# Optimize garbage collection
kubectl patch deployment translation-service -p '
{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "translation-service",
          "env": [{
            "name": "JAVA_OPTS",
            "value": "-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions"
          }]
        }]
      }
    }
  }
}' -n exalt-shared
```

#### 4. Database Connection Issues

**Symptoms:**
- Connection timeout errors
- "Too many connections" errors
- Database deadlocks

**Diagnosis:**
```bash
# Check database connections
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
SELECT state, count(*) 
FROM pg_stat_activity 
WHERE datname = 'translation_service_db' 
GROUP BY state;
"

# Check connection pool status
curl -s http://localhost:8094/actuator/metrics/hikaricp.connections.active | jq .

# Check for deadlocks
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
SELECT * FROM pg_stat_activity 
WHERE wait_event_type = 'Lock' 
AND datname = 'translation_service_db';
"
```

**Solutions:**
```bash
# Increase connection pool size
kubectl patch deployment translation-service -p '
{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "translation-service",
          "env": [{
            "name": "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE",
            "value": "50"
          }]
        }]
      }
    }
  }
}' -n exalt-shared

# Optimize database configuration
psql -h $DB_HOST -U postgres -c "
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '2GB';
ALTER SYSTEM SET effective_cache_size = '8GB';
SELECT pg_reload_conf();
"
```

### Runbooks

#### Service Restart Runbook

```bash
#!/bin/bash
# runbooks/service-restart.sh

SERVICE_NAME="translation-service"
NAMESPACE="exalt-shared"
MAX_RESTART_TIME=300

echo "=== Translation Service Restart Runbook ==="
echo "Timestamp: $(date)"
echo "Operator: $(whoami)"

# Pre-restart checks
echo "1. Pre-restart health check..."
kubectl get pods -l app=$SERVICE_NAME -n $NAMESPACE

# Drain traffic
echo "2. Draining traffic..."
kubectl patch service $SERVICE_NAME -p '{"spec":{"selector":{"app":"maintenance"}}}' -n $NAMESPACE

# Wait for active connections to finish
echo "3. Waiting for active connections to finish..."
sleep 30

# Restart deployment
echo "4. Restarting deployment..."
kubectl rollout restart deployment/$SERVICE_NAME -n $NAMESPACE

# Wait for rollout to complete
echo "5. Waiting for rollout to complete..."
kubectl rollout status deployment/$SERVICE_NAME -n $NAMESPACE --timeout=${MAX_RESTART_TIME}s

# Restore traffic
echo "6. Restoring traffic..."
kubectl patch service $SERVICE_NAME -p '{"spec":{"selector":{"app":"'$SERVICE_NAME'"}}}' -n $NAMESPACE

# Post-restart verification
echo "7. Post-restart verification..."
sleep 30
kubectl get pods -l app=$SERVICE_NAME -n $NAMESPACE

# Health check
echo "8. Health check..."
for i in {1..5}; do
    if curl -s http://translation-service:8094/actuator/health | grep -q "UP"; then
        echo "✅ Service is healthy"
        break
    else
        echo "⏳ Waiting for service to be ready... ($i/5)"
        sleep 10
    fi
done

echo "=== Restart completed ==="
```

#### Database Failover Runbook

```bash
#!/bin/bash
# runbooks/database-failover.sh

PRIMARY_DB="postgres-primary"
REPLICA_DB="postgres-replica"
NAMESPACE="exalt-shared"

echo "=== Database Failover Runbook ==="
echo "Timestamp: $(date)"
echo "Operator: $(whoami)"

# Check primary database status
echo "1. Checking primary database status..."
if kubectl exec -it $PRIMARY_DB-0 -n $NAMESPACE -- pg_isready -U postgres; then
    echo "❌ Primary database is still responding. Failover may not be necessary."
    read -p "Continue with failover? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Promote replica to primary
echo "2. Promoting replica to primary..."
kubectl exec -it $REPLICA_DB-0 -n $NAMESPACE -- pg_promote

# Update service to point to new primary
echo "3. Updating service endpoint..."
kubectl patch service postgres-service -p '
{
  "spec": {
    "selector": {
      "app": "'$REPLICA_DB'"
    }
  }
}' -n $NAMESPACE

# Update application configuration
echo "4. Updating application configuration..."
kubectl patch deployment translation-service -p '
{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "translation-service",
          "env": [{
            "name": "DB_HOST",
            "value": "'$REPLICA_DB'-service"
          }]
        }]
      }
    }
  }
}' -n $NAMESPACE

# Restart application to pick up new configuration
echo "5. Restarting application..."
kubectl rollout restart deployment/translation-service -n $NAMESPACE
kubectl rollout status deployment/translation-service -n $NAMESPACE --timeout=300s

# Verify connectivity
echo "6. Verifying database connectivity..."
kubectl exec -it translation-service-pod -n $NAMESPACE -- \
  psql -h $REPLICA_DB-service -U translation_user -d translation_service_db -c "SELECT 1;"

echo "=== Database failover completed ==="
```

## Translation Quality Management

### Quality Metrics and Monitoring

#### Quality Score Calculation

```java
@Component
public class QualityScoreCalculator {
    
    public double calculateQualityScore(TranslationResult result) {
        double score = 0.0;
        
        // Provider confidence score (40% weight)
        score += result.getProviderConfidence() * 0.4;
        
        // Language pair accuracy (30% weight)
        score += getLanguagePairAccuracy(result.getSourceLang(), result.getTargetLang()) * 0.3;
        
        // Content type factor (20% weight)
        score += getContentTypeScore(result.getContentType()) * 0.2;
        
        // Human review score (10% weight)
        if (result.isHumanReviewed()) {
            score += result.getHumanReviewScore() * 0.1;
        }
        
        return Math.min(score, 1.0);
    }
}
```

#### Quality Monitoring Dashboard

```bash
#!/bin/bash
# scripts/quality-monitoring.sh

QUALITY_REPORT_FILE="/var/log/translation-service/quality-report-$(date +%Y%m%d).txt"

{
    echo "=== Translation Quality Report - $(date) ==="
    
    # Overall quality metrics
    echo "1. Overall Quality Metrics (Last 24 hours)"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        COUNT(*) as total_translations,
        AVG(quality_score) as avg_quality,
        MIN(quality_score) as min_quality,
        MAX(quality_score) as max_quality,
        COUNT(CASE WHEN quality_score < 0.7 THEN 1 END) as low_quality_count
    FROM translation_results
    WHERE created_at >= current_timestamp - interval '24 hours';
    "
    
    # Quality by provider
    echo "2. Quality by Provider"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        req.provider_used,
        COUNT(*) as translations,
        AVG(res.quality_score) as avg_quality,
        COUNT(CASE WHEN res.quality_score < 0.7 THEN 1 END) as low_quality
    FROM translation_results res
    JOIN translation_requests req ON res.request_id = req.id
    WHERE res.created_at >= current_timestamp - interval '24 hours'
    GROUP BY req.provider_used
    ORDER BY avg_quality DESC;
    "
    
    # Quality by language pair
    echo "3. Quality by Language Pair"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        req.source_language || ' -> ' || req.target_language as language_pair,
        COUNT(*) as translations,
        AVG(res.quality_score) as avg_quality
    FROM translation_results res
    JOIN translation_requests req ON res.request_id = req.id
    WHERE res.created_at >= current_timestamp - interval '24 hours'
    GROUP BY req.source_language, req.target_language
    ORDER BY avg_quality DESC;
    "
    
    # Human review queue
    echo "4. Human Review Queue Status"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        COUNT(CASE WHEN human_reviewed = false THEN 1 END) as pending_review,
        COUNT(CASE WHEN human_reviewed = true THEN 1 END) as completed_review,
        AVG(CASE WHEN human_reviewed = true THEN quality_score END) as avg_reviewed_quality
    FROM translation_results
    WHERE created_at >= current_timestamp - interval '24 hours';
    "
    
} > $QUALITY_REPORT_FILE

# Check for quality alerts
if psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -tA -c "
SELECT COUNT(*) FROM translation_results 
WHERE created_at >= current_timestamp - interval '1 hour'
AND quality_score < 0.6;
" | grep -q '^[1-9]'; then
    echo "Quality alert: Low quality translations detected in the last hour" | \
    mail -s "Translation Quality Alert" -a $QUALITY_REPORT_FILE quality-team@exalt.com
fi
```

### Human Review Workflow

#### Review Assignment

```bash
#!/bin/bash
# scripts/assign-reviews.sh

echo "Assigning translations for human review..."

# Get pending high-priority reviews
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
SELECT 
    tr.id,
    req.source_content,
    tr.translated_content,
    req.source_language,
    req.target_language,
    req.quality_level,
    tr.quality_score
FROM translation_results tr
JOIN translation_requests req ON tr.request_id = req.id
WHERE tr.human_reviewed = false
AND (req.quality_level = 'HIGH' OR tr.quality_score < 0.8)
ORDER BY req.created_at
LIMIT 50;
" > pending_reviews.csv

# Assign to reviewers based on language expertise
python3 scripts/assign_to_reviewers.py pending_reviews.csv

# Send notification to reviewers
mail -s "New Translation Reviews Assigned" translation-reviewers@exalt.com < assignment_summary.txt

rm -f pending_reviews.csv assignment_summary.txt
```

#### Review Quality Tracking

```sql
-- Create review tracking table
CREATE TABLE IF NOT EXISTS review_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    translation_result_id UUID REFERENCES translation_results(id),
    reviewer_id UUID NOT NULL,
    review_time_seconds INTEGER,
    changes_made INTEGER DEFAULT 0,
    feedback TEXT,
    final_score DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Track reviewer performance
SELECT 
    reviewer_id,
    COUNT(*) as reviews_completed,
    AVG(final_score) as avg_final_score,
    AVG(review_time_seconds) as avg_review_time,
    AVG(changes_made) as avg_changes_made
FROM review_tracking
WHERE created_at >= current_date - interval '30 days'
GROUP BY reviewer_id
ORDER BY reviews_completed DESC;
```

## Performance Optimization

### JVM Tuning

#### Production JVM Settings

```bash
# JVM configuration for production
JAVA_OPTS="
-Xms2g
-Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions
-XX:+UseCGroupMemoryLimitForHeap
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/app/logs/heapdump.hprof
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCApplicationStoppedTime
-Xloggc:/app/logs/gc.log
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=5
-XX:GCLogFileSize=100M
"
```

#### GC Tuning Script

```bash
#!/bin/bash
# scripts/gc-tuning.sh

LOG_FILE="/app/logs/gc.log"
ANALYSIS_FILE="/tmp/gc-analysis.txt"

echo "Analyzing GC performance..."

# Analyze GC logs
if [ -f "$LOG_FILE" ]; then
    # Parse GC log for key metrics
    grep "Total time" "$LOG_FILE" | tail -100 | awk '{
        total_time += $NF
        count++
    } END {
        print "Average GC time (last 100 collections):", total_time/count "ms"
    }' > $ANALYSIS_FILE
    
    # Check for long GC pauses
    long_pauses=$(grep -E "Total time.*[0-9]{4,}" "$LOG_FILE" | wc -l)
    echo "Long GC pauses (>1000ms): $long_pauses" >> $ANALYSIS_FILE
    
    # Memory usage analysis
    grep "Heap" "$LOG_FILE" | tail -10 >> $ANALYSIS_FILE
    
    # Recommendations
    echo "GC Tuning Recommendations:" >> $ANALYSIS_FILE
    if [ $long_pauses -gt 10 ]; then
        echo "- Consider increasing heap size or switching to ZGC" >> $ANALYSIS_FILE
    fi
    
    cat $ANALYSIS_FILE
    
    # Send analysis to team
    mail -s "GC Performance Analysis" -a $ANALYSIS_FILE ops-team@exalt.com
fi
```

### Database Optimization

#### Query Performance Analysis

```sql
-- Query performance analysis
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    min_time,
    max_time,
    rows,
    100.0 * shared_blks_hit / nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent
FROM pg_stat_statements
WHERE query LIKE '%translation%'
ORDER BY total_time DESC
LIMIT 20;

-- Identify missing indexes
SELECT 
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats
WHERE schemaname = 'public'
AND tablename IN ('translation_requests', 'translation_results', 'translation_cache')
ORDER BY n_distinct DESC;
```

#### Index Optimization

```sql
-- Optimize indexes for common queries
CREATE INDEX CONCURRENTLY idx_translation_requests_composite
ON translation_requests(status, source_language, target_language, created_at);

CREATE INDEX CONCURRENTLY idx_translation_results_quality
ON translation_results(quality_score, human_reviewed, created_at);

CREATE INDEX CONCURRENTLY idx_translation_cache_lookup
ON translation_cache(content_hash, source_language, target_language)
WHERE expires_at > current_timestamp;

-- Partial index for active requests
CREATE INDEX CONCURRENTLY idx_translation_requests_active
ON translation_requests(created_at, processing_time_ms)
WHERE status IN ('PENDING', 'PROCESSING');
```

### Cache Optimization

#### Redis Configuration Tuning

```bash
# Redis optimization configuration
redis-cli CONFIG SET maxmemory 4gb
redis-cli CONFIG SET maxmemory-policy allkeys-lru
redis-cli CONFIG SET timeout 300
redis-cli CONFIG SET tcp-keepalive 60
redis-cli CONFIG SET save "900 1 300 10 60 10000"

# Enable Redis persistence
redis-cli CONFIG SET appendonly yes
redis-cli CONFIG SET appendfsync everysec
```

#### Cache Warming Strategy

```java
@Component
public class CacheWarmingService {
    
    @Scheduled(cron = "0 0 * * * ?") // Every hour
    public void warmCommonTranslations() {
        // Get most frequently requested translations
        List<TranslationRequest> popularRequests = getPopularTranslations();
        
        for (TranslationRequest request : popularRequests) {
            if (!cacheManager.isCached(request)) {
                // Pre-load translation into cache
                translationService.translateAndCache(request);
            }
        }
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmCriticalTranslations() {
        // Load critical system translations
        loadUITranslations();
        loadLegalTranslations();
        loadCommonEcommerceTerms();
    }
}
```

## Security Operations

### Security Monitoring

#### Security Metrics Dashboard

```yaml
# Security monitoring queries
security_metrics:
  - name: failed_authentication_attempts
    query: sum(rate(spring_security_authentication_failure_total[5m]))
    threshold: 10
    
  - name: unauthorized_access_attempts
    query: sum(rate(http_requests_total{status="401"}[5m]))
    threshold: 20
    
  - name: suspicious_translation_patterns
    query: sum(rate(translation_requests_total{content_type="script"}[5m]))
    threshold: 5
    
  - name: high_volume_users
    query: sum by (user_id) (rate(translation_requests_total[1h])) > 1000
```

#### Security Audit Script

```bash
#!/bin/bash
# scripts/security-audit.sh

AUDIT_DATE=$(date +%Y%m%d)
AUDIT_FILE="/var/log/translation-service/security-audit-$AUDIT_DATE.log"

{
    echo "=== Security Audit Report - $(date) ==="
    
    # Check for suspicious activities
    echo "1. Suspicious Activities Check"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        DATE_TRUNC('hour', created_at) as hour,
        COUNT(*) as request_count,
        COUNT(DISTINCT source_content) as unique_content,
        COUNT(DISTINCT created_by) as unique_users
    FROM translation_requests
    WHERE created_at >= current_timestamp - interval '24 hours'
    GROUP BY DATE_TRUNC('hour', created_at)
    HAVING COUNT(*) > 1000
    ORDER BY hour;
    "
    
    # Check for potential data exfiltration
    echo "2. Data Exfiltration Check"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        created_by,
        COUNT(*) as requests,
        SUM(LENGTH(source_content)) as total_characters,
        COUNT(DISTINCT target_language) as languages_used
    FROM translation_requests
    WHERE created_at >= current_timestamp - interval '24 hours'
    GROUP BY created_by
    HAVING COUNT(*) > 500 OR SUM(LENGTH(source_content)) > 1000000
    ORDER BY requests DESC;
    "
    
    # Check API key usage
    echo "3. API Key Usage Analysis"
    grep -E "api_key|authentication" /var/log/translation-service/access.log | \
    grep -E "$(date +%Y-%m-%d)" | \
    awk '{print $1}' | sort | uniq -c | sort -nr | head -20
    
    # Check for privilege escalation attempts
    echo "4. Privilege Escalation Check"
    grep -E "admin|root|sudo" /var/log/translation-service/application.log | \
    grep -E "$(date +%Y-%m-%d)"
    
} > $AUDIT_FILE

# Send security alert if issues found
if grep -qE "WARNING|ALERT|SUSPICIOUS" $AUDIT_FILE; then
    mail -s "Security Audit Alert - Translation Service" \
         -a $AUDIT_FILE \
         security-team@exalt.com < $AUDIT_FILE
fi
```

### Vulnerability Management

#### Dependency Scanning

```bash
#!/bin/bash
# scripts/dependency-scan.sh

echo "Scanning dependencies for vulnerabilities..."

# Scan Maven dependencies
mvn org.owasp:dependency-check-maven:check -Dformat=JSON -DfailBuildOnCVSS=7

# Scan Docker image
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(pwd):/tmp clair-scanner:latest \
  --clair="http://clair:6060" \
  --report="/tmp/vulnerability-report.json" \
  exalt/translation-service:latest

# Generate security report
python3 scripts/generate-security-report.py \
  --maven-report target/dependency-check-report.json \
  --docker-report vulnerability-report.json \
  --output security-report.html

# Send report to security team
mail -s "Translation Service Vulnerability Report" \
     -a security-report.html \
     security-team@exalt.com
```

#### Security Patch Management

```bash
#!/bin/bash
# scripts/security-patch.sh

PATCH_DATE=$(date +%Y%m%d)
PATCH_LOG="/var/log/translation-service/security-patch-$PATCH_DATE.log"

{
    echo "=== Security Patch Report - $(date) ==="
    
    # Update base image
    echo "1. Updating base Docker image..."
    docker pull openjdk:17-jre-slim
    
    # Rebuild application image
    echo "2. Rebuilding application image..."
    docker build -t exalt/translation-service:latest-secure .
    
    # Update dependencies
    echo "3. Updating Maven dependencies..."
    mvn versions:use-latest-releases -DallowSnapshots=false
    
    # Run security tests
    echo "4. Running security tests..."
    mvn test -Dtest=SecurityTests
    
    # Deploy to staging for validation
    echo "5. Deploying to staging..."
    kubectl set image deployment/translation-service \
      translation-service=exalt/translation-service:latest-secure \
      -n exalt-staging
    
    # Validate deployment
    echo "6. Validating deployment..."
    kubectl rollout status deployment/translation-service -n exalt-staging --timeout=300s
    
} > $PATCH_LOG 2>&1

# Send patch report
mail -s "Security Patch Application Report" \
     -a $PATCH_LOG \
     security-team@exalt.com
```

## Backup and Recovery

### Database Backup

#### Automated Backup Script

```bash
#!/bin/bash
# scripts/database-backup.sh

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/translation-service"
BACKUP_FILE="$BACKUP_DIR/translation_service_backup_$BACKUP_DATE.sql"
RETENTION_DAYS=30

# Create backup directory
mkdir -p $BACKUP_DIR

# Perform database backup
echo "Starting database backup..."
pg_dump -h $DB_HOST -U $DB_USERNAME -d $DB_NAME \
  --format=custom \
  --compress=9 \
  --verbose \
  --file=$BACKUP_FILE

# Verify backup
if [ $? -eq 0 ]; then
    echo "✅ Database backup completed successfully"
    
    # Calculate backup size
    BACKUP_SIZE=$(du -h $BACKUP_FILE | cut -f1)
    echo "Backup size: $BACKUP_SIZE"
    
    # Upload to cloud storage
    aws s3 cp $BACKUP_FILE s3://exalt-backups/translation-service/
    
    # Clean up old backups
    find $BACKUP_DIR -name "*.sql" -type f -mtime +$RETENTION_DAYS -delete
    
    # Send success notification
    echo "Database backup completed successfully. Size: $BACKUP_SIZE" | \
    mail -s "Translation Service Backup Success" ops-team@exalt.com
else
    echo "❌ Database backup failed"
    
    # Send failure notification
    echo "Database backup failed. Please check the logs." | \
    mail -s "Translation Service Backup Failed" ops-team@exalt.com
    
    exit 1
fi
```

#### Point-in-Time Recovery

```bash
#!/bin/bash
# scripts/point-in-time-recovery.sh

RECOVERY_TIME="$1"
RECOVERY_DB="translation_service_recovery"

if [ -z "$RECOVERY_TIME" ]; then
    echo "Usage: $0 'YYYY-MM-DD HH:MM:SS'"
    exit 1
fi

echo "Performing point-in-time recovery to: $RECOVERY_TIME"

# Stop application
kubectl scale deployment translation-service --replicas=0 -n exalt-shared

# Create recovery database
createdb -h $DB_HOST -U postgres $RECOVERY_DB

# Restore from backup
LATEST_BACKUP=$(aws s3 ls s3://exalt-backups/translation-service/ | sort | tail -n 1 | awk '{print $4}')
aws s3 cp s3://exalt-backups/translation-service/$LATEST_BACKUP /tmp/

pg_restore -h $DB_HOST -U postgres -d $RECOVERY_DB \
  --verbose \
  --clean \
  --if-exists \
  /tmp/$LATEST_BACKUP

# Apply WAL files up to recovery point
# (This requires WAL archiving to be enabled)
pg_ctl -D /var/lib/postgresql/data recovery -t "$RECOVERY_TIME"

# Verify recovery
psql -h $DB_HOST -U postgres -d $RECOVERY_DB -c "
SELECT 
    COUNT(*) as total_records,
    MAX(created_at) as latest_record
FROM translation_requests;
"

echo "Point-in-time recovery completed. Review the recovery database: $RECOVERY_DB"
```

### Application State Backup

#### Configuration Backup

```bash
#!/bin/bash
# scripts/config-backup.sh

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
CONFIG_BACKUP_DIR="/backups/translation-service/config"
CONFIG_BACKUP_FILE="$CONFIG_BACKUP_DIR/config_backup_$BACKUP_DATE.tar.gz"

mkdir -p $CONFIG_BACKUP_DIR

# Backup Kubernetes configurations
kubectl get configmaps -n exalt-shared -o yaml > /tmp/configmaps.yaml
kubectl get secrets -n exalt-shared -o yaml > /tmp/secrets.yaml
kubectl get deployments -n exalt-shared -o yaml > /tmp/deployments.yaml
kubectl get services -n exalt-shared -o yaml > /tmp/services.yaml

# Backup custom dictionaries
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
COPY (SELECT * FROM custom_dictionaries) TO '/tmp/custom_dictionaries.csv' WITH CSV HEADER;
"

# Create backup archive
tar -czf $CONFIG_BACKUP_FILE \
  /tmp/configmaps.yaml \
  /tmp/secrets.yaml \
  /tmp/deployments.yaml \
  /tmp/services.yaml \
  /tmp/custom_dictionaries.csv

# Upload to cloud storage
aws s3 cp $CONFIG_BACKUP_FILE s3://exalt-backups/translation-service/config/

# Clean up temporary files
rm -f /tmp/*.yaml /tmp/*.csv

echo "Configuration backup completed: $CONFIG_BACKUP_FILE"
```

### Disaster Recovery Plan

#### Recovery Procedures

```bash
#!/bin/bash
# scripts/disaster-recovery.sh

RECOVERY_REGION="us-west-2"
RECOVERY_CLUSTER="exalt-recovery-cluster"

echo "=== Disaster Recovery Procedure ==="
echo "Starting disaster recovery process..."

# 1. Assess the situation
echo "1. Assessing primary environment..."
kubectl cluster-info --context=primary-cluster || echo "Primary cluster unavailable"

# 2. Activate recovery environment
echo "2. Activating recovery environment..."
kubectl config use-context $RECOVERY_CLUSTER

# 3. Restore database
echo "3. Restoring database in recovery region..."
LATEST_BACKUP=$(aws s3 ls s3://exalt-backups/translation-service/ --region $RECOVERY_REGION | sort | tail -n 1 | awk '{print $4}')
aws s3 cp s3://exalt-backups/translation-service/$LATEST_BACKUP /tmp/ --region $RECOVERY_REGION

# Create recovery database instance
aws rds create-db-instance \
  --db-instance-identifier translation-service-recovery \
  --db-instance-class db.r5.large \
  --engine postgres \
  --master-username postgres \
  --master-user-password "$RECOVERY_DB_PASSWORD" \
  --allocated-storage 100 \
  --region $RECOVERY_REGION

# Wait for database to be available
aws rds wait db-instance-available \
  --db-instance-identifier translation-service-recovery \
  --region $RECOVERY_REGION

# Restore data
pg_restore -h translation-service-recovery.region.rds.amazonaws.com \
  -U postgres -d translation_service_db \
  --verbose --clean --if-exists \
  /tmp/$LATEST_BACKUP

# 4. Deploy application
echo "4. Deploying application to recovery environment..."
kubectl apply -f k8s/ -n exalt-shared

# Update configuration for recovery environment
kubectl patch deployment translation-service -p '
{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "translation-service",
          "env": [{
            "name": "DB_HOST",
            "value": "translation-service-recovery.region.rds.amazonaws.com"
          }]
        }]
      }
    }
  }
}' -n exalt-shared

# 5. Update DNS to point to recovery environment
echo "5. Updating DNS records..."
aws route53 change-resource-record-sets \
  --hosted-zone-id $HOSTED_ZONE_ID \
  --change-batch file://dns-failover.json

# 6. Verify recovery
echo "6. Verifying recovery environment..."
kubectl wait --for=condition=available deployment/translation-service -n exalt-shared --timeout=300s

# Test application
curl -f http://translation-api.exalt.com/actuator/health || {
    echo "❌ Recovery verification failed"
    exit 1
}

echo "✅ Disaster recovery completed successfully"
echo "Recovery environment is now active"
```

## Scaling Operations

### Auto-scaling Configuration

#### Horizontal Pod Autoscaler

```yaml
# Enhanced HPA configuration
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: translation-service-hpa
  namespace: exalt-shared
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: translation-service
  minReplicas: 3
  maxReplicas: 50
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: translation_requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  - type: External
    external:
      metric:
        name: translation_queue_depth
      target:
        type: AverageValue
        averageValue: "100"
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
      - type: Pods
        value: 2
        periodSeconds: 60
      selectPolicy: Min
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 60
      - type: Pods
        value: 4
        periodSeconds: 60
      selectPolicy: Max
```

#### Vertical Pod Autoscaler

```yaml
# VPA configuration for right-sizing
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: translation-service-vpa
  namespace: exalt-shared
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: translation-service
  updatePolicy:
    updateMode: "Auto"
  resourcePolicy:
    containerPolicies:
    - containerName: translation-service
      minAllowed:
        cpu: 100m
        memory: 128Mi
      maxAllowed:
        cpu: 2
        memory: 4Gi
      controlledResources: ["cpu", "memory"]
```

### Capacity Planning

#### Capacity Monitoring Script

```bash
#!/bin/bash
# scripts/capacity-monitoring.sh

CAPACITY_REPORT="/var/log/translation-service/capacity-report-$(date +%Y%m%d).txt"

{
    echo "=== Capacity Planning Report - $(date) ==="
    
    # Current resource utilization
    echo "1. Current Resource Utilization"
    kubectl top pods -l app=translation-service -n exalt-shared
    kubectl top nodes
    
    # Translation volume trends
    echo "2. Translation Volume Trends (Last 7 days)"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        DATE(created_at) as date,
        COUNT(*) as total_requests,
        AVG(processing_time_ms) as avg_processing_time,
        SUM(LENGTH(source_content)) as total_characters
    FROM translation_requests
    WHERE created_at >= current_date - interval '7 days'
    GROUP BY DATE(created_at)
    ORDER BY date;
    "
    
    # Peak load analysis
    echo "3. Peak Load Analysis"
    psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
    SELECT 
        EXTRACT(hour FROM created_at) as hour_of_day,
        COUNT(*) as avg_requests_per_hour
    FROM translation_requests
    WHERE created_at >= current_date - interval '7 days'
    GROUP BY EXTRACT(hour FROM created_at)
    ORDER BY avg_requests_per_hour DESC;
    "
    
    # Provider capacity utilization
    echo "4. Provider Capacity Utilization"
    curl -s http://localhost:8094/api/v1/providers/capacity | jq .
    
    # Scaling recommendations
    echo "5. Scaling Recommendations"
    current_replicas=$(kubectl get deployment translation-service -n exalt-shared -o jsonpath='{.spec.replicas}')
    avg_cpu=$(kubectl top pods -l app=translation-service -n exalt-shared --no-headers | awk '{sum+=$2} END {print sum/NR}' | sed 's/m//')
    
    if [ $avg_cpu -gt 800 ]; then
        echo "- Recommend scaling up: CPU usage is ${avg_cpu}m"
        recommended_replicas=$((current_replicas * 150 / 100))
        echo "- Suggested replicas: $recommended_replicas"
    elif [ $avg_cpu -lt 300 ]; then
        echo "- Consider scaling down: CPU usage is ${avg_cpu}m"
        recommended_replicas=$((current_replicas * 80 / 100))
        echo "- Suggested replicas: $recommended_replicas"
    else
        echo "- Current scaling is appropriate"
    fi
    
} > $CAPACITY_REPORT

# Send capacity report
mail -s "Translation Service Capacity Report" \
     -a $CAPACITY_REPORT \
     capacity-planning@exalt.com
```

## Incident Response

### Incident Response Plan

#### Alert Classification

```yaml
Severity Levels:
  P1 - Critical:
    - Service completely down
    - Data loss or corruption
    - Security breach
    - Response time: 15 minutes
    
  P2 - High:
    - Significant performance degradation
    - High error rates (>5%)
    - Provider failures
    - Response time: 1 hour
    
  P3 - Medium:
    - Minor performance issues
    - Quality degradation
    - Non-critical feature failures
    - Response time: 4 hours
    
  P4 - Low:
    - Cosmetic issues
    - Enhancement requests
    - Documentation updates
    - Response time: 24 hours
```

#### Incident Response Runbook

```bash
#!/bin/bash
# runbooks/incident-response.sh

INCIDENT_ID="$1"
SEVERITY="$2"
DESCRIPTION="$3"

if [ -z "$INCIDENT_ID" ] || [ -z "$SEVERITY" ] || [ -z "$DESCRIPTION" ]; then
    echo "Usage: $0 <incident-id> <severity> <description>"
    echo "Example: $0 INC-2024-001 P1 'Translation service down'"
    exit 1
fi

INCIDENT_LOG="/var/log/translation-service/incidents/$INCIDENT_ID.log"
mkdir -p /var/log/translation-service/incidents

{
    echo "=== INCIDENT RESPONSE LOG ==="
    echo "Incident ID: $INCIDENT_ID"
    echo "Severity: $SEVERITY"
    echo "Description: $DESCRIPTION"
    echo "Start Time: $(date)"
    echo "Responder: $(whoami)"
    echo "=================================="
    
    # Initial assessment
    echo "1. INITIAL ASSESSMENT"
    echo "Service status check..."
    kubectl get pods -l app=translation-service -n exalt-shared
    
    echo "Health check..."
    curl -s http://translation-service:8094/actuator/health | jq .
    
    echo "Recent logs..."
    kubectl logs -l app=translation-service -n exalt-shared --tail=50
    
    # Immediate actions based on severity
    case $SEVERITY in
        P1)
            echo "2. P1 CRITICAL RESPONSE"
            echo "Escalating to on-call team..."
            # Send immediate alert
            curl -X POST https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK \
              -H 'Content-type: application/json' \
              --data '{"text":"🚨 P1 INCIDENT: '$DESCRIPTION' - '$INCIDENT_ID'"}'
            
            # Page on-call engineer
            curl -X POST https://events.pagerduty.com/v2/enqueue \
              -H 'Content-Type: application/json' \
              -d '{
                "routing_key": "YOUR_PAGERDUTY_KEY",
                "event_action": "trigger",
                "dedup_key": "'$INCIDENT_ID'",
                "payload": {
                  "summary": "'$DESCRIPTION'",
                  "source": "translation-service",
                  "severity": "critical"
                }
              }'
            
            # Check if service restart is needed
            if ! curl -s http://translation-service:8094/actuator/health | grep -q "UP"; then
                echo "Service appears down, attempting restart..."
                kubectl rollout restart deployment/translation-service -n exalt-shared
            fi
            ;;
            
        P2)
            echo "2. P2 HIGH RESPONSE"
            echo "Investigating performance issues..."
            # Check metrics
            curl -s http://translation-service:8094/actuator/metrics/http.server.requests | jq .
            
            # Check database performance
            psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "
            SELECT query, mean_time, calls 
            FROM pg_stat_statements 
            WHERE query LIKE '%translation%' 
            ORDER BY mean_time DESC LIMIT 5;
            "
            ;;
            
        P3|P4)
            echo "2. P3/P4 STANDARD RESPONSE"
            echo "Monitoring and investigating..."
            # Standard monitoring and investigation
            ;;
    esac
    
    echo "3. MONITORING AND UPDATES"
    # Continue monitoring
    
} > $INCIDENT_LOG 2>&1

# Start incident tracking
echo "Incident $INCIDENT_ID logged. Monitoring in progress..."
echo "Log file: $INCIDENT_LOG"

# Set up monitoring loop for critical incidents
if [ "$SEVERITY" = "P1" ]; then
    # Monitor every 5 minutes for P1 incidents
    while true; do
        if curl -s http://translation-service:8094/actuator/health | grep -q "UP"; then
            echo "$(date): Service is UP" >> $INCIDENT_LOG
            break
        else
            echo "$(date): Service still DOWN" >> $INCIDENT_LOG
            sleep 300
        fi
    done
fi
```

### Post-Incident Review

#### Post-Incident Report Template

```bash
#!/bin/bash
# scripts/post-incident-report.sh

INCIDENT_ID="$1"
INCIDENT_LOG="/var/log/translation-service/incidents/$INCIDENT_ID.log"
REPORT_FILE="/var/log/translation-service/post-incident-reports/$INCIDENT_ID-report.md"

mkdir -p /var/log/translation-service/post-incident-reports

# Generate post-incident report
cat > $REPORT_FILE << EOF
# Post-Incident Report: $INCIDENT_ID

## Incident Summary
- **Incident ID**: $INCIDENT_ID
- **Date**: $(date)
- **Duration**: [TO BE FILLED]
- **Severity**: [TO BE FILLED]
- **Impact**: [TO BE FILLED]

## Timeline
[TO BE FILLED - Extract from incident log]

## Root Cause Analysis
### What Happened
[TO BE FILLED]

### Why It Happened
[TO BE FILLED]

### Contributing Factors
[TO BE FILLED]

## Resolution
### Immediate Actions Taken
[TO BE FILLED]

### Permanent Fix
[TO BE FILLED]

## Lessons Learned
### What Went Well
[TO BE FILLED]

### What Could Be Improved
[TO BE FILLED]

## Action Items
| Action | Owner | Due Date | Status |
|--------|-------|----------|--------|
| [TO BE FILLED] | [TO BE FILLED] | [TO BE FILLED] | Open |

## Prevention Measures
[TO BE FILLED]

## Appendices
### Relevant Logs
\`\`\`
$(tail -100 $INCIDENT_LOG)
\`\`\`

### Metrics and Graphs
[TO BE FILLED]

---
*Report generated on $(date)*
EOF

echo "Post-incident report template created: $REPORT_FILE"
echo "Please complete the report and share with the team."
```

This comprehensive operations guide provides the foundation for maintaining a robust, secure, and high-performing Translation Service. Regular review and updates of these procedures ensure continued operational excellence as the service evolves and scales.
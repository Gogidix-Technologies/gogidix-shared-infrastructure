# Notification Service Operations Guide

## Overview

This document provides comprehensive operational procedures for managing the Notification Service in production environments, including monitoring, maintenance, incident response, and performance optimization for multi-channel notification delivery.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Monitoring and Alerting](#monitoring-and-alerting)
3. [Performance Management](#performance-management)
4. [Incident Response](#incident-response)
5. [Template Management](#template-management)
6. [Provider Management](#provider-management)
7. [Queue Management](#queue-management)
8. [Security Operations](#security-operations)
9. [Compliance and Audit](#compliance-and-audit)
10. [Troubleshooting Guide](#troubleshooting-guide)

## Service Operations

### Daily Operations Checklist

#### Morning Operations (8:00 AM)
- [ ] Check service health status
- [ ] Review overnight delivery reports
- [ ] Verify provider connectivity
- [ ] Check queue depths and processing rates
- [ ] Review failed notifications
- [ ] Validate template rendering
- [ ] Check rate limiting status
- [ ] Review security alerts

#### Mid-Day Operations (1:00 PM)
- [ ] Monitor peak traffic performance
- [ ] Check delivery success rates
- [ ] Review provider response times
- [ ] Validate A/B test results
- [ ] Check resource utilization
- [ ] Review user engagement metrics

#### Evening Operations (6:00 PM)
- [ ] Generate daily delivery report
- [ ] Review bounce and complaint rates
- [ ] Update operational metrics
- [ ] Plan maintenance activities
- [ ] Review compliance status

### Health Monitoring Commands

```bash
# Overall service health
curl -X GET http://localhost:8087/actuator/health

# Provider-specific health checks
curl -X GET http://localhost:8087/actuator/health/emailProvider
curl -X GET http://localhost:8087/actuator/health/smsProvider
curl -X GET http://localhost:8087/actuator/health/pushProvider

# Database connectivity
curl -X GET http://localhost:8087/actuator/health/db

# Queue health
curl -X GET http://localhost:8087/actuator/health/redis
```

### Service Level Objectives

| Metric | Target | Warning | Critical |
|--------|--------|---------|----------|
| Service Availability | 99.95% | 99.9% | < 99.9% |
| Email Delivery Rate | > 95% | < 90% | < 85% |
| SMS Delivery Rate | > 98% | < 95% | < 90% |
| Push Delivery Rate | > 99% | < 97% | < 95% |
| API Response Time (p95) | < 500ms | > 1s | > 2s |
| Template Rendering | < 100ms | > 500ms | > 1s |
| Queue Processing Lag | < 5s | > 30s | > 60s |

## Monitoring and Alerting

### Key Metrics

#### Business Metrics
```yaml
metrics:
  delivery:
    - notifications_sent_total{channel, status}
    - notifications_delivered_total{channel, provider}
    - notifications_failed_total{channel, reason}
    - delivery_rate_by_channel{channel}
    - delivery_time_seconds{channel, provider}
    
  engagement:
    - email_opens_total{campaign}
    - email_clicks_total{campaign}
    - unsubscribe_rate{channel}
    - bounce_rate{provider}
    - spam_complaints_total{provider}
    
  templates:
    - template_renders_total{template_id}
    - template_render_duration_seconds{template_id}
    - template_errors_total{template_id, error_type}
```

#### Technical Metrics
```yaml
metrics:
  performance:
    - queue_depth{queue_name, priority}
    - queue_processing_duration_seconds{queue_name}
    - provider_response_time_seconds{provider, operation}
    - rate_limit_hits_total{user_id, provider}
    - retry_attempts_total{channel, reason}
    
  infrastructure:
    - database_connections_active
    - redis_connections_active
    - kafka_consumer_lag{topic, partition}
    - jvm_memory_used_bytes{area}
    - cpu_usage_percent
```

### Alert Configuration

```yaml
groups:
- name: notification-service.rules
  rules:
  
  # Critical Alerts
  - alert: NotificationServiceDown
    expr: up{job="notification-service"} == 0
    for: 2m
    labels:
      severity: critical
      team: platform
    annotations:
      summary: "Notification service is down"
      description: "Notification service {{ $labels.instance }} is unreachable"
      runbook: "https://wiki.example.com/runbooks/notification-service-down"

  - alert: HighDeliveryFailureRate
    expr: |
      (
        sum(rate(notifications_failed_total[5m])) /
        sum(rate(notifications_sent_total[5m]))
      ) > 0.1
    for: 10m
    labels:
      severity: critical
    annotations:
      summary: "High notification delivery failure rate"
      description: "Delivery failure rate is {{ $value | humanizePercentage }}"

  - alert: ProviderDown
    expr: provider_health{status!="UP"} == 1
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "Notification provider is down"
      description: "Provider {{ $labels.provider }} is not responding"

  - alert: QueueBacklog
    expr: queue_depth > 10000
    for: 15m
    labels:
      severity: warning
    annotations:
      summary: "High queue backlog detected"
      description: "Queue {{ $labels.queue_name }} has {{ $value }} pending items"

  - alert: HighAPILatency
    expr: histogram_quantile(0.95, api_request_duration_seconds) > 2
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "High API response latency"
      description: "95th percentile latency is {{ $value }}s"

  # Provider-Specific Alerts
  - alert: EmailBounceRateHigh
    expr: email_bounce_rate > 0.05
    for: 30m
    labels:
      severity: warning
    annotations:
      summary: "High email bounce rate"
      description: "Email bounce rate is {{ $value | humanizePercentage }}"

  - alert: SMSDeliveryIssues
    expr: sms_delivery_rate < 0.95
    for: 15m
    labels:
      severity: warning
    annotations:
      summary: "SMS delivery rate below threshold"
      description: "SMS delivery rate is {{ $value | humanizePercentage }}"
```

### Monitoring Dashboards

#### Grafana Dashboard Configuration
```json
{
  "dashboard": {
    "title": "Notification Service Operations",
    "panels": [
      {
        "title": "Service Health Overview",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"notification-service\"}",
            "legendFormat": "Service Status"
          }
        ]
      },
      {
        "title": "Notifications Sent by Channel",
        "type": "graph",
        "targets": [
          {
            "expr": "sum(rate(notifications_sent_total[5m])) by (channel)",
            "legendFormat": "{{ channel }}"
          }
        ]
      },
      {
        "title": "Delivery Success Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "sum(rate(notifications_delivered_total[5m])) by (channel) / sum(rate(notifications_sent_total[5m])) by (channel)",
            "legendFormat": "{{ channel }} Success Rate"
          }
        ]
      },
      {
        "title": "Queue Depths",
        "type": "graph",
        "targets": [
          {
            "expr": "queue_depth",
            "legendFormat": "{{ queue_name }} - {{ priority }}"
          }
        ]
      },
      {
        "title": "Provider Response Times",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, provider_response_time_seconds)",
            "legendFormat": "{{ provider }} 95th percentile"
          }
        ]
      },
      {
        "title": "Template Performance",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, template_render_duration_seconds)",
            "legendFormat": "{{ template_id }} 95th percentile"
          }
        ]
      }
    ]
  }
}
```

## Performance Management

### Performance Baselines

| Component | Metric | Baseline | Target |
|-----------|--------|----------|---------|
| Email Delivery | Throughput | 1k emails/min | 10k emails/min |
| SMS Delivery | Throughput | 500 SMS/min | 5k SMS/min |
| Push Notifications | Throughput | 10k push/min | 100k push/min |
| Template Rendering | Latency | 50ms | 20ms |
| API Response | Latency (p95) | 200ms | 100ms |
| Queue Processing | Rate | 1k items/min | 10k items/min |

### Performance Optimization

#### Queue Optimization
```bash
# Monitor queue processing rates
redis-cli -a your_redis_password

# Check queue sizes
LLEN notification:email:high
LLEN notification:sms:normal
LLEN notification:push:low

# Optimize Redis configuration
redis-cli CONFIG SET maxmemory-policy allkeys-lru
redis-cli CONFIG SET maxmemory 4gb

# Monitor queue processing metrics
curl http://localhost:8087/actuator/metrics/notification.queue.processed.rate
```

#### Provider Performance Tuning
```java
// Email provider optimization
@ConfigurationProperties("notification.email")
public class EmailConfiguration {
    private int connectionPoolSize = 20;
    private int connectionTimeout = 30000;
    private int requestTimeout = 60000;
    private int batchSize = 100;
    private boolean useConnectionPooling = true;
}

// SMS provider optimization  
@ConfigurationProperties("notification.sms")
public class SMSConfiguration {
    private int maxConcurrentRequests = 10;
    private int retryAttempts = 3;
    private int retryDelay = 5000;
    private boolean enableBatching = true;
}
```

#### Database Performance
```sql
-- Optimize notification queries
EXPLAIN ANALYZE 
SELECT * FROM notifications 
WHERE recipient = ? AND status = 'PENDING' 
ORDER BY created_at DESC LIMIT 100;

-- Create performance indexes
CREATE INDEX CONCURRENTLY idx_notifications_recipient_status 
ON notifications(recipient, status);

CREATE INDEX CONCURRENTLY idx_notifications_created_at 
ON notifications(created_at) WHERE status = 'PENDING';

-- Partition large tables
CREATE TABLE notifications_2024_01 PARTITION OF notifications
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
```

### Load Testing

```bash
#!/bin/bash
# load-test.sh - Notification service load testing

echo "Starting notification service load test..."

# Test email notifications
for i in {1..1000}; do
  curl -X POST http://localhost:8087/api/v1/notifications/send \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "{
      \"channel\": \"EMAIL\",
      \"recipient\": \"test$i@example.com\",
      \"templateId\": \"welcome-email\",
      \"variables\": {\"firstName\": \"User$i\"}
    }" &
  
  if (( i % 100 == 0 )); then
    echo "Sent $i email notifications"
    wait
  fi
done

# Test SMS notifications
for i in {1..500}; do
  curl -X POST http://localhost:8087/api/v1/notifications/send \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "{
      \"channel\": \"SMS\",
      \"recipient\": \"+123456789$i\",
      \"templateId\": \"verification-sms\",
      \"variables\": {\"code\": \"$i\"}
    }" &
    
  if (( i % 50 == 0 )); then
    echo "Sent $i SMS notifications"
    wait
  fi
done

echo "Load test completed"
```

## Incident Response

### Incident Classification

| Level | Response Time | Examples |
|-------|---------------|----------|
| P1 | 15 minutes | Service completely down, data loss |
| P2 | 30 minutes | Provider outages, high failure rates |
| P3 | 2 hours | Performance degradation, template issues |
| P4 | 24 hours | Minor bugs, cosmetic issues |

### Response Procedures

#### P1 - Service Outage
```bash
#!/bin/bash
# p1-response.sh - Critical incident response

echo "P1 INCIDENT: Service outage detected"

# 1. Check service status
curl -f http://localhost:8087/actuator/health || echo "Service is down"

# 2. Check dependencies
curl -f http://postgres:5432 || echo "Database unreachable"
redis-cli -a $REDIS_PASSWORD ping || echo "Redis unreachable"
kafka-topics --bootstrap-server kafka:9092 --list || echo "Kafka unreachable"

# 3. Check logs for errors
tail -n 100 /var/log/notification/notification-service.log | grep ERROR

# 4. Restart service if needed
sudo systemctl restart notification-service

# 5. Verify recovery
sleep 30
curl -f http://localhost:8087/actuator/health && echo "Service recovered"
```

#### P2 - Provider Outage
```bash
#!/bin/bash
# provider-outage-response.sh

PROVIDER=$1
echo "P2 INCIDENT: Provider $PROVIDER outage"

# 1. Verify provider status
curl -X POST http://localhost:8087/api/v1/notifications/providers/test \
  -H "Content-Type: application/json" \
  -d "{\"provider\": \"$PROVIDER\"}"

# 2. Enable fallback provider
curl -X PUT http://localhost:8087/api/v1/notifications/providers/failover \
  -H "Content-Type: application/json" \
  -d "{\"from\": \"$PROVIDER\", \"to\": \"backup-provider\"}"

# 3. Queue failed notifications for retry
redis-cli -a $REDIS_PASSWORD LPUSH "retry:$PROVIDER" "$(date)"

# 4. Monitor recovery
watch -n 30 'curl -s http://localhost:8087/actuator/health/emailProvider'
```

#### High Queue Backlog Response
```bash
#!/bin/bash
# queue-backlog-response.sh

QUEUE_NAME=$1
BACKLOG_SIZE=$2

echo "P3 INCIDENT: High queue backlog: $QUEUE_NAME ($BACKLOG_SIZE items)"

# 1. Scale up workers
kubectl scale deployment notification-worker --replicas=10

# 2. Increase processing rate
redis-cli -a $REDIS_PASSWORD CONFIG SET timeout 0

# 3. Monitor queue reduction
while true; do
  CURRENT_SIZE=$(redis-cli -a $REDIS_PASSWORD LLEN $QUEUE_NAME)
  echo "Queue size: $CURRENT_SIZE"
  
  if [ $CURRENT_SIZE -lt 1000 ]; then
    echo "Queue backlog resolved"
    break
  fi
  
  sleep 60
done

# 4. Scale back to normal
kubectl scale deployment notification-worker --replicas=3
```

## Template Management

### Template Operations

#### Daily Template Tasks
```bash
# Validate all templates
curl http://localhost:8087/api/v1/notifications/templates/validate

# Check template usage statistics
curl http://localhost:8087/api/v1/notifications/templates/stats

# Update template cache
curl -X POST http://localhost:8087/api/v1/notifications/templates/cache/refresh
```

#### Template Deployment
```bash
#!/bin/bash
# deploy-templates.sh

TEMPLATE_DIR=$1

echo "Deploying templates from $TEMPLATE_DIR"

for template_file in $TEMPLATE_DIR/*.json; do
  echo "Deploying $(basename $template_file)"
  
  curl -X POST http://localhost:8087/api/v1/notifications/templates \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d @$template_file
    
  if [ $? -eq 0 ]; then
    echo "Successfully deployed $(basename $template_file)"
  else
    echo "Failed to deploy $(basename $template_file)"
  fi
done
```

#### A/B Testing Management
```bash
# Create A/B test
curl -X POST http://localhost:8087/api/v1/notifications/ab-tests \
  -H "Content-Type: application/json" \
  -d '{
    "name": "welcome-email-test",
    "templateA": "welcome-email-v1",
    "templateB": "welcome-email-v2",
    "trafficSplit": 50,
    "metrics": ["open_rate", "click_rate"]
  }'

# Check A/B test results
curl http://localhost:8087/api/v1/notifications/ab-tests/welcome-email-test/results

# Promote winning template
curl -X POST http://localhost:8087/api/v1/notifications/ab-tests/welcome-email-test/promote \
  -H "Content-Type: application/json" \
  -d '{"winner": "templateB"}'
```

## Provider Management

### Provider Health Monitoring

```bash
#!/bin/bash
# provider-health-check.sh

echo "Checking provider health..."

# SendGrid health check
SENDGRID_STATUS=$(curl -s -X POST https://api.sendgrid.com/v3/mail/send \
  -H "Authorization: Bearer $SENDGRID_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"personalizations":[{"to":[{"email":"test@example.com"}]}],"from":{"email":"noreply@example.com"},"subject":"Test","content":[{"type":"text/plain","value":"Test"}]}' \
  -w "%{http_code}" -o /dev/null)

if [ "$SENDGRID_STATUS" -eq 202 ]; then
  echo "SendGrid: HEALTHY"
else
  echo "SendGrid: UNHEALTHY (HTTP $SENDGRID_STATUS)"
fi

# Twilio health check
TWILIO_STATUS=$(curl -s -X GET "https://api.twilio.com/2010-04-01/Accounts/$TWILIO_ACCOUNT_SID.json" \
  -u "$TWILIO_ACCOUNT_SID:$TWILIO_AUTH_TOKEN" \
  -w "%{http_code}" -o /dev/null)

if [ "$TWILIO_STATUS" -eq 200 ]; then
  echo "Twilio: HEALTHY"
else
  echo "Twilio: UNHEALTHY (HTTP $TWILIO_STATUS)"
fi

# Firebase health check
FIREBASE_STATUS=$(curl -s -X POST "https://fcm.googleapis.com/fcm/send" \
  -H "Authorization: key=$FIREBASE_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{"dry_run":true,"to":"test-token","notification":{"title":"Test"}}' \
  -w "%{http_code}" -o /dev/null)

if [ "$FIREBASE_STATUS" -eq 200 ]; then
  echo "Firebase: HEALTHY"
else
  echo "Firebase: UNHEALTHY (HTTP $FIREBASE_STATUS)"
fi
```

### Provider Rate Limiting

```bash
# Check current rate limits
curl http://localhost:8087/api/v1/notifications/providers/rate-limits

# Update rate limits
curl -X PUT http://localhost:8087/api/v1/notifications/providers/sendgrid/rate-limit \
  -H "Content-Type: application/json" \
  -d '{"requestsPerMinute": 600}'

# Monitor rate limit hits
curl http://localhost:8087/actuator/metrics/notification.rate.limit.hits
```

## Queue Management

### Queue Monitoring

```bash
#!/bin/bash
# queue-monitor.sh

echo "=== Queue Status Report ==="
echo "Timestamp: $(date)"

# Check all queue depths
for queue in email:high email:normal email:low sms:high sms:normal sms:low push:high push:normal push:low; do
  DEPTH=$(redis-cli -a $REDIS_PASSWORD LLEN "notification:$queue")
  echo "$queue: $DEPTH items"
done

# Check failed message queue
FAILED=$(redis-cli -a $REDIS_PASSWORD LLEN "notification:failed")
echo "Failed messages: $FAILED"

# Check retry queue
RETRY=$(redis-cli -a $REDIS_PASSWORD LLEN "notification:retry")
echo "Retry queue: $RETRY"

echo "=== End Report ==="
```

### Queue Maintenance

```bash
#!/bin/bash
# queue-maintenance.sh

echo "Starting queue maintenance..."

# Clean up old failed messages (older than 7 days)
redis-cli -a $REDIS_PASSWORD EVAL "
  local failed_key = 'notification:failed'
  local cutoff = ARGV[1]
  local messages = redis.call('LRANGE', failed_key, 0, -1)
  local removed = 0
  
  for i, message in ipairs(messages) do
    local msg_data = cjson.decode(message)
    if msg_data.timestamp < cutoff then
      redis.call('LREM', failed_key, 1, message)
      removed = removed + 1
    end
  end
  
  return removed
" 0 $(date -d '7 days ago' +%s)

# Archive processed messages
redis-cli -a $REDIS_PASSWORD RENAME notification:completed notification:archive:$(date +%Y%m%d)

# Optimize Redis memory
redis-cli -a $REDIS_PASSWORD MEMORY PURGE

echo "Queue maintenance completed"
```

## Security Operations

### Security Monitoring

```bash
#!/bin/bash
# security-monitor.sh

echo "=== Security Monitoring Report ==="

# Check for suspicious API access patterns
curl http://localhost:8087/actuator/metrics/security.access.denied | jq

# Monitor rate limiting violations
curl http://localhost:8087/actuator/metrics/security.rate.limit.exceeded | jq

# Check authentication failures
grep "AUTH_FAILED" /var/log/notification/security.log | tail -10

# Monitor provider API key usage
curl http://localhost:8087/api/v1/notifications/providers/usage-stats

echo "=== End Security Report ==="
```

### Data Protection Operations

```bash
# Audit PII access
grep "PII_ACCESS" /var/log/notification/audit.log | \
  awk '{print $1, $5, $7}' | sort | uniq -c

# Check encryption status
curl http://localhost:8087/api/v1/notifications/encryption/status

# Rotate encryption keys
curl -X POST http://localhost:8087/api/v1/notifications/encryption/rotate \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Compliance and Audit

### Compliance Reporting

```bash
#!/bin/bash
# generate-compliance-report.sh

REPORT_DATE=$(date +%Y-%m)
REPORT_DIR="/var/reports/notification/$REPORT_DATE"

mkdir -p $REPORT_DIR

echo "Generating compliance report for $REPORT_DATE"

# Delivery statistics
curl http://localhost:8087/api/v1/notifications/reports/delivery?month=$REPORT_DATE \
  > $REPORT_DIR/delivery-stats.json

# Bounce and complaint rates
curl http://localhost:8087/api/v1/notifications/reports/bounces?month=$REPORT_DATE \
  > $REPORT_DIR/bounce-stats.json

# Unsubscribe data
curl http://localhost:8087/api/v1/notifications/reports/unsubscribes?month=$REPORT_DATE \
  > $REPORT_DIR/unsubscribe-stats.json

# Generate summary report
cat > $REPORT_DIR/summary.md << EOF
# Notification Service Compliance Report - $REPORT_DATE

## Summary
- Total notifications sent: $(jq '.total_sent' $REPORT_DIR/delivery-stats.json)
- Delivery rate: $(jq '.delivery_rate' $REPORT_DIR/delivery-stats.json)%
- Bounce rate: $(jq '.bounce_rate' $REPORT_DIR/bounce-stats.json)%
- Complaint rate: $(jq '.complaint_rate' $REPORT_DIR/bounce-stats.json)%

## Compliance Status
- CAN-SPAM compliant: ✓
- GDPR compliant: ✓
- Unsubscribe processing: < 10 days
EOF

echo "Compliance report generated in $REPORT_DIR"
```

### GDPR Operations

```bash
# Process data deletion request
curl -X DELETE http://localhost:8087/api/v1/notifications/users/user123/data \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Export user data
curl http://localhost:8087/api/v1/notifications/users/user123/export \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  > user123-data-export.json

# Check data retention compliance
curl http://localhost:8087/api/v1/notifications/compliance/retention-check
```

## Troubleshooting Guide

### Common Issues

#### 1. High Delivery Failure Rate
```bash
# Check provider status
curl http://localhost:8087/actuator/health/emailProvider

# Review recent failures
curl http://localhost:8087/api/v1/notifications/failures?limit=100

# Check provider rate limits
curl http://localhost:8087/api/v1/notifications/providers/rate-limits

# Test individual provider
curl -X POST http://localhost:8087/api/v1/notifications/providers/test \
  -H "Content-Type: application/json" \
  -d '{"provider": "sendgrid", "testType": "connectivity"}'
```

#### 2. Template Rendering Errors
```bash
# Validate template syntax
curl -X POST http://localhost:8087/api/v1/notifications/templates/validate \
  -H "Content-Type: application/json" \
  -d '{"templateId": "welcome-email"}'

# Test template with sample data
curl -X POST http://localhost:8087/api/v1/notifications/templates/test \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "welcome-email",
    "variables": {"firstName": "Test", "email": "test@example.com"}
  }'

# Check template cache
curl http://localhost:8087/actuator/caches/templates
```

#### 3. Queue Processing Issues
```bash
# Check queue processor status
curl http://localhost:8087/actuator/metrics/notification.queue.processor.active

# Monitor queue processing rate
watch -n 5 'redis-cli -a $REDIS_PASSWORD LLEN notification:email:high'

# Check for stuck messages
redis-cli -a $REDIS_PASSWORD LRANGE notification:email:high 0 10

# Restart queue processors
curl -X POST http://localhost:8087/api/v1/notifications/queue/restart
```

#### 4. Performance Issues
```bash
# Check JVM metrics
curl http://localhost:8087/actuator/metrics/jvm.memory.used

# Monitor database connections
curl http://localhost:8087/actuator/metrics/hikaricp.connections.active

# Check Redis performance
redis-cli -a $REDIS_PASSWORD --latency -h localhost -p 6379

# Profile application
jstack $(pgrep -f notification-service) > thread-dump.txt
```

### Debug Commands

```bash
# Enable debug logging
curl -X POST http://localhost:8087/actuator/loggers/com.exalt.notification \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Collect thread dump
kill -3 $(pgrep -f notification-service)

# Monitor real-time metrics
watch -n 1 'curl -s http://localhost:8087/actuator/metrics/notifications.sent.total | jq'

# Check configuration
curl http://localhost:8087/actuator/configprops
```

## Operational Runbooks

### Daily Maintenance Runbook
```bash
#!/bin/bash
# daily-maintenance.sh

echo "Starting daily maintenance - $(date)"

# 1. Health check
./health-check.sh

# 2. Clean up old logs
find /var/log/notification -name "*.log" -mtime +7 -delete

# 3. Archive completed notifications
./archive-notifications.sh

# 4. Update metrics
./update-daily-metrics.sh

# 5. Generate daily report
./generate-daily-report.sh

echo "Daily maintenance completed - $(date)"
```

### Emergency Response Runbook
```bash
#!/bin/bash
# emergency-response.sh

INCIDENT_TYPE=$1

case $INCIDENT_TYPE in
  "service-down")
    ./p1-response.sh
    ;;
  "provider-outage")
    ./provider-outage-response.sh $2
    ;;
  "high-failure-rate")
    ./high-failure-response.sh
    ;;
  "queue-backlog")
    ./queue-backlog-response.sh $2 $3
    ;;
  *)
    echo "Unknown incident type: $INCIDENT_TYPE"
    exit 1
    ;;
esac
```

## Contact Information

### Escalation Matrix
- L1 Support: notification-support@example.com
- L2 Engineering: notification-engineering@example.com
- L3 On-Call: +1-555-NOTIFY-HELP
- Provider Support: provider-escalation@example.com

### External Contacts
- SendGrid Support: support@sendgrid.com
- Twilio Support: help@twilio.com
- Firebase Support: firebase-support@google.com

For detailed procedures and additional runbooks, refer to the internal operations wiki.
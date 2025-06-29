# Billing Engine Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the Billing Engine service in production environments. It covers day-to-day operations, incident response, performance management, and financial compliance requirements.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Financial Operations](#financial-operations)
3. [Payment Processing](#payment-processing)
4. [Subscription Management](#subscription-management)
5. [Monitoring and Alerting](#monitoring-and-alerting)
6. [Performance Management](#performance-management)
7. [Backup and Recovery](#backup-and-recovery)
8. [Incident Response](#incident-response)
9. [Compliance and Auditing](#compliance-and-auditing)
10. [Maintenance Procedures](#maintenance-procedures)

## Service Operations

### Service Management

#### Starting the Billing Engine

**Production Environment:**
```bash
# Using PM2
pm2 start ecosystem.config.js --env production

# Using systemd
sudo systemctl start billing-engine

# Using Docker
docker-compose -f docker-compose.prod.yml up -d billing-engine

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n billing
kubectl scale deployment billing-engine --replicas=3 -n billing
```

#### Stopping the Service

```bash
# Graceful shutdown with PM2
pm2 stop billing-engine
pm2 delete billing-engine

# Systemd stop
sudo systemctl stop billing-engine

# Docker compose
docker-compose -f docker-compose.prod.yml stop billing-engine

# Kubernetes graceful shutdown
kubectl scale deployment billing-engine --replicas=0 -n billing
```

#### Service Status Checks

```bash
# PM2 status
pm2 status billing-engine
pm2 show billing-engine

# System service status
sudo systemctl status billing-engine

# Health endpoints
curl http://localhost:3401/health
curl http://localhost:3401/health/live
curl http://localhost:3401/health/ready

# Component health checks
curl http://localhost:3401/health/database
curl http://localhost:3401/health/redis
curl http://localhost:3401/health/payment-gateways
```

### Configuration Management

#### Dynamic Configuration Updates

```bash
# Reload configuration without restart
curl -X POST http://localhost:3401/admin/config/reload \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# View current configuration (sanitized)
curl http://localhost:3401/admin/config \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update specific settings
curl -X PATCH http://localhost:3401/admin/config \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "billing": {
      "retryAttempts": 5,
      "gracePeriodDays": 7
    }
  }'
```

#### Feature Flag Management

```bash
# View all feature flags
curl http://localhost:3401/admin/features \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Enable/disable features
curl -X PUT http://localhost:3401/admin/features/usage-billing \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"enabled": true}'

# A/B test configuration
curl -X PUT http://localhost:3401/admin/features/new-checkout-flow \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "percentage": 50,
    "criteria": {"segment": "premium"}
  }'
```

## Financial Operations

### Revenue Monitoring

#### Real-time Revenue Tracking

```bash
# Current day revenue
curl http://localhost:3401/api/v1/analytics/revenue/today \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Monthly recurring revenue (MRR)
curl http://localhost:3401/api/v1/analytics/mrr \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Revenue by product/plan
curl "http://localhost:3401/api/v1/analytics/revenue/breakdown?groupBy=plan&period=month" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Churn metrics
curl http://localhost:3401/api/v1/analytics/churn \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Financial Reconciliation

```bash
# Daily reconciliation report
curl -X POST http://localhost:3401/api/v1/reports/reconciliation \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-06-24",
    "includeDetails": true
  }'

# Payment gateway reconciliation
curl http://localhost:3401/api/v1/reconciliation/stripe/2024-06-24 \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Identify discrepancies
curl http://localhost:3401/api/v1/reconciliation/discrepancies \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Invoice Management

#### Invoice Operations

```bash
# Generate pending invoices
curl -X POST http://localhost:3401/api/v1/invoices/generate-pending \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Retry failed invoices
curl -X POST http://localhost:3401/api/v1/invoices/retry-failed \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-06-01",
    "endDate": "2024-06-24",
    "maxRetries": 3
  }'

# Send invoice reminders
curl -X POST http://localhost:3401/api/v1/invoices/send-reminders \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "daysOverdue": 3,
    "template": "payment-reminder"
  }'

# Void invoice
curl -X POST http://localhost:3401/api/v1/invoices/inv_123/void \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Duplicate charge"}'
```

### Tax Operations

#### Tax Calculation and Reporting

```bash
# Generate tax report
curl -X POST http://localhost:3401/api/v1/tax/report \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "period": "Q2-2024",
    "jurisdiction": "US-CA",
    "format": "csv"
  }'

# Update tax rates
curl -X PUT http://localhost:3401/api/v1/tax/rates \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jurisdiction": "US-NY",
    "rate": 0.08875,
    "effectiveDate": "2024-07-01"
  }'

# Validate tax exemptions
curl http://localhost:3401/api/v1/tax/exemptions/validate \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Payment Processing

### Payment Gateway Management

#### Gateway Health Monitoring

```bash
# Check gateway status
curl http://localhost:3401/api/v1/gateways/status \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Test gateway connectivity
curl -X POST http://localhost:3401/api/v1/gateways/test \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"gateway": "stripe", "amount": 100, "currency": "USD"}'

# Gateway performance metrics
curl http://localhost:3401/api/v1/gateways/metrics \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Payment Retry Management

```bash
# View retry queue
curl http://localhost:3401/api/v1/payments/retry-queue \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Manual payment retry
curl -X POST http://localhost:3401/api/v1/payments/pay_123/retry \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"useAlternateMethod": true}'

# Update retry strategy
curl -X PUT http://localhost:3401/api/v1/payments/retry-config \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "maxAttempts": 5,
    "backoffMultiplier": 2,
    "initialDelay": 3600
  }'
```

### Refund Processing

```bash
# Process refund
curl -X POST http://localhost:3401/api/v1/refunds \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "pay_123",
    "amount": 99.99,
    "reason": "customer_request",
    "notify": true
  }'

# Bulk refund processing
curl -X POST http://localhost:3401/api/v1/refunds/bulk \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentIds": ["pay_123", "pay_124", "pay_125"],
    "reason": "service_issue",
    "notifyCustomers": true
  }'

# Refund status report
curl http://localhost:3401/api/v1/refunds/report?startDate=2024-06-01 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Subscription Management

### Subscription Lifecycle

#### Subscription Monitoring

```bash
# Active subscriptions count
curl http://localhost:3401/api/v1/subscriptions/stats/active \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Subscriptions ending soon
curl http://localhost:3401/api/v1/subscriptions/ending-soon?days=30 \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Trial conversions
curl http://localhost:3401/api/v1/subscriptions/stats/trial-conversion \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Subscription health metrics
curl http://localhost:3401/api/v1/subscriptions/health \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Subscription Operations

```bash
# Pause subscription
curl -X POST http://localhost:3401/api/v1/subscriptions/sub_123/pause \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "customer_request",
    "resumeDate": "2024-07-01"
  }'

# Change subscription plan
curl -X PUT http://localhost:3401/api/v1/subscriptions/sub_123/plan \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "newPlanId": "plan_premium",
    "prorate": true,
    "effectiveDate": "next_billing_cycle"
  }'

# Cancel subscription
curl -X POST http://localhost:3401/api/v1/subscriptions/sub_123/cancel \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "customer_request",
    "feedback": "Too expensive",
    "cancelAtPeriodEnd": true
  }'
```

### Usage-Based Billing

```bash
# Record usage event
curl -X POST http://localhost:3401/api/v1/usage \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionId": "sub_123",
    "meterId": "api_calls",
    "quantity": 1000,
    "timestamp": "2024-06-24T10:00:00Z",
    "metadata": {"endpoint": "/api/v1/products"}
  }'

# Get usage summary
curl http://localhost:3401/api/v1/usage/sub_123/summary?period=current \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Reset usage counters
curl -X POST http://localhost:3401/api/v1/usage/reset \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"subscriptionId": "sub_123", "meterId": "api_calls"}'
```

## Monitoring and Alerting

### Key Performance Indicators

#### Financial KPIs

| Metric | Target | Warning | Critical |
|--------|--------|---------|----------|
| Payment Success Rate | > 95% | < 92% | < 90% |
| Average Transaction Time | < 2s | > 3s | > 5s |
| Failed Payment Rate | < 5% | > 8% | > 10% |
| Refund Rate | < 2% | > 5% | > 10% |
| Churn Rate | < 5% | > 8% | > 10% |

#### Operational KPIs

| Metric | Target | Warning | Critical |
|--------|--------|---------|----------|
| API Response Time | < 200ms | > 500ms | > 1000ms |
| Database Query Time | < 50ms | > 100ms | > 500ms |
| Queue Processing Time | < 30s | > 60s | > 300s |
| Error Rate | < 0.1% | > 1% | > 5% |

### Monitoring Setup

#### Prometheus Metrics

```yaml
# Custom billing metrics
- name: billing_revenue_total
  type: counter
  help: Total revenue processed
  labels: [currency, gateway, plan]

- name: billing_subscriptions_active
  type: gauge
  help: Number of active subscriptions
  labels: [plan, status]

- name: billing_payment_duration_seconds
  type: histogram
  help: Payment processing duration
  labels: [gateway, status]

- name: billing_refunds_total
  type: counter
  help: Total refunds processed
  labels: [reason, gateway]
```

#### Alert Rules

```yaml
groups:
  - name: billing_alerts
    rules:
      - alert: HighPaymentFailureRate
        expr: rate(billing_payments_failed_total[5m]) / rate(billing_payments_total[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High payment failure rate detected"
          description: "Payment failure rate is {{ $value | humanizePercentage }}"

      - alert: LowRevenueAlert
        expr: increase(billing_revenue_total[1h]) < 1000
        for: 2h
        labels:
          severity: warning
        annotations:
          summary: "Low revenue in the last hour"
          description: "Revenue in the last hour: ${{ $value }}"

      - alert: SubscriptionProcessingBacklog
        expr: billing_subscription_queue_size > 1000
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Large subscription processing backlog"
          description: "Queue size: {{ $value }}"
```

### Dashboards

#### Grafana Dashboard Configuration

```json
{
  "dashboard": {
    "title": "Billing Engine Overview",
    "panels": [
      {
        "title": "Revenue (24h)",
        "targets": [{
          "expr": "sum(increase(billing_revenue_total[24h])) by (currency)"
        }]
      },
      {
        "title": "Payment Success Rate",
        "targets": [{
          "expr": "rate(billing_payments_success_total[5m]) / rate(billing_payments_total[5m])"
        }]
      },
      {
        "title": "Active Subscriptions",
        "targets": [{
          "expr": "billing_subscriptions_active"
        }]
      },
      {
        "title": "API Response Time",
        "targets": [{
          "expr": "histogram_quantile(0.95, billing_http_request_duration_seconds_bucket)"
        }]
      }
    ]
  }
}
```

## Performance Management

### Performance Optimization

#### Database Query Optimization

```sql
-- Analyze slow queries
SELECT query, calls, mean_time, total_time
FROM pg_stat_statements
WHERE mean_time > 100
ORDER BY mean_time DESC
LIMIT 20;

-- Update table statistics
ANALYZE customers;
ANALYZE subscriptions;
ANALYZE invoices;
ANALYZE payments;

-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0
ORDER BY schemaname, tablename;
```

#### Redis Optimization

```bash
# Monitor Redis performance
redis-cli --latency

# Check memory usage
redis-cli INFO memory

# Optimize memory
redis-cli CONFIG SET maxmemory-policy allkeys-lru

# Monitor slow queries
redis-cli SLOWLOG GET 10
```

#### Node.js Performance

```bash
# Generate heap snapshot
kill -USR2 $(pgrep -f billing-engine)

# CPU profiling
node --prof src/index.js

# Process profile data
node --prof-process isolate-*.log > profile.txt

# Memory monitoring
node --expose-gc --trace-gc src/index.js
```

### Load Testing

```bash
# Run load test scenarios
npm run load-test:payments
npm run load-test:subscriptions
npm run load-test:webhooks

# Stress test payment processing
artillery run tests/load/payment-stress.yml

# Concurrent user simulation
artillery run tests/load/concurrent-users.yml
```

## Backup and Recovery

### Database Backup

#### Automated Backup Script

```bash
#!/bin/bash
# backup-billing-db.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/billing"
S3_BUCKET="exalt-billing-backups"

# Create backup directory
mkdir -p $BACKUP_DIR

# Database backup with compression
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME | gzip > $BACKUP_DIR/billing_db_$DATE.sql.gz

# Backup Redis state
redis-cli --rdb $BACKUP_DIR/redis_dump_$DATE.rdb

# Upload to S3
aws s3 cp $BACKUP_DIR/billing_db_$DATE.sql.gz s3://$S3_BUCKET/database/
aws s3 cp $BACKUP_DIR/redis_dump_$DATE.rdb s3://$S3_BUCKET/redis/

# Clean old local backups (keep 7 days)
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete
find $BACKUP_DIR -name "*.rdb" -mtime +7 -delete

# Verify backup
if [ $? -eq 0 ]; then
    echo "Backup completed successfully: $DATE"
    # Send success notification
    curl -X POST $SLACK_WEBHOOK -d "{\"text\":\"Billing DB backup completed: $DATE\"}"
else
    echo "Backup failed: $DATE"
    # Send failure alert
    curl -X POST $PAGERDUTY_WEBHOOK -d "{\"event_action\":\"trigger\",\"payload\":{\"summary\":\"Billing DB backup failed\"}}"
fi
```

#### Backup Schedule

```cron
# Crontab entries
# Full backup every 6 hours
0 */6 * * * /opt/billing/scripts/backup-billing-db.sh

# Incremental backup every hour
0 * * * * /opt/billing/scripts/incremental-backup.sh

# Weekly full backup with verification
0 2 * * 0 /opt/billing/scripts/full-backup-verify.sh
```

### Recovery Procedures

#### Database Recovery

```bash
# Stop billing service
kubectl scale deployment billing-engine --replicas=0 -n billing

# Restore database from backup
gunzip -c /backup/billing/billing_db_20240624_020000.sql.gz | psql -h $DB_HOST -U $DB_USER $DB_NAME

# Restore Redis data
redis-cli --rdb /backup/billing/redis_dump_20240624_020000.rdb

# Verify data integrity
npm run db:verify
npm run redis:verify

# Restart service
kubectl scale deployment billing-engine --replicas=3 -n billing
```

#### Point-in-Time Recovery

```bash
# Restore to specific timestamp
pg_restore -h $DB_HOST -U $DB_USER -d $DB_NAME \
  --clean --create --verbose \
  --target-time="2024-06-24 10:00:00" \
  /backup/billing/billing_db_20240624_020000.sql.gz
```

## Incident Response

### Incident Classification

| Severity | Response Time | Resolution Target | Examples |
|----------|---------------|-------------------|----------|
| Critical | 15 minutes | 1 hour | Payment gateway down, data breach |
| High | 30 minutes | 4 hours | Failed payments spike, subscription errors |
| Medium | 2 hours | 24 hours | Slow performance, minor bugs |
| Low | 8 hours | 1 week | UI issues, feature requests |

### Common Incident Procedures

#### Payment Gateway Failure

1. **Immediate Response**
   ```bash
   # Check gateway status
   curl http://localhost:3401/api/v1/gateways/status
   
   # Enable failover gateway
   curl -X POST http://localhost:3401/admin/gateways/failover \
     -H "Authorization: Bearer $ADMIN_TOKEN" \
     -d '{"primary": "stripe", "backup": "paypal"}'
   
   # Queue failed payments for retry
   curl -X POST http://localhost:3401/admin/payments/queue-failed \
     -H "Authorization: Bearer $ADMIN_TOKEN"
   ```

2. **Communication**
   ```bash
   # Send customer notifications
   curl -X POST http://localhost:3401/admin/notifications/payment-issue \
     -H "Authorization: Bearer $ADMIN_TOKEN" \
     -d '{"template": "payment-gateway-issue", "affectedGateway": "stripe"}'
   ```

#### High Churn Rate

1. **Analysis**
   ```bash
   # Generate churn analysis report
   curl -X POST http://localhost:3401/api/v1/analytics/churn-analysis \
     -H "Authorization: Bearer $ADMIN_TOKEN" \
     -d '{"period": "last_30_days", "includeReasons": true}'
   
   # Identify at-risk customers
   curl http://localhost:3401/api/v1/analytics/at-risk-customers \
     -H "Authorization: Bearer $ADMIN_TOKEN"
   ```

2. **Retention Actions**
   ```bash
   # Send retention offers
   curl -X POST http://localhost:3401/api/v1/campaigns/retention \
     -H "Authorization: Bearer $ADMIN_TOKEN" \
     -d '{
       "targetSegment": "at_risk",
       "offerType": "discount",
       "discountPercent": 25,
       "validityDays": 30
     }'
   ```

## Compliance and Auditing

### Financial Compliance

#### PCI DSS Compliance

```bash
# Run PCI compliance scan
npm run security:pci-scan

# Check encryption status
curl http://localhost:3401/admin/security/encryption-status \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Audit card data access
curl http://localhost:3401/admin/audit/card-data-access \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### SOX Compliance

```bash
# Generate SOX compliance report
curl -X POST http://localhost:3401/api/v1/compliance/sox-report \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"quarter": "Q2-2024", "includeControls": true}'

# Audit trail verification
curl http://localhost:3401/api/v1/audit/verify-integrity \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Audit Log Management

```bash
# Export audit logs
curl -X POST http://localhost:3401/api/v1/audit/export \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "startDate": "2024-06-01",
    "endDate": "2024-06-30",
    "format": "json",
    "includeSystemEvents": true
  }'

# Search audit logs
curl "http://localhost:3401/api/v1/audit/search?action=payment.processed&user=admin" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Verify audit log integrity
curl http://localhost:3401/api/v1/audit/verify-integrity \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-billing-maintenance.sh

echo "=== Billing Engine Daily Maintenance ===" > /tmp/billing-maintenance.log

# Health checks
curl -s http://localhost:3401/health >> /tmp/billing-maintenance.log

# Payment gateway status
curl -s http://localhost:3401/api/v1/gateways/status >> /tmp/billing-maintenance.log

# Failed payments count
FAILED_COUNT=$(curl -s http://localhost:3401/api/v1/payments/failed/count)
echo "Failed payments: $FAILED_COUNT" >> /tmp/billing-maintenance.log

# Subscription renewals pending
RENEWALS=$(curl -s http://localhost:3401/api/v1/subscriptions/pending-renewals/count)
echo "Pending renewals: $RENEWALS" >> /tmp/billing-maintenance.log

# Database maintenance
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "VACUUM ANALYZE;"

# Clear old webhook events
curl -X POST http://localhost:3401/admin/webhooks/cleanup \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Send report
mail -s "Billing Engine Daily Report" billing-ops@exalt.com < /tmp/billing-maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-billing-maintenance.sh

# Update exchange rates
curl -X POST http://localhost:3401/admin/exchange-rates/update \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Generate revenue report
curl -X POST http://localhost:3401/api/v1/reports/weekly-revenue \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"email": "finance@exalt.com"}'

# Clean up old invoices
curl -X POST http://localhost:3401/admin/invoices/archive \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"olderThan": "1 year"}'

# Optimize database
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "REINDEX DATABASE $DB_NAME;"

# Security audit
npm run security:audit
```

### Update Procedures

#### Rolling Updates

```bash
# Pre-update checks
npm run test:production
npm run db:migrate:dry-run

# Start rolling update
kubectl set image deployment/billing-engine \
  billing-engine=billing-engine:v2.1.0 \
  -n billing

# Monitor rollout
kubectl rollout status deployment/billing-engine -n billing

# Run post-deployment tests
npm run test:smoke

# Rollback if needed
kubectl rollout undo deployment/billing-engine -n billing
```

#### Database Schema Updates

```bash
# Create migration
npm run migration:create -- --name add_invoice_templates

# Test migration
npm run migration:test

# Apply migration with backup
npm run migration:apply-safe

# Verify migration
npm run db:verify-schema
```

### Emergency Procedures

#### Payment Processing Freeze

```bash
# Freeze all payment processing
curl -X POST http://localhost:3401/admin/emergency/freeze-payments \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"reason": "Security incident detected"}'

# Queue all incoming payments
curl -X POST http://localhost:3401/admin/emergency/queue-mode \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Resume payment processing
curl -X POST http://localhost:3401/admin/emergency/resume-payments \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Data Export (Emergency)

```bash
# Export all critical data
curl -X POST http://localhost:3401/admin/emergency/export-all \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"destination": "s3://emergency-backup/", "encrypt": true}'
```

### Contact Information

- **Primary On-Call**: +1-555-0300 (billing-primary@exalt.com)
- **Secondary On-Call**: +1-555-0301 (billing-secondary@exalt.com)
- **Finance Team**: finance-emergency@exalt.com
- **Security Team**: security-emergency@exalt.com
- **Database Team**: dba-emergency@exalt.com

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Monthly*
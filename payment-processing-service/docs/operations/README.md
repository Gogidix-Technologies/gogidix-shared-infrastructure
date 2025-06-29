# Payment Processing Service - Operations Guide

## Table of Contents
1. [Overview](#overview)
2. [Monitoring](#monitoring)
3. [Maintenance](#maintenance)
4. [Troubleshooting](#troubleshooting)
5. [PCI DSS Compliance](#pci-dss-compliance)
6. [Security Operations](#security-operations)
7. [Incident Response](#incident-response)
8. [Backup and Recovery](#backup-and-recovery)
9. [Performance Optimization](#performance-optimization)
10. [Compliance and Auditing](#compliance-and-auditing)

## Overview

This operations guide provides comprehensive procedures for maintaining, monitoring, and troubleshooting the Payment Processing Service in production environments. It covers daily operations, security protocols, compliance requirements, and emergency procedures.

### Service Criticality
- **Classification**: Tier 1 - Business Critical
- **SLA**: 99.99% availability (52.56 minutes downtime/year)
- **RPO**: 5 minutes
- **RTO**: 1 hour

## Monitoring

### 1. Key Performance Indicators (KPIs)

#### Business Metrics
```yaml
Payment Success Rate:
  Target: > 95%
  Alert Threshold: < 90%
  Measurement: successful_payments / total_payment_attempts

Average Processing Time:
  Target: < 2 seconds
  Alert Threshold: > 3 seconds
  Measurement: payment_processing_duration_seconds

Transaction Volume:
  Normal Range: 1,000 - 10,000 per hour
  Alert: > 20% deviation from baseline

Revenue Processing:
  Track: Hourly, Daily, Weekly, Monthly
  Alert: > 15% deviation from forecast
```

#### Technical Metrics
```yaml
API Response Time:
  P50: < 100ms
  P95: < 200ms
  P99: < 500ms

Error Rate:
  Target: < 0.1%
  Alert Threshold: > 0.5%

Database Performance:
  Connection Pool Usage: < 80%
  Query Time P99: < 100ms
  Deadlocks: 0

Redis Performance:
  Memory Usage: < 75%
  Hit Rate: > 90%
  Eviction Rate: < 1%
```

### 2. Monitoring Stack

#### Prometheus Metrics
```yaml
# Key metrics to monitor
- payment_processing_total
- payment_processing_duration_seconds
- payment_gateway_errors_total
- payment_fraud_detections_total
- webhook_processing_duration_seconds
- database_connection_pool_usage
- redis_memory_usage_bytes
```

#### Grafana Dashboards

**Payment Overview Dashboard**
```json
{
  "panels": [
    {
      "title": "Payment Success Rate",
      "query": "rate(payment_success_total[5m]) / rate(payment_attempts_total[5m])"
    },
    {
      "title": "Transaction Volume",
      "query": "sum(rate(payment_processing_total[5m])) by (gateway)"
    },
    {
      "title": "Processing Time",
      "query": "histogram_quantile(0.99, payment_processing_duration_seconds)"
    },
    {
      "title": "Gateway Errors",
      "query": "rate(payment_gateway_errors_total[5m]) by (gateway, error_type)"
    }
  ]
}
```

#### AlertManager Rules
```yaml
groups:
- name: payment_alerts
  rules:
  - alert: HighPaymentFailureRate
    expr: |
      (1 - (rate(payment_success_total[5m]) / rate(payment_attempts_total[5m]))) > 0.1
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High payment failure rate detected"
      description: "Payment failure rate is {{ $value | humanizePercentage }}"

  - alert: PaymentProcessingLatency
    expr: |
      histogram_quantile(0.99, payment_processing_duration_seconds) > 3
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High payment processing latency"
      description: "P99 latency is {{ $value }}s"

  - alert: DatabaseConnectionPoolExhausted
    expr: |
      hikari_connections_active / hikari_connections_max > 0.9
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "Database connection pool nearly exhausted"
      description: "Connection pool usage is {{ $value | humanizePercentage }}"
```

### 3. Log Aggregation

#### ELK Stack Configuration
```yaml
# Logstash pipeline
input {
  beats {
    port => 5044
  }
}

filter {
  if [service_name] == "payment-processing-service" {
    grok {
      match => {
        "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{GREEDYDATA:msg}"
      }
    }
    
    if [logger] =~ /PaymentController/ {
      grok {
        match => {
          "msg" => "Payment %{DATA:action} for amount %{NUMBER:amount} %{DATA:currency} with gateway %{DATA:gateway}"
        }
      }
      mutate {
        convert => { "amount" => "float" }
      }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "payment-logs-%{+YYYY.MM.dd}"
  }
}
```

#### Kibana Queries
```
# Failed payments
level:ERROR AND logger:*PaymentService* AND message:"Payment failed"

# Fraud detections
logger:*FraudDetectionService* AND action:blocked

# Webhook failures
logger:*WebhookController* AND level:ERROR

# Database errors
logger:*JdbcTemplate* AND level:ERROR
```

### 4. Application Performance Monitoring (APM)

#### Distributed Tracing with Jaeger
```yaml
# Key traces to monitor
- Payment flow end-to-end
- Gateway API calls
- Database transactions
- Cache operations
- Webhook processing

# Sampling configuration
jaeger:
  sampler:
    type: adaptive
    max-traces-per-second: 100
    sampling-server-url: http://jaeger-agent:5778/sampling
```

## Maintenance

### 1. Routine Maintenance Tasks

#### Daily Tasks
```bash
# 1. Check service health
curl -s http://payment-service:8092/actuator/health | jq .

# 2. Review error logs
kubectl logs -n exalt-shared deployment/payment-processing-service --since=24h | grep ERROR

# 3. Verify payment reconciliation
./scripts/daily-reconciliation.sh

# 4. Check webhook queue
redis-cli -h redis-host llen webhook_queue

# 5. Monitor disk usage
df -h /var/log/payment-service
```

#### Weekly Tasks
```bash
# 1. Database maintenance
psql -U payment_user -d payment_db -c "VACUUM ANALYZE;"

# 2. Clear old webhook logs
DELETE FROM webhook_logs WHERE created_at < NOW() - INTERVAL '30 days';

# 3. Update fraud rules
./scripts/update-fraud-rules.sh

# 4. Certificate expiration check
openssl x509 -enddate -noout -in /etc/ssl/certs/payment-service.crt

# 5. Dependency vulnerability scan
mvn dependency-check:check
```

#### Monthly Tasks
```bash
# 1. Full database backup and test restore
pg_dump -U payment_user payment_db > payment_db_backup_$(date +%Y%m%d).sql

# 2. Security audit
./scripts/security-audit.sh

# 3. Performance baseline update
./scripts/update-performance-baseline.sh

# 4. Disaster recovery drill
./scripts/dr-drill.sh

# 5. PCI compliance scan
./scripts/pci-compliance-check.sh
```

### 2. Database Maintenance

#### Index Management
```sql
-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'payment'
ORDER BY idx_scan;

-- Rebuild fragmented indexes
REINDEX TABLE payment.payments;
REINDEX TABLE payment.transactions;

-- Update statistics
ANALYZE payment.payments;
ANALYZE payment.transactions;
```

#### Partition Management
```sql
-- Create monthly partitions for transactions
CREATE TABLE payment.transactions_2024_01 PARTITION OF payment.transactions
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- Drop old partitions
DROP TABLE payment.transactions_2023_01;
```

### 3. Cache Maintenance

#### Redis Maintenance
```bash
# Memory optimization
redis-cli --bigkeys
redis-cli MEMORY DOCTOR

# Clear expired keys
redis-cli EVAL "return redis.call('del', unpack(redis.call('keys', 'payment:*:expired')))" 0

# Backup Redis data
redis-cli BGSAVE

# Compact AOF file
redis-cli BGREWRITEAOF
```

## Troubleshooting

### 1. Common Issues and Solutions

#### High Payment Failure Rate
```bash
# 1. Check gateway status
curl -s https://api.stripe.com/v1/status | jq .

# 2. Analyze failure patterns
SELECT 
    gateway,
    error_code,
    COUNT(*) as count,
    AVG(amount) as avg_amount
FROM payment.payments
WHERE status = 'FAILED'
AND created_at > NOW() - INTERVAL '1 hour'
GROUP BY gateway, error_code
ORDER BY count DESC;

# 3. Check fraud rules
SELECT * FROM payment.fraud_rules WHERE enabled = true;

# 4. Verify API credentials
./scripts/verify-gateway-credentials.sh
```

#### Performance Degradation
```bash
# 1. Check slow queries
SELECT 
    query,
    calls,
    mean_exec_time,
    total_exec_time
FROM pg_stat_statements
WHERE mean_exec_time > 100
ORDER BY mean_exec_time DESC
LIMIT 10;

# 2. Analyze connection pool
curl -s http://payment-service:8092/actuator/metrics/hikaricp.connections.active | jq .

# 3. Check GC performance
jstat -gcutil <pid> 1000 10

# 4. Thread dump analysis
jstack <pid> > thread_dump.txt
```

#### Webhook Processing Issues
```bash
# 1. Check webhook queue length
redis-cli llen webhook_queue

# 2. Inspect failed webhooks
redis-cli lrange webhook_dlq 0 10

# 3. Verify webhook signatures
./scripts/verify-webhook-signature.sh

# 4. Replay failed webhooks
./scripts/replay-webhooks.sh --from="2024-01-01" --to="2024-01-02"
```

### 2. Emergency Procedures

#### Service Outage
```bash
# 1. Immediate health check
./scripts/emergency-health-check.sh

# 2. Failover to backup region
kubectl patch service payment-processing-service \
  -p '{"spec":{"selector":{"region":"backup"}}}'

# 3. Enable circuit breaker
curl -X POST http://payment-service:8092/actuator/circuit-breaker/enable

# 4. Notify stakeholders
./scripts/send-outage-notification.sh
```

#### Data Corruption
```bash
# 1. Stop writes immediately
kubectl scale deployment payment-processing-service --replicas=0

# 2. Identify corruption extent
pg_dump --table=payments --data-only payment_db > payments_backup.sql

# 3. Restore from backup
psql -U payment_user payment_db < payment_db_backup_latest.sql

# 4. Verify data integrity
./scripts/verify-data-integrity.sh
```

## PCI DSS Compliance

### 1. Security Controls

#### Network Security
```yaml
# Firewall rules
- Allow inbound: 443 (HTTPS only)
- Allow outbound: Payment gateway IPs only
- Deny all other traffic

# Network segmentation
- DMZ: Load balancers, API gateway
- Application tier: Payment service
- Data tier: Database, Redis
- No direct internet access from data tier
```

#### Data Protection
```java
// Card data encryption
@Entity
public class PaymentMethod {
    @Convert(converter = CardNumberEncryptor.class)
    @Column(name = "card_number")
    private String cardNumber;
    
    @Convert(converter = CVVEncryptor.class)
    @Column(name = "cvv")
    @Transient // Never store CVV
    private String cvv;
}

// Tokenization
public class TokenizationService {
    public String tokenize(String cardNumber) {
        // Generate secure token
        String token = UUID.randomUUID().toString();
        
        // Store mapping securely
        tokenVault.store(token, encrypt(cardNumber));
        
        return token;
    }
}
```

### 2. Compliance Checklist

#### Daily Requirements
- [ ] Review access logs for unauthorized attempts
- [ ] Monitor file integrity on critical systems
- [ ] Check anti-virus definition updates
- [ ] Verify backup completion

#### Monthly Requirements
- [ ] Review user access rights
- [ ] Analyze security logs
- [ ] Update security patches
- [ ] Test incident response procedures

#### Quarterly Requirements
- [ ] Vulnerability scanning (ASV)
- [ ] Review firewall rules
- [ ] Update risk assessment
- [ ] Security awareness training

#### Annual Requirements
- [ ] Penetration testing
- [ ] Security policy review
- [ ] Full PCI assessment
- [ ] Business continuity test

### 3. Audit Trail

#### Required Audit Events
```java
@Component
public class AuditService {
    
    @EventListener
    public void auditPaymentEvent(PaymentEvent event) {
        AuditLog log = AuditLog.builder()
            .timestamp(Instant.now())
            .userId(SecurityContext.getUserId())
            .action(event.getType())
            .resourceType("Payment")
            .resourceId(event.getPaymentId())
            .ipAddress(RequestContext.getIpAddress())
            .userAgent(RequestContext.getUserAgent())
            .result(event.getResult())
            .build();
            
        auditRepository.save(log);
    }
}
```

## Security Operations

### 1. Access Control

#### Role-Based Access Control (RBAC)
```yaml
roles:
  payment-admin:
    permissions:
      - payment:read
      - payment:write
      - payment:refund
      - payment:configure
      - audit:read
      
  payment-operator:
    permissions:
      - payment:read
      - payment:refund
      - audit:read
      
  payment-viewer:
    permissions:
      - payment:read
      - audit:read
```

#### API Key Management
```bash
# Generate new API key
./scripts/generate-api-key.sh --client="merchant-123" --scopes="payment:create,payment:read"

# Rotate API keys
./scripts/rotate-api-keys.sh --older-than="90d"

# Revoke compromised key
./scripts/revoke-api-key.sh --key="sk_live_compromised_key"
```

### 2. Security Monitoring

#### Threat Detection Rules
```yaml
rules:
  - name: Brute Force Detection
    condition: failed_auth_attempts > 5 within 5m from same_ip
    action: block_ip_for_1h
    
  - name: Unusual Transaction Pattern
    condition: transaction_count > 100 within 1h from same_card
    action: flag_for_review
    
  - name: Geographic Anomaly
    condition: transaction_location distance > 1000km from last_transaction within 1h
    action: require_additional_verification
```

#### Security Incident Response
```bash
# 1. Contain the threat
./scripts/emergency-block-ip.sh --ip="malicious-ip"

# 2. Investigate
./scripts/security-investigation.sh --incident-id="INC-2024-001"

# 3. Collect evidence
./scripts/collect-forensics.sh --output="/secure/evidence/"

# 4. Report
./scripts/generate-incident-report.sh --incident-id="INC-2024-001"
```

## Incident Response

### 1. Incident Classification

| Severity | Description | Response Time | Examples |
|----------|-------------|---------------|----------|
| P1 | Critical - Service Down | 15 minutes | Complete outage, data breach |
| P2 | High - Major Impact | 30 minutes | Partial outage, high failure rate |
| P3 | Medium - Limited Impact | 2 hours | Performance degradation |
| P4 | Low - Minor Issue | 24 hours | Non-critical bugs |

### 2. Response Procedures

#### P1 Incident Response
```bash
# 1. Initial Response (0-15 min)
- Page on-call engineer
- Create incident channel
- Initial assessment
- Start incident log

# 2. Triage (15-30 min)
- Identify root cause
- Implement immediate mitigation
- Notify stakeholders
- Update status page

# 3. Resolution (30+ min)
- Deploy fix
- Verify resolution
- Monitor for stability
- Document timeline

# 4. Post-Incident (24-48 hours)
- Conduct post-mortem
- Create action items
- Update runbooks
- Share learnings
```

### 3. Communication Templates

#### Status Page Update
```markdown
**Investigating** - We are investigating reports of payment processing delays. 
Some customers may experience longer than usual processing times.

**Identified** - We have identified an issue with our payment gateway connection. 
Our team is working on a resolution.

**Monitoring** - A fix has been implemented and we are monitoring the results. 
Payment processing has returned to normal.

**Resolved** - This incident has been resolved. All systems are operational.
```

## Backup and Recovery

### 1. Backup Strategy

#### Database Backups
```bash
# Full backup (daily)
pg_dump -h $DB_HOST -U $DB_USER -d payment_db \
  --format=custom \
  --verbose \
  --file=payment_db_$(date +%Y%m%d).dump

# Incremental backup (hourly)
pg_basebackup -h $DB_HOST -U replication_user \
  -D /backup/incremental/$(date +%Y%m%d_%H) \
  --wal-method=stream \
  --checkpoint=fast

# Verify backup
pg_restore --list payment_db_$(date +%Y%m%d).dump
```

#### Application State Backup
```bash
# Redis backup
redis-cli BGSAVE
cp /var/lib/redis/dump.rdb /backup/redis/dump_$(date +%Y%m%d).rdb

# Configuration backup
tar -czf /backup/config/payment_config_$(date +%Y%m%d).tar.gz \
  /etc/payment-service/ \
  /opt/payment-service/config/
```

### 2. Recovery Procedures

#### Database Recovery
```bash
# Point-in-time recovery
pg_restore -h $DB_HOST -U $DB_USER -d payment_db_recovery \
  --clean \
  --if-exists \
  --verbose \
  payment_db_20240101.dump

# Replay WAL logs to specific time
recovery_target_time = '2024-01-01 14:30:00'
```

#### Disaster Recovery
```bash
# 1. Activate DR site
./scripts/activate-dr-site.sh --region="us-west-2"

# 2. Update DNS
./scripts/update-dns.sh --record="payments.exalt.com" --target="dr-payments.exalt.com"

# 3. Restore data
./scripts/restore-from-s3.sh --bucket="payment-backups" --date="2024-01-01"

# 4. Verify services
./scripts/dr-verification.sh
```

## Performance Optimization

### 1. Database Optimization

#### Query Optimization
```sql
-- Identify slow queries
SELECT 
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    stddev_exec_time
FROM pg_stat_statements
WHERE mean_exec_time > 100
ORDER BY mean_exec_time DESC;

-- Add missing indexes
CREATE INDEX CONCURRENTLY idx_payments_customer_created 
ON payment.payments(customer_id, created_at DESC);

-- Optimize common queries
CREATE MATERIALIZED VIEW payment_daily_summary AS
SELECT 
    DATE(created_at) as payment_date,
    gateway,
    currency,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount
FROM payment.payments
WHERE status = 'COMPLETED'
GROUP BY DATE(created_at), gateway, currency;
```

#### Connection Pool Tuning
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-test-query: SELECT 1
```

### 2. Application Optimization

#### JVM Tuning
```bash
JAVA_OPTS="-server \
  -Xms2g \
  -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=16m \
  -XX:+ParallelRefProcEnabled \
  -XX:+DisableExplicitGC \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/payment-service/ \
  -Djava.security.egd=file:/dev/./urandom"
```

#### Caching Strategy
```java
@Cacheable(value = "payment-methods", key = "#customerId")
public List<PaymentMethod> getPaymentMethods(String customerId) {
    return paymentMethodRepository.findByCustomerId(customerId);
}

@CacheEvict(value = "payment-methods", key = "#customerId")
public void updatePaymentMethod(String customerId, PaymentMethod method) {
    paymentMethodRepository.save(method);
}
```

### 3. Load Testing

#### JMeter Test Plan
```xml
<ThreadGroup>
  <stringProp name="ThreadGroup.num_threads">100</stringProp>
  <stringProp name="ThreadGroup.ramp_time">60</stringProp>
  <stringProp name="ThreadGroup.duration">300</stringProp>
  
  <HTTPSamplerProxy>
    <stringProp name="HTTPSampler.domain">payments.exalt.com</stringProp>
    <stringProp name="HTTPSampler.path">/api/v1/payments</stringProp>
    <stringProp name="HTTPSampler.method">POST</stringProp>
  </HTTPSamplerProxy>
</ThreadGroup>
```

## Compliance and Auditing

### 1. Regulatory Compliance

#### GDPR Compliance
```java
@RestController
@RequestMapping("/api/v1/privacy")
public class PrivacyController {
    
    @PostMapping("/data-export/{customerId}")
    public ResponseEntity<CustomerData> exportCustomerData(@PathVariable String customerId) {
        // Export all customer payment data
        return ResponseEntity.ok(privacyService.exportCustomerData(customerId));
    }
    
    @DeleteMapping("/data-erasure/{customerId}")
    public ResponseEntity<Void> eraseCustomerData(@PathVariable String customerId) {
        // Implement right to be forgotten
        privacyService.eraseCustomerData(customerId);
        return ResponseEntity.noContent().build();
    }
}
```

#### SOX Compliance
```sql
-- Audit trail for financial transactions
CREATE TABLE payment.audit_log (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    ip_address INET,
    user_agent TEXT,
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_resource (resource_type, resource_id)
);
```

### 2. Audit Reports

#### Monthly Compliance Report
```sql
-- Payment processing summary
SELECT 
    DATE_TRUNC('month', created_at) as month,
    COUNT(*) as total_transactions,
    SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as successful,
    SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed,
    SUM(amount) as total_volume,
    AVG(amount) as avg_transaction_size
FROM payment.payments
WHERE created_at >= DATE_TRUNC('month', CURRENT_DATE - INTERVAL '1 month')
GROUP BY DATE_TRUNC('month', created_at);

-- Security events summary
SELECT 
    event_type,
    COUNT(*) as occurrences,
    COUNT(DISTINCT user_id) as unique_users,
    COUNT(DISTINCT ip_address) as unique_ips
FROM payment.security_events
WHERE created_at >= DATE_TRUNC('month', CURRENT_DATE - INTERVAL '1 month')
GROUP BY event_type
ORDER BY occurrences DESC;
```

### 3. Continuous Compliance Monitoring

#### Automated Compliance Checks
```bash
#!/bin/bash
# Daily compliance check script

# Check encryption status
echo "Checking encryption compliance..."
./scripts/verify-encryption.sh

# Verify access controls
echo "Checking access control compliance..."
./scripts/audit-access-controls.sh

# Validate data retention
echo "Checking data retention compliance..."
./scripts/verify-data-retention.sh

# Generate compliance report
echo "Generating compliance report..."
./scripts/generate-compliance-report.sh --output="/reports/compliance_$(date +%Y%m%d).pdf"
```

## Appendix

### A. Emergency Contacts

| Role | Name | Phone | Email |
|------|------|-------|-------|
| Payment Lead | John Smith | +1-555-0100 | john.smith@exalt.com |
| Security Lead | Jane Doe | +1-555-0101 | jane.doe@exalt.com |
| Database Admin | Bob Wilson | +1-555-0102 | bob.wilson@exalt.com |
| Stripe Support | - | +1-888-584-4559 | support@stripe.com |
| PayPal Support | - | +1-888-221-1161 | support@paypal.com |

### B. Useful Commands

```bash
# Quick health check
curl -s http://localhost:8092/actuator/health | jq .

# Payment statistics
curl -s http://localhost:8092/actuator/metrics/payment.success.rate | jq .

# Force garbage collection
curl -X POST http://localhost:8092/actuator/gc

# Thread dump
curl -s http://localhost:8092/actuator/threaddump | jq .

# Heap dump
curl -X POST http://localhost:8092/actuator/heapdump -o heap.hprof
```

### C. Reference Documentation

- [PCI DSS v4.0 Requirements](https://www.pcisecuritystandards.org/)
- [Stripe API Documentation](https://stripe.com/docs/api)
- [PayPal Developer Documentation](https://developer.paypal.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [PostgreSQL Performance Tuning](https://www.postgresql.org/docs/current/performance-tips.html)
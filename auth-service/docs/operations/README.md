# Authentication Service Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the Authentication Service in production environments. It covers day-to-day operations, security management, incident response, and compliance requirements.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Security Operations](#security-operations)
3. [User Management](#user-management)
4. [Session Management](#session-management)
5. [Monitoring and Alerting](#monitoring-and-alerting)
6. [Performance Management](#performance-management)
7. [Backup and Recovery](#backup-and-recovery)
8. [Incident Response](#incident-response)
9. [Compliance and Auditing](#compliance-and-auditing)
10. [Maintenance Procedures](#maintenance-procedures)

## Service Operations

### Service Management

#### Starting the Authentication Service

**Production Environment:**
```bash
# Using systemd
sudo systemctl start auth-service

# Using Docker
docker-compose -f docker-compose.prod.yml up -d auth-service

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n auth-service
kubectl scale deployment auth-service --replicas=3 -n auth-service
```

#### Stopping the Service

```bash
# Graceful shutdown (allows active sessions to complete)
curl -X POST http://localhost:8083/actuator/shutdown

# Force stop if needed
sudo systemctl stop auth-service

# Kubernetes graceful shutdown
kubectl delete deployment auth-service -n auth-service --grace-period=60
```

#### Service Status Checks

```bash
# System status
sudo systemctl status auth-service

# Health endpoints
curl http://localhost:8083/actuator/health
curl http://localhost:8083/actuator/health/readiness
curl http://localhost:8083/actuator/health/liveness

# Database connectivity
curl http://localhost:8083/actuator/health/db

# Redis connectivity
curl http://localhost:8083/actuator/health/redis

# Eureka registration status
curl http://localhost:8083/actuator/health/eureka
```

### Configuration Management

#### Dynamic Configuration Updates

```bash
# Refresh configuration from config server
curl -X POST http://localhost:8083/actuator/refresh

# View current configuration
curl http://localhost:8083/actuator/configprops | jq '.security'

# Update JWT settings (requires restart)
curl -X PUT http://localhost:8083/actuator/env \
  -H "Content-Type: application/json" \
  -d '{"name": "security.jwt.expiration.access-token", "value": "7200"}'

# Update password policy
curl -X PUT http://localhost:8083/actuator/auth/password-policy \
  -H "Content-Type: application/json" \
  -d '{
    "minLength": 10,
    "requireUppercase": true,
    "requireLowercase": true,
    "requireDigits": true,
    "requireSpecialCharacters": true
  }'
```

#### Feature Flag Management

```bash
# Check feature flag status
curl http://localhost:8083/actuator/features

# Enable multi-factor authentication
curl -X POST http://localhost:8083/actuator/features/mfa/enable

# Disable social login
curl -X POST http://localhost:8083/actuator/features/social-login/disable

# Update rate limiting settings
curl -X PUT http://localhost:8083/actuator/auth/rate-limiting \
  -H "Content-Type: application/json" \
  -d '{
    "maxLoginAttempts": 3,
    "lockoutDuration": 900,
    "requestsPerMinute": 60
  }'
```

## Security Operations

### Authentication Monitoring

#### Failed Login Monitoring

```bash
# Monitor failed login attempts
curl http://localhost:8083/actuator/auth/failed-logins | jq '.'

# Get failed logins for specific user
curl "http://localhost:8083/actuator/auth/failed-logins?email=user@gogidix.com"

# Monitor brute force attempts
curl http://localhost:8083/actuator/auth/brute-force-attempts

# Check account lockouts
curl http://localhost:8083/actuator/auth/locked-accounts
```

#### Security Event Monitoring

```bash
# Real-time security events
tail -f /var/log/auth-service/security.log | grep "SECURITY_EVENT"

# Monitor authentication success rate
curl http://localhost:8083/actuator/metrics/auth.authentication.success.rate

# Monitor suspicious activities
curl http://localhost:8083/actuator/auth/suspicious-activities

# Check for concurrent session violations
curl http://localhost:8083/actuator/auth/session-violations
```

### Token Management

#### JWT Token Operations

```bash
# Check active tokens count
curl http://localhost:8083/actuator/auth/tokens/active-count

# Revoke specific token
curl -X POST http://localhost:8083/actuator/auth/tokens/revoke \
  -H "Content-Type: application/json" \
  -d '{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'

# Revoke all tokens for user
curl -X POST http://localhost:8083/actuator/auth/tokens/revoke-user \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-uuid-here"}'

# Check token blacklist size
curl http://localhost:8083/actuator/auth/tokens/blacklist-size

# Clean expired tokens from blacklist
curl -X POST http://localhost:8083/actuator/auth/tokens/cleanup-blacklist
```

#### Token Rotation

```bash
# Force token rotation for security incident
curl -X POST http://localhost:8083/actuator/auth/tokens/rotate-all

# Update JWT signing secret (requires coordinated deployment)
curl -X POST http://localhost:8083/actuator/auth/jwt/rotate-secret

# Verify token rotation completion
curl http://localhost:8083/actuator/auth/tokens/rotation-status
```

### Access Control Management

#### Role and Permission Updates

```bash
# List all roles
curl http://localhost:8083/api/v1/admin/roles

# Create new role
curl -X POST http://localhost:8083/api/v1/admin/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "WAREHOUSE_SUPERVISOR",
    "description": "Warehouse Supervisor Role",
    "category": "WAREHOUSE"
  }'

# Assign permission to role
curl -X POST http://localhost:8083/api/v1/admin/roles/role-id/permissions \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"permissionId": "permission-uuid"}'

# Update user roles
curl -X PUT http://localhost:8083/api/v1/admin/users/user-id/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roleIds": ["role-uuid-1", "role-uuid-2"]}'
```

## User Management

### User Account Operations

#### User Account Administration

```bash
# List users with pagination
curl "http://localhost:8083/api/v1/admin/users?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Get user details
curl http://localhost:8083/api/v1/admin/users/user-id \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update user information
curl -X PUT http://localhost:8083/api/v1/admin/users/user-id \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Updated",
    "lastName": "Name",
    "email": "updated@gogidix.com"
  }'

# Enable/disable user account
curl -X POST http://localhost:8083/api/v1/admin/users/user-id/enable \
  -H "Authorization: Bearer $ADMIN_TOKEN"

curl -X POST http://localhost:8083/api/v1/admin/users/user-id/disable \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Password Management

```bash
# Force password reset for user
curl -X POST http://localhost:8083/api/v1/admin/users/user-id/force-password-reset \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Unlock user account
curl -X POST http://localhost:8083/api/v1/admin/users/user-id/unlock \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Check password expiry status
curl http://localhost:8083/api/v1/admin/users/password-expiry-report \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Bulk password expiry notification
curl -X POST http://localhost:8083/api/v1/admin/users/notify-password-expiry \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"daysBeforeExpiry": 7}'
```

### Multi-Factor Authentication Management

#### MFA Operations

```bash
# Check MFA enrollment status
curl http://localhost:8083/actuator/auth/mfa/enrollment-stats

# Disable MFA for user (emergency)
curl -X POST http://localhost:8083/api/v1/admin/users/user-id/mfa/disable \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "User lost access to MFA device"}'

# Generate emergency backup codes
curl -X POST http://localhost:8083/api/v1/admin/users/user-id/mfa/emergency-codes \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# MFA method statistics
curl http://localhost:8083/actuator/auth/mfa/method-statistics
```

## Session Management

### Session Monitoring

#### Active Session Management

```bash
# Get active sessions count
curl http://localhost:8083/actuator/auth/sessions/active-count

# List active sessions for user
curl "http://localhost:8083/api/v1/admin/users/user-id/sessions" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Get session details
curl http://localhost:8083/api/v1/admin/sessions/session-id \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Terminate specific session
curl -X DELETE http://localhost:8083/api/v1/admin/sessions/session-id \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Terminate all user sessions
curl -X DELETE http://localhost:8083/api/v1/admin/users/user-id/sessions \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Session Security

```bash
# Check for suspicious session activities
curl http://localhost:8083/actuator/auth/sessions/suspicious

# Monitor concurrent session violations
curl http://localhost:8083/actuator/auth/sessions/concurrent-violations

# Session timeout configuration
curl -X PUT http://localhost:8083/actuator/auth/sessions/timeout \
  -H "Content-Type: application/json" \
  -d '{"timeoutMinutes": 480}'

# Force session cleanup
curl -X POST http://localhost:8083/actuator/auth/sessions/cleanup
```

## Monitoring and Alerting

### Key Performance Indicators

#### Authentication Metrics

| Metric | Normal Range | Warning Threshold | Critical Threshold |
|--------|--------------|-------------------|-------------------|
| Authentication Success Rate | > 95% | < 90% | < 80% |
| Average Response Time | < 200ms | > 500ms | > 1000ms |
| Failed Login Rate | < 5% | > 10% | > 20% |
| Token Generation Rate | < 1000/min | > 1500/min | > 2000/min |
| Database Connection Pool | < 80% | > 90% | > 95% |

#### Security Metrics

| Metric | Normal Range | Alert Threshold | Action Required |
|--------|--------------|-----------------|----------------|
| Brute Force Attempts | < 10/hour | > 50/hour | Block IP ranges |
| Account Lockouts | < 5/hour | > 20/hour | Investigate patterns |
| Suspicious Activities | 0 | > 5/hour | Security review |
| MFA Bypass Attempts | 0 | > 1 | Immediate investigation |
| Token Revocations | < 10/hour | > 50/hour | Security analysis |

### Monitoring Setup

#### Prometheus Metrics

```yaml
# Custom authentication metrics
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,auth
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      service: auth-service
      environment: ${SPRING_PROFILES_ACTIVE}
    distribution:
      percentiles-histogram:
        auth.authentication.duration: true
        auth.token.generation.duration: true
```

#### Key Metrics Queries

```promql
# Authentication success rate
rate(auth_authentication_success_total[5m]) / rate(auth_authentication_attempts_total[5m])

# Average authentication time
histogram_quantile(0.95, auth_authentication_duration_seconds_bucket)

# Failed login rate by user
rate(auth_authentication_failure_total[5m]) by (user_email)

# Token generation rate
rate(auth_token_generation_total[5m])

# Active sessions count
auth_sessions_active_count

# Database connection pool usage
hikaricp_connections_active / hikaricp_connections_max
```

### Alert Rules

#### Prometheus Alert Configuration

```yaml
groups:
  - name: auth-service
    rules:
      - alert: AuthServiceHighFailureRate
        expr: rate(auth_authentication_failure_total[5m]) / rate(auth_authentication_attempts_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High authentication failure rate"
          description: "Authentication failure rate is {{ $value | humanizePercentage }}"

      - alert: AuthServiceBruteForceAttack
        expr: rate(auth_brute_force_attempts_total[5m]) > 10
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Brute force attack detected"
          description: "Brute force attempts: {{ $value }}/sec"

      - alert: AuthServiceDatabaseConnectionHigh
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High database connection usage"
          description: "Connection pool usage: {{ $value | humanizePercentage }}"

      - alert: AuthServiceTokenBlacklistGrowth
        expr: increase(auth_token_blacklist_size[1h]) > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Rapid token blacklist growth"
          description: "Blacklist grew by {{ $value }} tokens in 1 hour"
```

## Performance Management

### Performance Optimization

#### Database Query Optimization

```sql
-- Monitor slow queries
SELECT query, mean_time, calls, total_time
FROM pg_stat_statements
WHERE mean_time > 100
ORDER BY mean_time DESC
LIMIT 10;

-- Add missing indexes
CREATE INDEX CONCURRENTLY idx_users_email_enabled ON users(email, enabled);
CREATE INDEX CONCURRENTLY idx_sessions_user_expires ON user_sessions(user_id, expires_at);
CREATE INDEX CONCURRENTLY idx_audit_log_user_time ON security_audit_log(user_id, created_at);

-- Update table statistics
ANALYZE users;
ANALYZE user_sessions;
ANALYZE security_audit_log;
```

#### JVM Performance Tuning

```bash
# Production JVM settings for high load
export JAVA_OPTS="
  -Xmx4g
  -Xms4g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+OptimizeStringConcat
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/auth-service/heapdumps/
  -Dspring.jpa.hibernate.jdbc.batch_size=25
  -Dspring.jpa.hibernate.order_inserts=true
  -Dspring.jpa.hibernate.order_updates=true
  -Dserver.tomcat.max-threads=300
  -Dserver.tomcat.max-connections=4096
"
```

#### Redis Configuration Optimization

```bash
# Redis configuration for session storage
redis-cli CONFIG SET maxmemory 2gb
redis-cli CONFIG SET maxmemory-policy allkeys-lru
redis-cli CONFIG SET timeout 300
redis-cli CONFIG SET tcp-keepalive 60

# Monitor Redis performance
redis-cli INFO stats
redis-cli MONITOR
```

### Load Testing

#### Authentication Load Tests

```bash
# Test authentication endpoint
artillery quick --count 100 --num 50 http://localhost:8083/api/v1/auth/login

# Test token validation
artillery quick --count 200 --num 100 http://localhost:8083/api/v1/auth/validate

# Concurrent session test
./scripts/concurrent-session-test.sh 1000 60

# MFA verification load test
./scripts/mfa-load-test.sh 500 30
```

#### Performance Monitoring During Tests

```bash
# Monitor authentication metrics
watch -n 5 'curl -s http://localhost:8083/actuator/metrics/auth.authentication.duration | jq ".measurements[0].value"'

# Monitor JVM metrics
watch -n 5 'curl -s http://localhost:8083/actuator/metrics/jvm.memory.used | jq ".measurements[0].value"'

# Monitor database connections
watch -n 5 'curl -s http://localhost:8083/actuator/metrics/hikaricp.connections.active | jq ".measurements[0].value"'
```

## Backup and Recovery

### Database Backup

#### Automated Backup Script

```bash
#!/bin/bash
# backup-auth-database.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/auth-service"
DB_NAME="authservice_db"
DB_USER="authservice"

# Create backup directory
mkdir -p $BACKUP_DIR

# Database backup
pg_dump -h localhost -U $DB_USER -d $DB_NAME > $BACKUP_DIR/auth_db_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/auth_db_$DATE.sql

# Clean old backups (keep 30 days)
find $BACKUP_DIR -name "auth_db_*.sql.gz" -mtime +30 -delete

# Backup verification
if [ -f "$BACKUP_DIR/auth_db_$DATE.sql.gz" ]; then
    echo "Database backup completed successfully: $DATE"
    
    # Test backup integrity
    gunzip -t $BACKUP_DIR/auth_db_$DATE.sql.gz
    if [ $? -eq 0 ]; then
        echo "Backup integrity verified"
    else
        echo "Backup integrity check failed"
        exit 1
    fi
else
    echo "Database backup failed"
    exit 1
fi

# Upload to cloud storage (optional)
aws s3 cp $BACKUP_DIR/auth_db_$DATE.sql.gz s3://gogidix-backups/auth-service/
```

#### Backup Schedule

```cron
# Crontab entries
# Full backup daily at 2 AM
0 2 * * * /opt/auth-service/scripts/backup-auth-database.sh >> /var/log/backup.log 2>&1

# Incremental backup every 6 hours
0 */6 * * * /opt/auth-service/scripts/incremental-backup.sh >> /var/log/backup.log 2>&1

# Configuration backup weekly
0 3 * * 0 /opt/auth-service/scripts/backup-config.sh >> /var/log/backup.log 2>&1
```

### Recovery Procedures

#### Database Recovery

```bash
# Stop auth service
kubectl scale deployment auth-service --replicas=0 -n auth-service

# Restore database from backup
gunzip -c /backup/auth-service/auth_db_20240624_020000.sql.gz | psql -h localhost -U authservice authservice_db

# Verify data integrity
psql -h localhost -U authservice -d authservice_db -c "SELECT COUNT(*) FROM users;"
psql -h localhost -U authservice -d authservice_db -c "SELECT COUNT(*) FROM user_sessions;"

# Restart auth service
kubectl scale deployment auth-service --replicas=3 -n auth-service

# Verify service health
kubectl get pods -l app=auth-service -n auth-service
curl http://auth-service:8083/actuator/health
```

#### Configuration Recovery

```bash
# Restore JWT secrets
kubectl delete secret jwt-secret -n auth-service
kubectl apply -f backup/jwt-secret-20240624.yaml

# Restore database connection secrets
kubectl delete secret auth-db-secret -n auth-service
kubectl apply -f backup/auth-db-secret-20240624.yaml

# Restart service to pick up new configuration
kubectl rollout restart deployment/auth-service -n auth-service
```

## Incident Response

### Incident Classification

| Severity | Response Time | Resolution Target | Examples |
|----------|---------------|-------------------|----------|
| Critical | 15 minutes | 1 hour | Service down, data breach, authentication bypass |
| High | 30 minutes | 4 hours | High error rate, performance degradation, security alerts |
| Medium | 2 hours | 24 hours | Feature issues, minor security concerns |
| Low | 8 hours | 1 week | Enhancement requests, documentation updates |

### Common Incident Procedures

#### Service Unavailable

1. **Immediate Response**
   ```bash
   # Check service status
   kubectl get pods -l app=auth-service -n auth-service
   
   # Check logs
   kubectl logs -l app=auth-service -n auth-service --tail=100
   
   # Check database connectivity
   curl http://auth-service:8083/actuator/health/db
   
   # Restart if needed
   kubectl rollout restart deployment/auth-service -n auth-service
   ```

2. **Root Cause Analysis**
   ```bash
   # Check resource usage
   kubectl top pods -l app=auth-service -n auth-service
   
   # Check events
   kubectl get events -n auth-service --sort-by='.lastTimestamp'
   
   # Analyze logs
   grep -i "error\|exception" /var/log/auth-service/application.log | tail -50
   ```

#### Security Incident

1. **Immediate Actions**
   ```bash
   # Check for suspicious activities
   curl http://localhost:8083/actuator/auth/suspicious-activities
   
   # Block suspicious IPs
   curl -X POST http://localhost:8083/actuator/auth/block-ip \
     -d '{"ipAddress": "192.168.1.100", "reason": "Suspicious activity"}'
   
   # Revoke all tokens if necessary
   curl -X POST http://localhost:8083/actuator/auth/tokens/revoke-all
   
   # Enable emergency mode
   curl -X POST http://localhost:8083/actuator/auth/emergency-mode
   ```

2. **Investigation**
   ```bash
   # Collect security logs
   grep "SECURITY_VIOLATION\|AUTH_FAILURE" /var/log/auth-service/security.log > incident-logs.txt
   
   # Generate security report
   curl http://localhost:8083/actuator/auth/security-report > security-incident.json
   
   # Analyze access patterns
   curl http://localhost:8083/api/v1/admin/audit/access-patterns \
     -H "Authorization: Bearer $ADMIN_TOKEN"
   ```

## Compliance and Auditing

### Regulatory Compliance

#### GDPR Compliance Operations

```bash
# Export user data (GDPR Article 20)
curl -X GET "http://localhost:8083/api/v1/admin/users/user-id/export" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Delete user data (Right to be forgotten)
curl -X DELETE "http://localhost:8083/api/v1/admin/users/user-id/gdpr-delete" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Data processing audit
curl "http://localhost:8083/api/v1/admin/audit/data-processing?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Audit Log Management

```bash
# Generate compliance report
curl "http://localhost:8083/api/v1/admin/audit/compliance-report?type=SOX&period=quarterly" \
  -H "Authorization: Bearer $ADMIN_TOKEN" > sox-report.json

# Export audit logs
curl "http://localhost:8083/api/v1/admin/audit/export?format=json&startDate=2024-06-01" \
  -H "Authorization: Bearer $ADMIN_TOKEN" > audit-export.json

# Audit log integrity verification
curl -X POST "http://localhost:8083/api/v1/admin/audit/verify-integrity" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Security Auditing

#### Access Review

```bash
# Generate access review report
curl "http://localhost:8083/api/v1/admin/audit/access-review" \
  -H "Authorization: Bearer $ADMIN_TOKEN" > access-review.json

# Privileged access report
curl "http://localhost:8083/api/v1/admin/audit/privileged-access" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Dormant account report
curl "http://localhost:8083/api/v1/admin/audit/dormant-accounts?days=90" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-auth-maintenance.sh

echo "=== Auth Service Daily Maintenance ===" > /tmp/auth-maintenance.log

# Check service health
curl -f http://localhost:8083/actuator/health >> /tmp/auth-maintenance.log

# Check database health
curl http://localhost:8083/actuator/health/db | jq '.status' >> /tmp/auth-maintenance.log

# Check active sessions count
curl http://localhost:8083/actuator/auth/sessions/active-count >> /tmp/auth-maintenance.log

# Check token blacklist size
curl http://localhost:8083/actuator/auth/tokens/blacklist-size >> /tmp/auth-maintenance.log

# Check failed login attempts in last 24 hours
curl "http://localhost:8083/actuator/auth/failed-logins?hours=24" | jq 'length' >> /tmp/auth-maintenance.log

# Cleanup expired sessions
curl -X POST http://localhost:8083/actuator/auth/sessions/cleanup

# Send report
mail -s "Auth Service Daily Report" auth-team@gogidix.com < /tmp/auth-maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-auth-maintenance.sh

# Update security statistics
curl -X POST http://localhost:8083/actuator/auth/statistics/update

# Clean up old audit logs
find /var/log/auth-service -name "*.log.*" -mtime +30 -delete

# Rotate JWT blacklist (remove expired tokens)
curl -X POST http://localhost:8083/actuator/auth/tokens/cleanup-blacklist

# Generate weekly security report
curl "http://localhost:8083/api/v1/admin/audit/weekly-report" \
  -H "Authorization: Bearer $ADMIN_TOKEN" > /var/reports/auth-security-$(date +%Y%m%d).json

# Database maintenance
psql -h localhost -U authservice -d authservice_db -c "VACUUM ANALYZE;"

# Check for accounts requiring password update
curl "http://localhost:8083/api/v1/admin/users/password-expiry-report" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Update Procedures

#### Rolling Updates

```bash
# Update deployment with new image
kubectl set image deployment/auth-service \
  auth-service=auth-service:v2.1.0 \
  -n auth-service

# Monitor rollout
kubectl rollout status deployment/auth-service -n auth-service

# Verify health after update
kubectl get pods -l app=auth-service -n auth-service
curl http://auth-service/actuator/health

# Run post-deployment tests
./scripts/post-deployment-tests.sh

# Rollback if necessary
kubectl rollout undo deployment/auth-service -n auth-service
```

#### Database Schema Updates

```bash
# Run Flyway migrations
curl -X POST http://localhost:8083/actuator/flyway/migrate

# Check migration status
curl http://localhost:8083/actuator/flyway/info

# Rollback if needed (manual process)
psql -h localhost -U authservice -d authservice_db -f rollback-scripts/V2_1__rollback.sql
```

### Emergency Contacts

- **Primary On-Call**: +1-555-0200 (auth-primary@gogidix.com)
- **Secondary On-Call**: +1-555-0201 (auth-secondary@gogidix.com)
- **Security Team**: security-emergency@gogidix.com
- **DBA Team**: dba-emergency@gogidix.com
- **DevOps Team**: devops-emergency@gogidix.com

### Escalation Procedures

1. **Level 1**: Service team member (15 minutes)
2. **Level 2**: Senior engineer + Team lead (30 minutes)
3. **Level 3**: Architecture team + Security team (1 hour)
4. **Level 4**: Executive team + External consultants (2 hours)

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Monthly*
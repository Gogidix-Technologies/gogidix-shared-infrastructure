# API Gateway Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the API Gateway service in production environments.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Security Operations](#security-operations)
3. [Traffic Management](#traffic-management)
4. [Monitoring and Alerting](#monitoring-and-alerting)
5. [Performance Management](#performance-management)
6. [Backup and Recovery](#backup-and-recovery)
7. [Troubleshooting](#troubleshooting)
8. [Maintenance Procedures](#maintenance-procedures)
9. [Incident Response](#incident-response)

## Service Operations

### Service Management

#### Starting the API Gateway

**Production Environment:**
```bash
# Using systemd
sudo systemctl start api-gateway

# Using Docker
docker-compose -f docker-compose.prod.yml up -d api-gateway

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n api-gateway
```

#### Stopping the Service

```bash
# Graceful shutdown (allows existing requests to complete)
sudo systemctl stop api-gateway

# Force stop if needed
sudo systemctl kill api-gateway

# Kubernetes graceful shutdown
kubectl delete deployment api-gateway -n api-gateway --grace-period=60
```

#### Service Status Checks

```bash
# System status
sudo systemctl status api-gateway

# Health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/health/liveness

# Gateway-specific health
curl http://localhost:8080/actuator/gateway/routes
curl http://localhost:8080/actuator/gateway/circuit-breakers
```

### Configuration Management

#### Dynamic Route Management

```bash
# View current routes
curl http://localhost:8080/actuator/gateway/routes | jq '.'

# Add new route dynamically
curl -X POST http://localhost:8080/actuator/gateway/routes \
  -H "Content-Type: application/json" \
  -d '{
    "id": "new-service-route",
    "uri": "http://new-service:8080",
    "predicates": [{"name": "Path", "args": {"pattern": "/api/v1/new-service/**"}}],
    "filters": [{"name": "StripPrefix", "args": {"parts": "3"}}]
  }'

# Remove route
curl -X DELETE http://localhost:8080/actuator/gateway/routes/new-service-route

# Refresh routes from discovery
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

#### Circuit Breaker Management

```bash
# Check circuit breaker states
curl http://localhost:8080/actuator/circuit-breakers

# Force circuit breaker open
curl -X POST http://localhost:8080/actuator/circuit-breakers/social-commerce/force-open

# Reset circuit breaker
curl -X POST http://localhost:8080/actuator/circuit-breakers/social-commerce/reset

# Get circuit breaker metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls
```

#### Rate Limit Management

```bash
# Check rate limit status
curl http://localhost:8080/actuator/rate-limits

# Reset rate limit for specific key
curl -X POST http://localhost:8080/actuator/rate-limits/reset \
  -H "Content-Type: application/json" \
  -d '{"key": "user:12345"}'

# Update rate limit configuration
curl -X PUT http://localhost:8080/actuator/rate-limits/config \
  -H "Content-Type: application/json" \
  -d '{
    "global": {"requestsPerSecond": 1500, "burstCapacity": 3000},
    "perUser": {"requestsPerSecond": 150, "burstCapacity": 300}
  }'
```

## Security Operations

### Authentication and Authorization

#### JWT Token Management

```bash
# Validate JWT token
curl -X POST http://localhost:8080/actuator/auth/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'

# Revoke JWT token
curl -X POST http://localhost:8080/actuator/auth/revoke \
  -H "Content-Type: application/json" \
  -d '{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'

# Check revoked tokens
curl http://localhost:8080/actuator/auth/revoked-tokens
```

#### Security Monitoring

```bash
# Monitor authentication failures
tail -f /var/log/api-gateway/security.log | grep "AUTH_FAILURE"

# Track authorization violations
grep "AUTHORIZATION_DENIED" /var/log/api-gateway/audit.log | tail -20

# Monitor suspicious activities
curl http://localhost:8080/actuator/security/suspicious-activities

# Check blocked IPs
curl http://localhost:8080/actuator/security/blocked-ips
```

#### SSL/TLS Certificate Management

```bash
# Check certificate expiry
openssl x509 -in /etc/ssl/certs/api-gateway.crt -noout -dates

# Verify certificate chain
openssl verify -CAfile /etc/ssl/ca-bundle.crt /etc/ssl/certs/api-gateway.crt

# Test SSL connectivity
openssl s_client -connect api.gogidix.com:443 -servername api.gogidix.com

# Update certificate (zero-downtime)
kubectl create secret tls api-gateway-tls-new \
  --cert=new-api-gateway.crt \
  --key=new-api-gateway.key \
  -n api-gateway

kubectl patch deployment api-gateway \
  -p '{"spec":{"template":{"spec":{"volumes":[{"name":"ssl-certs","secret":{"secretName":"api-gateway-tls-new"}}]}}}}' \
  -n api-gateway
```

### CORS Management

```bash
# Check CORS configuration
curl http://localhost:8080/actuator/configprops | grep cors

# Test CORS preflight request
curl -X OPTIONS http://localhost:8080/api/v1/test \
  -H "Origin: https://app.gogidix.com" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization,Content-Type"

# Update CORS settings
curl -X PUT http://localhost:8080/actuator/cors/config \
  -H "Content-Type: application/json" \
  -d '{
    "allowedOrigins": ["https://app.gogidix.com", "https://admin.gogidix.com"],
    "allowedMethods": ["GET", "POST", "PUT", "DELETE"],
    "maxAge": 3600
  }'
```

## Traffic Management

### Load Balancing Operations

```bash
# Check backend service health
curl http://localhost:8080/actuator/gateway/services

# View load balancing statistics
curl http://localhost:8080/actuator/load-balancer/stats

# Remove unhealthy instance
curl -X DELETE http://localhost:8080/actuator/load-balancer/instances/social-commerce-1

# Add new instance
curl -X POST http://localhost:8080/actuator/load-balancer/instances \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "social-commerce",
    "host": "social-commerce-3",
    "port": 8081,
    "healthy": true
  }'
```

### Service Discovery Operations

```bash
# Check service registry
curl http://localhost:8080/actuator/service-registry

# Force service discovery refresh
curl -X POST http://localhost:8080/actuator/service-registry/refresh

# Check specific service instances
curl http://localhost:8080/actuator/service-registry/social-commerce

# Manually register service instance
curl -X POST http://localhost:8080/actuator/service-registry/register \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "temp-service",
    "host": "temp-service.internal",
    "port": 8080
  }'
```

### Request Routing

```bash
# Test request routing
curl -v http://localhost:8080/api/v1/social-commerce/products

# Trace request path
curl -H "X-Trace-Request: true" http://localhost:8080/api/v1/analytics/reports

# Check routing decisions
tail -f /var/log/api-gateway/routing.log

# View routing metrics
curl http://localhost:8080/actuator/metrics/gateway.requests | jq '.measurements'
```

## Monitoring and Alerting

### Key Performance Indicators (KPIs)

#### Gateway Health Metrics

| Metric | Normal Range | Alert Threshold | Critical Threshold |
|--------|--------------|-----------------|-------------------|
| Response Time | < 100ms | > 500ms | > 1000ms |
| Request Rate | < 1000 RPS | > 1500 RPS | > 2000 RPS |
| Error Rate | < 1% | > 5% | > 10% |
| Circuit Breaker Open | 0 | > 2 | > 5 |
| Memory Usage | < 75% | > 85% | > 95% |

#### Security Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Auth Failure Rate | < 1% | > 5% |
| Rate Limited Requests | < 0.1% | > 1% |
| Blocked Requests | < 0.01% | > 0.1% |
| Suspicious Activities | 0 | > 10/hour |

### Prometheus Metrics

#### Custom Gateway Metrics

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      service: api-gateway
      environment: ${SPRING_PROFILES_ACTIVE}
    distribution:
      percentiles-histogram:
        gateway.requests: true
        http.server.requests: true
```

#### Key Metrics to Monitor

```promql
# Request rate
rate(gateway_requests_total[5m])

# Response time percentiles
histogram_quantile(0.95, gateway_request_duration_seconds_bucket)

# Error rate
rate(gateway_requests_total{status=~"5.."}[5m]) / rate(gateway_requests_total[5m])

# Circuit breaker state
gateway_circuit_breaker_state{name="social-commerce"}

# Rate limiting events
rate(gateway_rate_limit_events_total[5m])

# Authentication failures
rate(gateway_auth_failures_total[5m])
```

### Grafana Dashboards

#### Gateway Overview Dashboard

```json
{
  "dashboard": {
    "title": "API Gateway - Overview",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(gateway_requests_total[5m])"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, gateway_request_duration_seconds_bucket)"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(gateway_requests_total{status=~\"5..\"}[5m])"
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
  - name: api-gateway
    rules:
      - alert: APIGatewayHighResponseTime
        expr: histogram_quantile(0.95, gateway_request_duration_seconds_bucket) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API Gateway high response time"
          description: "95th percentile response time is {{ $value }}s"

      - alert: APIGatewayHighErrorRate
        expr: rate(gateway_requests_total{status=~"5.."}[5m]) / rate(gateway_requests_total[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "API Gateway high error rate"
          description: "Error rate is {{ $value | humanizePercentage }}"

      - alert: APIGatewayCircuitBreakerOpen
        expr: gateway_circuit_breaker_state > 0
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Circuit breaker {{ $labels.name }} is open"

      - alert: APIGatewayAuthFailureSpike
        expr: rate(gateway_auth_failures_total[5m]) > 10
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "High authentication failure rate"
          description: "Auth failure rate: {{ $value }}/sec"
```

## Performance Management

### Performance Optimization

#### JVM Tuning for High Throughput

```bash
# Production JVM settings
export JAVA_OPTS="
  -Xmx4g
  -Xms4g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+OptimizeStringConcat
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/api-gateway/heapdumps/
  -Dserver.tomcat.max-threads=400
  -Dserver.tomcat.max-connections=8192
  -Dserver.tomcat.accept-count=1000
"
```

#### Connection Pool Tuning

```yaml
# HTTP client configuration
http-client:
  pool:
    max-connections: 1000
    max-connections-per-route: 100
    connection-timeout: 5s
    socket-timeout: 30s
    connection-request-timeout: 10s
    keep-alive-duration: 20s
    time-to-live: 60s
    validate-after-inactivity: 10s
```

#### Rate Limiting Optimization

```yaml
rate-limiting:
  storage: redis  # Use Redis for distributed rate limiting
  redis:
    host: redis-cluster
    port: 6379
    cluster: true
  algorithm: sliding-window-log  # More accurate than token bucket
  cleanup-interval: 60s
```

### Load Testing

#### Performance Test Scripts

```bash
# Apache Bench for basic load testing
ab -n 10000 -c 100 -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8080/api/v1/social-commerce/products

# Artillery.js for complex scenarios
artillery run --config load-test-config.yml load-test-scenarios.yml

# K6 for advanced load testing
k6 run --vus 100 --duration 5m gateway-load-test.js

# Custom script for authentication flow testing
./scripts/auth-load-test.sh 1000 60
```

#### Performance Monitoring During Tests

```bash
# Monitor key metrics during load test
watch -n 5 'curl -s http://localhost:8080/actuator/metrics/gateway.requests | jq ".measurements[0].value"'

# Monitor JVM metrics
watch -n 5 'curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq ".measurements[0].value"'

# Monitor circuit breaker status
watch -n 1 'curl -s http://localhost:8080/actuator/circuit-breakers | jq ".[].state"'
```

## Backup and Recovery

### Configuration Backup

```bash
#!/bin/bash
# backup-gateway-config.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/api-gateway"

# Backup gateway configuration
curl http://localhost:8080/actuator/configprops > $BACKUP_DIR/gateway_config_$DATE.json

# Backup route definitions
curl http://localhost:8080/actuator/gateway/routes > $BACKUP_DIR/routes_$DATE.json

# Backup security configuration
kubectl get secret jwt-secret -o yaml > $BACKUP_DIR/jwt_secret_$DATE.yaml
kubectl get configmap api-gateway-config -o yaml > $BACKUP_DIR/config_map_$DATE.yaml

# Backup SSL certificates
cp /etc/ssl/certs/api-gateway.* $BACKUP_DIR/

echo "Gateway configuration backup completed: $DATE"
```

#### Backup Schedule

```cron
# Crontab entries
# Configuration backup daily at 3 AM
0 3 * * * /opt/api-gateway/scripts/backup-gateway-config.sh >> /var/log/backup.log 2>&1

# SSL certificate backup weekly
0 4 * * 0 /opt/api-gateway/scripts/backup-ssl-certs.sh >> /var/log/backup.log 2>&1
```

### Recovery Procedures

#### Configuration Recovery

```bash
# Stop gateway service
kubectl scale deployment api-gateway --replicas=0 -n api-gateway

# Restore configuration
kubectl delete configmap api-gateway-config -n api-gateway
kubectl apply -f /backup/api-gateway/config_map_20240624_030000.yaml

# Restore secrets
kubectl delete secret jwt-secret -n api-gateway
kubectl apply -f /backup/api-gateway/jwt_secret_20240624_030000.yaml

# Restart gateway service
kubectl scale deployment api-gateway --replicas=3 -n api-gateway

# Verify recovery
kubectl get pods -l app=api-gateway -n api-gateway
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Common Issues and Solutions

#### 1. High Response Time

**Symptoms:**
- Slow API responses
- Timeout errors
- High latency metrics

**Investigation:**
```bash
# Check backend service health
curl http://localhost:8080/actuator/gateway/services

# Monitor connection pool usage
curl http://localhost:8080/actuator/metrics/http.client.requests

# Check circuit breaker states
curl http://localhost:8080/actuator/circuit-breakers
```

**Solutions:**
- Increase connection pool size
- Optimize backend service performance
- Enable request caching
- Configure appropriate timeouts

#### 2. Authentication Issues

**Symptoms:**
- 401 Unauthorized errors
- JWT validation failures

**Investigation:**
```bash
# Check JWT configuration
curl http://localhost:8080/actuator/configprops | grep jwt

# Validate sample token
curl -X POST http://localhost:8080/actuator/auth/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "'$JWT_TOKEN'"}'

# Check token expiration
echo $JWT_TOKEN | cut -d. -f2 | base64 -d | jq '.exp'
```

**Solutions:**
- Verify JWT secret configuration
- Check token expiration times
- Validate token issuer and audience
- Review authentication filter configuration

#### 3. Rate Limiting Issues

**Symptoms:**
- 429 Too Many Requests errors
- Legitimate users being throttled

**Investigation:**
```bash
# Check rate limit configuration
curl http://localhost:8080/actuator/rate-limits

# Monitor rate limiting metrics
curl http://localhost:8080/actuator/metrics/gateway.rate.limit

# Check Redis connection (if using distributed rate limiting)
redis-cli ping
```

**Solutions:**
- Adjust rate limit thresholds
- Implement user-specific rate limits
- Use distributed rate limiting
- Configure burst capacity appropriately

#### 4. Circuit Breaker Tripping

**Symptoms:**
- Services marked as unavailable
- 503 Service Unavailable errors

**Investigation:**
```bash
# Check circuit breaker states
curl http://localhost:8080/actuator/circuit-breakers

# View circuit breaker metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker

# Check backend service health
for service in social-commerce warehousing analytics; do
  echo "Checking $service..."
  curl http://$service:8080/actuator/health
done
```

**Solutions:**
- Adjust failure rate thresholds
- Increase sliding window size
- Fix underlying service issues
- Implement proper health checks

### Performance Debugging

```bash
# Generate thread dump
jcmd $(pgrep -f api-gateway) Thread.print > /tmp/gateway-threads.dump

# Generate heap dump
jcmd $(pgrep -f api-gateway) GC.run_finalization
jcmd $(pgrep -f api-gateway) VM.dump_heap /tmp/gateway-heap.hprof

# Monitor garbage collection
jstat -gc $(pgrep -f api-gateway) 5s

# Profile with async-profiler
java -jar async-profiler.jar -d 60 -f /tmp/gateway-profile.html $(pgrep -f api-gateway)
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-maintenance.sh

echo "=== API Gateway Daily Maintenance ===" > /tmp/gateway-maintenance.log

# Check service health
curl -f http://localhost:8080/actuator/health >> /tmp/gateway-maintenance.log

# Check route availability
curl http://localhost:8080/actuator/gateway/routes | jq 'length' >> /tmp/gateway-maintenance.log

# Check error rates
curl http://localhost:8080/actuator/metrics/gateway.requests | grep -i error >> /tmp/gateway-maintenance.log

# Check certificate expiry (if SSL enabled)
openssl x509 -in /etc/ssl/certs/api-gateway.crt -noout -dates >> /tmp/gateway-maintenance.log

# Check disk space
df -h >> /tmp/gateway-maintenance.log

# Send report
mail -s "API Gateway Daily Report" gateway-team@gogidix.com < /tmp/gateway-maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-maintenance.sh

# Update rate limiting statistics
curl -X POST http://localhost:8080/actuator/rate-limits/statistics/update

# Clean up old log files
find /var/log/api-gateway -name "*.log.*" -mtime +7 -delete

# Rotate JWT blacklist (remove expired tokens)
curl -X POST http://localhost:8080/actuator/auth/cleanup-revoked-tokens

# Generate security report
curl http://localhost:8080/actuator/security/report > /var/reports/gateway-security-$(date +%Y%m%d).json

# Update circuit breaker baselines
python scripts/update_circuit_breaker_baselines.py
```

### Update Procedures

#### Rolling Updates (Kubernetes)

```bash
# Update deployment with new image
kubectl set image deployment/api-gateway \
  api-gateway=api-gateway:v2.1.0 \
  -n api-gateway

# Monitor rollout
kubectl rollout status deployment/api-gateway -n api-gateway

# Verify health after update
kubectl get pods -l app=api-gateway -n api-gateway
curl http://api-gateway-service/actuator/health

# Rollback if necessary
kubectl rollout undo deployment/api-gateway -n api-gateway
```

#### Blue-Green Deployment

```bash
# Deploy green version
kubectl apply -f k8s/deployment-green.yaml

# Run smoke tests
./scripts/smoke-test.sh green

# Switch traffic to green
kubectl patch service api-gateway \
  -p '{"spec":{"selector":{"version":"green"}}}'

# Monitor for issues
watch kubectl get pods -l app=api-gateway,version=green

# Rollback if needed
kubectl patch service api-gateway \
  -p '{"spec":{"selector":{"version":"blue"}}}'
```

## Incident Response

### Incident Classification

| Severity | Response Time | Resolution Target | Escalation |
|----------|---------------|-------------------|------------|
| Critical | 5 minutes | 1 hour | Immediate |
| High | 15 minutes | 4 hours | 30 minutes |
| Medium | 1 hour | 24 hours | 2 hours |
| Low | 4 hours | 1 week | 24 hours |

### Common Incident Procedures

#### Gateway Down

1. **Immediate Response**
   ```bash
   # Check service status
   kubectl get pods -l app=api-gateway -n api-gateway
   
   # Check logs
   kubectl logs -l app=api-gateway -n api-gateway --tail=100
   
   # Restart if needed
   kubectl rollout restart deployment/api-gateway -n api-gateway
   ```

2. **Load Balancer Failover**
   ```bash
   # Switch to backup gateway
   aws elbv2 modify-target-group --target-group-arn $TG_ARN \
     --health-check-path /backup-health
   ```

#### Security Breach

1. **Immediate Actions**
   ```bash
   # Block suspicious IPs
   curl -X POST http://localhost:8080/actuator/security/block-ip \
     -d '{"ip": "192.168.1.100", "reason": "Suspicious activity"}'
   
   # Revoke all JWT tokens (if necessary)
   curl -X POST http://localhost:8080/actuator/auth/revoke-all-tokens
   
   # Enable emergency mode
   curl -X POST http://localhost:8080/actuator/security/emergency-mode
   ```

2. **Investigation**
   ```bash
   # Collect security logs
   grep "SECURITY_VIOLATION" /var/log/api-gateway/security.log > incident-logs.txt
   
   # Generate security report
   curl http://localhost:8080/actuator/security/incident-report > security-incident.json
   ```

### Emergency Contacts

- **Primary On-Call**: +1-555-0100 (gateway-primary@gogidix.com)
- **Secondary On-Call**: +1-555-0101 (gateway-secondary@gogidix.com)
- **Security Team**: security-emergency@gogidix.com
- **DevOps Team**: devops-emergency@gogidix.com

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Monthly*
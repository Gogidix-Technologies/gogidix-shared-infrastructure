# Service Registry Operations

## Overview

This document provides comprehensive operational procedures, monitoring guidelines, and maintenance instructions for the Service Registry (Eureka) in production environments. The Service Registry is critical infrastructure for microservices discovery and communication.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Monitoring and Alerting](#monitoring-and-alerting)
3. [Performance Management](#performance-management)
4. [Service Discovery Management](#service-discovery-management)
5. [High Availability Operations](#high-availability-operations)
6. [Security Operations](#security-operations)
7. [Troubleshooting](#troubleshooting)
8. [Maintenance Procedures](#maintenance-procedures)
9. [Disaster Recovery](#disaster-recovery)

## Service Operations

### Service Management

#### Starting the Service

**Production Environment:**
```bash
# Using systemd
sudo systemctl start service-registry

# Using Docker
docker-compose -f docker-compose.prod.yml up -d service-registry

# Using Kubernetes
kubectl apply -f k8s/deployment.yaml -n shared-infrastructure
```

#### Stopping the Service

```bash
# Graceful shutdown (allows service deregistration)
sudo systemctl stop service-registry

# Force stop if needed
sudo systemctl kill service-registry

# Kubernetes
kubectl delete deployment service-registry -n shared-infrastructure
```

#### Service Status Checks

```bash
# System status
sudo systemctl status service-registry

# Health endpoints
curl http://localhost:8761/actuator/health
curl http://localhost:8761/actuator/health/readiness
curl http://localhost:8761/actuator/health/liveness

# Eureka dashboard
curl http://localhost:8761/

# Registry information
curl http://localhost:8761/eureka/apps
```

### Configuration Management

#### Environment-Specific Configurations

**Production (`application-prod.yml`):**
```yaml
spring:
  profiles: prod
  
eureka:
  instance:
    hostname: ${EUREKA_HOSTNAME:service-registry}
    prefer-ip-address: false
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30
  
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 15000
    renewal-percent-threshold: 0.85
    max-idle-registry-connections: 20
    
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://eureka-replica:8761/eureka/

server:
  port: 8761
  tomcat:
    max-threads: 200
    accept-count: 100

logging:
  level:
    root: INFO
    com.netflix.eureka: INFO
    com.exalt.registry: INFO
  file:
    name: /var/log/service-registry/eureka.log
    max-file-size: 100MB
    max-history: 30
```

#### Runtime Configuration Updates

```bash
# Check registry status
curl http://localhost:8761/eureka/status

# Enable/disable self-preservation
curl -X POST http://localhost:8761/eureka/self-preservation/enable
curl -X POST http://localhost:8761/eureka/self-preservation/disable

# Get registry information
curl http://localhost:8761/eureka/apps -H "Accept: application/json"
```

## Monitoring and Alerting

### Key Performance Indicators (KPIs)

#### Registry Health Metrics

| Metric | Normal Range | Alert Threshold | Critical Threshold |
|--------|--------------|-----------------|-------------------|
| Response Time | < 100ms | > 500ms | > 1s |
| CPU Usage | < 60% | > 80% | > 90% |
| Memory Usage | < 70% | > 85% | > 95% |
| Registered Services | 10-50 | > 100 | > 200 |
| Heartbeat Success Rate | > 99% | < 95% | < 90% |

#### Service Discovery Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Service Registration Time | < 30s | > 60s |
| Service Deregistration Time | < 30s | > 60s |
| Registry Sync Latency | < 10s | > 30s |
| Failed Heartbeats | < 1% | > 5% |

### Custom Metrics

#### Prometheus Metrics Configuration

```java
// metrics/EurekaMetrics.java
@Component
public class EurekaMetrics {
    
    private final Gauge registeredServices = Gauge.builder("eureka_registered_services")
            .description("Number of registered services")
            .register(Metrics.globalRegistry);
    
    private final Counter heartbeats = Counter.builder("eureka_heartbeats_total")
            .description("Total heartbeat requests")
            .labelNames("service_name", "status")
            .register(Metrics.globalRegistry);
    
    private final Timer registrationTime = Timer.builder("eureka_registration_duration")
            .description("Service registration time")
            .register(Metrics.globalRegistry);
    
    private final Gauge selfPreservationMode = Gauge.builder("eureka_self_preservation_enabled")
            .description("Self-preservation mode status")
            .register(Metrics.globalRegistry);
    
    private final Counter registryRequests = Counter.builder("eureka_registry_requests_total")
            .description("Registry access requests")
            .labelNames("endpoint", "status")
            .register(Metrics.globalRegistry);
}
```

#### Key Metrics to Monitor

```promql
# Service registration rate
rate(eureka_registrations_total[5m])

# Heartbeat success rate
rate(eureka_heartbeats_total{status="success"}[5m]) / rate(eureka_heartbeats_total[5m]) * 100

# Registry response time
histogram_quantile(0.95, eureka_request_duration_seconds_bucket)

# Number of registered services
eureka_registered_services

# Self-preservation mode status
eureka_self_preservation_enabled
```

### Alert Rules

#### Prometheus Alert Configuration

```yaml
# alert-rules.yml
groups:
  - name: service-registry
    rules:
      - alert: EurekaServiceRegistryDown
        expr: up{job="service-registry"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Eureka service registry is down"
          description: "Service registry has been down for more than 1 minute"

      - alert: EurekaHighHeartbeatFailures
        expr: rate(eureka_heartbeats_total{status="failure"}[5m]) / rate(eureka_heartbeats_total[5m]) * 100 > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High heartbeat failure rate"
          description: "Heartbeat failure rate is {{ $value }}%"

      - alert: EurekaSelfPreservationMode
        expr: eureka_self_preservation_enabled == 1
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Eureka in self-preservation mode"
          description: "Eureka has been in self-preservation mode for more than 10 minutes"
```

## Service Discovery Management

### Service Registration Operations

```bash
# List all registered services
curl http://localhost:8761/eureka/apps -H "Accept: application/json" | jq .

# Get specific service information
curl http://localhost:8761/eureka/apps/AUTH-SERVICE -H "Accept: application/json"

# Check service instance health
curl http://localhost:8761/eureka/apps/AUTH-SERVICE/auth-service-001

# Force service deregistration
curl -X DELETE http://localhost:8761/eureka/apps/AUTH-SERVICE/auth-service-001
```

### Registry Maintenance

```bash
# Clear registry cache
curl -X DELETE http://localhost:8761/eureka/apps/__clear_cache__

# Refresh registry
curl -X POST http://localhost:8761/eureka/refresh

# Get registry metadata
curl http://localhost:8761/eureka/lastn
```

### Service Health Monitoring

```bash
# Monitor service status
#!/bin/bash
# monitor-services.sh

services=$(curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" | \
  jq -r '.applications.application[].name')

for service in $services; do
  status=$(curl -s http://localhost:8761/eureka/apps/$service -H "Accept: application/json" | \
    jq -r '.application.instance[0].status')
  echo "$service: $status"
done
```

## High Availability Operations

### Multi-Instance Setup

#### Peer Discovery Configuration

```yaml
# eureka-node1.yml
eureka:
  instance:
    hostname: eureka-node1
  client:
    service-url:
      defaultZone: http://eureka-node2:8761/eureka/,http://eureka-node3:8761/eureka/

# eureka-node2.yml
eureka:
  instance:
    hostname: eureka-node2
  client:
    service-url:
      defaultZone: http://eureka-node1:8761/eureka/,http://eureka-node3:8761/eureka/
```

#### Cluster Health Monitoring

```bash
# Check cluster status
curl http://eureka-node1:8761/eureka/status
curl http://eureka-node2:8761/eureka/status
curl http://eureka-node3:8761/eureka/status

# Monitor peer synchronization
curl http://localhost:8761/eureka/peerreplication
```

### Load Balancing

```bash
# Configure load balancer health checks
# HAProxy configuration
backend eureka_servers
    option httpchk GET /actuator/health
    server eureka1 eureka-node1:8761 check
    server eureka2 eureka-node2:8761 check
    server eureka3 eureka-node3:8761 check
```

## Security Operations

### Access Control

```bash
# Monitor registry access
grep "REGISTRY_ACCESS" /var/log/service-registry/audit.log

# Check authentication status
curl -u admin:password http://localhost:8761/eureka/apps

# Monitor unauthorized access attempts
grep "UNAUTHORIZED" /var/log/service-registry/security.log
```

### Security Configuration

```yaml
# Security configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  security:
    enabled: true
    
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    auth:
      enabled: true
      username: ${EUREKA_USERNAME:admin}
      password: ${EUREKA_PASSWORD:secure_password}
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Service Registration Failures

**Symptoms:**
- Services not appearing in registry
- Registration timeouts
- Heartbeat failures

**Investigation:**
```bash
# Check service logs
grep "REGISTRATION" /var/log/service-registry/eureka.log

# Verify network connectivity
telnet service-host 8761

# Check service configuration
curl http://service-host:8080/actuator/info
```

**Solutions:**
- Verify network connectivity
- Check service configuration
- Restart failing services
- Increase timeout values

#### 2. Self-Preservation Mode Issues

**Symptoms:**
- Services not being evicted
- Stale service instances
- Registry showing offline services

**Investigation:**
```bash
# Check self-preservation status
curl http://localhost:8761/eureka/status

# Monitor heartbeat rates
grep "heartbeat" /var/log/service-registry/eureka.log
```

**Solutions:**
- Disable self-preservation if appropriate
- Investigate network issues
- Check service health endpoints
- Adjust threshold settings

#### 3. Memory and Performance Issues

**Symptoms:**
- High memory usage
- Slow response times
- Registry timeouts

**Investigation:**
```bash
# Check memory usage
jstat -gc <eureka-pid>

# Monitor registry size
curl http://localhost:8761/eureka/apps | wc -l

# Check GC logs
grep "GC" /var/log/service-registry/gc.log
```

**Solutions:**
- Increase heap size
- Optimize GC settings
- Clean up stale registrations
- Scale horizontally

### Log Analysis

#### Important Log Patterns

```bash
# Registration events
grep -E "REGISTER|DEREGISTER" /var/log/service-registry/eureka.log

# Heartbeat issues
grep -E "HEARTBEAT|RENEWAL" /var/log/service-registry/eureka.log

# Self-preservation events
grep "SELF_PRESERVATION" /var/log/service-registry/eureka.log

# Network issues
grep -E "TIMEOUT|CONNECTION" /var/log/service-registry/eureka.log
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks

```bash
#!/bin/bash
# daily-maintenance.sh

echo "=== Service Registry Daily Report ===" > /tmp/maintenance.log

# Check registry health
curl -f http://localhost:8761/actuator/health >> /tmp/maintenance.log

# Count registered services
service_count=$(curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" | \
  jq '.applications.application | length')
echo "Registered services: $service_count" >> /tmp/maintenance.log

# Check self-preservation status
preservation_status=$(curl -s http://localhost:8761/eureka/status | \
  grep -o "SELF_PRESERVATION_MODE_[A-Z]*")
echo "Self-preservation: $preservation_status" >> /tmp/maintenance.log

# Send report
mail -s "Service Registry Daily Report" registry-team@exalt.com < /tmp/maintenance.log
```

#### Weekly Tasks

```bash
#!/bin/bash
# weekly-maintenance.sh

# Clean old logs
find /var/log/service-registry -name "*.log.*" -mtime +7 -delete

# Generate registry health report
curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" > \
  /tmp/registry-report.json

# Check for stale instances
./scripts/check-stale-instances.sh

# Monitor registry performance
./scripts/performance-check.sh
```

### Update Procedures

#### Rolling Updates (Kubernetes)

```bash
# Update deployment
kubectl set image deployment/service-registry \
  service-registry=service-registry:v1.6.0 \
  -n shared-infrastructure

# Monitor rollout
kubectl rollout status deployment/service-registry -n shared-infrastructure

# Verify registry health
kubectl exec -it deployment/service-registry -n shared-infrastructure -- \
  curl http://localhost:8761/actuator/health
```

## Disaster Recovery

### Recovery Time Objectives (RTO)

| Component | RTO Target | Recovery Procedure |
|-----------|------------|-------------------|
| Single Instance | 2 minutes | Health check restart |
| Full Registry | 5 minutes | Multi-instance failover |
| Data Recovery | 10 minutes | Registry rebuild |

### Recovery Point Objectives (RPO)

| Data Type | RPO Target | Backup Frequency |
|-----------|------------|------------------|
| Service Registry | 0 minutes | Real-time replication |
| Configuration | 5 minutes | Git versioning |
| Logs | 15 minutes | Log streaming |

### Emergency Procedures

1. **Registry Failure**: Automatic failover to replica instances
2. **Split Brain**: Manual intervention to resolve conflicts
3. **Data Corruption**: Registry rebuild from service registrations
4. **Network Partition**: Regional registry isolation

### Emergency Contacts

- **Primary On-Call**: +1-555-0133 (registry-team-primary@exalt.com)
- **Secondary On-Call**: +1-555-0134 (registry-team-secondary@exalt.com)
- **Platform Team**: platform-emergency@exalt.com
- **DevOps Team**: devops-emergency@exalt.com

## Compliance and Auditing

### Registry Audit Configuration

```yaml
# Audit configuration
audit:
  enabled: true
  events:
    - SERVICE_REGISTRATION
    - SERVICE_DEREGISTRATION
    - HEARTBEAT_RECEIVED
    - ADMIN_ACCESS
    - CONFIGURATION_CHANGE
  retention-days: 90
  format: json
```

### Compliance Monitoring

```bash
# Generate compliance report
curl -X GET http://localhost:8761/admin/audit/report \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Monitor service compliance
./scripts/service-compliance-check.sh
```

## Support and Escalation

### Support Levels

1. **L1 Support**: Basic health checks, service restart
2. **L2 Support**: Configuration changes, registry management
3. **L3 Support**: Architecture changes, cluster operations

### Escalation Matrix

| Issue Severity | Initial Response | Resolution Target | Escalation Path |
|----------------|------------------|-------------------|-----------------|
| Critical | 2 minutes | 15 minutes | L1 → L2 → L3 → Management |
| High | 5 minutes | 1 hour | L1 → L2 → L3 |
| Medium | 30 minutes | 4 hours | L1 → L2 |
| Low | 2 hours | 24 hours | L1 |

---

*Last Updated: 2024-06-25*
*Document Version: 1.0*
*Review Schedule: Monthly*
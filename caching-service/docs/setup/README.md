# Caching Service Setup Guide

## Overview

This comprehensive setup guide covers all aspects of installing, configuring, and deploying the Caching Service in various environments. The guide includes prerequisites, step-by-step installation procedures, configuration options, and environment-specific setup instructions.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Development Environment Setup](#development-environment-setup)
3. [Production Environment Setup](#production-environment-setup)
4. [Configuration Management](#configuration-management)
5. [Redis Cluster Setup](#redis-cluster-setup)
6. [Security Configuration](#security-configuration)
7. [Monitoring Setup](#monitoring-setup)
8. [Testing and Validation](#testing-and-validation)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

#### Hardware Requirements

| Environment | CPU | Memory | Storage | Network |
|-------------|-----|--------|---------|---------|
| Development | 2 cores | 4 GB | 20 GB | 100 Mbps |
| Staging | 4 cores | 8 GB | 50 GB | 1 Gbps |
| Production | 8+ cores | 16+ GB | 100+ GB | 10 Gbps |

#### Software Requirements

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 17+ | OpenJDK or Oracle JDK |
| Maven | 3.8+ | Build tool |
| Docker | 20.10+ | Container runtime |
| Docker Compose | 2.0+ | Multi-container orchestration |
| Kubernetes | 1.24+ | Production orchestration |
| Redis | 6.2+ | Cache backend |
| PostgreSQL | 14+ | Configuration storage |

### Network Requirements

```yaml
network_ports:
  application:
    - port: 8403
      protocol: tcp
      description: "HTTP API endpoints"
    - port: 8404
      protocol: tcp
      description: "Management endpoints"
  
  redis:
    - port: 6379
      protocol: tcp
      description: "Redis standalone"
    - port: 16379
      protocol: tcp
      description: "Redis Cluster bus"
  
  monitoring:
    - port: 9090
      protocol: tcp
      description: "Prometheus metrics"
    - port: 8080
      protocol: tcp
      description: "Health checks"
```

### Security Requirements

```yaml
certificates:
  - type: "TLS Certificate"
    purpose: "HTTPS endpoints"
    validity: "1 year minimum"
  
  - type: "Client Certificate"
    purpose: "mTLS authentication"
    validity: "6 months"

access_control:
  - service_accounts
  - rbac_policies
  - network_policies
  - pod_security_policies
```

## Development Environment Setup

### Local Development Setup

#### Step 1: Environment Preparation

```bash
# Create development directory
mkdir -p ~/dev/gogidix/caching-service
cd ~/dev/gogidix/caching-service

# Clone repository
git clone https://github.com/gogidix/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/caching-service
```

#### Step 2: Java and Maven Setup

```bash
# Verify Java installation
java -version
# Expected: openjdk version "17.0.7"

# Verify Maven installation
mvn -version
# Expected: Apache Maven 3.8.6

# Install Java if needed (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-17-jdk maven

# Install Java if needed (macOS)
brew install openjdk@17 maven

# Set JAVA_HOME (add to ~/.bashrc or ~/.zshrc)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

#### Step 3: Docker Environment Setup

```bash
# Create docker-compose for development
cat > docker-compose.dev.yml << 'EOF'
version: '3.8'

services:
  redis-cluster:
    image: redis/redis-stack:7.0.6-RC8
    container_name: caching-redis-cluster
    ports:
      - "6379:6379"
      - "8001:8001"
    environment:
      REDIS_ARGS: "--cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000"
    volumes:
      - redis_data:/data
      - ./config/redis/redis.conf:/usr/local/etc/redis/redis.conf
    command: redis-server /usr/local/etc/redis/redis.conf

  redis-insight:
    image: redislabs/redisinsight:latest
    container_name: redis-insight
    ports:
      - "8001:8001"
    depends_on:
      - redis-cluster

  postgres:
    image: postgres:15
    container_name: caching-postgres
    environment:
      POSTGRES_DB: caching_service
      POSTGRES_USER: caching_user
      POSTGRES_PASSWORD: caching_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql

  prometheus:
    image: prom/prometheus:latest
    container_name: caching-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./config/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus

  grafana:
    image: grafana/grafana:latest
    container_name: caching-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./config/grafana/dashboards:/var/lib/grafana/dashboards

volumes:
  redis_data:
  postgres_data:
  prometheus_data:
  grafana_data:
EOF

# Start development environment
docker-compose -f docker-compose.dev.yml up -d
```

#### Step 4: Application Configuration

```bash
# Create development configuration
mkdir -p src/main/resources/config

cat > src/main/resources/application-dev.yml << 'EOF'
server:
  port: 8403

spring:
  application:
    name: caching-service
  profiles:
    active: dev
  
  datasource:
    url: jdbc:postgresql://localhost:5432/caching_service
    username: caching_user
    password: caching_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# Cache Configuration
cache:
  redis:
    cluster:
      nodes:
        - localhost:6379
      password: ${REDIS_PASSWORD:}
      connection-timeout: 2000
      socket-timeout: 5000
      max-attempts: 3
    
    pool:
      max-total: 50
      max-idle: 20
      min-idle: 5
      test-on-borrow: true
      test-on-return: true
  
  regions:
    products:
      default-ttl: PT1H
      max-size: 10000
      eviction-policy: LRU
    
    sessions:
      default-ttl: PT30M
      max-size: 5000
      eviction-policy: LRU
    
    api-responses:
      default-ttl: PT5M
      max-size: 1000
      eviction-policy: TTL

# Monitoring Configuration
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

# Logging Configuration
logging:
  level:
    com.gogidix.caching: DEBUG
    org.springframework.cache: DEBUG
    redis: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
EOF
```

#### Step 5: Build and Run

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Start application
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Verify application is running
curl http://localhost:8403/actuator/health
```

### IDE Setup

#### IntelliJ IDEA Configuration

```xml
<!-- .idea/workspace.xml snippet -->
<component name="RunManager">
  <configuration name="CachingServiceApplication" type="SpringBootApplicationConfigurationType">
    <module name="caching-service" />
    <option name="SPRING_BOOT_MAIN_CLASS" value="com.gogidix.caching.CachingServiceApplication" />
    <option name="ACTIVE_PROFILES" value="dev" />
    <option name="VM_PARAMETERS" value="-Xmx2g -Xms512m -XX:+UseG1GC" />
    <method v="2">
      <option name="Make" enabled="true" />
    </method>
  </configuration>
</component>
```

#### VS Code Configuration

```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Launch CachingServiceApplication",
      "request": "launch",
      "mainClass": "com.gogidix.caching.CachingServiceApplication",
      "projectName": "caching-service",
      "args": "--spring.profiles.active=dev",
      "vmArgs": "-Xmx2g -Xms512m -XX:+UseG1GC"
    }
  ]
}

// .vscode/settings.json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.problem.application-properties.enabled": true
}
```

## Production Environment Setup

### Infrastructure Preparation

#### Kubernetes Cluster Setup

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: caching-system
  labels:
    name: caching-system
    environment: production

---
# k8s/resource-quota.yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: caching-system-quota
  namespace: caching-system
spec:
  hard:
    requests.cpu: "20"
    requests.memory: 40Gi
    limits.cpu: "40"
    limits.memory: 80Gi
    persistentvolumeclaims: "10"
```

#### Redis Cluster Setup

```yaml
# k8s/redis-cluster.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis-cluster
  namespace: caching-system
spec:
  serviceName: redis-cluster
  replicas: 6
  selector:
    matchLabels:
      app: redis-cluster
  template:
    metadata:
      labels:
        app: redis-cluster
    spec:
      containers:
      - name: redis
        image: redis:7.0.11
        ports:
        - containerPort: 6379
          name: client
        - containerPort: 16379
          name: gossip
        command:
        - redis-server
        - /etc/redis/redis.conf
        - --cluster-enabled
        - "yes"
        - --cluster-config-file
        - /var/lib/redis/nodes.conf
        - --cluster-node-timeout
        - "5000"
        - --appendonly
        - "yes"
        - --protected-mode
        - "no"
        env:
        - name: POD_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        volumeMounts:
        - name: redis-config
          mountPath: /etc/redis
        - name: redis-data
          mountPath: /var/lib/redis
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
          limits:
            memory: "4Gi"
            cpu: "2"
      volumes:
      - name: redis-config
        configMap:
          name: redis-config
  volumeClaimTemplates:
  - metadata:
      name: redis-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 50Gi

---
apiVersion: v1
kind: Service
metadata:
  name: redis-cluster
  namespace: caching-system
spec:
  clusterIP: None
  selector:
    app: redis-cluster
  ports:
  - port: 6379
    targetPort: 6379
    name: client
  - port: 16379
    targetPort: 16379
    name: gossip
```

#### Application Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: caching-service
  namespace: caching-system
  labels:
    app: caching-service
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: caching-service
  template:
    metadata:
      labels:
        app: caching-service
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8404"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: caching-service
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: caching-service
        image: gogidix/caching-service:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8403
          name: http
        - containerPort: 8404
          name: management
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: REDIS_CLUSTER_NODES
          value: "redis-cluster-0.redis-cluster:6379,redis-cluster-1.redis-cluster:6379,redis-cluster-2.redis-cluster:6379"
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: redis-secret
              key: password
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: url
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: password
        volumeMounts:
        - name: config
          mountPath: /etc/config
          readOnly: true
        - name: cache-storage
          mountPath: /var/cache
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8404
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8404
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "4Gi"
            cpu: "2"
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
      volumes:
      - name: config
        configMap:
          name: caching-service-config
      - name: cache-storage
        emptyDir:
          sizeLimit: 2Gi
      nodeSelector:
        node-type: cache-optimized
      tolerations:
      - key: "cache-workload"
        operator: "Equal"
        value: "true"
        effect: "NoSchedule"

---
apiVersion: v1
kind: Service
metadata:
  name: caching-service
  namespace: caching-system
  labels:
    app: caching-service
spec:
  selector:
    app: caching-service
  ports:
  - name: http
    port: 80
    targetPort: 8403
  - name: management
    port: 8404
    targetPort: 8404
  type: ClusterIP
```

## Configuration Management

### Application Configuration

```yaml
# config/application-production.yml
server:
  port: 8403
  servlet:
    context-path: /
  compression:
    enabled: true
    mime-types: application/json,text/plain,text/css,application/javascript
  http2:
    enabled: true

spring:
  application:
    name: caching-service
  
  profiles:
    active: production
  
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      idle-timeout: 300000
      max-lifetime: 600000
      connection-timeout: 30000
      validation-timeout: 5000
      leak-detection-threshold: 60000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        jdbc:
          batch_size: 25
        order_inserts: true
        order_updates: true
        connection:
          provider_disables_autocommit: true

# Cache Configuration
cache:
  redis:
    cluster:
      nodes: ${REDIS_CLUSTER_NODES}
      password: ${REDIS_PASSWORD}
      connection-timeout: 5000
      socket-timeout: 10000
      max-attempts: 5
      max-redirections: 5
    
    pool:
      max-total: 200
      max-idle: 50
      min-idle: 10
      test-on-borrow: true
      test-on-return: true
      test-while-idle: true
      time-between-eviction-runs: 30000
      min-evictable-idle-time: 60000
    
    serialization:
      type: json
      compression: gzip
      encryption: aes-256-gcm
    
    ssl:
      enabled: true
      keystore: ${SSL_KEYSTORE_PATH}
      keystore-password: ${SSL_KEYSTORE_PASSWORD}
      truststore: ${SSL_TRUSTSTORE_PATH}
      truststore-password: ${SSL_TRUSTSTORE_PASSWORD}
  
  regions:
    products:
      default-ttl: PT2H
      max-size: 100000
      eviction-policy: LRU
      enable-compression: true
      enable-encryption: true
    
    sessions:
      default-ttl: PT1H
      max-size: 50000
      eviction-policy: LRU
      enable-compression: false
      enable-encryption: true
    
    api-responses:
      default-ttl: PT10M
      max-size: 10000
      eviction-policy: TTL
      enable-compression: true
      enable-encryption: false
  
  strategies:
    default: cache-aside
    products: refresh-ahead
    sessions: write-through
    api-responses: cache-aside
  
  warming:
    enabled: true
    strategies:
      - region: products
        type: popular-items
        schedule: "0 2 * * *"
        concurrent-threads: 10
      - region: categories
        type: all-items
        schedule: "0 3 * * *"
        concurrent-threads: 5

# Security Configuration
security:
  authentication:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400
    
    mtls:
      enabled: true
      client-cert-header: X-Client-Cert
      trusted-ca: ${TRUSTED_CA_CERT}
  
  authorization:
    rbac:
      enabled: true
      roles:
        - name: cache-admin
          permissions: ["cache:*"]
        - name: cache-user
          permissions: ["cache:read", "cache:write"]
        - name: cache-readonly
          permissions: ["cache:read"]

# Monitoring Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
    
    metrics:
      enabled: true
  
  metrics:
    export:
      prometheus:
        enabled: true
        step: 10s
      cloudwatch:
        enabled: ${CLOUDWATCH_ENABLED:false}
        namespace: CachingService
    
    tags:
      service: caching-service
      environment: ${ENVIRONMENT:production}
      region: ${AWS_REGION:us-east-1}

# Logging Configuration
logging:
  level:
    root: INFO
    com.gogidix.caching: INFO
    org.springframework.cache: WARN
    redis: WARN
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  
  file:
    name: /var/log/caching-service/application.log
    max-size: 100MB
    max-history: 30
  
  logback:
    rollingpolicy:
      max-file-size: 100MB
      total-size-cap: 10GB
```

### Redis Configuration

```redis
# config/redis/redis.conf
# Basic Configuration
bind 0.0.0.0
port 6379
protected-mode no
tcp-backlog 511
tcp-keepalive 300
timeout 0

# Cluster Configuration
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 15000
cluster-announce-ip ${POD_IP}
cluster-announce-port 6379
cluster-announce-bus-port 16379

# Memory Management
maxmemory 4gb
maxmemory-policy allkeys-lru
maxmemory-samples 5

# Persistence
save 900 1
save 300 10
save 60 10000
dbfilename dump.rdb
dir /var/lib/redis
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

# Security
requirepass ${REDIS_PASSWORD}
masterauth ${REDIS_PASSWORD}

# Performance Tuning
tcp-nodelay yes
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
list-compress-depth 0
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
hll-sparse-max-bytes 3000
stream-node-max-bytes 4096
stream-node-max-entries 100

# Logging
loglevel notice
logfile /var/log/redis/redis.log
syslog-enabled no

# Client Output Buffer Limits
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit replica 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60

# TLS Configuration (if enabled)
# tls-port 6380
# tls-cert-file /etc/ssl/redis/server.crt
# tls-key-file /etc/ssl/redis/server.key
# tls-ca-cert-file /etc/ssl/redis/ca.crt
```

## Security Configuration

### TLS/SSL Setup

```bash
# Generate SSL certificates for Redis
openssl genrsa -out redis-server.key 2048
openssl req -new -key redis-server.key -out redis-server.csr
openssl x509 -req -days 365 -in redis-server.csr -signkey redis-server.key -out redis-server.crt

# Create Kubernetes secrets
kubectl create secret tls redis-tls-secret \
  --cert=redis-server.crt \
  --key=redis-server.key \
  -n caching-system

kubectl create secret generic redis-ca-secret \
  --from-file=ca.crt=ca.crt \
  -n caching-system
```

### RBAC Configuration

```yaml
# k8s/rbac.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: caching-service
  namespace: caching-system

---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: caching-system
  name: caching-service-role
rules:
- apiGroups: [""]
  resources: ["pods", "services", "endpoints"]
  verbs: ["get", "list", "watch"]
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list", "watch"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: caching-service-binding
  namespace: caching-system
subjects:
- kind: ServiceAccount
  name: caching-service
  namespace: caching-system
roleRef:
  kind: Role
  name: caching-service-role
  apiGroup: rbac.authorization.k8s.io
```

### Network Policies

```yaml
# k8s/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: caching-service-netpol
  namespace: caching-system
spec:
  podSelector:
    matchLabels:
      app: caching-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: api-gateway
    - namespaceSelector:
        matchLabels:
          name: microservices
    ports:
    - protocol: TCP
      port: 8403
  - from:
    - namespaceSelector:
        matchLabels:
          name: monitoring
    ports:
    - protocol: TCP
      port: 8404
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: redis-cluster
    ports:
    - protocol: TCP
      port: 6379
  - to: []
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
```

## Monitoring Setup

### Prometheus Configuration

```yaml
# config/prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "caching-service-rules.yml"

scrape_configs:
  - job_name: 'caching-service'
    static_configs:
      - targets: ['caching-service:8404']
    metrics_path: /actuator/prometheus
    scrape_interval: 10s
    
  - job_name: 'redis-cluster'
    static_configs:
      - targets: 
        - 'redis-cluster-0:6379'
        - 'redis-cluster-1:6379'
        - 'redis-cluster-2:6379'
    metrics_path: /metrics

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### Grafana Dashboards

```json
{
  "dashboard": {
    "id": null,
    "title": "Caching Service Dashboard",
    "tags": ["caching", "redis", "performance"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "Cache Hit Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(cache_requests_total{result=\"hit\"}[5m]) / rate(cache_requests_total[5m])",
            "legendFormat": "Hit Rate"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percentunit",
            "min": 0,
            "max": 1
          }
        }
      },
      {
        "id": 2,
        "title": "Cache Operations per Second",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(cache_operations_total[5m])",
            "legendFormat": "{{operation}}"
          }
        ]
      },
      {
        "id": 3,
        "title": "Redis Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "redis_memory_used_bytes / redis_memory_max_bytes",
            "legendFormat": "Memory Usage %"
          }
        ]
      },
      {
        "id": 4,
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])",
            "legendFormat": "95th Percentile"
          },
          {
            "expr": "histogram_quantile(0.50, rate(http_request_duration_seconds_bucket[5m])",
            "legendFormat": "50th Percentile"
          }
        ]
      }
    ]
  }
}
```

## Testing and Validation

### Health Check Validation

```bash
#!/bin/bash
# scripts/validate-deployment.sh

echo "=== Caching Service Deployment Validation ==="

# Check service health
echo "Checking service health..."
HEALTH_RESPONSE=$(curl -s http://caching-service:8403/actuator/health)
if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
    echo "✅ Service health check passed"
else
    echo "❌ Service health check failed"
    echo "$HEALTH_RESPONSE"
    exit 1
fi

# Check Redis connectivity
echo "Checking Redis connectivity..."
REDIS_HEALTH=$(curl -s http://caching-service:8403/actuator/health/redis)
if echo "$REDIS_HEALTH" | grep -q '"status":"UP"'; then
    echo "✅ Redis connectivity check passed"
else
    echo "❌ Redis connectivity check failed"
    echo "$REDIS_HEALTH"
    exit 1
fi

# Test cache operations
echo "Testing cache operations..."
CACHE_TEST=$(curl -s -X POST http://caching-service:8403/api/v1/cache/test \
  -H "Content-Type: application/json" \
  -d '{"key": "test-key", "value": "test-value"}')

if echo "$CACHE_TEST" | grep -q '"success":true'; then
    echo "✅ Cache operations test passed"
else
    echo "❌ Cache operations test failed"
    echo "$CACHE_TEST"
    exit 1
fi

# Check metrics endpoint
echo "Checking metrics endpoint..."
METRICS_RESPONSE=$(curl -s http://caching-service:8404/actuator/prometheus)
if echo "$METRICS_RESPONSE" | grep -q 'cache_requests_total'; then
    echo "✅ Metrics endpoint check passed"
else
    echo "❌ Metrics endpoint check failed"
    exit 1
fi

echo "🎉 All validation checks passed!"
```

### Load Testing

```javascript
// tests/load/cache-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 500 },
    { duration: '2m', target: 1000 },
    { duration: '5m', target: 1000 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    errors: ['rate<0.1'],
  },
};

const BASE_URL = 'http://caching-service:8403';

export default function () {
  let responses = http.batch([
    // Cache GET operations
    ['GET', `${BASE_URL}/api/v1/cache/products/123`],
    ['GET', `${BASE_URL}/api/v1/cache/users/456`],
    ['GET', `${BASE_URL}/api/v1/cache/sessions/789`],
    
    // Cache PUT operations
    ['PUT', `${BASE_URL}/api/v1/cache/products/124`, JSON.stringify({
      name: 'Test Product',
      price: 99.99
    }), { headers: { 'Content-Type': 'application/json' } }],
  ]);

  responses.forEach((response) => {
    check(response, {
      'status is 200 or 404': (r) => [200, 404].includes(r.status),
      'response time < 500ms': (r) => r.timings.duration < 500,
    }) || errorRate.add(1);
  });

  sleep(1);
}
```

## Troubleshooting

### Common Issues

#### Issue: Redis Connection Failures

**Symptoms:**
```
ERROR c.e.c.redis.RedisConnectionManager - Failed to connect to Redis cluster
```

**Solutions:**
```bash
# Check Redis cluster status
kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli cluster info

# Verify network connectivity
kubectl exec -it caching-service-xxx -n caching-system -- telnet redis-cluster-0 6379

# Check Redis authentication
kubectl get secret redis-secret -n caching-system -o yaml

# Restart Redis cluster if needed
kubectl delete pod redis-cluster-0 redis-cluster-1 redis-cluster-2 -n caching-system
```

#### Issue: High Memory Usage

**Symptoms:**
```
WARN c.e.c.memory.MemoryMonitor - High memory usage detected: 85%
```

**Solutions:**
```bash
# Check cache regions usage
curl http://caching-service:8404/actuator/metrics/cache.size

# Clear specific cache regions
curl -X DELETE http://caching-service:8403/api/v1/cache/regions/products/clear

# Update eviction policies
kubectl patch configmap caching-service-config -n caching-system --patch '
data:
  application.yml: |
    cache:
      regions:
        products:
          eviction-policy: LRU
          max-size: 50000
'
```

#### Issue: Slow Cache Performance

**Symptoms:**
```
WARN c.e.c.performance.PerformanceMonitor - Slow cache operation: 1200ms
```

**Solutions:**
```bash
# Check Redis performance
kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli --latency

# Analyze slow queries
kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli slowlog get 10

# Optimize serialization
curl -X PUT http://caching-service:8403/admin/config/serialization \
  -H "Content-Type: application/json" \
  -d '{"type": "binary", "compression": "none"}'
```

### Debugging Tools

```bash
# Debug script
#!/bin/bash
# scripts/debug-caching-service.sh

echo "=== Caching Service Debug Information ==="

# Service status
echo "--- Service Status ---"
kubectl get pods -l app=caching-service -n caching-system

# Recent logs
echo "--- Recent Logs ---"
kubectl logs -l app=caching-service -n caching-system --tail=50

# Redis cluster status
echo "--- Redis Cluster Status ---"
kubectl exec -it redis-cluster-0 -n caching-system -- redis-cli cluster info

# Performance metrics
echo "--- Performance Metrics ---"
curl -s http://caching-service:8404/actuator/metrics/cache.requests | jq .

# Memory usage
echo "--- Memory Usage ---"
curl -s http://caching-service:8404/actuator/metrics/jvm.memory.used | jq .

# Active cache regions
echo "--- Active Cache Regions ---"
curl -s http://caching-service:8403/api/v1/cache/regions | jq .
```

### Log Analysis

```bash
# Analyze cache performance logs
kubectl logs -l app=caching-service -n caching-system | \
  grep "PERFORMANCE" | \
  awk '{print $1, $2, $NF}' | \
  sort | uniq -c | sort -nr

# Find error patterns
kubectl logs -l app=caching-service -n caching-system | \
  grep "ERROR" | \
  cut -d' ' -f4- | \
  sort | uniq -c | sort -nr

# Monitor cache hit rates
kubectl logs -l app=caching-service -n caching-system | \
  grep "cache.hit" | \
  tail -100 | \
  awk '{print $1, $2, $NF}' | \
  sort
```

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Quarterly*
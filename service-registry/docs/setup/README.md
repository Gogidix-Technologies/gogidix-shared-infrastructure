# Setup Documentation - Service Registry (Eureka)

## Overview

This document provides comprehensive setup instructions for the Service Registry (Eureka) service, including development environment setup, production deployment, configuration management, and troubleshooting guidelines.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Development Environment Setup](#development-environment-setup)
3. [Configuration](#configuration)
4. [Production Deployment](#production-deployment)
5. [Docker Setup](#docker-setup)
6. [Kubernetes Deployment](#kubernetes-deployment)
7. [Monitoring Setup](#monitoring-setup)
8. [Security Configuration](#security-configuration)
9. [Troubleshooting](#troubleshooting)
10. [Best Practices](#best-practices)

## Prerequisites

### System Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| Java | OpenJDK 17+ | OpenJDK 17 |
| Memory | 512MB | 1GB+ |
| CPU | 1 core | 2+ cores |
| Disk | 1GB | 5GB+ |
| Network | 100Mbps | 1Gbps+ |

### Software Dependencies

- Java 17+ (OpenJDK recommended)
- Maven 3.8+ or Gradle 7+
- Docker 20+ (for containerization)
- Kubernetes 1.20+ (for orchestration)
- Git (for source control)

### Development Tools

```bash
# Install Java 17
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Verify Java installation
java -version
javac -version

# Install Maven
sudo apt-get install maven

# Verify Maven installation
mvn -version

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

## Development Environment Setup

### 1. Clone Repository

```bash
# Clone the main repository
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/service-registry

# Check project structure
ls -la
```

### 2. Project Structure

```
service-registry/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/exalt/infrastructure/registry/
│   │   │       ├── ServiceRegistryApplication.java
│   │   │       ├── config/
│   │   │       │   ├── EurekaServerConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── MetricsConfig.java
│   │   │       ├── health/
│   │   │       │   └── RegistryHealthIndicator.java
│   │   │       └── monitoring/
│   │   │           └── RegistryMetrics.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── logback-spring.xml
│   └── test/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

### 3. Build Project

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package

# Skip tests if needed
mvn clean package -DskipTests
```

### 4. Local Development

#### Single Instance Setup

```bash
# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run JAR directly
java -jar target/service-registry-1.0.0.jar --spring.profiles.active=dev
```

#### Cluster Setup (3 instances)

```bash
# Terminal 1 - Peer 1
java -jar target/service-registry-1.0.0.jar --spring.profiles.active=peer1 --server.port=8761

# Terminal 2 - Peer 2  
java -jar target/service-registry-1.0.0.jar --spring.profiles.active=peer2 --server.port=8762

# Terminal 3 - Peer 3
java -jar target/service-registry-1.0.0.jar --spring.profiles.active=peer3 --server.port=8763
```

### 5. Verify Installation

```bash
# Check service status
curl http://localhost:8761/actuator/health

# Check Eureka dashboard
open http://localhost:8761

# Check registered services
curl http://localhost:8761/eureka/apps
```

## Configuration

### Application Configuration

#### `application.yml` (Base Configuration)

```yaml
spring:
  application:
    name: service-registry
  profiles:
    active: dev
    
server:
  port: 8761
  servlet:
    context-path: /

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,eureka
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.netflix.eureka: DEBUG
    com.netflix.discovery: DEBUG
    com.exalt.infrastructure.registry: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/service-registry.log
    max-size: 100MB
    max-history: 30
```

#### `application-dev.yml` (Development)

```yaml
spring:
  profiles: dev

eureka:
  instance:
    hostname: localhost
    prefer-ip-address: false
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 10
    
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
    
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000
    
security:
  basic:
    enabled: false

logging:
  level:
    com.netflix: DEBUG
    com.exalt: DEBUG
```

#### `application-prod.yml` (Production)

```yaml
spring:
  profiles: prod

eureka:
  instance:
    hostname: ${HOSTNAME:service-registry}
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    non-secure-port: 8761
    secure-port: 8443
    secure-port-enabled: true
    
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: ${EUREKA_SERVERS:http://eureka-1:8761/eureka/,http://eureka-2:8762/eureka/,http://eureka-3:8763/eureka/}
    registry-fetch-interval-seconds: 30
    
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 60000
    renewal-percent-threshold: 0.85
    
security:
  basic:
    enabled: true
    username: ${EUREKA_USERNAME:admin}
    password: ${EUREKA_PASSWORD:}
    
server:
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE_PATH:classpath:keystore.jks}
    key-store-password: ${SSL_KEYSTORE_PASSWORD:}
    key-store-type: JKS
    trust-store: ${SSL_TRUSTSTORE_PATH:classpath:truststore.jks}
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD:}

logging:
  level:
    com.netflix: INFO
    com.exalt: INFO
```

#### Cluster Configuration

```yaml
# application-peer1.yml
spring:
  profiles: peer1
eureka:
  instance:
    hostname: eureka-peer1
  client:
    service-url:
      defaultZone: http://eureka-peer2:8762/eureka/,http://eureka-peer3:8763/eureka/

---
# application-peer2.yml  
spring:
  profiles: peer2
server:
  port: 8762
eureka:
  instance:
    hostname: eureka-peer2
  client:
    service-url:
      defaultZone: http://eureka-peer1:8761/eureka/,http://eureka-peer3:8763/eureka/

---
# application-peer3.yml
spring:
  profiles: peer3
server:
  port: 8763
eureka:
  instance:
    hostname: eureka-peer3
  client:
    service-url:
      defaultZone: http://eureka-peer1:8761/eureka/,http://eureka-peer2:8762/eureka/
```

### Environment Variables

```bash
# Core Configuration
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8761
export EUREKA_USERNAME=admin
export EUREKA_PASSWORD=secure-password

# Cluster Configuration  
export EUREKA_SERVERS=http://eureka-1:8761/eureka/,http://eureka-2:8762/eureka/,http://eureka-3:8763/eureka/
export HOSTNAME=eureka-server-1

# SSL Configuration
export SSL_KEYSTORE_PATH=/config/ssl/keystore.jks
export SSL_KEYSTORE_PASSWORD=keystore-password
export SSL_TRUSTSTORE_PATH=/config/ssl/truststore.jks
export SSL_TRUSTSTORE_PASSWORD=truststore-password

# Database (if using persistent storage)
export DB_HOST=postgres-server
export DB_PORT=5432
export DB_NAME=service_registry
export DB_USERNAME=eureka_user
export DB_PASSWORD=db-password

# JVM Configuration
export JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
```

## Production Deployment

### System Preparation

```bash
# Create dedicated user
sudo useradd -r -s /bin/false -d /opt/service-registry eureka

# Create directories
sudo mkdir -p /opt/service-registry/{bin,config,logs,ssl}
sudo chown -R eureka:eureka /opt/service-registry

# Copy application
sudo cp target/service-registry-1.0.0.jar /opt/service-registry/bin/
sudo chown eureka:eureka /opt/service-registry/bin/service-registry-1.0.0.jar
```

### SystemD Service

```ini
# /etc/systemd/system/service-registry.service
[Unit]
Description=Service Registry (Eureka)
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
User=eureka
Group=eureka
WorkingDirectory=/opt/service-registry
ExecStart=/usr/bin/java -jar /opt/service-registry/bin/service-registry-1.0.0.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
KillMode=mixed
KillSignal=TERM
TimeoutStopSec=30

# Environment
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC
EnvironmentFile=-/opt/service-registry/config/environment

# Security
NoNewPrivileges=yes
PrivateTmp=yes
ProtectSystem=strict
ProtectHome=yes
ReadWritePaths=/opt/service-registry/logs

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable service-registry
sudo systemctl start service-registry

# Check status
sudo systemctl status service-registry

# View logs
sudo journalctl -u service-registry -f
```

### Nginx Reverse Proxy

```nginx
# /etc/nginx/sites-available/service-registry
upstream eureka-cluster {
    server eureka-1:8761;
    server eureka-2:8762;
    server eureka-3:8763;
}

server {
    listen 80;
    server_name eureka.exalt.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name eureka.exalt.com;
    
    ssl_certificate /etc/ssl/certs/eureka.exalt.com.crt;
    ssl_certificate_key /etc/ssl/private/eureka.exalt.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    location / {
        proxy_pass http://eureka-cluster;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Timeout settings
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Health check endpoint
    location /actuator/health {
        proxy_pass http://eureka-cluster;
        access_log off;
    }
}
```

## Docker Setup

### Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim

# Metadata
LABEL maintainer="Exalt Infrastructure Team <infrastructure@exalt.com>"
LABEL version="1.0.0"
LABEL description="Service Registry (Eureka Server)"

# Create app user
RUN groupadd -r eureka && useradd -r -g eureka eureka

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    netcat \
    && rm -rf /var/lib/apt/lists/*

# Create directories
RUN mkdir -p /opt/service-registry/{config,logs,ssl} && \
    chown -R eureka:eureka /opt/service-registry

# Copy application
COPY target/service-registry-*.jar /opt/service-registry/app.jar
RUN chown eureka:eureka /opt/service-registry/app.jar

# Copy configuration
COPY src/main/resources/application*.yml /opt/service-registry/config/

# Set working directory
WORKDIR /opt/service-registry

# Switch to app user
USER eureka

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

# Expose ports
EXPOSE 8761 8762 8763

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
```

### Docker Compose

```yaml
version: '3.8'

services:
  eureka-1:
    build: .
    image: exalt/service-registry:latest
    container_name: eureka-1
    hostname: eureka-1
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=peer1
      - EUREKA_INSTANCE_HOSTNAME=eureka-1
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-2:8762/eureka/,http://eureka-3:8763/eureka/
    volumes:
      - eureka-1-logs:/opt/service-registry/logs
      - ./config:/opt/service-registry/config
    networks:
      - eureka-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  eureka-2:
    build: .
    image: exalt/service-registry:latest
    container_name: eureka-2
    hostname: eureka-2
    ports:
      - "8762:8762"
    environment:
      - SPRING_PROFILES_ACTIVE=peer2
      - SERVER_PORT=8762
      - EUREKA_INSTANCE_HOSTNAME=eureka-2
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-1:8761/eureka/,http://eureka-3:8763/eureka/
    volumes:
      - eureka-2-logs:/opt/service-registry/logs
      - ./config:/opt/service-registry/config
    networks:
      - eureka-network
    restart: unless-stopped
    depends_on:
      - eureka-1

  eureka-3:
    build: .
    image: exalt/service-registry:latest
    container_name: eureka-3
    hostname: eureka-3
    ports:
      - "8763:8763"
    environment:
      - SPRING_PROFILES_ACTIVE=peer3
      - SERVER_PORT=8763
      - EUREKA_INSTANCE_HOSTNAME=eureka-3
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-1:8761/eureka/,http://eureka-2:8762/eureka/
    volumes:
      - eureka-3-logs:/opt/service-registry/logs
      - ./config:/opt/service-registry/config
    networks:
      - eureka-network
    restart: unless-stopped
    depends_on:
      - eureka-1
      - eureka-2

volumes:
  eureka-1-logs:
  eureka-2-logs:
  eureka-3-logs:

networks:
  eureka-network:
    driver: bridge
```

### Docker Commands

```bash
# Build image
docker build -t exalt/service-registry:latest .

# Run single instance
docker run -d \
  --name eureka-server \
  -p 8761:8761 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -v $(pwd)/logs:/opt/service-registry/logs \
  exalt/service-registry:latest

# Run cluster
docker-compose up -d

# Scale services
docker-compose up -d --scale eureka-1=2

# View logs
docker-compose logs -f eureka-1

# Stop services
docker-compose down
```

## Kubernetes Deployment

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: eureka-config
  namespace: infrastructure
data:
  application.yml: |
    spring:
      application:
        name: service-registry
    eureka:
      instance:
        prefer-ip-address: true
        lease-renewal-interval-in-seconds: 30
        lease-expiration-duration-in-seconds: 90
      client:
        register-with-eureka: true
        fetch-registry: true
        service-url:
          defaultZone: http://eureka-0.eureka-headless:8761/eureka/,http://eureka-1.eureka-headless:8761/eureka/,http://eureka-2.eureka-headless:8761/eureka/
      server:
        enable-self-preservation: true
        eviction-interval-timer-in-ms: 60000
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
```

### Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: eureka-secret
  namespace: infrastructure
type: Opaque
data:
  username: YWRtaW4=  # admin
  password: c2VjdXJlLXBhc3N3b3Jk  # secure-password
```

### StatefulSet

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: eureka
  namespace: infrastructure
spec:
  serviceName: eureka-headless
  replicas: 3
  selector:
    matchLabels:
      app: eureka
  template:
    metadata:
      labels:
        app: eureka
    spec:
      containers:
      - name: eureka
        image: exalt/service-registry:latest
        ports:
        - containerPort: 8761
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: EUREKA_INSTANCE_HOSTNAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: EUREKA_USERNAME
          valueFrom:
            secretKeyRef:
              name: eureka-secret
              key: username
        - name: EUREKA_PASSWORD
          valueFrom:
            secretKeyRef:
              name: eureka-secret
              key: password
        volumeMounts:
        - name: config
          mountPath: /opt/service-registry/config
        - name: logs
          mountPath: /opt/service-registry/logs
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8761
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8761
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
      volumes:
      - name: config
        configMap:
          name: eureka-config
  volumeClaimTemplates:
  - metadata:
      name: logs
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 1Gi
```

### Services

```yaml
# Headless service for StatefulSet
apiVersion: v1
kind: Service
metadata:
  name: eureka-headless
  namespace: infrastructure
spec:
  clusterIP: None
  selector:
    app: eureka
  ports:
  - name: http
    port: 8761
    targetPort: 8761

---
# LoadBalancer service for external access
apiVersion: v1
kind: Service
metadata:
  name: eureka-service
  namespace: infrastructure
spec:
  selector:
    app: eureka
  ports:
  - name: http
    port: 8761
    targetPort: 8761
  type: LoadBalancer
```

### Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: eureka-ingress
  namespace: infrastructure
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/auth-type: basic
    nginx.ingress.kubernetes.io/auth-secret: eureka-auth
spec:
  tls:
  - hosts:
    - eureka.exalt.com
    secretName: eureka-tls
  rules:
  - host: eureka.exalt.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: eureka-service
            port:
              number: 8761
```

### Deployment Commands

```bash
# Create namespace
kubectl create namespace infrastructure

# Apply configurations
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/statefulset.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# Check status
kubectl get pods -n infrastructure -l app=eureka
kubectl get services -n infrastructure
kubectl get ingress -n infrastructure

# View logs
kubectl logs -n infrastructure eureka-0 -f

# Scale up/down
kubectl scale statefulset eureka --replicas=5 -n infrastructure

# Port forward for testing
kubectl port-forward -n infrastructure service/eureka-service 8761:8761
```

## Monitoring Setup

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'eureka-server'
    static_configs:
      - targets: ['eureka-1:8761', 'eureka-2:8762', 'eureka-3:8763']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
```

### Grafana Dashboard

```json
{
  "dashboard": {
    "id": null,
    "title": "Eureka Server Metrics",
    "panels": [
      {
        "title": "Registered Services",
        "type": "stat",
        "targets": [
          {
            "expr": "eureka_server_registry_size",
            "legendFormat": "Services"
          }
        ]
      },
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])",
            "legendFormat": "Requests/sec"
          }
        ]
      }
    ]
  }
}
```

### Alert Rules

```yaml
# eureka-alerts.yml
groups:
  - name: eureka-server
    rules:
      - alert: EurekaServerDown
        expr: up{job="eureka-server"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Eureka server is down"
          description: "Eureka server {{ $labels.instance }} has been down for more than 1 minute"
          
      - alert: EurekaHighRequestLatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High request latency on Eureka server"
          description: "95th percentile latency is {{ $value }}s on {{ $labels.instance }}"
```

## Security Configuration

### SSL/TLS Setup

```bash
# Generate keystore
keytool -genkeypair -alias eureka-server \
  -keyalg RSA -keysize 2048 \
  -keystore keystore.jks \
  -storepass changeit \
  -dname "CN=eureka.exalt.com,OU=Infrastructure,O=Exalt,C=US"

# Generate truststore
keytool -import -alias eureka-ca \
  -file ca-certificate.crt \
  -keystore truststore.jks \
  -storepass changeit
```

### Basic Authentication

```yaml
# Security configuration
security:
  basic:
    enabled: true
    username: ${EUREKA_USERNAME:admin}
    password: ${EUREKA_PASSWORD:secure-password}
    
management:
  security:
    enabled: true
    roles: ADMIN
```

### Network Security

```bash
# Firewall configuration
sudo ufw allow 8761/tcp  # Eureka HTTP
sudo ufw allow 8443/tcp  # Eureka HTTPS
sudo ufw deny 8762/tcp   # Block peer communication from external
sudo ufw deny 8763/tcp   # Block peer communication from external
```

## Troubleshooting

### Common Issues

#### 1. Service Registration Failures

```bash
# Check Eureka server logs
tail -f /opt/service-registry/logs/service-registry.log

# Check network connectivity
telnet eureka-server 8761

# Verify configuration
curl http://admin:password@eureka-server:8761/actuator/health
```

#### 2. Peer Replication Issues

```bash
# Check peer connectivity
curl http://eureka-1:8761/eureka/apps
curl http://eureka-2:8762/eureka/apps
curl http://eureka-3:8763/eureka/apps

# Verify cluster configuration
grep -i "defaultZone" /opt/service-registry/config/application-prod.yml
```

#### 3. High Memory Usage

```bash
# Check JVM memory usage
jstat -gc $(pgrep -f service-registry)

# Analyze heap dump
jmap -histo $(pgrep -f service-registry)

# Adjust JVM settings
export JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
```

### Diagnostic Commands

```bash
# Check application status
curl http://localhost:8761/actuator/health
curl http://localhost:8761/actuator/info
curl http://localhost:8761/actuator/metrics

# Check registered services
curl http://localhost:8761/eureka/apps

# Check specific service
curl http://localhost:8761/eureka/apps/ORDER-SERVICE

# Check server configuration
curl http://admin:password@localhost:8761/actuator/configprops
```

### Log Analysis

```bash
# Enable debug logging
export LOGGING_LEVEL_COM_NETFLIX_EUREKA=DEBUG

# Search for errors
grep -i error /opt/service-registry/logs/service-registry.log

# Monitor real-time logs
tail -f /opt/service-registry/logs/service-registry.log | grep -E "(ERROR|WARN)"

# Analyze registration patterns
grep "registered" /opt/service-registry/logs/service-registry.log | tail -20
```

## Best Practices

### Performance Optimization

1. **JVM Tuning**
   ```bash
   # Recommended JVM options
   -Xms512m -Xmx1g
   -XX:+UseG1GC
   -XX:MaxGCPauseMillis=100
   -XX:+HeapDumpOnOutOfMemoryError
   ```

2. **Cache Configuration**
   ```yaml
   eureka:
     server:
       response-cache-update-interval-ms: 30000
       use-read-only-response-cache: true
   ```

3. **Network Optimization**
   ```yaml
   eureka:
     client:
       registry-fetch-interval-seconds: 30
       cache-refresh-task-time-interval-seconds: 30
   ```

### Security Best Practices

1. **Enable Authentication**
2. **Use HTTPS in Production**
3. **Restrict Network Access**
4. **Regular Security Updates**
5. **Monitor Access Logs**

### Operational Best Practices

1. **Use Cluster Deployment**
2. **Monitor Health Endpoints**
3. **Configure Proper Timeouts**
4. **Enable Self-Preservation**
5. **Regular Backups**

### Development Best Practices

1. **Use Profiles for Different Environments**
2. **Implement Custom Health Checks**
3. **Configure Proper Logging**
4. **Use Configuration Management**
5. **Automate Deployment**

---

*Last Updated: 2024-06-25*
*Document Version: 1.0*
*Review Schedule: Quarterly*
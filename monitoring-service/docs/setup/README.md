# Monitoring Service - Setup Guide

## Overview

This guide provides comprehensive instructions for setting up the Exalt Monitoring Service in various environments, from local development to production deployment.

## Prerequisites

### System Requirements

#### Minimum Requirements (Development)
- **CPU**: 4 cores
- **Memory**: 8 GB RAM
- **Storage**: 50 GB available space
- **Network**: 1 Gbps network interface

#### Recommended Requirements (Production)
- **CPU**: 16 cores
- **Memory**: 32 GB RAM
- **Storage**: 500 GB SSD (metrics), 1 TB SSD (logs)
- **Network**: 10 Gbps network interface
- **Redundancy**: Multi-node deployment across availability zones

### Software Dependencies

#### Required Software
```bash
# Core Dependencies
Java 17+                    # Application runtime
Maven 3.8+                  # Build system
Docker 20.10+              # Containerization
Docker Compose 2.0+        # Local orchestration
Kubernetes 1.25+           # Production orchestration

# Infrastructure Dependencies
PostgreSQL 14+             # Metadata storage
Redis 6.2+                 # Caching layer
Apache Kafka 3.0+          # Event streaming
Elasticsearch 8.10+        # Log storage
```

#### Optional Dependencies
```bash
# Development Tools
kubectl                    # Kubernetes CLI
helm 3.0+                 # Kubernetes package manager
istioctl                  # Service mesh CLI
jq                        # JSON processing
curl                      # HTTP client
```

## Environment Setup

### Local Development Environment

#### 1. Clone Repository
```bash
git clone <repository-url>
cd CLEAN-SOCIAL-ECOMMERCE-ECOSYSTEM/shared-infrastructure/monitoring-service
```

#### 2. Environment Configuration
```bash
# Copy environment template
cp .env.template .env

# Edit configuration
vim .env
```

#### 3. Environment Variables
```bash
# .env file configuration
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=monitoring_service_db
DB_USER=monitoring_service_user
DB_PASSWORD=secure_password_change_in_production

# Service Registry
EUREKA_SERVER_URL=http://localhost:8761/eureka
SERVICE_REGISTRY_ENABLED=true

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_CONSUMER_GROUP=monitoring-service-group
KAFKA_PRODUCER_ACKS=all

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_password_change_in_production
REDIS_TIMEOUT=5000

# Monitoring Service Configuration
SERVER_PORT=8090
SPRING_PROFILES_ACTIVE=development
LOG_LEVEL=INFO

# Prometheus Configuration
PROMETHEUS_URL=http://localhost:9090
PROMETHEUS_RETENTION_TIME=30d
PROMETHEUS_SCRAPE_INTERVAL=15s

# Grafana Configuration
GRAFANA_URL=http://localhost:3000
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=grafana_admin_password_change_in_production

# AlertManager Configuration
ALERTMANAGER_URL=http://localhost:9093
ALERTMANAGER_SMTP_HOST=smtp.gmail.com
ALERTMANAGER_SMTP_PORT=587
ALERTMANAGER_SMTP_USERNAME=alerts@exalt.com
ALERTMANAGER_SMTP_PASSWORD=smtp_password_change_in_production

# Jaeger Configuration
JAEGER_ENDPOINT=http://localhost:14268/api/traces
JAEGER_SAMPLER_TYPE=const
JAEGER_SAMPLER_PARAM=1

# Elasticsearch Configuration
ELASTICSEARCH_HOST=localhost
ELASTICSEARCH_PORT=9200
ELASTICSEARCH_USERNAME=elastic
ELASTICSEARCH_PASSWORD=elastic_password_change_in_production

# Security Configuration
JWT_SECRET=jwt_secret_key_change_in_production_use_256_bit_key
OAUTH_CLIENT_ID=monitoring-service-client
OAUTH_CLIENT_SECRET=oauth_client_secret_change_in_production

# Feature Flags
METRICS_COLLECTION_ENABLED=true
LOG_AGGREGATION_ENABLED=true
DISTRIBUTED_TRACING_ENABLED=true
ALERTING_ENABLED=true
```

#### 4. Start Infrastructure Dependencies
```bash
# Start all dependencies with Docker Compose
docker-compose up -d

# Verify services are running
docker-compose ps

# Check logs
docker-compose logs -f prometheus grafana
```

#### 5. Build and Run Application
```bash
# Build application
mvn clean compile

# Run tests
mvn test

# Start application
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=development
```

#### 6. Verify Installation
```bash
# Check application health
curl http://localhost:8090/actuator/health

# Check Prometheus metrics
curl http://localhost:8090/actuator/prometheus

# Access Grafana dashboard
open http://localhost:3000
# Login: admin / grafana_admin_password_change_in_production

# Access Prometheus UI
open http://localhost:9090

# Access AlertManager UI
open http://localhost:9093
```

## Docker Deployment

### Container Configuration

#### 1. Build Docker Image
```bash
# Build application JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t com.exalt.monitoring-service:latest .

# Tag for registry
docker tag com.exalt.monitoring-service:latest registry.exalt.com/monitoring-service:1.0.0
```

#### 2. Docker Compose Production Setup
```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  monitoring-service:
    image: com.exalt.monitoring-service:latest
    ports:
      - "8090:8090"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - postgres
      - redis
      - kafka
      - prometheus
      - grafana
    networks:
      - monitoring-network
    restart: unless-stopped

  prometheus:
    image: prom/prometheus:v2.47.2
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./prometheus/rules:/etc/prometheus/rules
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--web.enable-lifecycle'
      - '--web.enable-admin-api'
    networks:
      - monitoring-network
    restart: unless-stopped

  grafana:
    image: grafana/grafana:10.2.0
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_SERVER_ROOT_URL=https://monitoring.exalt.com/grafana
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
      - ./grafana/dashboards:/var/lib/grafana/dashboards
    networks:
      - monitoring-network
    restart: unless-stopped

  alertmanager:
    image: prom/alertmanager:v0.26.0
    ports:
      - "9093:9093"
    volumes:
      - ./alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml
      - alertmanager-data:/alertmanager
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
      - '--web.external-url=https://monitoring.exalt.com/alertmanager'
    networks:
      - monitoring-network
    restart: unless-stopped

  jaeger:
    image: jaegertracing/all-in-one:1.50
    ports:
      - "16686:16686"
      - "14268:14268"
    environment:
      - COLLECTOR_OTLP_ENABLED=true
      - SPAN_STORAGE_TYPE=elasticsearch
      - ES_SERVER_URLS=http://elasticsearch:9200
    networks:
      - monitoring-network
    restart: unless-stopped

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.4
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - monitoring-network
    restart: unless-stopped

volumes:
  prometheus-data:
  grafana-data:
  alertmanager-data:
  elasticsearch-data:

networks:
  monitoring-network:
    driver: bridge
```

#### 3. Run Production Stack
```bash
# Start production stack
docker-compose -f docker-compose.prod.yml up -d

# Scale monitoring service
docker-compose -f docker-compose.prod.yml up -d --scale monitoring-service=3

# Check status
docker-compose -f docker-compose.prod.yml ps
```

## Kubernetes Deployment

### Cluster Preparation

#### 1. Namespace Creation
```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: monitoring
  labels:
    name: monitoring
    app.kubernetes.io/name: monitoring-service
    app.kubernetes.io/part-of: exalt-ecosystem
```

#### 2. Apply Namespace
```bash
kubectl apply -f k8s/namespace.yaml
```

### Configuration Management

#### 1. ConfigMap
```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: monitoring-service-config
  namespace: monitoring
data:
  application.yml: |
    server:
      port: 8090
    
    spring:
      application:
        name: monitoring-service
      profiles:
        active: kubernetes
      datasource:
        url: jdbc:postgresql://postgres-service:5432/monitoring_service_db
        username: ${DB_USER}
        password: ${DB_PASSWORD}
        driver-class-name: org.postgresql.Driver
      
      redis:
        host: redis-service
        port: 6379
        password: ${REDIS_PASSWORD}
      
      kafka:
        bootstrap-servers: kafka-service:9092
        consumer:
          group-id: monitoring-service-group
        producer:
          acks: all
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: always
      metrics:
        export:
          prometheus:
            enabled: true
    
    eureka:
      client:
        service-url:
          defaultZone: http://eureka-service:8761/eureka
        enabled: true
```

#### 2. Secrets
```yaml
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: monitoring-service-secrets
  namespace: monitoring
type: Opaque
data:
  DB_PASSWORD: <base64-encoded-password>
  REDIS_PASSWORD: <base64-encoded-password>
  JWT_SECRET: <base64-encoded-jwt-secret>
  GRAFANA_ADMIN_PASSWORD: <base64-encoded-grafana-password>
  OAUTH_CLIENT_SECRET: <base64-encoded-oauth-secret>
```

#### 3. Create Secrets
```bash
# Create secrets
kubectl create secret generic monitoring-service-secrets \
  --from-literal=DB_PASSWORD=secure_db_password \
  --from-literal=REDIS_PASSWORD=secure_redis_password \
  --from-literal=JWT_SECRET=secure_jwt_secret_256_bit \
  --from-literal=GRAFANA_ADMIN_PASSWORD=secure_grafana_password \
  --from-literal=OAUTH_CLIENT_SECRET=secure_oauth_secret \
  --namespace=monitoring
```

### Application Deployment

#### 1. Deployment Configuration
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: monitoring-service
  namespace: monitoring
  labels:
    app: monitoring-service
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: monitoring-service
  template:
    metadata:
      labels:
        app: monitoring-service
        version: v1
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8090"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
      - name: monitoring-service
        image: registry.exalt.com/monitoring-service:1.0.0
        ports:
        - containerPort: 8090
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DB_USER
          value: "monitoring_service_user"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: monitoring-service-secrets
              key: DB_PASSWORD
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: monitoring-service-secrets
              key: REDIS_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: monitoring-service-secrets
              key: JWT_SECRET
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8090
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8090
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
      volumes:
      - name: config-volume
        configMap:
          name: monitoring-service-config
```

#### 2. Service Configuration
```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: monitoring-service
  namespace: monitoring
  labels:
    app: monitoring-service
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8090"
spec:
  type: ClusterIP
  ports:
  - port: 8090
    targetPort: 8090
    protocol: TCP
    name: http
  selector:
    app: monitoring-service
```

#### 3. Ingress Configuration
```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: monitoring-service-ingress
  namespace: monitoring
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - monitoring.exalt.com
    secretName: monitoring-service-tls
  rules:
  - host: monitoring.exalt.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: monitoring-service
            port:
              number: 8090
```

### Monitoring Stack Deployment

#### 1. Prometheus Deployment
```yaml
# prometheus-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: monitoring
spec:
  replicas: 2
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      containers:
      - name: prometheus
        image: prom/prometheus:v2.47.2
        ports:
        - containerPort: 9090
        args:
          - '--config.file=/etc/prometheus/prometheus.yml'
          - '--storage.tsdb.path=/prometheus'
          - '--storage.tsdb.retention.time=30d'
          - '--web.enable-lifecycle'
          - '--web.enable-admin-api'
        volumeMounts:
        - name: prometheus-config
          mountPath: /etc/prometheus
        - name: prometheus-storage
          mountPath: /prometheus
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
      volumes:
      - name: prometheus-config
        configMap:
          name: prometheus-config
      - name: prometheus-storage
        persistentVolumeClaim:
          claimName: prometheus-pvc
```

#### 2. Grafana Deployment
```yaml
# grafana-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: monitoring
spec:
  replicas: 2
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
      - name: grafana
        image: grafana/grafana:10.2.0
        ports:
        - containerPort: 3000
        env:
        - name: GF_SECURITY_ADMIN_PASSWORD
          valueFrom:
            secretKeyRef:
              name: monitoring-service-secrets
              key: GRAFANA_ADMIN_PASSWORD
        - name: GF_USERS_ALLOW_SIGN_UP
          value: "false"
        volumeMounts:
        - name: grafana-storage
          mountPath: /var/lib/grafana
        - name: grafana-provisioning
          mountPath: /etc/grafana/provisioning
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
      volumes:
      - name: grafana-storage
        persistentVolumeClaim:
          claimName: grafana-pvc
      - name: grafana-provisioning
        configMap:
          name: grafana-provisioning
```

### Deploy to Kubernetes

#### 1. Apply All Configurations
```bash
# Apply in order
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/pvc.yaml
kubectl apply -f k8s/prometheus-deployment.yaml
kubectl apply -f k8s/grafana-deployment.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

#### 2. Verify Deployment
```bash
# Check pods
kubectl get pods -n monitoring

# Check services
kubectl get svc -n monitoring

# Check ingress
kubectl get ingress -n monitoring

# View logs
kubectl logs -f deployment/monitoring-service -n monitoring

# Port forward for testing
kubectl port-forward svc/monitoring-service 8090:8090 -n monitoring
```

## Configuration Reference

### Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "/etc/prometheus/rules/*.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'monitoring-service'
    kubernetes_sd_configs:
      - role: endpoints
        namespaces:
          names:
            - monitoring
    relabel_configs:
      - source_labels: [__meta_kubernetes_service_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_service_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
```

### Grafana Provisioning
```yaml
# datasources.yml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true

  - name: Elasticsearch
    type: elasticsearch
    access: proxy
    url: http://elasticsearch:9200
    database: "[logs-]YYYY.MM.DD"
    interval: Daily

  - name: Jaeger
    type: jaeger
    access: proxy
    url: http://jaeger:16686
```

### AlertManager Configuration
```yaml
# alertmanager.yml
global:
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'alerts@exalt.com'
  smtp_auth_username: 'alerts@exalt.com'
  smtp_auth_password: '${SMTP_PASSWORD}'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

receivers:
  - name: 'web.hook'
    email_configs:
      - to: 'infrastructure-team@exalt.com'
        subject: 'Alert: {{ .GroupLabels.alertname }}'
        body: |
          {{ range .Alerts }}
          Alert: {{ .Annotations.summary }}
          Description: {{ .Annotations.description }}
          {{ end }}

  - name: 'slack'
    slack_configs:
      - api_url: '${SLACK_WEBHOOK_URL}'
        channel: '#alerts'
        title: 'Alert: {{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

## Validation and Testing

### Health Check Validation
```bash
# Application health
curl -f http://localhost:8090/actuator/health || exit 1

# Prometheus metrics
curl -f http://localhost:8090/actuator/prometheus | grep -q "jvm_memory_used_bytes" || exit 1

# Service registration
curl -f http://eureka-service:8761/eureka/apps/MONITORING-SERVICE || exit 1
```

### Integration Testing
```bash
# Run integration tests
mvn test -Dtest=**/*IntegrationTest

# Run with testcontainers
mvn test -Dspring.profiles.active=test

# Database migration test
mvn flyway:migrate -Dflyway.configFiles=src/test/resources/flyway-test.conf
```

### Load Testing
```bash
# Install Apache Bench
sudo apt-get install apache2-utils

# Basic load test
ab -n 1000 -c 10 http://localhost:8090/api/v1/health

# Stress test with custom scenarios
docker run --rm -i loadimpact/k6 run - <loadtest.js
```

## Troubleshooting Setup Issues

### Common Issues

#### 1. Database Connection Issues
```bash
# Check database connectivity
kubectl exec -it deployment/monitoring-service -n monitoring -- \
  pg_isready -h postgres-service -p 5432 -U monitoring_service_user

# Check database logs
kubectl logs deployment/postgres -n infrastructure

# Test connection manually
kubectl run postgres-client --rm -it --image postgres:14 -- \
  psql -h postgres-service -U monitoring_service_user -d monitoring_service_db
```

#### 2. Service Discovery Issues
```bash
# Check Eureka registration
curl http://eureka-service:8761/eureka/apps

# Check service logs
kubectl logs deployment/monitoring-service -n monitoring | grep -i eureka

# Test service discovery
kubectl exec -it deployment/monitoring-service -n monitoring -- \
  nslookup eureka-service
```

#### 3. Kubernetes Deployment Issues
```bash
# Check pod status
kubectl describe pod <pod-name> -n monitoring

# Check events
kubectl get events -n monitoring --sort-by=.metadata.creationTimestamp

# Check resource usage
kubectl top pods -n monitoring

# Check persistent volumes
kubectl get pv,pvc -n monitoring
```

### Recovery Procedures

#### 1. Application Recovery
```bash
# Restart deployment
kubectl rollout restart deployment/monitoring-service -n monitoring

# Scale down and up
kubectl scale deployment monitoring-service --replicas=0 -n monitoring
kubectl scale deployment monitoring-service --replicas=3 -n monitoring

# Check rollout status
kubectl rollout status deployment/monitoring-service -n monitoring
```

#### 2. Data Recovery
```bash
# Restore from backup
kubectl exec -it deployment/postgres -n infrastructure -- \
  pg_restore -h localhost -U postgres -d monitoring_service_db /backup/latest.dump

# Verify data integrity
kubectl exec -it deployment/monitoring-service -n monitoring -- \
  java -jar app.jar --spring.profiles.active=verify-data
```

## Next Steps

After successful setup:

1. **Configure Dashboards**: Import pre-built dashboards and create custom ones
2. **Set up Alerting**: Configure alert rules and notification channels
3. **Enable Tracing**: Instrument applications for distributed tracing
4. **Tune Performance**: Optimize queries and resource allocation
5. **Set up Backups**: Configure automated backup procedures

## Related Documentation

- [Architecture Documentation](../architecture/README.md) - System design and components
- [Operations Guide](../operations/README.md) - Daily operations and maintenance
- [API Documentation](../../api-docs/openapi.yaml) - REST API specification
- [Troubleshooting Guide](../operations/troubleshooting.md) - Common issues and solutions

---

**Document Version**: 1.0
**Last Updated**: June 25, 2024
**Author**: Exalt Infrastructure Team
**Review Cycle**: Monthly
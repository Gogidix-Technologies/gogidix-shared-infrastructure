# Setup Guide - Logging Service

## Overview

This guide provides comprehensive instructions for setting up the Logging Service, including local development, Docker deployment, and production configuration for the ELK stack and supporting components.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Component Configuration](#component-configuration)
6. [Security Setup](#security-setup)
7. [Performance Tuning](#performance-tuning)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **CPU**: Minimum 8 cores (16 recommended for production)
- **RAM**: Minimum 16GB (32GB recommended for production)
- **Storage**: 500GB+ SSD storage for log retention
- **OS**: Linux (Ubuntu 20.04+ or RHEL 8+)

### Software Requirements

```bash
# Java Development
- Java 17+ (OpenJDK)
- Maven 3.8+

# Container Runtime
- Docker 24.0+
- Docker Compose 2.20+
- Kubernetes 1.28+ (for production)

# ELK Stack
- Elasticsearch 8.10.0
- Logstash 8.10.0
- Kibana 8.10.0

# Message Queue
- Apache Kafka 3.5.0
- Zookeeper 3.8.2

# Development Tools
- Git 2.40+
- curl/httpie
- jq for JSON processing
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt-tech/logging-service.git
cd logging-service
```

### 2. Environment Configuration

```bash
# Copy environment template
cp .env.template .env

# Edit configuration
nano .env
```

Required environment variables:
```env
# Service Configuration
SERVICE_NAME=logging-service
SERVICE_PORT=8086
SPRING_PROFILES_ACTIVE=dev

# Elasticsearch Configuration
ES_HOSTS=localhost:9200,localhost:9201,localhost:9202
ES_USERNAME=elastic
ES_PASSWORD=changeme
ES_SSL_ENABLED=false

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_SECURITY_PROTOCOL=PLAINTEXT
KAFKA_GROUP_ID=logging-service

# Kibana Configuration
KIBANA_HOST=localhost:5601
KIBANA_USERNAME=kibana_system
KIBANA_PASSWORD=changeme

# Logstash Configuration
LOGSTASH_HOST=localhost:5000
LOGSTASH_BEATS_PORT=5044
LOGSTASH_HTTP_PORT=9600

# Security Configuration
JWT_SECRET=your_jwt_secret_key_min_32_chars
API_KEY_SALT=your_api_key_salt

# Storage Configuration
LOG_RETENTION_DAYS=30
INDEX_SHARDS=3
INDEX_REPLICAS=1

# Performance Configuration
BATCH_SIZE=1000
FLUSH_INTERVAL_MS=5000
MAX_BULK_SIZE_MB=100
```

### 3. Start Infrastructure Services

```bash
# Start Elasticsearch cluster
docker-compose -f docker-compose.elasticsearch.yml up -d

# Wait for Elasticsearch to be ready
until curl -s http://localhost:9200/_cluster/health | grep -q '"status":"green"'; do
  echo "Waiting for Elasticsearch..."
  sleep 5
done

# Start Kafka
docker-compose -f docker-compose.kafka.yml up -d

# Start Kibana
docker-compose -f docker-compose.kibana.yml up -d
```

### 4. Initialize Elasticsearch

```bash
# Create index templates
curl -X PUT "localhost:9200/_index_template/logs" \
  -H 'Content-Type: application/json' \
  -d @elasticsearch/templates/logs-template.json

# Create lifecycle policies
curl -X PUT "localhost:9200/_ilm/policy/logs-policy" \
  -H 'Content-Type: application/json' \
  -d @elasticsearch/policies/logs-ilm-policy.json

# Create initial indices
curl -X PUT "localhost:9200/logs-application-000001" \
  -H 'Content-Type: application/json' \
  -d '{
    "aliases": {
      "logs-application": {
        "is_write_index": true
      }
    }
  }'
```

### 5. Start Logstash

```bash
# Validate configuration
docker run --rm -v $(pwd)/logstash:/config \
  docker.elastic.co/logstash/logstash:8.10.0 \
  logstash -f /config/pipeline/logstash.conf --config.test_and_exit

# Start Logstash
docker-compose -f docker-compose.logstash.yml up -d
```

### 6. Build and Run Application

```bash
# Build application
mvn clean package

# Run application
java -jar target/logging-service-1.0.0.jar

# Or use Maven
mvn spring-boot:run
```

### 7. Verify Setup

```bash
# Check service health
curl http://localhost:8086/actuator/health

# Test log ingestion
curl -X POST http://localhost:8086/api/v1/logs \
  -H "Content-Type: application/json" \
  -d '{
    "level": "INFO",
    "message": "Test log message",
    "service": "test-service"
  }'

# Check Elasticsearch
curl http://localhost:9200/_cat/indices?v

# Access Kibana
open http://localhost:5601
```

## Docker Setup

### 1. Complete Docker Compose Configuration

```yaml
version: '3.8'

services:
  # Elasticsearch Cluster
  elasticsearch-master:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    container_name: es-master
    environment:
      - node.name=es-master
      - cluster.name=logs-cluster
      - discovery.seed_hosts=es-data-1,es-data-2
      - cluster.initial_master_nodes=es-master
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
      - xpack.security.enabled=false
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - es-master-data:/usr/share/elasticsearch/data
    networks:
      - elastic
    ports:
      - "9200:9200"

  elasticsearch-data-1:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    container_name: es-data-1
    environment:
      - node.name=es-data-1
      - cluster.name=logs-cluster
      - discovery.seed_hosts=es-master,es-data-2
      - cluster.initial_master_nodes=es-master
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms4g -Xmx4g"
      - node.roles=data,ingest
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - es-data-1-data:/usr/share/elasticsearch/data
    networks:
      - elastic

  elasticsearch-data-2:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    container_name: es-data-2
    environment:
      - node.name=es-data-2
      - cluster.name=logs-cluster
      - discovery.seed_hosts=es-master,es-data-1
      - cluster.initial_master_nodes=es-master
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms4g -Xmx4g"
      - node.roles=data,ingest
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - es-data-2-data:/usr/share/elasticsearch/data
    networks:
      - elastic

  # Kibana
  kibana:
    image: docker.elastic.co/kibana/kibana:8.10.0
    container_name: kibana
    environment:
      - ELASTICSEARCH_HOSTS=["http://es-master:9200"]
      - ELASTICSEARCH_USERNAME=kibana_system
      - ELASTICSEARCH_PASSWORD=changeme
    ports:
      - "5601:5601"
    networks:
      - elastic
    depends_on:
      - elasticsearch-master

  # Logstash
  logstash:
    image: docker.elastic.co/logstash/logstash:8.10.0
    container_name: logstash
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
      - ./logstash/config/logstash.yml:/usr/share/logstash/config/logstash.yml
      - ./logstash/config/pipelines.yml:/usr/share/logstash/config/pipelines.yml
    environment:
      - "LS_JAVA_OPTS=-Xms2g -Xmx2g"
    ports:
      - "5000:5000"
      - "5044:5044"
      - "9600:9600"
    networks:
      - elastic
    depends_on:
      - elasticsearch-master
      - kafka

  # Kafka
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    networks:
      - elastic

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    networks:
      - elastic

  # Filebeat
  filebeat:
    image: docker.elastic.co/beats/filebeat:8.10.0
    container_name: filebeat
    user: root
    volumes:
      - ./filebeat/filebeat.yml:/usr/share/filebeat/filebeat.yml:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    command: filebeat -e -strict.perms=false
    networks:
      - elastic
    depends_on:
      - kafka

  # Logging Service
  logging-service:
    build: .
    container_name: logging-service
    ports:
      - "8086:8086"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - ES_HOSTS=es-master:9200
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    networks:
      - elastic
    depends_on:
      - elasticsearch-master
      - kafka
      - logstash

volumes:
  es-master-data:
  es-data-1-data:
  es-data-2-data:

networks:
  elastic:
    driver: bridge
```

### 2. Build and Run

```bash
# Build all services
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

## Kubernetes Deployment

### 1. Namespace and ConfigMaps

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: logging-system

---
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: logging-config
  namespace: logging-system
data:
  application.yml: |
    spring:
      application:
        name: logging-service
    elasticsearch:
      hosts: ${ES_HOSTS}
      username: ${ES_USERNAME}
      password: ${ES_PASSWORD}
    kafka:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
```

### 2. Elasticsearch Deployment

```yaml
# elasticsearch-master.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: elasticsearch-master
  namespace: logging-system
spec:
  serviceName: elasticsearch-master
  replicas: 3
  selector:
    matchLabels:
      app: elasticsearch-master
  template:
    metadata:
      labels:
        app: elasticsearch-master
    spec:
      initContainers:
      - name: fix-permissions
        image: busybox
        command: ["sh", "-c", "chown -R 1000:1000 /usr/share/elasticsearch/data"]
        volumeMounts:
        - name: data
          mountPath: /usr/share/elasticsearch/data
      containers:
      - name: elasticsearch
        image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
        resources:
          requests:
            memory: "4Gi"
            cpu: "2"
          limits:
            memory: "8Gi"
            cpu: "4"
        ports:
        - containerPort: 9200
          name: http
        - containerPort: 9300
          name: transport
        env:
        - name: cluster.name
          value: logs-cluster
        - name: node.name
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: discovery.seed_hosts
          value: "elasticsearch-master-0.elasticsearch-master,elasticsearch-master-1.elasticsearch-master,elasticsearch-master-2.elasticsearch-master"
        - name: cluster.initial_master_nodes
          value: "elasticsearch-master-0,elasticsearch-master-1,elasticsearch-master-2"
        - name: ES_JAVA_OPTS
          value: "-Xms4g -Xmx4g"
        volumeMounts:
        - name: data
          mountPath: /usr/share/elasticsearch/data
  volumeClaimTemplates:
  - metadata:
      name: data
    spec:
      accessModes: ["ReadWriteOnce"]
      storageClassName: fast-ssd
      resources:
        requests:
          storage: 100Gi
```

### 3. Logging Service Deployment

```yaml
# logging-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: logging-service
  namespace: logging-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: logging-service
  template:
    metadata:
      labels:
        app: logging-service
    spec:
      containers:
      - name: logging-service
        image: com.exalt/logging-service:latest
        ports:
        - containerPort: 8086
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: ES_HOSTS
          value: "elasticsearch-master:9200"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-0.kafka-headless:9092,kafka-1.kafka-headless:9092,kafka-2.kafka-headless:9092"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8086
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8086
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 4. Service Definitions

```yaml
# services.yaml
apiVersion: v1
kind: Service
metadata:
  name: elasticsearch-master
  namespace: logging-system
spec:
  clusterIP: None
  selector:
    app: elasticsearch-master
  ports:
  - port: 9200
    name: http
  - port: 9300
    name: transport

---
apiVersion: v1
kind: Service
metadata:
  name: logging-service
  namespace: logging-system
spec:
  selector:
    app: logging-service
  ports:
  - port: 8086
    targetPort: 8086
  type: LoadBalancer
```

### 5. Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace logging-system

# Deploy Elasticsearch
kubectl apply -f k8s/elasticsearch/

# Wait for Elasticsearch to be ready
kubectl wait --for=condition=ready pod -l app=elasticsearch-master -n logging-system --timeout=300s

# Deploy Kafka
kubectl apply -f k8s/kafka/

# Deploy Logging Service
kubectl apply -f k8s/logging-service/

# Verify deployment
kubectl get all -n logging-system
```

## Component Configuration

### 1. Logstash Pipeline Configuration

```ruby
# logstash/pipeline/logstash.conf
input {
  kafka {
    bootstrap_servers => "${KAFKA_BOOTSTRAP_SERVERS}"
    topics => ["logs-application", "logs-access", "logs-error", "logs-audit"]
    group_id => "logstash-consumers"
    codec => "json"
    consumer_threads => 4
    decorate_events => true
  }
  
  beats {
    port => 5044
    ssl => false
  }
  
  http {
    port => 5000
    codec => "json"
  }
}

filter {
  # Add timestamp if missing
  if ![timestamp] {
    ruby {
      code => "event.set('timestamp', Time.now.utc.iso8601(3))"
    }
  }
  
  # Parse log levels
  if [level] {
    mutate {
      uppercase => ["level"]
    }
  }
  
  # Extract service metadata
  if [docker] {
    mutate {
      add_field => {
        "service" => "%{[docker][container][labels][com.exalt.service]}"
        "version" => "%{[docker][container][labels][com.exalt.version]}"
      }
    }
  }
  
  # GeoIP enrichment
  if [client_ip] {
    geoip {
      source => "client_ip"
      target => "geoip"
      database => "/usr/share/logstash/GeoLite2-City.mmdb"
    }
  }
  
  # Security filtering
  mutate {
    gsub => [
      "message", "password[\"':=]\\s*[\"']?[^\"'\\s]+[\"']?", "password=***",
      "message", "token[\"':=]\\s*[\"']?[^\"'\\s]+[\"']?", "token=***",
      "message", "api[-_]?key[\"':=]\\s*[\"']?[^\"'\\s]+[\"']?", "api_key=***"
    ]
  }
  
  # Add processing metadata
  mutate {
    add_field => {
      "[@metadata][target_index]" => "logs-%{[service]}-%{+YYYY.MM.dd}"
      "processed_at" => "%{[@timestamp]}"
      "processor_version" => "1.0.0"
    }
  }
}

output {
  elasticsearch {
    hosts => ["${ES_HOSTS}"]
    index => "%{[@metadata][target_index]}"
    template_name => "logs"
    template => "/usr/share/logstash/templates/logs-template.json"
    template_overwrite => true
    
    # Authentication
    user => "${ES_USERNAME}"
    password => "${ES_PASSWORD}"
    
    # Performance settings
    workers => 4
    flush_size => 1000
    idle_flush_time => 5
  }
  
  # Send errors to dedicated index
  if [level] == "ERROR" {
    elasticsearch {
      hosts => ["${ES_HOSTS}"]
      index => "logs-errors-%{+YYYY.MM.dd}"
      user => "${ES_USERNAME}"
      password => "${ES_PASSWORD}"
    }
  }
  
  # Monitoring output
  if [@metadata][beat] {
    stdout {
      codec => dots
    }
  }
}
```

### 2. Filebeat Configuration

```yaml
# filebeat/filebeat.yml
filebeat.inputs:
- type: container
  paths:
    - '/var/lib/docker/containers/*/*.log'
  processors:
    - add_docker_metadata:
        host: "unix:///var/run/docker.sock"
    - decode_json_fields:
        fields: ["message"]
        target: "json"
        overwrite_keys: true
    - drop_event:
        when:
          regexp:
            message: "^\\s*$"

- type: log
  enabled: true
  paths:
    - /var/log/apps/*.log
  multiline:
    pattern: '^\d{4}-\d{2}-\d{2}'
    negate: true
    match: after
  processors:
    - add_fields:
        target: ''
        fields:
          log_type: application

output.kafka:
  hosts: ["${KAFKA_HOSTS}"]
  topic: 'logs-%{[fields.log_type]}'
  partition.round_robin:
    reachable_only: false
  required_acks: 1
  compression: gzip
  max_message_bytes: 1000000

processors:
  - add_host_metadata:
      when.not.contains.tags: forwarded
  - add_kubernetes_metadata:
      in_cluster: true
      
monitoring:
  enabled: true
  elasticsearch:
    hosts: ["${ES_HOSTS}"]
```

### 3. Kibana Configuration

```yaml
# kibana/kibana.yml
server.name: kibana
server.host: "0.0.0.0"
server.port: 5601

elasticsearch.hosts: ["${ELASTICSEARCH_HOSTS}"]
elasticsearch.username: "${ELASTICSEARCH_USERNAME}"
elasticsearch.password: "${ELASTICSEARCH_PASSWORD}"

elasticsearch.ssl.verificationMode: none

# Logging configuration
logging:
  appenders:
    console:
      type: console
      layout:
        type: pattern
        pattern: "[%date] [%level] [%logger] %message"
  root:
    level: info

# Security
xpack.security.enabled: true
xpack.encryptedSavedObjects.encryptionKey: "${KIBANA_ENCRYPTION_KEY}"

# Monitoring
monitoring.ui.enabled: true
monitoring.ui.container.elasticsearch.enabled: true

# Advanced Settings
elasticsearch.requestTimeout: 30000
elasticsearch.shardTimeout: 30000

# Saved Objects
savedObjects.maxImportExportSize: 10000
```

## Security Setup

### 1. SSL/TLS Configuration

```bash
# Generate certificates
docker run --rm -v $(pwd)/certs:/certs \
  docker.elastic.co/elasticsearch/elasticsearch:8.10.0 \
  bin/elasticsearch-certutil ca --out /certs/elastic-ca.p12 --pass ""

docker run --rm -v $(pwd)/certs:/certs \
  docker.elastic.co/elasticsearch/elasticsearch:8.10.0 \
  bin/elasticsearch-certutil cert --ca /certs/elastic-ca.p12 --out /certs/elastic-certificates.p12 --pass ""

# Configure Elasticsearch with SSL
elasticsearch:
  xpack:
    security:
      enabled: true
      transport:
        ssl:
          enabled: true
          verification_mode: certificate
          keystore:
            path: elastic-certificates.p12
          truststore:
            path: elastic-certificates.p12
```

### 2. User Authentication

```bash
# Set built-in user passwords
docker exec -it elasticsearch-master \
  bin/elasticsearch-setup-passwords interactive

# Create custom roles
curl -X PUT "localhost:9200/_security/role/logs_reader" \
  -H "Content-Type: application/json" \
  -u elastic:password \
  -d '{
    "cluster": ["monitor"],
    "indices": [{
      "names": ["logs-*"],
      "privileges": ["read", "view_index_metadata"]
    }]
  }'

# Create users
curl -X PUT "localhost:9200/_security/user/log_viewer" \
  -H "Content-Type: application/json" \
  -u elastic:password \
  -d '{
    "password": "viewer_password",
    "roles": ["logs_reader"],
    "full_name": "Log Viewer"
  }'
```

### 3. API Security

```java
// API Key Configuration
@Configuration
public class ApiKeyConfig {
    @Bean
    public ApiKeyAuthenticationFilter apiKeyFilter() {
        return new ApiKeyAuthenticationFilter();
    }
}

// Generate API Key
curl -X POST "localhost:8086/api/v1/auth/api-key" \
  -H "Content-Type: application/json" \
  -u admin:password \
  -d '{
    "name": "monitoring-service",
    "permissions": ["logs:read", "logs:write"]
  }'
```

## Performance Tuning

### 1. Elasticsearch Optimization

```yaml
# JVM Heap Settings
ES_JAVA_OPTS: "-Xms8g -Xmx8g"

# Thread Pool Configuration
thread_pool:
  write:
    size: 10
    queue_size: 1000
  search:
    size: 30
    queue_size: 1000

# Index Settings
index:
  number_of_shards: 3
  number_of_replicas: 1
  refresh_interval: 30s
  translog:
    durability: async
    sync_interval: 30s
```

### 2. Kafka Optimization

```properties
# Broker Configuration
num.network.threads=8
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600

# Log Configuration
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000

# Performance
compression.type=lz4
batch.size=16384
linger.ms=10
```

### 3. Application Tuning

```yaml
# Spring Boot Configuration
spring:
  kafka:
    producer:
      batch-size: 16384
      linger-ms: 10
      compression-type: lz4
    consumer:
      max-poll-records: 500
      fetch-min-size: 1
      
# Thread Pool Configuration
logging:
  thread-pool:
    core-size: 10
    max-size: 50
    queue-capacity: 1000
```

## Troubleshooting

### Common Issues

#### 1. Elasticsearch Won't Start
```bash
# Check logs
docker logs elasticsearch-master

# Common fixes:
# - Increase vm.max_map_count
sudo sysctl -w vm.max_map_count=262144

# - Fix permissions
sudo chown -R 1000:1000 /var/lib/elasticsearch

# - Check ulimits
ulimit -n 65536
```

#### 2. Kafka Connection Issues
```bash
# Test Kafka connectivity
kafka-console-producer --broker-list localhost:9092 --topic test

# List topics
kafka-topics --bootstrap-server localhost:9092 --list

# Check consumer groups
kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

#### 3. Logstash Pipeline Errors
```bash
# Test configuration
logstash -f logstash.conf --config.test_and_exit

# Debug mode
logstash -f logstash.conf --debug

# Check pipeline statistics
curl -X GET "localhost:9600/_node/stats/pipelines?pretty"
```

#### 4. Performance Issues
```bash
# Check Elasticsearch cluster health
curl -X GET "localhost:9200/_cluster/health?pretty"

# Check node statistics
curl -X GET "localhost:9200/_nodes/stats?pretty"

# Check hot threads
curl -X GET "localhost:9200/_nodes/hot_threads"

# Monitor indexing rate
watch -n 1 'curl -s localhost:9200/_cat/indices?v | grep logs'
```

### Debug Commands

```bash
# Elasticsearch
curl -X GET "localhost:9200/_cat/nodes?v"
curl -X GET "localhost:9200/_cat/indices?v"
curl -X GET "localhost:9200/_cat/shards?v"
curl -X GET "localhost:9200/_cluster/allocation/explain?pretty"

# Kafka
kafka-consumer-groups --bootstrap-server localhost:9092 --group logstash-consumers --describe
kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic logs-application

# Application logs
tail -f logs/logging-service.log | jq '.'
```

## Maintenance Tasks

### Daily Tasks
- Monitor disk usage
- Check index health
- Review error logs
- Verify backup completion

### Weekly Tasks
- Optimize indices
- Update GeoIP database
- Review and tune slow queries
- Clean up old indices

### Monthly Tasks
- Security audit
- Performance review
- Capacity planning
- Update dependencies

## Next Steps

1. Configure alerting rules in Elastalert
2. Set up Grafana dashboards
3. Implement log retention policies
4. Configure cross-cluster replication
5. Set up automated backups
6. Integrate with APM tools
7. Configure machine learning jobs
8. Set up audit logging

For production deployment, ensure you have:
- Proper SSL/TLS certificates
- Strong passwords for all users
- Network security policies
- Backup and recovery procedures
- Monitoring and alerting
- Capacity planning
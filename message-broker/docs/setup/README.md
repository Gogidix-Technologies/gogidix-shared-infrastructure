# Setup Guide - Message Broker Service

## Overview

This guide provides comprehensive instructions for setting up the Message Broker Service (Apache Kafka) including local development, Docker deployment, Kubernetes installation, and production configuration.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Configuration Management](#configuration-management)
6. [Security Setup](#security-setup)
7. [Performance Tuning](#performance-tuning)
8. [Monitoring Setup](#monitoring-setup)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **CPU**: Minimum 8 cores (16+ recommended for production)
- **RAM**: Minimum 16GB (32GB+ recommended for production)
- **Storage**: 500GB+ SSD with high IOPS
- **Network**: Low latency, high bandwidth network
- **OS**: Linux (Ubuntu 20.04+ or RHEL 8+)

### Software Requirements

```bash
# Java Runtime
- Java 11 or 17 (OpenJDK recommended)

# Container Runtime
- Docker 24.0+
- Docker Compose 2.20+
- Kubernetes 1.28+ (for production)

# Development Tools
- Maven 3.8+
- Git 2.40+
- kubectl 1.28+
- helm 3.12+

# Monitoring Tools
- Prometheus 2.45+
- Grafana 10.0+
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt-tech/message-broker.git
cd message-broker
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
# Kafka Configuration
KAFKA_CLUSTER_ID=exalt-kafka-cluster
KAFKA_BROKER_ID=1
KAFKA_ZOOKEEPER_CONNECT=localhost:2181
KAFKA_LISTENERS=PLAINTEXT://localhost:9092,SSL://localhost:9093
KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,SSL://localhost:9093
KAFKA_LOG_DIRS=/var/kafka/logs
KAFKA_NUM_PARTITIONS=3
KAFKA_DEFAULT_REPLICATION_FACTOR=1
KAFKA_MIN_INSYNC_REPLICAS=1

# Zookeeper Configuration
ZOOKEEPER_CLIENT_PORT=2181
ZOOKEEPER_TICK_TIME=2000
ZOOKEEPER_INIT_LIMIT=10
ZOOKEEPER_SYNC_LIMIT=5

# Schema Registry Configuration
SCHEMA_REGISTRY_HOST=localhost
SCHEMA_REGISTRY_PORT=8081
SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL=localhost:2181
SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS=PLAINTEXT://localhost:9092

# Kafka Connect Configuration
CONNECT_BOOTSTRAP_SERVERS=localhost:9092
CONNECT_REST_PORT=8083
CONNECT_GROUP_ID=kafka-connect-cluster
CONNECT_CONFIG_STORAGE_TOPIC=_connect-configs
CONNECT_OFFSET_STORAGE_TOPIC=_connect-offsets
CONNECT_STATUS_STORAGE_TOPIC=_connect-status

# Security Configuration
KAFKA_SSL_KEYSTORE_LOCATION=/var/kafka/ssl/kafka.server.keystore.jks
KAFKA_SSL_KEYSTORE_PASSWORD=changeme
KAFKA_SSL_KEY_PASSWORD=changeme
KAFKA_SSL_TRUSTSTORE_LOCATION=/var/kafka/ssl/kafka.server.truststore.jks
KAFKA_SSL_TRUSTSTORE_PASSWORD=changeme

# Performance Configuration
KAFKA_HEAP_SIZE=4G
KAFKA_NUM_NETWORK_THREADS=8
KAFKA_NUM_IO_THREADS=8
KAFKA_SOCKET_SEND_BUFFER_BYTES=102400
KAFKA_SOCKET_RECEIVE_BUFFER_BYTES=102400
KAFKA_SOCKET_REQUEST_MAX_BYTES=104857600

# Monitoring
KAFKA_JMX_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost -Dcom.sun.management.jmxremote.port=9999
KAFKA_JMX_PORT=9999
```

### 3. Start Zookeeper

```bash
# Using Docker
docker run -d \
  --name zookeeper \
  -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  -e ZOOKEEPER_TICK_TIME=2000 \
  confluentinc/cp-zookeeper:7.4.0

# Or using local installation
bin/zookeeper-server-start.sh config/zookeeper.properties
```

### 4. Start Kafka Broker

```bash
# Using Docker
docker run -d \
  --name kafka \
  -p 9092:9092 \
  -p 9093:9093 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,SSL://localhost:9093 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.4.0

# Or using local installation
bin/kafka-server-start.sh config/server.properties
```

### 5. Create Initial Topics

```bash
# Create application topics
./scripts/create-topics.sh

# Or manually
kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic order.events \
  --partitions 6 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config compression.type=lz4

kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic payment.events \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=2592000000

kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic user.events \
  --partitions 6 \
  --replication-factor 1 \
  --config retention.ms=7776000000
```

### 6. Start Schema Registry

```bash
# Using Docker
docker run -d \
  --name schema-registry \
  -p 8081:8081 \
  -e SCHEMA_REGISTRY_HOST_NAME=schema-registry \
  -e SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-schema-registry:7.4.0

# Register schemas
curl -X POST http://localhost:8081/subjects/order-events-value/versions \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d @schemas/order-event.avsc
```

### 7. Verify Installation

```bash
# Check Kafka cluster
kafka-metadata-shell --snapshot /var/kafka/logs/__cluster_metadata-0/00000000000000000000.log --print-brokers

# List topics
kafka-topics --list --bootstrap-server localhost:9092

# Test producer
kafka-console-producer --bootstrap-server localhost:9092 --topic test

# Test consumer
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning

# Check Schema Registry
curl http://localhost:8081/subjects
```

## Docker Setup

### 1. Complete Docker Compose Configuration

```yaml
version: '3.8'

services:
  zookeeper-1:
    image: confluentinc/cp-zookeeper:7.4.0
    hostname: zookeeper-1
    container_name: zookeeper-1
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_SERVER_ID: 1
      ZOOKEEPER_SERVERS: zookeeper-1:2888:3888;zookeeper-2:2888:3888;zookeeper-3:2888:3888
    volumes:
      - zookeeper-1-data:/var/lib/zookeeper/data
      - zookeeper-1-logs:/var/lib/zookeeper/logs
    networks:
      - kafka-network

  zookeeper-2:
    image: confluentinc/cp-zookeeper:7.4.0
    hostname: zookeeper-2
    container_name: zookeeper-2
    ports:
      - "2182:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_SERVER_ID: 2
      ZOOKEEPER_SERVERS: zookeeper-1:2888:3888;zookeeper-2:2888:3888;zookeeper-3:2888:3888
    volumes:
      - zookeeper-2-data:/var/lib/zookeeper/data
      - zookeeper-2-logs:/var/lib/zookeeper/logs
    networks:
      - kafka-network

  zookeeper-3:
    image: confluentinc/cp-zookeeper:7.4.0
    hostname: zookeeper-3
    container_name: zookeeper-3
    ports:
      - "2183:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_SERVER_ID: 3
      ZOOKEEPER_SERVERS: zookeeper-1:2888:3888;zookeeper-2:2888:3888;zookeeper-3:2888:3888
    volumes:
      - zookeeper-3-data:/var/lib/zookeeper/data
      - zookeeper-3-logs:/var/lib/zookeeper/logs
    networks:
      - kafka-network

  kafka-1:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka-1
    container_name: kafka-1
    depends_on:
      - zookeeper-1
      - zookeeper-2
      - zookeeper-3
    ports:
      - "9092:9092"
      - "19092:19092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper-1:2181,zookeeper-2:2181,zookeeper-3:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-1:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      KAFKA_NUM_PARTITIONS: 3
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_MIN_INSYNC_REPLICAS: 2
      KAFKA_LOG_RETENTION_HOURS: 168
      KAFKA_LOG_SEGMENT_BYTES: 1073741824
      KAFKA_LOG_RETENTION_CHECK_INTERVAL_MS: 300000
      KAFKA_COMPRESSION_TYPE: producer
    volumes:
      - kafka-1-data:/var/lib/kafka/data
    networks:
      - kafka-network

  kafka-2:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka-2
    container_name: kafka-2
    depends_on:
      - zookeeper-1
      - zookeeper-2
      - zookeeper-3
    ports:
      - "9093:9092"
      - "19093:19093"
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper-1:2181,zookeeper-2:2181,zookeeper-3:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-2:29093,PLAINTEXT_HOST://localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
      KAFKA_LOG_DIRS: /var/lib/kafka/data
    volumes:
      - kafka-2-data:/var/lib/kafka/data
    networks:
      - kafka-network

  kafka-3:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka-3
    container_name: kafka-3
    depends_on:
      - zookeeper-1
      - zookeeper-2
      - zookeeper-3
    ports:
      - "9094:9092"
      - "19094:19094"
    environment:
      KAFKA_BROKER_ID: 3
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper-1:2181,zookeeper-2:2181,zookeeper-3:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-3:29094,PLAINTEXT_HOST://localhost:9094
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
      KAFKA_LOG_DIRS: /var/lib/kafka/data
    volumes:
      - kafka-3-data:/var/lib/kafka/data
    networks:
      - kafka-network

  schema-registry:
    image: confluentinc/cp-schema-registry:7.4.0
    hostname: schema-registry
    container_name: schema-registry
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: 'kafka-1:29092,kafka-2:29093,kafka-3:29094'
      SCHEMA_REGISTRY_LISTENERS: http://0.0.0.0:8081
    networks:
      - kafka-network

  kafka-connect:
    image: confluentinc/cp-kafka-connect:7.4.0
    hostname: kafka-connect
    container_name: kafka-connect
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
      - schema-registry
    ports:
      - "8083:8083"
    environment:
      CONNECT_BOOTSTRAP_SERVERS: 'kafka-1:29092,kafka-2:29093,kafka-3:29094'
      CONNECT_REST_ADVERTISED_HOST_NAME: kafka-connect
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: compose-connect-group
      CONNECT_CONFIG_STORAGE_TOPIC: docker-connect-configs
      CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR: 3
      CONNECT_OFFSET_FLUSH_INTERVAL_MS: 10000
      CONNECT_OFFSET_STORAGE_TOPIC: docker-connect-offsets
      CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR: 3
      CONNECT_STATUS_STORAGE_TOPIC: docker-connect-status
      CONNECT_STATUS_STORAGE_REPLICATION_FACTOR: 3
      CONNECT_KEY_CONVERTER: org.apache.kafka.connect.storage.StringConverter
      CONNECT_VALUE_CONVERTER: io.confluent.connect.avro.AvroConverter
      CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL: http://schema-registry:8081
      CONNECT_PLUGIN_PATH: "/usr/share/java,/usr/share/confluent-hub-components"
      CONNECT_LOG4J_LOGGERS: org.apache.zookeeper=ERROR,org.I0Itec.zkclient=ERROR,org.reflections=ERROR
    volumes:
      - ./connectors:/usr/share/confluent-hub-components
    networks:
      - kafka-network

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: exalt-kafka
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka-1:29092,kafka-2:29093,kafka-3:29094
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper-1:2181,zookeeper-2:2181,zookeeper-3:2181
      KAFKA_CLUSTERS_0_SCHEMAREGISTRY: http://schema-registry:8081
      KAFKA_CLUSTERS_0_KAFKACONNECT_0_NAME: kafka-connect
      KAFKA_CLUSTERS_0_KAFKACONNECT_0_ADDRESS: http://kafka-connect:8083
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
      - schema-registry
      - kafka-connect
    networks:
      - kafka-network

volumes:
  zookeeper-1-data:
  zookeeper-1-logs:
  zookeeper-2-data:
  zookeeper-2-logs:
  zookeeper-3-data:
  zookeeper-3-logs:
  kafka-1-data:
  kafka-2-data:
  kafka-3-data:

networks:
  kafka-network:
    driver: bridge
```

### 2. Start Docker Environment

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Scale kafka brokers
docker-compose up -d --scale kafka=5

# Stop services
docker-compose down

# Remove volumes
docker-compose down -v
```

## Kubernetes Deployment

### 1. Install Kafka Operator

```bash
# Add Strimzi Helm repository
helm repo add strimzi https://strimzi.io/charts/
helm repo update

# Install Strimzi operator
kubectl create namespace kafka
helm install strimzi-kafka-operator strimzi/strimzi-kafka-operator \
  --namespace kafka \
  --version 0.36.0
```

### 2. Deploy Kafka Cluster

```yaml
# kafka-cluster.yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: exalt-kafka-cluster
  namespace: kafka
spec:
  kafka:
    version: 3.5.0
    replicas: 3
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: tls
        port: 9093
        type: internal
        tls: true
      - name: external
        port: 9094
        type: loadbalancer
        tls: true
    config:
      offsets.topic.replication.factor: 3
      transaction.state.log.replication.factor: 3
      transaction.state.log.min.isr: 2
      default.replication.factor: 3
      min.insync.replicas: 2
      inter.broker.protocol.version: "3.5"
      log.retention.hours: 168
      log.segment.bytes: 1073741824
      compression.type: "producer"
    storage:
      type: persistent-claim
      size: 100Gi
      class: fast-ssd
      deleteClaim: false
    resources:
      requests:
        memory: 8Gi
        cpu: "2"
      limits:
        memory: 16Gi
        cpu: "4"
    jvmOptions:
      -Xms: 6g
      -Xmx: 6g
    metricsConfig:
      type: jmxPrometheusExporter
      valueFrom:
        configMapKeyRef:
          name: kafka-metrics
          key: kafka-metrics-config.yml
  zookeeper:
    replicas: 3
    storage:
      type: persistent-claim
      size: 20Gi
      class: fast-ssd
      deleteClaim: false
    resources:
      requests:
        memory: 2Gi
        cpu: "1"
      limits:
        memory: 4Gi
        cpu: "2"
  entityOperator:
    topicOperator:
      resources:
        requests:
          memory: 512Mi
          cpu: "0.5"
        limits:
          memory: 1Gi
          cpu: "1"
    userOperator:
      resources:
        requests:
          memory: 512Mi
          cpu: "0.5"
        limits:
          memory: 1Gi
          cpu: "1"
```

### 3. Deploy Schema Registry

```yaml
# schema-registry.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: schema-registry
  namespace: kafka
spec:
  replicas: 2
  selector:
    matchLabels:
      app: schema-registry
  template:
    metadata:
      labels:
        app: schema-registry
    spec:
      containers:
      - name: schema-registry
        image: confluentinc/cp-schema-registry:7.4.0
        ports:
        - containerPort: 8081
        env:
        - name: SCHEMA_REGISTRY_HOST_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS
          value: "exalt-kafka-cluster-kafka-bootstrap:9092"
        - name: SCHEMA_REGISTRY_LISTENERS
          value: "http://0.0.0.0:8081"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1"
        livenessProbe:
          httpGet:
            path: /
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: schema-registry
  namespace: kafka
spec:
  selector:
    app: schema-registry
  ports:
  - port: 8081
    targetPort: 8081
  type: ClusterIP
```

### 4. Deploy Kafka Connect

```yaml
# kafka-connect.yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaConnect
metadata:
  name: exalt-kafka-connect
  namespace: kafka
  annotations:
    strimzi.io/use-connector-resources: "true"
spec:
  version: 3.5.0
  replicas: 3
  bootstrapServers: exalt-kafka-cluster-kafka-bootstrap:9093
  tls:
    trustedCertificates:
      - secretName: exalt-kafka-cluster-cluster-ca-cert
        certificate: ca.crt
  config:
    group.id: kafka-connect-cluster
    offset.storage.topic: connect-cluster-offsets
    config.storage.topic: connect-cluster-configs
    status.storage.topic: connect-cluster-status
    config.storage.replication.factor: 3
    offset.storage.replication.factor: 3
    status.storage.replication.factor: 3
    key.converter: org.apache.kafka.connect.storage.StringConverter
    value.converter: io.confluent.connect.avro.AvroConverter
    value.converter.schema.registry.url: http://schema-registry:8081
  resources:
    requests:
      memory: 2Gi
      cpu: "1"
    limits:
      memory: 4Gi
      cpu: "2"
  jvmOptions:
    -Xms: 1g
    -Xmx: 2g
  build:
    output:
      type: docker
      image: your-registry.com/kafka-connect-custom:latest
    plugins:
      - name: debezium-postgres
        artifacts:
          - type: tgz
            url: https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/2.3.0.Final/debezium-connector-postgres-2.3.0.Final-plugin.tar.gz
      - name: mongodb-connector
        artifacts:
          - type: maven
            group: org.mongodb.kafka
            artifact: mongo-kafka-connect
            version: 1.10.0
```

### 5. Deploy to Kubernetes

```bash
# Create metrics ConfigMap
kubectl create configmap kafka-metrics \
  --from-file=kafka-metrics-config.yml \
  -n kafka

# Deploy Kafka cluster
kubectl apply -f kafka-cluster.yaml

# Wait for cluster to be ready
kubectl wait kafka/exalt-kafka-cluster --for=condition=Ready --timeout=300s -n kafka

# Deploy Schema Registry
kubectl apply -f schema-registry.yaml

# Deploy Kafka Connect
kubectl apply -f kafka-connect.yaml

# Create topics
kubectl apply -f topics.yaml

# Verify deployment
kubectl get kafka,kafkatopic,kafkaconnect -n kafka
```

## Configuration Management

### 1. Broker Configuration

```properties
# server.properties
broker.id=1
listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
advertised.listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL

# Log Configuration
log.dirs=/var/kafka/logs
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
log.cleanup.policy=delete

# Replication
default.replication.factor=3
min.insync.replicas=2
unclean.leader.election.enable=false

# Performance
num.network.threads=8
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
num.partitions=3

# Compression
compression.type=producer

# Internal Topics
offsets.topic.replication.factor=3
transaction.state.log.replication.factor=3
transaction.state.log.min.isr=2

# Group Coordinator
group.initial.rebalance.delay.ms=3000

# Security
security.inter.broker.protocol=SSL
ssl.keystore.location=/var/kafka/ssl/kafka.server.keystore.jks
ssl.keystore.password=changeme
ssl.key.password=changeme
ssl.truststore.location=/var/kafka/ssl/kafka.server.truststore.jks
ssl.truststore.password=changeme
ssl.client.auth=required
ssl.endpoint.identification.algorithm=https
```

### 2. Producer Configuration

```java
Properties props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);

// Performance
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864);

// Reliability
props.put(ProducerConfig.ACKS_CONFIG, "all");
props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

// Schema Registry
props.put("schema.registry.url", "http://localhost:8081");
```

### 3. Consumer Configuration

```java
Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroDeserializer.class);

// Performance
props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

// Session Management
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);

// Offset Management
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

// Schema Registry
props.put("schema.registry.url", "http://localhost:8081");
props.put("specific.avro.reader", true);
```

## Security Setup

### 1. SSL/TLS Configuration

```bash
# Generate CA certificate
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365

# Generate broker certificates
keytool -keystore kafka.server.keystore.jks -alias localhost -validity 365 -genkey -keyalg RSA
keytool -keystore kafka.server.keystore.jks -alias localhost -certreq -file cert-file
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial
keytool -keystore kafka.server.keystore.jks -alias CARoot -import -file ca-cert
keytool -keystore kafka.server.keystore.jks -alias localhost -import -file cert-signed

# Create truststore
keytool -keystore kafka.server.truststore.jks -alias CARoot -import -file ca-cert

# Client certificates
keytool -keystore kafka.client.keystore.jks -alias localhost -validity 365 -genkey -keyalg RSA
keytool -keystore kafka.client.keystore.jks -alias localhost -certreq -file client-cert-file
openssl x509 -req -CA ca-cert -CAkey ca-key -in client-cert-file -out client-cert-signed -days 365 -CAcreateserial
keytool -keystore kafka.client.keystore.jks -alias CARoot -import -file ca-cert
keytool -keystore kafka.client.keystore.jks -alias localhost -import -file client-cert-signed
```

### 2. SASL Configuration

```properties
# server.properties
listeners=SASL_SSL://localhost:9093
security.inter.broker.protocol=SASL_SSL
sasl.mechanism.inter.broker.protocol=SCRAM-SHA-512
sasl.enabled.mechanisms=SCRAM-SHA-512

# JAAS configuration
listener.name.sasl_ssl.scram-sha-512.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required \
  username="admin" \
  password="admin-secret";
```

```bash
# Create SCRAM credentials
kafka-configs --zookeeper localhost:2181 --alter \
  --add-config 'SCRAM-SHA-512=[password=admin-secret]' \
  --entity-type users --entity-name admin
```

### 3. ACL Configuration

```bash
# Producer ACLs
kafka-acls --bootstrap-server localhost:9093 \
  --command-config admin.properties \
  --add --allow-principal User:producer-app \
  --operation Write --operation Describe \
  --topic order-events

# Consumer ACLs
kafka-acls --bootstrap-server localhost:9093 \
  --command-config admin.properties \
  --add --allow-principal User:consumer-app \
  --operation Read --operation Describe \
  --topic order-events \
  --group consumer-group

# Admin ACLs
kafka-acls --bootstrap-server localhost:9093 \
  --command-config admin.properties \
  --add --allow-principal User:admin \
  --operation All --cluster
```

## Performance Tuning

### 1. OS Tuning

```bash
# /etc/sysctl.conf
# Network tuning
net.core.wmem_default=131072
net.core.rmem_default=131072
net.core.wmem_max=2097152
net.core.rmem_max=2097152
net.ipv4.tcp_wmem=4096 65536 2097152
net.ipv4.tcp_rmem=4096 65536 2097152

# File descriptors
fs.file-max=100000

# Virtual memory
vm.swappiness=1
vm.dirty_background_ratio=5
vm.dirty_ratio=15

# Apply settings
sysctl -p
```

### 2. JVM Tuning

```bash
# Kafka JVM options
export KAFKA_HEAP_OPTS="-Xms8g -Xmx8g"
export KAFKA_JVM_PERFORMANCE_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -XX:+AlwaysPreTouch"
```

### 3. Disk Layout

```bash
# Multiple log directories for parallel I/O
log.dirs=/mnt/kafka-disk1/logs,/mnt/kafka-disk2/logs,/mnt/kafka-disk3/logs

# XFS filesystem with optimizations
mkfs.xfs -f -d agcount=16 -l size=512m /dev/nvme1n1
mount -o noatime,nodiratime,nobarrier /dev/nvme1n1 /mnt/kafka-disk1
```

## Monitoring Setup

### 1. Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka-1:9090', 'kafka-2:9090', 'kafka-3:9090']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        regex: '([^:]+):.*'
        replacement: '${1}'

  - job_name: 'kafka-jmx'
    static_configs:
      - targets: ['kafka-jmx-exporter:5556']
```

### 2. JMX Exporter Configuration

```yaml
# kafka-jmx-exporter.yml
lowercaseOutputName: true
lowercaseOutputLabelNames: true
whitelistObjectNames:
  - kafka.server:type=BrokerTopicMetrics,name=*
  - kafka.server:type=ReplicaManager,name=*
  - kafka.controller:type=KafkaController,name=*
  - kafka.network:type=RequestMetrics,name=*
  - kafka.consumer:type=consumer-fetch-manager-metrics,client-id=*
  - kafka.producer:type=producer-metrics,client-id=*
rules:
  - pattern: kafka.server<type=(.+), name=(.+), topic=(.+)><>Count
    name: kafka_server_$1_$2_total
    type: COUNTER
    labels:
      topic: "$3"
```

### 3. Grafana Dashboard

```json
{
  "dashboard": {
    "title": "Kafka Cluster Monitoring",
    "panels": [
      {
        "title": "Messages In Per Second",
        "targets": [{
          "expr": "sum(rate(kafka_server_BrokerTopicMetrics_MessagesInPerSec_total[5m])) by (topic)"
        }]
      },
      {
        "title": "Consumer Lag",
        "targets": [{
          "expr": "kafka_consumer_lag_sum"
        }]
      },
      {
        "title": "Under Replicated Partitions",
        "targets": [{
          "expr": "kafka_server_ReplicaManager_UnderReplicatedPartitions"
        }]
      },
      {
        "title": "Request Latency",
        "targets": [{
          "expr": "kafka_network_RequestMetrics_TotalTimeMs{quantile=\"0.99\"}"
        }]
      }
    ]
  }
}
```

## Troubleshooting

### Common Issues

#### 1. Leader Election Issues
```bash
# Check partition leaders
kafka-topics --describe --bootstrap-server localhost:9092

# Force leader election
kafka-leader-election --bootstrap-server localhost:9092 \
  --election-type preferred --all-topic-partitions
```

#### 2. Consumer Lag
```bash
# Check consumer lag
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group my-consumer-group

# Reset consumer offset
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group my-consumer-group --reset-offsets \
  --topic my-topic --to-earliest --execute
```

#### 3. Disk Space Issues
```bash
# Check log sizes
du -sh /var/kafka/logs/*

# Force log cleanup
kafka-log-dirs --describe --bootstrap-server localhost:9092

# Delete old segments
kafka-delete-records --bootstrap-server localhost:9092 \
  --offset-json-file delete-records.json
```

### Debug Tools

```bash
# Dump log segments
kafka-dump-log --files /var/kafka/logs/my-topic-0/00000000000000000000.log

# Verify consumer group coordination
kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Check broker metadata
kafka-metadata-shell --snapshot /var/kafka/logs/__cluster_metadata-0/00000000000000000000.log

# Performance testing
kafka-producer-perf-test --topic test-topic \
  --throughput 10000 --record-size 1024 \
  --num-records 1000000 \
  --producer-props bootstrap.servers=localhost:9092

kafka-consumer-perf-test --broker-list localhost:9092 \
  --topic test-topic --messages 1000000
```

## Maintenance Scripts

### 1. Backup Script
```bash
#!/bin/bash
# backup-kafka.sh

# Backup topic configurations
kafka-topics --bootstrap-server localhost:9092 --describe > topics-backup.txt

# Backup consumer offsets
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --all-groups --describe > consumer-groups-backup.txt

# Backup ACLs
kafka-acls --bootstrap-server localhost:9092 --list > acls-backup.txt
```

### 2. Health Check Script
```bash
#!/bin/bash
# health-check.sh

# Check broker health
for broker in kafka-1:9092 kafka-2:9092 kafka-3:9092; do
  echo "Checking $broker..."
  kafka-broker-api-versions --bootstrap-server $broker
done

# Check under-replicated partitions
kafka-topics --bootstrap-server localhost:9092 \
  --describe --under-replicated-partitions

# Check offline partitions
kafka-topics --bootstrap-server localhost:9092 \
  --describe --unavailable-partitions
```

## Next Steps

1. Configure monitoring dashboards
2. Set up alerting rules
3. Implement backup strategy
4. Configure disaster recovery
5. Set up security policies
6. Performance baseline testing
7. Documentation of procedures
8. Training for operations team

For production deployment:
- Review security hardening guide
- Implement network segregation
- Set up audit logging
- Configure resource quotas
- Plan capacity for growth
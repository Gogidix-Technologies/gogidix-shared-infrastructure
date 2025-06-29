# Architecture Documentation - Message Broker Service

## Overview

The Message Broker Service provides reliable, scalable, and distributed messaging infrastructure for the Social E-commerce Ecosystem. Built on Apache Kafka, it enables asynchronous communication, event streaming, and real-time data pipelines across all microservices.

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Component Architecture](#component-architecture)
3. [Data Flow](#data-flow)
4. [Technology Stack](#technology-stack)
5. [Topic Design](#topic-design)
6. [Security Architecture](#security-architecture)
7. [Scalability Design](#scalability-design)
8. [Integration Patterns](#integration-patterns)

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Producer Services                            │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │ Order   │  │Payment  │  │Inventory│  │ User    │  │Analytics│  │
│  │ Service │  │Service  │  │Service  │  │Service  │  │Service  │  │
│  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘  │
└───────┼────────────┼────────────┼────────────┼────────────┼────────┘
        │            │            │            │            │
        ▼            ▼            ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Kafka Cluster (Message Broker)                   │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                        Zookeeper Ensemble                    │   │
│  │  ┌──────────┐    ┌──────────┐    ┌──────────┐             │   │
│  │  │ ZK Node 1│    │ ZK Node 2│    │ ZK Node 3│             │   │
│  │  └──────────┘    └──────────┘    └──────────┘             │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                        Kafka Brokers                         │   │
│  │  ┌──────────┐    ┌──────────┐    ┌──────────┐             │   │
│  │  │ Broker 1 │    │ Broker 2 │    │ Broker 3 │             │   │
│  │  └──────────┘    └──────────┘    └──────────┘             │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                          Topics                              │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐           │   │
│  │  │   Orders   │  │  Payments  │  │ Inventory  │           │   │
│  │  ├────────────┤  ├────────────┤  ├────────────┤           │   │
│  │  │ Partition 0│  │ Partition 0│  │ Partition 0│           │   │
│  │  │ Partition 1│  │ Partition 1│  │ Partition 1│           │   │
│  │  │ Partition 2│  │ Partition 2│  │ Partition 2│           │   │
│  │  └────────────┘  └────────────┘  └────────────┘           │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
        ┌─────────────────────────┴─────────────────────────┐
        ▼                                                   ▼
┌──────────────────┐                              ┌──────────────────┐
│ Consumer Groups  │                              │  Kafka Connect   │
│ ┌──────────────┐ │                              │ ┌──────────────┐ │
│ │Analytics     │ │                              │ │ Source       │ │
│ │Consumers     │ │                              │ │ Connectors   │ │
│ ├──────────────┤ │                              │ ├──────────────┤ │
│ │Notification  │ │                              │ │ Sink         │ │
│ │Consumers     │ │                              │ │ Connectors   │ │
│ └──────────────┘ │                              │ └──────────────┘ │
└──────────────────┘                              └──────────────────┘
```

### Kafka Cluster Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Kafka Broker Details                     │
│                                                             │
│  Broker 1 (Leader)          Broker 2              Broker 3  │
│  ┌─────────────┐           ┌─────────────┐    ┌──────────┐│
│  │ Topic: Orders│           │ Topic: Orders│    │Topic:    ││
│  │ Partition 0 │           │ Partition 1 │    │Orders    ││
│  │ (Leader)    │           │ (Leader)    │    │Part. 2   ││
│  │             │           │             │    │(Leader)  ││
│  │ Partition 1 │           │ Partition 2 │    │          ││
│  │ (Replica)   │           │ (Replica)   │    │Part. 0   ││
│  │             │           │             │    │(Replica) ││
│  │ Partition 2 │           │ Partition 0 │    │          ││
│  │ (Replica)   │           │ (Replica)   │    │Part. 1   ││
│  └─────────────┘           └─────────────┘    └──────────┘│
└─────────────────────────────────────────────────────────────┘
```

## Component Architecture

### Core Components

#### 1. Broker Management Service
```java
@Service
public class BrokerManagementService {
    private final AdminClient adminClient;
    private final MetricsCollector metricsCollector;
    
    public ClusterHealth checkClusterHealth() {
        DescribeClusterResult result = adminClient.describeCluster();
        Collection<Node> nodes = result.nodes().get();
        
        return ClusterHealth.builder()
            .totalBrokers(nodes.size())
            .activeBrokers(getActiveBrokers(nodes))
            .underReplicatedPartitions(getUnderReplicatedPartitions())
            .offlinePartitions(getOfflinePartitions())
            .build();
    }
    
    public void rebalancePartitions() {
        Map<String, Map<TopicPartition, List<Integer>>> reassignments = 
            calculateOptimalReassignment();
        adminClient.alterPartitionReassignments(reassignments);
    }
}
```

#### 2. Producer Service
```java
@Component
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MessageValidator validator;
    private final MessageSerializer serializer;
    
    @Async
    public CompletableFuture<SendResult<String, Object>> sendMessage(
            String topic, String key, Object message) {
        
        // Validate message
        validator.validate(message);
        
        // Add headers
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, message);
        record.headers()
            .add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes())
            .add("service", getServiceName().getBytes())
            .add("version", "1.0".getBytes());
        
        // Send with callback
        return kafkaTemplate.send(record)
            .completable()
            .whenComplete(this::handleResult);
    }
    
    private void handleResult(SendResult<String, Object> result, Throwable ex) {
        if (ex != null) {
            metricsCollector.recordFailure(ex);
            errorHandler.handle(ex);
        } else {
            metricsCollector.recordSuccess(result);
        }
    }
}
```

#### 3. Consumer Service
```java
@Component
public class KafkaConsumerService {
    private final MessageProcessor messageProcessor;
    private final ErrorHandler errorHandler;
    
    @KafkaListener(
        topics = "${kafka.topics}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            ConsumerRecord<String, Object> record,
            Acknowledgment acknowledgment) {
        
        try {
            // Process message
            ProcessingResult result = messageProcessor.process(record);
            
            // Acknowledge if successful
            if (result.isSuccessful()) {
                acknowledgment.acknowledge();
            } else {
                // Handle processing failure
                errorHandler.handleProcessingFailure(record, result);
            }
            
        } catch (Exception e) {
            // Handle exception with retry logic
            handleException(record, e, acknowledgment);
        }
    }
    
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        autoCreateTopics = "true",
        include = {RecoverableException.class}
    )
    private void handleException(
            ConsumerRecord<String, Object> record, 
            Exception e, 
            Acknowledgment acknowledgment) {
        
        if (isRecoverable(e)) {
            throw new RecoverableException("Retryable error", e);
        } else {
            // Send to DLQ
            deadLetterPublisher.publish(record, e);
            acknowledgment.acknowledge();
        }
    }
}
```

#### 4. Schema Registry Integration
```java
@Configuration
public class SchemaRegistryConfig {
    
    @Bean
    public SchemaRegistryClient schemaRegistryClient(
            @Value("${schema.registry.url}") String schemaRegistryUrl) {
        return new CachedSchemaRegistryClient(
            schemaRegistryUrl,
            100,
            Collections.singletonMap(
                SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO"
            )
        );
    }
    
    @Bean
    public KafkaAvroSerializer kafkaAvroSerializer() {
        return new KafkaAvroSerializer(schemaRegistryClient());
    }
    
    @Bean
    public KafkaAvroDeserializer kafkaAvroDeserializer() {
        return new KafkaAvroDeserializer(schemaRegistryClient());
    }
}
```

### Supporting Components

#### Topic Management
```java
@Service
public class TopicManagementService {
    private final AdminClient adminClient;
    
    public void createTopic(TopicConfiguration config) {
        NewTopic newTopic = new NewTopic(
            config.getName(),
            config.getPartitions(),
            config.getReplicationFactor()
        );
        
        Map<String, String> topicConfig = new HashMap<>();
        topicConfig.put(TopicConfig.RETENTION_MS_CONFIG, 
            String.valueOf(config.getRetentionMs()));
        topicConfig.put(TopicConfig.COMPRESSION_TYPE_CONFIG, 
            config.getCompressionType());
        topicConfig.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, 
            String.valueOf(config.getMinInSyncReplicas()));
        
        newTopic.configs(topicConfig);
        
        CreateTopicsResult result = adminClient.createTopics(
            Collections.singletonList(newTopic)
        );
        
        result.all().get();
    }
}
```

#### Monitoring and Metrics
```java
@Component
public class KafkaMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void handleProducerMetrics(ProducerMetricEvent event) {
        meterRegistry.gauge("kafka.producer.record.send.rate", 
            event.getRecordSendRate());
        meterRegistry.gauge("kafka.producer.request.latency.avg", 
            event.getRequestLatencyAvg());
        meterRegistry.gauge("kafka.producer.buffer.available.bytes", 
            event.getBufferAvailableBytes());
    }
    
    @EventListener
    public void handleConsumerMetrics(ConsumerMetricEvent event) {
        meterRegistry.gauge("kafka.consumer.records.consumed.rate", 
            event.getRecordsConsumedRate());
        meterRegistry.gauge("kafka.consumer.records.lag", 
            event.getRecordsLag());
        meterRegistry.gauge("kafka.consumer.fetch.latency.avg", 
            event.getFetchLatencyAvg());
    }
}
```

## Data Flow

### Message Flow Architecture

```
1. Producer publishes message
   ↓
2. Message serialized (Avro/JSON)
   ↓
3. Partitioner determines partition
   ↓
4. Message buffered locally
   ↓
5. Batch sent to leader broker
   ↓
6. Leader writes to log
   ↓
7. Followers replicate
   ↓
8. Leader acknowledges
   ↓
9. Producer receives confirmation
   ↓
10. Consumers poll for messages
   ↓
11. Messages delivered to consumer group
   ↓
12. Consumer processes message
   ↓
13. Consumer commits offset
```

### Event Streaming Pipeline

```yaml
pipeline:
  source:
    - database_changes (CDC)
    - application_events
    - external_apis
    - iot_devices
    
  processing:
    - validation
    - enrichment
    - transformation
    - aggregation
    
  sink:
    - real_time_analytics
    - data_warehouse
    - search_index
    - cache_update
```

## Technology Stack

### Core Technologies

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| Message Broker | Apache Kafka | 3.5.0 | Distributed streaming platform |
| Coordination | Apache Zookeeper | 3.8.2 | Cluster coordination |
| Schema Registry | Confluent Schema Registry | 7.4.0 | Schema management |
| Connect | Kafka Connect | 3.5.0 | Data integration |
| Streams | Kafka Streams | 3.5.0 | Stream processing |
| Monitoring | Prometheus + Grafana | Latest | Metrics and monitoring |

### Client Libraries

```xml
<dependencies>
    <!-- Kafka Client -->
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.5.0</version>
    </dependency>
    
    <!-- Spring Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>3.0.9</version>
    </dependency>
    
    <!-- Avro -->
    <dependency>
        <groupId>org.apache.avro</groupId>
        <artifactId>avro</artifactId>
        <version>1.11.2</version>
    </dependency>
    
    <!-- Schema Registry -->
    <dependency>
        <groupId>io.confluent</groupId>
        <artifactId>kafka-avro-serializer</artifactId>
        <version>7.4.0</version>
    </dependency>
    
    <!-- Kafka Streams -->
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-streams</artifactId>
        <version>3.5.0</version>
    </dependency>
</dependencies>
```

## Topic Design

### Topic Naming Convention

```
{domain}.{entity}.{event-type}.{version}

Examples:
- order.order.created.v1
- payment.transaction.completed.v1
- inventory.stock.updated.v1
- user.profile.changed.v1
```

### Topic Configuration

```yaml
topics:
  order-events:
    partitions: 12
    replication-factor: 3
    retention-ms: 604800000  # 7 days
    compression-type: lz4
    min-insync-replicas: 2
    
  payment-events:
    partitions: 6
    replication-factor: 3
    retention-ms: 2592000000  # 30 days
    compression-type: snappy
    min-insync-replicas: 2
    
  user-events:
    partitions: 18
    replication-factor: 3
    retention-ms: 7776000000  # 90 days
    compression-type: lz4
    min-insync-replicas: 2
```

### Partitioning Strategy

```java
public class CustomPartitioner implements Partitioner {
    
    @Override
    public int partition(
            String topic, 
            Object key, 
            byte[] keyBytes,
            Object value, 
            byte[] valueBytes, 
            Cluster cluster) {
        
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        
        if (keyBytes == null) {
            // Round-robin for null keys
            return ThreadLocalRandom.current().nextInt(numPartitions);
        }
        
        // Use murmur2 hash for better distribution
        return Math.abs(Utils.murmur2(keyBytes)) % numPartitions;
    }
}
```

## Security Architecture

### Authentication & Authorization

```yaml
kafka:
  security:
    protocol: SASL_SSL
    sasl:
      mechanism: SCRAM-SHA-512
      jaas:
        config: |
          org.apache.kafka.common.security.scram.ScramLoginModule required
          username="${KAFKA_USERNAME}"
          password="${KAFKA_PASSWORD}";
    ssl:
      truststore:
        location: /var/kafka/ssl/kafka.client.truststore.jks
        password: ${TRUSTSTORE_PASSWORD}
      keystore:
        location: /var/kafka/ssl/kafka.client.keystore.jks
        password: ${KEYSTORE_PASSWORD}
```

### ACL Configuration

```bash
# Producer ACLs
kafka-acls --bootstrap-server localhost:9092 \
  --add --allow-principal User:order-service \
  --operation Write --topic order.* \
  --command-config client.properties

# Consumer ACLs
kafka-acls --bootstrap-server localhost:9092 \
  --add --allow-principal User:analytics-service \
  --operation Read --topic '*' \
  --group analytics-consumers \
  --command-config client.properties
```

### Encryption

```java
@Configuration
public class EncryptionConfig {
    
    @Bean
    public MessageEncryptor messageEncryptor() {
        return new AESMessageEncryptor(encryptionKey);
    }
    
    @Bean
    public ProducerInterceptor<String, Object> encryptionInterceptor() {
        return new EncryptionProducerInterceptor(messageEncryptor());
    }
    
    @Bean
    public ConsumerInterceptor<String, Object> decryptionInterceptor() {
        return new DecryptionConsumerInterceptor(messageEncryptor());
    }
}
```

## Scalability Design

### Horizontal Scaling

```yaml
scaling:
  brokers:
    initial: 3
    max: 9
    triggers:
      - metric: disk_usage
        threshold: 75%
        action: add_broker
      - metric: network_throughput
        threshold: 80%
        action: add_broker
        
  partitions:
    strategy: dynamic
    min_per_topic: 3
    max_per_topic: 100
    partition_size_target: 1GB
```

### Performance Optimization

```java
@Configuration
public class KafkaPerformanceConfig {
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // Batching
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        
        // Compression
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        
        // Buffer
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864);
        
        // Retries
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new DefaultKafkaProducerFactory<>(props);
    }
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // Fetching
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        
        // Session
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
```

## Integration Patterns

### Event Sourcing

```java
@Service
public class EventSourcingService {
    private final KafkaProducer<String, Event> eventProducer;
    
    public void saveEvent(DomainEvent event) {
        String aggregateId = event.getAggregateId();
        String topic = getTopicForAggregate(event.getAggregateType());
        
        ProducerRecord<String, Event> record = 
            new ProducerRecord<>(topic, aggregateId, event);
            
        eventProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                throw new EventPublishException("Failed to publish event", exception);
            }
        });
    }
    
    public List<Event> getEventHistory(String aggregateId) {
        // Consume all events for aggregate
        return eventStore.getEvents(aggregateId);
    }
}
```

### CQRS Implementation

```java
@Component
public class CQRSEventHandler {
    
    @KafkaListener(topics = "order.order.created.v1")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Update read model
        orderReadModelRepository.save(
            OrderReadModel.fromEvent(event)
        );
        
        // Update search index
        searchIndexer.index(event);
        
        // Update cache
        cacheManager.evict("orders", event.getOrderId());
    }
}
```

### Saga Pattern

```java
@Component
public class OrderSagaOrchestrator {
    
    @KafkaListener(topics = "order.saga.started")
    public void startOrderSaga(OrderSagaStarted event) {
        // Step 1: Reserve inventory
        publishCommand(new ReserveInventoryCommand(event.getOrderId()));
    }
    
    @KafkaListener(topics = "inventory.reservation.completed")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        // Step 2: Process payment
        publishCommand(new ProcessPaymentCommand(event.getOrderId()));
    }
    
    @KafkaListener(topics = "payment.processing.completed")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        // Step 3: Complete order
        publishCommand(new CompleteOrderCommand(event.getOrderId()));
    }
    
    // Compensation handlers
    @KafkaListener(topics = "payment.processing.failed")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        // Compensate: Release inventory
        publishCommand(new ReleaseInventoryCommand(event.getOrderId()));
    }
}
```

## Monitoring and Observability

### Metrics Collection

```java
@Component
public class KafkaMetricsExporter {
    
    @Scheduled(fixedDelay = 60000)
    public void exportMetrics() {
        Map<MetricName, ? extends Metric> metrics = kafkaProducer.metrics();
        
        metrics.forEach((name, metric) -> {
            if (isImportantMetric(name)) {
                meterRegistry.gauge(
                    "kafka." + name.group() + "." + name.name(),
                    Tags.of("client.id", clientId),
                    metric.metricValue()
                );
            }
        });
    }
}
```

### Distributed Tracing

```java
@Component
public class KafkaTracingInterceptor implements ProducerInterceptor<String, Object> {
    private final Tracer tracer;
    
    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
        Span span = tracer.nextSpan()
            .name("kafka.send")
            .tag("kafka.topic", record.topic())
            .start();
            
        record.headers().add("trace-id", 
            span.context().traceId().getBytes());
            
        return record;
    }
}
```

## Disaster Recovery

### Multi-Region Setup

```yaml
clusters:
  primary:
    region: us-east-1
    brokers: 5
    topics: all
    
  secondary:
    region: us-west-2
    brokers: 5
    topics: all
    
  replication:
    tool: MirrorMaker2
    lag_threshold: 1000
    topics_pattern: ".*"
```

### Backup Strategy

```bash
# Topic backup
kafka-topics --bootstrap-server localhost:9092 \
  --describe --topics-with-overrides > topics-backup.txt

# Consumer offset backup
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --all-groups --describe > consumer-groups-backup.txt
```

## Best Practices

### Producer Best Practices
1. Use idempotent producers
2. Implement proper error handling
3. Use compression for large messages
4. Batch messages when possible
5. Monitor producer metrics

### Consumer Best Practices
1. Use consumer groups for scalability
2. Implement proper offset management
3. Handle rebalancing gracefully
4. Use appropriate deserialization
5. Monitor consumer lag

### Operational Best Practices
1. Monitor cluster health continuously
2. Plan capacity ahead of time
3. Implement proper security
4. Regular backup and disaster recovery drills
5. Keep Kafka and dependencies updated
# Architecture Documentation - Logging Service

## Overview

The Logging Service provides centralized logging, log aggregation, analysis, and monitoring capabilities for the entire Social E-commerce Ecosystem. Built on the ELK (Elasticsearch, Logstash, Kibana) stack with additional components for alerting and distributed tracing.

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Component Architecture](#component-architecture)
3. [Data Flow](#data-flow)
4. [Technology Stack](#technology-stack)
5. [API Design](#api-design)
6. [Security Architecture](#security-architecture)
7. [Scalability Design](#scalability-design)
8. [Integration Points](#integration-points)

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Microservices Layer                          │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │Service A│  │Service B│  │Service C│  │Service D│  │Service E│  │
│  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘  │
│       │            │            │            │            │         │
└───────┼────────────┼────────────┼────────────┼────────────┼────────┘
        │            │            │            │            │
        ▼            ▼            ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         Log Shippers Layer                           │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │Filebeat │  │Fluentd  │  │Logstash │  │ Vector  │  │ Fluent  │  │
│  │         │  │         │  │ Agent   │  │         │  │  Bit    │  │
│  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘  │
└───────┼────────────┼────────────┼────────────┼────────────┼────────┘
        │            │            │            │            │
        ▼            ▼            ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Message Queue (Kafka)                           │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │
│  │logs-app    │  │logs-access │  │logs-error  │  │logs-audit  │   │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘   │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Log Processing (Logstash)                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │  Parser  │  │  Filter  │  │Enrichment│  │  Router  │          │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘          │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Storage Layer (Elasticsearch)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │ Master Nodes │  │  Data Nodes  │  │ Ingest Nodes │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
        ┌─────────────────────────┴─────────────────────────┐
        ▼                                                   ▼
┌──────────────┐                                    ┌──────────────┐
│   Kibana     │                                    │  Grafana     │
│(Visualization)│                                    │ (Monitoring) │
└──────────────┘                                    └──────────────┘
```

### Component Responsibilities

#### Log Shippers
- **Filebeat**: Lightweight log shipper for application logs
- **Fluentd**: Unified logging layer with pluggable architecture
- **Logstash Agent**: Heavy-duty log processing at source
- **Vector**: High-performance observability data pipeline
- **Fluent Bit**: Lightweight log processor and forwarder

#### Message Queue (Kafka)
- **Topics**: Segregated by log type (application, access, error, audit)
- **Partitioning**: Based on service name for parallel processing
- **Retention**: 7 days for replay capability
- **Replication**: Factor of 3 for high availability

#### Log Processing (Logstash)
- **Parsing**: Extract structured data from unstructured logs
- **Filtering**: Remove sensitive information, apply business rules
- **Enrichment**: Add metadata, geolocation, threat intelligence
- **Routing**: Direct logs to appropriate indices and outputs

#### Storage (Elasticsearch)
- **Index Strategy**: Time-based indices with rollover
- **Sharding**: Optimized for write and query performance
- **Replication**: Ensures data availability and query distribution
- **Lifecycle Management**: Automated archival and deletion

## Component Architecture

### Core Components

#### 1. Log Ingestion Service
```java
@Component
public class LogIngestionService {
    private final KafkaTemplate<String, LogEntry> kafkaTemplate;
    private final LogValidator validator;
    private final LogEnricher enricher;
    
    public void ingestLog(LogEntry entry) {
        // Validate log entry
        validator.validate(entry);
        
        // Enrich with metadata
        LogEntry enriched = enricher.enrich(entry);
        
        // Send to Kafka
        kafkaTemplate.send(getTopicName(entry), enriched);
    }
}
```

#### 2. Log Processing Pipeline
```java
@Component
public class LogProcessingPipeline {
    private final List<LogProcessor> processors;
    private final ElasticsearchClient esClient;
    
    @KafkaListener(topics = "logs-*")
    public void processLog(LogEntry entry) {
        // Apply processing chain
        LogEntry processed = entry;
        for (LogProcessor processor : processors) {
            processed = processor.process(processed);
        }
        
        // Index to Elasticsearch
        esClient.index(processed);
    }
}
```

#### 3. Query Service
```java
@RestController
@RequestMapping("/api/v1/logs")
public class LogQueryController {
    private final LogSearchService searchService;
    
    @PostMapping("/search")
    public SearchResponse search(@RequestBody SearchRequest request) {
        return searchService.search(request);
    }
    
    @GetMapping("/aggregate")
    public AggregationResponse aggregate(@RequestParam Map<String, String> params) {
        return searchService.aggregate(params);
    }
}
```

### Supporting Components

#### Log Forwarder Configuration
```yaml
filebeat:
  inputs:
    - type: log
      paths:
        - /var/log/apps/*.log
      multiline:
        pattern: '^\d{4}-\d{2}-\d{2}'
        negate: true
        match: after
      processors:
        - add_docker_metadata: ~
        - add_kubernetes_metadata: ~
  output:
    kafka:
      hosts: ["kafka1:9092", "kafka2:9092", "kafka3:9092"]
      topic: 'logs-%{[fields.service]}'
      partition.round_robin:
        reachable_only: true
```

#### Logstash Pipeline
```ruby
input {
  kafka {
    bootstrap_servers => "kafka1:9092,kafka2:9092,kafka3:9092"
    topics_pattern => "logs-.*"
    codec => "json"
    group_id => "logstash-processors"
  }
}

filter {
  # Parse different log formats
  if [log_type] == "application" {
    grok {
      match => {
        "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} \\[%{DATA:thread}\\] %{DATA:logger} - %{GREEDYDATA:msg}"
      }
    }
  }
  
  # Add GeoIP information
  if [client_ip] {
    geoip {
      source => "client_ip"
      target => "geoip"
    }
  }
  
  # Remove sensitive data
  mutate {
    gsub => [
      "message", "password=\\S+", "password=***",
      "message", "token=\\S+", "token=***"
    ]
  }
}

output {
  elasticsearch {
    hosts => ["es1:9200", "es2:9200", "es3:9200"]
    index => "logs-%{[service]}-%{+YYYY.MM.dd}"
    template_name => "logs"
    template => "/etc/logstash/templates/logs.json"
  }
  
  # Alert on errors
  if [level] == "ERROR" {
    http {
      url => "http://alerting-service/webhook"
      http_method => "post"
      format => "json"
    }
  }
}
```

## Data Flow

### Log Journey

1. **Generation**: Service generates log entry
2. **Collection**: Log shipper collects from file/stdout
3. **Buffering**: Local buffer prevents data loss
4. **Shipping**: Send to Kafka with retry logic
5. **Queuing**: Kafka provides durability and buffering
6. **Processing**: Logstash consumes and processes
7. **Storage**: Indexed in Elasticsearch
8. **Visualization**: Available in Kibana/Grafana

### Data Pipeline

```
Service → Logger → File/Stdout → Shipper → Kafka → Logstash → Elasticsearch → Kibana
                                    ↓                               ↓
                                 Buffer                         Alerting
```

## Technology Stack

### Core Technologies

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| Message Queue | Apache Kafka | 3.5.0 | Log buffering and distribution |
| Log Processing | Logstash | 8.10.0 | Log parsing and enrichment |
| Storage | Elasticsearch | 8.10.0 | Log storage and search |
| Visualization | Kibana | 8.10.0 | Log analysis and dashboards |
| Monitoring | Grafana | 10.0.0 | Metrics and alerting |
| Tracing | Jaeger | 1.47.0 | Distributed tracing |

### Supporting Libraries

```xml
<dependencies>
    <!-- Logging -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.4.11</version>
    </dependency>
    
    <!-- Structured Logging -->
    <dependency>
        <groupId>net.logstash.logback</groupId>
        <artifactId>logstash-logback-encoder</artifactId>
        <version>7.4</version>
    </dependency>
    
    <!-- Elasticsearch Client -->
    <dependency>
        <groupId>co.elastic.clients</groupId>
        <artifactId>elasticsearch-java</artifactId>
        <version>8.10.0</version>
    </dependency>
    
    <!-- Kafka Client -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>3.0.11</version>
    </dependency>
    
    <!-- Metrics -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
        <version>1.11.4</version>
    </dependency>
</dependencies>
```

## API Design

### RESTful Endpoints

#### Search API
```yaml
POST /api/v1/logs/search
Content-Type: application/json

{
  "query": {
    "match": {
      "message": "error"
    }
  },
  "filters": {
    "service": ["order-service", "payment-service"],
    "level": ["ERROR", "WARN"],
    "timeRange": {
      "from": "2024-01-01T00:00:00Z",
      "to": "2024-01-31T23:59:59Z"
    }
  },
  "size": 100,
  "from": 0,
  "sort": [{
    "@timestamp": "desc"
  }]
}
```

#### Aggregation API
```yaml
GET /api/v1/logs/aggregate?field=level&interval=1h&from=now-24h
Response:
{
  "aggregations": {
    "by_level": {
      "buckets": [
        {"key": "ERROR", "doc_count": 1234},
        {"key": "WARN", "doc_count": 5678},
        {"key": "INFO", "doc_count": 89012}
      ]
    },
    "over_time": {
      "buckets": [
        {
          "key_as_string": "2024-01-15T10:00:00Z",
          "doc_count": 10234
        }
      ]
    }
  }
}
```

### Streaming API

#### WebSocket Connection
```javascript
const ws = new WebSocket('wss://logs.example.com/api/v1/logs/stream');

ws.send(JSON.stringify({
  filters: {
    service: 'order-service',
    level: ['ERROR', 'WARN']
  }
}));

ws.onmessage = (event) => {
  const log = JSON.parse(event.data);
  console.log('New log:', log);
};
```

## Security Architecture

### Authentication & Authorization

#### API Security
```java
@Configuration
@EnableWebSecurity
public class LogSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/logs/search").hasRole("LOG_READER")
                .requestMatchers("/api/v1/logs/admin/**").hasRole("LOG_ADMIN")
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .build();
    }
}
```

### Data Security

#### Log Sanitization
```java
@Component
public class LogSanitizer {
    private final List<Pattern> sensitivePatterns = Arrays.asList(
        Pattern.compile("password=\\S+"),
        Pattern.compile("token=\\S+"),
        Pattern.compile("api[_-]?key=\\S+"),
        Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b") // Credit card
    );
    
    public String sanitize(String message) {
        String sanitized = message;
        for (Pattern pattern : sensitivePatterns) {
            sanitized = pattern.matcher(sanitized).replaceAll("***");
        }
        return sanitized;
    }
}
```

#### Encryption at Rest
```yaml
elasticsearch:
  xpack:
    security:
      enabled: true
      transport:
        ssl:
          enabled: true
          verification_mode: certificate
      http:
        ssl:
          enabled: true
```

## Scalability Design

### Horizontal Scaling

#### Kafka Scaling
```yaml
kafka:
  topics:
    logs-app:
      partitions: 12
      replication-factor: 3
    logs-error:
      partitions: 6
      replication-factor: 3
```

#### Elasticsearch Scaling
```yaml
elasticsearch:
  cluster:
    name: logs-cluster
    initial_master_nodes:
      - es-master-1
      - es-master-2
      - es-master-3
  node:
    roles:
      - master: 3 nodes (dedicated)
      - data: 6 nodes (hot-warm architecture)
      - ingest: 2 nodes (preprocessing)
```

### Performance Optimization

#### Index Lifecycle Management
```json
{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_age": "1d",
            "max_size": "50GB"
          }
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "shrink": {
            "number_of_shards": 1
          },
          "forcemerge": {
            "max_num_segments": 1
          }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

## Integration Points

### Service Integration

#### SDK for Services
```java
@Component
public class LoggingSDK {
    private final LogForwarder forwarder;
    
    public void log(LogLevel level, String message, Map<String, Object> context) {
        LogEntry entry = LogEntry.builder()
            .timestamp(Instant.now())
            .level(level)
            .message(message)
            .service(getServiceName())
            .context(context)
            .build();
            
        forwarder.forward(entry);
    }
}
```

### External Integrations

#### Alerting Integration
```yaml
elastalert:
  rules:
    - name: high_error_rate
      type: frequency
      index: logs-*
      filter:
        - terms:
            level: ["ERROR"]
      num_events: 100
      timeframe:
        minutes: 5
      alert:
        - slack:
            webhook_url: ${SLACK_WEBHOOK_URL}
        - pagerduty:
            service_key: ${PAGERDUTY_KEY}
```

#### SIEM Integration
```java
@Component
public class SIEMForwarder {
    @EventListener
    public void onSecurityLog(SecurityLogEvent event) {
        if (event.getSeverity() >= SecuritySeverity.HIGH) {
            siemClient.forward(event);
        }
    }
}
```

## Deployment Architecture

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: elasticsearch
spec:
  serviceName: elasticsearch
  replicas: 3
  template:
    spec:
      containers:
      - name: elasticsearch
        image: com.exalt/elasticsearch:8.10.0
        resources:
          requests:
            memory: "4Gi"
            cpu: "2"
          limits:
            memory: "8Gi"
            cpu: "4"
        volumeMounts:
        - name: data
          mountPath: /usr/share/elasticsearch/data
  volumeClaimTemplates:
  - metadata:
      name: data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 100Gi
```

## Performance Considerations

### Optimization Strategies

1. **Batch Processing**: Group logs before sending
2. **Compression**: Use gzip for network transfer
3. **Indexing Strategy**: Optimize mappings for search patterns
4. **Caching**: Cache frequent queries in Redis
5. **Load Balancing**: Distribute across multiple Logstash instances

### Monitoring Metrics

- Log ingestion rate
- Processing lag
- Search query performance
- Storage utilization
- Error rates by service
- Alert response times

## Future Enhancements

1. **ML-based Anomaly Detection**: Identify unusual patterns
2. **Log Correlation**: Automatic incident correlation
3. **Predictive Alerting**: Forecast issues before they occur
4. **Natural Language Search**: Query logs using natural language
5. **Automated Root Cause Analysis**: AI-powered incident analysis
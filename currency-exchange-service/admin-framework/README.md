# Admin Framework

A shared administrative framework for standardizing admin dashboards across all domains in the Micro-Social-Ecommerce-Ecosystems project.

## Overview

This framework provides a common foundation for building administrative dashboards and interfaces across the Social Commerce, Warehousing, and Courier Services domains. It includes standardized components, interfaces, and patterns to ensure consistency while allowing domain-specific customization.

## Key Features

- **Abstract Base Classes**: Core components that can be extended for domain-specific needs
- **Standardized Interfaces**: Consistent APIs across all admin implementations
- **Reusable UI Components**: Common dashboard components for analytics, reporting, and management
- **Centralized Dashboard Integration**: Standardized integration with the centralized business intelligence dashboard
- **Reliable Messaging**: WebSocket message acknowledgment for guaranteed message delivery
- **Error Handling**: Comprehensive error handling and retry mechanisms for failed messages
- **WebSocket Integration**: Implemented secure WebSocket configuration with STOMP
- **WebSocket Rate Limiting**: Configurable rate limiting for WebSocket connections and messages

## Directory Structure

```
admin-framework/
├── core/                              # Core application framework
│   ├── BaseAdminApplication.java      # Base application setup
│   ├── security/                      # Security configuration
│   └── layout/                        # Common layout components
├── components/                        # Reusable components
│   ├── reporting/                     # Reporting framework
│   │   ├── model/                     # Report data models
│   │   ├── service/                   # Report generation services
│   │   └── controller/                # Report API endpoints
│   ├── region/                        # Regional management
│   │   ├── model/                     # Region data models
│   │   ├── repository/                # Region data access
│   │   ├── service/                   # Region management services
│   │   ├── controller/                # Region API endpoints
│   │   └── examples/                  # Example implementations
│   ├── dashboard/                     # Dashboard components
│   └── policy/                        # Policy management
├── data/                              # Data access
│   ├── AbstractAdminRepository.java   # Base repository
│   └── dto/                           # Common DTOs
├── util/                              # Utilities
│   ├── ExportService.java             # Export functionality
│   ├── CSVGenerator.java              # CSV generation
│   └── PDFGenerator.java              # PDF generation
├── integration/                       # Centralized dashboard integration
│   ├── CentralizedDashboardClient.java # Integration client
│   └── DataSynchronizationService.java # Data synchronization
└── websocket/                         # WebSocket functionality
    ├── WebSocketConfig.java           # WebSocket configuration
    ├── WebSocketController.java       # WebSocket endpoints
    └── acknowledge/                   # Message acknowledgment
        ├── AcknowledgmentService.java # Acknowledgment logic
        └── AcknowledgmentChannelInterceptor.java # Message interceptor
```

## How to Use

### 1. Add as a Dependency

Add the admin-framework as a dependency in your admin dashboard project:

```xml
<dependency>
    <groupId>com.microsocial.shared</groupId>
    <artifactId>admin-framework</artifactId>
    <version>${admin-framework.version}</version>
</dependency>
```

### 2. Extend Base Components

Create domain-specific implementations by extending the base classes:

```java
@RestController
public class WarehouseReportController extends AbstractReportController<WarehouseReport> {
    @Override
    protected List<WarehouseReport> filterDomainSpecificReports(List<WarehouseReport> reports) {
        // Warehouse-specific implementation
    }
}
```

### 3. Implement Domain-Specific Logic

While using the standardized components, implement the domain-specific business logic:

```java
@Service
public class CourierRegionService extends AbstractRegionService {
    @Override
    protected void applyDomainPolicies(Region region) {
        // Courier-specific regional policies
    }
}
```

## WebSocket Acknowledgment

The WebSocket acknowledgment system provides reliable message delivery guarantees for critical operations. It ensures that messages are properly received and processed by the server.

### Key Features

- **Guaranteed Delivery**: Messages are retried until acknowledged
- **Error Handling**: Automatic error reporting for failed messages
- **Message Deduplication**: Prevents duplicate processing of messages
- **Configurable Retry**: Adjustable retry attempts and delays

### How It Works

1. **Client Sends Message**: Client includes a unique `message-id` header
2. **Server Processes**: Server processes the message and sends an acknowledgment
3. **Client Handles ACK/NACK**: Client receives acknowledgment or schedules retry

### Example Usage

#### Sending a Message with Acknowledgment

```javascript
// Client-side JavaScript example
const messageId = 'msg-' + Date.now();
stompClient.send('/app/events', 
  { 'message-id': messageId },
  JSON.stringify({ type: 'SOME_EVENT', data: '...' })
);

// Subscribe to acknowledgments
stompClient.subscribe(`/user/queue/ack`, (message) => {
  const ack = JSON.parse(message.body);
  if (ack.messageId === messageId) {
    if (ack.success) {
      console.log('Message processed successfully');
    } else {
      console.error('Error processing message:', ack.message);
    }
  }
});
```

#### Server-Side Implementation

```java
@MessageMapping("/events")
@SendToUser(WebSocketConfig.QUEUE_PREFIX + "/events/response")
public String handleEvent(Message<String> message, Principal principal) {
    return acknowledgmentService.processWithAck(
        message,
        payload -> {
            // Process the message
            return "{\"status\":\"success\"}";
        }
    );
}
```

## WebSocket Rate Limiting

The WebSocket rate limiting feature provides a way to control the number of messages and connections allowed within a certain time window.

### Monitoring and Metrics

The rate limiting system includes comprehensive monitoring capabilities:

#### Metrics

- **websocket_rate_limit_total**: Counter for rate limiting events
  - Tags:
    - `type`: "message" or "connection"
    - `result`: "exceeded", "allowed", or "blocked"

#### Endpoints

- `GET /actuator/metrics/websocket.rate_limit`: Raw metrics data
- `GET /api/websocket/metrics/rate-limits`: Current rate limiting configuration

#### Configuration

```properties
# Rate Limit Monitoring
websocket.rate-limit.monitoring.enabled=true
websocket.rate-limit.monitoring.retention-days=30
websocket.rate-limit.monitoring.detailed-logging=false

# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics,websocket,rate-limits
management.endpoint.rate-limits.enabled=true
```

#### Alerting

You can set up alerts based on rate limiting metrics. For example, to alert when rate limits are being hit frequently:

```yaml
# Example Prometheus alert rule
- alert: HighWebSocketRateLimitHits
  expr: rate(websocket_rate_limit_total{result="exceeded"}[5m]) > 10
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "High number of WebSocket rate limit hits"
    description: "WebSocket rate limits are being hit frequently"
```

### Configuration

Configure the rate limiting behavior in your application properties:

```properties
# WebSocket rate limiting settings
websocket.rate-limit.enabled=true
websocket.rate-limit.messages-per-second=100
websocket.rate-limit.connections-per-ip=10
websocket.rate-limit.time-window-seconds=60
websocket.rate-limit.block-on-limit-exceeded=true
```

- `enabled`: Enable or disable rate limiting
- `messages-per-second`: Maximum messages allowed per second per connection
- `connections-per-ip`: Maximum concurrent connections allowed per IP address
- `time-window-seconds`: Time window in seconds for rate limiting
- `block-on-limit-exceeded`: Whether to block or drop messages when limits are exceeded

## Component Details

### Region Management

The region management component provides a standardized way to manage geographical or organizational regions across different domains. It includes:

#### Model

- `BaseRegion`: Abstract base class for all region types
  - Common properties: id, name, code, description, status, hierarchy
  - Support for region hierarchies with parent-child relationships
  - Status tracking (ACTIVE, INACTIVE, PENDING_APPROVAL, DEPRECATED)
  - Region categorization with RegionType enum

#### Repository

- `BaseRegionRepository`: Interface defining common data access methods
  - Standard CRUD operations
  - Search by various criteria (name, code, status, type)
  - Hierarchical data access methods

#### Service

- `AbstractRegionService`: Abstract service providing region management functionality
  - CRUD operations with validation
  - Hierarchy management
  - Status management (activation/deactivation)
  - Pre/post processing hooks for domain customization

#### Controller

- `AbstractRegionController`: Abstract REST controller for region APIs
  - Standard REST endpoints for region management
  - Consistent response formats
  - Authentication integration

#### Example Implementation

The `examples` directory contains a sample implementation for e-commerce regions:

- `EcommerceRegion`: Extends BaseRegion with e-commerce specific properties
  - Shipping configuration
  - Currency support
  - Tax codes
- `EcommerceRegionService`: Domain-specific service implementation
  - Shipping management
  - Currency management
- `EcommerceRegionController`: REST API for e-commerce regions

### Reporting

The reporting component provides a standardized way to generate and manage reports across different domains. It includes:

- Base models for report data
- Services for report generation and export
- Controllers for report API endpoints

## Integration with Centralized Dashboard

All admin dashboards built with this framework automatically integrate with the Centralized Business Intelligence Dashboard through standardized APIs and data formats.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

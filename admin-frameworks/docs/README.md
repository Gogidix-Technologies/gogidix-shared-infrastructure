# Admin Framework Documentation

## Overview
The Admin Framework provides a comprehensive set of components, utilities, and services for building administrative applications in the Social E-commerce Ecosystem. It offers common functionality for admin applications, such as security configuration, reporting, dashboards, region management, and policy management.

## Components

### Core Components
- **BaseAdminApplication**: The base class for all admin applications. It provides common functionality such as security, user management, and dashboard configuration.
- **SecurityConfig**: Security configuration for admin applications. It provides security settings for authentication, authorization, and access control.

### Feature Components
- **Reporting**: The reporting component provides functionality for generating reports, scheduling reports, and distributing reports.
- **Dashboard**: The dashboard component provides functionality for creating dashboards with customizable widgets.
- **Region Management**: The region management component provides functionality for managing regions and hierarchical organization structures.
- **Policy Management**: The policy management component provides functionality for managing policies and enforcing policy compliance.

### Data Access Layer
- **Repository**: The repository interface provides a common abstraction for data access operations.
- **JpaRepository**: The JPA implementation of the repository interface provides JPA-based data access.

### Utility Services
- **Validator**: The validator utility provides functionality for validating objects and reporting validation errors.
- **Logger**: The logger utility provides logging functionality for the admin framework.

### Integration Components
- **RestClient**: The REST client provides functionality for making HTTP requests to external services.
- **MessageBroker**: The message broker provides functionality for publishing and subscribing to messages.

## Getting Started
To use the Admin Framework, follow these steps:

1. Create a new admin application that extends BaseAdminApplication
2. Configure security settings using SecurityConfig
3. Add the required components (Reporting, Dashboard, Region Management, Policy Management) to your application
4. Use the data access layer to interact with the database
5. Use the utility services and integration components as needed

## Examples

### Creating a New Admin Application
`java
import com.gogidix.admin.core.BaseAdminApplication;
import com.gogidix.admin.core.SecurityConfig;
import com.gogidix.admin.components.reporting.ReportingComponent;
import com.gogidix.admin.components.dashboard.DashboardComponent;
import com.gogidix.admin.components.region.RegionManagement;
import com.gogidix.admin.components.policy.PolicyManagement;

public class MyAdminApplication extends BaseAdminApplication {
    private final SecurityConfig securityConfig;
    private final ReportingComponent reportingComponent;
    private final DashboardComponent dashboardComponent;
    private final RegionManagement regionManagement;
    private final PolicyManagement policyManagement;
    
    public MyAdminApplication() {
        super("My Admin Application", "My custom admin application");
        
        this.securityConfig = new SecurityConfig();
        this.reportingComponent = new ReportingComponent("My Reporting", "My custom reporting component");
        this.dashboardComponent = new DashboardComponent("My Dashboard", "My custom dashboard component");
        this.regionManagement = new RegionManagement("My Region Management", "My custom region management component");
        this.policyManagement = new PolicyManagement("My Policy Management", "My custom policy management component");
    }
    
    // Add your custom admin application logic here
}
`

### Using the Data Access Layer
`java
import com.gogidix.admin.dal.Repository;
import com.gogidix.admin.dal.JpaRepository;
import com.gogidix.admin.components.region.Region;

public class RegionService {
    private final Repository<Region> regionRepository;
    
    public RegionService() {
        this.regionRepository = new JpaRepository<>(Region.class);
    }
    
    public Region createRegion(String name, String code, String description) {
        Region region = new Region(name, code, description);
        return regionRepository.save(region);
    }
    
    public Optional<Region> getRegionById(UUID id) {
        return regionRepository.findById(id);
    }
    
    public List<Region> getAllRegions() {
        return regionRepository.findAll();
    }
    
    public void deleteRegion(UUID id) {
        regionRepository.deleteById(id);
    }
}
`

### Using the REST Client
`java
import com.gogidix.admin.integration.RestClient;

public class ExternalService {
    private final RestClient restClient;
    
    public ExternalService() {
        this.restClient = new RestClient();
    }
    
    public String getExternalData() throws Exception {
        return restClient.get("https://api.example.com/data");
    }
    
    public String postExternalData(String data) throws Exception {
        return restClient.post("https://api.example.com/data", data);
    }
}
`

### Using the Message Broker
`java
import com.gogidix.admin.integration.MessageBroker;

public class NotificationService {
    private final MessageBroker messageBroker;
    
    public NotificationService() {
        this.messageBroker = new MessageBroker();
    }
    
    public void subscribeToNotifications(Consumer<Object> callback) {
        UUID subscriberId = messageBroker.subscribe("notifications", callback);
        // Store subscriberId for later unsubscribing
    }
    
    public void sendNotification(String message) {
        messageBroker.publish("notifications", message);
    }
    
    public void sendNotificationAsync(String message) {
        messageBroker.publishAsync("notifications", message);
    }
}
`

## Best Practices
1. **Security**: Always use SecurityConfig to configure security settings
2. **Validation**: Use the Validator utility to validate input data
3. **Logging**: Use the Logger utility for logging
4. **Error Handling**: Handle errors and exceptions appropriately
5. **Performance**: Use asynchronous methods for long-running operations

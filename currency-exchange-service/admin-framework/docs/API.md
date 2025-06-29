# Admin Framework API Documentation

## Core API

### BaseAdminApplication
- BaseAdminApplication(): Default constructor
- BaseAdminApplication(String name, String description): Constructor with name and description
- UUID getId(): Get the application ID
- String getName(): Get the application name
- oid setName(String name): Set the application name
- String getDescription(): Get the application description
- oid setDescription(String description): Set the application description
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### SecurityConfig
- SecurityConfig(): Default constructor with secure defaults
- List<String> getAllowedOrigins(): Get the allowed origins
- oid setAllowedOrigins(List<String> allowedOrigins): Set the allowed origins
- oolean isCsrfEnabled(): Check if CSRF protection is enabled
- oid setCsrfEnabled(boolean csrfEnabled): Enable or disable CSRF protection
- String getTokenExpirationSeconds(): Get the token expiration time in seconds
- oid setTokenExpirationSeconds(String tokenExpirationSeconds): Set the token expiration time in seconds
- String getJwtSecret(): Get the JWT secret
- oid setJwtSecret(String jwtSecret): Set the JWT secret

## Component API

### ReportingComponent
- ReportingComponent(): Default constructor
- ReportingComponent(String name, String description): Constructor with name and description
- oid addReportTemplate(ReportTemplate template): Add a report template
- oolean removeReportTemplate(UUID templateId): Remove a report template
- List<ReportTemplate> getReportTemplates(): Get all report templates
- UUID getId(): Get the component ID
- String getName(): Get the component name
- oid setName(String name): Set the component name
- String getDescription(): Get the component description
- oid setDescription(String description): Set the component description
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### ReportTemplate
- ReportTemplate(): Default constructor
- ReportTemplate(String name, String description, String templateContent, String templateType): Constructor with name, description, and template content
- UUID getId(): Get the template ID
- String getName(): Get the template name
- oid setName(String name): Set the template name
- String getDescription(): Get the template description
- oid setDescription(String description): Set the template description
- String getTemplateContent(): Get the template content
- oid setTemplateContent(String templateContent): Set the template content
- String getTemplateType(): Get the template type
- oid setTemplateType(String templateType): Set the template type
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### DashboardComponent
- DashboardComponent(): Default constructor
- DashboardComponent(String name, String description): Constructor with name and description
- oid addWidget(DashboardWidget widget): Add a widget
- oolean removeWidget(UUID widgetId): Remove a widget
- List<DashboardWidget> getWidgets(): Get all widgets
- UUID getId(): Get the component ID
- String getName(): Get the component name
- oid setName(String name): Set the component name
- String getDescription(): Get the component description
- oid setDescription(String description): Set the component description
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### DashboardWidget
- DashboardWidget(): Default constructor
- DashboardWidget(String name, String description, String widgetType, int positionX, int positionY, int width, int height): Constructor with name, description, and widget type
- oid addConfigParam(String key, String value): Add a configuration parameter
- String removeConfigParam(String key): Remove a configuration parameter
- Map<String, String> getConfiguration(): Get all configuration parameters
- UUID getId(): Get the widget ID
- String getName(): Get the widget name
- oid setName(String name): Set the widget name
- String getDescription(): Get the widget description
- oid setDescription(String description): Set the widget description
- String getWidgetType(): Get the widget type
- oid setWidgetType(String widgetType): Set the widget type
- int getPositionX(): Get the X position
- oid setPositionX(int positionX): Set the X position
- int getPositionY(): Get the Y position
- oid setPositionY(int positionY): Set the Y position
- int getWidth(): Get the width
- oid setWidth(int width): Set the width
- int getHeight(): Get the height
- oid setHeight(int height): Set the height
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### RegionManagement
- RegionManagement(): Default constructor
- RegionManagement(String name, String description): Constructor with name and description
- oid addRegion(Region region): Add a region
- oolean removeRegion(UUID regionId): Remove a region
- Optional<Region> getRegionById(UUID regionId): Get a region by ID
- List<Region> getRegions(): Get all regions
- List<Region> getRegionsByParentId(UUID parentRegionId): Get regions by parent region ID
- List<Region> getTopLevelRegions(): Get top-level regions (regions without a parent)
- UUID getId(): Get the component ID
- String getName(): Get the component name
- oid setName(String name): Set the component name
- String getDescription(): Get the component description
- oid setDescription(String description): Set the component description
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### Region
- Region(): Default constructor
- Region(String name, String code, String description): Constructor with name, code, and description
- Region(String name, String code, String description, Region parentRegion): Constructor with name, code, description, and parent region
- UUID getId(): Get the region ID
- String getName(): Get the region name
- oid setName(String name): Set the region name
- String getCode(): Get the region code
- oid setCode(String code): Set the region code
- String getDescription(): Get the region description
- oid setDescription(String description): Set the region description
- Region getParentRegion(): Get the parent region
- oid setParentRegion(Region parentRegion): Set the parent region
- oolean isActive(): Check if the region is active
- oid setActive(boolean active): Set the region as active or inactive
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp
- String toString(): Get a string representation of the region

### PolicyManagement
- PolicyManagement(): Default constructor
- PolicyManagement(String name, String description): Constructor with name and description
- oid addPolicy(Policy policy): Add a policy
- oolean removePolicy(UUID policyId): Remove a policy
- Optional<Policy> getPolicyById(UUID policyId): Get a policy by ID
- List<Policy> getPolicies(): Get all policies
- List<Policy> getActivePolicies(): Get active policies
- List<Policy> getPoliciesByType(String policyType): Get policies by type
- UUID getId(): Get the component ID
- String getName(): Get the component name
- oid setName(String name): Set the component name
- String getDescription(): Get the component description
- oid setDescription(String description): Set the component description
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### Policy
- Policy(): Default constructor
- Policy(String name, String description, String policyText, String policyType): Constructor with name, description, policy text, and policy type
- Policy(String name, String description, String policyText, String policyType, LocalDateTime effectiveDate, LocalDateTime expirationDate): Constructor with name, description, policy text, policy type, and dates
- oolean isEffective(): Check if the policy is currently effective
- UUID getId(): Get the policy ID
- String getName(): Get the policy name
- oid setName(String name): Set the policy name
- String getDescription(): Get the policy description
- oid setDescription(String description): Set the policy description
- String getPolicyText(): Get the policy text
- oid setPolicyText(String policyText): Set the policy text
- String getPolicyType(): Get the policy type
- oid setPolicyType(String policyType): Set the policy type
- LocalDateTime getEffectiveDate(): Get the effective date
- oid setEffectiveDate(LocalDateTime effectiveDate): Set the effective date
- LocalDateTime getExpirationDate(): Get the expiration date
- oid setExpirationDate(LocalDateTime expirationDate): Set the expiration date
- oolean isActive(): Check if the policy is active
- oid setActive(boolean active): Set the policy as active or inactive
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp
- String toString(): Get a string representation of the policy

## Data Access API

### Repository<T>
- T save(T entity): Save an entity
- Optional<T> findById(UUID id): Find an entity by ID
- List<T> findAll(): Find all entities
- oid delete(T entity): Delete an entity
- oolean deleteById(UUID id): Delete an entity by ID
- long count(): Count all entities

### JpaRepository<T>
- JpaRepository(Class<T> entityClass): Constructor with entity class
- T save(T entity): Save an entity
- Optional<T> findById(UUID id): Find an entity by ID
- List<T> findAll(): Find all entities
- oid delete(T entity): Delete an entity
- oolean deleteById(UUID id): Delete an entity by ID
- long count(): Count all entities

## Utility API

### Validator<T>
- Validator(T object): Constructor with object
- Validator<T> validate(String fieldName, FieldGetter<T> getter, Predicate<Object> predicate, String errorMessage): Validate a field
- oolean hasErrors(): Check if validation has errors
- List<ValidationError> getErrors(): Get validation errors
- T getObject(): Get the validated object

### ValidationError
- ValidationError(String fieldName, String message): Constructor with field name and message
- String getFieldName(): Get the field name
- String getMessage(): Get the error message
- String toString(): Get a string representation of the validation error

### Logger
- Logger(Class<?> loggerClass): Constructor with logger class
- static Logger getLogger(Class<?> clazz): Get a logger for a class
- oid setMinimumLevel(Level level): Set the minimum log level
- oid debug(String message): Log a debug message
- oid info(String message): Log an info message
- oid warn(String message): Log a warning message
- oid error(String message): Log an error message
- oid error(String message, Exception e): Log an error message with an exception
- oid log(Level level, String message): Log a message with a level

## Integration API

### RestClient
- RestClient(): Default constructor
- RestClient(int timeoutSeconds): Constructor with timeout
- String get(String url): Make a GET request
- String get(String url, Map<String, String> headers): Make a GET request with headers
- String post(String url, String body): Make a POST request
- String post(String url, String body, Map<String, String> headers): Make a POST request with headers
- CompletableFuture<String> getAsync(String url): Make an asynchronous GET request
- CompletableFuture<String> getAsync(String url, Map<String, String> headers): Make an asynchronous GET request with headers
- CompletableFuture<String> postAsync(String url, String body): Make an asynchronous POST request
- CompletableFuture<String> postAsync(String url, String body, Map<String, String> headers): Make an asynchronous POST request with headers

### MessageBroker
- MessageBroker(): Default constructor
- oid publish(String topic, Object message): Publish a message to a topic
- CompletableFuture<Void> publishAsync(String topic, Object message): Publish a message to a topic asynchronously
- UUID subscribe(String topic, Consumer<Object> callback): Subscribe to a topic
- oolean unsubscribe(String topic, UUID subscriberId): Unsubscribe from a topic

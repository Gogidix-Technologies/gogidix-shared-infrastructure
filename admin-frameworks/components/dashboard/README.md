# Dashboard Component

The Dashboard component provides a standardized framework for creating and managing administrative dashboards across all domains in the Micro-Social-Ecommerce ecosystem.

## Features

- **Extensible Dashboard Model**: Base classes for creating domain-specific dashboard implementations
- **Widget System**: Flexible widget framework for displaying various types of data
- **Dashboard Layouts**: Support for different dashboard layouts and configurations
- **Data Integration**: Easy integration with domain-specific data sources

## Structure

- **model**: Core dashboard data models including `BaseDashboard`, `DashboardWidget`, and `DashboardLayout`
- **service**: Services for managing dashboards including creation, updating, and configuration
- **controller**: REST controllers for dashboard APIs
- **components**: Concrete widget implementations like `ChartWidget` and `KpiWidget`

## How to Use

Extend the base classes to create domain-specific implementations:

```java
public class WarehouseDashboard extends BaseDashboard {
    @Override
    public void configureDomainSpecificComponents() {
        // Warehouse-specific dashboard configuration
        
        // Add KPI widgets for warehouse metrics
        KpiWidget inventoryKpi = new KpiWidget("Inventory Levels", "Total Inventory", 15000, "units");
        addWidget(inventoryKpi);
        
        // Add chart widgets for warehouse analytics
        ChartWidget orderTrendChart = new ChartWidget("Order Trends", ChartWidget.ChartType.LINE);
        // Configure chart
        addWidget(orderTrendChart);
    }
}
```

Implement domain-specific services by extending the base service:

```java
@Service
public class WarehouseDashboardService extends AbstractDashboardService<WarehouseDashboard> {
    // Implementation
}
```

Create REST controllers for your domain dashboards:

```java
@RestController
@RequestMapping("/api/warehouse/dashboard")
public class WarehouseDashboardController extends AbstractDashboardController<WarehouseDashboard, WarehouseDashboardService> {
    @Autowired
    public WarehouseDashboardController(WarehouseDashboardService dashboardService) {
        super(dashboardService);
    }
    
    @Override
    protected void registerDomainSpecificEndpoints() {
        // Register any warehouse-specific endpoints
    }
}
```

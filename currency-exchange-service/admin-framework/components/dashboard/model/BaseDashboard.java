package com.exalt.shared.shared.admin.components.dashboard.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Base dashboard model providing common functionality for all domain dashboards.
 * This abstract class defines the structure and common behavior for dashboard objects
 * across different domains in the Micro-Social-Ecommerce ecosystem.
 */
public abstract class BaseDashboard {
    
    private UUID id;
    private String name;
    private String description;
    private Date lastUpdated;
    private String createdBy;
    private Date createdDate;
    private boolean isDefault;
    private List<DashboardWidget> widgets;
    private DashboardLayout layout;
    
    public BaseDashboard() {
        this.id = UUID.randomUUID();
        this.widgets = new ArrayList<>();
        this.createdDate = new Date();
        this.lastUpdated = new Date();
    }
    
    public BaseDashboard(String name, String description, String createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    /**
     * Adds a widget to the dashboard
     * 
     * @param widget The widget to add
     * @return true if successfully added
     */
    public boolean addWidget(DashboardWidget widget) {
        updateLastModified();
        return widgets.add(widget);
    }
    
    /**
     * Removes a widget from the dashboard
     * 
     * @param widgetId The ID of the widget to remove
     * @return true if successfully removed
     */
    public boolean removeWidget(UUID widgetId) {
        boolean removed = widgets.removeIf(w -> w.getId().equals(widgetId));
        if (removed) {
            updateLastModified();
        }
        return removed;
    }
    
    /**
     * Refreshes all widgets in the dashboard
     */
    public void refreshWidgets() {
        widgets.forEach(DashboardWidget::refreshData);
        updateLastModified();
    }
    
    /**
     * Updates the last modified timestamp
     */
    protected void updateLastModified() {
        this.lastUpdated = new Date();
    }
    
    /**
     * Domain-specific method to be implemented by each domain dashboard
     * to customize the dashboard with domain-specific widgets and layouts
     */
    public abstract void configureDomainSpecificComponents();

    // Getters and Setters
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateLastModified();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateLastModified();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
        updateLastModified();
    }

    public List<DashboardWidget> getWidgets() {
        return widgets;
    }

    public void setWidgets(List<DashboardWidget> widgets) {
        this.widgets = widgets;
        updateLastModified();
    }

    public DashboardLayout getLayout() {
        return layout;
    }

    public void setLayout(DashboardLayout layout) {
        this.layout = layout;
        updateLastModified();
    }
}

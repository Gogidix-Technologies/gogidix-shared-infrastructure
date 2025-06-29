package com.exalt.shared.ecommerce.admin.components.dashboard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.microsocial.shared.admin.components.dashboard.model.DashboardWidget;

/**
 * Dashboard component for admin applications.
 * This component provides dashboard functionality for admin applications,
 * including widget management, layout configuration, and data visualization.
 */
public class DashboardComponent {
    private UUID id;
    private String name;
    private String description;
    private List<DashboardWidget> widgets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public DashboardComponent() {
        this.id = UUID.randomUUID();
        this.widgets = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with name and description
     * 
     * @param name The name of the dashboard component
     * @param description The description of the dashboard component
     */
    public DashboardComponent(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    /**
     * Add a widget to the dashboard
     * 
     * @param widget The widget to add
     */
    public void addWidget(DashboardWidget widget) {
        this.widgets.add(widget);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Remove a widget from the dashboard
     * 
     * @param widgetId The ID of the widget to remove
     * @return true if the widget was removed, false otherwise
     */
    public boolean removeWidget(UUID widgetId) {
        boolean removed = this.widgets.removeIf(widget -> widget.getId().equals(widgetId));
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }
    
    /**
     * Get all widgets
     * 
     * @return The list of widgets
     */
    public List<DashboardWidget> getWidgets() {
        return widgets;
    }
    
    // Getters and setters
    
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

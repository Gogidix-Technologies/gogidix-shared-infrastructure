package com.gogidix.shared.admin.components.dashboard.model;

import java.util.Date;
import java.util.UUID;

/**
 * Base model for dashboard widgets.
 * Represents a single UI component on the dashboard that displays specific data or functionality.
 */
public abstract class DashboardWidget {
    
    private UUID id;
    private String title;
    private String description;
    private WidgetType type;
    private WidgetSize size;
    private Date lastRefreshed;
    private boolean autoRefresh;
    private int refreshIntervalSeconds;
    private WidgetStatus status;
    private WidgetPosition position;
    
    public DashboardWidget() {
        this.id = UUID.randomUUID();
        this.lastRefreshed = new Date();
        this.status = WidgetStatus.ACTIVE;
        this.size = WidgetSize.MEDIUM;
        this.autoRefresh = false;
        this.refreshIntervalSeconds = 300; // Default 5 minutes
    }
    
    public DashboardWidget(String title, WidgetType type) {
        this();
        this.title = title;
        this.type = type;
    }
    
    /**
     * Refreshes the widget data.
     * This is an abstract method that should be implemented by specific widget types
     * to update their data from the appropriate data source.
     */
    public abstract void refreshData();
    
    /**
     * Returns the widget data in a standardized format.
     * Each widget implementation will provide its own specific data structure.
     * 
     * @return Object representing the widget's data
     */
    public abstract Object getWidgetData();
    
    /**
     * Updates the last refreshed timestamp
     */
    protected void updateLastRefreshed() {
        this.lastRefreshed = new Date();
    }

    // Getters and Setters
    
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WidgetType getType() {
        return type;
    }

    public void setType(WidgetType type) {
        this.type = type;
    }

    public WidgetSize getSize() {
        return size;
    }

    public void setSize(WidgetSize size) {
        this.size = size;
    }

    public Date getLastRefreshed() {
        return lastRefreshed;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public int getRefreshIntervalSeconds() {
        return refreshIntervalSeconds;
    }

    public void setRefreshIntervalSeconds(int refreshIntervalSeconds) {
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    public WidgetStatus getStatus() {
        return status;
    }

    public void setStatus(WidgetStatus status) {
        this.status = status;
    }

    public WidgetPosition getPosition() {
        return position;
    }

    public void setPosition(WidgetPosition position) {
        this.position = position;
    }
    
    /**
     * Widget status enum
     */
    public enum WidgetStatus {
        ACTIVE, 
        INACTIVE, 
        ERROR, 
        LOADING
    }
    
    /**
     * Widget type enum defining standard widget types
     */
    public enum WidgetType {
        CHART, 
        TABLE, 
        KPI, 
        MAP, 
        ALERT, 
        REPORT, 
        CALENDAR, 
        CUSTOM
    }
    
    /**
     * Widget size enum
     */
    public enum WidgetSize {
        SMALL, 
        MEDIUM, 
        LARGE, 
        EXTRA_LARGE, 
        FULL_WIDTH
    }
}

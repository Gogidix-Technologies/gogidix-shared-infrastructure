package com.gogidix.shared.admin.components.dashboard.service;

import com.gogidix.shared.admin.components.dashboard.model.BaseDashboard;
import com.gogidix.shared.admin.components.dashboard.model.DashboardWidget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract service for dashboard management.
 * Provides common functionality for managing dashboards across different domains.
 */
public abstract class AbstractDashboardService<T extends BaseDashboard> {
    
    /**
     * Creates a new dashboard
     * 
     * @param dashboard The dashboard to create
     * @return The created dashboard
     */
    public abstract T createDashboard(T dashboard);
    
    /**
     * Updates an existing dashboard
     * 
     * @param dashboard The dashboard to update
     * @return The updated dashboard
     */
    public abstract T updateDashboard(T dashboard);
    
    /**
     * Finds a dashboard by ID
     * 
     * @param dashboardId The ID of the dashboard to find
     * @return Optional containing the dashboard if found, empty otherwise
     */
    public abstract Optional<T> findDashboardById(UUID dashboardId);
    
    /**
     * Deletes a dashboard
     * 
     * @param dashboardId The ID of the dashboard to delete
     * @return true if successfully deleted
     */
    public abstract boolean deleteDashboard(UUID dashboardId);
    
    /**
     * Finds all dashboards
     * 
     * @return List of all dashboards
     */
    public abstract List<T> findAllDashboards();
    
    /**
     * Finds the default dashboard
     * 
     * @return Optional containing the default dashboard if found, empty otherwise
     */
    public abstract Optional<T> findDefaultDashboard();
    
    /**
     * Sets a dashboard as the default
     * 
     * @param dashboardId The ID of the dashboard to set as default
     * @return The updated dashboard
     */
    public abstract T setDefaultDashboard(UUID dashboardId);
    
    /**
     * Adds a widget to a dashboard
     * 
     * @param dashboardId The ID of the dashboard
     * @param widget The widget to add
     * @return The updated dashboard
     */
    public abstract T addWidgetToDashboard(UUID dashboardId, DashboardWidget widget);
    
    /**
     * Removes a widget from a dashboard
     * 
     * @param dashboardId The ID of the dashboard
     * @param widgetId The ID of the widget to remove
     * @return The updated dashboard
     */
    public abstract T removeWidgetFromDashboard(UUID dashboardId, UUID widgetId);
    
    /**
     * Refreshes all widgets in a dashboard
     * 
     * @param dashboardId The ID of the dashboard
     * @return The updated dashboard
     */
    public abstract T refreshDashboard(UUID dashboardId);
    
    /**
     * Exports a dashboard configuration
     * 
     * @param dashboardId The ID of the dashboard to export
     * @return String representation of the dashboard configuration
     */
    public abstract String exportDashboardConfiguration(UUID dashboardId);
    
    /**
     * Imports a dashboard configuration
     * 
     * @param configurationJson JSON string containing the dashboard configuration
     * @return The imported dashboard
     */
    public abstract T importDashboardConfiguration(String configurationJson);
    
    /**
     * Finds dashboards by user
     * 
     * @param userId The ID of the user
     * @return List of dashboards assigned to the user
     */
    public abstract List<T> findDashboardsByUser(String userId);
    
    /**
     * Domain-specific method to apply any domain-specific rules or processing
     * 
     * @param dashboard The dashboard to process
     * @return The processed dashboard
     */
    protected abstract T applyDomainSpecificProcessing(T dashboard);
}

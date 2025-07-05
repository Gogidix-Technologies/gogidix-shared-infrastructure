package com.gogidix.shared.shared.admin.components.dashboard.controller;

import com.microsocial.shared.admin.components.dashboard.model.BaseDashboard;
import com.microsocial.shared.admin.components.dashboard.model.DashboardWidget;
import com.microsocial.shared.admin.components.dashboard.service.AbstractDashboardService;

import java.util.List;
import java.util.UUID;

/**
 * Abstract controller for dashboard management APIs.
 * Provides standardized REST endpoints for dashboard operations across different domains.
 * 
 * @param <T> The specific dashboard type (extending BaseDashboard)
 * @param <S> The specific dashboard service (extending AbstractDashboardService)
 */
public abstract class AbstractDashboardController<T extends BaseDashboard, S extends AbstractDashboardService<T>> {
    
    protected final S dashboardService;
    
    public AbstractDashboardController(S dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    /**
     * Creates a new dashboard
     * 
     * @param dashboard The dashboard to create
     * @return The created dashboard
     */
    public T createDashboard(T dashboard) {
        return dashboardService.createDashboard(dashboard);
    }
    
    /**
     * Updates an existing dashboard
     * 
     * @param dashboardId The ID of the dashboard to update
     * @param dashboard The updated dashboard data
     * @return The updated dashboard
     */
    public T updateDashboard(UUID dashboardId, T dashboard) {
        dashboard.setId(dashboardId);
        return dashboardService.updateDashboard(dashboard);
    }
    
    /**
     * Gets a dashboard by ID
     * 
     * @param dashboardId The ID of the dashboard to get
     * @return The dashboard if found
     */
    public T getDashboard(UUID dashboardId) {
        return dashboardService.findDashboardById(dashboardId)
                .orElseThrow(() -> new RuntimeException("Dashboard not found with ID: " + dashboardId));
    }
    
    /**
     * Deletes a dashboard
     * 
     * @param dashboardId The ID of the dashboard to delete
     * @return true if successfully deleted
     */
    public boolean deleteDashboard(UUID dashboardId) {
        return dashboardService.deleteDashboard(dashboardId);
    }
    
    /**
     * Gets all dashboards
     * 
     * @return List of all dashboards
     */
    public List<T> getAllDashboards() {
        return dashboardService.findAllDashboards();
    }
    
    /**
     * Gets the default dashboard
     * 
     * @return The default dashboard
     */
    public T getDefaultDashboard() {
        return dashboardService.findDefaultDashboard()
                .orElseThrow(() -> new RuntimeException("No default dashboard found"));
    }
    
    /**
     * Sets a dashboard as the default
     * 
     * @param dashboardId The ID of the dashboard to set as default
     * @return The updated dashboard
     */
    public T setDefaultDashboard(UUID dashboardId) {
        return dashboardService.setDefaultDashboard(dashboardId);
    }
    
    /**
     * Adds a widget to a dashboard
     * 
     * @param dashboardId The ID of the dashboard
     * @param widget The widget to add
     * @return The updated dashboard
     */
    public T addWidget(UUID dashboardId, DashboardWidget widget) {
        return dashboardService.addWidgetToDashboard(dashboardId, widget);
    }
    
    /**
     * Removes a widget from a dashboard
     * 
     * @param dashboardId The ID of the dashboard
     * @param widgetId The ID of the widget to remove
     * @return The updated dashboard
     */
    public T removeWidget(UUID dashboardId, UUID widgetId) {
        return dashboardService.removeWidgetFromDashboard(dashboardId, widgetId);
    }
    
    /**
     * Refreshes a dashboard
     * 
     * @param dashboardId The ID of the dashboard to refresh
     * @return The refreshed dashboard
     */
    public T refreshDashboard(UUID dashboardId) {
        return dashboardService.refreshDashboard(dashboardId);
    }
    
    /**
     * Exports a dashboard configuration
     * 
     * @param dashboardId The ID of the dashboard to export
     * @return String representation of the dashboard configuration
     */
    public String exportDashboardConfiguration(UUID dashboardId) {
        return dashboardService.exportDashboardConfiguration(dashboardId);
    }
    
    /**
     * Imports a dashboard configuration
     * 
     * @param configurationJson JSON string containing the dashboard configuration
     * @return The imported dashboard
     */
    public T importDashboardConfiguration(String configurationJson) {
        return dashboardService.importDashboardConfiguration(configurationJson);
    }
    
    /**
     * Gets dashboards by user
     * 
     * @param userId The ID of the user
     * @return List of dashboards assigned to the user
     */
    public List<T> getDashboardsByUser(String userId) {
        return dashboardService.findDashboardsByUser(userId);
    }
    
    /**
     * Domain-specific endpoint implementation
     * Allows domains to add custom endpoints specific to their needs
     */
    protected abstract void registerDomainSpecificEndpoints();
}

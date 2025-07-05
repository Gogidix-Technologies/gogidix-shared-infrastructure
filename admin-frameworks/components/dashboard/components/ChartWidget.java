package com.gogidix.shared.admin.components.dashboard.components;

import com.gogidix.shared.admin.components.dashboard.model.DashboardWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chart widget for displaying data in various chart formats.
 * This is a concrete implementation of the DashboardWidget abstract class.
 */
public class ChartWidget extends DashboardWidget {
    
    private ChartType chartType;
    private Map<String, Object> chartOptions;
    private List<Map<String, Object>> chartData;
    private String dataSourceUrl;
    private boolean animated;
    private boolean interactive;
    
    public ChartWidget() {
        super("Chart Widget", WidgetType.CHART);
        this.chartType = ChartType.BAR;
        this.chartOptions = new HashMap<>();
        this.chartData = new ArrayList<>();
        this.animated = true;
        this.interactive = true;
    }
    
    public ChartWidget(String title, ChartType chartType) {
        super(title, WidgetType.CHART);
        this.chartType = chartType;
        this.chartOptions = new HashMap<>();
        this.chartData = new ArrayList<>();
        this.animated = true;
        this.interactive = true;
    }
    
    @Override
    public void refreshData() {
        // In a real implementation, this would fetch data from the dataSourceUrl
        // or some other data source based on the widget configuration
        
        // For now, just update the last refreshed timestamp
        updateLastRefreshed();
    }
    
    @Override
    public Object getWidgetData() {
        Map<String, Object> data = new HashMap<>();
        data.put("type", chartType.name());
        data.put("options", chartOptions);
        data.put("data", chartData);
        data.put("animated", animated);
        data.put("interactive", interactive);
        return data;
    }
    
    /**
     * Adds a data point to the chart
     * 
     * @param dataPoint Map containing the data point
     */
    public void addDataPoint(Map<String, Object> dataPoint) {
        chartData.add(dataPoint);
        updateLastRefreshed();
    }
    
    /**
     * Adds a chart option
     * 
     * @param key Option key
     * @param value Option value
     */
    public void addChartOption(String key, Object value) {
        chartOptions.put(key, value);
    }
    
    // Getters and Setters
    
    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public Map<String, Object> getChartOptions() {
        return chartOptions;
    }

    public void setChartOptions(Map<String, Object> chartOptions) {
        this.chartOptions = chartOptions;
    }

    public List<Map<String, Object>> getChartData() {
        return chartData;
    }

    public void setChartData(List<Map<String, Object>> chartData) {
        this.chartData = chartData;
        updateLastRefreshed();
    }

    public String getDataSourceUrl() {
        return dataSourceUrl;
    }

    public void setDataSourceUrl(String dataSourceUrl) {
        this.dataSourceUrl = dataSourceUrl;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }
    
    /**
     * Chart type enum defining supported chart types
     */
    public enum ChartType {
        BAR,
        LINE,
        PIE,
        DOUGHNUT,
        AREA,
        SCATTER,
        RADAR,
        POLAR,
        BUBBLE,
        HEATMAP
    }
}

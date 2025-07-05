package com.gogidix.shared.shared.admin.components.dashboard.components;

import com.microsocial.shared.admin.components.dashboard.model.DashboardWidget;

import java.util.HashMap;
import java.util.Map;

/**
 * KPI (Key Performance Indicator) widget for displaying business metrics.
 * This is a concrete implementation of the DashboardWidget abstract class.
 */
public class KpiWidget extends DashboardWidget {
    
    private String metric;
    private Object value;
    private Object previousValue;
    private String unit;
    private TrendDirection trend;
    private String icon;
    private String dataSourceUrl;
    private boolean showTrend;
    private String trendPeriod;
    private Map<String, Object> thresholds;
    
    public KpiWidget() {
        super("KPI Widget", WidgetType.KPI);
        this.trend = TrendDirection.NEUTRAL;
        this.showTrend = true;
        this.trendPeriod = "vs. previous period";
        this.thresholds = new HashMap<>();
    }
    
    public KpiWidget(String title, String metric, Object value, String unit) {
        super(title, WidgetType.KPI);
        this.metric = metric;
        this.value = value;
        this.unit = unit;
        this.trend = TrendDirection.NEUTRAL;
        this.showTrend = true;
        this.trendPeriod = "vs. previous period";
        this.thresholds = new HashMap<>();
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
        data.put("metric", metric);
        data.put("value", value);
        data.put("previousValue", previousValue);
        data.put("unit", unit);
        data.put("trend", trend.name());
        data.put("icon", icon);
        data.put("showTrend", showTrend);
        data.put("trendPeriod", trendPeriod);
        data.put("thresholds", thresholds);
        return data;
    }
    
    /**
     * Calculates the trend direction based on the current and previous values
     */
    public void calculateTrend() {
        if (value == null || previousValue == null) {
            this.trend = TrendDirection.NEUTRAL;
            return;
        }
        
        if (value instanceof Number && previousValue instanceof Number) {
            double current = ((Number) value).doubleValue();
            double previous = ((Number) previousValue).doubleValue();
            
            if (current > previous) {
                this.trend = TrendDirection.UP;
            } else if (current < previous) {
                this.trend = TrendDirection.DOWN;
            } else {
                this.trend = TrendDirection.NEUTRAL;
            }
        }
    }
    
    /**
     * Adds a threshold for the KPI
     * 
     * @param name Threshold name (e.g., "warning", "critical")
     * @param value Threshold value
     */
    public void addThreshold(String name, Object value) {
        thresholds.put(name, value);
    }
    
    // Getters and Setters
    
    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        calculateTrend();
        updateLastRefreshed();
    }

    public Object getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(Object previousValue) {
        this.previousValue = previousValue;
        calculateTrend();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public TrendDirection getTrend() {
        return trend;
    }

    public void setTrend(TrendDirection trend) {
        this.trend = trend;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDataSourceUrl() {
        return dataSourceUrl;
    }

    public void setDataSourceUrl(String dataSourceUrl) {
        this.dataSourceUrl = dataSourceUrl;
    }

    public boolean isShowTrend() {
        return showTrend;
    }

    public void setShowTrend(boolean showTrend) {
        this.showTrend = showTrend;
    }

    public String getTrendPeriod() {
        return trendPeriod;
    }

    public void setTrendPeriod(String trendPeriod) {
        this.trendPeriod = trendPeriod;
    }

    public Map<String, Object> getThresholds() {
        return thresholds;
    }

    public void setThresholds(Map<String, Object> thresholds) {
        this.thresholds = thresholds;
    }
    
    /**
     * Trend direction enum
     */
    public enum TrendDirection {
        UP,       // Positive trend
        DOWN,     // Negative trend
        NEUTRAL   // No significant change
    }
}

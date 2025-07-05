package com.gogidix.shared.admin.components.dashboard.model;

/**
 * Represents the position and size of a widget in the dashboard layout.
 */
public class WidgetPosition {
    
    private String widgetId;
    private int row;
    private int column;
    private int width;
    private int height;
    private boolean locked;
    
    public WidgetPosition() {
        this.row = 0;
        this.column = 0;
        this.width = 1;
        this.height = 1;
        this.locked = false;
    }
    
    public WidgetPosition(String widgetId, int row, int column, int width, int height) {
        this();
        this.widgetId = widgetId;
        this.row = row;
        this.column = column;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Checks if this widget position overlaps with another
     * 
     * @param other Another widget position
     * @return true if positions overlap
     */
    public boolean overlaps(WidgetPosition other) {
        return !(this.row + this.height <= other.row || 
                 other.row + other.height <= this.row ||
                 this.column + this.width <= other.column ||
                 other.column + other.width <= this.column);
    }
    
    // Getters and Setters
    
    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width < 1 ? 1 : width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height < 1 ? 1 : height;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}

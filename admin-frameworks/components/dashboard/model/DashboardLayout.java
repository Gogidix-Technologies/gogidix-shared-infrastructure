package com.gogidix.shared.admin.components.dashboard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the layout configuration for a dashboard.
 * Defines how widgets are arranged and displayed on the dashboard.
 */
public class DashboardLayout {
    
    private LayoutType type;
    private int columns;
    private int rowHeight;
    private boolean compact;
    private List<WidgetPosition> positions;
    
    public DashboardLayout() {
        this.type = LayoutType.GRID;
        this.columns = 12; // Default 12-column grid
        this.rowHeight = 100; // Default row height in pixels
        this.compact = true;
        this.positions = new ArrayList<>();
    }
    
    public DashboardLayout(LayoutType type, int columns) {
        this();
        this.type = type;
        this.columns = columns;
    }
    
    /**
     * Adds a widget position to the layout
     * 
     * @param position The widget position to add
     * @return true if successfully added
     */
    public boolean addWidgetPosition(WidgetPosition position) {
        return positions.add(position);
    }
    
    /**
     * Removes a widget position from the layout
     * 
     * @param widgetId The ID of the widget whose position to remove
     * @return true if successfully removed
     */
    public boolean removeWidgetPosition(String widgetId) {
        return positions.removeIf(p -> p.getWidgetId().equals(widgetId));
    }
    
    /**
     * Optimize the layout by arranging widgets to minimize empty space
     */
    public void optimizeLayout() {
        // Implementation depends on the layout algorithm
        // For now, a simple sorting by row and column
        positions.sort((p1, p2) -> {
            if (p1.getRow() == p2.getRow()) {
                return Integer.compare(p1.getColumn(), p2.getColumn());
            }
            return Integer.compare(p1.getRow(), p2.getRow());
        });
        
        // Reindex positions to eliminate gaps
        int currentRow = 0;
        int currentCol = 0;
        
        for (WidgetPosition position : positions) {
            if (currentCol + position.getWidth() > columns) {
                currentCol = 0;
                currentRow++;
            }
            
            position.setRow(currentRow);
            position.setColumn(currentCol);
            
            currentCol += position.getWidth();
        }
    }
    
    // Getters and Setters
    
    public LayoutType getType() {
        return type;
    }

    public void setType(LayoutType type) {
        this.type = type;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public List<WidgetPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<WidgetPosition> positions) {
        this.positions = positions;
    }
    
    /**
     * Layout type enum defining standard dashboard layouts
     */
    public enum LayoutType {
        GRID,       // Grid-based layout with rows and columns
        FREEFORM,   // Free-form layout where widgets can be positioned anywhere
        MASONRY,    // Masonry-style layout with different height columns
        FIXED       // Fixed-position layout with predefined positions
    }
}

package com.exalt.ecosystem.shared.admin.export;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface for handling specific export formats.
 *
 * @param <T> The type of data to export
 */
public interface ExportHandler<T> {

    /**
     * Export data to the specified output stream.
     *
     * @param data The data to export
     * @param outputStream The output stream to write to
     * @param options Additional export options
     */
    void export(List<T> data, OutputStream outputStream, Map<String, Object> options) throws ExportException;
    
    /**
     * Get the format this handler supports.
     *
     * @return The supported export format
     */
    ExportFormat getFormat();
    
    /**
     * Check if this handler supports the given data type.
     *
     * @param dataType The data type to check
     * @return true if supported, false otherwise
     */
    default boolean supports(Class<?> dataType) {
        return true; // Default implementation supports all types
    }
}

package com.gogidix.ecosystem.shared.admin.export;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Service interface for exporting data in various formats.
 */
public interface ExportService {

    /**
     * Export data to the specified format.
     *
     * @param data The data to export
     * @param format The export format (e.g., CSV, EXCEL, PDF)
     * @param outputStream The output stream to write the exported data to
     * @param options Additional export options
     */
    <T> void exportData(List<T> data, ExportFormat format, OutputStream outputStream, Map<String, Object> options);
    
    /**
     * Export data with a custom template.
     *
     * @param data The data to export
     * @param templateName The name of the template to use
     * @param outputStream The output stream to write the exported data to
     * @param options Additional export options
     */
    <T> void exportWithTemplate(List<T> data, String templateName, OutputStream outputStream, Map<String, Object> options);
    
    /**
     * Get the list of supported export formats.
     *
     * @return List of supported export formats
     */
    List<ExportFormat> getSupportedFormats();
    
    /**
     * Check if a specific format is supported.
     *
     * @param format The format to check
     * @return true if supported, false otherwise
     */
    boolean isFormatSupported(ExportFormat format);
}

package com.gogidix.ecosystem.shared.admin.export;

import com.gogidix.ecosystem.shared.admin.export.exception.TemplateException;
import com.gogidix.ecosystem.shared.admin.export.model.ExportTemplate;
import com.gogidix.ecosystem.shared.admin.export.service.ExportTemplateService;
import com.gogidix.ecosystem.shared.admin.export.service.TemplateProcessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the ExportService.
 */
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportServiceImpl.class);
    private final Map<ExportFormat, ExportHandler> handlers = new ConcurrentHashMap<>();
    private final Map<String, ExportTemplateHandler> templateHandlers = new ConcurrentHashMap<>();
    private final ExportTemplateService templateService;
    private final TemplateProcessor templateProcessor;

    public ExportServiceImpl(
            List<ExportHandler> handlers, 
            List<ExportTemplateHandler> templateHandlers,
            ExportTemplateService templateService,
            TemplateProcessor templateProcessor) {
        
        this.templateService = templateService;
        this.templateProcessor = templateProcessor;
        
        // Register all available handlers
        handlers.forEach(handler -> {
            this.handlers.put(handler.getFormat(), handler);
            log.info("Registered export handler for format: {}", handler.getFormat());
        });
        
        // Register all template handlers
        if (templateHandlers != null) {
            templateHandlers.forEach(handler -> {
                this.templateHandlers.put(handler.getTemplateName(), handler);
                log.info("Registered template handler: {}", handler.getTemplateName());
            });
        }
    }

    @Override
    public <T> void exportData(List<T> data, ExportFormat format, OutputStream outputStream, Map<String, Object> options) {
        Objects.requireNonNull(data, "Data cannot be null");
        Objects.requireNonNull(format, "Format cannot be null");
        Objects.requireNonNull(outputStream, "Output stream cannot be null");
        
        if (data.isEmpty()) {
            log.warn("Attempted to export empty data list");
            return;
        }
        
        ExportHandler handler = handlers.get(format);
        if (handler == null) {
            throw new UnsupportedOperationException("Export format not supported: " + format);
        }
        
        try {
            log.debug("Exporting {} items as {}", data.size(), format);
            handler.export(data, outputStream, options != null ? options : Collections.emptyMap());
            log.debug("Successfully exported {} items", data.size());
        } catch (Exception e) {
            log.error("Error exporting data as " + format, e);
            throw new ExportException("Failed to export data: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> void exportWithTemplate(List<T> data, String templateName, OutputStream outputStream, Map<String, Object> options) {
        Objects.requireNonNull(data, "Data cannot be null");
        Objects.requireNonNull(templateName, "Template name cannot be null");
        Objects.requireNonNull(outputStream, "Output stream cannot be null");
        
        try {
            log.debug("Exporting {} items with template: {}", data.size(), templateName);
            
            // Check if it's a registered template handler
            if (templateHandlers.containsKey(templateName)) {
                ExportTemplateHandler handler = templateHandlers.get(templateName);
                handler.export(data, outputStream, options != null ? options : Collections.emptyMap());
            } 
            // Otherwise, try to find a template by name
            else {
                ExportTemplate template = templateService.getTemplateByName(templateName);
                exportWithTemplate(data, template, outputStream, options);
            }
            
            log.debug("Successfully exported with template: {}", templateName);
        } catch (Exception e) {
            log.error("Error exporting with template: " + templateName, e);
            throw new ExportException("Failed to export with template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Export data using a template definition.
     */
    public <T> void exportWithTemplate(List<T> data, ExportTemplate template, OutputStream outputStream, Map<String, Object> options) {
        try {
            // Process data according to template
            List<Map<String, Object>> processedData = templateProcessor.processData(data, template);
            
            // Get the export format from template
            String formatStr = template.getFormat().toUpperCase();
            ExportFormat format;
            try {
                format = ExportFormat.valueOf(formatStr);
            } catch (IllegalArgumentException e) {
                throw new TemplateException("Unsupported export format in template: " + formatStr);
            }
            
            // Get the appropriate handler
            ExportHandler handler = handlers.get(format);
            if (handler == null) {
                throw new UnsupportedOperationException("Export format not supported: " + format);
            }
            
            // Prepare options with template-specific settings
            Map<String, Object> exportOptions = new HashMap<>();
            if (options != null) {
                exportOptions.putAll(options);
            }
            
            // Add template-specific options
            if (template.getFormatOptions() != null) {
                exportOptions.putAll(template.getFormatOptions());
            }
            
            // Add headers if this is a tabular format
            if (format == ExportFormat.CSV || format == ExportFormat.EXCEL_XLSX) {
                List<String> headers = templateProcessor.getExportHeaders(template);
                exportOptions.put("headers", headers);
            }
            
            // Export the processed data
            handler.export(processedData, outputStream, exportOptions);
            
        } catch (Exception e) {
            throw new ExportException("Error exporting with template: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExportFormat> getSupportedFormats() {
        return new ArrayList<>(handlers.keySet());
    }

    @Override
    public boolean isFormatSupported(ExportFormat format) {
        return handlers.containsKey(format);
    }
}

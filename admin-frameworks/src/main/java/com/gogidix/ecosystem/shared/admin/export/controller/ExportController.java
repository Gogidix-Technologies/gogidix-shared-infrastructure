package com.gogidix.ecosystem.shared.admin.export.controller;

import com.gogidix.ecosystem.shared.admin.export.ExportFormat;
import com.gogidix.ecosystem.shared.admin.export.ExportService;
import com.gogidix.ecosystem.shared.admin.export.ExportTemplateHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling export operations.
 */
@RestController
@RequestMapping("/api/v1/export")
@Tag(name = "Export", description = "API for exporting data in various formats")
public class ExportController {

    private static final Logger log = LoggerFactory.getLogger(ExportController.class);
    private final ExportService exportService;
    private final List<ExportTemplateHandler<?>> templateHandlers;

    public ExportController(ExportService exportService, List<ExportTemplateHandler<?>> templateHandlers) {
        this.exportService = exportService;
        this.templateHandlers = templateHandlers;
    }

    @GetMapping("/formats")
    @Operation(summary = "Get supported export formats")
    @ApiResponse(responseCode = "200", description = "List of supported export formats")
    public List<ExportFormat> getSupportedFormats() {
        return exportService.getSupportedFormats();
    }

    @PostMapping("/{format}")
    @Operation(summary = "Export data in the specified format")
    @ApiResponse(
        responseCode = "200",
        description = "Export successful",
        content = @Content(mediaType = "application/octet-stream", 
                         schema = @Schema(type = "string", format = "binary"))
    )
    public ResponseEntity<Resource> exportData(
            @Parameter(description = "Export format (e.g., CSV, EXCEL_XLSX, PDF)")
            @PathVariable ExportFormat format,
            
            @Parameter(description = "Data to export")
            @RequestBody List<Map<String, Object>> data,
            
            @Parameter(description = "Export options")
            @RequestParam(required = false) Map<String, String> options) {
        
        log.info("Exporting {} items as {}", data.size(), format);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportData(data, format, outputStream, Map.copyOf(options));
        
        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=" + generateFileName(format))
                .body(resource);
    }

    @GetMapping("/templates")
    @Operation(summary = "Get available export templates")
    @ApiResponse(responseCode = "200", description = "List of available export templates")
    public List<String> getAvailableTemplates() {
        return templateHandlers.stream()
                .map(ExportTemplateHandler::getTemplateName)
                .toList();
    }

    @PostMapping("/templates/{templateName}")
    @Operation(summary = "Export data using a specific template")
    @ApiResponse(
        responseCode = "200",
        description = "Export successful",
        content = @Content(mediaType = "application/octet-stream", 
                         schema = @Schema(type = "string", format = "binary"))
    )
    public ResponseEntity<Resource> exportWithTemplate(
            @Parameter(description = "Template name")
            @PathVariable String templateName,
            
            @Parameter(description = "Data to export")
            @RequestBody List<Map<String, Object>> data,
            
            @Parameter(description = "Template options")
            @RequestParam(required = false) Map<String, String> options) {
        
        log.info("Exporting {} items with template: {}", data.size(), templateName);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportWithTemplate(data, templateName, outputStream, Map.copyOf(options));
        
        // Get the format from the template handler
        String format = templateHandlers.stream()
                .filter(h -> h.getTemplateName().equals(templateName))
                .findFirst()
                .map(h -> h.getOutputFormat().name().toLowerCase())
                .orElse("bin");
        
        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=export_" + templateName + "." + format)
                .body(resource);
    }

    private String generateFileName(ExportFormat format) {
        return String.format("export_%d.%s", 
                System.currentTimeMillis(), 
                format.name().toLowerCase());
    }
}

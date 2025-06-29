package com.exalt.admin.components.reporting.controller;

import com.exalt.admin.components.reporting.model.BaseReport;
import com.exalt.admin.components.reporting.service.AbstractReportService;
import com.exalt.admin.util.ExportFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Abstract controller for handling reporting functionality across all admin dashboards.
 * This controller provides standardized endpoints for generating, listing, and exporting reports.
 * 
 * @param <T> Report type, which must extend BaseReport
 */
public abstract class AbstractReportController<T extends BaseReport> {

    private final AbstractReportService<T> reportService;
    
    /**
     * Constructor for AbstractReportController.
     * 
     * @param reportService The report service to use
     */
    protected AbstractReportController(AbstractReportService<T> reportService) {
        this.reportService = reportService;
    }
    
    /**
     * Get all reports.
     * 
     * @return List of reports
     */
    @GetMapping("/reports")
    public ResponseEntity<List<T>> getAllReports() {
        List<T> reports = reportService.getAllReports();
        reports = filterDomainSpecificReports(reports);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get report by ID.
     * 
     * @param id Report ID
     * @return Report or 404 if not found
     */
    @GetMapping("/reports/{id}")
    public ResponseEntity<T> getReportById(@PathVariable String id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Generate a new report.
     * 
     * @param startDate Start date for report data
     * @param endDate End date for report data
     * @param parameters Additional parameters for report generation
     * @return Generated report
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<T> generateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestBody(required = false) Map<String, Object> parameters) {
        
        // Apply domain-specific parameters
        parameters = enrichReportParameters(parameters);
        
        T report = reportService.generateReport(startDate, endDate, parameters);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Export a report in the specified format.
     * 
     * @param id Report ID
     * @param format Export format (e.g., PDF, CSV, EXCEL)
     * @return Exported report file
     */
    @GetMapping("/reports/{id}/export")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String id,
            @RequestParam ExportFormat format) {
        
        byte[] exportedReport = reportService.exportReport(id, format);
        
        // Set appropriate headers based on format
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_" + id + "." + format.toString().toLowerCase());
        
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        switch (format) {
            case PDF:
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case CSV:
                mediaType = MediaType.parseMediaType("text/csv");
                break;
            case EXCEL:
                mediaType = MediaType.parseMediaType("application/vnd.ms-excel");
                break;
        }
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(exportedReport);
    }
    
    /**
     * Apply domain-specific filtering to reports.
     * This method should be implemented by each domain controller to apply
     * domain-specific filtering to the list of reports.
     * 
     * @param reports Reports to filter
     * @return Filtered reports
     */
    protected abstract List<T> filterDomainSpecificReports(List<T> reports);
    
    /**
     * Enrich report parameters with domain-specific data.
     * This method can be overridden by domain controllers to add additional
     * domain-specific parameters to the report generation process.
     * 
     * @param parameters Original parameters
     * @return Enriched parameters
     */
    protected Map<String, Object> enrichReportParameters(Map<String, Object> parameters) {
        // Default implementation returns the original parameters
        return parameters;
    }
}

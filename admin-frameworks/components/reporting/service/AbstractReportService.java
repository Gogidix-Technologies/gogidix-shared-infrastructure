package com.exalt.admin.components.reporting.service;

import com.exalt.admin.components.reporting.model.BaseReport;
import com.exalt.admin.util.ExportFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract service for report generation and management.
 * Provides common functionality for all report services.
 * 
 * @param <T> Report type, which must extend BaseReport
 */
public abstract class AbstractReportService<T extends BaseReport> {
    
    /**
     * Get all reports.
     * 
     * @return List of all reports
     */
    public abstract List<T> getAllReports();
    
    /**
     * Get a report by ID.
     * 
     * @param id Report ID
     * @return Optional containing the report if found, empty otherwise
     */
    public abstract Optional<T> getReportById(String id);
    
    /**
     * Generate a new report.
     * 
     * @param startDate Start date for report data
     * @param endDate End date for report data
     * @param parameters Additional parameters for report generation
     * @return Generated report
     */
    public T generateReport(LocalDate startDate, LocalDate endDate, Map<String, Object> parameters) {
        // Create a new report instance
        T report = createReport(startDate, endDate, parameters);
        
        try {
            // Validate parameters
            if (!report.validateParameters()) {
                report.markFailed("Invalid report parameters");
                return report;
            }
            
            // Update status
            report.setStatus(BaseReport.ReportStatus.GENERATING);
            
            // Generate report data
            report.generate();
            
            // Mark as complete
            report.markComplete();
            
            // Apply domain-specific post-processing
            postProcessReport(report);
            
            // Save report
            saveReport(report);
            
            return report;
        } catch (Exception e) {
            report.markFailed("Error generating report: " + e.getMessage());
            saveReport(report);
            return report;
        }
    }
    
    /**
     * Export a report in the specified format.
     * 
     * @param id Report ID
     * @param format Export format (e.g., PDF, CSV, EXCEL)
     * @return Exported report as byte array
     */
    public byte[] exportReport(String id, ExportFormat format) {
        Optional<T> reportOpt = getReportById(id);
        
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Report not found: " + id);
        }
        
        T report = reportOpt.get();
        
        // Ensure report is complete
        if (report.getStatus() != BaseReport.ReportStatus.COMPLETED) {
            throw new IllegalStateException("Report is not ready for export, status: " + report.getStatus());
        }
        
        // Apply domain-specific export logic
        return doExportReport(report, format);
    }
    
    /**
     * Create a new report instance with the specified parameters.
     * This method should be implemented by concrete service classes to
     * create the appropriate report type.
     * 
     * @param startDate Start date for report data
     * @param endDate End date for report data
     * @param parameters Additional parameters for report generation
     * @return New report instance
     */
    protected abstract T createReport(LocalDate startDate, LocalDate endDate, Map<String, Object> parameters);
    
    /**
     * Save a report to the repository.
     * This method should be implemented by concrete service classes to
     * save the report to the appropriate repository.
     * 
     * @param report Report to save
     * @return Saved report
     */
    protected abstract T saveReport(T report);
    
    /**
     * Apply domain-specific post-processing to the report.
     * This method can be overridden by domain services to add additional
     * processing to the report after generation.
     * 
     * @param report Report to process
     */
    protected void postProcessReport(T report) {
        // Default implementation does nothing
    }
    
    /**
     * Export the report in the specified format.
     * This method should be implemented by concrete service classes to
     * perform the actual export.
     * 
     * @param report Report to export
     * @param format Export format
     * @return Exported report as byte array
     */
    protected abstract byte[] doExportReport(T report, ExportFormat format);
}

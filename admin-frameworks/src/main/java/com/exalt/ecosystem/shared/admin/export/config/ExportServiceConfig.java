package com.exalt.ecosystem.shared.admin.export.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.exalt.ecosystem.shared.admin.export.ExportService;
import com.exalt.ecosystem.shared.admin.export.ExportServiceImpl;
import com.exalt.ecosystem.shared.admin.export.ExportHandler;
import com.exalt.ecosystem.shared.admin.export.ExportTemplateHandler;
import com.exalt.ecosystem.shared.admin.export.handler.CsvExportHandler;
import com.exalt.ecosystem.shared.admin.export.handler.JsonExportHandler;
import com.exalt.ecosystem.shared.admin.export.handler.XmlExportHandler;
import com.exalt.ecosystem.shared.admin.export.handler.excel.ExcelExportHandler;
import com.exalt.ecosystem.shared.admin.export.handler.pdf.PdfExportHandler;
import com.exalt.ecosystem.shared.admin.export.repository.ExportTemplateRepository;
import com.exalt.ecosystem.shared.admin.export.service.ExportTemplateService;
import com.exalt.ecosystem.shared.admin.export.service.TemplateProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for the Export Service.
 * Sets up all necessary beans and components.
 */
@Configuration
public class ExportServiceConfig {

    /**
     * Creates and configures the ExportService bean.
     */
    @Bean
    public ExportService exportService(
            List<ExportHandler> exportHandlers,
            List<ExportTemplateHandler> templateHandlers,
            ExportTemplateService exportTemplateService,
            TemplateProcessor templateProcessor) {
        return new ExportServiceImpl(
                exportHandlers,
                templateHandlers,
                exportTemplateService,
                templateProcessor
        );
    }

    /**
     * Creates and configures the ExportTemplateService bean.
     */
    @Bean
    public ExportTemplateService exportTemplateService(ExportTemplateRepository templateRepository) {
        return new ExportTemplateService(templateRepository);
    }

    /**
     * Creates and configures the TemplateProcessor bean.
     */
    @Bean
    public TemplateProcessor templateProcessor(ObjectMapper objectMapper) {
        return new TemplateProcessor(objectMapper);
    }

    /**
     * Creates and configures the CSV export handler.
     */
    @Bean
    public CsvExportHandler csvExportHandler() {
        return new CsvExportHandler();
    }

    /**
     * Creates and configures the Excel export handler.
     */
    @Bean
    public ExcelExportHandler excelExportHandler() {
        return new ExcelExportHandler();
    }

    /**
     * Creates and configures the PDF export handler.
     */
    @Bean
    public PdfExportHandler pdfExportHandler() {
        return new PdfExportHandler();
    }

    /**
     * Creates and configures the JSON export handler.
     */
    @Bean
    public JsonExportHandler jsonExportHandler() {
        return new JsonExportHandler();
    }

    /**
     * Creates and configures the XML export handler.
     */
    @Bean
    public XmlExportHandler xmlExportHandler() {
        return new XmlExportHandler();
    }
}

package com.gogidix.shared.ecommerce.admin.export.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsocial.ecommerce.admin.export.ExportService;
import com.microsocial.ecommerce.admin.export.ExportServiceImpl;
import com.microsocial.ecommerce.admin.export.handler.*;
import com.microsocial.ecommerce.admin.export.repository.ExportTemplateRepository;
import com.microsocial.ecommerce.admin.export.service.ExportTemplateService;
import com.microsocial.ecommerce.admin.export.service.TemplateProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

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
    public JsonExportHandler jsonExportHandler(ObjectMapper objectMapper) {
        return new JsonExportHandler(objectMapper);
    }

    /**
     * Creates and configures the XML export handler.
     */
    @Bean
    public XmlExportHandler xmlExportHandler(ObjectMapper objectMapper) {
        return new XmlExportHandler(objectMapper);
    }
}

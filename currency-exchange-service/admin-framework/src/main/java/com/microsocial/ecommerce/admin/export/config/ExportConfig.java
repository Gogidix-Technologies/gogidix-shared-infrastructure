package com.exalt.shared.ecommerce.admin.export.config;

import com.microsocial.ecommerce.admin.export.ExportHandler;
import com.microsocial.ecommerce.admin.export.handler.JsonExportHandler;
import com.microsocial.ecommerce.admin.export.handler.XmlExportHandler;
import com.microsocial.ecommerce.admin.export.handler.excel.ExcelExportHandler;
import com.microsocial.ecommerce.admin.export.handler.pdf.PdfExportHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for export-related beans.
 */
@Configuration
public class ExportConfig {

    /**
     * Configures the list of available export handlers.
     *
     * @param excelExportHandler The Excel export handler
     * @param pdfExportHandler The PDF export handler
     * @return List of available export handlers
     */
    @Bean
    public List<ExportHandler<?>> exportHandlers(
            ExcelExportHandler<?> excelExportHandler,
            PdfExportHandler<?> pdfExportHandler,
            JsonExportHandler<?> jsonExportHandler,
            XmlExportHandler<?> xmlExportHandler) {
        return List.of(
            excelExportHandler,
            pdfExportHandler,
            jsonExportHandler,
            xmlExportHandler
        );
    }
}

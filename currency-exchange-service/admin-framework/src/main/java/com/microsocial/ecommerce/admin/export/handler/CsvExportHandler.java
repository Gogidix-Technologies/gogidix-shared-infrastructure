package com.exalt.shared.ecommerce.admin.export.handler;

import com.microsocial.ecommerce.admin.export.ExportException;
import com.microsocial.ecommerce.admin.export.ExportFormat;
import com.microsocial.ecommerce.admin.export.ExportHandler;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * CSV export handler implementation.
 */
@Component
public class CsvExportHandler<T> implements ExportHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(CsvExportHandler.class);
    
    @Override
    public void export(List<T> data, OutputStream outputStream, Map<String, Object> options) throws ExportException {
        if (data == null || data.isEmpty()) {
            log.warn("No data provided for CSV export");
            return;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
             CSVPrinter csvPrinter = new CSVPrinter(writer, getCsvFormat(options))) {
            
            // Get headers from the first item's fields
            if (!data.isEmpty()) {
                T firstItem = data.get(0);
                Field[] fields = firstItem.getClass().getDeclaredFields();
                
                // Print headers
                String[] headers = new String[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    headers[i] = fields[i].getName();
                }
                csvPrinter.printRecord((Object[]) headers);
                
                // Print data rows
                for (T item : data) {
                    Object[] row = new Object[fields.length];
                    for (int i = 0; i < fields.length; i++) {
                        try {
                            row[i] = fields[i].get(item);
                        } catch (IllegalAccessException e) {
                            row[i] = "";
                            log.warn("Could not access field: " + fields[i].getName(), e);
                        }
                    }
                    csvPrinter.printRecord(row);
                }
            }
            
            csvPrinter.flush();
            log.info("Exported {} items to CSV format", data.size());
            
        } catch (IOException e) {
            throw new ExportException("Error writing CSV data", e);
        }
    }
    
    private CSVFormat getCsvFormat(Map<String, Object> options) {
        // Default format is RFC4180 with header auto-detection
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true) // We'll write our own header
                .build();
        
        // Apply options if provided
        if (options != null) {
            if (options.containsKey("delimiter") && options.get("delimiter") instanceof Character) {
                format = format.withDelimiter((Character) options.get("delimiter"));
            }
            if (options.containsKey("withHeader") && Boolean.FALSE.equals(options.get("withHeader"))) {
                format = format.withSkipHeaderRecord(true);
            }
        }
        
        return format;
    }
    
    @Override
    public ExportFormat getFormat() {
        return ExportFormat.CSV;
    }
}

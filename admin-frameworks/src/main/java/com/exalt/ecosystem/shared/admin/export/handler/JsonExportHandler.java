package com.exalt.ecosystem.shared.admin.export.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.exalt.ecosystem.shared.admin.export.ExportException;
import com.exalt.ecosystem.shared.admin.export.ExportFormat;
import com.exalt.ecosystem.shared.admin.export.ExportHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Export handler for JSON format.
 */
@Component
public class JsonExportHandler<T> implements ExportHandler<T> {

    private final ObjectMapper objectMapper;

    public JsonExportHandler() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void export(List<T> data, OutputStream outputStream, Map<String, Object> options) throws ExportException {
        try {
            if (data == null || data.isEmpty()) {
                outputStream.write("[]".getBytes());
                return;
            }

            boolean prettyPrint = options != null && Boolean.TRUE.equals(options.get("prettyPrint"));
            
            if (prettyPrint) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, data);
            } else {
                objectMapper.writeValue(outputStream, data);
            }
            
            outputStream.flush();
        } catch (JsonProcessingException e) {
            throw new ExportException("Error converting data to JSON", e);
        } catch (IOException e) {
            throw new ExportException("Error writing JSON to output stream", e);
        }
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.JSON;
    }
}

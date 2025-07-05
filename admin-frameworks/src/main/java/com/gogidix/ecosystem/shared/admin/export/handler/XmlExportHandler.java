package com.gogidix.ecosystem.shared.admin.export.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.gogidix.ecosystem.shared.admin.export.ExportException;
import com.gogidix.ecosystem.shared.admin.export.ExportFormat;
import com.gogidix.ecosystem.shared.admin.export.ExportHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Export handler for XML format.
 */
@Component
public class XmlExportHandler<T> implements ExportHandler<T> {

    private final XmlMapper xmlMapper;

    public XmlExportHandler() {
        this.xmlMapper = new XmlMapper();
        // Configure pretty printing by default
        this.xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        this.xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_1_1);
    }

    @Override
    public void export(List<T> data, OutputStream outputStream, Map<String, Object> options) throws ExportException {
        try {
            if (data == null || data.isEmpty()) {
                outputStream.write("<list/>".getBytes());
                return;
            }

            boolean prettyPrint = options == null || !Boolean.FALSE.equals(options.get("prettyPrint"));
            
            if (prettyPrint) {
                xmlMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, data);
            } else {
                xmlMapper.writeValue(outputStream, data);
            }
            
            outputStream.flush();
        } catch (IOException e) {
            throw new ExportException("Error writing XML to output stream", e);
        }
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.XML;
    }
}

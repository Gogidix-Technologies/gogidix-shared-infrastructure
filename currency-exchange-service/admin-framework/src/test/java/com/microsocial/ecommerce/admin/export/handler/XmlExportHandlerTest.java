package com.exalt.shared.ecommerce.admin.export.handler;

import com.microsocial.ecommerce.admin.export.ExportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class XmlExportHandlerTest {

    private XmlExportHandler<TestData> xmlExportHandler;
    private final TestData testData = new TestData("test", 123, LocalDate.of(2023, 1, 1));

    @BeforeEach
    void setUp() {
        xmlExportHandler = new XmlExportHandler<>();
    }

    @Test
    void getFormat_shouldReturnXmlFormat() {
        assertEquals("XML", xmlExportHandler.getFormat().name());
    }

    @Test
    void export_shouldGenerateValidXml() throws Exception {
        // Arrange
        List<TestData> data = List.of(testData);
        OutputStream outputStream = new ByteArrayOutputStream();

        // Act
        xmlExportHandler.export(data, outputStream, null);
        String result = outputStream.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("<name>test</name>"));
        assertTrue(result.contains("<value>123</value>"));
        assertTrue(result.contains("<date>2023-01-01</date>"));
        assertTrue(result.startsWith("<?xml version='1.1' encoding='UTF-8'?>"));
    }

    @Test
    void export_withPrettyPrint_shouldGenerateFormattedXml() throws Exception {
        // Arrange
        List<TestData> data = List.of(testData);
        OutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> options = Map.of("prettyPrint", true);

        // Act
        xmlExportHandler.export(data, outputStream, options);
        String result = outputStream.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\n")); // Should contain newlines for pretty printing
        assertTrue(result.contains("<name>test</name>"));
    }

    @Test
    void export_withEmptyData_shouldGenerateEmptyList() throws Exception {
        // Arrange
        OutputStream outputStream = new ByteArrayOutputStream();

        // Act
        xmlExportHandler.export(List.of(), outputStream, null);
        String result = outputStream.toString();

        // Assert
        assertTrue(result.contains("<list/>") || result.contains("<list></list>"));
    }
    
    @Test
    void export_withNullData_shouldGenerateEmptyList() throws Exception {
        // Arrange
        OutputStream outputStream = new ByteArrayOutputStream();

        // Act
        xmlExportHandler.export(null, outputStream, null);
        String result = outputStream.toString();

        // Assert
        assertEquals("<list/>", result.trim());
    }


    @Test
    void export_whenIOException_shouldThrowExportException() {
        // Arrange
        OutputStream failingStream = new OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                throw new java.io.IOException("Test exception");
            }
        };

        // Act & Assert
        assertThrows(ExportException.class, 
            () -> xmlExportHandler.export(List.of(testData), failingStream, null));
    }

    // Test data class (must be public for XML serialization)
    public static class TestData {
        private String name;
        private int value;
        private LocalDate date;

        // Default constructor required for XML serialization
        public TestData() {
        }

        public TestData(String name, int value, LocalDate date) {
            this.name = name;
            this.value = value;
            this.date = date;
        }

        // Getters and setters required for XML serialization
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }
}

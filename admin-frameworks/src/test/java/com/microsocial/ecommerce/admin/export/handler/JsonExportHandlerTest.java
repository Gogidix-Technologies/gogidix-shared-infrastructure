package com.gogidix.ecommerce.admin.export.handler;

import com.gogidix.ecommerce.admin.export.ExportException;
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
class JsonExportHandlerTest {

    private JsonExportHandler<TestData> jsonExportHandler;
    private final TestData testData = new TestData("test", 123, LocalDate.of(2023, 1, 1));

    @BeforeEach
    void setUp() {
        jsonExportHandler = new JsonExportHandler<>();
    }

    @Test
    void getFormat_shouldReturnJsonFormat() {
        assertEquals("JSON", jsonExportHandler.getFormat().name());
    }

    @Test
    void export_shouldGenerateValidJson() throws Exception {
        // Arrange
        List<TestData> data = List.of(testData);
        OutputStream outputStream = new ByteArrayOutputStream();

        // Act
        jsonExportHandler.export(data, outputStream, null);
        String result = outputStream.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\"name\":\"test\""));
        assertTrue(result.contains("\"value\":123"));
        assertTrue(result.contains("\"date\":\"2023-01-01\""));
    }

    @Test
    void export_withPrettyPrint_shouldGenerateFormattedJson() throws Exception {
        // Arrange
        List<TestData> data = List.of(testData);
        OutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> options = Map.of("prettyPrint", true);

        // Act
        jsonExportHandler.export(data, outputStream, options);
        String result = outputStream.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\n")); // Should contain newlines for pretty printing
        assertTrue(result.contains("\"name\" : \"test\""));
    }

    @Test
    void export_withEmptyData_shouldGenerateEmptyArray() throws Exception {
        // Arrange
        OutputStream outputStream = new ByteArrayOutputStream();

        // Act
        jsonExportHandler.export(List.of(), outputStream, null);
        String result = outputStream.toString();

        // Assert
        assertEquals("[]", result.trim());
    }

    
    @Test
    void export_withNullData_shouldGenerateEmptyArray() throws Exception {
        // Arrange
        OutputStream outputStream = new ByteArrayOutputStream();

        // Act
        jsonExportHandler.export(null, outputStream, null);
        String result = outputStream.toString();

        // Assert
        assertEquals("[]", result.trim());
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
            () -> jsonExportHandler.export(List.of(testData), failingStream, null));
    }

    // Test data class
    public static class TestData {
        private String name;
        private int value;
        private LocalDate date;

        public TestData() {
            // Default constructor for JSON deserialization
        }

        public TestData(String name, int value, LocalDate date) {
            this.name = name;
            this.value = value;
            this.date = date;
        }

        // Getters and setters required for JSON serialization
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }
}

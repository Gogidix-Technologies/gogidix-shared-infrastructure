package com.gogidix.shared.ecommerce.admin.export.handler.pdf;

import com.microsocial.ecommerce.admin.export.ExportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PdfExportHandlerTest {

    private PdfExportHandler<TestData> pdfExportHandler;
    private List<TestData> testData;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        pdfExportHandler = new PdfExportHandler<>();
        testData = Arrays.asList(
            createTestData(1, "Product A", 99.99, true, "Description A"),
            createTestData(2, "Product B", 149.99, true, "Description B"),
            createTestData(3, "Product C", 199.99, false, "Description C")
        );
    }
    
    private TestData createTestData(long id, String name, double price, boolean active, String description) {
        TestData data = new TestData();
        data.setId(id);
        data.setName(name);
        data.setDescription(description);
        data.setPrice(price);
        data.setActive(active);
        data.setCreatedAt(new Date());
        data.setUpdatedAt(LocalDate.now());
        data.setLastModified(LocalDateTime.now());
        data.setAmount(new BigDecimal("1234.56"));
        return data;
    }
    
    @Test
    void export_ShouldGeneratePdfFile() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        pdfExportHandler.export(testData, outputStream, null);
        
        // Then
        byte[] result = outputStream.toByteArray();
        assertTrue(result.length > 0, "Generated PDF file should not be empty");
        
        // Verify PDF header
        assertTrue(result[0] == '%' && result[1] == 'P' && result[2] == 'D' && result[3] == 'F', 
                  "File should be a valid PDF");
    }
    
    @Test
    void export_WithCustomOptions_ShouldApplyOptions() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> options = new HashMap<>();
        options.put("title", "Custom Report Title");
        options.put("includeHeader", true);
        options.put("landscape", true);
        
        // When
        pdfExportHandler.export(testData, outputStream, options);
        
        // Then
        byte[] result = outputStream.toByteArray();
        assertTrue(result.length > 0, "Generated PDF file should not be empty");
    }
    
    @Test
    void export_WithEmptyData_ShouldNotFail() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        pdfExportHandler.export(Collections.emptyList(), outputStream, null);
        
        // Then
        // No exception should be thrown
    }
    
    @Test
    void export_WithNullData_ShouldNotFail() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        pdfExportHandler.export(null, outputStream, null);
        
        // Then
        // No exception should be thrown
    }
    
    @Test
    void export_ToFile_ShouldCreateValidPdfFile() throws Exception {
        // Given
        File outputFile = tempDir.resolve("test_export.pdf").toFile();
        
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            // When
            pdfExportHandler.export(testData, fos, null);
        }
        
        // Then
        assertTrue(outputFile.exists(), "Output file should exist");
        assertTrue(outputFile.length() > 0, "Output file should not be empty");
        
        // Verify PDF header
        byte[] fileContent = Files.readAllBytes(outputFile.toPath());
        assertTrue(fileContent[0] == '%' && fileContent[1] == 'P' && 
                  fileContent[2] == 'D' && fileContent[3] == 'F', 
                  "File should be a valid PDF");
    }
    
    @Test
    void getFormat_ShouldReturnPdf() {
        // When
        var format = pdfExportHandler.getFormat();
        
        // Then
        assertNotNull(format, "Format should not be null");
        assertEquals("PDF", format.name(), "Format should be PDF");
    }
    
    @Test
    void export_WithInheritedFields_ShouldIncludeAllFields() throws Exception {
        // Given
        class ChildData extends TestData {
            private String additionalField = "additional value";
            
            public String getAdditionalField() {
                return additionalField;
            }
            
            public void setAdditionalField(String value) {
                this.additionalField = value;
            }
        }
        
        ChildData child = new ChildData();
        child.setId(100L);
        child.setName("Child Product");
        child.setPrice(299.99);
        child.setActive(true);
        child.setAdditionalField("test value");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        pdfExportHandler.export(List.of(child), outputStream, null);
        
        // Then
        byte[] result = outputStream.toByteArray();
        assertTrue(result.length > 0, "Generated PDF file should not be empty");
    }
    
    @Test
    void export_WithNullValues_ShouldHandleGracefully() throws Exception {
        // Given
        TestData data = new TestData();
        data.setId(1L);
        data.setName(null);  // Null value
        data.setPrice(0);
        data.setActive(false);
        data.setCreatedAt(null);  // Null date
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        pdfExportHandler.export(List.of(data), outputStream, null);
        
        // Then
        byte[] result = outputStream.toByteArray();
        assertTrue(result.length > 0, "Generated PDF file should not be empty");
    }
}

package com.gogidix.shared.ecommerce.admin.export.handler.excel;

import com.microsocial.ecommerce.admin.export.ExportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExcelExportHandlerTest {

    private ExcelExportHandler<TestData> excelExportHandler;
    private List<TestData> testData;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        excelExportHandler = new ExcelExportHandler<>();
        testData = Arrays.asList(
            TestData.createSample(),
            TestData.createSample(),
            TestData.createSample()
        );
    }
    
    @Test
    void export_ShouldGenerateExcelFile() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        excelExportHandler.export(testData, outputStream, null);
        
        // Then
        byte[] result = outputStream.toByteArray();
        assertTrue(result.length > 0, "Generated Excel file should not be empty");
        
        // Verify the file starts with the ZIP header (XLSX is a ZIP archive)
        assertEquals(0x50, result[0] & 0xFF, "First byte should be 'P' (PK header)");
        assertEquals(0x4B, result[1] & 0xFF, "Second byte should be 'K' (PK header)");
    }
    
    @Test
    void export_WithCustomOptions_ShouldApplyOptions() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> options = new HashMap<>();
        options.put("sheetName", "CustomSheet");
        options.put("includeHeader", true);
        options.put("dateFormat", "yyyy/MM/dd");
        
        // When
        excelExportHandler.export(testData, outputStream, options);
        
        // Then
        byte[] result = outputStream.toByteArray();
        assertTrue(result.length > 0, "Generated Excel file should not be empty");
    }
    
    @Test
    void export_WithEmptyData_ShouldNotFail() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        excelExportHandler.export(List.of(), outputStream, null);
        
        // Then
        // No exception should be thrown
    }
    
    @Test
    void export_WithNullData_ShouldNotFail() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        excelExportHandler.export(null, outputStream, null);
        
        // Then
        // No exception should be thrown
    }
    
    @Test
    void export_WithInvalidOptions_ShouldUseDefaults() throws Exception {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> options = new HashMap<>();
        options.put("invalidOption", "value");
        
        // When
        excelExportHandler.export(testData, outputStream, options);
        
        // Then
        byte[] result = outputStream.toByteArray();
        assertTrue(result.length > 0, "Generated Excel file should not be empty");
    }
    
    @Test
    void export_ToFile_ShouldCreateValidExcelFile() throws Exception {
        // Given
        File outputFile = tempDir.resolve("test_export.xlsx").toFile();
        
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            // When
            excelExportHandler.export(testData, fos, null);
        }
        
        // Then
        assertTrue(outputFile.exists(), "Output file should exist");
        assertTrue(outputFile.length() > 0, "Output file should not be empty");
        
        // Verify the file starts with the ZIP header (XLSX is a ZIP archive)
        byte[] fileContent = Files.readAllBytes(outputFile.toPath());
        assertEquals(0x50, fileContent[0] & 0xFF, "First byte should be 'P' (PK header)");
        assertEquals(0x4B, fileContent[1] & 0xFF, "Second byte should be 'K' (PK header)");
    }
    
    @Test
    void getFormat_ShouldReturnExcelXlsx() {
        // When
        var format = excelExportHandler.getFormat();
        
        // Then
        assertNotNull(format, "Format should not be null");
        assertEquals("EXCEL_XLSX", format.name(), "Format should be EXCEL_XLSX");
    }
    
    @Test
    void export_WithInheritedFields_ShouldIncludeAllFields() throws Exception {
        // Given
        class ChildData extends TestData {
            private String additionalField = "additional value";
            
            public String getAdditionalField() {
                return additionalField;
            }
        }
        
        ChildData child = new ChildData();
        ReflectionTestUtils.setField(child, "id", 2L);
        ReflectionTestUtils.setField(child, "name", "Child Data");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        excelExportHandler.export(List.of(child), outputStream, null);
        
        // Then
        byte[] result = outputStream.toByteArray();
        assertTrue(result.length > 0, "Generated Excel file should not be empty");
    }
}

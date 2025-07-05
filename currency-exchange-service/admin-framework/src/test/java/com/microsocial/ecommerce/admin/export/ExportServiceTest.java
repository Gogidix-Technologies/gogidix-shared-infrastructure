package com.gogidix.shared.ecommerce.admin.export;

import com.microsocial.ecommerce.admin.export.handler.CsvExportHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private CsvExportHandler<Object> csvExportHandler;
    
    private ExportService exportService;
    
    @BeforeEach
    void setUp() {
        when(csvExportHandler.getFormat()).thenReturn(ExportFormat.CSV);
        exportService = new ExportServiceImpl(List.of(csvExportHandler), List.of());
    }
    
    @Test
    void exportData_shouldUseCorrectHandler() {
        // Given
        List<Object> testData = List.of(new TestData("test", 123));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When
        exportService.exportData(testData, ExportFormat.CSV, outputStream, Map.of());
        
        // Then
        verify(csvExportHandler).export(eq(testData), any(), eq(Map.of()));
    }
    
    @Test
    void getSupportedFormats_shouldReturnAllSupportedFormats() {
        // When
        var formats = exportService.getSupportedFormats();
        
        // Then
        assertFalse(formats.isEmpty());
        assertTrue(formats.contains(ExportFormat.CSV));
    }
    
    @Test
    void isFormatSupported_shouldReturnTrueForSupportedFormat() {
        // When
        boolean isSupported = exportService.isFormatSupported(ExportFormat.CSV);
        
        // Then
        assertTrue(isSupported);
    }
    
    @Test
    void isFormatSupported_shouldReturnFalseForUnsupportedFormat() {
        // When
        boolean isSupported = exportService.isFormatSupported(ExportFormat.PDF);
        
        // Then
        assertFalse(isSupported);
    }
    
    @Test
    void exportWithTemplate_shouldThrowExceptionForUnknownTemplate() {
        // Given
        List<Object> testData = List.of(new TestData("test", 123));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> {
            exportService.exportWithTemplate(testData, "unknown-template", outputStream, Map.of());
        });
    }
    
    // Test data class
    private static class TestData {
        private final String name;
        private final int value;
        
        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        // Getters needed for reflection-based access in tests
        public String getName() { return name; }
        public int getValue() { return value; }
    }
}

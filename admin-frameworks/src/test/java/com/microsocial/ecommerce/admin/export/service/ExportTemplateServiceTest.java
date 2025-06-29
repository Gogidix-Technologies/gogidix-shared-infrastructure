package com.exalt.ecommerce.admin.export.service;

import com.exalt.ecosystem.shared.admin.export.exception.TemplateException;
import com.exalt.ecosystem.shared.admin.export.model.ExportTemplate;
import com.exalt.ecosystem.shared.admin.export.service.ExportTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportTemplateServiceTest {

    @Mock
    private ExportTemplateRepository templateRepository;

    @InjectMocks
    private ExportTemplateService templateService;

    private ExportTemplate testTemplate;
    private final String templateId = "test-template-id";
    private final String templateName = "Test Template";
    private final String createdBy = "test-user";

    @BeforeEach
    void setUp() {
        // Create a test template with required fields
        testTemplate = new ExportTemplate();
        testTemplate.setId(templateId);
        testTemplate.setName(templateName);
        testTemplate.setDescription("Test template description");
        testTemplate.setFormat("CSV");
        testTemplate.setEntityType("com.example.Entity");
        
        ExportTemplate.FieldMapping fieldMapping = new ExportTemplate.FieldMapping();
        fieldMapping.setSourceField("field1");
        fieldMapping.setTargetField("Field 1");
        fieldMapping.setDisplayName("Field One");
        fieldMapping.setDataType("String");
        fieldMapping.setDisplayOrder(1);
        
        testTemplate.setFieldMappings(Collections.singletonList(fieldMapping));
        testTemplate.setActive(true);
        testTemplate.setCreatedBy(createdBy);
        testTemplate.setUpdatedBy(createdBy);
    }

    @Test
    void createTemplate_ValidTemplate_ReturnsCreatedTemplate() {
        // Given
        when(templateRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(templateRepository.save(any(ExportTemplate.class))).thenReturn(testTemplate);

        // When
        ExportTemplate created = templateService.createTemplate(testTemplate, createdBy);

        // Then
        assertNotNull(created);
        assertEquals(templateName, created.getName());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        verify(templateRepository).save(any(ExportTemplate.class));
    }

    @Test
    void createTemplate_DuplicateName_ThrowsException() {
        // Given
        when(templateRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThrows(TemplateException.class, 
            () -> templateService.createTemplate(testTemplate, createdBy));
        verify(templateRepository, never()).save(any(ExportTemplate.class));
    }

    @Test
    void updateTemplate_ValidUpdate_ReturnsUpdatedTemplate() {
        // Given
        String updatedName = "Updated Template";        
        ExportTemplate updates = new ExportTemplate();
        updates.setName(updatedName);
        updates.setDescription("Updated description");
        
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(templateRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(templateRepository.save(any(ExportTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ExportTemplate updated = templateService.updateTemplate(templateId, updates, "updated-user");

        // Then
        assertNotNull(updated);
        assertEquals(updatedName, updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals("updated-user", updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void getTemplateById_ExistingId_ReturnsTemplate() {
        // Given
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));

        // When
        ExportTemplate found = templateService.getTemplateById(templateId);

        // Then
        assertNotNull(found);
        assertEquals(templateId, found.getId());
        assertEquals(templateName, found.getName());
    }

    @Test
    void getTemplateById_NonExistingId_ThrowsException() {
        // Given
        when(templateRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TemplateException.class, 
            () -> templateService.getTemplateById("non-existing-id"));
    }

    @Test
    void getAllTemplates_ReturnsActiveTemplates() {
        // Given
        when(templateRepository.findByActiveTrue())
            .thenReturn(Collections.singletonList(testTemplate));

        // When
        List<ExportTemplate> templates = templateService.getAllTemplates();

        // Then
        assertFalse(templates.isEmpty());
        assertEquals(1, templates.size());
        assertEquals(templateId, templates.get(0).getId());
    }

    @Test
    void getTemplatesByEntityType_ReturnsMatchingTemplates() {
        // Given
        String entityType = "com.example.Entity";
        when(templateRepository.findByEntityTypeAndActiveTrue(entityType))
            .thenReturn(Collections.singletonList(testTemplate));

        // When
        List<ExportTemplate> templates = templateService.getTemplatesByEntityType(entityType);

        // Then
        assertFalse(templates.isEmpty());
        assertEquals(entityType, templates.get(0).getEntityType());
    }

    @Test
    void deleteTemplate_ExistingId_DeletesTemplate() {
        // Given
        when(templateRepository.existsById(templateId)).thenReturn(true);

        // When
        templateService.deleteTemplate(templateId);

        // Then
        verify(templateRepository).deleteById(templateId);
    }

    @Test
    void deactivateTemplate_ExistingId_DeactivatesTemplate() {
        // Given
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(ExportTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        templateService.deactivateTemplate(templateId, "deleter");
        
        // Then
        assertFalse(testTemplate.isActive());
        assertEquals("deleter", testTemplate.getUpdatedBy());
        verify(templateRepository).save(testTemplate);
    }

    @Test
    void validateTemplate_InvalidTemplate_ThrowsException() {
        // Create invalid template (missing required fields)
        ExportTemplate invalid = new ExportTemplate();
        
        // Test validation
        assertThrows(TemplateException.class, 
            () -> templateService.createTemplate(invalid, createdBy));
    }
    
    @Test
    void validateTemplate_MissingFieldMappings_ThrowsException() {
        // Create template with missing field mappings
        ExportTemplate template = new ExportTemplate();
        template.setName("Invalid Template");
        template.setFormat("CSV");
        template.setEntityType("com.example.Entity");
        template.setFieldMappings(Collections.emptyList());
        
        // Test validation
        assertThrows(TemplateException.class, 
            () -> templateService.createTemplate(template, createdBy),
            "Should throw exception when no field mappings are provided");
    }
}

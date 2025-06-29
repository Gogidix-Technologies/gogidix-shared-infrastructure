package com.exalt.shared.ecommerce.admin.export.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsocial.ecommerce.admin.export.model.ExportTemplate;
import com.microsocial.ecommerce.admin.export.service.ExportTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExportTemplateControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ExportTemplateService templateService;

    @InjectMocks
    private ExportTemplateController templateController;

    private ExportTemplate testTemplate;
    private final String templateId = "test-template-123";
    private final String templateName = "Test Template";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(templateController).build();
        
        // Create a test template
        testTemplate = new ExportTemplate();
        testTemplate.setId(templateId);
        testTemplate.setName(templateName);
        testTemplate.setDescription("Test template description");
        testTemplate.setFormat("CSV");
        testTemplate.setEntityType("com.example.Entity");
        testTemplate.setActive(true);
        testTemplate.setCreatedBy("test-user");
        testTemplate.setCreatedAt(LocalDateTime.now());
        testTemplate.setUpdatedBy("test-user");
        testTemplate.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createTemplate_ValidRequest_ReturnsCreatedTemplate() throws Exception {
        // Given
        when(templateService.createTemplate(any(ExportTemplate.class), anyString()))
            .thenReturn(testTemplate);

        // When & Then
        mockMvc.perform(post("/api/export/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTemplate))
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(templateId)))
                .andExpect(jsonPath("$.name", is(templateName)));

        verify(templateService).createTemplate(any(ExportTemplate.class), anyString());
    }

    @Test
    void getTemplate_ExistingId_ReturnsTemplate() throws Exception {
        // Given
        when(templateService.getTemplateById(templateId)).thenReturn(testTemplate);

        // When & Then
        mockMvc.perform(get("/api/export/templates/" + templateId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(templateId)))
                .andExpect(jsonPath("$.name", is(templateName)));
    }

    @Test
    void getTemplateByName_ExistingName_ReturnsTemplate() throws Exception {
        // Given
        when(templateService.getTemplateByName(templateName)).thenReturn(testTemplate);

        // When & Then
        mockMvc.perform(get("/api/export/templates/name/" + templateName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(templateName)));
    }

    @Test
    void getAllTemplates_ReturnsTemplateList() throws Exception {
        // Given
        List<ExportTemplate> templates = Arrays.asList(testTemplate);
        when(templateService.getAllTemplates()).thenReturn(templates);

        // When & Then
        mockMvc.perform(get("/api/export/templates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(templateName)));
    }

    @Test
    void getTemplatesByEntityType_ValidType_ReturnsTemplates() throws Exception {
        // Given
        String entityType = "com.example.Entity";
        List<ExportTemplate> templates = Arrays.asList(testTemplate);
        when(templateService.getTemplatesByEntityType(entityType)).thenReturn(templates);

        // When & Then
        mockMvc.perform(get("/api/export/templates/entity-type/" + entityType)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entityType", is(entityType)));
    }

    @Test
    void updateTemplate_ValidRequest_ReturnsUpdatedTemplate() throws Exception {
        // Given
        String updatedName = "Updated Template";
        testTemplate.setName(updatedName);
        
        when(templateService.updateTemplate(eq(templateId), any(ExportTemplate.class), anyString()))
            .thenReturn(testTemplate);

        // When & Then
        mockMvc.perform(put("/api/export/templates/" + templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTemplate))
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updatedName)));
    }

    @Test
    void deleteTemplate_ExistingId_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(templateService).deleteTemplate(templateId);

        // When & Then
        mockMvc.perform(delete("/api/export/templates/" + templateId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(templateService).deleteTemplate(templateId);
    }

    @Test
    void deactivateTemplate_ExistingId_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(templateService).deactivateTemplate(eq(templateId), anyString());

        // When & Then
        mockMvc.perform(post("/api/export/templates/" + templateId + "/deactivate")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());

        verify(templateService).deactivateTemplate(eq(templateId), anyString());
    }
}

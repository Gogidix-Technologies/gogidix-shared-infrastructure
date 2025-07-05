package com.gogidix.ecosystem.shared.admin.export.service;

import com.gogidix.ecosystem.shared.admin.export.exception.TemplateException;
import com.gogidix.ecosystem.shared.admin.export.model.ExportTemplate;
import com.gogidix.ecosystem.shared.admin.export.repository.ExportTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing export templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportTemplateService {

    private final ExportTemplateRepository templateRepository;

    /**
     * Create a new export template.
     */
    public ExportTemplate createTemplate(ExportTemplate template, String createdBy) {
        validateTemplate(template);
        
        // Check if template with same name already exists
        if (templateRepository.existsByNameIgnoreCase(template.getName())) {
            throw new TemplateException("A template with name " + template.getName() + " already exists");
        }
        
        template.setCreatedBy(createdBy);
        template.setUpdatedBy(createdBy);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        
        return templateRepository.save(template);
    }

    /**
     * Update an existing template.
     */
    public ExportTemplate updateTemplate(String id, ExportTemplate updates, String updatedBy) {
        ExportTemplate existing = getTemplateById(id);
        
        // Check if name is being changed and if it conflicts with another template
        if (StringUtils.hasText(updates.getName()) && 
            !updates.getName().equalsIgnoreCase(existing.getName()) &&
            templateRepository.existsByNameIgnoreCase(updates.getName())) {
            throw new TemplateException("A template with name " + updates.getName() + " already exists");
        }
        
        // Update fields if they are provided in the updates
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getFormat() != null) existing.setFormat(updates.getFormat());
        if (updates.getEntityType() != null) existing.setEntityType(updates.getEntityType());
        if (updates.getFieldMappings() != null) existing.setFieldMappings(updates.getFieldMappings());
        if (updates.getFormatOptions() != null) existing.setFormatOptions(updates.getFormatOptions());
        if (updates.getTenantId() != null) existing.setTenantId(updates.getTenantId());
        
        existing.setActive(updates.isActive());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(LocalDateTime.now());
        
        return templateRepository.save(existing);
    }

    /**
     * Get a template by ID.
     */
    public ExportTemplate getTemplateById(String id) {
        try {
            Long idLong = Long.parseLong(id);
            return templateRepository.findById(idLong)
                    .orElseThrow(() -> new TemplateException("Template not found with id: " + id));
        } catch (NumberFormatException e) {
            throw new TemplateException("Invalid template ID format: " + id);
        }
    }

    /**
     * Get a template by name.
     */
    public ExportTemplate getTemplateByName(String name) {
        return templateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new TemplateException("Template not found with name: " + name));
    }

    /**
     * Get all active templates.
     */
    public List<ExportTemplate> getAllTemplates() {
        return templateRepository.findByActiveTrue();
    }

    /**
     * Get templates by entity type.
     */
    public List<ExportTemplate> getTemplatesByEntityType(String entityType) {
        return templateRepository.findByEntityTypeAndActiveTrue(entityType);
    }

    /**
     * Delete a template by ID.
     */
    public void deleteTemplate(String id) {
        try {
            Long idLong = Long.parseLong(id);
            if (!templateRepository.existsById(idLong)) {
                throw new TemplateException("Template not found with id: " + id);
            }
            templateRepository.deleteById(idLong);
        } catch (NumberFormatException e) {
            throw new TemplateException("Invalid template ID format: " + id);
        }
    }

    /**
     * Deactivate a template (soft delete).
     */
    public void deactivateTemplate(String id, String updatedBy) {
        ExportTemplate template = getTemplateById(id);
        template.setActive(false);
        template.setUpdatedBy(updatedBy);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);
    }

    /**
     * Validate template data.
     */
    private void validateTemplate(ExportTemplate template) {
        if (!StringUtils.hasText(template.getName())) {
            throw new TemplateException("Template name is required");
        }
        if (!StringUtils.hasText(template.getFormat())) {
            throw new TemplateException("Export format is required");
        }
        if (!StringUtils.hasText(template.getEntityType())) {
            throw new TemplateException("Entity type is required");
        }
        if (template.getFieldMappings() == null || template.getFieldMappings().isEmpty()) {
            throw new TemplateException("At least one field mapping is required");
        }
        
        // Validate field mappings
        for (int i = 0; i < template.getFieldMappings().size(); i++) {
            ExportTemplate.FieldMapping mapping = template.getFieldMappings().get(i);
            if (!StringUtils.hasText(mapping.getSourceField())) {
                throw new TemplateException("Source field is required for mapping at index " + i);
            }
            if (!StringUtils.hasText(mapping.getTargetField())) {
                // Default to source field if target is not specified
                mapping.setTargetField(mapping.getSourceField());
            }
            if (!StringUtils.hasText(mapping.getDisplayName())) {
                // Default to target field if display name is not specified
                mapping.setDisplayName(mapping.getTargetField());
            }
        }
    }
}

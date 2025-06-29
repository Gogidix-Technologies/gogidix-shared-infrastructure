package com.exalt.shared.ecommerce.admin.export.repository;

import com.microsocial.ecommerce.admin.export.model.ExportTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing export templates.
 */
@Repository
public interface ExportTemplateRepository extends MongoRepository<ExportTemplate, String> {
    
    /**
     * Find all active templates.
     */
    List<ExportTemplate> findByActiveTrue();
    
    /**
     * Find templates by entity type.
     */
    List<ExportTemplate> findByEntityTypeAndActiveTrue(String entityType);
    
    /**
     * Find template by name (case-insensitive).
     */
    Optional<ExportTemplate> findByNameIgnoreCase(String name);
    
    /**
     * Check if a template with the given name exists.
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Find templates by tenant ID.
     */
    List<ExportTemplate> findByTenantId(String tenantId);
    
    /**
     * Find templates by format type.
     */
    List<ExportTemplate> findByFormatAndActiveTrue(String format);
}

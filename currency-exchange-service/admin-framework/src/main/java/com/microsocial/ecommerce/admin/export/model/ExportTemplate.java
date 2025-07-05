package com.gogidix.shared.ecommerce.admin.export.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a template configuration for custom exports.
 */
@Document(collection = "export_templates")
@Data
public class ExportTemplate {
    @Id
    private String id;
    private String name;
    private String description;
    private String format;  // JSON, XML, CSV, etc.
    private String entityType;  // The type of entity this template applies to
    private List<FieldMapping> fieldMappings;
    private Map<String, Object> formatOptions;
    private boolean active = true;
    private String createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String updatedBy;
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String tenantId;  // For multi-tenant support

    @Data
    public static class FieldMapping {
        private String sourceField;    // Field name in the source object
        private String targetField;    // Field name in the export (can be different)
        private String displayName;    // Human-readable name for the column/field
        private String dataType;       // String, Number, Date, etc.
        private String format;         // Format pattern (e.g., date format)
        private boolean visible = true;
        private int displayOrder;      // Order in which fields appear in the export
        private Map<String, Object> customAttributes; // For additional field-specific options
    }
}

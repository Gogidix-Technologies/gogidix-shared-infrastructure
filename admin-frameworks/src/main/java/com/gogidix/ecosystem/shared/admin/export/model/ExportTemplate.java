package com.gogidix.ecosystem.shared.admin.export.model;

import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a template configuration for custom exports.
 */
@Entity
@Table(name = "export_templates")
@Data
public class ExportTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String format;  // JSON, XML, CSV, etc.
    private String entityType;  // The type of entity this template applies to
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<FieldMapping> fieldMappings;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
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

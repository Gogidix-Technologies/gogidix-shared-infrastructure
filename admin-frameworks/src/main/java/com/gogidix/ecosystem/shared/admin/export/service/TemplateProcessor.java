package com.gogidix.ecosystem.shared.admin.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogidix.ecosystem.shared.admin.export.model.ExportTemplate;
import com.gogidix.ecosystem.shared.admin.export.exception.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Processes data according to export templates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateProcessor {

    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * Process data according to the specified template.
     *
     * @param data The data to process
     * @param template The template to apply
     * @param <T> The type of the input data
     * @return A list of maps representing the processed data
     */
    public <T> List<Map<String, Object>> processData(List<T> data, ExportTemplate template) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }

        return data.stream()
                .map(item -> processItem(item, template))
                .collect(Collectors.toList());
    }

    /**
     * Process a single data item according to the template.
     */
    private <T> Map<String, Object> processItem(T item, ExportTemplate template) {
        Map<String, Object> result = new LinkedHashMap<>();
        StandardEvaluationContext context = new StandardEvaluationContext(item);

        for (ExportTemplate.FieldMapping mapping : template.getFieldMappings()) {
            if (!mapping.isVisible()) {
                continue; // Skip hidden fields
            }

            try {
                Object value = evaluateExpression(context, mapping.getSourceField(), item);
                
                // Apply formatting if specified
                if (mapping.getFormat() != null && value != null) {
                    value = formatValue(value, mapping.getFormat(), mapping.getDataType());
                }
                
                result.put(mapping.getTargetField(), value);
            } catch (Exception e) {
                log.warn("Error processing field {}: {}", mapping.getSourceField(), e.getMessage());
                result.put(mapping.getTargetField(), null);
            }
        }

        return result;
    }

    /**
     * Evaluate a SpEL expression against the given context.
     */
    private <T> Object evaluateExpression(StandardEvaluationContext context, String expression, T item) {
        try {
            // Convert item to map for simpler property access
            if (expression.startsWith("#")) {
                // This is a SpEL expression, evaluate it directly
                Expression exp = expressionParser.parseExpression(expression);
                return exp.getValue(context);
            } else {
                // Simple property access
                return objectMapper.convertValue(item, Map.class).get(expression);
            }
        } catch (Exception e) {
            throw new TemplateException("Error evaluating expression: " + expression, e);
        }
    }

    /**
     * Format a value according to the specified format and data type.
     */
    private Object formatValue(Object value, String format, String dataType) {
        if (value == null) {
            return null;
        }

        // Implement formatting based on data type
        try {
            if ("date".equalsIgnoreCase(dataType) && value instanceof String) {
                // Handle date formatting
                // In a real implementation, you would parse the date string and format it
                // For now, just return the value as is
                return value;
            } else if ("number".equalsIgnoreCase(dataType) && value instanceof Number) {
                // Handle number formatting
                // In a real implementation, you would apply the format pattern
                return String.format(format, value);
            }
            // Add more data type specific formatting as needed
        } catch (Exception e) {
            log.warn("Error formatting value {}: {}", value, e.getMessage());
        }

        return value;
    }

    /**
     * Get the headers for the export based on the template.
     */
    public List<String> getExportHeaders(ExportTemplate template) {
        return template.getFieldMappings().stream()
                .filter(ExportTemplate.FieldMapping::isVisible)
                .sorted(Comparator.comparingInt(ExportTemplate.FieldMapping::getDisplayOrder))
                .map(mapping -> 
                    StringUtils.hasText(mapping.getDisplayName()) ? 
                    mapping.getDisplayName() : mapping.getTargetField())
                .collect(Collectors.toList());
    }
}

package com.exalt.shared.ecommerce.admin.export.controller;

import com.microsocial.ecommerce.admin.export.model.ExportTemplate;
import com.microsocial.ecommerce.admin.export.service.ExportTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing export templates.
 */
@RestController
@RequestMapping("/api/export/templates")
@RequiredArgsConstructor
@Tag(name = "Export Templates", description = "APIs for managing export templates")
public class ExportTemplateController {

    private final ExportTemplateService templateService;

    @PostMapping
    @Operation(summary = "Create a new export template")
    public ResponseEntity<ExportTemplate> createTemplate(
            @Valid @RequestBody ExportTemplate template,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(templateService.createTemplate(template, username));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing template")
    public ResponseEntity<ExportTemplate> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody ExportTemplate template,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return ResponseEntity.ok(templateService.updateTemplate(id, template, username));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a template by ID")
    public ResponseEntity<ExportTemplate> getTemplate(
            @Parameter(description = "ID of the template to retrieve") 
            @PathVariable String id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get a template by name")
    public ResponseEntity<ExportTemplate> getTemplateByName(
            @Parameter(description = "Name of the template to retrieve") 
            @PathVariable String name) {
        return ResponseEntity.ok(templateService.getTemplateByName(name));
    }

    @GetMapping
    @Operation(summary = "Get all active templates")
    public ResponseEntity<List<ExportTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/entity-type/{entityType}")
    @Operation(summary = "Get templates by entity type")
    public ResponseEntity<List<ExportTemplate>> getTemplatesByEntityType(
            @Parameter(description = "Entity type to filter templates") 
            @PathVariable String entityType) {
        return ResponseEntity.ok(templateService.getTemplatesByEntityType(entityType));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a template by ID")
    public void deleteTemplate(
            @Parameter(description = "ID of the template to delete") 
            @PathVariable String id) {
        templateService.deleteTemplate(id);
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate a template (soft delete)")
    public void deactivateTemplate(
            @Parameter(description = "ID of the template to deactivate") 
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        templateService.deactivateTemplate(id, username);
    }
}

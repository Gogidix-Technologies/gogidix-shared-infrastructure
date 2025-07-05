package com.gogidix.ecommerce.admin.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogidix.ecosystem.shared.admin.export.exception.TemplateException;
import com.gogidix.ecosystem.shared.admin.export.model.ExportTemplate;
import com.gogidix.ecosystem.shared.admin.export.service.TemplateProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateProcessorTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TemplateProcessor templateProcessor;

    private ExportTemplate template;
    private TestUser testUser;

    @BeforeEach
    void setUp() {
        // Create a test template
        template = new ExportTemplate();
        template.setId("test-template");
        template.setName("User Export");
        template.setFormat("CSV");
        template.setEntityType("com.example.User");

        // Create field mappings
        ExportTemplate.FieldMapping idMapping = new ExportTemplate.FieldMapping();
        idMapping.setSourceField("id");
        idMapping.setTargetField("userId");
        idMapping.setDisplayName("User ID");
        idMapping.setDataType("number");
        idMapping.setDisplayOrder(1);

        ExportTemplate.FieldMapping nameMapping = new ExportTemplate.FieldMapping();
        nameMapping.setSourceField("fullName");
        nameMapping.setTargetField("name");
        nameMapping.setDisplayName("Full Name");
        nameMapping.setDataType("string");
        nameMapping.setDisplayOrder(2);

        ExportTemplate.FieldMapping emailMapping = new ExportTemplate.FieldMapping();
        emailMapping.setSourceField("email");
        emailMapping.setTargetField("email");
        emailMapping.setDisplayName("Email Address");
        emailMapping.setDataType("string");
        emailMapping.setDisplayOrder(3);

        ExportTemplate.FieldMapping joinDateMapping = new ExportTemplate.FieldMapping();
        joinDateMapping.setSourceField("joinDate");
        joinDateMapping.setTargetField("memberSince");
        joinDateMapping.setDisplayName("Member Since");
        joinDateMapping.setDataType("date");
        joinDateMapping.setFormat("yyyy-MM-dd");
        joinDateMapping.setDisplayOrder(4);

        // Add a computed field
        ExportTemplate.FieldMapping statusMapping = new ExportTemplate.FieldMapping();
        statusMapping.setSourceField("#active ? 'Active' : 'Inactive'");
        statusMapping.setTargetField("status");
        statusMapping.setDisplayName("Account Status");
        statusMapping.setDataType("string");
        statusMapping.setDisplayOrder(5);

        template.setFieldMappings(Arrays.asList(
            idMapping, nameMapping, emailMapping, joinDateMapping, statusMapping
        ));

        // Create a test user
        testUser = new TestUser();
        testUser.setId(123L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setActive(true);
        testUser.setJoinDate(LocalDate.of(2020, 1, 15));
    }

    @Test
    void processData_WithValidTemplate_ReturnsProcessedData() {
        // Given
        List<TestUser> users = List.of(testUser);

        // When
        List<Map<String, Object>> result = templateProcessor.processData(users, template);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Map<String, Object> processedUser = result.get(0);
        assertEquals(123L, processedUser.get("userId"));
        assertEquals("John Doe", processedUser.get("name"));
        assertEquals("john.doe@example.com", processedUser.get("email"));
        assertEquals("2020-01-15", processedUser.get("memberSince").toString());
        assertEquals("Active", processedUser.get("status"));
    }

    @Test
    void processData_WithNullData_ReturnsEmptyList() {
        // When
        List<Map<String, Object>> result = templateProcessor.processData(null, template);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void processData_WithEmptyData_ReturnsEmptyList() {
        // When
        List<Map<String, Object>> result = templateProcessor.processData(List.of(), template);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getExportHeaders_ReturnsHeadersInDisplayOrder() {
        // When
        List<String> headers = templateProcessor.getExportHeaders(template);

        // Then
        assertNotNull(headers);
        assertEquals(5, headers.size());
        assertEquals("User ID", headers.get(0));
        assertEquals("Full Name", headers.get(1));
        assertEquals("Email Address", headers.get(2));
        assertEquals("Member Since", headers.get(3));
        assertEquals("Account Status", headers.get(4));
    }

    @Test
    void processData_WithHiddenField_ExcludesHiddenField() {
        // Given
        template.getFieldMappings().get(2).setVisible(false); // Hide email field
        List<TestUser> users = List.of(testUser);

        // When
        List<Map<String, Object>> result = templateProcessor.processData(users, template);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        Map<String, Object> processedUser = result.get(0);
        assertFalse(processedUser.containsKey("email")); // Email should be excluded
        assertTrue(processedUser.containsKey("userId")); // Other fields should be included
    }

    // Helper test class
    public static class TestUser {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private boolean active;
        private LocalDate joinDate;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public LocalDate getJoinDate() { return joinDate; }
        public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
        
        // Computed property
        public String getFullName() {
            return firstName + " " + lastName;
        }
    }
}

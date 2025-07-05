package com.gogidix.ecommerce.admin.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogidix.ecommerce.admin.export.config.TestConfig;
import com.gogidix.ecommerce.admin.export.handler.*;
import com.gogidix.ecommerce.admin.export.model.ExportTemplate;
import com.gogidix.ecommerce.admin.export.service.ExportTemplateService;
import com.gogidix.ecommerce.admin.export.service.TemplateProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for the ExportService.
 * Tests the complete export flow with template processing.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ExportService.class, ExportServiceImpl.class})
@Import(TestConfig.class)
class ExportServiceIntegrationTest {

    @Autowired
    private ExportService exportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ExportTemplateService templateService;

    @Autowired
    private List<ExportHandler> exportHandlers;

    private TestUser testUser;
    private ExportTemplate testTemplate;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new TestUser();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setActive(true);
        testUser.setJoinDate(LocalDate.of(2020, 1, 15));

        // Create a test template
        testTemplate = new ExportTemplate();
        testTemplate.setId("test-template");
        testTemplate.setName("User Export");
        testTemplate.setFormat("CSV");
        testTemplate.setEntityType(TestUser.class.getName());

        // Set up field mappings
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
        emailMapping.setDisplayName("Email");
        emailMapping.setDataType("string");
        emailMapping.setDisplayOrder(3);

        ExportTemplate.FieldMapping joinDateMapping = new ExportTemplate.FieldMapping();
        joinDateMapping.setSourceField("joinDate");
        joinDateMapping.setTargetField("memberSince");
        joinDateMapping.setDisplayName("Member Since");
        joinDateMapping.setDataType("date");
        joinDateMapping.setFormat("yyyy-MM-dd");
        joinDateMapping.setDisplayOrder(4);

        ExportTemplate.FieldMapping statusMapping = new ExportTemplate.FieldMapping();
        statusMapping.setSourceField("#active ? 'Active' : 'Inactive'");
        statusMapping.setTargetField("status");
        statusMapping.setDisplayName("Status");
        statusMapping.setDataType("string");
        statusMapping.setDisplayOrder(5);

        testTemplate.setFieldMappings(List.of(
            idMapping, nameMapping, emailMapping, joinDateMapping, statusMapping
        ));

        // Mock template service
        when(templateService.getTemplateByName(anyString())).thenReturn(testTemplate);
    }

    @Test
    void exportWithTemplate_ValidData_GeneratesCsv() throws Exception {
        // Given
        List<TestUser> users = List.of(testUser);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        exportService.exportWithTemplate(users, "user-export-template", outputStream, null);

        // Then
        String result = outputStream.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify CSV header
        assertTrue(result.contains("User ID,Full Name,Email,Member Since,Status"));
        // Verify data
        assertTrue(result.contains("1,John Doe,john.doe@example.com,2020-01-15,Active"));
    }

    @Test
    void exportWithTemplate_JsonFormat_GeneratesJson() throws Exception {
        // Given
        testTemplate.setFormat("JSON");
        List<TestUser> users = List.of(testUser);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        exportService.exportWithTemplate(users, "user-json-export", outputStream, Map.of("prettyPrint", true));

        // Then
        String result = outputStream.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify JSON structure
        assertTrue(result.contains("\"userId\": 1"));
        assertTrue(result.contains("\"name\": \"John Doe\""));
        assertTrue(result.contains("\"email\": \"john.doe@example.com\""));
        assertTrue(result.contains("\"memberSince\": \"2020-01-15\""));
        assertTrue(result.contains("\"status\": \"Active\""));
    }

    @Test
    void exportWithTemplate_XmlFormat_GeneratesXml() throws Exception {
        // Given
        testTemplate.setFormat("XML");
        List<TestUser> users = List.of(testUser);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        exportService.exportWithTemplate(users, "user-xml-export", outputStream, Map.of("prettyPrint", true));

        // Then
        String result = outputStream.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify XML structure
        assertTrue(result.contains("<item>"));
        assertTrue(result.contains("<userId>1</userId>"));
        assertTrue(result.contains("<name>John Doe</name>"));
        assertTrue(result.contains("<email>john.doe@example.com</email>"));
        assertTrue(result.contains("<memberSince>2020-01-15</memberSince>"));
        assertTrue(result.contains("<status>Active</status>"));
    }

    // Test helper class
    public static class TestUser {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private boolean active;
        private LocalDate joinDate;

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

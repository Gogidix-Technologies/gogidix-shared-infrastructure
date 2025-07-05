package com.gogidix.shared.ecommerce.admin.export.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/export/templates");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void handleTemplateException_ReturnsBadRequest() {
        // Given
        String errorMessage = "Template validation failed";
        TemplateException ex = new TemplateException(errorMessage);

        // When
        ResponseEntity<Object> response = exceptionHandler.handleTemplateException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof java.util.Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(errorMessage, body.get("message"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals(400, body.get("status"));
        assertEquals("/api/export/templates", body.get("path"));
    }

    @Test
    void handleExportException_ReturnsInternalServerError() {
        // Given
        String errorMessage = "Export processing failed";
        ExportException ex = new ExportException(errorMessage);

        // When
        ResponseEntity<Object> response = exceptionHandler.handleExportException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof java.util.Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(errorMessage, body.get("message"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals(500, body.get("status"));
    }

    @Test
    void handleAllUncaughtException_ReturnsInternalServerError() {
        // Given
        String errorMessage = "Unexpected error occurred";
        Exception ex = new RuntimeException(errorMessage);

        // When
        ResponseEntity<Object> response = exceptionHandler.handleAllUncaughtException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof java.util.Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("An unexpected error occurred", body.get("message"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals(500, body.get("status"));
    }

    @Test
    void handleMethodArgumentNotValid_ReturnsBadRequest() {
        // Given
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getMessage()).thenReturn("Validation failed");
        
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // When
        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
            ex, headers, status, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof java.util.Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Validation Error", body.get("error"));
        assertEquals(400, body.get("status"));
    }

    @Test
    void handleConstraintViolationException_ReturnsBadRequest() {
        // Given
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        when(ex.getMessage()).thenReturn("Validation failed: name must not be empty");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleAllUncaughtException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}

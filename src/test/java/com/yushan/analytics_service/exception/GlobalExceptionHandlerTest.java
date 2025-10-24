package com.yushan.analytics_service.exception;

import com.yushan.analytics_service.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        ResponseEntity<ApiResponse<String>> response = 
                globalExceptionHandler.handleResourceNotFoundException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getCode());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void testHandleValidationException() {
        ValidationException exception = new ValidationException("Validation failed");

        ResponseEntity<ApiResponse<String>> response = 
                globalExceptionHandler.handleValidationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void testHandleUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException("Unauthorized access");

        ResponseEntity<ApiResponse<String>> response = 
                globalExceptionHandler.handleUnauthorizedException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getCode());
        assertEquals("Unauthorized access", response.getBody().getMessage());
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        ResponseEntity<ApiResponse<String>> response = 
                globalExceptionHandler.handleAccessDeniedException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Access denied"));
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "Field error message");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

        ResponseEntity<ApiResponse<Map<String, String>>> response = 
                globalExceptionHandler.handleMethodArgumentNotValid(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ApiResponse<String>> response = 
                globalExceptionHandler.handleIllegalArgumentException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getCode());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalStateException() {
        IllegalStateException exception = new IllegalStateException("Illegal state");

        ResponseEntity<ApiResponse<String>> response = 
                globalExceptionHandler.handleIllegalStateException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getCode());
        assertEquals("Illegal state", response.getBody().getMessage());
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new Exception("Generic error occurred");

        ResponseEntity<ApiResponse<String>> response = 
                globalExceptionHandler.handleGlobalException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("An unexpected error occurred"));
    }
}


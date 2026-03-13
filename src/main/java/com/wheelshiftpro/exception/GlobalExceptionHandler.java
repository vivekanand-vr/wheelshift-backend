package com.wheelshiftpro.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for REST API.
 * Provides standardized error responses following RFC7807 Problem Details.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .code("RESOURCE_NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, 
            HttpServletRequest request) {
        
        log.error("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Business Rule Violation")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .code(ex.getErrorCode())
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle DuplicateResourceException
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex, 
            HttpServletRequest request) {
        
        log.error("Duplicate resource: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Duplicate Resource")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .code("DUPLICATE_RESOURCE")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        log.error("Validation failed: {}", ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            validationErrors.add(ErrorResponse.ValidationError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue)
                    .build());
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Validation Failed")
                .status(422)
                .detail("One or more fields have validation errors")
                .instance(request.getRequestURI())
                .code("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .errors(validationErrors)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(422));
    }

    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, 
            HttpServletRequest request) {
        
        log.error("Constraint violation: {}", ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> {
            validationErrors.add(ErrorResponse.ValidationError.builder()
                    .field(violation.getPropertyPath().toString())
                    .message(violation.getMessage())
                    .rejectedValue(violation.getInvalidValue())
                    .build());
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Constraint Violation")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("One or more constraints were violated")
                .instance(request.getRequestURI())
                .code("CONSTRAINT_VIOLATION")
                .timestamp(LocalDateTime.now())
                .errors(validationErrors)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle NoResourceFoundException (static resources not found, 404 errors)
     * Only returns JSON for API routes. Non-API routes will show HTML error pages.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, 
            HttpServletRequest request) throws NoResourceFoundException {
        
        String requestUri = request.getRequestURI();
        
        // Only handle API routes with JSON response
        if (requestUri.startsWith("/api")) {
            log.warn("API endpoint not found: {}", requestUri);
            
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .type("about:blank")
                    .title("API Endpoint Not Found")
                    .status(HttpStatus.NOT_FOUND.value())
                    .detail("The requested API endpoint '" + requestUri + "' does not exist")
                    .instance(requestUri)
                    .code("API_ENDPOINT_NOT_FOUND")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        
        // For non-API routes, re-throw to let Spring's error handling show HTML error pages
        log.info("Non-API resource not found: {}, delegating to default error handling", requestUri);
        throw ex;
    }

    /**
     * Handle data integrity violations (foreign key, unique constraints, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, 
            HttpServletRequest request) {
        
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        
        String detail = "Data integrity constraint violation. Please check your data.";
        String code = "DATA_INTEGRITY_VIOLATION";
        
        // Try to provide more specific error message
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("foreign key")) {
                detail = "The referenced resource does not exist or cannot be deleted due to existing references.";
                code = "FOREIGN_KEY_VIOLATION";
            } else if (ex.getMessage().contains("unique")) {
                detail = "A resource with the same unique identifier already exists.";
                code = "UNIQUE_CONSTRAINT_VIOLATION";
            }
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Data Integrity Violation")
                .status(HttpStatus.CONFLICT.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .code(code)
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle invalid data access API usage (transient entity references, etc.)
     */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataAccessApiUsageException(
            InvalidDataAccessApiUsageException ex, 
            HttpServletRequest request) {
        
        log.error("Invalid data access API usage: {}", ex.getMessage(), ex);
        
        String detail = "Invalid data operation. Please ensure all referenced resources exist.";
        String code = "INVALID_DATA_OPERATION";
        
        // Check for common issues
        if (ex.getMessage() != null && ex.getMessage().contains("transient")) {
            detail = "Referenced resource must be saved before this operation. Please ensure the resource exists in the database.";
            code = "TRANSIENT_ENTITY_REFERENCE";
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Invalid Data Operation")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .code(code)
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle File Storage Related Exceptions
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(
            FileStorageException ex, 
            HttpServletRequest request) {
        
        log.error("File storage error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("File Storage Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .code("FILE_STORAGE_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle HTTP method not supported (e.g., GET instead of POST)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, 
            HttpServletRequest request) {
        
        log.warn("HTTP method not supported: {} for {}", ex.getMethod(), request.getRequestURI());
        
        String supportedMethods = ex.getSupportedHttpMethods() != null 
                ? String.join(", ", ex.getSupportedHttpMethods().stream().map(Object::toString).toList())
                : "Unknown";
        
        String detail = String.format("Request method '%s' is not supported for this endpoint. Supported methods: %s", 
                ex.getMethod(), supportedMethods);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Method Not Allowed")
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .code("METHOD_NOT_ALLOWED")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle unsupported media type (e.g., sending XML when only JSON is supported)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, 
            HttpServletRequest request) {
        
        log.warn("Media type not supported: {} for {}", ex.getContentType(), request.getRequestURI());
        
        String supportedTypes = ex.getSupportedMediaTypes() != null 
                ? ex.getSupportedMediaTypes().stream().map(Object::toString).collect(java.util.stream.Collectors.joining(", "))
                : "Unknown";
        
        String detail = String.format("Content type '%s' is not supported. Supported types: %s", 
                ex.getContentType(), supportedTypes);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Unsupported Media Type")
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .code("UNSUPPORTED_MEDIA_TYPE")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle missing required request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, 
            HttpServletRequest request) {
        
        log.warn("Missing required parameter: {} for {}", ex.getParameterName(), request.getRequestURI());
        
        String detail = String.format("Required parameter '%s' of type '%s' is missing", 
                ex.getParameterName(), ex.getParameterType());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Missing Required Parameter")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .code("MISSING_PARAMETER")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle method argument type mismatch (e.g., passing string when integer expected)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, 
            HttpServletRequest request) {
        
        log.warn("Method argument type mismatch: {} for {}", ex.getName(), request.getRequestURI());
        
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String detail = String.format("Parameter '%s' with value '%s' could not be converted to type '%s'", 
                ex.getName(), ex.getValue(), requiredType);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Invalid Parameter Type")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .code("INVALID_PARAMETER_TYPE")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle session expiry specifically
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpiredException(
            SessionExpiredException ex, 
            HttpServletRequest request) {
        
        log.warn("Session expired for {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Session Expired")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail("Your session has expired. Please login again.")
                .instance(request.getRequestURI())
                .code("SESSION_EXPIRED")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle insufficient permissions (403 Forbidden)
     */
    @ExceptionHandler(InsufficientPermissionException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermissionException(
            InsufficientPermissionException ex, 
            HttpServletRequest request) {
        
        log.warn("Insufficient permissions for {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Insufficient Permissions")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You do not have sufficient permissions to access this resource.")
                .instance(request.getRequestURI())
                .code("INSUFFICIENT_PERMISSIONS")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle Spring Security AccessDeniedException (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, 
            HttpServletRequest request) {
        
        log.warn("Access denied for {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Access Denied")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You do not have permission to access this resource.")
                .instance(request.getRequestURI())
                .code("ACCESS_DENIED")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle authorization denied (fallback for other auth issues)
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
            AuthorizationDeniedException ex, 
            HttpServletRequest request) {
        
        log.warn("Authorization denied for {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Authorization Failed")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail("Authorization failed. Please login and try again.")
                .instance(request.getRequestURI())
                .code("AUTHORIZATION_FAILED")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle authentication exceptions (login failures, etc.)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, 
            HttpServletRequest request) {
        
        log.warn("Authentication failed for {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Authentication Failed")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail("Authentication failed. Please check your credentials and try again.")
                .instance(request.getRequestURI())
                .code("AUTHENTICATION_FAILED")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle IllegalArgumentException (business rule violations that shouldn't happen)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, 
            HttpServletRequest request) {
        
        log.error("Illegal argument error: {}", ex.getMessage(), ex);
        
        // Provide a clear error message based on the exception
        String detail = ex.getMessage() != null && !ex.getMessage().isEmpty() 
                ? ex.getMessage() 
                : "The requested operation is not allowed due to business rules.";
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Operation Not Allowed")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .code("OPERATION_NOT_ALLOWED")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please try again later.")
                .instance(request.getRequestURI())
                .code("INTERNAL_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

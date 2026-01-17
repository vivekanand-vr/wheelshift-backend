package com.wheelshiftpro.exception;

/**
 * Exception thrown when user doesn't have permission to access a resource
 */
public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException(String message) {
        super(message);
    }
    
    public InsufficientPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
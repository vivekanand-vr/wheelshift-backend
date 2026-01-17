package com.wheelshiftpro.exception;

/**
 * Exception thrown when user session has expired
 */
public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException(String message) {
        super(message);
    }
    
    public SessionExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
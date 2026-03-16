package com.wheelshiftpro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheelshiftpro.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom handler for Authentication (401) exceptions in Spring Security.
 * Handles cases where a user is not authenticated (not logged in).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("Authentication failed for request to {}: {}", request.getRequestURI(), authException.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Authentication Required")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail("You must be logged in to access this resource. Please provide valid authentication credentials.")
                .instance(request.getRequestURI())
                .code("AUTHENTICATION_REQUIRED")
                .timestamp(LocalDateTime.now())
                .build();
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

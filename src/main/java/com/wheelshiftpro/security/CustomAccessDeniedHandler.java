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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom handler for Access Denied (403) exceptions in Spring Security.
 * Handles cases where a user is authenticated but lacks required permissions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, 
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("Access denied for user at {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("about:blank")
                .title("Access Denied")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You are not allowed to access this resource. Please contact your administrator if you believe this is an error.")
                .instance(request.getRequestURI())
                .code("ACCESS_DENIED")
                .timestamp(LocalDateTime.now())
                .build();
        
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

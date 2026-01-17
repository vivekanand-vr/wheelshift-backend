package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.LoginRequest;
import com.wheelshiftpro.dto.response.AuthResponse;
import com.wheelshiftpro.dto.response.SessionValidationResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.exception.SessionExpiredException;
import com.wheelshiftpro.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate an employee and return user details with roles and permissions")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());

            // First check if employee exists
            Employee employee = employeeRepository.findByEmail(request.getEmail())
                    .orElse(null);
            
            if (employee == null) {
                log.warn("Login failed - User not found: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(AuthResponse.builder()
                                .message("User not registered or not found")
                                .build());
            }

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            // Store authentication in security context
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            // Save security context to session
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            
            log.info("Authentication successful and session created for: {}", request.getEmail());

            // Extract roles and permissions
            Set<RoleType> roles = employee.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());

            Set<String> permissions = employee.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet());

            AuthResponse response = AuthResponse.builder()
                    .employeeId(employee.getId())
                    .email(employee.getEmail())
                    .name(employee.getName())
                    .roles(roles)
                    .permissions(permissions)
                    .message("Login successful")
                    .tokenType("Bearer")
                    .build();

            log.info("Login successful for employee: {}", employee.getId());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {} - Invalid password", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("Invalid password")
                            .build());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user details")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Set<RoleType> roles = employee.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        Set<String> permissions = employee.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        AuthResponse response = AuthResponse.builder()
                .employeeId(employee.getId())
                .email(employee.getEmail())
                .name(employee.getName())
                .roles(roles)
                .permissions(permissions)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-session")
    @Operation(summary = "Validate session", description = "Check if current session is valid and active")
    public ResponseEntity<SessionValidationResponse> validateSession(
            Authentication authentication, 
            HttpServletRequest request) {
        
        HttpSession session = request.getSession(false);
        
        // Check if session exists
        if (session == null) {
            log.warn("Session validation failed: No session found");
            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(false)
                    .expired(true)
                    .message("No active session found")
                    .errorCode("NO_SESSION")
                    .build());
        }
        
        // Check if authentication is valid
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Session validation failed: No valid authentication");
            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(false)
                    .expired(true)
                    .message("Session expired or invalid")
                    .errorCode("SESSION_EXPIRED")
                    .build());
        }
        
        try {
            String email = authentication.getName();
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new SessionExpiredException("Employee not found for authenticated user"));
            
            log.debug("Session validation successful for employee: {}", employee.getId());
            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(true)
                    .expired(false)
                    .message("Session is valid")
                    .employeeId(employee.getId())
                    .email(employee.getEmail())
                    .build());
                    
        } catch (Exception e) {
            log.error("Session validation error: {}", e.getMessage());
            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(false)
                    .expired(true)
                    .message("Session validation failed")
                    .errorCode("VALIDATION_ERROR")
                    .build());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current user")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        // Clear security context
        SecurityContextHolder.clearContext();
        
        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        log.info("Logout successful");
        return ResponseEntity.ok(AuthResponse.builder()
                .message("Logout successful")
                .build());
    }
}

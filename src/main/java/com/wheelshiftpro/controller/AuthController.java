package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.LoginRequest;
import com.wheelshiftpro.dto.response.AuthResponse;
import com.wheelshiftpro.dto.response.SessionValidationResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.exception.SessionExpiredException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.security.JwtTokenProvider;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "JWT-based authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate an employee and return JWT token with user details, roles and permissions")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
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
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate JWT token
            String jwt = jwtTokenProvider.generateToken(authentication);
            
            log.info("Authentication successful and JWT token generated for: {}", request.getEmail());

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
                    .accessToken(jwt)
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
    @Operation(summary = "Validate token", description = "Check if current JWT token is valid and active")
    public ResponseEntity<SessionValidationResponse> validateSession(Authentication authentication) {
        
        // Check if authentication is valid
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Token validation failed: No valid authentication");
            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(false)
                    .expired(true)
                    .message("Token expired or invalid")
                    .errorCode("TOKEN_EXPIRED")
                    .build());
        }
        
        try {
            String email = authentication.getName();
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new SessionExpiredException("Employee not found for authenticated user"));
            
            log.debug("Token validation successful for employee: {}", employee.getId());
            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(true)
                    .expired(false)
                    .message("Token is valid")
                    .employeeId(employee.getId())
                    .email(employee.getEmail())
                    .build());
                    
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.ok(SessionValidationResponse.builder()
                    .valid(false)
                    .expired(true)
                    .message("Token validation failed")
                    .errorCode("VALIDATION_ERROR")
                    .build());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current user (client should discard JWT token)")
    public ResponseEntity<AuthResponse> logout() {
        // Clear security context
        SecurityContextHolder.clearContext();
        
        log.info("Logout successful");
        return ResponseEntity.ok(AuthResponse.builder()
                .message("Logout successful. Please discard the JWT token on client side.")
                .build());
    }
}

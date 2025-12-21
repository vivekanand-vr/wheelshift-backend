package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.LoginRequest;
import com.wheelshiftpro.dto.response.AuthResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.RoleType;
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
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate an employee and return user details with roles and permissions")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            log.info(authentication.isAuthenticated() ? "Authentication successful" : "Authentication failed");
            // Fetch employee details
            Employee employee = employeeRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

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
            log.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("Invalid email or password")
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

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current user")
    public ResponseEntity<AuthResponse> logout() {
        // In a stateless JWT implementation, logout would involve token invalidation
        // For now, just return success
        return ResponseEntity.ok(AuthResponse.builder()
                .message("Logout successful")
                .build());
    }
}

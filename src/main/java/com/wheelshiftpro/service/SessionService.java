package com.wheelshiftpro.service;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.exception.SessionExpiredException;
import com.wheelshiftpro.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for authentication management and validation utilities.
 * Note: Despite the class name, this service now works with JWT tokens,
 * not HTTP sessions. The name is kept for backward compatibility.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final EmployeeRepository employeeRepository;

    /**
     * Validate if the current authentication is active and valid
     */
    public boolean isAuthenticationValid() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("No valid authentication found");
                return false;
            }

            // Verify user still exists in database
            String email = authentication.getName();
            Optional<Employee> employee = employeeRepository.findByEmail(email);
            if (employee.isEmpty()) {
                log.warn("Authenticated user no longer exists in database: {}", email);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Error validating authentication: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the current authenticated employee
     */
    public Employee getCurrentEmployee() throws SessionExpiredException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SessionExpiredException("No valid authentication found");
        }

        String email = authentication.getName();
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new SessionExpiredException("Employee not found for authenticated user: " + email));
    }

    /**
     * Get the current authenticated employee's ID
     */
    public Long getCurrentEmployeeId() throws SessionExpiredException {
        return getCurrentEmployee().getId();
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String roleName) {
        try {
            Employee employee = getCurrentEmployee();
            return employee.getRoles().stream()
                    .anyMatch(role -> role.getName().name().equals(roleName));
        } catch (SessionExpiredException e) {
            log.debug("Cannot check role for expired authentication: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user has specific permission
     */
    public boolean hasPermission(String permissionName) {
        try {
            Employee employee = getCurrentEmployee();
            return employee.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .anyMatch(permission -> permission.getName().equals(permissionName));
        } catch (SessionExpiredException e) {
            log.debug("Cannot check permission for expired authentication: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clear the current security context
     */
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
        log.info("Security context cleared");
    }
}
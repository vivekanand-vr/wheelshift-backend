package com.wheelshiftpro.service;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.exception.SessionExpiredException;
import com.wheelshiftpro.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for session management and validation utilities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final EmployeeRepository employeeRepository;

    /**
     * Validate if the current session is active and valid
     */
    public boolean isSessionValid(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.debug("No active session found");
                return false;
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("No valid authentication in session");
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
            log.error("Error validating session: {}", e.getMessage());
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
            log.debug("Cannot check role for expired session: {}", e.getMessage());
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
            log.debug("Cannot check permission for expired session: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get session timeout in seconds from the session
     */
    public int getSessionTimeoutSeconds(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getMaxInactiveInterval();
        }
        return 0; // Default when no session
    }

    /**
     * Extend session timeout (refresh the session)
     */
    public void extendSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Access the session to update last access time
            session.getLastAccessedTime();
            log.debug("Session extended for session ID: {}", session.getId());
        }
    }

    /**
     * Invalidate current session
     */
    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            session.invalidate();
            SecurityContextHolder.clearContext();
            log.info("Session invalidated: {}", sessionId);
        }
    }
}
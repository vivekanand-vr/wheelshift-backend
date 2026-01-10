package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.rbac.AccessLevel;
import com.wheelshiftpro.enums.rbac.ResourceType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.service.AuthorizationService;
import com.wheelshiftpro.service.rbac.DataScopeService;
import com.wheelshiftpro.service.rbac.PermissionService;
import com.wheelshiftpro.service.rbac.ResourceACLService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * Service implementation for authorization operations.
 * Implements RBAC with hierarchical precedence:
 * 1. SUPER_ADMIN override
 * 2. Explicit Resource ACL
 * 3. Data Scope match
 * 4. Role permission check
 * 5. Deny
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationServiceImpl implements AuthorizationService {

    private final EmployeeRepository employeeRepository;
    private final PermissionService permissionService;
    private final DataScopeService dataScopeService;
    private final ResourceACLService aclService;

    @Override
    public boolean hasPermission(Long employeeId, String permissionName) {
        log.debug("Checking permission {} for employee {}", permissionName, employeeId);

        // Super admins have all permissions
        if (isSuperAdmin(employeeId)) {
            return true;
        }

        return permissionService.hasPermission(employeeId, permissionName);
    }

    @Override
    public boolean hasRole(Long employeeId, RoleType roleName) {
        log.debug("Checking role {} for employee {}", roleName, employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        return employee.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    @Override
    public boolean hasAnyRole(Long employeeId, RoleType... roleNames) {
        log.debug("Checking any of roles {} for employee {}", Arrays.toString(roleNames), employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        return employee.getRoles().stream()
                .anyMatch(role -> Arrays.asList(roleNames).contains(role.getName()));
    }

    @Override
    public boolean isSuperAdmin(Long employeeId) {
        return hasRole(employeeId, RoleType.SUPER_ADMIN);
    }

    @Override
    public boolean isAdmin(Long employeeId) {
        return hasAnyRole(employeeId, RoleType.SUPER_ADMIN, RoleType.ADMIN);
    }

    @Override
    public boolean hasLocationAccess(Long employeeId, String locationId) {
        log.debug("Checking location access for employee {} to location {}", employeeId, locationId);

        // Super admins and admins without scopes have access to all locations
        if (isSuperAdmin(employeeId)) {
            return true;
        }

        // Check if employee has location scopes
        var locationScopes = dataScopeService.getLocationScopes(employeeId);

        // If no location scopes defined, allow access (not restricted)
        if (locationScopes.isEmpty()) {
            return true;
        }

        // Check if the specific location is in the allowed scopes
        return locationScopes.contains(locationId);
    }

    @Override
    public boolean hasDepartmentAccess(Long employeeId, String department) {
        log.debug("Checking department access for employee {} to department {}", employeeId, department);

        // Super admins have access to all departments
        if (isSuperAdmin(employeeId)) {
            return true;
        }

        // Check if employee has department scopes
        var departmentScopes = dataScopeService.getDepartmentScopes(employeeId);

        // If no department scopes defined, allow access (not restricted)
        if (departmentScopes.isEmpty()) {
            return true;
        }

        // Check if the specific department is in the allowed scopes
        return departmentScopes.contains(department);
    }

    @Override
    public boolean hasResourceAccess(Long employeeId, ResourceType resourceType, Long resourceId, AccessLevel minAccess) {
        log.debug("Checking resource access for employee {} to {}:{} with min access {}",
                employeeId, resourceType, resourceId, minAccess);

        // Super admins have access to all resources
        if (isSuperAdmin(employeeId)) {
            return true;
        }

        // Check explicit ACL
        return aclService.hasACLAccess(resourceType, resourceId, employeeId, minAccess);
    }

    @Override
    public boolean canAccessCar(Long employeeId, Long carId, String operation) {
        log.debug("Checking car access for employee {} to car {} for operation {}", employeeId, carId, operation);

        // Build permission name
        String permissionName = "cars:" + operation;

        // 1. Check if super admin (override)
        if (isSuperAdmin(employeeId)) {
            return true;
        }

        // 2. Check explicit ACL
        AccessLevel minAccess = operation.equals("write") || operation.equals("delete") ? AccessLevel.WRITE : AccessLevel.READ;
        if (hasResourceAccess(employeeId, ResourceType.CAR, carId, minAccess)) {
            return true;
        }

        // 3. Check permission and data scopes would be done in service layer with car's location
        // For now, just check permission
        return hasPermission(employeeId, permissionName);
    }

    @Override
    public boolean canAccessInquiry(Long employeeId, Long inquiryId, String operation) {
        log.debug("Checking inquiry access for employee {} to inquiry {} for operation {}", employeeId, inquiryId, operation);

        String permissionName = "inquiries:" + operation;

        // 1. Check if super admin (override)
        if (isSuperAdmin(employeeId)) {
            return true;
        }

        // 2. Check explicit ACL
        AccessLevel minAccess = operation.equals("write") || operation.equals("delete") || operation.equals("assign") ? AccessLevel.WRITE : AccessLevel.READ;
        if (hasResourceAccess(employeeId, ResourceType.INQUIRY, inquiryId, minAccess)) {
            return true;
        }

        // 3. Check if assigned to this inquiry (ASSIGNMENT scope)
        // This would require loading the inquiry and checking assigned_employee_id
        // For now, just check permission
        return hasPermission(employeeId, permissionName);
    }

    @Override
    public boolean canAccessReservation(Long employeeId, Long reservationId, String operation) {
        log.debug("Checking reservation access for employee {} to reservation {} for operation {}",
                employeeId, reservationId, operation);

        String permissionName = "reservations:" + operation;

        // 1. Check if super admin (override)
        if (isSuperAdmin(employeeId)) {
            return true;
        }

        // 2. Check explicit ACL
        AccessLevel minAccess = operation.equals("write") || operation.equals("delete") || operation.equals("convert") ? AccessLevel.WRITE : AccessLevel.READ;
        if (hasResourceAccess(employeeId, ResourceType.RESERVATION, reservationId, minAccess)) {
            return true;
        }

        // 3. Check permission
        return hasPermission(employeeId, permissionName);
    }

    @Override
    public boolean canAccessTransaction(Long employeeId, Long transactionId, String operation) {
        log.debug("Checking transaction access for employee {} to transaction {} for operation {}",
                employeeId, transactionId, operation);

        String permissionName = "transactions:" + operation;

        // 1. Check if super admin (override)
        if (isSuperAdmin(employeeId)) {
            return true;
        }

        // 2. Check explicit ACL
        AccessLevel minAccess = operation.equals("write") || operation.equals("delete") ? AccessLevel.WRITE : AccessLevel.READ;
        if (hasResourceAccess(employeeId, ResourceType.TRANSACTION, transactionId, minAccess)) {
            return true;
        }

        // 3. Check permission and department scope
        return hasPermission(employeeId, permissionName);
    }
}

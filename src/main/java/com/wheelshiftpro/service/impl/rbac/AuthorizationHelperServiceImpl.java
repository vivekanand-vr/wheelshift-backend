package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.rbac.AccessLevel;
import com.wheelshiftpro.enums.rbac.ResourceType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.enums.rbac.ScopeType;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.service.AuthorizationService;
import com.wheelshiftpro.service.rbac.AuthorizationHelperService;
import com.wheelshiftpro.service.rbac.DataScopeService;
import com.wheelshiftpro.service.rbac.PermissionService;
import com.wheelshiftpro.service.rbac.ResourceACLService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper service implementation for authorization checks.
 * Provides convenient methods for checking permissions, roles, and access levels.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationHelperServiceImpl implements AuthorizationHelperService {

    private final AuthorizationService authService;
    private final PermissionService permissionService;
    private final DataScopeService dataScopeService;
    private final ResourceACLService aclService;
    private final EmployeeRepository employeeRepository;

    // ==================== Permission Checks ====================

    @Override
    public boolean hasPermission(Long employeeId, String permissionName) {
        log.debug("Helper: Checking permission {} for employee {}", permissionName, employeeId);
        return authService.hasPermission(employeeId, permissionName);
    }

    @Override
    public boolean hasAnyPermission(Long employeeId, String... permissionNames) {
        log.debug("Helper: Checking if employee {} has any of permissions: {}", employeeId, Arrays.toString(permissionNames));
        
        for (String permission : permissionNames) {
            if (authService.hasPermission(employeeId, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(Long employeeId, String... permissionNames) {
        log.debug("Helper: Checking if employee {} has all permissions: {}", employeeId, Arrays.toString(permissionNames));
        
        for (String permission : permissionNames) {
            if (!authService.hasPermission(employeeId, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<String> getEmployeePermissions(Long employeeId) {
        log.debug("Helper: Getting all permissions for employee {}", employeeId);
        return permissionService.getEmployeePermissions(employeeId);
    }

    // ==================== Role Checks ====================

    @Override
    public boolean hasRole(Long employeeId, RoleType roleName) {
        log.debug("Helper: Checking role {} for employee {}", roleName, employeeId);
        return authService.hasRole(employeeId, roleName);
    }

    @Override
    public boolean hasAnyRole(Long employeeId, RoleType... roleNames) {
        log.debug("Helper: Checking if employee {} has any of roles: {}", employeeId, Arrays.toString(roleNames));
        return authService.hasAnyRole(employeeId, roleNames);
    }

    @Override
    public boolean hasAllRoles(Long employeeId, RoleType... roleNames) {
        log.debug("Helper: Checking if employee {} has all roles: {}", employeeId, Arrays.toString(roleNames));
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        
        Set<RoleType> employeeRoles = employee.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        
        for (RoleType role : roleNames) {
            if (!employeeRoles.contains(role)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSuperAdmin(Long employeeId) {
        log.debug("Helper: Checking if employee {} is super admin", employeeId);
        return authService.isSuperAdmin(employeeId);
    }

    @Override
    public boolean isAdmin(Long employeeId) {
        log.debug("Helper: Checking if employee {} is admin", employeeId);
        return authService.isAdmin(employeeId);
    }

    @Override
    public Set<RoleType> getEmployeeRoles(Long employeeId) {
        log.debug("Helper: Getting all roles for employee {}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        
        return employee.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
    }

    // ==================== Resource Access Checks ====================

    @Override
    public boolean canAccessResource(Long employeeId, ResourceType resourceType, Long resourceId, AccessLevel minAccess) {
        log.debug("Helper: Checking resource access for employee {} to {}:{} with min access {}",
                employeeId, resourceType, resourceId, minAccess);
        return authService.hasResourceAccess(employeeId, resourceType, resourceId, minAccess);
    }

    @Override
    public boolean canReadResource(Long employeeId, ResourceType resourceType, Long resourceId) {
        log.debug("Helper: Checking read access for employee {} to {}:{}", employeeId, resourceType, resourceId);
        
        // Check if has permission to read this resource type
        String resourceName = resourceType.name().toLowerCase().replace("_", "-");
        if (!authService.hasPermission(employeeId, resourceName + ":read") &&
            !authService.hasPermission(employeeId, resourceName + ":*")) {
            return false;
        }
        
        // Check ACL
        return authService.hasResourceAccess(employeeId, resourceType, resourceId, AccessLevel.READ);
    }

    @Override
    public boolean canWriteResource(Long employeeId, ResourceType resourceType, Long resourceId) {
        log.debug("Helper: Checking write access for employee {} to {}:{}", employeeId, resourceType, resourceId);
        
        // Check if has permission to write this resource type
        String resourceName = resourceType.name().toLowerCase().replace("_", "-");
        if (!authService.hasPermission(employeeId, resourceName + ":write") &&
            !authService.hasPermission(employeeId, resourceName + ":*")) {
            return false;
        }
        
        // Check ACL
        return authService.hasResourceAccess(employeeId, resourceType, resourceId, AccessLevel.WRITE);
    }

    @Override
    public boolean canDeleteResource(Long employeeId, ResourceType resourceType, Long resourceId) {
        log.debug("Helper: Checking delete access for employee {} to {}:{}", employeeId, resourceType, resourceId);
        
        // Check if has permission to delete this resource type
        String resourceName = resourceType.name().toLowerCase().replace("_", "-");
        if (!authService.hasPermission(employeeId, resourceName + ":delete") &&
            !authService.hasPermission(employeeId, resourceName + ":*")) {
            return false;
        }
        
        // Check ACL (needs WRITE or ADMIN)
        return authService.hasResourceAccess(employeeId, resourceType, resourceId, AccessLevel.WRITE);
    }

    @Override
    public boolean hasResourceACL(Long employeeId, ResourceType resourceType, Long resourceId) {
        log.debug("Helper: Checking if employee {} has ACL for {}:{}", employeeId, resourceType, resourceId);
        return aclService.hasACLAccess(resourceType, resourceId, employeeId, AccessLevel.READ);
    }

    // ==================== Data Scope Checks ====================

    @Override
    public boolean hasLocationAccess(Long employeeId, String locationId) {
        log.debug("Helper: Checking location access for employee {} to location {}", employeeId, locationId);
        return authService.hasLocationAccess(employeeId, locationId);
    }

    @Override
    public boolean hasDepartmentAccess(Long employeeId, String department) {
        log.debug("Helper: Checking department access for employee {} to department {}", employeeId, department);
        return authService.hasDepartmentAccess(employeeId, department);
    }

    @Override
    public boolean hasAssignmentAccess(Long employeeId, ResourceType resourceType, Long resourceId) {
        log.debug("Helper: Checking assignment access for employee {} to {}:{}", employeeId, resourceType, resourceId);
        
        // Check if employee has ASSIGNMENT scope
        if (!hasDataScopes(employeeId, ScopeType.ASSIGNMENT)) {
            return false;
        }
        
        // This would need to be implemented based on the resource type
        // For now, we'll check if the employee has ACL access
        return aclService.hasACLAccess(resourceType, resourceId, employeeId, AccessLevel.READ);
    }

    @Override
    public Set<String> getLocationScopes(Long employeeId) {
        log.debug("Helper: Getting location scopes for employee {}", employeeId);
        return dataScopeService.getLocationScopes(employeeId);
    }

    @Override
    public Set<String> getDepartmentScopes(Long employeeId) {
        log.debug("Helper: Getting department scopes for employee {}", employeeId);
        return dataScopeService.getDepartmentScopes(employeeId);
    }

    @Override
    public boolean hasDataScopes(Long employeeId, ScopeType scopeType) {
        log.debug("Helper: Checking if employee {} has {} scopes", employeeId, scopeType);
        return !dataScopeService.getScopesByEmployeeIdAndType(employeeId, scopeType).isEmpty();
    }

    // ==================== Bulk Authorization Checks ====================

    @Override
    public Map<Long, Boolean> canAccessMultipleResources(Long employeeId, ResourceType resourceType,
                                                        List<Long> resourceIds, AccessLevel minAccess) {
        log.debug("Helper: Checking bulk access for employee {} to {} resources of type {}",
                employeeId, resourceIds.size(), resourceType);
        
        Map<Long, Boolean> accessMap = new HashMap<>();
        
        // If super admin, grant access to all
        if (authService.isSuperAdmin(employeeId)) {
            for (Long resourceId : resourceIds) {
                accessMap.put(resourceId, true);
            }
            return accessMap;
        }
        
        // Check each resource
        for (Long resourceId : resourceIds) {
            boolean hasAccess = authService.hasResourceAccess(employeeId, resourceType, resourceId, minAccess);
            accessMap.put(resourceId, hasAccess);
        }
        
        return accessMap;
    }

    @Override
    public <T> List<T> filterAccessibleResources(Long employeeId, List<T> resources,
                                                 Function<T, Long> idExtractor,
                                                 ResourceType resourceType, AccessLevel minAccess) {
        log.debug("Helper: Filtering {} resources of type {} for employee {}",
                resources.size(), resourceType, employeeId);
        
        // If super admin, return all
        if (authService.isSuperAdmin(employeeId)) {
            return new ArrayList<>(resources);
        }
        
        // Filter resources
        return resources.stream()
                .filter(resource -> {
                    Long resourceId = idExtractor.apply(resource);
                    return authService.hasResourceAccess(employeeId, resourceType, resourceId, minAccess);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasAnyResourceAccess(Long employeeId, ResourceType resourceType,
                                       List<Long> resourceIds, AccessLevel minAccess) {
        log.debug("Helper: Checking if employee {} has access to any of {} {} resources",
                employeeId, resourceIds.size(), resourceType);
        
        // If super admin, return true
        if (authService.isSuperAdmin(employeeId)) {
            return true;
        }
        
        // Check if has access to at least one resource
        for (Long resourceId : resourceIds) {
            if (authService.hasResourceAccess(employeeId, resourceType, resourceId, minAccess)) {
                return true;
            }
        }
        
        return false;
    }
}

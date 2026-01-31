package com.wheelshiftpro.service.rbac;

import com.wheelshiftpro.enums.rbac.AccessLevel;
import com.wheelshiftpro.enums.rbac.ResourceType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.enums.rbac.ScopeType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Helper service for authorization checks.
 * Provides convenient methods for checking permissions, roles, and access levels.
 */
public interface AuthorizationHelperService {

    // ==================== Permission Checks ====================

    /**
     * Check if an employee has a specific permission
     */
    boolean hasPermission(Long employeeId, String permissionName);

    /**
     * Check if an employee has ANY of the specified permissions
     */
    boolean hasAnyPermission(Long employeeId, String... permissionNames);

    /**
     * Check if an employee has ALL of the specified permissions
     */
    boolean hasAllPermissions(Long employeeId, String... permissionNames);

    /**
     * Get all permissions for an employee
     */
    Set<String> getEmployeePermissions(Long employeeId);

    // ==================== Role Checks ====================

    /**
     * Check if an employee has a specific role
     */
    boolean hasRole(Long employeeId, RoleType roleName);

    /**
     * Check if an employee has ANY of the specified roles
     */
    boolean hasAnyRole(Long employeeId, RoleType... roleNames);

    /**
     * Check if an employee has ALL of the specified roles
     */
    boolean hasAllRoles(Long employeeId, RoleType... roleNames);

    /**
     * Check if an employee is a Super Admin
     */
    boolean isSuperAdmin(Long employeeId);

    /**
     * Check if an employee is an Admin or Super Admin
     */
    boolean isAdmin(Long employeeId);

    /**
     * Get all roles for an employee
     */
    Set<RoleType> getEmployeeRoles(Long employeeId);

    // ==================== Resource Access Checks ====================

    /**
     * Check if an employee can access a specific resource with a minimum access level
     */
    boolean canAccessResource(Long employeeId, ResourceType resourceType, Long resourceId, AccessLevel minAccess);

    /**
     * Check if an employee can read a specific resource
     */
    boolean canReadResource(Long employeeId, ResourceType resourceType, Long resourceId);

    /**
     * Check if an employee can write to a specific resource
     */
    boolean canWriteResource(Long employeeId, ResourceType resourceType, Long resourceId);

    /**
     * Check if an employee can delete a specific resource
     */
    boolean canDeleteResource(Long employeeId, ResourceType resourceType, Long resourceId);

    /**
     * Check if an employee has an ACL entry for a specific resource
     */
    boolean hasResourceACL(Long employeeId, ResourceType resourceType, Long resourceId);

    // ==================== Data Scope Checks ====================

    /**
     * Check if an employee has access to a specific location
     */
    boolean hasLocationAccess(Long employeeId, String locationId);

    /**
     * Check if an employee has access to a specific department
     */
    boolean hasDepartmentAccess(Long employeeId, String department);

    /**
     * Check if a resource is assigned to an employee (ASSIGNMENT scope)
     */
    boolean hasAssignmentAccess(Long employeeId, ResourceType resourceType, Long resourceId);

    /**
     * Get all location scopes for an employee
     */
    Set<String> getLocationScopes(Long employeeId);

    /**
     * Get all department scopes for an employee
     */
    Set<String> getDepartmentScopes(Long employeeId);

    /**
     * Check if an employee has any data scopes of a specific type
     */
    boolean hasDataScopes(Long employeeId, ScopeType scopeType);

    // ==================== Bulk Authorization Checks ====================

    /**
     * Check if an employee can access multiple resources
     * @return Map of resourceId -> canAccess
     */
    Map<Long, Boolean> canAccessMultipleResources(Long employeeId, ResourceType resourceType,
                                                  List<Long> resourceIds, AccessLevel minAccess);

    /**
     * Filter a list of resources to only those the employee can access
     */
    <T> List<T> filterAccessibleResources(Long employeeId, List<T> resources,
                                         Function<T, Long> idExtractor,
                                         ResourceType resourceType, AccessLevel minAccess);

    /**
     * Check if an employee has access to at least one of the specified resources
     */
    boolean hasAnyResourceAccess(Long employeeId, ResourceType resourceType,
                                List<Long> resourceIds, AccessLevel minAccess);
}

package com.wheelshiftpro.service;

import com.wheelshiftpro.enums.AccessLevel;
import com.wheelshiftpro.enums.ResourceType;
import com.wheelshiftpro.enums.RoleType;

/**
 * Service interface for authorization operations.
 * Implements RBAC with data scopes and resource-level ACLs.
 */
public interface AuthorizationService {

    /**
     * Check if an employee has a specific permission
     */
    boolean hasPermission(Long employeeId, String permissionName);

    /**
     * Check if an employee has a specific role
     */
    boolean hasRole(Long employeeId, RoleType roleName);

    /**
     * Check if an employee has any of the specified roles
     */
    boolean hasAnyRole(Long employeeId, RoleType... roleNames);

    /**
     * Check if an employee is a Super Admin
     */
    boolean isSuperAdmin(Long employeeId);

    /**
     * Check if an employee is an Admin or Super Admin
     */
    boolean isAdmin(Long employeeId);

    /**
     * Check if an employee has access to a specific location
     */
    boolean hasLocationAccess(Long employeeId, String locationId);

    /**
     * Check if an employee has access to a specific department
     */
    boolean hasDepartmentAccess(Long employeeId, String department);

    /**
     * Check if an employee has ACL access to a resource
     */
    boolean hasResourceAccess(Long employeeId, ResourceType resourceType, Long resourceId, AccessLevel minAccess);

    /**
     * Check if an employee can access a car (based on permissions, scopes, and ACL)
     */
    boolean canAccessCar(Long employeeId, Long carId, String operation);

    /**
     * Check if an employee can access an inquiry (based on assignment or permissions)
     */
    boolean canAccessInquiry(Long employeeId, Long inquiryId, String operation);

    /**
     * Check if an employee can access a reservation (based on assignment or permissions)
     */
    boolean canAccessReservation(Long employeeId, Long reservationId, String operation);

    /**
     * Check if an employee can access a transaction (based on permissions and scopes)
     */
    boolean canAccessTransaction(Long employeeId, Long transactionId, String operation);
}

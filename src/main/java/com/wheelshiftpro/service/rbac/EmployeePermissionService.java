package com.wheelshiftpro.service.rbac;

import com.wheelshiftpro.dto.request.rbac.EmployeePermissionRequest;
import com.wheelshiftpro.dto.response.rbac.EmployeePermissionResponse;

import java.util.List;
import java.util.Set;

/**
 * Service interface for managing custom employee permissions.
 * Allows super admins to assign permissions directly to employees.
 */
public interface EmployeePermissionService {

    /**
     * Assign a custom permission to an employee.
     * Only super admins can perform this operation.
     *
     * @param employeeId the employee ID
     * @param request the permission assignment request
     * @param grantedBy the ID of the admin granting the permission
     * @return the created employee permission response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee or permission not found
     * @throws com.wheelshiftpro.exception.DuplicateResourceException if permission already assigned
     */
    EmployeePermissionResponse assignPermissionToEmployee(Long employeeId, 
                                                          EmployeePermissionRequest request,
                                                          Long grantedBy);

    /**
     * Remove a custom permission from an employee.
     *
     * @param employeeId the employee ID
     * @param permissionId the permission ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if assignment not found
     */
    void removePermissionFromEmployee(Long employeeId, Long permissionId);

    /**
     * Get all custom permissions for an employee.
     *
     * @param employeeId the employee ID
     * @return list of employee permission responses
     */
    List<EmployeePermissionResponse> getEmployeeCustomPermissions(Long employeeId);

    /**
     * Get custom permission names for an employee.
     *
     * @param employeeId the employee ID
     * @return set of permission names (e.g., "cars:write", "transactions:read")
     */
    Set<String> getEmployeeCustomPermissionNames(Long employeeId);

    /**
     * Remove all custom permissions from an employee.
     *
     * @param employeeId the employee ID
     */
    void removeAllCustomPermissions(Long employeeId);

    /**
     * Get a specific employee permission assignment by ID.
     *
     * @param id the employee permission ID
     * @return the employee permission response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if not found
     */
    EmployeePermissionResponse getEmployeePermissionById(Long id);

    /**
     * Check if an employee has a specific custom permission.
     *
     * @param employeeId the employee ID
     * @param permissionName the permission name (e.g., "cars:write")
     * @return true if employee has the custom permission
     */
    boolean hasCustomPermission(Long employeeId, String permissionName);
}

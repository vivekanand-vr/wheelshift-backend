package com.wheelshiftpro.service.rbac;

import com.wheelshiftpro.dto.request.rbac.PermissionRequest;
import com.wheelshiftpro.dto.response.rbac.PermissionResponse;
import com.wheelshiftpro.entity.rbac.Permission;

import java.util.List;
import java.util.Set;

/**
 * Service interface for Permission operations
 */
public interface PermissionService {

    /**
     * Create a new permission
     */
    PermissionResponse createPermission(PermissionRequest request);

    /**
     * Update an existing permission
     */
    PermissionResponse updatePermission(Long permissionId, PermissionRequest request);

    /**
     * Delete a permission
     */
    void deletePermission(Long permissionId);

    /**
     * Get permission by ID
     */
    PermissionResponse getPermissionById(Long permissionId);

    /**
     * Get permission by name
     */
    PermissionResponse getPermissionByName(String name);

    /**
     * Get all permissions
     */
    List<PermissionResponse> getAllPermissions();

    /**
     * Get permissions by role ID
     */
    Set<PermissionResponse> getPermissionsByRoleId(Long roleId);

    /**
     * Get permissions by employee ID
     */
    Set<PermissionResponse> getPermissionsByEmployeeId(Long employeeId);

    /**
     * Get permission entity by ID (for internal use)
     */
    Permission getPermissionEntityById(Long permissionId);

    /**
     * Check if employee has a specific permission
     */
    boolean hasPermission(Long employeeId, String permissionName);
}

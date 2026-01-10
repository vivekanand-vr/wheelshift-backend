package com.wheelshiftpro.service.rbac;

import com.wheelshiftpro.dto.request.rbac.RoleRequest;
import com.wheelshiftpro.dto.response.rbac.RoleResponse;
import com.wheelshiftpro.entity.rbac.Role;
import com.wheelshiftpro.enums.rbac.RoleType;

import java.util.List;
import java.util.Set;

/**
 * Service interface for Role operations
 */
public interface RoleService {

    /**
     * Create a new role
     */
    RoleResponse createRole(RoleRequest request);

    /**
     * Update an existing role
     */
    RoleResponse updateRole(Long roleId, RoleRequest request);

    /**
     * Delete a role
     */
    void deleteRole(Long roleId);

    /**
     * Get role by ID
     */
    RoleResponse getRoleById(Long roleId);

    /**
     * Get role by name
     */
    RoleResponse getRoleByName(RoleType name);

    /**
     * Get all roles
     */
    List<RoleResponse> getAllRoles();

    /**
     * Get roles by employee ID
     */
    Set<RoleResponse> getRolesByEmployeeId(Long employeeId);

    /**
     * Assign role to employee
     */
    void assignRoleToEmployee(Long employeeId, Long roleId);

    /**
     * Remove role from employee
     */
    void removeRoleFromEmployee(Long employeeId, Long roleId);

    /**
     * Add permission to role
     */
    void addPermissionToRole(Long roleId, Long permissionId);

    /**
     * Remove permission from role
     */
    void removePermissionFromRole(Long roleId, Long permissionId);

    /**
     * Get role entity by ID (for internal use)
     */
    Role getRoleEntityById(Long roleId);

    /**
     * Get role entity by name (for internal use)
     */
    Role getRoleEntityByName(RoleType name);
}

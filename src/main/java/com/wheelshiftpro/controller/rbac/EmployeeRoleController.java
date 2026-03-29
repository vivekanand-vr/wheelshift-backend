package com.wheelshiftpro.controller.rbac;

import com.wheelshiftpro.dto.response.rbac.RoleResponse;
import com.wheelshiftpro.service.rbac.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Controller for managing employee role assignments
 */
@RestController
@RequestMapping("/api/v1/rbac/employees")
@RequiredArgsConstructor
@Tag(name = "RBAC - Employee Roles", description = "Employee role assignment endpoints")
public class EmployeeRoleController {

    private final RoleService roleService;

    @GetMapping("/{employeeId}/roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get employee roles", description = "Retrieve all roles assigned to an employee")
    public ResponseEntity<Set<RoleResponse>> getEmployeeRoles(@PathVariable Long employeeId) {
        Set<RoleResponse> response = roleService.getRolesByEmployeeId(employeeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{employeeId}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Assign role to employee", description = "Assign a role to an employee (Admin or Super Admin)")
    public ResponseEntity<Void> assignRoleToEmployee(
            @PathVariable Long employeeId,
            @PathVariable Long roleId) {
        roleService.assignRoleToEmployee(employeeId, roleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{employeeId}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Remove role from employee", description = "Remove a role from an employee (Admin or Super Admin)")
    public ResponseEntity<Void> removeRoleFromEmployee(
            @PathVariable Long employeeId,
            @PathVariable Long roleId) {
        roleService.removeRoleFromEmployee(employeeId, roleId);
        return ResponseEntity.noContent().build();
    }
}

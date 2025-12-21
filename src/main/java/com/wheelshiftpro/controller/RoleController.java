package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.RoleRequest;
import com.wheelshiftpro.dto.response.RoleResponse;
import com.wheelshiftpro.enums.RoleType;
import com.wheelshiftpro.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing roles and role-permission assignments
 */
@RestController
@RequestMapping("/api/v1/rbac/roles")
@RequiredArgsConstructor
@Tag(name = "RBAC - Roles", description = "Role management and role-permission assignment endpoints")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role", description = "Create a new custom role (Super Admin only)")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update a role", description = "Update role details (Super Admin only)")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete a role", description = "Delete a custom role (Super Admin only, cannot delete system roles)")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get role by ID", description = "Retrieve role details by ID")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long roleId) {
        RoleResponse response = roleService.getRoleById(roleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get role by name", description = "Retrieve role details by name")
    public ResponseEntity<RoleResponse> getRoleByName(@PathVariable RoleType name) {
        RoleResponse response = roleService.getRoleByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieve all roles in the system")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> response = roleService.getAllRoles();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Add permission to role", description = "Grant a permission to a role (Super Admin only)")
    public ResponseEntity<Void> addPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Remove permission from role", description = "Revoke a permission from a role (Super Admin only)")
    public ResponseEntity<Void> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }
}

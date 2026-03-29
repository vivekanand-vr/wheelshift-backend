package com.wheelshiftpro.controller.rbac;

import com.wheelshiftpro.dto.request.rbac.PermissionRequest;
import com.wheelshiftpro.dto.response.rbac.PermissionResponse;
import com.wheelshiftpro.service.rbac.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controller for managing permissions
 */
@RestController
@RequestMapping("/api/v1/rbac/permissions")
@RequiredArgsConstructor
@Tag(name = "RBAC - Permissions", description = "Permission management endpoints")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new permission", description = "Create a new permission (Super Admin only)")
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{permissionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update a permission", description = "Update permission details (Super Admin only)")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.updatePermission(permissionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a permission", description = "Delete a permission (Super Admin only)")
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get permission by ID", description = "Retrieve permission details by ID")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Long permissionId) {
        PermissionResponse response = permissionService.getPermissionById(permissionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get permission by name", description = "Retrieve permission details by name (resource:action)")
    public ResponseEntity<PermissionResponse> getPermissionByName(@PathVariable String name) {
        PermissionResponse response = permissionService.getPermissionByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get all permissions", description = "Retrieve all permissions in the system")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> response = permissionService.getAllPermissions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get permissions by role", description = "Retrieve all permissions for a specific role")
    public ResponseEntity<Set<PermissionResponse>> getPermissionsByRoleId(@PathVariable Long roleId) {
        Set<PermissionResponse> response = permissionService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get permissions by employee", description = "Retrieve all permissions for a specific employee (through their roles)")
    public ResponseEntity<Set<PermissionResponse>> getPermissionsByEmployeeId(@PathVariable Long employeeId) {
        Set<PermissionResponse> response = permissionService.getPermissionsByEmployeeId(employeeId);
        return ResponseEntity.ok(response);
    }
}

package com.wheelshiftpro.controller.rbac;

import com.wheelshiftpro.dto.request.rbac.DataScopeRequest;
import com.wheelshiftpro.dto.response.rbac.DataScopeResponse;
import com.wheelshiftpro.service.rbac.DataScopeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Controller for managing employee data scopes
 */
@RestController
@RequestMapping("/api/v1/rbac/employees")
@RequiredArgsConstructor
@Tag(name = "RBAC - Data Scopes", description = "Employee data scope management endpoints")
public class DataScopeController {

    private final DataScopeService dataScopeService;

    @GetMapping("/{employeeId}/scopes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get employee scopes", description = "Retrieve all data scopes for an employee")
    public ResponseEntity<Set<DataScopeResponse>> getEmployeeScopes(@PathVariable Long employeeId) {
        Set<DataScopeResponse> response = dataScopeService.getScopesByEmployeeId(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scopes/{scopeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get scope by ID", description = "Retrieve a specific data scope by ID")
    public ResponseEntity<DataScopeResponse> getScopeById(@PathVariable Long scopeId) {
        DataScopeResponse response = dataScopeService.getScopeById(scopeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{employeeId}/scopes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Add scope to employee", description = "Add a data scope to an employee (Admin or Super Admin)")
    public ResponseEntity<DataScopeResponse> addScopeToEmployee(
            @PathVariable Long employeeId,
            @Valid @RequestBody DataScopeRequest request) {
        DataScopeResponse response = dataScopeService.addScopeToEmployee(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/scopes/{scopeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a scope", description = "Update an existing data scope (Admin or Super Admin)")
    public ResponseEntity<DataScopeResponse> updateScope(
            @PathVariable Long scopeId,
            @Valid @RequestBody DataScopeRequest request) {
        DataScopeResponse response = dataScopeService.updateScope(scopeId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/scopes/{scopeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Remove scope", description = "Remove a data scope from an employee (Admin or Super Admin)")
    public ResponseEntity<Void> removeScope(@PathVariable Long scopeId) {
        dataScopeService.removeScopeFromEmployee(scopeId);
        return ResponseEntity.noContent().build();
    }
}

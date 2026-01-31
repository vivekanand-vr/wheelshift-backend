package com.wheelshiftpro.controller.rbac;

import com.wheelshiftpro.dto.request.rbac.EmployeePermissionRequest;
import com.wheelshiftpro.dto.response.rbac.EmployeePermissionResponse;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.rbac.EmployeePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for managing custom employee permissions.
 * Only super admins can assign/remove custom permissions to/from employees.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rbac/employee-permissions")
@RequiredArgsConstructor
@Tag(name = "Employee Custom Permissions", description = "APIs for managing custom permissions assigned directly to employees")
public class EmployeePermissionController {

    private final EmployeePermissionService employeePermissionService;

    @PostMapping("/employees/{employeeId}")
    @Operation(summary = "Assign custom permission to employee", 
               description = "Super admin can assign a custom permission to an employee (independent of roles)")
    public ResponseEntity<EmployeePermissionResponse> assignPermissionToEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Valid @RequestBody EmployeePermissionRequest request,
            @AuthenticationPrincipal EmployeeUserDetails currentUser) {
        
        log.info("Admin {} assigning permission to employee {}", currentUser.getId(), employeeId);
        
        EmployeePermissionResponse response = employeePermissionService
                .assignPermissionToEmployee(employeeId, request, currentUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/employees/{employeeId}/permissions/{permissionId}")
    @Operation(summary = "Remove custom permission from employee", 
               description = "Remove a custom permission assignment from an employee")
    public ResponseEntity<Void> removePermissionFromEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Permission ID") @PathVariable Long permissionId) {
        
        log.info("Removing permission {} from employee {}", permissionId, employeeId);
        
        employeePermissionService.removePermissionFromEmployee(employeeId, permissionId);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employees/{employeeId}")
    @Operation(summary = "Get employee's custom permissions", 
               description = "Get all custom permissions assigned to an employee")
    public ResponseEntity<List<EmployeePermissionResponse>> getEmployeeCustomPermissions(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        log.debug("Fetching custom permissions for employee {}", employeeId);
        
        List<EmployeePermissionResponse> permissions = employeePermissionService
                .getEmployeeCustomPermissions(employeeId);
        
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/employees/{employeeId}/permission-names")
    @Operation(summary = "Get employee's custom permission names", 
               description = "Get custom permission names (e.g., 'cars:write') for an employee")
    public ResponseEntity<Set<String>> getEmployeeCustomPermissionNames(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        log.debug("Fetching custom permission names for employee {}", employeeId);
        
        Set<String> permissionNames = employeePermissionService
                .getEmployeeCustomPermissionNames(employeeId);
        
        return ResponseEntity.ok(permissionNames);
    }

    @DeleteMapping("/employees/{employeeId}")
    @Operation(summary = "Remove all custom permissions from employee", 
               description = "Remove all custom permission assignments from an employee")
    public ResponseEntity<Void> removeAllCustomPermissions(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        log.info("Removing all custom permissions from employee {}", employeeId);
        
        employeePermissionService.removeAllCustomPermissions(employeeId);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee permission by ID", 
               description = "Get a specific employee permission assignment by ID")
    public ResponseEntity<EmployeePermissionResponse> getEmployeePermissionById(
            @Parameter(description = "Employee Permission ID") @PathVariable Long id) {
        
        log.debug("Fetching employee permission {}", id);
        
        EmployeePermissionResponse response = employeePermissionService.getEmployeePermissionById(id);
        
        return ResponseEntity.ok(response);
    }
}

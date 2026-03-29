package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.EmployeeRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.EmployeeResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.EmployeeStatus;
import com.wheelshiftpro.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing employees and staff")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new employee", description = "Registers a new employee in the system")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update an employee", description = "Updates an existing employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get employee by ID", description = "Retrieves a specific employee by their ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get all employees", description = "Retrieves all employees with pagination and optional regex search")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getAllEmployees(
            @Parameter(description = "Search term (regex pattern) to filter employees by name, email, position, or department") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EmployeeResponse> response = employeeService.getAllEmployees(search, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete an employee", description = "Deletes an employee by ID")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Employee deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Search employees", description = "Search employees by name, role, or status")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> searchEmployees(
            @Parameter(description = "Name filter") @RequestParam(required = false) String name,
            @Parameter(description = "Role filter") @RequestParam(required = false) String role,
            @Parameter(description = "Employee status filter") @RequestParam(required = false) EmployeeStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EmployeeResponse> response = employeeService.searchEmployees(name, role, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get employees by role", description = "Retrieves all employees with a specific role")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getEmployeesByRole(
            @Parameter(description = "Employee role") @PathVariable String role,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EmployeeResponse> response = employeeService.getEmployeesByRole(role, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get active employees", description = "Retrieves all active employees")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getActiveEmployees(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<EmployeeResponse> response = employeeService.getActiveEmployees(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update employee status", description = "Updates the status of an employee")
    public ResponseEntity<ApiResponse<Void>> updateEmployeeStatus(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Parameter(description = "New employee status") @RequestParam EmployeeStatus status) {
        employeeService.updateEmployeeStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<Void>success("Employee status updated successfully", null));
    }

    @GetMapping("/{id}/performance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get employee performance", description = "Retrieves performance metrics for an employee")
    public ResponseEntity<ApiResponse<Object>> getEmployeePerformance(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        Object performance = employeeService.getEmployeePerformance(id);
        return ResponseEntity.ok(ApiResponse.success(performance));
    }
}

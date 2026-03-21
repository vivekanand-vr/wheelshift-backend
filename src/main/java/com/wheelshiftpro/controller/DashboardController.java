package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.dashboard.*;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.service.DashboardService;
import com.wheelshiftpro.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for role-based dashboard endpoints.
 * Provides comprehensive metrics and statistics tailored to each role.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Role-based dashboard endpoints with personalized metrics and statistics")
public class DashboardController {
    
    private final DashboardService dashboardService;
    private final EmployeeService employeeService;
    
    /**
     * Get admin dashboard with system-wide metrics
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
        summary = "Get admin dashboard",
        description = "Retrieves comprehensive dashboard data for administrators including overview stats, " +
                     "revenue metrics, inventory health, recent activities, top employees, system alerts, and notifications. " +
                     "Accessible to SUPER_ADMIN and ADMIN roles only."
    )
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard(
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Admin dashboard requested by: {}", authentication.getName());
        Long employeeId = getCurrentEmployeeId(authentication);
        
        AdminDashboardResponse dashboard = dashboardService.getAdminDashboard(employeeId);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Admin dashboard retrieved successfully",
            dashboard
        ));
    }
    
    /**
     * Get sales dashboard with pipeline and performance metrics
     */
    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('SALES', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get sales dashboard",
        description = "Retrieves sales-specific dashboard with personal stats, sales pipeline, performance metrics, " +
                     "quick actions, available inventory, and notifications. Shows data relevant to the sales employee's activities. " +
                     "Accessible to SALES, ADMIN, and SUPER_ADMIN roles."
    )
    public ResponseEntity<ApiResponse<SalesDashboardResponse>> getSalesDashboard(
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Sales dashboard requested by: {}", authentication.getName());
        Long employeeId = getCurrentEmployeeId(authentication);
        
        SalesDashboardResponse dashboard = dashboardService.getSalesDashboard(employeeId);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Sales dashboard retrieved successfully",
            dashboard
        ));
    }
    
    /**
     * Get inspector dashboard with inspection queue and tasks
     */
    @GetMapping("/inspector")
    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get inspector dashboard",
        description = "Retrieves inspector-specific dashboard with inspection queue, personal statistics, " +
                     "vehicle status, assigned tasks, location summaries, recent inspections, and notifications. " +
                     "Optimized for field inspection work. Accessible to INSPECTOR, ADMIN, and SUPER_ADMIN roles."
    )
    public ResponseEntity<ApiResponse<InspectorDashboardResponse>> getInspectorDashboard(
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Inspector dashboard requested by: {}", authentication.getName());
        Long employeeId = getCurrentEmployeeId(authentication);
        
        InspectorDashboardResponse dashboard = dashboardService.getInspectorDashboard(employeeId);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Inspector dashboard retrieved successfully",
            dashboard
        ));
    }
    
    /**
     * Get finance dashboard with financial metrics and reports
     */
    @GetMapping("/finance")
    @PreAuthorize("hasAnyRole('FINANCE', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get finance dashboard",
        description = "Retrieves finance-specific dashboard with financial overview, transaction summary, " +
                     "profitability metrics, aging analysis, budget tracking, and notifications. " +
                     "Provides comprehensive financial insights and P&L data. Accessible to FINANCE, ADMIN, and SUPER_ADMIN roles."
    )
    public ResponseEntity<ApiResponse<FinanceDashboardResponse>> getFinanceDashboard(
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Finance dashboard requested by: {}", authentication.getName());
        Long employeeId = getCurrentEmployeeId(authentication);
        
        FinanceDashboardResponse dashboard = dashboardService.getFinanceDashboard(employeeId);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Finance dashboard retrieved successfully",
            dashboard
        ));
    }
    
    /**
     * Get store manager dashboard with location and movement metrics
     */
    @GetMapping("/store-manager")
    @PreAuthorize("hasAnyRole('STORE_MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get store manager dashboard",
        description = "Retrieves store manager dashboard with location overview, vehicle distribution, " +
                     "movement activity, capacity alerts, maintenance status, location performance, and notifications. " +
                     "Focuses on storage operations and vehicle logistics. Accessible to STORE_MANAGER, ADMIN, and SUPER_ADMIN roles."
    )
    public ResponseEntity<ApiResponse<StoreManagerDashboardResponse>> getStoreManagerDashboard(
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Store manager dashboard requested by: {}", authentication.getName());
        Long employeeId = getCurrentEmployeeId(authentication);
        
        StoreManagerDashboardResponse dashboard = dashboardService.getStoreManagerDashboard(employeeId);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Store manager dashboard retrieved successfully",
            dashboard
        ));
    }
    
    /**
     * Get current user's dashboard (auto-detects role)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user's dashboard",
        description = "Auto-detects the authenticated user's primary role and returns the appropriate dashboard. " +
                     "This is a convenience endpoint that eliminates the need to call role-specific endpoints. " +
                     "Returns AdminDashboardResponse for ADMIN/SUPER_ADMIN, SalesDashboardResponse for SALES, " +
                     "InspectorDashboardResponse for INSPECTOR, FinanceDashboardResponse for FINANCE, " +
                     "or StoreManagerDashboardResponse for STORE_MANAGER. Accessible to all authenticated users."
    )
    public ResponseEntity<ApiResponse<Object>> getCurrentUserDashboard(
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Dashboard requested for current user: {}", authentication.getName());
        Long employeeId = getCurrentEmployeeId(authentication);
        
        Object dashboard = dashboardService.getDashboardForCurrentUser(employeeId);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Dashboard retrieved successfully",
            dashboard
        ));
    }
    
    /**
     * Helper method to get current employee ID from authentication
     */
    private Long getCurrentEmployeeId(Authentication authentication) {
        String email = authentication.getName();
        return employeeService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email))
                .getId();
    }
}

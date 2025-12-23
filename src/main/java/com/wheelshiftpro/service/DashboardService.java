package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.response.dashboard.*;

/**
 * Service interface for dashboard operations.
 * Provides role-specific dashboard data with metrics and statistics.
 */
public interface DashboardService {
    
    /**
     * Get admin dashboard with comprehensive system metrics
     * 
     * @param employeeId ID of the employee requesting dashboard
     * @return AdminDashboardResponse with all admin widgets
     */
    AdminDashboardResponse getAdminDashboard(Long employeeId);
    
    /**
     * Get sales dashboard with pipeline and performance metrics
     * 
     * @param employeeId ID of the sales employee
     * @return SalesDashboardResponse with sales-specific data
     */
    SalesDashboardResponse getSalesDashboard(Long employeeId);
    
    /**
     * Get inspector dashboard with inspection queue and tasks
     * 
     * @param employeeId ID of the inspector employee
     * @return InspectorDashboardResponse with inspection metrics
     */
    InspectorDashboardResponse getInspectorDashboard(Long employeeId);
    
    /**
     * Get finance dashboard with financial metrics
     * 
     * @param employeeId ID of the finance employee
     * @return FinanceDashboardResponse with financial data
     */
    FinanceDashboardResponse getFinanceDashboard(Long employeeId);
    
    /**
     * Get store manager dashboard with location metrics
     * 
     * @param employeeId ID of the store manager
     * @return StoreManagerDashboardResponse with location data
     */
    StoreManagerDashboardResponse getStoreManagerDashboard(Long employeeId);
    
    /**
     * Get dashboard for current user based on their primary role
     * Auto-detects role and returns appropriate dashboard
     * 
     * @param employeeId ID of the current employee
     * @return Role-specific dashboard response
     */
    Object getDashboardForCurrentUser(Long employeeId);
}

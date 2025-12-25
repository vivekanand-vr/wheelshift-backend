package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.EmployeeRequest;
import com.wheelshiftpro.dto.response.EmployeeResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.enums.EmployeeStatus;

/**
 * Service interface for employee management operations.
 */
public interface EmployeeService {

    /**
     * Creates a new employee in the system.
     * Validates email uniqueness before creation.
     *
     * @param request the employee creation request
     * @return the created employee response
     * @throws com.wheelshiftpro.exception.DuplicateResourceException if email already exists
     */
    EmployeeResponse createEmployee(EmployeeRequest request);

    /**
     * Updates an existing employee.
     *
     * @param id the employee ID
     * @param request the update request
     * @return the updated employee response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee not found
     */
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    /**
     * Retrieves an employee by ID.
     *
     * @param id the employee ID
     * @return the employee response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee not found
     */
    EmployeeResponse getEmployeeById(Long id);

    /**
     * Retrieves all employees with pagination and optional regex search.
     *
     * @param search the search term (regex pattern) to filter employees (optional)
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated employee responses
     */
    PageResponse<EmployeeResponse> getAllEmployees(String search, int page, int size);

    /**
     * Deletes an employee by ID.
     * Prevents deletion if employee has associated sales, tasks, or events.
     *
     * @param id the employee ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee not found
     * @throws com.wheelshiftpro.exception.BusinessException if employee has dependencies
     */
    void deleteEmployee(Long id);

    /**
     * Searches employees by name, role, or status.
     *
     * @param name optional name filter (partial match)
     * @param role optional role filter
     * @param status optional status filter
     * @param page the page number
     * @param size the page size
     * @return paginated search results
     */
    PageResponse<EmployeeResponse> searchEmployees(String name, String role, EmployeeStatus status, int page, int size);

    /**
     * Retrieves employees by role.
     *
     * @param role the role to filter by
     * @param page the page number
     * @param size the page size
     * @return paginated employees with the specified role
     */
    PageResponse<EmployeeResponse> getEmployeesByRole(String role, int page, int size);

    /**
     * Retrieves active employees only.
     *
     * @param page the page number
     * @param size the page size
     * @return paginated active employees
     */
    PageResponse<EmployeeResponse> getActiveEmployees(int page, int size);

    /**
     * Updates employee status.
     *
     * @param id the employee ID
     * @param status the new status
     * @return the updated employee response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee not found
     */
    EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status);

    /**
     * Retrieves employee performance metrics including total sales and commission.
     *
     * @param id the employee ID
     * @return performance statistics
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee not found
     */
    Object getEmployeePerformance(Long id);
}

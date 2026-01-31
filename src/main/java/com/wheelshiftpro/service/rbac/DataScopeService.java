package com.wheelshiftpro.service.rbac;

import com.wheelshiftpro.dto.request.rbac.DataScopeRequest;
import com.wheelshiftpro.dto.response.rbac.DataScopeResponse;
import com.wheelshiftpro.enums.rbac.ScopeType;

import java.util.List;
import java.util.Set;

/**
 * Service interface for Employee Data Scope operations
 */
public interface DataScopeService {

    /**
     * Add a data scope to an employee
     */
    DataScopeResponse addScopeToEmployee(Long employeeId, DataScopeRequest request);

    /**
     * Update an existing data scope
     */
    DataScopeResponse updateScope(Long scopeId, DataScopeRequest request);

    /**
     * Remove a data scope from an employee
     */
    void removeScopeFromEmployee(Long scopeId);

    /**
     * Get all scopes for an employee
     */
    Set<DataScopeResponse> getScopesByEmployeeId(Long employeeId);

    /**
     * Get a specific scope by ID
     */
    DataScopeResponse getScopeById(Long scopeId);

    /**
     * Get scopes by employee and scope type
     */
    List<DataScopeResponse> getScopesByEmployeeIdAndType(Long employeeId, ScopeType scopeType);

    /**
     * Check if employee has access to a specific scope value
     */
    boolean hasScope(Long employeeId, ScopeType scopeType, String scopeValue);

    /**
     * Get all location scope values for an employee
     */
    Set<String> getLocationScopes(Long employeeId);

    /**
     * Get all department scope values for an employee
     */
    Set<String> getDepartmentScopes(Long employeeId);
}

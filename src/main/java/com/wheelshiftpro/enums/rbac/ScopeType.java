package com.wheelshiftpro.enums.rbac;

/**
 * Enum representing data scope types for filtering employee access.
 */
public enum ScopeType {
    /**
     * Scope by storage location
     */
    LOCATION,
    
    /**
     * Scope by department
     */
    DEPARTMENT,
    
    /**
     * Scope by assignment (only records assigned to the employee)
     */
    ASSIGNMENT
}

package com.wheelshiftpro.enums.rbac;

/**
 * Enum representing the different role types in the system.
 * Defines hierarchical access levels from SUPER_ADMIN to STORE_MANAGER.
 */
public enum RoleType {
    /**
     * Full system control; manage admins, global settings, feature flags, audit logs
     */
    SUPER_ADMIN,
    
    /**
     * Manage employees, inventory, inquiries, reservations, sales within data scopes
     */
    ADMIN,
    
    /**
     * Manage inquiries, reservations, sales within scope/assignment
     */
    SALES,
    
    /**
     * Create and update inspections
     */
    INSPECTOR,
    
    /**
     * View/record transactions, financial reports within scope
     */
    FINANCE,
    
    /**
     * Manage storage locations and movements within locations
     */
    STORE_MANAGER
}

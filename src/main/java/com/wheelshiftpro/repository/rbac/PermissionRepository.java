package com.wheelshiftpro.repository.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wheelshiftpro.entity.rbac.Permission;

import java.util.Optional;
import java.util.Set;

/**
 * Repository for Permission entity operations.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Find a permission by its name (resource:action format)
     */
    Optional<Permission> findByName(String name);

    /**
     * Find permissions by resource
     */
    Set<Permission> findByResource(String resource);

    /**
     * Find permissions by resource and action
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Check if a permission name already exists
     */
    boolean existsByName(String name);

    /**
     * Find all permissions for a specific role
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    Set<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * Find all permissions for an employee (through their roles)
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.employees e " +
           "WHERE e.id = :employeeId")
    Set<Permission> findByEmployeeId(@Param("employeeId") Long employeeId);
}

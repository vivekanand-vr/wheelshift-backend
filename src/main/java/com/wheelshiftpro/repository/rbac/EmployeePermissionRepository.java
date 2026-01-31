package com.wheelshiftpro.repository.rbac;

import com.wheelshiftpro.entity.rbac.EmployeePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for EmployeePermission entity.
 * Manages custom permissions assigned directly to employees.
 */
@Repository
public interface EmployeePermissionRepository extends JpaRepository<EmployeePermission, Long> {

    /**
     * Find all custom permissions for an employee
     */
    List<EmployeePermission> findByEmployeeId(Long employeeId);

    /**
     * Find a specific employee-permission assignment
     */
    Optional<EmployeePermission> findByEmployeeIdAndPermissionId(Long employeeId, Long permissionId);

    /**
     * Check if an employee has a specific permission assigned
     */
    boolean existsByEmployeeIdAndPermissionId(Long employeeId, Long permissionId);

    /**
     * Delete all custom permissions for an employee
     * @return the number of deleted records
     */
    int deleteByEmployeeId(Long employeeId);

    /**
     * Delete a specific employee-permission assignment
     */
    void deleteByEmployeeIdAndPermissionId(Long employeeId, Long permissionId);

    /**
     * Find all employees who have a specific custom permission
     */
    List<EmployeePermission> findByPermissionId(Long permissionId);

    /**
     * Get custom permission names for an employee
     */
    @Query("SELECT p.name FROM EmployeePermission ep JOIN ep.permission p " +
           "WHERE ep.employee.id = :employeeId")
    Set<String> findPermissionNamesByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Count custom permissions for an employee
     */
    long countByEmployeeId(Long employeeId);
}

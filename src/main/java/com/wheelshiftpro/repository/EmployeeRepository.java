package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find employee by email.
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<Employee> findByEmail(String email);

    /**
     * Check if employee exists by email.
     */
    boolean existsByEmail(String email);

    /**
     * Check if another employee exists with the same email, excluding the given ID.
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Count employees that have a role with the given name.
     */
    @Query("SELECT COUNT(e) FROM Employee e JOIN e.roles r WHERE r.name = :roleName")
    long countByRolesName(@Param("roleName") com.wheelshiftpro.enums.rbac.RoleType roleName);

    /**
     * Find employees by status.
     */
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    /**
     * Find employees by department.
     */
    List<Employee> findByDepartment(String department);

    /**
     * Find employees by position.
     */
    List<Employee> findByPosition(String position);

    /**
     * Search employees by name or email.
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Employee> searchEmployees(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search employees by pattern (regex-like) on name, email, position, or department.
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :pattern, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :pattern, '%')) OR " +
           "LOWER(e.position) LIKE LOWER(CONCAT('%', :pattern, '%')) OR " +
           "LOWER(e.department) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    Page<Employee> searchEmployeesByPattern(@Param("pattern") String pattern, Pageable pageable);

    /**
     * Get employee count by department.
     */
    @Query("SELECT e.department as department, COUNT(e) as count FROM Employee e WHERE e.status = 'ACTIVE' GROUP BY e.department")
    List<Object[]> getEmployeeCountByDepartment();

    /**
     * Count employees by status.
     */
    long countByStatus(EmployeeStatus status);
}

package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Role;
import com.wheelshiftpro.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository for Role entity operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its name
     */
    Optional<Role> findByName(RoleType name);

    /**
     * Find all system roles
     */
    Set<Role> findByIsSystemTrue();

    /**
     * Find all custom (non-system) roles
     */
    Set<Role> findByIsSystemFalse();

    /**
     * Check if a role name already exists
     */
    boolean existsByName(RoleType name);

    /**
     * Find all roles assigned to an employee
     */
    @Query("SELECT r FROM Role r JOIN r.employees e WHERE e.id = :employeeId")
    Set<Role> findByEmployeeId(@Param("employeeId") Long employeeId);
}

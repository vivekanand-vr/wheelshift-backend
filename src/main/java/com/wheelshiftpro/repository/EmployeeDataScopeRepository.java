package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.EmployeeDataScope;
import com.wheelshiftpro.enums.ScopeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Repository for EmployeeDataScope entity operations.
 */
@Repository
public interface EmployeeDataScopeRepository extends JpaRepository<EmployeeDataScope, Long> {

    /**
     * Find all data scopes for a specific employee
     */
    Set<EmployeeDataScope> findByEmployeeId(Long employeeId);

    /**
     * Find data scopes by employee and scope type
     */
    List<EmployeeDataScope> findByEmployeeIdAndScopeType(Long employeeId, ScopeType scopeType);

    /**
     * Find a specific scope by employee, type, and value
     */
    EmployeeDataScope findByEmployeeIdAndScopeTypeAndScopeValue(
        Long employeeId, 
        ScopeType scopeType, 
        String scopeValue
    );

    /**
     * Delete all scopes for an employee
     */
    void deleteByEmployeeId(Long employeeId);

    /**
     * Delete specific scope by employee, type, and value
     */
    void deleteByEmployeeIdAndScopeTypeAndScopeValue(
        Long employeeId, 
        ScopeType scopeType, 
        String scopeValue
    );
}

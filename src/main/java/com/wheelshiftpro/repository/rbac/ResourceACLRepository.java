package com.wheelshiftpro.repository.rbac;

import com.wheelshiftpro.entity.rbac.ResourceACL;
import com.wheelshiftpro.enums.rbac.AccessLevel;
import com.wheelshiftpro.enums.rbac.ResourceType;
import com.wheelshiftpro.enums.rbac.SubjectType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ResourceACL entity operations.
 */
@Repository
public interface ResourceACLRepository extends JpaRepository<ResourceACL, Long> {

    /**
     * Find all ACL entries for a specific resource
     */
    List<ResourceACL> findByResourceTypeAndResourceId(ResourceType resourceType, Long resourceId);

    /**
     * Find ACL entries for a specific subject (role or employee)
     */
    List<ResourceACL> findBySubjectTypeAndSubjectId(SubjectType subjectType, Long subjectId);

    /**
     * Find a specific ACL entry
     */
    Optional<ResourceACL> findByResourceTypeAndResourceIdAndSubjectTypeAndSubjectIdAndAccess(
        ResourceType resourceType,
        Long resourceId,
        SubjectType subjectType,
        Long subjectId,
        AccessLevel access
    );

    /**
     * Check if an employee has access to a resource through ACL
     */
    @Query("SELECT CASE WHEN COUNT(acl) > 0 THEN true ELSE false END " +
           "FROM ResourceACL acl " +
           "WHERE acl.resourceType = :resourceType " +
           "AND acl.resourceId = :resourceId " +
           "AND ((acl.subjectType = 'EMPLOYEE' AND acl.subjectId = :employeeId) " +
           "     OR (acl.subjectType = 'ROLE' AND acl.subjectId IN :roleIds)) " +
           "AND acl.access >= :minAccess")
    boolean hasAccess(
        @Param("resourceType") ResourceType resourceType,
        @Param("resourceId") Long resourceId,
        @Param("employeeId") Long employeeId,
        @Param("roleIds") List<Long> roleIds,
        @Param("minAccess") AccessLevel minAccess
    );

    /**
     * Delete all ACL entries for a resource
     */
    void deleteByResourceTypeAndResourceId(ResourceType resourceType, Long resourceId);

    /**
     * Delete ACL entries for a specific subject
     */
    void deleteBySubjectTypeAndSubjectId(SubjectType subjectType, Long subjectId);
}

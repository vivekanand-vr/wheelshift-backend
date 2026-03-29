package com.wheelshiftpro.service.impl.rbac;

import com.wheelshiftpro.dto.request.rbac.ResourceACLRequest;
import com.wheelshiftpro.dto.response.rbac.ResourceACLResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.rbac.ResourceACL;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.rbac.AccessLevel;
import com.wheelshiftpro.enums.rbac.ResourceType;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.rbac.ResourceACLRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.rbac.ResourceACLService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Resource ACL operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResourceACLServiceImpl implements ResourceACLService {

    private final ResourceACLRepository aclRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Override
    public ResourceACLResponse addACL(ResourceType resourceType, Long resourceId, ResourceACLRequest request, Long grantedBy) {
        log.info("Adding ACL for resource {}:{} to subject {}:{}", 
                resourceType, resourceId, request.getSubjectType(), request.getSubjectId());

        // Check if ACL already exists
        var existing = aclRepository.findByResourceTypeAndResourceIdAndSubjectTypeAndSubjectIdAndAccess(
                resourceType, resourceId, request.getSubjectType(), request.getSubjectId(), request.getAccess());

        if (existing.isPresent()) {
            throw new DuplicateResourceException("ResourceACL",
                    request.getSubjectType() + "/" + request.getSubjectId(),
                    resourceType + "/" + resourceId);
        }

        ResourceACL acl = ResourceACL.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .subjectType(request.getSubjectType())
                .subjectId(request.getSubjectId())
                .access(request.getAccess())
                .reason(request.getReason())
                .grantedBy(grantedBy)
                .build();

        acl = aclRepository.save(acl);
        log.info("ACL added successfully with ID: {}", acl.getId());

        auditService.log(AuditCategory.SYSTEM, acl.getId(), "CREATE_ACL",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                resourceType + "/" + resourceId + " -> " + request.getSubjectType()
                        + "/" + request.getSubjectId() + " [" + request.getAccess() + "]");

        return mapToResponse(acl);
    }

    @Override
    public void removeACL(Long aclId) {
        log.info("Removing ACL with ID: {}", aclId);

        ResourceACL acl = aclRepository.findById(aclId)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceACL", "id", aclId));

        aclRepository.delete(acl);
        log.info("ACL removed successfully: {}", aclId);

        auditService.log(AuditCategory.SYSTEM, aclId, "DELETE_ACL",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                acl.getResourceType() + "/" + acl.getResourceId() + " -> "
                        + acl.getSubjectType() + "/" + acl.getSubjectId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceACLResponse> getACLByResource(ResourceType resourceType, Long resourceId) {
        log.debug("Fetching ACL for resource {}:{}", resourceType, resourceId);
        return aclRepository.findByResourceTypeAndResourceId(resourceType, resourceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasACLAccess(ResourceType resourceType, Long resourceId, Long employeeId, AccessLevel minAccess) {
        log.debug("Checking ACL access for employee {} on resource {}:{}", employeeId, resourceType, resourceId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        List<Long> roleIds = employee.getRoles().stream()
                .map(role -> role.getId())
                .collect(Collectors.toList());

        return aclRepository.hasAccess(resourceType, resourceId, employeeId, roleIds, minAccess);
    }

    @Override
    public void removeAllACLForResource(ResourceType resourceType, Long resourceId) {
        log.info("Removing all ACL entries for resource {}:{}", resourceType, resourceId);
        aclRepository.deleteByResourceTypeAndResourceId(resourceType, resourceId);
        log.info("All ACL entries removed successfully");

        auditService.log(AuditCategory.SYSTEM, resourceId, "DELETE_ALL_ACL",
                AuditLevel.CRITICAL, resolveCurrentEmployee(),
                "All ACL entries removed for " + resourceType + "/" + resourceId);
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }

    private ResourceACLResponse mapToResponse(ResourceACL acl) {
        return ResourceACLResponse.builder()
                .id(acl.getId())
                .resourceType(acl.getResourceType())
                .resourceId(acl.getResourceId())
                .subjectType(acl.getSubjectType())
                .subjectId(acl.getSubjectId())
                .access(acl.getAccess())
                .reason(acl.getReason())
                .grantedBy(acl.getGrantedBy())
                .createdAt(acl.getCreatedAt())
                .updatedAt(acl.getUpdatedAt())
                .build();
    }
}

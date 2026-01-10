package com.wheelshiftpro.service.rbac;

import com.wheelshiftpro.dto.request.rbac.ResourceACLRequest;
import com.wheelshiftpro.dto.response.rbac.ResourceACLResponse;
import com.wheelshiftpro.enums.rbac.AccessLevel;
import com.wheelshiftpro.enums.rbac.ResourceType;

import java.util.List;

/**
 * Service interface for Resource ACL operations
 */
public interface ResourceACLService {

    /**
     * Add an ACL entry for a resource
     */
    ResourceACLResponse addACL(ResourceType resourceType, Long resourceId, ResourceACLRequest request, Long grantedBy);

    /**
     * Remove an ACL entry
     */
    void removeACL(Long aclId);

    /**
     * Get all ACL entries for a resource
     */
    List<ResourceACLResponse> getACLByResource(ResourceType resourceType, Long resourceId);

    /**
     * Check if employee has ACL access to a resource
     */
    boolean hasACLAccess(ResourceType resourceType, Long resourceId, Long employeeId, AccessLevel minAccess);

    /**
     * Remove all ACL entries for a resource
     */
    void removeAllACLForResource(ResourceType resourceType, Long resourceId);
}

package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.ResourceACLRequest;
import com.wheelshiftpro.dto.response.ResourceACLResponse;
import com.wheelshiftpro.enums.ResourceType;
import com.wheelshiftpro.service.ResourceACLService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing resource-level access control lists (ACLs)
 */
@RestController
@RequestMapping("/api/v1/rbac/acl")
@RequiredArgsConstructor
@Tag(name = "RBAC - ACL", description = "Resource access control list management endpoints")
public class ResourceACLController {

    private final ResourceACLService aclService;

    @GetMapping("/{resourceType}/{resourceId}")
    @Operation(summary = "Get ACL for resource", description = "Retrieve all ACL entries for a specific resource")
    public ResponseEntity<List<ResourceACLResponse>> getACLByResource(
            @PathVariable ResourceType resourceType,
            @PathVariable Long resourceId) {
        List<ResourceACLResponse> response = aclService.getACLByResource(resourceType, resourceId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{resourceType}/{resourceId}")
    @Operation(summary = "Add ACL entry", description = "Add an ACL entry for a resource (Super Admin or resource owner)")
    public ResponseEntity<ResourceACLResponse> addACL(
            @PathVariable ResourceType resourceType,
            @PathVariable Long resourceId,
            @Valid @RequestBody ResourceACLRequest request,
            @RequestHeader(value = "X-Employee-Id", required = false) Long grantedBy) {
        ResourceACLResponse response = aclService.addACL(resourceType, resourceId, request, grantedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{aclId}")
    @Operation(summary = "Remove ACL entry", description = "Remove an ACL entry (Super Admin or resource owner)")
    public ResponseEntity<Void> removeACL(@PathVariable Long aclId) {
        aclService.removeACL(aclId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{resourceType}/{resourceId}")
    @Operation(summary = "Remove all ACL for resource", description = "Remove all ACL entries for a resource (Super Admin only)")
    public ResponseEntity<Void> removeAllACLForResource(
            @PathVariable ResourceType resourceType,
            @PathVariable Long resourceId) {
        aclService.removeAllACLForResource(resourceType, resourceId);
        return ResponseEntity.noContent().build();
    }
}

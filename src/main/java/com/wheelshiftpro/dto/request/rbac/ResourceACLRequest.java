package com.wheelshiftpro.dto.request.rbac;

import com.wheelshiftpro.enums.rbac.AccessLevel;
import com.wheelshiftpro.enums.rbac.SubjectType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a resource ACL entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceACLRequest {

    @NotNull(message = "Subject type is required")
    private SubjectType subjectType;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Access level is required")
    private AccessLevel access;

    @Size(max = 512, message = "Reason must not exceed 512 characters")
    private String reason;
}

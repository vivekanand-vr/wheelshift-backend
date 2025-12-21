package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.AccessLevel;
import com.wheelshiftpro.enums.ResourceType;
import com.wheelshiftpro.enums.SubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for resource ACL response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceACLResponse {

    private Long id;
    private ResourceType resourceType;
    private Long resourceId;
    private SubjectType subjectType;
    private Long subjectId;
    private AccessLevel access;
    private String reason;
    private Long grantedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

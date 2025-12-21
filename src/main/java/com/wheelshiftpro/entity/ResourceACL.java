package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.AccessLevel;
import com.wheelshiftpro.enums.ResourceType;
import com.wheelshiftpro.enums.SubjectType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entity representing Access Control List (ACL) entries.
 * ACLs provide fine-grained, per-record access control beyond role-based permissions.
 */
@Entity
@Table(name = "resource_acl",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_resource_acl",
           columnNames = {"resource_type", "resource_id", "subject_type", "subject_id", "access"}
       ),
       indexes = {
           @Index(name = "idx_resource_acl_resource", columnList = "resource_type, resource_id"),
           @Index(name = "idx_resource_acl_subject", columnList = "subject_type, subject_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceACL extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Resource type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 20, nullable = false)
    private ResourceType resourceType;

    @NotNull(message = "Resource ID is required")
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @NotNull(message = "Subject type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", length = 20, nullable = false)
    private SubjectType subjectType;

    @NotNull(message = "Subject ID is required")
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @NotNull(message = "Access level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "access", length = 10, nullable = false)
    private AccessLevel access;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "granted_by")
    private Long grantedBy;
}

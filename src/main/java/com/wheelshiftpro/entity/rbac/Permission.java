package com.wheelshiftpro.entity.rbac;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import com.wheelshiftpro.entity.BaseEntity;

/**
 * Entity representing a permission in the RBAC system.
 * Permissions define specific actions on resources (e.g., cars:read, cars:write).
 */
@Entity
@Table(name = "permissions",
       uniqueConstraints = @UniqueConstraint(name = "uk_permission_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Resource is required")
    @Size(max = 64, message = "Resource must not exceed 64 characters")
    @Column(name = "resource", length = 64, nullable = false)
    private String resource;

    @NotBlank(message = "Action is required")
    @Size(max = 32, message = "Action must not exceed 32 characters")
    @Column(name = "action", length = 32, nullable = false)
    private String action;

    @NotBlank(message = "Name is required")
    @Size(max = 96, message = "Name must not exceed 96 characters")
    @Column(name = "name", length = 96, nullable = false, unique = true)
    private String name;

    @Size(max = 256, message = "Description must not exceed 256 characters")
    @Column(name = "description", length = 256)
    private String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Helper method to construct permission name from resource and action
     */
    public static String buildPermissionName(String resource, String action) {
        return resource + ":" + action;
    }

    /**
     * Pre-persist hook to ensure name is built from resource and action
     */
    @PrePersist
    @PreUpdate
    public void buildName() {
        if (this.name == null || this.name.isEmpty()) {
            this.name = buildPermissionName(this.resource, this.action);
        }
    }
}

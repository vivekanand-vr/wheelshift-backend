package com.wheelshiftpro.entity.rbac;

import com.wheelshiftpro.entity.BaseEntity;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.rbac.RoleType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a role in the RBAC system.
 * Roles define sets of permissions and can be assigned to employees.
 */
@Entity
@Table(name = "roles",
       uniqueConstraints = @UniqueConstraint(name = "uk_role_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Role type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 64, nullable = false, unique = true)
    private RoleType name;

    @Size(max = 256, message = "Description must not exceed 256 characters")
    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_role_permission", columnNames = {"role_id", "permission_id"})
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Employee> employees = new HashSet<>();

    /**
     * Helper method to add a permission to this role
     */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    /**
     * Helper method to remove a permission from this role
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }
}

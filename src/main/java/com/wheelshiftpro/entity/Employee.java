package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.EmployeeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing an employee in the system.
 * Manages employee information, credentials, and relationships with sales and inquiries.
 */
@Entity
@Table(name = "employees",
       uniqueConstraints = @UniqueConstraint(name = "uk_employee_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 128, message = "Name must not exceed 128 characters")
    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 128, message = "Email must not exceed 128 characters")
    @Column(name = "email", length = 128, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password hash must not exceed 255 characters")
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Size(max = 32, message = "Phone must not exceed 32 characters")
    @Column(name = "phone", length = 32)
    private String phone;

    @Size(max = 64, message = "Position must not exceed 64 characters")
    @Column(name = "position", length = 64)
    private String position;

    @Size(max = 64, message = "Department must not exceed 64 characters")
    @Column(name = "department", length = 64)
    private String department;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "assignedEmployee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Inquiry> assignedInquiries = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sale> handledSales = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Task> assignedTasks = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "employee_roles",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_employee_role", columnNames = {"employee_id", "role_id"})
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EmployeeDataScope> dataScopes = new HashSet<>();

    /**
     * Helper method to add a role to this employee
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getEmployees().add(this);
    }

    /**
     * Helper method to remove a role from this employee
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getEmployees().remove(this);
    }

    /**
     * Helper method to add a data scope
     */
    public void addDataScope(EmployeeDataScope scope) {
        this.dataScopes.add(scope);
        scope.setEmployee(this);
    }

    /**
     * Helper method to remove a data scope
     */
    public void removeDataScope(EmployeeDataScope scope) {
        this.dataScopes.remove(scope);
        scope.setEmployee(null);
    }
}

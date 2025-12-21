package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.ScopeEffect;
import com.wheelshiftpro.enums.ScopeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entity representing data scopes for employees.
 * Scopes restrict employee access to specific locations, departments, or assignments.
 */
@Entity
@Table(name = "employee_data_scopes",
       indexes = {
           @Index(name = "idx_employee_scope", columnList = "employee_id, scope_type, scope_value")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDataScope extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Scope type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", length = 20, nullable = false)
    private ScopeType scopeType;

    @NotBlank(message = "Scope value is required")
    @Size(max = 128, message = "Scope value must not exceed 128 characters")
    @Column(name = "scope_value", length = 128, nullable = false)
    private String scopeValue;

    @NotNull(message = "Effect is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "effect", length = 10, nullable = false)
    @Builder.Default
    private ScopeEffect effect = ScopeEffect.INCLUDE;

    @Size(max = 512, message = "Description must not exceed 512 characters")
    @Column(name = "description", length = 512)
    private String description;
}

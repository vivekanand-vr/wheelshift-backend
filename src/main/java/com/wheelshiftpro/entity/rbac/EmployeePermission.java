package com.wheelshiftpro.entity.rbac;

import com.wheelshiftpro.entity.BaseEntity;
import com.wheelshiftpro.entity.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entity representing a custom permission assigned directly to an employee.
 * This allows super admins to grant additional permissions to employees
 * beyond what their roles provide.
 * 
 * Example: A SALES employee might need temporary access to financial reports,
 * so a super admin can assign "transactions:read" permission directly.
 */
@Entity
@Table(name = "employee_permissions",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_employee_permission",
                           columnNames = {"employee_id", "permission_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePermission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee receiving the custom permission
     */
    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_emp_perm_employee"))
    private Employee employee;

    /**
     * The permission being granted
     */
    @NotNull(message = "Permission is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_emp_perm_permission"))
    private Permission permission;

    /**
     * The admin who granted this permission (for audit trail)
     */
    @NotNull(message = "Granted by admin is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false,
                foreignKey = @ForeignKey(name = "fk_emp_perm_granted_by"))
    private Employee grantedBy;

    /**
     * Reason for granting this custom permission
     */
    @Column(name = "reason", length = 255)
    private String reason;
}

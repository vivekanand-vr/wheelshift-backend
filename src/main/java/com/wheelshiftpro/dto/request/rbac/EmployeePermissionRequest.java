package com.wheelshiftpro.dto.request.rbac;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning a custom permission to an employee.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePermissionRequest {

    @NotNull(message = "Permission ID is required")
    private Long permissionId;

    /**
     * Reason for granting this custom permission
     */
    private String reason;
}

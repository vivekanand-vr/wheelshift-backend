package com.wheelshiftpro.dto.response.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for employee permission response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePermissionResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long permissionId;
    private String permissionName;
    private String permissionResource;
    private String permissionAction;
    private Long grantedBy;
    private String grantedByName;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

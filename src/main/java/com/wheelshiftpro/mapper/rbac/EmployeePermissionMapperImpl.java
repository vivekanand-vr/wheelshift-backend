package com.wheelshiftpro.mapper.rbac;

import com.wheelshiftpro.dto.response.rbac.EmployeePermissionResponse;
import com.wheelshiftpro.entity.rbac.EmployeePermission;
import org.springframework.stereotype.Component;

/**
 * Manual implementation of EmployeePermissionMapper (temporary until MapStruct processes correctly)
 */
@Component
public class EmployeePermissionMapperImpl implements EmployeePermissionMapper {

    @Override
    public EmployeePermissionResponse toResponse(EmployeePermission employeePermission) {
        if (employeePermission == null) {
            return null;
        }

        return EmployeePermissionResponse.builder()
                .id(employeePermission.getId())
                .employeeId(employeePermission.getEmployee().getId())
                .employeeName(employeePermission.getEmployee().getName())
                .employeeEmail(employeePermission.getEmployee().getEmail())
                .permissionId(employeePermission.getPermission().getId())
                .permissionName(employeePermission.getPermission().getName())
                .permissionResource(employeePermission.getPermission().getResource())
                .permissionAction(employeePermission.getPermission().getAction())
                .grantedBy(employeePermission.getGrantedBy().getId())
                .grantedByName(employeePermission.getGrantedBy().getName())
                .reason(employeePermission.getReason())
                .createdAt(employeePermission.getCreatedAt())
                .updatedAt(employeePermission.getUpdatedAt())
                .build();
    }
}

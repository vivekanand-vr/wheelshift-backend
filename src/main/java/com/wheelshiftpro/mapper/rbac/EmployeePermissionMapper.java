package com.wheelshiftpro.mapper.rbac;

import com.wheelshiftpro.dto.response.rbac.EmployeePermissionResponse;
import com.wheelshiftpro.entity.rbac.EmployeePermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for EmployeePermission entity to DTO mappings.
 */
@Mapper(componentModel = "spring")
public interface EmployeePermissionMapper {

    /**
     * Map EmployeePermission entity to response DTO.
     *
     * @param employeePermission the entity
     * @return the response DTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "employeeEmail", source = "employee.email")
    @Mapping(target = "permissionId", source = "permission.id")
    @Mapping(target = "permissionName", source = "permission.name")
    @Mapping(target = "permissionResource", source = "permission.resource")
    @Mapping(target = "permissionAction", source = "permission.action")
    @Mapping(target = "grantedBy", source = "grantedBy.id")
    @Mapping(target = "grantedByName", source = "grantedBy.name")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    EmployeePermissionResponse toResponse(EmployeePermission employeePermission);
}

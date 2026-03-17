package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.EmployeeRequest;
import com.wheelshiftpro.dto.response.EmployeeResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Employee entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { FileUrlBuilder.class })
public interface EmployeeMapper {

    /**
     * Converts Employee entity to EmployeeResponse DTO.
     */
    @Mapping(target = "profileImageUrl", expression = "java(FileUrlBuilder.buildFileUrl(employee.getProfileImageId()))")
    EmployeeResponse toResponse(Employee employee);

    /**
     * Converts EmployeeRequest DTO to Employee entity.
     */
    @Mapping(target = "id", ignore = true)
    Employee toEntity(EmployeeRequest request);

    /**
     * Converts list of Employee entities to list of EmployeeResponse DTOs.
     */
    List<EmployeeResponse> toResponseList(List<Employee> employees);

    /**
     * Updates Employee entity from EmployeeRequest DTO.
     * Ignores null values in the request.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(EmployeeRequest request, @MappingTarget Employee employee);
}

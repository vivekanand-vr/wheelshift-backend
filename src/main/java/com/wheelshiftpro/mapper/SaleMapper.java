package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.SaleRequest;
import com.wheelshiftpro.dto.response.SaleResponse;
import com.wheelshiftpro.entity.Sale;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Sale entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleMapper {

    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "carVin", source = "car.vinNumber")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    SaleResponse toResponse(Sale entity);

    List<SaleResponse> toResponseList(List<Sale> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "totalCommission", ignore = true)
    Sale toEntity(SaleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalCommission", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "employee", ignore = true)
    void updateEntityFromRequest(SaleRequest request, @MappingTarget Sale entity);
}

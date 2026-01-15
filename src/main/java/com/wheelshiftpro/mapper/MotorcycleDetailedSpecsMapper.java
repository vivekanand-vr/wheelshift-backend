package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.MotorcycleDetailedSpecsRequest;
import com.wheelshiftpro.dto.response.MotorcycleDetailedSpecsResponse;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.entity.MotorcycleDetailedSpecs;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for MotorcycleDetailedSpecs entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MotorcycleDetailedSpecsMapper {

    /**
     * Convert entity to response DTO
     */
    @Mapping(target = "motorcycleId", source = "motorcycle.id")
    @Mapping(target = "powerToWeightRatio", expression = "java(entity.getPowerToWeightRatio())")
    MotorcycleDetailedSpecsResponse toResponse(MotorcycleDetailedSpecs entity);

    /**
     * Convert request DTO to entity (do NOT map motorcycle relationship)
     */
    @Mapping(target = "motorcycle", ignore = true)
    MotorcycleDetailedSpecs toEntity(MotorcycleDetailedSpecsRequest request);

    /**
     * Convert list of entities to list of response DTOs
     */
    List<MotorcycleDetailedSpecsResponse> toResponseList(List<MotorcycleDetailedSpecs> entities);

    /**
     * Update entity from request DTO (for PATCH/PUT operations)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "motorcycle", ignore = true)
    void updateEntityFromRequest(MotorcycleDetailedSpecsRequest request, @MappingTarget MotorcycleDetailedSpecs entity);

    /**
     * Helper method to set motorcycle relationship
     */
    default void setMotorcycle(MotorcycleDetailedSpecs entity, Motorcycle motorcycle) {
        entity.setMotorcycle(motorcycle);
    }
}

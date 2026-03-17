package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.MotorcycleModelRequest;
import com.wheelshiftpro.dto.response.MotorcycleModelResponse;
import com.wheelshiftpro.entity.MotorcycleModel;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for MotorcycleModel entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = {
        FileUrlBuilder.class }
)
public interface MotorcycleModelMapper {

    /**
     * Convert entity to response DTO
     */
    @Mapping(target = "fullName", expression = "java(entity.getFullName())")
    @Mapping(target = "modelImageUrl", expression = "java(FileUrlBuilder.buildFileUrl(entity.getModelImageId()))")
    MotorcycleModelResponse toResponse(MotorcycleModel entity);

    /**
     * Convert request DTO to entity
     */
    MotorcycleModel toEntity(MotorcycleModelRequest request);

    /**
     * Convert list of entities to list of response DTOs
     */
    List<MotorcycleModelResponse> toResponseList(List<MotorcycleModel> entities);

    /**
     * Update entity from request DTO (for PATCH/PUT operations)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(MotorcycleModelRequest request, @MappingTarget MotorcycleModel entity);
}

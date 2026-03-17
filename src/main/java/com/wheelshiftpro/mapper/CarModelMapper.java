package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.CarModelRequest;
import com.wheelshiftpro.dto.response.CarModelResponse;
import com.wheelshiftpro.entity.CarModel;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for CarModel entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = {
        FileUrlBuilder.class })
public interface CarModelMapper {

    @Mapping(target = "modelImageUrl", expression = "java(FileUrlBuilder.buildFileUrl(entity.getModelImageId()))")
    CarModelResponse toResponse(CarModel entity);

    List<CarModelResponse> toResponseList(List<CarModel> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cars", ignore = true)
    CarModel toEntity(CarModelRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cars", ignore = true)
    void updateEntityFromRequest(CarModelRequest request, @MappingTarget CarModel entity);
}

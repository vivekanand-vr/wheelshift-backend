package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.StorageLocationRequest;
import com.wheelshiftpro.dto.response.StorageLocationResponse;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for StorageLocation entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = { FileUrlBuilder.class })
public interface StorageLocationMapper {

    @Mapping(target = "availableCapacity", expression = "java(entity.getAvailableCapacity())")
    @Mapping(target = "currentVehicleCount", expression = "java(entity.getCurrentVehicleCount())")
    @Mapping(target = "locationImageUrl", expression = "java(FileUrlBuilder.buildFileUrl(entity.getLocationImageId()))")
    StorageLocationResponse toResponse(StorageLocation entity);

    List<StorageLocationResponse> toResponseList(List<StorageLocation> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cars", ignore = true)
    @Mapping(target = "currentCarCount", constant = "0")
    @Mapping(target = "currentMotorcycleCount", constant = "0")
    StorageLocation toEntity(StorageLocationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cars", ignore = true)
    @Mapping(target = "currentCarCount", ignore = true)
    @Mapping(target = "currentMotorcycleCount", ignore = true)
    void updateEntityFromRequest(StorageLocationRequest request, @MappingTarget StorageLocation entity);
}

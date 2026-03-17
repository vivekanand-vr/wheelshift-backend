package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.CarInspectionRequest;
import com.wheelshiftpro.dto.response.CarInspectionResponse;
import com.wheelshiftpro.entity.CarInspection;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for CarInspection entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { FileUrlBuilder.class })
public interface CarInspectionMapper {

    /**
     * Converts CarInspection entity to CarInspectionResponse DTO.
     */
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.vinNumber", target = "carVin")
    @Mapping(target = "inspectionImageIds", expression = "java(FileUrlBuilder.splitToList(carInspection.getInspectionImageIds()))")
    @Mapping(target = "inspectionImageUrls", expression = "java(FileUrlBuilder.buildFileUrls(carInspection.getInspectionImageIds()))")
    @Mapping(target = "inspectionReportFileUrl", expression = "java(FileUrlBuilder.buildFileUrl(carInspection.getInspectionReportFileId()))")
    CarInspectionResponse toResponse(CarInspection carInspection);

    /**
     * Converts CarInspectionRequest DTO to CarInspection entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car.id")
    @Mapping(target = "inspectionImageIds", expression = "java(FileUrlBuilder.joinList(request.getInspectionImageIds()))")
    CarInspection toEntity(CarInspectionRequest request);

    /**
     * Converts list of CarInspection entities to list of CarInspectionResponse DTOs.
     */
    List<CarInspectionResponse> toResponseList(List<CarInspection> carInspections);

    /**
     * Updates CarInspection entity from CarInspectionRequest DTO.
     * Ignores null values in the request.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car.id")
    @Mapping(target = "inspectionImageIds", expression = "java(FileUrlBuilder.joinList(request.getInspectionImageIds()))")
    void updateEntityFromRequest(CarInspectionRequest request, @MappingTarget CarInspection carInspection);
}

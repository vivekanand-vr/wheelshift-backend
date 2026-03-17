package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.MotorcycleInspectionRequest;
import com.wheelshiftpro.dto.response.MotorcycleInspectionResponse;
import com.wheelshiftpro.entity.MotorcycleInspection;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for MotorcycleInspection entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { FileUrlBuilder.class })
public interface MotorcycleInspectionMapper {

    /**
     * Converts MotorcycleInspection entity to MotorcycleInspectionResponse DTO.
     */
    @Mapping(source = "motorcycle.id", target = "motorcycleId")
    @Mapping(source = "motorcycle.vinNumber", target = "motorcycleVin")
    @Mapping(source = "inspector.id", target = "inspectorId")
    @Mapping(source = "inspector.name", target = "inspectorName")
    @Mapping(target = "inspectionImageIds", expression = "java(FileUrlBuilder.splitToList(motorcycleInspection.getInspectionImageIds()))")
    @Mapping(target = "inspectionImageUrls", expression = "java(FileUrlBuilder.buildFileUrls(motorcycleInspection.getInspectionImageIds()))")
    @Mapping(target = "inspectionReportFileUrl", expression = "java(FileUrlBuilder.buildFileUrl(motorcycleInspection.getInspectionReportFileId()))")
    MotorcycleInspectionResponse toResponse(MotorcycleInspection motorcycleInspection);

    /**
     * Converts MotorcycleInspectionRequest DTO to MotorcycleInspection entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "motorcycleId", target = "motorcycle.id")
    @Mapping(source = "inspectorId", target = "inspector.id")
    @Mapping(target = "inspectionImageIds", expression = "java(FileUrlBuilder.joinList(request.getInspectionImageIds()))")
    MotorcycleInspection toEntity(MotorcycleInspectionRequest request);

    /**
     * Converts list of MotorcycleInspection entities to list of MotorcycleInspectionResponse DTOs.
     */
    List<MotorcycleInspectionResponse> toResponseList(List<MotorcycleInspection> motorcycleInspections);

    /**
     * Updates MotorcycleInspection entity from MotorcycleInspectionRequest DTO.
     * Ignores null values in the request.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "motorcycleId", target = "motorcycle.id")
    @Mapping(source = "inspectorId", target = "inspector.id")
    @Mapping(target = "inspectionImageIds", expression = "java(FileUrlBuilder.joinList(request.getInspectionImageIds()))")
    void updateEntityFromRequest(MotorcycleInspectionRequest request, @MappingTarget MotorcycleInspection motorcycleInspection);
}

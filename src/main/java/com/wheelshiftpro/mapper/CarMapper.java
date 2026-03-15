package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.CarRequest;
import com.wheelshiftpro.dto.response.CarResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.utils.FileUrlBuilder;

import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Car entity and DTOs.
 * Simplified after merging CarDetailedSpecs and CarFeature into Car entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = {
        FileUrlBuilder.class })
public interface CarMapper {

    @Mapping(target = "carModelId", source = "carModel.id")
    @Mapping(target = "carModelMake", source = "carModel.make")
    @Mapping(target = "carModelModel", source = "carModel.model")
    @Mapping(target = "carModelVariant", source = "carModel.variant")
    @Mapping(target = "storageLocationId", source = "storageLocation.id")
    @Mapping(target = "storageLocationName", source = "storageLocation.name")
    
    // Detailed specs are now direct fields on Car entity
    @Mapping(target = "doors", source = "doors")
    @Mapping(target = "seats", source = "seats")
    @Mapping(target = "cargoCapacityLiters", source = "cargoCapacityLiters")
    @Mapping(target = "acceleration0To100", source = "acceleration0To100")
    @Mapping(target = "topSpeedKmh", source = "topSpeedKmh")
    
    // Features are now JSON field on Car entity
    @Mapping(target = "features", source = "features")
    
    // File IDs
    @Mapping(target = "primaryImageId", source = "primaryImageId")
    @Mapping(target = "galleryImageIds", source = "galleryImageIds")
    @Mapping(target = "documentFileIds", source = "documentFileIds")

    // Generate file URLs using FileUrlBuilder for response DTO
    @Mapping(target = "primaryImageUrl", expression = "java(FileUrlBuilder.buildFileUrl(car.getPrimaryImageId()))")
    @Mapping(target = "galleryImageUrls", expression = "java(FileUrlBuilder.buildFileUrls(car.getGalleryImageIds()))")
    @Mapping(target = "documentFileUrls", expression = "java(FileUrlBuilder.buildFileUrls(car.getDocumentFileIds()))")
    CarResponse toResponse(Car car);

    /**
     * Convert list of Car entities to list of CarResponse DTOs.
     *
     * @param cars List of Car entities
     * @return List of CarResponse DTOs
     */
    List<CarResponse> toResponseList(List<Car> cars);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carModel", ignore = true)
    @Mapping(target = "storageLocation", ignore = true)
    @Mapping(target = "inspections", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "sale", ignore = true)
    
    // File IDs conversion
    @Mapping(target = "primaryImageId", source = "primaryImageId")
    @Mapping(target = "galleryImageIds", expression = "java(FileUrlBuilder.joinList(request.getGalleryImageIds()))")
    @Mapping(target = "documentFileIds", expression = "java(FileUrlBuilder.joinList(request.getDocumentFileIds()))")
    
    // Direct mapping for detailed specs and features
    @Mapping(target = "doors", source = "doors")
    @Mapping(target = "seats", source = "seats")
    @Mapping(target = "cargoCapacityLiters", source = "cargoCapacityLiters")
    @Mapping(target = "acceleration0To100", source = "acceleration0To100")
    @Mapping(target = "topSpeedKmh", source = "topSpeedKmh")
    @Mapping(target = "features", source = "features")
    Car toEntity(CarRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carModel", ignore = true)
    @Mapping(target = "storageLocation", ignore = true)
    @Mapping(target = "inspections", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "sale", ignore = true)
    
    // File IDs conversion
    @Mapping(target = "galleryImageIds", expression = "java(FileUrlBuilder.joinList(request.getGalleryImageIds()))")
    @Mapping(target = "documentFileIds", expression = "java(FileUrlBuilder.joinList(request.getDocumentFileIds()))")
    
    // Direct mapping for detailed specs and features
    @Mapping(target = "doors", source = "doors")
    @Mapping(target = "seats", source = "seats")
    @Mapping(target = "cargoCapacityLiters", source = "cargoCapacityLiters")
    @Mapping(target = "acceleration0To100", source = "acceleration0To100")
    @Mapping(target = "topSpeedKmh", source = "topSpeedKmh")
    @Mapping(target = "features", source = "features")
    void updateEntityFromRequest(CarRequest request, @MappingTarget Car entity);
}

package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.CarRequest;
import com.wheelshiftpro.dto.response.CarResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.CarDetailedSpecs;
import com.wheelshiftpro.entity.CarFeature;
import com.wheelshiftpro.utils.FileUrlBuilder;

import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Car entity and DTOs.
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
    @Mapping(target = "doors", source = "detailedSpecs.doors")
    @Mapping(target = "seats", source = "detailedSpecs.seats")
    @Mapping(target = "cargoCapacityLiters", source = "detailedSpecs.cargoCapacityLiters")
    @Mapping(target = "acceleration0To100", source = "detailedSpecs.acceleration0To100")
    @Mapping(target = "topSpeedKmh", source = "detailedSpecs.topSpeedKmh")
    @Mapping(target = "features", expression = "java(mapFeaturesToMap(car.getFeatures()))")
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
    @Mapping(target = "detailedSpecs", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "inspections", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "sale", ignore = true)
    @Mapping(target = "primaryImageId", source = "primaryImageId")
    @Mapping(target = "galleryImageIds", expression = "java(FileUrlBuilder.joinList(request.getGalleryImageIds()))")
    @Mapping(target = "documentFileIds", expression = "java(FileUrlBuilder.joinList(request.getDocumentFileIds()))")
    Car toEntity(CarRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carModel", ignore = true)
    @Mapping(target = "storageLocation", ignore = true)
    @Mapping(target = "detailedSpecs", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "inspections", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "sale", ignore = true)
    @Mapping(target = "galleryImageIds", expression = "java(FileUrlBuilder.joinList(request.getGalleryImageIds()))")
    @Mapping(target = "documentFileIds", expression = "java(FileUrlBuilder.joinList(request.getDocumentFileIds()))")
    void updateEntityFromRequest(CarRequest request, @MappingTarget Car entity);

    /**
     * Helper method to convert list of CarFeature entities to a Map<String, String>
     * for
     * 
     * @param features
     * @return Map of feature name to feature value, or null if features list is
     *         null
     */
    default Map<String, String> mapFeaturesToMap(List<CarFeature> features) {
        if (features == null) {
            return null;
        }
        return features.stream()
                .collect(Collectors.toMap(CarFeature::getFeatureName, CarFeature::getFeatureValue));
    }

    /**
     * Helper method to map detailed specs from CarRequest to CarDetailedSpecs
     * entity.
     * 
     * @param request CarRequest containing detailed specs fields
     * @return CarDetailedSpecs entity or null if request is null
     */
    default CarDetailedSpecs mapToDetailedSpecs(CarRequest request) {
        if (request == null) {
            return null;
        }
        return CarDetailedSpecs.builder()
                .doors(request.getDoors())
                .seats(request.getSeats())
                .cargoCapacityLiters(request.getCargoCapacityLiters())
                .acceleration0To100(request.getAcceleration0To100())
                .topSpeedKmh(request.getTopSpeedKmh())
                .build();
    }
}

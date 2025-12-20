package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.CarRequest;
import com.wheelshiftpro.dto.response.CarResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.CarDetailedSpecs;
import com.wheelshiftpro.entity.CarFeature;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Car entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
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
    @Mapping(target = "features", expression = "java(mapFeaturesToMap(entity.getFeatures()))")
    CarResponse toResponse(Car entity);

    List<CarResponse> toResponseList(List<Car> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carModel", ignore = true)
    @Mapping(target = "storageLocation", ignore = true)
    @Mapping(target = "detailedSpecs", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "inspections", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "sale", ignore = true)
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
    void updateEntityFromRequest(CarRequest request, @MappingTarget Car entity);

    default Map<String, String> mapFeaturesToMap(List<CarFeature> features) {
        if (features == null) {
            return null;
        }
        return features.stream()
                .collect(Collectors.toMap(CarFeature::getFeatureName, CarFeature::getFeatureValue));
    }

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

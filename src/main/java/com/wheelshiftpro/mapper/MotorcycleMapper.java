package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.MotorcycleRequest;
import com.wheelshiftpro.dto.response.MotorcycleResponse;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.entity.MotorcycleModel;
import com.wheelshiftpro.entity.StorageLocation;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Motorcycle entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MotorcycleMapper {

    /**
     * Convert entity to response DTO
     */
    @Mapping(target = "motorcycleModelId", source = "motorcycleModel.id")
    @Mapping(target = "motorcycleModelFullName", expression = "java(entity.getMotorcycleModel() != null ? entity.getMotorcycleModel().getFullName() : null)")
    @Mapping(target = "make", source = "motorcycleModel.make")
    @Mapping(target = "model", source = "motorcycleModel.model")
    @Mapping(target = "variant", source = "motorcycleModel.variant")
    @Mapping(target = "modelYear", source = "motorcycleModel.year")
    @Mapping(target = "storageLocationId", source = "storageLocation.id")
    @Mapping(target = "storageLocationName", source = "storageLocation.name")
    @Mapping(target = "profitMargin", expression = "java(entity.calculateProfitMargin())")
    @Mapping(target = "ageInYears", expression = "java(entity.getAgeInYears())")
    @Mapping(target = "isInsuranceExpired", expression = "java(entity.isInsuranceExpired())")
    @Mapping(target = "isPollutionCertificateExpired", expression = "java(entity.isPollutionCertificateExpired())")
    @Mapping(target = "fullIdentification", expression = "java(entity.getFullIdentification())")
    MotorcycleResponse toResponse(Motorcycle entity);

    /**
     * Convert request DTO to entity (do NOT map relationships here)
     */
    @Mapping(target = "motorcycleModel", ignore = true)
    @Mapping(target = "storageLocation", ignore = true)
    @Mapping(target = "detailedSpecs", ignore = true)
    Motorcycle toEntity(MotorcycleRequest request);

    /**
     * Convert list of entities to list of response DTOs
     */
    List<MotorcycleResponse> toResponseList(List<Motorcycle> entities);

    /**
     * Update entity from request DTO (for PATCH/PUT operations)
     * Do NOT update relationships using this method
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "motorcycleModel", ignore = true)
    @Mapping(target = "storageLocation", ignore = true)
    @Mapping(target = "detailedSpecs", ignore = true)
    void updateEntityFromRequest(MotorcycleRequest request, @MappingTarget Motorcycle entity);

    /**
     * Helper method to map motorcycle model ID to entity
     * This should be called manually in the service layer
     */
    default void setMotorcycleModel(Motorcycle entity, MotorcycleModel motorcycleModel) {
        entity.setMotorcycleModel(motorcycleModel);
    }

    /**
     * Helper method to map storage location ID to entity
     * This should be called manually in the service layer
     */
    default void setStorageLocation(Motorcycle entity, StorageLocation storageLocation) {
        entity.setStorageLocation(storageLocation);
    }
}

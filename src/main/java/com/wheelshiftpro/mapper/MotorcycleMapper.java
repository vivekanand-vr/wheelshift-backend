package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.MotorcycleRequest;
import com.wheelshiftpro.dto.response.MotorcycleResponse;
import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.entity.MotorcycleModel;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.utils.FileUrlBuilder;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Motorcycle entity and DTOs.
 * Simplified after merging MotorcycleDetailedSpecs into Motorcycle entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = { FileUrlBuilder.class })
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
    @Mapping(target = "powerToWeightRatio", expression = "java(entity.getPowerToWeightRatio())")
    @Mapping(target = "ageInYears", expression = "java(entity.getAgeInYears())")
    @Mapping(target = "isInsuranceExpired", expression = "java(entity.isInsuranceExpired())")
    @Mapping(target = "isPollutionCertificateExpired", expression = "java(entity.isPollutionCertificateExpired())")
    @Mapping(target = "fullIdentification", expression = "java(entity.getFullIdentification())")
    
    // File IDs
    @Mapping(target = "primaryImageId", source = "primaryImageId")
    @Mapping(target = "galleryImageIds", source = "galleryImageIds")
    @Mapping(target = "documentFileIds", source = "documentFileIds")
    
    // Generate file URLs using FileUrlBuilder
    @Mapping(target = "primaryImageUrl", expression = "java(FileUrlBuilder.buildFileUrl(entity.getPrimaryImageId()))")
    @Mapping(target = "galleryImageUrls", expression = "java(FileUrlBuilder.buildFileUrls(entity.getGalleryImageIds()))")
    @Mapping(target = "documentFileUrls", expression = "java(FileUrlBuilder.buildFileUrls(entity.getDocumentFileIds()))")
    MotorcycleResponse toResponse(Motorcycle entity);

    /**
     * Convert request DTO to entity (do NOT map relationships here)
     */
    @Mapping(target = "motorcycleModel", ignore = true)
    @Mapping(target = "storageLocation", ignore = true)
    
    // File IDs conversion
    @Mapping(target = "primaryImageId", source = "primaryImageId")
    @Mapping(target = "galleryImageIds", expression = "java(FileUrlBuilder.joinList(request.getGalleryImageIds()))")
    @Mapping(target = "documentFileIds", expression = "java(FileUrlBuilder.joinList(request.getDocumentFileIds()))")
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
    
    // File IDs conversion
    @Mapping(target = "galleryImageIds", expression = "java(FileUrlBuilder.joinList(request.getGalleryImageIds()))")
    @Mapping(target = "documentFileIds", expression = "java(FileUrlBuilder.joinList(request.getDocumentFileIds()))")
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

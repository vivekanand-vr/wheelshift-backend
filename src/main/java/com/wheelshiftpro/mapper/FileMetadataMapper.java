package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.response.FileMetadataResponse;
import com.wheelshiftpro.entity.FileMetadata;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for converting between FileMetadata entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface FileMetadataMapper {

    /**
     * Convert FileMetadata entity to full response DTO.
     *
     * @param fileMetadata FileMetadata entity
     * @return FileMetadataResponse DTO
     */
    FileMetadataResponse toResponse(FileMetadata fileMetadata);

    /**
     * Convert FileMetadata entity to simplified response DTO.
     *
     * @param fileMetadata FileMetadata entity
     * @return Simplified FileMetadataResponse DTO
     */
    FileMetadataResponse.Simplified toSimplifiedResponse(FileMetadata fileMetadata);

    /**
     * Convert list of FileMetadata entities to list of response DTOs.
     *
     * @param fileMetadataList List of FileMetadata entities
     * @return List of FileMetadataResponse DTOs
     */
    List<FileMetadataResponse> toResponseList(List<FileMetadata> fileMetadataList);

    /**
     * Convert list of FileMetadata entities to list of simplified response DTOs.
     *
     * @param fileMetadataList List of FileMetadata entities
     * @return List of simplified FileMetadataResponse DTOs
     */
    List<FileMetadataResponse.Simplified> toSimplifiedResponseList(List<FileMetadata> fileMetadataList);
}
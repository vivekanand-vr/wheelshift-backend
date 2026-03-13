package com.wheelshiftpro.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for file upload with optional metadata.
 * Note: The actual file is sent as MultipartFile, this DTO contains additional metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {

    @Size(max = 64, message = "Upload source must not exceed 64 characters")
    private String uploadSource;

    @Size(max = 128, message = "Uploaded by must not exceed 128 characters")
    private String uploadedBy;

    private String additionalMetadata; // JSON string for any additional metadata
}
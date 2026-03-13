package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.FileStatus;
import com.wheelshiftpro.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for file metadata.
 * Contains all information about an uploaded file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataResponse {

    private Long id;
    private String fileId;
    private String originalFilename;
    private FileType fileType;
    private String mimeType;
    private Long fileSize;
    private String fileExtension;
    private String publicUrl;
    private String uploadSource;
    private String uploadedBy;
    private FileStatus status;
    private String metadataJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Simplified response with only essential information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Simplified {
        private String fileId;
        private String originalFilename;
        private FileType fileType;
        private String publicUrl;
        private Long fileSize;
    }
}
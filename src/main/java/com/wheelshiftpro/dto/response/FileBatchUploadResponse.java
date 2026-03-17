package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for batch file upload operations.
 * Contains information about successfully uploaded files and any failures.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileBatchUploadResponse {

    private Integer totalFiles;
    private Integer successCount;
    private Integer failureCount;
    private List<FileMetadataResponse.Simplified> successfulUploads;
    private List<FileUploadError> failures;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUploadError {
        private String filename;
        private String errorMessage;
    }
}
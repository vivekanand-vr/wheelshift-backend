package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.FileUploadRequest;
import com.wheelshiftpro.dto.response.FileBatchUploadResponse;
import com.wheelshiftpro.dto.response.FileMetadataResponse;
import com.wheelshiftpro.enums.FileType;
import com.wheelshiftpro.service.FileStorageService;
import com.wheelshiftpro.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for file storage operations.
 * Provides endpoints for uploading, downloading, and managing files.
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Storage", description = "Endpoints for file storage and management")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a single file",
            description = "Upload a file and get back the file ID and public URL. Files are automatically segregated by type."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "File uploaded successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or file validation failed"
            )
    })
    public ResponseEntity<ApiResponse<FileMetadataResponse>> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Upload source (e.g., car_images, documents)")
            @RequestParam(required = false) String uploadSource,
            
            @Parameter(description = "User who uploaded the file")
            @RequestParam(required = false) String uploadedBy,
            
            @Parameter(description = "Additional metadata as JSON string")
            @RequestParam(required = false) String additionalMetadata) {

        FileUploadRequest request = FileUploadRequest.builder()
                .uploadSource(uploadSource)
                .uploadedBy(uploadedBy)
                .additionalMetadata(additionalMetadata)
                .build();

        FileMetadataResponse response = fileStorageService.uploadFile(file, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", response));
    }

    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload multiple files in batch",
            description = "Upload multiple files at once. Returns details about successful and failed uploads."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Batch upload completed (may include partial failures)"
            )
    })
    public ResponseEntity<ApiResponse<FileBatchUploadResponse>> uploadFiles(
            @Parameter(description = "Files to upload", required = true)
            @RequestParam("files") MultipartFile[] files,
            
            @Parameter(description = "Upload source (e.g., car_images, documents)")
            @RequestParam(required = false) String uploadSource,
            
            @Parameter(description = "User who uploaded the files")
            @RequestParam(required = false) String uploadedBy,
            
            @Parameter(description = "Additional metadata as JSON string")
            @RequestParam(required = false) String additionalMetadata) {

        FileUploadRequest request = FileUploadRequest.builder()
                .uploadSource(uploadSource)
                .uploadedBy(uploadedBy)
                .additionalMetadata(additionalMetadata)
                .build();

        FileBatchUploadResponse response = fileStorageService.uploadFiles(files, request);

        return ResponseEntity.ok(ApiResponse.success("Batch upload completed", response));
    }

    @GetMapping("/{fileId}")
    @Operation(
            summary = "Download file by ID",
            description = "Download or stream a file using its unique file ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found"
            )
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Unique file identifier", required = true)
            @PathVariable String fileId,
            
            @Parameter(description = "Force download instead of inline display")
            @RequestParam(defaultValue = "false") boolean download) {

        Resource resource = fileStorageService.getFileAsResource(fileId);
        FileMetadataResponse metadata = fileStorageService.getFileMetadata(fileId);

        HttpHeaders headers = new HttpHeaders();
        
        if (download) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + metadata.getOriginalFilename() + "\"");
        } else {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + metadata.getOriginalFilename() + "\"");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                .body(resource);
    }

    @GetMapping("/{fileId}/metadata")
    @Operation(
            summary = "Get file metadata",
            description = "Retrieve metadata information for a file without downloading it"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Metadata retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found"
            )
    })
    public ResponseEntity<ApiResponse<FileMetadataResponse>> getFileMetadata(
            @Parameter(description = "Unique file identifier", required = true)
            @PathVariable String fileId) {

        FileMetadataResponse response = fileStorageService.getFileMetadata(fileId);
        return ResponseEntity.ok(ApiResponse.success("File metadata retrieved", response));
    }

    @GetMapping
    @Operation(
            summary = "Get all files",
            description = "Retrieve all files with pagination"
    )
    public ResponseEntity<ApiResponse<Page<FileMetadataResponse>>> getAllFiles(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {

        Page<FileMetadataResponse> response = fileStorageService.getAllFiles(pageable);
        return ResponseEntity.ok(ApiResponse.success("Files retrieved successfully", response));
    }

    @GetMapping("/type/{fileType}")
    @Operation(
            summary = "Get files by type",
            description = "Retrieve files of a specific type (IMAGE, PDF, EXCEL, CSV, DOCUMENT, OTHER)"
    )
    public ResponseEntity<ApiResponse<Page<FileMetadataResponse>>> getFilesByType(
            @Parameter(description = "Type of files to retrieve", required = true)
            @PathVariable FileType fileType,
            
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {

        Page<FileMetadataResponse> response = fileStorageService.getFilesByType(fileType, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                "Files of type " + fileType + " retrieved successfully", response));
    }

    @GetMapping("/source/{uploadSource}")
    @Operation(
            summary = "Get files by upload source",
            description = "Retrieve files from a specific upload source"
    )
    public ResponseEntity<ApiResponse<Page<FileMetadataResponse>>> getFilesByUploadSource(
            @Parameter(description = "Upload source identifier", required = true)
            @PathVariable String uploadSource,
            
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {

        Page<FileMetadataResponse> response = fileStorageService.getFilesByUploadSource(uploadSource, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                "Files from source " + uploadSource + " retrieved successfully", response));
    }

    @GetMapping("/user/{uploadedBy}")
    @Operation(
            summary = "Get files by user",
            description = "Retrieve files uploaded by a specific user"
    )
    public ResponseEntity<ApiResponse<Page<FileMetadataResponse>>> getFilesByUploadedBy(
            @Parameter(description = "Username or user identifier", required = true)
            @PathVariable String uploadedBy,
            
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {

        Page<FileMetadataResponse> response = fileStorageService.getFilesByUploadedBy(uploadedBy, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                "Files uploaded by " + uploadedBy + " retrieved successfully", response));
    }

    @PostMapping("/batch-metadata")
    @Operation(
            summary = "Get metadata for multiple files",
            description = "Retrieve metadata for multiple files by their IDs"
    )
    public ResponseEntity<ApiResponse<List<FileMetadataResponse>>> getFilesByIds(
            @Parameter(description = "List of file IDs", required = true)
            @RequestBody List<String> fileIds) {

        List<FileMetadataResponse> response = fileStorageService.getFilesByIds(fileIds);
        return ResponseEntity.ok(ApiResponse.success("File metadata retrieved successfully", response));
    }

    @DeleteMapping("/{fileId}/soft")
    @Operation(
            summary = "Soft delete a file",
            description = "Mark a file as deleted without removing it from storage"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File soft deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found"
            )
    })
    public ResponseEntity<ApiResponse<Void>> softDeleteFile(
            @Parameter(description = "Unique file identifier", required = true)
            @PathVariable String fileId) {

        fileStorageService.softDeleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File soft deleted successfully", null));
    }

    @DeleteMapping("/{fileId}/hard")
    @Operation(
            summary = "Hard delete a file",
            description = "Permanently delete a file from storage and database"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File hard deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found"
            )
    })
    public ResponseEntity<ApiResponse<Void>> hardDeleteFile(
            @Parameter(description = "Unique file identifier", required = true)
            @PathVariable String fileId) {

        fileStorageService.hardDeleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File hard deleted successfully", null));
    }

    @PutMapping("/{fileId}/archive")
    @Operation(
            summary = "Archive a file",
            description = "Mark a file as archived"
    )
    public ResponseEntity<ApiResponse<Void>> archiveFile(
            @Parameter(description = "Unique file identifier", required = true)
            @PathVariable String fileId) {

        fileStorageService.archiveFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File archived successfully", null));
    }

    @PutMapping("/{fileId}/restore")
    @Operation(
            summary = "Restore a file",
            description = "Restore a deleted or archived file to active status"
    )
    public ResponseEntity<ApiResponse<Void>> restoreFile(
            @Parameter(description = "Unique file identifier", required = true)
            @PathVariable String fileId) {

        fileStorageService.restoreFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File restored successfully", null));
    }

    @GetMapping("/statistics")
    @Operation(
            summary = "Get storage statistics",
            description = "Retrieve storage usage statistics including counts and sizes by file type"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStorageStatistics() {
        Map<String, Object> stats = fileStorageService.getStorageStatistics();
        return ResponseEntity.ok(ApiResponse.success("Storage statistics retrieved successfully", stats));
    }

    @PostMapping("/cleanup")
    @Operation(
            summary = "Cleanup old deleted files",
            description = "Remove files that have been marked as deleted for more than the specified number of days"
    )
    public ResponseEntity<ApiResponse<Map<String, Integer>>> cleanupOldDeletedFiles(
            @Parameter(description = "Number of days old to consider for cleanup")
            @RequestParam(defaultValue = "30") int daysOld) {

        int cleanedCount = fileStorageService.cleanupOldDeletedFiles(daysOld);
        Map<String, Integer> result = Map.of("filesRemoved", cleanedCount);
        return ResponseEntity.ok(ApiResponse.success(
                "Cleanup completed. " + cleanedCount + " files removed", result));
    }
}
package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.FileUploadRequest;
import com.wheelshiftpro.dto.response.FileBatchUploadResponse;
import com.wheelshiftpro.dto.response.FileMetadataResponse;
import com.wheelshiftpro.enums.FileType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service interface for file storage operations.
 * Provides methods for uploading, retrieving, and managing files.
 */
public interface FileStorageService {

    /**
     * Upload a single file to the storage system.
     *
     * @param file MultipartFile to upload
     * @param request Additional metadata for the file
     * @return FileMetadataResponse containing file details and public URL
     */
    FileMetadataResponse uploadFile(MultipartFile file, FileUploadRequest request);

    /**
     * Upload multiple files in batch.
     *
     * @param files Array of MultipartFiles to upload
     * @param request Additional metadata for the files
     * @return FileBatchUploadResponse containing results of all uploads
     */
    FileBatchUploadResponse uploadFiles(MultipartFile[] files, FileUploadRequest request);

    /**
     * Get file metadata by file ID.
     *
     * @param fileId Unique file identifier
     * @return FileMetadataResponse containing file details
     */
    FileMetadataResponse getFileMetadata(String fileId);

    /**
     * Get file as Resource for download/streaming.
     *
     * @param fileId Unique file identifier
     * @return Resource containing the file
     */
    Resource getFileAsResource(String fileId);

    /**
     * Get all files with pagination.
     *
     * @param pageable Pagination information
     * @return Page of FileMetadataResponse
     */
    Page<FileMetadataResponse> getAllFiles(Pageable pageable);

    /**
     * Get files by file type with pagination.
     *
     * @param fileType Type of file
     * @param pageable Pagination information
     * @return Page of FileMetadataResponse
     */
    Page<FileMetadataResponse> getFilesByType(FileType fileType, Pageable pageable);

    /**
     * Get files by upload source with pagination.
     *
     * @param uploadSource Source of upload
     * @param pageable Pagination information
     * @return Page of FileMetadataResponse
     */
    Page<FileMetadataResponse> getFilesByUploadSource(String uploadSource, Pageable pageable);

    /**
     * Get files uploaded by a specific user with pagination.
     *
     * @param uploadedBy User who uploaded the files
     * @param pageable Pagination information
     * @return Page of FileMetadataResponse
     */
    Page<FileMetadataResponse> getFilesByUploadedBy(String uploadedBy, Pageable pageable);

    /**
     * Get multiple files by their IDs.
     *
     * @param fileIds List of file IDs
     * @return List of FileMetadataResponse
     */
    List<FileMetadataResponse> getFilesByIds(List<String> fileIds);

    /**
     * Soft delete a file (marks as deleted, doesn't remove from storage).
     *
     * @param fileId Unique file identifier
     */
    void softDeleteFile(String fileId);

    /**
     * Hard delete a file (removes from storage and database).
     *
     * @param fileId Unique file identifier
     */
    void hardDeleteFile(String fileId);

    /**
     * Archive a file (marks as archived).
     *
     * @param fileId Unique file identifier
     */
    void archiveFile(String fileId);

    /**
     * Restore a deleted or archived file.
     *
     * @param fileId Unique file identifier
     */
    void restoreFile(String fileId);

    /**
     * Get storage statistics.
     *
     * @return Map containing storage statistics
     */
    Map<String, Object> getStorageStatistics();

    /**
     * Clean up old deleted files (for scheduled jobs).
     *
     * @param daysOld Number of days old to consider for cleanup
     * @return Number of files cleaned up
     */
    int cleanupOldDeletedFiles(int daysOld);

    /**
     * Validate file before upload.
     *
     * @param file MultipartFile to validate
     * @param fileType Expected file type
     * @throws IllegalArgumentException if validation fails
     */
    void validateFile(MultipartFile file, FileType fileType);
}
package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.FileMetadata;
import com.wheelshiftpro.enums.FileStatus;
import com.wheelshiftpro.enums.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FileMetadata entity.
 * Provides data access methods for file metadata operations.
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * Find file metadata by file ID (UUID).
     *
     * @param fileId Unique file identifier
     * @return Optional containing FileMetadata if found
     */
    Optional<FileMetadata> findByFileId(String fileId);

    /**
     * Find active file metadata by file ID.
     *
     * @param fileId Unique file identifier
     * @return Optional containing FileMetadata if found and active
     */
    Optional<FileMetadata> findByFileIdAndStatus(String fileId, FileStatus status);

    /**
     * Find all files by file type with pagination.
     *
     * @param fileType Type of file
     * @param pageable Pagination information
     * @return Page of FileMetadata
     */
    Page<FileMetadata> findByFileType(FileType fileType, Pageable pageable);

    /**
     * Find all active files by file type with pagination.
     *
     * @param fileType Type of file
     * @param status File status
     * @param pageable Pagination information
     * @return Page of FileMetadata
     */
    Page<FileMetadata> findByFileTypeAndStatus(FileType fileType, FileStatus status, Pageable pageable);

    /**
     * Find all files by upload source with pagination.
     *
     * @param uploadSource Source of upload
     * @param pageable Pagination information
     * @return Page of FileMetadata
     */
    Page<FileMetadata> findByUploadSource(String uploadSource, Pageable pageable);

    /**
     * Find all files by status with pagination.
     *
     * @param status File status
     * @param pageable Pagination information
     * @return Page of FileMetadata
     */
    Page<FileMetadata> findByStatus(FileStatus status, Pageable pageable);

    /**
     * Find all files uploaded by a specific user.
     *
     * @param uploadedBy User who uploaded the files
     * @param pageable Pagination information
     * @return Page of FileMetadata
     */
    Page<FileMetadata> findByUploadedBy(String uploadedBy, Pageable pageable);

    /**
     * Find files created within a date range.
     *
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination information
     * @return Page of FileMetadata
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    Page<FileMetadata> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find files by multiple file IDs.
     *
     * @param fileIds List of file IDs
     * @return List of FileMetadata
     */
    List<FileMetadata> findByFileIdIn(List<String> fileIds);

    /**
     * Count total files by file type.
     *
     * @param fileType Type of file
     * @return Count of files
     */
    Long countByFileType(FileType fileType);

    /**
     * Count total active files by file type.
     *
     * @param fileType Type of file
     * @param status File status
     * @return Count of files
     */
    Long countByFileTypeAndStatus(FileType fileType, FileStatus status);

     /**
     * Count total active files by status.
     *
     * @param status File status
     * @return Count of files
     */
     Long countByStatus(FileStatus status);

    /**
     * Calculate total storage used by file type.
     *
     * @param fileType Type of file
     * @return Total size in bytes
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f WHERE f.fileType = :fileType AND f.status = 'ACTIVE'")
    Long calculateTotalStorageByFileType(@Param("fileType") FileType fileType);

    /**
     * Calculate total storage used.
     *
     * @return Total size in bytes
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f WHERE f.status = 'ACTIVE'")
    Long calculateTotalStorage();

    /**
     * Check if file ID exists.
     *
     * @param fileId File ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByFileId(String fileId);

    /**
     * Delete old deleted files (for cleanup jobs).
     *
     * @param deletedBefore Delete files marked as deleted before this date
     * @param status File status
     */
    @Query("DELETE FROM FileMetadata f WHERE f.status = :status AND f.updatedAt < :deletedBefore")
    void deleteOldDeletedFiles(@Param("status") FileStatus status, @Param("deletedBefore") LocalDateTime deletedBefore);
}
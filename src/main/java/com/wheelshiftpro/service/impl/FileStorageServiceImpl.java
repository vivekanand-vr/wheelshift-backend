package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.FileUploadRequest;
import com.wheelshiftpro.dto.response.FileBatchUploadResponse;
import com.wheelshiftpro.dto.response.FileMetadataResponse;
import com.wheelshiftpro.entity.FileMetadata;
import com.wheelshiftpro.enums.FileStatus;
import com.wheelshiftpro.enums.FileType;
import com.wheelshiftpro.exception.FileStorageException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.FileMetadataMapper;
import com.wheelshiftpro.repository.FileMetadataRepository;
import com.wheelshiftpro.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
/**
 * Implementation of FileStorageService.
 * Handles file upload, storage, retrieval, and management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileStorageServiceImpl implements FileStorageService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileMetadataMapper fileMetadataMapper;

    @Value("${file.storage.base-path:uploads}")
    private String baseStoragePath;

    @Value("${file.storage.base-url:/api/v1/files}")
    private String baseUrl;

    /**
     * Initialize storage directory on service startup.
     */
    private Path getStorageLocation() {
        Path location = Paths.get(baseStoragePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(location);
        } catch (IOException e) {
            throw new FileStorageException("Could not create storage directory", e);
        }
        return location;
    }

    @Override
    public FileMetadataResponse uploadFile(MultipartFile file, FileUploadRequest request) {
        log.info("Starting file upload: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }

        // Extract file information
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFilename);
        FileType fileType = FileType.fromExtension(fileExtension);

        // Validate file
        validateFile(file, fileType);

        // Generate unique file ID and stored filename
        String fileId = UUID.randomUUID().toString();
        String storedFilename = generateStoredFilename(fileId, fileExtension);

        // Determine storage path (segregated by file type)
        Path typeDirectory = getStorageLocation().resolve(fileType.getStoragePath());
        try {
            Files.createDirectories(typeDirectory);
        } catch (IOException e) {
            throw new FileStorageException("Could not create directory for file type: " + fileType, e);
        }

        Path targetLocation = typeDirectory.resolve(storedFilename);

        try {
            // Copy file to storage location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored successfully at: {}", targetLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not store file: " + originalFilename, e);
        }

        // Build public URL
        String publicUrl = buildPublicUrl(fileId);

        // Create relative storage path for database
        String relativePath = fileType.getStoragePath() + "/" + storedFilename;

        // Create file metadata
        FileMetadata fileMetadata = FileMetadata.builder()
                .fileId(fileId)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .fileType(fileType)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .fileExtension(fileExtension)
                .storagePath(relativePath)
                .publicUrl(publicUrl)
                .uploadSource(request != null ? request.getUploadSource() : null)
                .uploadedBy(request != null ? request.getUploadedBy() : null)
                .status(FileStatus.ACTIVE)
                .metadataJson(request != null ? request.getAdditionalMetadata() : null)
                .build();

        // Save to database
        FileMetadata saved = fileMetadataRepository.save(fileMetadata);
        log.info("File metadata saved with ID: {}", fileId);

        return fileMetadataMapper.toResponse(saved);
    }

    @Override
    public FileBatchUploadResponse uploadFiles(MultipartFile[] files, FileUploadRequest request) {
        log.info("Starting batch upload of {} files", files.length);

        List<FileMetadataResponse.Simplified> successfulUploads = new ArrayList<>();
        List<FileBatchUploadResponse.FileUploadError> failures = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileMetadataResponse response = uploadFile(file, request);
                successfulUploads.add(fileMetadataMapper.toSimplifiedResponse(
                        fileMetadataRepository.findByFileId(response.getFileId())
                                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", response.getFileId()))
                ));
            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                failures.add(FileBatchUploadResponse.FileUploadError.builder()
                        .filename(file.getOriginalFilename())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        log.info("Batch upload completed. Success: {}, Failures: {}", 
                successfulUploads.size(), failures.size());

        return FileBatchUploadResponse.builder()
                .totalFiles(files.length)
                .successCount(successfulUploads.size())
                .failureCount(failures.size())
                .successfulUploads(successfulUploads)
                .failures(failures)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataResponse getFileMetadata(String fileId) {
        log.debug("Retrieving metadata for file ID: {}", fileId);
        
        FileMetadata fileMetadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", fileId));
        
        return fileMetadataMapper.toResponse(fileMetadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getFileAsResource(String fileId) {
        log.debug("Retrieving file as resource for file ID: {}", fileId);
        
        FileMetadata fileMetadata = fileMetadataRepository.findByFileIdAndStatus(fileId, FileStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", fileId));

        try {
            Path filePath = getStorageLocation().resolve(fileMetadata.getStoragePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                log.debug("File resource found and readable: {}", filePath);
                return resource;
            } else {
                throw new FileStorageException("File not found or not readable: " + fileMetadata.getOriginalFilename());
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("File not found: " + fileMetadata.getOriginalFilename(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileMetadataResponse> getAllFiles(Pageable pageable) {
        log.debug("Retrieving all files with pagination");
        
        return fileMetadataRepository.findAll(pageable)
                .map(fileMetadataMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileMetadataResponse> getFilesByType(FileType fileType, Pageable pageable) {
        log.debug("Retrieving files by type: {}", fileType);
        
        return fileMetadataRepository.findByFileTypeAndStatus(fileType, FileStatus.ACTIVE, pageable)
                .map(fileMetadataMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileMetadataResponse> getFilesByUploadSource(String uploadSource, Pageable pageable) {
        log.debug("Retrieving files by upload source: {}", uploadSource);
        
        return fileMetadataRepository.findByUploadSource(uploadSource, pageable)
                .map(fileMetadataMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileMetadataResponse> getFilesByUploadedBy(String uploadedBy, Pageable pageable) {
        log.debug("Retrieving files uploaded by: {}", uploadedBy);
        
        return fileMetadataRepository.findByUploadedBy(uploadedBy, pageable)
                .map(fileMetadataMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadataResponse> getFilesByIds(List<String> fileIds) {
        log.debug("Retrieving files by IDs: {}", fileIds);
        
        List<FileMetadata> files = fileMetadataRepository.findByFileIdIn(fileIds);
        return fileMetadataMapper.toResponseList(files);
    }

    @Override
    public void softDeleteFile(String fileId) {
        log.info("Soft deleting file: {}", fileId);
        
        FileMetadata fileMetadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", fileId));
        
        fileMetadata.markAsDeleted();
        fileMetadataRepository.save(fileMetadata);
        
        log.info("File soft deleted successfully: {}", fileId);
    }

    @Override
    public void hardDeleteFile(String fileId) {
        log.info("Hard deleting file: {}", fileId);
        
        FileMetadata fileMetadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", fileId));
        
        // Delete physical file
        try {
            Path filePath = getStorageLocation().resolve(fileMetadata.getStoragePath()).normalize();
            Files.deleteIfExists(filePath);
            log.info("Physical file deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete physical file: {}", fileMetadata.getStoragePath(), e);
            throw new FileStorageException("Could not delete file from storage", e);
        }
        
        // Delete from database
        fileMetadataRepository.delete(fileMetadata);
        log.info("File hard deleted successfully: {}", fileId);
    }

    @Override
    public void archiveFile(String fileId) {
        log.info("Archiving file: {}", fileId);
        
        FileMetadata fileMetadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", fileId));
        
        fileMetadata.markAsArchived();
        fileMetadataRepository.save(fileMetadata);
        
        log.info("File archived successfully: {}", fileId);
    }

    @Override
    public void restoreFile(String fileId) {
        log.info("Restoring file: {}", fileId);
        
        FileMetadata fileMetadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", fileId));
        
        fileMetadata.setStatus(FileStatus.ACTIVE);
        fileMetadataRepository.save(fileMetadata);
        
        log.info("File restored successfully: {}", fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStorageStatistics() {
        log.debug("Calculating storage statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total files count
        long totalFiles = fileMetadataRepository.count();
        stats.put("totalFiles", totalFiles);
        
        // Total storage used
        Long totalStorage = fileMetadataRepository.calculateTotalStorage();
        stats.put("totalStorageBytes", totalStorage);
        stats.put("totalStorageMB", totalStorage / (1024.0 * 1024.0));
        stats.put("totalStorageGB", totalStorage / (1024.0 * 1024.0 * 1024.0));
        
        // Statistics by file type
        Map<String, Object> byType = new HashMap<>();
        for (FileType type : FileType.values()) {
            Map<String, Object> typeStats = new HashMap<>();
            Long count = fileMetadataRepository.countByFileTypeAndStatus(type, FileStatus.ACTIVE);
            Long storage = fileMetadataRepository.calculateTotalStorageByFileType(type);
            
            typeStats.put("count", count);
            typeStats.put("storageBytes", storage);
            typeStats.put("storageMB", storage / (1024.0 * 1024.0));
            
            byType.put(type.name(), typeStats);
        }
        stats.put("byFileType", byType);
        
        // Status breakdown
        Map<String, Long> byStatus = new HashMap<>();
        for (FileStatus status : FileStatus.values()) {
            Long count = fileMetadataRepository.countByStatus(status);
            byStatus.put(status.name(), count);
        }
        stats.put("byStatus", byStatus);
        
        return stats;
    }

    @Override
    public int cleanupOldDeletedFiles(int daysOld) {
        log.info("Cleaning up deleted files older than {} days", daysOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        
        // Find old deleted files
        Page<FileMetadata> deletedFiles = fileMetadataRepository.findByStatus(
                FileStatus.DELETED, 
                Pageable.unpaged()
        );
        
        int cleanedCount = 0;
        for (FileMetadata file : deletedFiles) {
            if (file.getUpdatedAt().isBefore(cutoffDate)) {
                try {
                    hardDeleteFile(file.getFileId());
                    cleanedCount++;
                } catch (Exception e) {
                    log.error("Failed to cleanup file: {}", file.getFileId(), e);
                }
            }
        }
        
        log.info("Cleanup completed. Removed {} files", cleanedCount);
        return cleanedCount;
    }

    @Override
    public void validateFile(MultipartFile file, FileType fileType) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Validate file size
        if (file.getSize() > fileType.getMaxSizeBytes()) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %d MB for %s files",
                            fileType.getMaxSizeBytes() / (1024 * 1024),
                            fileType.name())
            );
        }
        
        // Validate file extension
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename is empty");
        }
        
        String extension = getFileExtension(filename);
        if (!fileType.isExtensionAllowed(extension)) {
            throw new IllegalArgumentException(
                    String.format("File extension .%s is not allowed for %s files. Allowed: %s",
                            extension,
                            fileType.name(),
                            fileType.getAllowedExtensions())
            );
        }
        
        // Validate MIME type
        String mimeType = file.getContentType();
        if (mimeType != null && !fileType.isMimeTypeAllowed(mimeType)) {
            throw new IllegalArgumentException(
                    String.format("MIME type %s is not allowed for %s files",
                            mimeType,
                            fileType.name())
            );
        }
    }

    /**
     * Extract file extension from filename.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Generate a unique stored filename with timestamp.
     */
    private String generateStoredFilename(String fileId, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.%s", timestamp, fileId, extension);
    }

    /**
     * Build public URL for accessing the file.
     */
    private String buildPublicUrl(String fileId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(baseUrl)
                .path("/")
                .path(fileId)
                .toUriString();
    }
}
package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.FileStatus;
import com.wheelshiftpro.enums.FileType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entity representing file metadata in the storage system.
 * Stores information about uploaded files including their location,
 * type, size, and access details.
 */
@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "File ID is required")
    @Column(name = "file_id", length = 64, nullable = false, unique = true)
    private String fileId;

    @NotBlank(message = "Original filename is required")
    @Size(max = 255, message = "Original filename must not exceed 255 characters")
    @Column(name = "original_filename", length = 255, nullable = false)
    private String originalFilename;

    @NotBlank(message = "Stored filename is required")
    @Size(max = 255, message = "Stored filename must not exceed 255 characters")
    @Column(name = "stored_filename", length = 255, nullable = false)
    private String storedFilename;

    @NotNull(message = "File type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 20, nullable = false)
    private FileType fileType;

    @NotBlank(message = "MIME type is required")
    @Size(max = 128, message = "MIME type must not exceed 128 characters")
    @Column(name = "mime_type", length = 128, nullable = false)
    private String mimeType;

    @NotNull(message = "File size is required")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank(message = "File extension is required")
    @Size(max = 10, message = "File extension must not exceed 10 characters")
    @Column(name = "file_extension", length = 10, nullable = false)
    private String fileExtension;

    @NotBlank(message = "Storage path is required")
    @Size(max = 512, message = "Storage path must not exceed 512 characters")
    @Column(name = "storage_path", length = 512, nullable = false, unique = true)
    private String storagePath;

    @NotBlank(message = "Public URL is required")
    @Size(max = 512, message = "Public URL must not exceed 512 characters")
    @Column(name = "public_url", length = 512, nullable = false)
    private String publicUrl;

    @Size(max = 64, message = "Upload source must not exceed 64 characters")
    @Column(name = "upload_source", length = 64)
    private String uploadSource;

    @Size(max = 128, message = "Uploaded by must not exceed 128 characters")
    @Column(name = "uploaded_by", length = 128)
    private String uploadedBy;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private FileStatus status = FileStatus.ACTIVE;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;
    /**
     * Helper method to check if file is active
     */
    public boolean isActive() {
        return this.status == FileStatus.ACTIVE;
    }

    /**
     * Helper method to mark file as deleted
     */
    public void markAsDeleted() {
        this.status = FileStatus.DELETED;
    }

    /**
     * Helper method to mark file as archived
     */
    public void markAsArchived() {
        this.status = FileStatus.ARCHIVED;
    }
}
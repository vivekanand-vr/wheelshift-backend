package com.wheelshiftpro.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Enum representing different file types supported by the file storage system.
 * Files are segregated based on these types for organized storage and retrieval.
 */
@Getter
public enum FileType {
    
    IMAGE(
        "Image files",
        Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"),
        Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/svg+xml"),
        10 * 1024 * 1024, // 10 MB
        "images"
    ),
    
    PDF(
        "PDF documents",
        List.of("pdf"),
        List.of("application/pdf"),
        20 * 1024 * 1024, // 20 MB
        "pdfs"
    ),
    
    EXCEL(
        "Excel spreadsheets",
        Arrays.asList("xls", "xlsx", "xlsm", "xlsb"),
        Arrays.asList(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel.sheet.macroEnabled.12",
            "application/vnd.ms-excel.sheet.binary.macroEnabled.12"
        ),
        15 * 1024 * 1024, // 15 MB
        "excel"
    ),
    
    CSV(
        "CSV files",
        List.of("csv"),
        Arrays.asList("text/csv", "application/csv"),
        10 * 1024 * 1024, // 10 MB
        "csv"
    ),
    
    DOCUMENT(
        "Document files",
        Arrays.asList("doc", "docx", "txt", "rtf", "odt"),
        Arrays.asList(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "application/rtf",
            "application/vnd.oasis.opendocument.text"
        ),
        15 * 1024 * 1024, // 15 MB
        "documents"
    ),
    
    OTHER(
        "Other file types",
        List.of("*"),
        List.of("*"),
        10 * 1024 * 1024, // 10 MB
        "other"
    );

    private final String description;
    private final List<String> allowedExtensions;
    private final List<String> allowedMimeTypes;
    private final long maxSizeBytes;
    private final String storagePath; // Subdirectory for this file type

    FileType(String description, List<String> allowedExtensions, 
             List<String> allowedMimeTypes, long maxSizeBytes, String storagePath) {
        this.description = description;
        this.allowedExtensions = allowedExtensions;
        this.allowedMimeTypes = allowedMimeTypes;
        this.maxSizeBytes = maxSizeBytes;
        this.storagePath = storagePath;
    }

    /**
     * Determine file type from file extension.
     *
     * @param extension File extension (without dot)
     * @return Corresponding FileType
     */
    public static FileType fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return OTHER;
        }
        
        String normalizedExt = extension.toLowerCase().replaceAll("^\\.", "");
        
        return Arrays.stream(FileType.values())
                .filter(type -> type != OTHER)
                .filter(type -> type.getAllowedExtensions().contains(normalizedExt))
                .findFirst()
                .orElse(OTHER);
    }

    /**
     * Determine file type from MIME type.
     *
     * @param mimeType MIME type string
     * @return Corresponding FileType
     */
    public static FileType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return OTHER;
        }
        
        String normalizedMime = mimeType.toLowerCase();
        
        return Arrays.stream(FileType.values())
                .filter(type -> type != OTHER)
                .filter(type -> type.getAllowedMimeTypes().stream()
                        .anyMatch(allowed -> normalizedMime.contains(allowed)))
                .findFirst()
                .orElse(OTHER);
    }

    /**
     * Check if file extension is allowed for this file type.
     *
     * @param extension File extension to check
     * @return true if allowed, false otherwise
     */
    public boolean isExtensionAllowed(String extension) {
        if (this == OTHER) {
            return true;
        }
        
        String normalizedExt = extension.toLowerCase().replaceAll("^\\.", "");
        return allowedExtensions.contains(normalizedExt);
    }

    /**
     * Check if MIME type is allowed for this file type.
     *
     * @param mimeType MIME type to check
     * @return true if allowed, false otherwise
     */
    public boolean isMimeTypeAllowed(String mimeType) {
        if (this == OTHER) {
            return true;
        }
        
        String normalizedMime = mimeType.toLowerCase();
        return allowedMimeTypes.stream()
                .anyMatch(allowed -> normalizedMime.contains(allowed));
    }
}
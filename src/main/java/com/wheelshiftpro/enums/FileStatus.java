package com.wheelshiftpro.enums;

/**
 * Enum representing the status of a file in the storage system.
 */
public enum FileStatus {
    
    /**
     * File is active and accessible
     */
    ACTIVE("File is active and accessible"),
    
    /**
     * File has been marked as deleted (soft delete)
     */
    DELETED("File has been marked as deleted"),
    
    /**
     * File has been archived (not actively used but retained)
     */
    ARCHIVED("File has been archived");

    private final String description;

    FileStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
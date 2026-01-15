package com.wheelshiftpro.enums;

import lombok.Getter;

/**
 * Enum representing the status of a motorcycle in the inventory.
 * Similar to CarStatus but for motorcycles.
 */
@Getter
public enum MotorcycleStatus {
    AVAILABLE("Available", "Motorcycle is available for sale"),
    RESERVED("Reserved", "Motorcycle is reserved by a customer"),
    SOLD("Sold", "Motorcycle has been sold"),
    MAINTENANCE("Maintenance", "Motorcycle is under maintenance/repair"),
    INSPECTION_PENDING("Inspection Pending", "Motorcycle is awaiting inspection"),
    ON_HOLD("On Hold", "Motorcycle sale is temporarily on hold"),
    DAMAGED("Damaged", "Motorcycle has damage and needs repair"),
    TRANSFER_PENDING("Transfer Pending", "Ownership transfer is in progress"),
    EXPORTED("Exported", "Motorcycle has been exported");

    private final String displayName;
    private final String description;

    MotorcycleStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Check if the motorcycle is available for sale
     */
    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    /**
     * Check if the motorcycle can be reserved
     */
    public boolean canBeReserved() {
        return this == AVAILABLE;
    }

    /**
     * Check if the motorcycle is sold or reserved
     */
    public boolean isUnavailable() {
        return this == SOLD || this == RESERVED || this == EXPORTED;
    }

    /**
     * Check if the motorcycle needs attention
     */
    public boolean needsAttention() {
        return this == MAINTENANCE || this == INSPECTION_PENDING || this == DAMAGED;
    }
}

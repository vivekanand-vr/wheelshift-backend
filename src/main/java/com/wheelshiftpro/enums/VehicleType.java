package com.wheelshiftpro.enums;

import lombok.Getter;

/**
 * Enum representing the type of vehicle (car or motorcycle).
 * Used to distinguish between different vehicle types in the system.
 */
@Getter
public enum VehicleType {
    CAR("Car", "Four-wheeler vehicle"),
    MOTORCYCLE("Motorcycle", "Two-wheeler vehicle");

    private final String displayName;
    private final String description;

    VehicleType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Check if this is a car
     */
    public boolean isCar() {
        return this == CAR;
    }

    /**
     * Check if this is a motorcycle
     */
    public boolean isMotorcycle() {
        return this == MOTORCYCLE;
    }
}

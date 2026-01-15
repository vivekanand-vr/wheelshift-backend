package com.wheelshiftpro.enums;

import lombok.Getter;

/**
 * Enum representing the type/category of motorcycle.
 * Used to classify motorcycles into different categories for better inventory management.
 */
@Getter
public enum MotorcycleVehicleType {
    MOTORCYCLE("Motorcycle", "Standard motorcycle"),
    SCOOTER("Scooter", "Scooter/scooty with automatic transmission"),
    SPORT_BIKE("Sport Bike", "High-performance racing and sports bikes"),
    CRUISER("Cruiser", "Cruiser motorcycles for long-distance riding"),
    OFF_ROAD("Off-Road", "Adventure and off-road motorcycles"),
    TOURING("Touring", "Touring bikes designed for long journeys"),
    NAKED("Naked", "Naked/streetfighter motorcycles without fairings"),
    CAFE_RACER("Cafe Racer", "Retro-styled cafe racer motorcycles"),
    DIRT_BIKE("Dirt Bike", "Off-road dirt bikes"),
    MOPED("Moped", "Low-powered mopeds");

    private final String displayName;
    private final String description;

    MotorcycleVehicleType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Check if this is a scooter type
     */
    public boolean isScooter() {
        return this == SCOOTER || this == MOPED;
    }

    /**
     * Check if this is a performance motorcycle
     */
    public boolean isPerformanceBike() {
        return this == SPORT_BIKE || this == NAKED;
    }

    /**
     * Check if this is suitable for long-distance riding
     */
    public boolean isLongDistanceBike() {
        return this == TOURING || this == CRUISER;
    }
}

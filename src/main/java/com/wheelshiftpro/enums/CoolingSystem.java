package com.wheelshiftpro.enums;

import lombok.Getter;

/**
 * Enum representing cooling system types for motorcycles.
 */
@Getter
public enum CoolingSystem {
    AIR_COOLED("Air Cooled", "Air cooled engine"),
    LIQUID_COOLED("Liquid Cooled", "Liquid/water cooled engine"),
    OIL_COOLED("Oil Cooled", "Oil cooled engine"),
    AIR_OIL_COOLED("Air-Oil Cooled", "Combination of air and oil cooling");

    private final String displayName;
    private final String description;

    CoolingSystem(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}

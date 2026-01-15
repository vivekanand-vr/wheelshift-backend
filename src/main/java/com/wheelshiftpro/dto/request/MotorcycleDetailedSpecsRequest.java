package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.CoolingSystem;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating or updating motorcycle detailed specifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleDetailedSpecsRequest {

    // Engine Specifications
    private String engineType;
    private BigDecimal maxPowerBhp;
    private BigDecimal maxTorqueNm;
    private CoolingSystem coolingSystem;
    private BigDecimal fuelTankCapacity;
    private BigDecimal claimedMileageKmpl;

    // Dimensions
    @Min(value = 0, message = "Length must be at least 0")
    private Integer lengthMm;

    @Min(value = 0, message = "Width must be at least 0")
    private Integer widthMm;

    @Min(value = 0, message = "Height must be at least 0")
    private Integer heightMm;

    @Min(value = 0, message = "Wheelbase must be at least 0")
    private Integer wheelbaseMm;

    @Min(value = 0, message = "Ground clearance must be at least 0")
    private Integer groundClearanceMm;

    @Min(value = 0, message = "Kerb weight must be at least 0")
    private Integer kerbWeightKg;

    // Braking System
    private String frontBrakeType;
    private String rearBrakeType;
    private Boolean absAvailable;

    // Suspension
    private String frontSuspension;
    private String rearSuspension;

    // Tires
    private String frontTyreSize;
    private String rearTyreSize;

    // Features
    private Boolean hasElectricStart;
    private Boolean hasKickStart;
    private Boolean hasDigitalConsole;
    private Boolean hasUsbCharging;
    private Boolean hasLedLights;
    private String additionalFeatures;
}

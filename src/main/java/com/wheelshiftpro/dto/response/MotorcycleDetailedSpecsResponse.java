package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.CoolingSystem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for motorcycle detailed specifications response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleDetailedSpecsResponse {

    private Long id;
    private Long motorcycleId;
    
    // Engine Specifications
    private String engineType;
    private BigDecimal maxPowerBhp;
    private BigDecimal maxTorqueNm;
    private CoolingSystem coolingSystem;
    private BigDecimal fuelTankCapacity;
    private BigDecimal claimedMileageKmpl;
    
    // Dimensions
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private Integer wheelbaseMm;
    private Integer groundClearanceMm;
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
    
    // Computed fields
    private BigDecimal powerToWeightRatio;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

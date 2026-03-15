package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.CoolingSystem;
import com.wheelshiftpro.enums.MotorcycleStatus;
import com.wheelshiftpro.validation.OnCreate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating or updating a motorcycle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleRequest {

    @NotBlank(groups = OnCreate.class, message = "VIN number is required")
    @Size(min = 17, max = 17, message = "VIN number must be exactly 17 characters")
    private String vinNumber;

    @Size(max = 20, message = "Registration number must not exceed 20 characters")
    private String registrationNumber;

    @Size(max = 50, message = "Engine number must not exceed 50 characters")
    private String engineNumber;

    @Size(max = 50, message = "Chassis number must not exceed 50 characters")
    private String chassisNumber;

    @NotNull(groups = OnCreate.class, message = "Motorcycle model ID is required")
    private Long motorcycleModelId;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @Min(value = 0, message = "Mileage must be at least 0")
    private Integer mileageKm;

    @NotNull(groups = OnCreate.class, message = "Manufacture year is required")
    @Min(value = 1900, message = "Manufacture year must be at least 1900")
    private Integer manufactureYear;

    private LocalDate registrationDate;

    private MotorcycleStatus status;

    private Long storageLocationId;

    @NotNull(groups = OnCreate.class, message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    private BigDecimal purchasePrice;

    @NotNull(groups = OnCreate.class, message = "Purchase date is required")
    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum price must be greater than 0")
    private BigDecimal minimumPrice;

    @Min(value = 0, message = "Previous owners must be at least 0")
    private Integer previousOwners;

    private LocalDate insuranceExpiryDate;

    private LocalDate pollutionCertificateExpiry;

    private Boolean isFinanced;

    private Boolean isAccidental;

    private String description;

    // File IDs for images and documents
    @Size(max = 64, message = "Primary image ID must not exceed 64 characters")
    private String primaryImageId;
    private List<String> galleryImageIds;
    private List<String> documentFileIds;

    // Detailed Specs (merged from MotorcycleDetailedSpecsRequest)
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

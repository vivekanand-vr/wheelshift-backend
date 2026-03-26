package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.CoolingSystem;
import com.wheelshiftpro.enums.MotorcycleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Entity representing a motorcycle in the inventory.
 * Contains all information about a specific motorcycle including VIN, model,
 * pricing, status, etc.
 * 
 * @author WheelShift Pro Development Team
 * @version 1.0
 */
@Entity
@Table(name = "motorcycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Motorcycle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "VIN number is required")
    @Size(min = 17, max = 17, message = "VIN number must be exactly 17 characters")
    @Column(name = "vin_number", nullable = false, unique = true, length = 17)
    private String vinNumber;

    @Size(max = 20, message = "Registration number must not exceed 20 characters")
    @Column(name = "registration_number", unique = true, length = 20)
    private String registrationNumber;

    @Size(max = 50, message = "Engine number must not exceed 50 characters")
    @Column(name = "engine_number", length = 50)
    private String engineNumber;

    @Size(max = 50, message = "Chassis number must not exceed 50 characters")
    @Column(name = "chassis_number", length = 50)
    private String chassisNumber;

    @NotNull(message = "Motorcycle model is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorcycle_model_id", nullable = false, foreignKey = @ForeignKey(name = "fk_motorcycle_model"))
    private MotorcycleModel motorcycleModel;

    @Column(name = "primary_image_id", length = 64)
    private String primaryImageId;

    @Column(name = "gallery_image_ids", columnDefinition = "TEXT")
    private String galleryImageIds;

    @Column(name = "document_file_ids", columnDefinition = "TEXT")
    private String documentFileIds;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    @Column(length = 50)
    private String color;

    @Min(value = 0, message = "Mileage must be at least 0")
    @Column(name = "mileage_km")
    @Builder.Default
    private Integer mileageKm = 0;

    @NotNull(message = "Manufacture year is required")
    @Min(value = 1900, message = "Manufacture year must be at least 1900")
    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MotorcycleStatus status = MotorcycleStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_location_id", foreignKey = @ForeignKey(name = "fk_motorcycle_storage"))
    private StorageLocation storageLocation;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @NotNull(message = "Purchase date is required")
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum price must be greater than 0")
    @Column(name = "minimum_price", precision = 12, scale = 2)
    private BigDecimal minimumPrice;

    @Min(value = 0, message = "Previous owners must be at least 0")
    @Column(name = "previous_owners")
    @Builder.Default
    private Integer previousOwners = 1;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "pollution_certificate_expiry")
    private LocalDate pollutionCertificateExpiry;

    @Column(name = "is_financed")
    @Builder.Default
    private Boolean isFinanced = false;

    @Column(name = "is_accidental")
    @Builder.Default
    private Boolean isAccidental = false;

    @Size(max = 600, message = "Description must not exceed 600 characters")
    @Column(name = "description", length = 600)
    private String description;

    // Detailed Specs (merged from MotorcycleDetailedSpecs)
    // Engine Specifications
    @Column(name = "engine_type", length = 50)
    private String engineType;

    @Column(name = "max_power_bhp", precision = 6, scale = 2)
    private BigDecimal maxPowerBhp;

    @Column(name = "max_torque_nm", precision = 6, scale = 2)
    private BigDecimal maxTorqueNm;

    @Enumerated(EnumType.STRING)
    @Column(name = "cooling_system", length = 30)
    private CoolingSystem coolingSystem;

    @Column(name = "fuel_tank_capacity", precision = 5, scale = 2)
    private BigDecimal fuelTankCapacity;

    @Column(name = "claimed_mileage_kmpl", precision = 5, scale = 2)
    private BigDecimal claimedMileageKmpl;

    // Dimensions
    @Min(value = 0, message = "Length must be at least 0")
    @Column(name = "length_mm")
    private Integer lengthMm;

    @Min(value = 0, message = "Width must be at least 0")
    @Column(name = "width_mm")
    private Integer widthMm;

    @Min(value = 0, message = "Height must be at least 0")
    @Column(name = "height_mm")
    private Integer heightMm;

    @Min(value = 0, message = "Wheelbase must be at least 0")
    @Column(name = "wheelbase_mm")
    private Integer wheelbaseMm;

    @Min(value = 0, message = "Ground clearance must be at least 0")
    @Column(name = "ground_clearance_mm")
    private Integer groundClearanceMm;

    @Min(value = 0, message = "Kerb weight must be at least 0")
    @Column(name = "kerb_weight_kg")
    private Integer kerbWeightKg;

    // Braking System
    @Column(name = "front_brake_type", length = 50)
    private String frontBrakeType;

    @Column(name = "rear_brake_type", length = 50)
    private String rearBrakeType;

    @Column(name = "abs_available")
    @Builder.Default
    private Boolean absAvailable = false;

    // Suspension
    @Column(name = "front_suspension", length = 100)
    private String frontSuspension;

    @Column(name = "rear_suspension", length = 100)
    private String rearSuspension;

    // Tires
    @Column(name = "front_tyre_size", length = 30)
    private String frontTyreSize;

    @Column(name = "rear_tyre_size", length = 30)
    private String rearTyreSize;

    // Features
    @Column(name = "has_electric_start")
    @Builder.Default
    private Boolean hasElectricStart = true;

    @Column(name = "has_kick_start")
    @Builder.Default
    private Boolean hasKickStart = false;

    @Column(name = "has_digital_console")
    @Builder.Default
    private Boolean hasDigitalConsole = false;

    @Column(name = "has_usb_charging")
    @Builder.Default
    private Boolean hasUsbCharging = false;

    @Column(name = "has_led_lights")
    @Builder.Default
    private Boolean hasLedLights = false;

    @Column(name = "additional_features", columnDefinition = "TEXT")
    private String additionalFeatures;

    /**
     * Helper method to check if motorcycle is available for sale
     */
    public boolean isAvailable() {
        return status == MotorcycleStatus.AVAILABLE;
    }

    /**
     * Helper method to check if motorcycle is sold
     */
    public boolean isSold() {
        return status == MotorcycleStatus.SOLD;
    }

    /**
     * Helper method to check if motorcycle is reserved
     */
    public boolean isReserved() {
        return status == MotorcycleStatus.RESERVED;
    }

    /**
     * Helper method to update status to RESERVED
     */
    public void reserve() {
        if (!isAvailable()) {
            throw new IllegalStateException("Cannot reserve motorcycle that is not available");
        }
        this.status = MotorcycleStatus.RESERVED;
    }

    /**
     * Helper method to update status to SOLD
     */
    public void markAsSold() {
        if (isSold()) {
            throw new IllegalStateException("Motorcycle is already sold");
        }
        this.status = MotorcycleStatus.SOLD;
    }

    /**
     * Helper method to update status to AVAILABLE (cancel reservation)
     */
    public void makeAvailable() {
        this.status = MotorcycleStatus.AVAILABLE;
    }

    /**
     * Calculate profit margin if selling price is set
     */
    public BigDecimal calculateProfitMargin() {
        if (sellingPrice == null || purchasePrice == null) {
            return BigDecimal.ZERO;
        }
        return sellingPrice.subtract(purchasePrice);
    }

    /**
     * Check if insurance is expired
     */
    public boolean isInsuranceExpired() {
        if (insuranceExpiryDate == null) {
            return false;
        }
        return insuranceExpiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if pollution certificate is expired
     */
    public boolean isPollutionCertificateExpired() {
        if (pollutionCertificateExpiry == null) {
            return false;
        }
        return pollutionCertificateExpiry.isBefore(LocalDate.now());
    }

    /**
     * Get the age of the motorcycle in years
     */
    public int getAgeInYears() {
        return LocalDate.now().getYear() - manufactureYear;
    }

    /**
     * Get full motorcycle identification (Make Model + Registration/VIN)
     */
    public String getFullIdentification() {
        String modelInfo = motorcycleModel != null ? motorcycleModel.getFullName() : "Unknown Model";
        String identifier = registrationNumber != null ? registrationNumber : vinNumber;
        return modelInfo + " - " + identifier;
    }

    /**
     * Check if motorcycle has ABS
     */
    public boolean hasAbs() {
        return Boolean.TRUE.equals(absAvailable);
    }

    /**
     * Check if motorcycle has modern features (digital console + LED lights)
     */
    public boolean hasModernFeatures() {
        return Boolean.TRUE.equals(hasDigitalConsole) && Boolean.TRUE.equals(hasLedLights);
    }

    /**
     * Check if both disc brakes (front and rear)
     */
    public boolean hasFullDiscBrakes() {
        return frontBrakeType != null && frontBrakeType.toLowerCase().contains("disc") &&
               rearBrakeType != null && rearBrakeType.toLowerCase().contains("disc");
    }

    /**
     * Get power-to-weight ratio (BHP per kg)
     */
    public BigDecimal getPowerToWeightRatio() {
        if (maxPowerBhp == null || kerbWeightKg == null || kerbWeightKg == 0) {
            return BigDecimal.ZERO;
        }
        return maxPowerBhp.divide(BigDecimal.valueOf(kerbWeightKg), 4, RoundingMode.HALF_UP);
    }
}

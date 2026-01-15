package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.CoolingSystem;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity representing detailed specifications of a motorcycle.
 * Contains extended technical specifications, dimensions, features, etc.
 * Has a one-to-one relationship with Motorcycle entity.
 * 
 * @author WheelShift Pro Development Team
 * @version 1.0
 */
@Entity
@Table(name = "motorcycle_detailed_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleDetailedSpecs extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Motorcycle is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorcycle_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_motorcycle_specs"))
    private Motorcycle motorcycle;

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
        return maxPowerBhp.divide(BigDecimal.valueOf(kerbWeightKg), 4, BigDecimal.ROUND_HALF_UP);
    }
}

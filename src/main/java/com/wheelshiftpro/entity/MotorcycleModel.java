package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.MotorcycleVehicleType;
import com.wheelshiftpro.enums.TransmissionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entity representing a motorcycle model catalog.
 * Contains make, model, variant, and basic specifications for motorcycles.
 * 
 * @author WheelShift Pro Development Team
 * @version 1.0
 */
@Entity
@Table(name = "motorcycle_models", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"make", "model", "variant", "year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Make is required")
    @Size(max = 100, message = "Make must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String model;

    @Size(max = 100, message = "Variant must not exceed 100 characters")
    @Column(length = 100)
    private String variant;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    @Column(nullable = false)
    private Integer year;

    @Column(name = "engine_capacity")
    private Integer engineCapacity;

    @NotNull(message = "Fuel type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    @Builder.Default
    private FuelType fuelType = FuelType.PETROL;

    @NotNull(message = "Transmission type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type", nullable = false, length = 20)
    @Builder.Default
    private TransmissionType transmissionType = TransmissionType.MANUAL;

    @NotNull(message = "Vehicle type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 50)
    @Builder.Default
    private MotorcycleVehicleType vehicleType = MotorcycleVehicleType.MOTORCYCLE;

    @Column(name = "seating_capacity")
    @Builder.Default
    private Integer seatingCapacity = 2;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Get the full model name (make + model + variant)
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(make).append(" ").append(model);
        if (variant != null && !variant.isEmpty()) {
            fullName.append(" ").append(variant);
        }
        fullName.append(" (").append(year).append(")");
        return fullName.toString();
    }

    /**
     * Check if this is an electric motorcycle
     */
    public boolean isElectric() {
        return fuelType == FuelType.ELECTRIC;
    }

    /**
     * Check if this model is still active/in production
     */
    public boolean isCurrentlyActive() {
        return Boolean.TRUE.equals(isActive);
    }
}

package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.CarStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a car in the inventory.
 * Core entity managing the complete lifecycle of a vehicle from purchase to sale.
 */
@Entity
@Table(name = "cars",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_vin_number", columnNames = "vin_number"),
           @UniqueConstraint(name = "uk_registration_number", columnNames = "registration_number")
       },
       indexes = {
           @Index(name = "idx_status_year", columnList = "status, year"),
           @Index(name = "idx_storage_location", columnList = "storage_location_id"),
           @Index(name = "idx_car_model", columnList = "car_model_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_model_id", nullable = false, foreignKey = @ForeignKey(name = "fk_car_model"))
    @NotNull(message = "Car model is required")
    private CarModel carModel;

    @NotBlank(message = "VIN number is required")
    @Size(min = 17, max = 17, message = "VIN number must be exactly 17 characters")
    @Column(name = "vin_number", length = 17, nullable = false, unique = true)
    private String vinNumber;

    @Size(max = 32, message = "Registration number must not exceed 32 characters")
    @Column(name = "registration_number", length = 32, unique = true)
    private String registrationNumber;

    @NotNull(message = "Year is required")
    @Min(value = 1980, message = "Year must be at least 1980")
    @Max(value = 2100, message = "Year must not exceed 2100")
    @Column(name = "year", nullable = false)
    private Integer year;

    @Size(max = 32, message = "Color must not exceed 32 characters")
    @Column(name = "color", length = 32)
    private String color;

    @Min(value = 0, message = "Mileage cannot be negative")
    @Column(name = "mileage_km")
    private Integer mileageKm;

    @Min(value = 0, message = "Engine CC cannot be negative")
    @Column(name = "engine_cc")
    private Integer engineCc;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private CarStatus status = CarStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_location_id", foreignKey = @ForeignKey(name = "fk_storage_location"))
    private StorageLocation storageLocation;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative")
    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @OneToOne(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CarDetailedSpecs detailedSpecs;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CarFeature> features = new ArrayList<>();

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CarInspection> inspections = new ArrayList<>();

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FinancialTransaction> transactions = new ArrayList<>();

    @OneToOne(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Reservation reservation;

    @OneToOne(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Sale sale;

    /**
     * Checks if the car can be reserved.
     * @return true if car is available for reservation
     */
    public boolean canBeReserved() {
        return status == CarStatus.AVAILABLE;
    }

    /**
     * Checks if the car can be sold.
     * @return true if car can be sold
     */
    public boolean canBeSold() {
        return status == CarStatus.AVAILABLE || status == CarStatus.RESERVED;
    }
}

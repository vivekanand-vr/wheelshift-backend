package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.MotorcycleStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating or updating a motorcycle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleRequest {

    @NotBlank(message = "VIN number is required")
    @Size(min = 17, max = 17, message = "VIN number must be exactly 17 characters")
    private String vinNumber;

    @Size(max = 20, message = "Registration number must not exceed 20 characters")
    private String registrationNumber;

    @Size(max = 50, message = "Engine number must not exceed 50 characters")
    private String engineNumber;

    @Size(max = 50, message = "Chassis number must not exceed 50 characters")
    private String chassisNumber;

    @NotNull(message = "Motorcycle model ID is required")
    private Long motorcycleModelId;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @Min(value = 0, message = "Mileage must be at least 0")
    private Integer mileageKm;

    @NotNull(message = "Manufacture year is required")
    @Min(value = 1900, message = "Manufacture year must be at least 1900")
    private Integer manufactureYear;

    private LocalDate registrationDate;

    private MotorcycleStatus status;

    private Long storageLocationId;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    private BigDecimal purchasePrice;

    @NotNull(message = "Purchase date is required")
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
}

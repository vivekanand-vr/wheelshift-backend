package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.validation.OnCreate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for creating or updating a car.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarRequest {

    @NotNull(groups = OnCreate.class, message = "Car model ID is required")
    private Long carModelId;

    @NotBlank(groups = OnCreate.class, message = "VIN number is required")
    @Size(min = 17, max = 17, message = "VIN number must be exactly 17 characters")
    private String vinNumber;

    @Size(max = 32, message = "Registration number must not exceed 32 characters")
    private String registrationNumber;

    @NotNull(groups = OnCreate.class, message = "Year is required")
    @Min(value = 1980, message = "Year must be at least 1980")
    @Max(value = 2100, message = "Year must not exceed 2100")
    private Integer year;

    @Size(max = 32, message = "Color must not exceed 32 characters")
    private String color;

    @Min(value = 0, message = "Mileage cannot be negative")
    private Integer mileageKm;

    @Min(value = 0, message = "Engine CC cannot be negative")
    private Integer engineCc;

    private CarStatus status;

    private Long storageLocationId;

    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    // Detailed specs
    private Integer doors;
    private Integer seats;
    private Integer cargoCapacityLiters;
    private BigDecimal acceleration0To100;
    private Integer topSpeedKmh;

    // File IDs for images and documents
    @Size(max = 64, message = "Primary image ID must not exceed 64 characters")
    private String primaryImageId;
    private List<String> galleryImageIds;
    private List<String> documentFileIds;

    // Additional features as key-value pairs
    private Map<String, String> features;
}

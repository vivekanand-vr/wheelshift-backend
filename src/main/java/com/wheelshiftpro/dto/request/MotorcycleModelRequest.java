package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.MotorcycleVehicleType;
import com.wheelshiftpro.enums.TransmissionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a motorcycle model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleModelRequest {

    @NotBlank(message = "Make is required")
    @Size(max = 100, message = "Make must not exceed 100 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Size(max = 100, message = "Variant must not exceed 100 characters")
    private String variant;

    @Size(max = 64, message = "Model image ID must not exceed 64 characters")
    private String modelImageId;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    private Integer year;

    private Integer engineCapacity;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @NotNull(message = "Transmission type is required")
    private TransmissionType transmissionType;

    @NotNull(message = "Vehicle type is required")
    private MotorcycleVehicleType vehicleType;

    private Boolean isActive;
}

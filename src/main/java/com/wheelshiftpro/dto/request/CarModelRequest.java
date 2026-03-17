package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.TransmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a car model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarModelRequest {

    @NotBlank(message = "Make is required")
    @Size(max = 64, message = "Make must not exceed 64 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 64, message = "Model must not exceed 64 characters")
    private String model;

    @NotBlank(message = "Variant is required")
    @Size(max = 64, message = "Variant must not exceed 64 characters")
    private String variant;

    @Size(max = 64, message = "Model image ID must not exceed 64 characters")
    private String modelImageId;

    @Size(max = 32, message = "Emission norm must not exceed 32 characters")
    private String emissionNorm;

    private FuelType fuelType;

    @Size(max = 32, message = "Body type must not exceed 32 characters")
    private String bodyType;

    private Integer gears;

    private TransmissionType transmissionType;
}

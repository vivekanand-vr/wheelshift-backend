package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.MotorcycleVehicleType;
import com.wheelshiftpro.enums.TransmissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for motorcycle model response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleModelResponse {

    private Long id;
    private String make;
    private String model;
    private String variant;
    private Integer year;
    private Integer engineCapacity;
    private FuelType fuelType;
    private TransmissionType transmissionType;
    private MotorcycleVehicleType vehicleType;
    private Integer seatingCapacity;
    private Boolean isActive;
    private String fullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

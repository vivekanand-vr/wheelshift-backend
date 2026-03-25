package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.TransmissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for car model response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarModelResponse {

    private Long id;
    private String make;
    private String model;
    private String variant;
    private String modelImageId;
    private String modelImageUrl;
    private String emissionNorm;
    private FuelType fuelType;
    private String bodyType;
    private Integer gears;
    private TransmissionType transmissionType;
    private java.math.BigDecimal exShowroomPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

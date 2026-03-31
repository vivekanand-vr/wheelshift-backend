package com.wheelshiftpro.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Vehicle details embedded in the AI service similarity response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleDetailsDto(
        String make,
        String model,
        Integer year,
        Double price
) {}

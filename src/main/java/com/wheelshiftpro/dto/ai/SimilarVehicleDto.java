package com.wheelshiftpro.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single similar-vehicle suggestion returned by the AI service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SimilarVehicleDto(
        Long vehicleId,
        Double score,
        String reason,
        VehicleDetailsDto details
) {}

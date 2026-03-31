package com.wheelshiftpro.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Full response envelope returned by the AI service similarity endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SimilarVehiclesResponseDto(
        Long sourceVehicleId,
        String vehicleType,
        List<SimilarVehicleDto> suggestions,
        String method,
        Boolean cached
) {}

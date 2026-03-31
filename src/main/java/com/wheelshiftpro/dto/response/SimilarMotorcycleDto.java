package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.MotorcycleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Compact motorcycle info enriched with an AI similarity score and reason.
 * Returned as part of {@link SimilarMotorcyclesResponse}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarMotorcycleDto {

    private Long id;
    private String make;
    private String model;
    private String variant;
    private Integer year;
    private String color;
    private Integer mileageKm;
    private BigDecimal sellingPrice;
    private MotorcycleStatus status;
    private String storageLocationName;
    private String primaryImageUrl;

    /** AI-computed similarity score in [0, 1]. */
    private Double score;

    /** Human-readable explanation from the AI ranker. */
    private String reason;
}

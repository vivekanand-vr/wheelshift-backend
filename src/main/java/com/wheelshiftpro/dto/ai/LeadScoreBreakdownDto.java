package com.wheelshiftpro.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Per-signal point breakdown returned by the AI lead scoring service.
 * Max total is 100 points across all signals.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LeadScoreBreakdownDto(
        @JsonProperty("purchasing_history")  int purchasingHistory,
        @JsonProperty("inquiry_type")        int inquiryType,
        @JsonProperty("reservation_status")  int reservationStatus,
        @JsonProperty("inquiry_frequency")   int inquiryFrequency,
        @JsonProperty("response_engagement") int responseEngagement,
        @JsonProperty("vehicle_price_band")  int vehiclePriceBand
) {}

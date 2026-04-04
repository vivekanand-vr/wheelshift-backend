package com.wheelshiftpro.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Single lead score result returned by the AI service for one inquiry.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LeadScoreDto(
        Long inquiryId,
        Long clientId,
        Integer score,
        String priority,
        LeadScoreBreakdownDto breakdown,
        boolean cached,
        Instant scoredAt
) {}

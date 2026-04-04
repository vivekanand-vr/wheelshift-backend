package com.wheelshiftpro.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Batch scoring response envelope returned by the AI service.
 * The response always returns 200 — partial results are valid.
 * IDs that were not found in the AI service database appear in {@code failedIds}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LeadScoreBatchResponseDto(
        List<LeadScoreDto> results,
        int totalScored,
        List<Long> failedIds
) {}

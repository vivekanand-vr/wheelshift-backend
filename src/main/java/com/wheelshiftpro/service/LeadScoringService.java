package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.response.LeadScoreBatchResponse;
import com.wheelshiftpro.dto.response.LeadScoreResponse;

import java.util.List;

/**
 * Service interface for AI-powered lead scoring of inquiries.
 * Both methods degrade gracefully when the AI service is unavailable.
 */
public interface LeadScoringService {

    /**
     * Scores a single inquiry and returns an enriched result with full inquiry context.
     * Uses the single-score AI endpoint.
     *
     * @param inquiryId the inquiry to score
     * @return enriched lead score; {@code aiAvailable} is false when the AI service is unreachable
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if the inquiry does not exist
     */
    LeadScoreResponse scoreInquiry(Long inquiryId);

    /**
     * Scores up to 50 inquiries in a single AI service call.
     * Designed for enriching paginated inquiry list/dashboard views.
     * Uses the batch-score AI endpoint.
     *
     * @param inquiryIds list of inquiry IDs (1–50)
     * @return batch result enriched with inquiry context; {@code aiAvailable} is false
     *         when the AI service is unreachable
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if none of the inquiries exist locally
     */
    LeadScoreBatchResponse scoreInquiriesBatch(List<Long> inquiryIds);
}

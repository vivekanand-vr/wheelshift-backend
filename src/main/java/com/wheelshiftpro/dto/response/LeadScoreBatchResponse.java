package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch lead score result enriched with inquiry context.
 * Always returns 200 — partial results are valid when some IDs fail.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadScoreBatchResponse {

    /** Enriched score results for each successfully scored inquiry. */
    private List<LeadScoreResponse> results;

    /** Number of inquiries the AI service successfully scored. */
    private int totalScored;

    /** Inquiry IDs that were sent but could not be scored (not found in AI service DB). */
    private List<Long> failedIds;

    /** False when the AI service was completely unreachable — results will be empty. */
    private boolean aiAvailable;
}

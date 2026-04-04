package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.dto.ai.LeadScoreBreakdownDto;
import com.wheelshiftpro.enums.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Enriched lead score result for a single inquiry.
 * Combines AI-computed score data with inquiry context from the local database.
 * When {@code aiAvailable} is false, score and priority fields will be null.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadScoreResponse {

    private Long inquiryId;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String inquiryType;
    private InquiryStatus inquiryStatus;
    private String assignedEmployeeName;

    /** Conversion likelihood score (0–100). Null when AI service is unavailable. */
    private Integer score;

    /** Priority tier: "Hot", "Warm", or "Cold". Null when AI service is unavailable. */
    private String priority;

    /** Per-signal point breakdown. Null when AI service is unavailable. */
    private LeadScoreBreakdownDto breakdown;

    /** True if the AI response was served from cache. */
    private boolean cached;

    /** Timestamp when the score was computed by the AI service. */
    private Instant scoredAt;

    /** False when the AI service was unreachable — score/priority/breakdown will be null. */
    private boolean aiAvailable;
}

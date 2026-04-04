package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.LeadScoreBatchRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.LeadScoreBatchResponse;
import com.wheelshiftpro.dto.response.LeadScoreResponse;
import com.wheelshiftpro.service.LeadScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lead-scoring")
@RequiredArgsConstructor
@Tag(name = "Lead Scoring", description = "AI-powered lead scoring for customer inquiries")
public class LeadScoringController {

    private final LeadScoringService leadScoringService;

    @GetMapping("/inquiries/{inquiryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SALES','STORE_MANAGER')")
    @Operation(
            summary = "Score a single inquiry",
            description = "Returns an AI-computed lead score (0–100) and priority tier (Hot/Warm/Cold) " +
                    "for the given inquiry, enriched with local inquiry context. " +
                    "Degrades gracefully when the AI service is unavailable (aiAvailable=false)."
    )
    public ResponseEntity<ApiResponse<LeadScoreResponse>> scoreInquiry(
            @Parameter(description = "Inquiry ID to score") @PathVariable Long inquiryId) {
        LeadScoreResponse response = leadScoringService.scoreInquiry(inquiryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/inquiries/batch")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SALES','STORE_MANAGER')")
    @Operation(
            summary = "Score multiple inquiries",
            description = "Scores up to 50 inquiries in a single AI service call. " +
                    "Designed for enriching paginated sales-dashboard inquiry lists. " +
                    "Results are sorted by score descending to surface hot leads first. " +
                    "Partial results are valid — IDs not found in the AI service appear in failedIds. " +
                    "Degrades gracefully when the AI service is unavailable (aiAvailable=false)."
    )
    public ResponseEntity<ApiResponse<LeadScoreBatchResponse>> scoreInquiriesBatch(
            @Valid @RequestBody LeadScoreBatchRequest request) {
        LeadScoreBatchResponse response = leadScoringService.scoreInquiriesBatch(request.getInquiryIds());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

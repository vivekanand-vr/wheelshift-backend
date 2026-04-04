package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.client.AIServiceClient;
import com.wheelshiftpro.dto.ai.LeadScoreBatchResponseDto;
import com.wheelshiftpro.dto.ai.LeadScoreDto;
import com.wheelshiftpro.dto.response.LeadScoreBatchResponse;
import com.wheelshiftpro.dto.response.LeadScoreResponse;
import com.wheelshiftpro.entity.Inquiry;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.InquiryRepository;
import com.wheelshiftpro.service.LeadScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadScoringServiceImpl implements LeadScoringService {

    private final InquiryRepository inquiryRepository;
    private final AIServiceClient aiServiceClient;

    @Override
    @Transactional(readOnly = true)
    public LeadScoreResponse scoreInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", inquiryId));

        Optional<LeadScoreDto> aiResult = aiServiceClient.scoreInquiry(inquiryId);

        if (aiResult.isEmpty()) {
            log.debug("AI lead score unavailable for inquiry {} — returning enriched result with aiAvailable=false", inquiryId);
            return buildUnavailableResponse(inquiry);
        }

        return buildEnrichedResponse(inquiry, aiResult.get());
    }

    @Override
    @Transactional(readOnly = true)
    public LeadScoreBatchResponse scoreInquiriesBatch(List<Long> inquiryIds) {
        List<Inquiry> inquiries = inquiryRepository.findAllById(inquiryIds);

        Map<Long, Inquiry> inquiryMap = inquiries.stream()
                .collect(Collectors.toMap(Inquiry::getId, Function.identity()));

        Optional<LeadScoreBatchResponseDto> aiResult = aiServiceClient.scoreInquiriesBatch(inquiryIds);

        if (aiResult.isEmpty()) {
            log.debug("AI batch lead score unavailable for {} inquiries — returning aiAvailable=false", inquiryIds.size());
            return LeadScoreBatchResponse.builder()
                    .results(Collections.emptyList())
                    .totalScored(0)
                    .failedIds(inquiryIds)
                    .aiAvailable(false)
                    .build();
        }

        LeadScoreBatchResponseDto batchDto = aiResult.get();

        List<LeadScoreResponse> enriched = batchDto.results().stream()
                .filter(dto -> inquiryMap.containsKey(dto.inquiryId()))
                .map(dto -> buildEnrichedResponse(inquiryMap.get(dto.inquiryId()), dto))
                .collect(Collectors.toList());

        // Include inquiries that were found locally but the AI could not score
        List<Long> failedIds = batchDto.failedIds() != null ? batchDto.failedIds() : Collections.emptyList();

        return LeadScoreBatchResponse.builder()
                .results(enriched)
                .totalScored(batchDto.totalScored())
                .failedIds(failedIds)
                .aiAvailable(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private LeadScoreResponse buildEnrichedResponse(Inquiry inquiry, LeadScoreDto dto) {
        return LeadScoreResponse.builder()
                .inquiryId(inquiry.getId())
                .clientId(inquiry.getClient().getId())
                .clientName(inquiry.getClient().getName())
                .clientEmail(inquiry.getClient().getEmail())
                .inquiryType(inquiry.getInquiryType())
                .inquiryStatus(inquiry.getStatus())
                .assignedEmployeeName(resolveEmployeeName(inquiry))
                .score(dto.score())
                .priority(dto.priority())
                .breakdown(dto.breakdown())
                .cached(dto.cached())
                .scoredAt(dto.scoredAt())
                .aiAvailable(true)
                .build();
    }

    private LeadScoreResponse buildUnavailableResponse(Inquiry inquiry) {
        return LeadScoreResponse.builder()
                .inquiryId(inquiry.getId())
                .clientId(inquiry.getClient().getId())
                .clientName(inquiry.getClient().getName())
                .clientEmail(inquiry.getClient().getEmail())
                .inquiryType(inquiry.getInquiryType())
                .inquiryStatus(inquiry.getStatus())
                .assignedEmployeeName(resolveEmployeeName(inquiry))
                .score(null)
                .priority(null)
                .breakdown(null)
                .cached(false)
                .scoredAt(null)
                .aiAvailable(false)
                .build();
    }

    private String resolveEmployeeName(Inquiry inquiry) {
        if (inquiry.getAssignedEmployee() == null) {
            return null;
        }
        return inquiry.getAssignedEmployee().getName();
    }
}

package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response envelope for the similar-motorcycles recommendation endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarMotorcyclesResponse {

    private Long sourceMotorcycleId;
    private List<SimilarMotorcycleDto> similarMotorcycles;

    /** True when the AI service returned results; false when it was unavailable. */
    private boolean similaritiesAvailable;

    /** Ranking method used by the AI service: "hybrid", "content", or "collaborative". */
    private String method;
}

package com.wheelshiftpro.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request body for batch lead scoring — up to 50 inquiry IDs per call.
 */
@Data
public class LeadScoreBatchRequest {

    @NotEmpty(message = "At least one inquiry ID is required")
    @Size(max = 50, message = "Batch scoring supports a maximum of 50 inquiries per request")
    private List<Long> inquiryIds;
}

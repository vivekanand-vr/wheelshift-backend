package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating or updating an inquiry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryRequest {

    private Long carId;
    private Long motorcycleId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long assignedEmployeeId;

    @Size(max = 64, message = "Inquiry type must not exceed 64 characters")
    private String inquiryType;

    private String message;

    private InquiryStatus status;

    private String response;

    private List<String> attachmentFileIds;
}

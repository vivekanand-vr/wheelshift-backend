package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for inquiry response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponse {

    private Long id;
    private Long carId;
    private String carVin;
    private Long motorcycleId;
    private String motorcycleVin;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    private String inquiryType;
    private String message;
    private InquiryStatus status;
    private String response;
    private LocalDateTime responseDate;
    private List<String> attachmentFileIds;
    private List<String> attachmentFileUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

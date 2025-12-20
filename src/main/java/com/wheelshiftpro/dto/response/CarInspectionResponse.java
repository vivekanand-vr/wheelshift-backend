package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for car inspection response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarInspectionResponse {

    private Long id;
    private Long carId;
    private String carVin;
    private LocalDate inspectionDate;
    private String inspectorName;
    private String exteriorCondition;
    private String interiorCondition;
    private String mechanicalCondition;
    private String electricalCondition;
    private String accidentHistory;
    private String requiredRepairs;
    private BigDecimal estimatedRepairCost;
    private Boolean inspectionPass;
    private String reportUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

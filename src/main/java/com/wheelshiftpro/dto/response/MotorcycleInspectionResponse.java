package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for motorcycle inspection response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleInspectionResponse {

    private Long id;
    private Long motorcycleId;
    private String motorcycleVin;
    private LocalDate inspectionDate;
    private Long inspectorId;
    private String inspectorName;
    private String overallCondition;
    private List<String> inspectionImageIds;
    private String inspectionReportFileId;
    private List<String> inspectionImageUrls;
    private String inspectionReportFileUrl;

    // Condition Assessment
    private String engineCondition;
    private String transmissionCondition;
    private String suspensionCondition;
    private String brakeCondition;
    private String tyreCondition;
    private String electricalCondition;
    private String bodyCondition;

    // Issues & Repairs
    private Boolean hasAccidentHistory;
    private Boolean requiresRepair;
    private BigDecimal estimatedRepairCost;
    private String repairNotes;

    private Boolean passed;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

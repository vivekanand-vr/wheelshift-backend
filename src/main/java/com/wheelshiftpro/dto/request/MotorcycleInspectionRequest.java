package com.wheelshiftpro.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating or updating a motorcycle inspection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleInspectionRequest {

    @NotNull(message = "Motorcycle ID is required")
    private Long motorcycleId;

    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;

    private Long inspectorId;

    @NotBlank(message = "Overall condition is required")
    @Size(max = 20, message = "Overall condition must not exceed 20 characters")
    private String overallCondition;

    private List<String> inspectionImageIds;
    private String inspectionReportFileId;

    // Condition Assessment
    @Size(max = 20)
    private String engineCondition;

    @Size(max = 20)
    private String transmissionCondition;

    @Size(max = 20)
    private String suspensionCondition;

    @Size(max = 20)
    private String brakeCondition;

    @Size(max = 20)
    private String tyreCondition;

    @Size(max = 20)
    private String electricalCondition;

    @Size(max = 20)
    private String bodyCondition;

    // Issues & Repairs
    private Boolean hasAccidentHistory;
    private Boolean requiresRepair;

    @DecimalMin(value = "0.0", message = "Estimated repair cost must be at least 0")
    private BigDecimal estimatedRepairCost;

    private String repairNotes;

    private Boolean passed;
    private String notes;
}

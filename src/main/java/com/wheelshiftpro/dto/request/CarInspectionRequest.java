package com.wheelshiftpro.dto.request;

import jakarta.validation.constraints.DecimalMin;
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
 * DTO for creating or updating a car inspection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarInspectionRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;

    @Size(max = 64, message = "Inspector name must not exceed 64 characters")
    private String inspectorName;

    private List<String> inspectionImageIds;
    private String inspectionReportFileId;

    private String exteriorCondition;
    private String interiorCondition;
    private String mechanicalCondition;
    private String electricalCondition;
    private String accidentHistory;
    private String requiredRepairs;

    @DecimalMin(value = "0.0", inclusive = true, message = "Repair cost cannot be negative")
    private BigDecimal estimatedRepairCost;

    private Boolean inspectionPass;

    @Size(max = 512, message = "Report URL must not exceed 512 characters")
    private String reportUrl;
}

package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing an inspection record for a motorcycle.
 * Stores condition assessment, inspection details, and repair requirements.
 * 
 * @author WheelShift Pro Development Team
 * @version 1.0
 */
@Entity
@Table(name = "motorcycle_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleInspection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Motorcycle is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorcycle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_motorcycle_inspection_motorcycle"))
    private Motorcycle motorcycle;

    @NotNull(message = "Inspection date is required")
    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", foreignKey = @ForeignKey(name = "fk_motorcycle_inspection_inspector"))
    private Employee inspector;

    @NotBlank(message = "Overall condition is required")
    @Size(max = 20, message = "Overall condition must not exceed 20 characters")
    @Column(name = "overall_condition", nullable = false, length = 20)
    private String overallCondition;

    @Column(name = "inspection_image_ids", columnDefinition = "TEXT")
    private String inspectionImageIds;

    @Column(name = "inspection_report_file_id", length = 64)
    private String inspectionReportFileId;

    // Condition Assessment
    @Size(max = 20)
    @Column(name = "engine_condition", length = 20)
    private String engineCondition;

    @Size(max = 20)
    @Column(name = "transmission_condition", length = 20)
    private String transmissionCondition;

    @Size(max = 20)
    @Column(name = "suspension_condition", length = 20)
    private String suspensionCondition;

    @Size(max = 20)
    @Column(name = "brake_condition", length = 20)
    private String brakeCondition;

    @Size(max = 20)
    @Column(name = "tyre_condition", length = 20)
    private String tyreCondition;

    @Size(max = 20)
    @Column(name = "electrical_condition", length = 20)
    private String electricalCondition;

    @Size(max = 20)
    @Column(name = "body_condition", length = 20)
    private String bodyCondition;

    // Issues & Repairs
    @Column(name = "has_accident_history")
    @Builder.Default
    private Boolean hasAccidentHistory = false;

    @Column(name = "requires_repair")
    @Builder.Default
    private Boolean requiresRepair = false;

    @DecimalMin(value = "0.0", message = "Estimated repair cost must be at least 0")
    @Column(name = "estimated_repair_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal estimatedRepairCost = BigDecimal.ZERO;

    @Column(name = "repair_notes", columnDefinition = "TEXT")
    private String repairNotes;

    // Documents
    @Size(max = 500)
    @Column(name = "inspection_report_url", length = 500)
    private String inspectionReportUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean passed = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Check if inspection passed
     */
    public boolean hasPassed() {
        return Boolean.TRUE.equals(passed);
    }

    /**
     * Check if motorcycle requires repair
     */
    public boolean needsRepair() {
        return Boolean.TRUE.equals(requiresRepair);
    }

    /**
     * Check if motorcycle has accident history
     */
    public boolean hasAccidents() {
        return Boolean.TRUE.equals(hasAccidentHistory);
    }

    /**
     * Check if overall condition is excellent or good
     */
    public boolean isInGoodCondition() {
        if (overallCondition == null) {
            return false;
        }
        String condition = overallCondition.toUpperCase();
        return condition.equals("EXCELLENT") || condition.equals("GOOD");
    }
}

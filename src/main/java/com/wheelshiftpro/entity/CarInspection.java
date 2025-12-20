package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a vehicle inspection record.
 * Contains detailed inspection results and condition assessments.
 */
@Entity
@Table(name = "car_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarInspection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inspection_car"))
    @NotNull(message = "Car is required")
    private Car car;

    @NotNull(message = "Inspection date is required")
    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Size(max = 64, message = "Inspector name must not exceed 64 characters")
    @Column(name = "inspector_name", length = 64)
    private String inspectorName;

    @Column(name = "exterior_condition", columnDefinition = "TEXT")
    private String exteriorCondition;

    @Column(name = "interior_condition", columnDefinition = "TEXT")
    private String interiorCondition;

    @Column(name = "mechanical_condition", columnDefinition = "TEXT")
    private String mechanicalCondition;

    @Column(name = "electrical_condition", columnDefinition = "TEXT")
    private String electricalCondition;

    @Column(name = "accident_history", columnDefinition = "TEXT")
    private String accidentHistory;

    @Column(name = "required_repairs", columnDefinition = "TEXT")
    private String requiredRepairs;

    @DecimalMin(value = "0.0", inclusive = true, message = "Repair cost cannot be negative")
    @Column(name = "estimated_repair_cost", precision = 12, scale = 2)
    private BigDecimal estimatedRepairCost;

    @Column(name = "inspection_pass")
    private Boolean inspectionPass;

    @Size(max = 512, message = "Report URL must not exceed 512 characters")
    @Column(name = "report_url", length = 512)
    private String reportUrl;
}

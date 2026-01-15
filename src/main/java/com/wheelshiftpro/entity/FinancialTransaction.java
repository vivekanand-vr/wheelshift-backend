package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.TransactionType;
import com.wheelshiftpro.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a financial transaction related to a car.
 * Tracks all monetary activities including purchases, repairs, deposits, sales, and fees.
 */
@Entity
@Table(name = "financial_transactions",
       indexes = {
           @Index(name = "idx_transaction_car_type_date", columnList = "car_id, transaction_type, transaction_date")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", foreignKey = @ForeignKey(name = "fk_transaction_car"))
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorcycle_id", foreignKey = @ForeignKey(name = "fk_transaction_motorcycle"))
    private Motorcycle motorcycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", length = 20)
    @Builder.Default
    private VehicleType vehicleType = VehicleType.CAR;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 128, message = "Vendor name must not exceed 128 characters")
    @Column(name = "vendor_name", length = 128)
    private String vendorName;

    @Size(max = 512, message = "Receipt URL must not exceed 512 characters")
    @Column(name = "receipt_url", length = 512)
    private String receiptUrl;
}

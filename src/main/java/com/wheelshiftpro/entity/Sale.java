package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a completed car sale.
 * Records sale transaction details, commission, and associated documents.
 */
@Entity
@Table(name = "sales",
       uniqueConstraints = @UniqueConstraint(name = "uk_sale_car", columnNames = "car_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_sale_car"))
    @NotNull(message = "Car is required")
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sale_client"))
    @NotNull(message = "Client is required")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sale_employee"))
    @NotNull(message = "Employee is required")
    private Employee employee;

    @NotNull(message = "Sale date is required")
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0")
    @Column(name = "sale_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal salePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Commission rate cannot be negative")
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Total commission cannot be negative")
    @Column(name = "total_commission", precision = 12, scale = 2)
    private BigDecimal totalCommission;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Size(max = 512, message = "Documents URL must not exceed 512 characters")
    @Column(name = "documents_url", length = 512)
    private String documentsUrl;

    /**
     * Calculates and sets the total commission based on sale price and commission rate.
     */
    public void calculateCommission() {
        if (salePrice != null && commissionRate != null) {
            this.totalCommission = salePrice.multiply(commissionRate).divide(new BigDecimal("100"));
        }
    }
}

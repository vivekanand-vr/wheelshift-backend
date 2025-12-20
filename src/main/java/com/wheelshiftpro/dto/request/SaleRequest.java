package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating or updating a sale.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Sale date is required")
    private LocalDateTime saleDate;

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0")
    private BigDecimal salePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Commission rate cannot be negative")
    private BigDecimal commissionRate;

    private PaymentMethod paymentMethod;

    @Size(max = 512, message = "Documents URL must not exceed 512 characters")
    private String documentsUrl;
}

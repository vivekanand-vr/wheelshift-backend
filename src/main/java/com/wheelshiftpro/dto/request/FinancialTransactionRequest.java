package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating a financial transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransactionRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    private Long motorcycleId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

    private String description;

    @Size(max = 128, message = "Vendor name must not exceed 128 characters")
    private String vendorName;

    @Size(max = 512, message = "Receipt URL must not exceed 512 characters")
    private String receiptUrl;

    private List<String> transactionFileIds;
}

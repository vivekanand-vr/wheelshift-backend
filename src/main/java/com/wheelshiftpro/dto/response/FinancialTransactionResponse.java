package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for financial transaction response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransactionResponse {

    private Long id;
    private Long carId;
    private String carVin;
    private TransactionType transactionType;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String description;
    private String vendorName;
    private String receiptUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

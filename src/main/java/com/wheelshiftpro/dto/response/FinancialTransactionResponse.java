package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private Long motorcycleId;
    private String motorcycleVin;
    private TransactionType transactionType;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String description;
    private String vendorName;
    private String receiptUrl;
    private List<String> transactionFileIds;
    private List<String> transactionFileUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

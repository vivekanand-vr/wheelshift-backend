package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for sale response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponse {

    private Long id;
    private Long carId;
    private String carVin;
    private Long motorcycleId;
    private String motorcycleVin;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime saleDate;
    private BigDecimal salePrice;
    private BigDecimal commissionRate;
    private BigDecimal totalCommission;
    private PaymentMethod paymentMethod;
    private String documentsUrl;
    private List<String> saleDocumentIds;
    private List<String> saleDocumentUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

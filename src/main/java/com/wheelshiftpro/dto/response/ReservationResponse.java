package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for reservation response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Long id;
    private Long carId;
    private String carVin;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private LocalDateTime reservationDate;
    private LocalDateTime expiryDate;
    private ReservationStatus status;
    private BigDecimal depositAmount;
    private Boolean depositPaid;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

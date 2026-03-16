package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.validation.VehicleIdRequired;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating or updating a reservation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@VehicleIdRequired
public class ReservationRequest {

    private Long carId;

    private Long motorcycleId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Reservation date is required")
    private LocalDateTime reservationDate;

    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiryDate;

    private ReservationStatus status;

    @DecimalMin(value = "0.0", inclusive = true, message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    private Boolean depositPaid;

    private String notes;

    private List<String> reservationDocumentIds;
}

package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a reservation of a car by a client.
 * Ensures one active reservation per car.
 */
@Entity
@Table(name = "reservations",
       uniqueConstraints = @UniqueConstraint(name = "uk_reservation_car", columnNames = "car_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", unique = true, foreignKey = @ForeignKey(name = "fk_reservation_car"))
    private Car car;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorcycle_id", unique = true, foreignKey = @ForeignKey(name = "fk_reservation_motorcycle"))
    private Motorcycle motorcycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", length = 20)
    @Builder.Default
    private VehicleType vehicleType = VehicleType.CAR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_client"))
    @NotNull(message = "Client is required")
    private Client client;

    @NotNull(message = "Reservation date is required")
    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @NotNull(message = "Expiry date is required")
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @DecimalMin(value = "0.0", inclusive = true, message = "Deposit amount cannot be negative")
    @Column(name = "deposit_amount", precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "deposit_paid")
    @Builder.Default
    private Boolean depositPaid = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Checks if the reservation has expired.
     * @return true if current time is past expiry date
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate) && status != ReservationStatus.EXPIRED;
    }

    /**
     * Checks if the reservation can be converted to sale.
     * @return true if reservation is confirmed
     */
    public boolean canConvertToSale() {
        return status == ReservationStatus.CONFIRMED && depositPaid;
    }
}

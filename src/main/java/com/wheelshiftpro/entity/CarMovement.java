package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing the movement of a car between storage locations.
 * Maintains audit trail of vehicle movements.
 */
@Entity
@Table(name = "car_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movement_car"))
    @NotNull(message = "Car is required")
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id", foreignKey = @ForeignKey(name = "fk_from_location"))
    private StorageLocation fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_to_location"))
    @NotNull(message = "Destination location is required")
    private StorageLocation toLocation;

    @NotNull(message = "Movement date is required")
    @Column(name = "moved_at", nullable = false)
    private LocalDateTime movedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moved_by_employee_id", foreignKey = @ForeignKey(name = "fk_moved_by_employee"))
    private Employee movedBy;
}

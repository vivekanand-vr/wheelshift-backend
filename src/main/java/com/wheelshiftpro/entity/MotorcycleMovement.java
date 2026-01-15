package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing the movement of a motorcycle between storage locations.
 * Maintains audit trail of motorcycle movements.
 */
@Entity
@Table(name = "motorcycle_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorcycleMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorcycle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movement_motorcycle"))
    @NotNull(message = "Motorcycle is required")
    private Motorcycle motorcycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id", foreignKey = @ForeignKey(name = "fk_motorcycle_from_location"))
    private StorageLocation fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_motorcycle_to_location"))
    @NotNull(message = "Destination location is required")
    private StorageLocation toLocation;

    @NotNull(message = "Movement date is required")
    @Column(name = "moved_at", nullable = false)
    private LocalDateTime movedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moved_by_employee_id", foreignKey = @ForeignKey(name = "fk_motorcycle_moved_by_employee"))
    private Employee movedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

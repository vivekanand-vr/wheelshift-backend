package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a calendar event.
 * Tracks important business dates including inspections, reservations, and custom events.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Type is required")
    @Size(max = 64, message = "Type must not exceed 64 characters")
    @Column(name = "type", length = 64, nullable = false)
    private String type;

    @Size(max = 128, message = "Name must not exceed 128 characters")
    @Column(name = "name", length = 128)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", foreignKey = @ForeignKey(name = "fk_event_car"))
    private Car car;

    @Size(max = 128, message = "Title must not exceed 128 characters")
    @Column(name = "title", length = 128)
    private String title;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;
}

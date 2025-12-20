package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity representing detailed specifications of a car.
 * One-to-one relationship with Car entity.
 */
@Entity
@Table(name = "car_detailed_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarDetailedSpecs extends BaseEntity {

    @Id
    @Column(name = "car_id")
    private Long carId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "car_id", foreignKey = @ForeignKey(name = "fk_car_specs"))
    private Car car;

    @Min(value = 0, message = "Doors cannot be negative")
    @Column(name = "doors")
    private Integer doors;

    @Min(value = 0, message = "Seats cannot be negative")
    @Column(name = "seats")
    private Integer seats;

    @Min(value = 0, message = "Cargo capacity cannot be negative")
    @Column(name = "cargo_capacity_liters")
    private Integer cargoCapacityLiters;

    @Column(name = "acceleration_0_100", precision = 5, scale = 2)
    private BigDecimal acceleration0To100;

    @Min(value = 0, message = "Top speed cannot be negative")
    @Column(name = "top_speed_kmh")
    private Integer topSpeedKmh;
}

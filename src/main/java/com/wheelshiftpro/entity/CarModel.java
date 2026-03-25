package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.FuelType;
import com.wheelshiftpro.enums.TransmissionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a car model with specifications.
 * Contains make, model, variant, and technical specifications.
 */
@Entity
@Table(name = "car_models", uniqueConstraints = @UniqueConstraint(columnNames = { "make", "model",
        "variant" }), indexes = {
                @Index(name = "idx_fuel_type", columnList = "fuel_type"),
                @Index(name = "idx_body_type", columnList = "body_type")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Make is required")
    @Size(max = 64, message = "Make must not exceed 64 characters")
    @Column(name = "make", length = 64, nullable = false)
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 64, message = "Model must not exceed 64 characters")
    @Column(name = "model", length = 64, nullable = false)
    private String model;

    @Column(name = "model_image_id", length = 64)
    private String modelImageId;

    @NotBlank(message = "Variant is required")
    @Size(max = 64, message = "Variant must not exceed 64 characters")
    @Column(name = "variant", length = 64, nullable = false)
    private String variant;

    @Size(max = 32, message = "Emission norm must not exceed 32 characters")
    @Column(name = "emission_norm", length = 32)
    private String emissionNorm;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private FuelType fuelType;

    @Size(max = 32, message = "Body type must not exceed 32 characters")
    @Column(name = "body_type", length = 32)
    private String bodyType;

    @Column(name = "gears")
    private Integer gears;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type", length = 20)
    private TransmissionType transmissionType;

    @Column(name = "ex_showroom_price", precision = 12, scale = 2)
    private BigDecimal exShowroomPrice;

    @OneToMany(mappedBy = "carModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Car> cars = new ArrayList<>();
}

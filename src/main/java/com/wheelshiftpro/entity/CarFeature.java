package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entity representing additional features of a car as key-value pairs.
 * Allows dynamic feature attributes without schema changes.
 */
@Entity
@Table(name = "car_features",
       uniqueConstraints = @UniqueConstraint(name = "uk_car_feature", columnNames = {"car_id", "feature_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false, foreignKey = @ForeignKey(name = "fk_car_feature"))
    @NotNull(message = "Car is required")
    private Car car;

    @NotBlank(message = "Feature name is required")
    @Size(max = 64, message = "Feature name must not exceed 64 characters")
    @Column(name = "feature_name", length = 64, nullable = false)
    private String featureName;

    @Size(max = 128, message = "Feature value must not exceed 128 characters")
    @Column(name = "feature_value", length = 128)
    private String featureValue;
}

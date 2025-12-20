package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a physical storage location for vehicles.
 * Manages capacity tracking and vehicle assignments.
 */
@Entity
@Table(name = "storage_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Location name is required")
    @Size(max = 128, message = "Name must not exceed 128 characters")
    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 256, message = "Address must not exceed 256 characters")
    @Column(name = "address", length = 256, nullable = false)
    private String address;

    @Size(max = 64, message = "Contact person must not exceed 64 characters")
    @Column(name = "contact_person", length = 64)
    private String contactPerson;

    @Size(max = 32, message = "Contact number must not exceed 32 characters")
    @Column(name = "contact_number", length = 32)
    private String contactNumber;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Total capacity must be at least 1")
    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity;

    @NotNull(message = "Current vehicle count is required")
    @Min(value = 0, message = "Current vehicle count cannot be negative")
    @Column(name = "current_vehicle_count", nullable = false)
    @Builder.Default
    private Integer currentVehicleCount = 0;

    @OneToMany(mappedBy = "storageLocation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Car> cars = new ArrayList<>();

    /**
     * Checks if the location has available capacity.
     * @return true if there is space available, false otherwise
     */
    public boolean hasCapacity() {
        return currentVehicleCount < totalCapacity;
    }

    /**
     * Gets the available capacity.
     * @return number of available slots
     */
    public Integer getAvailableCapacity() {
        return totalCapacity - currentVehicleCount;
    }
}

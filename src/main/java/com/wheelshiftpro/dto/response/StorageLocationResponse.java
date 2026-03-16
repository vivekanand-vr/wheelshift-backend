package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for storage location response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageLocationResponse {

    private Long id;
    private String name;
    private String address;
    private String locationImageId;
    private String locationImageUrl;
    private String contactPerson;
    private String contactNumber;
    private Integer totalCapacity;
    private Integer currentVehicleCount;
    private Integer availableCapacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

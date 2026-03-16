package com.wheelshiftpro.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a storage location.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageLocationRequest {

    @NotBlank(message = "Location name is required")
    @Size(max = 128, message = "Name must not exceed 128 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 256, message = "Address must not exceed 256 characters")
    private String address;

    private String locationImageId;

    @Size(max = 64, message = "Contact person must not exceed 64 characters")
    private String contactPerson;

    @Size(max = 32, message = "Contact number must not exceed 32 characters")
    private String contactNumber;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Total capacity must be at least 1")
    private Integer totalCapacity;
}

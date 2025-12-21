package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.RoleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotNull(message = "Role type is required")
    private RoleType name;

    @Size(max = 256, message = "Description must not exceed 256 characters")
    private String description;

    private Boolean isSystem;
}

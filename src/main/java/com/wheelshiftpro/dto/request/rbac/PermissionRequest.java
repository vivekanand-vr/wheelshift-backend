package com.wheelshiftpro.dto.request.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a permission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {

    @NotBlank(message = "Resource is required")
    @Size(max = 64, message = "Resource must not exceed 64 characters")
    private String resource;

    @NotBlank(message = "Action is required")
    @Size(max = 32, message = "Action must not exceed 32 characters")
    private String action;

    @Size(max = 256, message = "Description must not exceed 256 characters")
    private String description;
}

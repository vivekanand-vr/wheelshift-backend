package com.wheelshiftpro.dto.request.rbac;

import com.wheelshiftpro.enums.rbac.ScopeEffect;
import com.wheelshiftpro.enums.rbac.ScopeType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating an employee data scope
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeRequest {

    @NotNull(message = "Scope type is required")
    private ScopeType scopeType;

    @NotBlank(message = "Scope value is required")
    @Size(max = 128, message = "Scope value must not exceed 128 characters")
    private String scopeValue;

    @NotNull(message = "Effect is required")
    @Builder.Default
    private ScopeEffect effect = ScopeEffect.INCLUDE;

    @Size(max = 512, message = "Description must not exceed 512 characters")
    private String description;
}

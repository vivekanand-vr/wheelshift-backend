package com.wheelshiftpro.dto.response.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.wheelshiftpro.enums.rbac.ScopeEffect;
import com.wheelshiftpro.enums.rbac.ScopeType;

/**
 * DTO for data scope response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeResponse {

    private Long id;
    private Long employeeId;
    private ScopeType scopeType;
    private String scopeValue;
    private ScopeEffect effect;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

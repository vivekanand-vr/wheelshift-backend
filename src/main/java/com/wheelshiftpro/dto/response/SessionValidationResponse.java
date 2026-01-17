package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for session validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionValidationResponse {
    private boolean valid;
    private boolean expired;
    private String message;
    private String errorCode;
    private Long employeeId;
    private String email;
}
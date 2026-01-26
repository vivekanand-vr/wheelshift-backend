package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import com.wheelshiftpro.enums.rbac.RoleType;

/**
 * DTO for authentication response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long employeeId;
    private String email;
    private String name;
    private Set<RoleType> roles;
    private Set<String> permissions;
    private String message;

    /**
     * JWT access token
     */
    private String accessToken;
    private String tokenType;
}

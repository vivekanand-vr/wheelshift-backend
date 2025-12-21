package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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
     * For future JWT implementation
     */
    private String token;
    private String tokenType;
}

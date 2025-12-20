package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 128, message = "Name must not exceed 128 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 128, message = "Email must not exceed 128 characters")
    private String email;

    @Size(max = 32, message = "Phone must not exceed 32 characters")
    private String phone;

    @Size(max = 128, message = "Location must not exceed 128 characters")
    private String location;

    private ClientStatus status;
}

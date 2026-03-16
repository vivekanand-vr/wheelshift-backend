package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating or updating an employee.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 128, message = "Name must not exceed 128 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 128, message = "Email must not exceed 128 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 32, message = "Phone must not exceed 32 characters")
    private String phone;

    private String profileImageId;

    @Size(max = 64, message = "Position must not exceed 64 characters")
    private String position;

    @Size(max = 64, message = "Department must not exceed 64 characters")
    private String department;

    private LocalDate joinDate;

    private EmployeeStatus status;
}

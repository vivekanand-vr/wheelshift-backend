package com.wheelshiftpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating or updating an event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

    @NotBlank(message = "Type is required")
    @Size(max = 64, message = "Type must not exceed 64 characters")
    private String type;

    @Size(max = 128, message = "Name must not exceed 128 characters")
    private String name;

    private Long carId;

    @Size(max = 128, message = "Title must not exceed 128 characters")
    private String title;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;
}

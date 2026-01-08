package com.wheelshiftpro.dto.request.notifications;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkNotificationReadRequest {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    @Builder.Default
    private Boolean read = true;
}

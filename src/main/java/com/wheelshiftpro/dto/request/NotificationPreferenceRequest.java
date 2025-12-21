package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.NotificationChannel;
import com.wheelshiftpro.enums.NotificationFrequency;
import com.wheelshiftpro.enums.NotificationSeverity;
import com.wheelshiftpro.enums.PrincipalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {
    
    @NotNull(message = "Principal type is required")
    private PrincipalType principalType;
    
    private Long principalId;
    
    @NotBlank(message = "Event type is required")
    private String eventType;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    
    @Builder.Default
    private Boolean enabled = true;
    
    @Builder.Default
    private NotificationFrequency frequency = NotificationFrequency.IMMEDIATE;
    
    private LocalTime quietHoursStart;
    
    private LocalTime quietHoursEnd;
    
    @Builder.Default
    private NotificationSeverity severityThreshold = NotificationSeverity.INFO;
}

package com.wheelshiftpro.dto.request.notifications;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

import com.wheelshiftpro.enums.notifications.NotificationSeverity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationEventRequest {
    
    @NotBlank(message = "Event type is required")
    private String eventType;
    
    @NotBlank(message = "Entity type is required")
    private String entityType;
    
    @NotNull(message = "Entity ID is required")
    private Long entityId;
    
    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;
    
    @Builder.Default
    private NotificationSeverity severity = NotificationSeverity.INFO;
    
    @NotNull(message = "Occurred at timestamp is required")
    private LocalDateTime occurredAt;
}

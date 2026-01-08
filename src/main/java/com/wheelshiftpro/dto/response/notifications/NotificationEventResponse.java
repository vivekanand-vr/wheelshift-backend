package com.wheelshiftpro.dto.response.notifications;

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
public class NotificationEventResponse {
    
    private Long id;
    private String eventType;
    private String entityType;
    private Long entityId;
    private Map<String, Object> payload;
    private NotificationSeverity severity;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}

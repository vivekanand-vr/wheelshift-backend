package com.wheelshiftpro.dto.response.notifications;

import com.wheelshiftpro.enums.PrincipalType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.notifications.NotificationFrequency;
import com.wheelshiftpro.enums.notifications.NotificationSeverity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {
    
    private Long id;
    private PrincipalType principalType;
    private Long principalId;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private Boolean enabled;
    private NotificationFrequency frequency;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private NotificationSeverity severityThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

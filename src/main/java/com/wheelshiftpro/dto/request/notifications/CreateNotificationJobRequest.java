package com.wheelshiftpro.dto.request.notifications;

import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationJobRequest {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotNull(message = "Recipient type is required")
    private RecipientType recipientType;
    
    private Long recipientId;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    
    private LocalDateTime scheduledFor;
}

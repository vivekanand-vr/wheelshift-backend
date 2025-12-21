package com.wheelshiftpro.dto.response;

import com.wheelshiftpro.enums.NotificationChannel;
import com.wheelshiftpro.enums.NotificationStatus;
import com.wheelshiftpro.enums.RecipientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationJobResponse {
    
    private Long id;
    private Long eventId;
    private RecipientType recipientType;
    private Long recipientId;
    private NotificationChannel channel;
    private NotificationStatus status;
    private LocalDateTime scheduledFor;
    private String dedupKey;
    private Integer retries;
    private String lastError;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enriched fields for IN_APP notifications
    private String title;
    private String message;
    private String eventType;
    private String entityType;
    private Long entityId;
}

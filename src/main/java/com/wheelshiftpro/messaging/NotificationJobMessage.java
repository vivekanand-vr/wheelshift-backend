package com.wheelshiftpro.messaging;

import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.notifications.NotificationSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka message payload for a notification job.
 * Carries enough data for the consumer to push via SSE without a DB round-trip.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationJobMessage implements Serializable {

    private Long jobId;
    private Long eventId;
    private NotificationEventType eventType;
    private String entityType;
    private Long entityId;

    private RecipientType recipientType;
    private Long recipientId;
    private NotificationChannel channel;
    private NotificationSeverity severity;

    /** Rendered title (filled by producer after template lookup, or empty if no template). */
    private String title;

    /** Rendered message body. */
    private String message;

    /** Raw event payload variables (used by the SSE consumer for client-side rendering). */
    private Map<String, Object> payload;

    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}

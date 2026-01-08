package com.wheelshiftpro.service.notifications;

import com.wheelshiftpro.dto.request.notifications.CreateNotificationEventRequest;
import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationSeverity;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for creating notification events
 */
@Component
public class NotificationEventHelper {
    
    private final NotificationService notificationService;
    
    public NotificationEventHelper(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Create and send a notification event
     */
    public void sendNotification(String eventType, String entityType, Long entityId,
                                 RecipientType recipientType, Long recipientId,
                                 Map<String, Object> additionalPayload) {
        Map<String, Object> payload = new HashMap<>(additionalPayload != null ? additionalPayload : new HashMap<>());
        payload.put("recipientType", recipientType.name());
        payload.put("recipientId", recipientId);
        
        CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .payload(payload)
                .severity(NotificationSeverity.INFO)
                .occurredAt(LocalDateTime.now())
                .build();
        
        notificationService.createNotificationEvent(request);
    }
    
    /**
     * Send notification to employee
     */
    public void notifyEmployee(Long employeeId, String eventType, String entityType, 
                               Long entityId, Map<String, Object> data) {
        sendNotification(eventType, entityType, entityId, RecipientType.EMPLOYEE, employeeId, data);
    }
    
    /**
     * Send notification to client
     */
    public void notifyClient(Long clientId, String eventType, String entityType, 
                            Long entityId, Map<String, Object> data) {
        sendNotification(eventType, entityType, entityId, RecipientType.CLIENT, clientId, data);
    }
    
    /**
     * Send critical notification
     */
    public void sendCriticalNotification(String eventType, String entityType, Long entityId,
                                        RecipientType recipientType, Long recipientId,
                                        Map<String, Object> additionalPayload) {
        Map<String, Object> payload = new HashMap<>(additionalPayload != null ? additionalPayload : new HashMap<>());
        payload.put("recipientType", recipientType.name());
        payload.put("recipientId", recipientId);
        
        CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .payload(payload)
                .severity(NotificationSeverity.CRITICAL)
                .occurredAt(LocalDateTime.now())
                .build();
        
        notificationService.createNotificationEvent(request);
    }
}

# WheelShift Notifications System - Implementation Guide

## Overview
This document provides implementation details for the WheelShift Notifications System. The system supports IN_APP notifications (with future support for EMAIL, SMS, WhatsApp, PUSH, and Webhooks).

## Architecture

### Components
1. **NotificationEvent** - The raw event that triggers notifications
2. **NotificationJob** - Individual notification delivery jobs
3. **NotificationTemplate** - Reusable templates with variable substitution
4. **NotificationPreference** - User preferences for notification delivery
5. **NotificationDelivery** - Tracks actual delivery attempts (for future use)

## Database Schema
The system uses 7 tables:
- `notification_events` - Stores notification events
- `notification_jobs` - Individual notification jobs
- `notification_deliveries` - Delivery tracking
- `notification_templates` - Message templates
- `notification_preferences` - User preferences
- `notification_providers` - External provider configurations (future)
- `notification_digests` - Aggregated notifications (future)

Migration file: `V6__Add_Notifications_Tables.sql`

## API Endpoints

### Notifications
- `POST /api/v1/notifications/events` - Create a notification event
- `POST /api/v1/notifications/jobs` - Create a notification job
- `GET /api/v1/notifications/recipient/{recipientType}/{recipientId}` - Get notifications for recipient
- `GET /api/v1/notifications/{id}` - Get notification by ID
- `PUT /api/v1/notifications/{id}/read` - Mark notification as read
- `PUT /api/v1/notifications/recipient/{recipientType}/{recipientId}/read-all` - Mark all as read
- `GET /api/v1/notifications/recipient/{recipientType}/{recipientId}/stats` - Get notification stats
- `GET /api/v1/notifications/recipient/{recipientType}/{recipientId}/unread-count` - Get unread count
- `GET /api/v1/notifications/events` - Get all events

### Preferences
- `POST /api/v1/notifications/preferences` - Create/update preference
- `GET /api/v1/notifications/preferences/{id}` - Get preference by ID
- `GET /api/v1/notifications/preferences/principal/{principalType}/{principalId}` - Get preferences for principal
- `GET /api/v1/notifications/preferences` - Get all preferences
- `DELETE /api/v1/notifications/preferences/{id}` - Delete preference

### Templates
- `POST /api/v1/notifications/templates` - Create template
- `GET /api/v1/notifications/templates/{id}` - Get template by ID
- `GET /api/v1/notifications/templates/latest` - Get latest template version
- `GET /api/v1/notifications/templates` - Get all templates
- `GET /api/v1/notifications/templates/channel/{channel}` - Get templates by channel
- `PUT /api/v1/notifications/templates/{id}` - Update template
- `DELETE /api/v1/notifications/templates/{id}` - Delete template

## Usage Examples

### 1. Sending a Notification (Manual)

```java
@Autowired
private NotificationService notificationService;

// Create event
CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
    .eventType("inquiry.assigned")
    .entityType("INQUIRY")
    .entityId(inquiryId)
    .payload(Map.of(
        "recipientType", "EMPLOYEE",
        "recipientId", employeeId,
        "inquiryId", inquiryId,
        "assignedBy", assignerName,
        "clientName", clientName
    ))
    .severity(NotificationSeverity.INFO)
    .occurredAt(LocalDateTime.now())
    .build();

notificationService.createNotificationEvent(request);
```

### 2. Sending a Notification (Using Helper)

```java
@Autowired
private NotificationEventHelper notificationHelper;

// Simple employee notification
Map<String, Object> data = new HashMap<>();
data.put("inquiryId", inquiryId);
data.put("assignedBy", assignerName);
data.put("clientName", clientName);

notificationHelper.notifyEmployee(
    employeeId, 
    "inquiry.assigned", 
    "INQUIRY", 
    inquiryId, 
    data
);
```

### 3. Getting Notifications for a User

```java
// Get notifications for an employee
Page<NotificationJobResponse> notifications = notificationService.getNotificationsForRecipient(
    RecipientType.EMPLOYEE,
    employeeId,
    NotificationChannel.IN_APP,
    PageRequest.of(0, 20)
);
```

### 4. Marking Notifications as Read

```java
// Mark single notification as read
notificationService.markNotificationAsRead(jobId);

// Mark all notifications as read
notificationService.markAllNotificationsAsRead(
    RecipientType.EMPLOYEE,
    employeeId,
    NotificationChannel.IN_APP
);
```

### 5. Creating a Notification Template

```java
NotificationTemplateRequest templateRequest = NotificationTemplateRequest.builder()
    .name("inquiry.assigned")
    .channel(NotificationChannel.IN_APP)
    .locale("en")
    .subject("New Inquiry Assigned")
    .content("A new inquiry (#{{inquiryId}}) has been assigned to you by {{assignedBy}}. Client: {{clientName}}")
    .variables(List.of("inquiryId", "assignedBy", "clientName"))
    .build();

notificationTemplateService.createTemplate(templateRequest, createdByEmployeeId);
```

### 6. Managing User Preferences

```java
// Create preference
NotificationPreferenceRequest preferenceRequest = NotificationPreferenceRequest.builder()
    .principalType(PrincipalType.EMPLOYEE)
    .principalId(employeeId)
    .eventType("inquiry.assigned")
    .channel(NotificationChannel.IN_APP)
    .enabled(true)
    .frequency(NotificationFrequency.IMMEDIATE)
    .build();

notificationPreferenceService.createOrUpdatePreference(preferenceRequest);
```

## Integration Examples

### Example: Inquiry Assigned Notification

```java
@Service
public class InquiryService {
    
    @Autowired
    private NotificationEventHelper notificationHelper;
    
    public void assignInquiry(Long inquiryId, Long employeeId, String assignerName) {
        // ... assign inquiry logic ...
        
        // Send notification
        Map<String, Object> data = new HashMap<>();
        data.put("inquiryId", inquiryId);
        data.put("assignedBy", assignerName);
        data.put("clientName", inquiry.getClient().getName());
        
        notificationHelper.notifyEmployee(
            employeeId,
            "inquiry.assigned",
            "INQUIRY",
            inquiryId,
            data
        );
    }
}
```

### Example: Reservation Expiring Soon

```java
@Service
public class ReservationExpiryScheduler {
    
    @Autowired
    private NotificationEventHelper notificationHelper;
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void checkExpiringReservations() {
        List<Reservation> expiringReservations = reservationRepository.findExpiringReservations();
        
        for (Reservation reservation : expiringReservations) {
            Map<String, Object> data = new HashMap<>();
            data.put("reservationId", reservation.getId());
            data.put("clientName", reservation.getClient().getName());
            data.put("hoursRemaining", calculateHoursRemaining(reservation));
            
            notificationHelper.notifyEmployee(
                reservation.getAssignedEmployee().getId(),
                "reservation.expiring.soon",
                "RESERVATION",
                reservation.getId(),
                data
            );
        }
    }
}
```

## Event Types

The following event types are pre-configured with templates:

### Inquiry Events
- `inquiry.assigned` - New inquiry assigned
- `inquiry.status.changed` - Inquiry status changed
- `inquiry.response.added` - Response added to inquiry

### Reservation Events
- `reservation.created` - New reservation created
- `reservation.expiring.soon` - Reservation expiring soon
- `reservation.converted` - Reservation converted to sale
- `reservation.cancelled` - Reservation cancelled

### Sale Events
- `sale.recorded` - New sale recorded
- `sale.documents.missing` - Documents missing
- `sale.documents.received` - Documents received

### Inspection Events
- `inspection.due` - Inspection due
- `inspection.failed` - Inspection failed
- `inspection.passed` - Inspection passed
- `inspection.report.uploaded` - Report uploaded

### Task Events
- `task.assigned` - Task assigned
- `task.overdue` - Task overdue
- `event.reminder` - Event reminder

## Template Variables

Templates use Mustache-style variables: `{{variableName}}`

Example template:
```
A new inquiry (#{{inquiryId}}) has been assigned to you by {{assignedBy}}. Client: {{clientName}}
```

## Future Enhancements

1. **Email Notifications** - Integration with email providers (SMTP, SendGrid, etc.)
2. **SMS Notifications** - Integration with SMS gateways (Twilio, etc.)
3. **Push Notifications** - Web push and mobile push notifications
4. **Webhooks** - Send notifications to external systems
5. **Digest Mode** - Aggregate multiple notifications into periodic digests
6. **Quiet Hours** - Respect user quiet hours preferences
7. **Provider Failover** - Automatic failover between notification providers
8. **Real-time Updates** - WebSocket integration for real-time notification delivery

## Security Considerations

1. All endpoints require authentication
2. RBAC controls access to notification management
3. Users can only view their own notifications
4. Admins can manage preferences and templates
5. Super Admins have full access to the system

## Performance Considerations

1. **Deduplication** - Uses dedupKey to prevent duplicate notifications
2. **Pagination** - All list endpoints support pagination
3. **Indexing** - Database indexes on frequently queried fields
4. **Async Processing** - Jobs can be processed asynchronously (future)
5. **Caching** - Template caching for improved performance (future)

## Monitoring

Track these metrics:
- Total notifications sent
- Unread notification count per user
- Failed notification count
- Average delivery time
- Template rendering errors

## Testing

Use the provided endpoints to test:
1. Create a test event
2. Verify job creation
3. Retrieve notifications
4. Mark as read
5. Check stats

## Support

For questions or issues, contact the development team.

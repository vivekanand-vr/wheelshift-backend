# Notification System

## Overview

WheelShift Pro features a comprehensive notification system supporting multiple channels: In-App, Email, SMS, WhatsApp, Push Notifications, and Webhooks.

## Architecture

### Core Components

1. **NotificationEvent** - Business events that trigger notifications
2. **NotificationJob** - Individual notification delivery tasks
3. **NotificationTemplate** - Reusable message templates
4. **NotificationPreference** - User preferences per channel
5. **NotificationDelivery** - Delivery tracking and status

## Supported Channels

| Channel | Status | Use Case |
|---------|--------|----------|
| **IN_APP** | ✅ Active | Real-time in-app notifications |
| **EMAIL** | 🚧 Configured | Email notifications |
| **SMS** | 🚧 Configured | Text message alerts |
| **WHATSAPP** | 📋 Planned | WhatsApp messages |
| **PUSH** | 📋 Planned | Mobile push notifications |
| **WEBHOOK** | 📋 Planned | External system integration |

## Event Types

### Built-in Events

- `inquiry.created` - New inquiry created
- `inquiry.assigned` - Inquiry assigned to employee
- `inquiry.updated` - Inquiry status changed
- `reservation.created` - New reservation made
- `reservation.expiring` - Reservation about to expire
- `sale.completed` - Sale transaction completed
- `inspection.due` - Car inspection due
- `task.assigned` - Task assigned to employee
- `payment.received` - Payment received

## Quick Start

### 1. Send Notification (Simple)

```java
@Autowired
private NotificationEventHelper notificationHelper;

// Notify an employee
Map<String, Object> data = new HashMap<>();
data.put("inquiryId", inquiryId);
data.put("clientName", clientName);

notificationHelper.notifyEmployee(
    employeeId,
    "inquiry.assigned",
    "INQUIRY",
    inquiryId,
    data
);
```

### 2. Send Notification (Advanced)

```java
@Autowired
private NotificationService notificationService;

// Create notification event
CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
    .eventType("sale.completed")
    .entityType("SALE")
    .entityId(saleId)
    .payload(Map.of(
        "recipientType", "EMPLOYEE",
        "recipientId", employeeId,
        "saleAmount", amount,
        "customerName", customerName,
        "carModel", carModel
    ))
    .severity(NotificationSeverity.INFO)
    .occurredAt(LocalDateTime.now())
    .build();

notificationService.createNotificationEvent(request);
```

### 3. Get Notifications

```java
// Get unread notifications
List<NotificationJobResponse> notifications = notificationService
    .getNotificationsByRecipient(RecipientType.EMPLOYEE, employeeId);

// Mark as read
notificationService.markAsRead(notificationId);

// Mark all as read
notificationService.markAllAsRead(RecipientType.EMPLOYEE, employeeId);

// Get unread count
long unreadCount = notificationService
    .getUnreadCount(RecipientType.EMPLOYEE, employeeId);
```

## Templates

### Template Variables

Templates support variable substitution using `{{variableName}}` syntax:

```
Inquiry {{inquiryId}} has been assigned to you by {{assignedBy}}.
Client: {{clientName}}
```

### Creating Templates

```bash
POST /api/v1/notifications/templates
{
  "name": "inquiry_assigned",
  "channel": "IN_APP",
  "subject": "New Inquiry Assigned",
  "body": "Inquiry {{inquiryId}} assigned to you by {{assignedBy}}. Client: {{clientName}}",
  "version": 1,
  "isActive": true
}
```

### Template Management

```bash
# Get template
GET /api/v1/notifications/templates/{id}

# Get latest version
GET /api/v1/notifications/templates/latest?name=inquiry_assigned&channel=IN_APP

# Get by channel
GET /api/v1/notifications/templates/channel/IN_APP

# Update template
PUT /api/v1/notifications/templates/{id}

# Delete template
DELETE /api/v1/notifications/templates/{id}
```

## Preferences

Users can control notification delivery per channel:

### Setting Preferences

```bash
POST /api/v1/notifications/preferences
{
  "principalType": "EMPLOYEE",
  "principalId": 5,
  "channel": "EMAIL",
  "enabled": true,
  "quiet_hours_start": "22:00",
  "quiet_hours_end": "08:00"
}
```

### Preference Options

- **enabled** - Enable/disable channel
- **quiet_hours_start** - Start of quiet hours (no notifications)
- **quiet_hours_end** - End of quiet hours
- **frequency** - INSTANT, DAILY_DIGEST, WEEKLY_DIGEST (future)

### Managing Preferences

```bash
# Get user preferences
GET /api/v1/notifications/preferences/principal/EMPLOYEE/5

# Update preference
POST /api/v1/notifications/preferences
{
  "principalType": "EMPLOYEE",
  "principalId": 5,
  "channel": "SMS",
  "enabled": false
}

# Delete preference
DELETE /api/v1/notifications/preferences/{id}
```

## API Endpoints

### Notifications

```bash
# Create event
POST /api/v1/notifications/events

# Create job
POST /api/v1/notifications/jobs

# Get notifications for user
GET /api/v1/notifications/recipient/{type}/{id}

# Get notification by ID
GET /api/v1/notifications/{id}

# Mark as read
PUT /api/v1/notifications/{id}/read

# Mark all as read
PUT /api/v1/notifications/recipient/{type}/{id}/read-all

# Get stats
GET /api/v1/notifications/recipient/{type}/{id}/stats

# Get unread count
GET /api/v1/notifications/recipient/{type}/{id}/unread-count

# Get all events
GET /api/v1/notifications/events
```

### Templates

```bash
# Create template
POST /api/v1/notifications/templates

# Get template
GET /api/v1/notifications/templates/{id}

# Get latest version
GET /api/v1/notifications/templates/latest

# Get all templates
GET /api/v1/notifications/templates

# Get by channel
GET /api/v1/notifications/templates/channel/{channel}

# Update template
PUT /api/v1/notifications/templates/{id}

# Delete template
DELETE /api/v1/notifications/templates/{id}
```

### Preferences

```bash
# Create/update preference
POST /api/v1/notifications/preferences

# Get preference
GET /api/v1/notifications/preferences/{id}

# Get user preferences
GET /api/v1/notifications/preferences/principal/{type}/{id}

# Get all preferences
GET /api/v1/notifications/preferences

# Delete preference
DELETE /api/v1/notifications/preferences/{id}
```

## Notification Severity

```java
public enum NotificationSeverity {
    INFO,      // General information
    WARNING,   // Warning message
    ERROR,     // Error notification
    SUCCESS    // Success message
}
```

## Status Tracking

```java
public enum NotificationStatus {
    PENDING,    // Waiting to be sent
    SENT,       // Successfully sent
    DELIVERED,  // Delivered to recipient
    READ,       // Read by recipient
    FAILED      // Delivery failed
}
```

## Integration Examples

### On Inquiry Assignment

```java
@Service
public class InquiryServiceImpl implements InquiryService {
    
    @Autowired
    private NotificationEventHelper notificationHelper;
    
    @Transactional
    public InquiryResponse assignInquiry(Long inquiryId, Long employeeId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));
        
        inquiry.setAssignedTo(employeeId);
        inquiryRepository.save(inquiry);
        
        // Send notification
        Map<String, Object> data = new HashMap<>();
        data.put("inquiryId", inquiryId);
        data.put("clientName", inquiry.getClient().getName());
        
        notificationHelper.notifyEmployee(
            employeeId,
            "inquiry.assigned",
            "INQUIRY",
            inquiryId,
            data
        );
        
        return mapper.toResponse(inquiry);
    }
}
```

### On Sale Completion

```java
@Service
public class SaleServiceImpl implements SaleService {
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public SaleResponse completeSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
            .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        
        sale.setStatus(SaleStatus.COMPLETED);
        saleRepository.save(sale);
        
        // Notify sales person
        CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
            .eventType("sale.completed")
            .entityType("SALE")
            .entityId(saleId)
            .payload(Map.of(
                "recipientType", "EMPLOYEE",
                "recipientId", sale.getSalesPersonId(),
                "amount", sale.getFinalPrice(),
                "carModel", sale.getCar().getModel().getName(),
                "customerName", sale.getClient().getName()
            ))
            .severity(NotificationSeverity.SUCCESS)
            .build();
        
        notificationService.createNotificationEvent(request);
        
        return mapper.toResponse(sale);
    }
}
```

## Database Schema

### Tables

- `notification_events` - Business events
- `notification_jobs` - Individual notifications
- `notification_deliveries` - Delivery tracking
- `notification_templates` - Message templates
- `notification_preferences` - User preferences
- `notification_providers` - External providers (future)
- `notification_digests` - Digest aggregation (future)

### Migration

`V6__Add_Notifications_Tables.sql` - Creates all notification tables

## Configuration

```properties
# Notification settings (future)
notification.enabled=true
notification.default-channel=IN_APP
notification.retry-attempts=3
notification.batch-size=100
```

## Best Practices

1. **Use Event Types** - Define clear event types for consistency
2. **Template Variables** - Keep variable names consistent across templates
3. **Respect Preferences** - Always check user preferences before sending
4. **Handle Failures** - Implement retry logic for failed deliveries
5. **Batch Processing** - Use batch processing for bulk notifications
6. **Monitor Delivery** - Track delivery status and success rates

## Common Scenarios

### Scenario 1: Real-time Updates
- Channel: IN_APP
- Use Case: Notify sales person of new inquiry
- Delivery: Immediate

### Scenario 2: Daily Summary
- Channel: EMAIL
- Use Case: Daily task summary
- Delivery: Scheduled (8 AM)

### Scenario 3: Urgent Alerts
- Channel: SMS + IN_APP
- Use Case: High-priority task assigned
- Delivery: Immediate, multiple channels

### Scenario 4: Customer Updates
- Channel: EMAIL + SMS
- Use Case: Reservation confirmation
- Delivery: Immediate

## Implementation Files

### Enums (5)
- `NotificationChannel`, `NotificationSeverity`, `NotificationStatus`, `RecipientType`, `PrincipalType`

### Entities (7)
- `NotificationEvent`, `NotificationJob`, `NotificationDelivery`, `NotificationTemplate`, `NotificationPreference`, `NotificationProvider`, `NotificationDigest`

### Services (4)
- `NotificationService`, `NotificationTemplateService`, `NotificationPreferenceService`, `NotificationEventHelper`

### Controllers (3)
- `NotificationController`, `NotificationTemplateController`, `NotificationPreferenceController`

## Troubleshooting

### Notifications Not Appearing

1. Check user preferences (channel enabled?)
2. Verify template exists for event type
3. Check notification job status
4. Review application logs

### Template Issues

1. Verify template variables match payload
2. Check template is active
3. Ensure correct channel selected
4. Test variable substitution

### Delivery Failures

1. Check delivery status in database
2. Review error logs
3. Verify external provider configuration (Email/SMS)
4. Check retry attempts

## Future Enhancements

- ✅ In-App notifications
- 🚧 Email integration
- 🚧 SMS integration
- 📋 WhatsApp integration
- 📋 Push notifications
- 📋 Webhook support
- 📋 Digest notifications
- 📋 Notification grouping
- 📋 Rich media support

## Additional Resources

- [Implementation Guide](IMPLEMENTATION.md)
- [Template Examples](TEMPLATES.md)
- API Documentation: http://localhost:8080/api/v1/swagger-ui.html

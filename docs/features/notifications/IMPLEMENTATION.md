# Notification System Implementation

## Quick Reference

### Send Notification

```java
@Autowired
private NotificationEventHelper helper;

// Simple notification
helper.notifyEmployee(
    employeeId,
    "inquiry.assigned",
    "INQUIRY",
    inquiryId,
    Map.of("inquiryId", inquiryId, "clientName", "John Doe")
);

// Multiple recipients
helper.notifyEmployees(
    List.of(emp1, emp2, emp3),
    "task.assigned",
    "TASK",
    taskId,
    data
);

// Role-based notification
helper.notifyRole(
    "SALES",
    "inquiry.created",
    "INQUIRY",
    inquiryId,
    data
);
```

## Database Schema

```sql
-- Notification Events
CREATE TABLE notification_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(100),
    payload JSON,
    severity VARCHAR(20),
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification Jobs
CREATE TABLE notification_jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    recipient_type VARCHAR(20) NOT NULL,
    recipient_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Templates
CREATE TABLE notification_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    version INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Preferences
CREATE TABLE notification_preferences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    principal_type VARCHAR(20) NOT NULL,
    principal_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Event Types

```java
public class NotificationEvents {
    // Inquiries
    public static final String INQUIRY_CREATED = "inquiry.created";
    public static final String INQUIRY_ASSIGNED = "inquiry.assigned";
    public static final String INQUIRY_UPDATED = "inquiry.updated";
    
    // Reservations
    public static final String RESERVATION_CREATED = "reservation.created";
    public static final String RESERVATION_EXPIRING = "reservation.expiring";
    
    // Sales
    public static final String SALE_COMPLETED = "sale.completed";
    
    // Inspections
    public static final String INSPECTION_DUE = "inspection.due";
    
    // Tasks
    public static final String TASK_ASSIGNED = "task.assigned";
    public static final String TASK_DUE = "task.due";
    
    // Payments
    public static final String PAYMENT_RECEIVED = "payment.received";
}
```

## Template Examples

### Inquiry Assigned Template

```
Name: inquiry_assigned
Channel: IN_APP
Subject: New Inquiry Assigned
Body: Inquiry #{{inquiryId}} has been assigned to you by {{assignedBy}}. 
      Client: {{clientName}}
```

### Sale Completed Template

```
Name: sale_completed
Channel: IN_APP
Subject: Sale Completed
Body: Congratulations! Sale #{{saleId}} for {{carModel}} has been completed. 
      Amount: ${{amount}}
```

## Integration Points

### In Service Layer

```java
@Service
public class InquiryServiceImpl {
    
    @Autowired
    private NotificationEventHelper notificationHelper;
    
    @Transactional
    public InquiryResponse assignInquiry(Long inquiryId, Long employeeId) {
        Inquiry inquiry = findInquiry(inquiryId);
        inquiry.setAssignedTo(employeeId);
        inquiryRepository.save(inquiry);
        
        // Trigger notification
        notificationHelper.notifyEmployee(
            employeeId,
            "inquiry.assigned",
            "INQUIRY",
            inquiryId,
            Map.of(
                "inquiryId", inquiryId,
                "clientName", inquiry.getClient().getName()
            )
        );
        
        return mapper.toResponse(inquiry);
    }
}
```

## API Endpoints

### Get Notifications
```bash
GET /api/v1/notifications/recipient/EMPLOYEE/5
```

### Mark as Read
```bash
PUT /api/v1/notifications/{id}/read
```

### Get Unread Count
```bash
GET /api/v1/notifications/recipient/EMPLOYEE/5/unread-count
```

### Create Template
```bash
POST /api/v1/notifications/templates
{
  "name": "custom_event",
  "channel": "IN_APP",
  "subject": "Subject",
  "body": "Message with {{variable}}",
  "isActive": true
}
```

### Set Preference
```bash
POST /api/v1/notifications/preferences
{
  "principalType": "EMPLOYEE",
  "principalId": 5,
  "channel": "EMAIL",
  "enabled": true
}
```

## Notification Statuses

```
PENDING → SENT → DELIVERED → READ
              ↘ FAILED
```

## Testing

```java
@Test
public void testNotification() {
    // Create event
    notificationHelper.notifyEmployee(
        employeeId, "test.event", "TEST", testId, data
    );
    
    // Verify job created
    List<NotificationJob> jobs = notificationService
        .getNotificationsByRecipient(RecipientType.EMPLOYEE, employeeId);
    
    assertEquals(1, jobs.size());
    assertEquals(NotificationStatus.PENDING, jobs.get(0).getStatus());
}
```

## Best Practices

1. **Use Helper Methods** - `NotificationEventHelper` for common cases
2. **Template Variables** - Keep consistent naming across templates
3. **Check Preferences** - System respects user preferences automatically
4. **Async Processing** - Use `@Async` for notification delivery
5. **Error Handling** - Implement retry logic for failed deliveries

## Migration File

`V6__Add_Notifications_Tables.sql` - Creates all notification tables

## Implementation Classes

- `NotificationService` - Core notification service
- `NotificationEventHelper` - Convenience methods
- `NotificationTemplateService` - Template management
- `NotificationPreferenceService` - User preferences
- Controllers: `NotificationController`, `NotificationTemplateController`, `NotificationPreferenceController`

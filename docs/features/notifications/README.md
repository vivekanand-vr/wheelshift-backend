# Notification System

## Overview

Comprehensive notification system supporting multiple channels: In-App, Email, SMS, WhatsApp, Push Notifications, and Webhooks.

## Core Components

1. **NotificationEvent** - Business events that trigger notifications
2. **NotificationJob** - Individual notification delivery tasks
3. **NotificationTemplate** - Reusable message templates with variable substitution
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

| Category | Event Type | Description |
|----------|------------|-------------|
| **Inquiries** | `inquiry.created` | New inquiry created |
| | `inquiry.assigned` | Inquiry assigned to employee |
| | `inquiry.updated` | Inquiry status changed |
| **Reservations** | `reservation.created` | New reservation made |
| | `reservation.expiring` | Reservation about to expire |
| | `reservation.cancelled` | Reservation cancelled |
| **Sales** | `sale.completed` | Sale transaction completed |
| | `sale.pending` | Sale pending approval |
| **Inspections** | `inspection.due` | Car inspection due |
| | `inspection.completed` | Inspection completed |
| | `inspection.failed` | Inspection failed |
| **Tasks** | `task.assigned` | Task assigned to employee |
| | `task.due` | Task due soon |
| | `task.completed` | Task completed |
| **Payments** | `payment.received` | Payment received |
| | `payment.overdue` | Payment overdue |

### Adding Custom Events

```java
public class NotificationEvents {
    public static final String CUSTOM_EVENT = "custom.event";
}

// Use in code
notificationHelper.notifyEmployee(
    employeeId, 
    NotificationEvents.CUSTOM_EVENT,
    "ENTITY_TYPE",
    entityId,
    data
);
```

## Quick Start

### 1. Simple Notification (Recommended)

```java
@Autowired
private NotificationEventHelper helper;

// Notify single employee
helper.notifyEmployee(
    employeeId,
    "inquiry.assigned",
    "INQUIRY",
    inquiryId,
    Map.of(
        "inquiryId", inquiryId, 
        "clientName", "John Doe",
        "assignedBy", "Manager"
    )
);

// Notify multiple employees
helper.Advanced Notification

```java
@Autowired
private NotificationService notificationService;

// Create notification event with full control
CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
    .eventType("sale.completed")
    .entityType("SALE")
    .entityId(saleId)
    .payload(Map.of(
        "recipientType", "EMPLOYEE",
        "recipientId", employeeId,
        "saleId", saleId,
        "saleAmount", 185000.00,
        "customerName", "John Doe",
        "carModel", "Toyota Camry 2023"
    ))
    .severity(NotificationSeverity.SUCCESS)
    .occurredAt(LocalDateTime.now())
    .build();

notificationService.createNotificationEvent(request);
```
Retrieve Notifications

```java
// Get paginated notifications for recipient
Page<NotificationJobResponse> notifications = notificationService
    .getNotificationsForRecipient(
        RecipientType.EMPLOYEE, 
        employeeId,
        NotificationChannel.IN_APP,
        pageable
    );

// Get unread count
long unreadCount = notificationService
    .getUnreadCount(RecipientType.EMPLOYEE, employeeId, NotificationChannel.IN_APP);

// Get notification stats
NotificationStatsResponse stats = notificationService
    .getNotificationStats(RecipientType.EMPLOYEE, employeeId);

// Mark single notification as read
notificationService.markNotificationAsRead(notificationId);

// Mark all as read
notificationService.markAllNotificationsAsRead(
    RecipientType.EMPLOYEE, 
    employeeId, 
    NotificationChannel.IN_APP
);
```

## Integration in Services

### Example: Inquiry Assignment

```java
@Service
public class InquiryServiceImpl implements InquiryService {
    
    @Autowired
    private NotificationEventHelper notificationHelper;
    
    @Transactional
    public InquiryResponse assignInquiry(Long inquiryId, Long employeeId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));
        
        Employee currentUser = getCurrentUser();
        inquiry.setAssignedTo(employeeId);
        inquiryRepository.save(inquiry);
        
        // Send notification to assigned employee
        notificationHelper.notifyEmployee(
            employeeId,
            "inquiry.assigned",
            "INQUIRY",
            inquiryId,
            Map.of(
                "inquiryId", inquiryId,
                "clientName", inquiry.getClient().getName(),
                "clientPhone", inquiry.getClient().getPhone(),
                "assignedBy", currentUser.getName()
            )
        );
        
        return mapper.toResponse(inquiry);
    }
}
```

### Example: Sale Completion

```java
@Service
public class SaleServiceImpl implements SaleService {
    
    @Autowired
    private NotificationEventHelper notificationHelper;
    
    @Transactional
    public SaleResponse completeSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
            .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setCompletedAt(LocalDateTime.now());
        saleRepository.save(sale);
        
        // Notify sales person
        notificationHelper.notifyEmployee(
            sale.getSalesPersonId(),
            "sale.completed",
            "SALE",
            saleId,
            Map.of(
                "saleId", saleId,
                "carModel", sale.getCar().getModel().getName(),
                "amount", sale.getFinalPrice(),
                "customerName", sale.getClient().getName(),
                "completedDate", sale.getCompletedAt().toString()
            )
        );
        
        return mapper.toResponse(sale);
    }
}
```

## Templates

**Create via API:**
```bash
POST /api/v1/notifications/templates
{
  "name": "inquiry_assigned",
  "channel": "IN_APP",
  "subject": "New Inquiry Assigned",
  "body": "Inquiry {{inquiryId}} assigned by {{assignedBy}}. Client: {{clientName}}",
  "version": 1,
  "isActive": true
}
```

## Preferences

User preferences control notification delivery per channel.

**Set Preference:**
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

**Available Options:**
- `enabled` - Enable/disable channel
- `quiet_hours_start` / `quiet_hours_end` - No notifications during quiet hours
- `frequency` - INSTANT, DAILY_DIGEST, WEEKLY_DIGEST (future) Templates

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/notifications/templates` | Create template |
| GET | `/api/v1/notifications/templates/{id}` | Get template by ID |
| GET | `/api/v1/notifications/templates/latest` | Get latest template version |
| GET | `/api/v1/notifications/templates` | Get all templates |
| GET | `/api/v1/notifications/templates/channel/{channel}` | Get templates by channel |
| PUT | `/api/v1/notifications/templates/{id}` | Update template |
| DELETE | `/api/v1/notifications/templates/{id}` | Delete template |

### Preferences

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/notifications/preferences` | Create/update preference |
| GET | `/api/v1/notifications/preferences/{id}` | Get preference by ID |
| GET | `/api/v1/notifications/preferences/principal/{type}/{id}` | Get user preferences |
| GET | `/api/v1/notifications/preferences` | Get all preferences |
| DELETE | `/api/v1/notifications/preferences/{id}` | Delete preference |

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

## Notification Template System

### Template Concept

Templates use variable substitution with `{{variableName}}` syntax. The system:
1. Matches event type to template name
2. Substitutes variables from event payload
3. Generates personalized notification message
4. Respects user channel preferences

### Built-in Template Examples

#### inquiry_assigned
```
Channel: IN_APP
Subject: New Inquiry Assigned
Body: Inquiry #{{inquiryId}} has been assigned to you by {{assignedBy}}. 
      Client: {{clientName}}
      Phone: {{clientPhone}}
```

#### sale_completed
```
Channel: IN_APP
Subject: Sale Completed
Body: Congratulations! Sale #{{saleId}} for {{carModel}} has been completed. 
      Final Amount: ${{amount}}
      Customer: {{customerName}}
```

#### reservation_expiring
```
Channel: IN_APP, EMAIL
Subject: Reservation Expiring Soon
Body: Reservation #{{reservationId}} for {{carModel}} expires in {{hoursRemaining}} hours.
      Customer: {{customerName}}
      Expiry: {{expiryDate}}
```

#### task_assigned
```
Channel: IN_APP
Subject: New Task Assigned
Body: Task "{{taskTitle}}" has been assigned to you.
      Priority: {{priority}}
      Due Date: {{dueDate}}
```

#### inspection_due
```
Channel: IN_APP, EMAIL
Subject: Inspection Due
Body: Car inspection due for {{carModel}} (VIN: {{vinNumber}}).
      Location: {{locationName}}
      Scheduled: {{scheduledDate}}
```

#### payment_received
```
Channel: IN_APP, EMAIL
Subject: Payment Received
Body: Payment of ${{amount}} received for {{transactionType}}.
      Transaction ID: {{transactionId}}
      Payment Method: {{paymentMethod}}
```

### Variable Naming Conventions

| Entity | Variables |
|--------|-----------|
| **Inquiry** | `inquiryId`, `clientName`, `clientPhone`, `clientEmail`, `carModel`, `assignedBy` |
| **Sale** | `saleId`, `carModel`, `amount`, `customerName`, `salesPerson`, `completedDate` |
| **Reservation** | `reservationId`, `carModel`, `customerName`, `expiryDate`, `hoursRemaining` |
| **Task** | `taskId`, `taskTitle`, `priority`, `dueDate`, `assignedBy` |
| **Inspection** | `inspectionId`, `carModel`, `vinNumber`, `locationName`, `scheduledDate` |
| **Payment** | `transactionId`, `amount`, `transactionType`, `paymentMethod` |

## Database Schema

```sql
-- Events that trigger notifications
notification_events (
  id, event_type, entity_type, entity_id, 
  payload JSON, severity, occurred_at, created_at
)

-- Individual notification delivery jobs
notification_jobs (
  id, event_id, recipient_type, recipient_id, channel,
  subject, body, status, scheduled_at, sent_at, 
  delivered_at, read_at, created_at
)

-- Reusable message templates
notification_templates (
  id, name, channel, subject, body, 
  version, is_active, locale, created_at
)

-- User channel preferences
notification_preferences (
  id, principal_type, principal_id, channel,
  enabled, metadata JSON, created_at
)

-- Delivery tracking
notification_deliveries (
  id, job_id, status, attempts, 
  last_attempt_at, error_message
)
```

**Migration**: `V6__Add_Notifications_Tables.sql`

## Implementation Classes

### Services
- `NotificationService` - Core notification operations
- `NotificationServiceImpl` - Service implementation
- `NotificationEventHelper` - Convenience methods for common patterns
- `NotificationTemplateService` - Template management
- `NotificationPreferenceService` - User preferences

### Controllers
- `NotificationController` - Notification CRUD and retrieval
- `NotificationTemplateController` - Template management
- `NotificationPreferenceController` - Preference management

### Repositories
- `NotificationEventRepository`
- `NotificationJobRepository`
- `NotificationDeliveryRepository`
- `NotificationTemplateRepository`
- `NotificationPreferenceRepository`

### Enums
- `NotificationChannel`: `IN_APP`, `EMAIL`, `SMS`, `WHATSAPP`, `PUSH`, `WEBHOOK`
- `NotificationSeverity`: `INFO`, `WARNING`, `ERROR`, `SUCCESS`
- `NotificationStatus`: `PENDING`, `SENT`, `DELIVERED`, `READ`, `FAILED`
- `RecipientType`: `EMPLOYEE`, `CLIENT`, `ROLE`
- `PrincipalType`: `EMPLOYEE`, `ROLE`, `DEPARTMENT`

## Status Flow

```
PENDING → SENT → DELIVERED → READ
             ↘ FAILED (with retry)
```

## Testing Example

```java
@Test
public void testNotificationWorkflow() {
    // Create event
    notificationHelper.notifyEmployee(
        employeeId, 
        "inquiry.assigned", 
        "INQUIRY", 
        inquiryId, 
        Map.of("inquiryId", inquiryId, "clientName", "John Doe")
    );
    
    // Verify job created
    List<NotificationJobResponse> jobs = notificationService
        .getNotificationsForRecipient(RecipientType.EMPLOYEE, employeeId, 
                                      NotificationChannel.IN_APP, pageable);
    
    assertThat(jobs).hasSize(1);
    assertThat(jobs.get(0).getStatus()).isEqualTo(NotificationStatus.PENDING);
    assertThat(jobs.get(0).getBody()).contains("John Doe");
}
```

## Best Practices

1. **Use Helper Methods** - `NotificationEventHelper` for common notification patterns
2. **Consistent Variables** - Keep template variable names consistent across all templates
3. **Event Types** - Define clear, descriptive event type names
4. **Error Handling** - Implement retry logic for failed deliveries
5. **Async Processing** - Use `@Async` for notification delivery to avoid blocking
6. **Template Versioning** - Version templates when making significant changes
7. **Respect Preferences** - System automatically respects user channel preferences
8. **Monitor Delivery** - Track delivery status and success rates

## API Documentation

Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

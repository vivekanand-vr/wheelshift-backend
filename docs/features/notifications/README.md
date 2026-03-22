# Notification System

## Overview

Event-driven notification system that keeps employees and clients informed about important business events. Notifications are delivered via two channels:

| Channel | Status | Description |
|---------|--------|-------------|
| **IN_APP** | ✅ Active | Browser notifications delivered to the frontend in real-time |
| **EMAIL** | 📋 Planned | Email delivery via SMTP |

---

## Entity Structure

Five entities make up the notification system. The diagram below shows how they are connected:

```
┌──────────────────────┐              ┌──────────────────────────────┐
│   NotificationEvent  │              │     NotificationTemplate     │
├──────────────────────┤              ├──────────────────────────────┤
│ id                   │              │ id                           │
│ event_type ──────────┼── lookup ───►│ name  (matches event_type)  │
│ entity_type          │  (at render) │ channel  (IN_APP | EMAIL)   │
│ entity_id            │              │ subject                      │
│ payload (JSON)       │              │ content  ({{var}} syntax)   │
│ severity             │              │ variables (JSON array)       │
│ occurred_at          │              │ locale / version             │
│ created_at           │              └──────────────────────────────┘
└──────────┬───────────┘
           │ 1 : N
           ▼
┌──────────────────────┐              ┌──────────────────────────────┐
│   NotificationJob    │◄─consulted ──┤   NotificationPreference     │
├──────────────────────┤  during job  ├──────────────────────────────┤
│ id                   │  creation    │ id                           │
│ event_id  (FK)       │              │ principal_type               │
│ recipient_type       │              │ principal_id                 │
│ recipient_id         │              │ event_type                   │
│ channel              │              │ channel  (IN_APP | EMAIL)   │
│ status               │              │ enabled                      │
│ dedup_key  (unique)  │              │ frequency (IMMEDIATE|DIGEST) │
│ retries              │              │ quiet_hours_start / end      │
│ sent_at              │              │ severity_threshold           │
│ scheduled_for        │              └──────────────────────────────┘
│ created_at           │
└──────────┬───────────┘
           │ 1 : N
           ▼
┌──────────────────────┐
│ NotificationDelivery │
├──────────────────────┤
│ id                   │
│ job_id  (FK)         │
│ provider             │
│ provider_message_id  │
│ status               │
│ sent_at              │
│ delivered_at         │
│ error_message        │
│ created_at           │
└──────────────────────┘
```

**NotificationTemplate** is not linked by a foreign key — it is looked up by `name + channel + locale` at render time, allowing templates to be updated without affecting stored jobs.

**NotificationPreference** is not linked by a foreign key either — it is queried during job creation to decide whether to create a job for a given channel.

---

## Processing Flow

How a notification travels from a business event to the recipient:

```
  Business Service  (e.g. InquiryServiceImpl, SaleServiceImpl)
          │
          │  notificationHelper.notifyEmployee(employeeId, "inquiry.assigned", ...)
          │       or  .notifyClient(...)  /  .sendCriticalNotification(...)
          ▼
   NotificationEventHelper
          │
          │  notificationService.createNotificationEvent(request)
          ▼
  ┌─────────────────────────────────────────────────────────────────┐
  │   NotificationServiceImpl  —  createNotificationEvent()         │
  │                                                                 │
  │  1. Build and persist  NotificationEvent                        │
  │     (event_type, entity_type, entity_id, payload, severity)     │
  │                                                                 │
  │  2. generateNotificationJobs(event)                             │
  │     ├─ Extract recipientType + recipientId from payload         │
  │     ├─ Always create an  IN_APP  job                            │
  │     ├─ Create an  EMAIL  job if preference is enabled           │
  │     └─ Dedup check: skip if identical job already exists        │
  │        (dedupKey = "{eventId}-{recipientType}-{recipientId}-    │
  │                     {channel}")                                 │
  │                                                                 │
  │  3. Persist NotificationJob(s)  [status = PENDING]              │
  └─────────────────────┬───────────────────────────────────────────┘
                        │
           ┌────────────┴─────────────┐
           │                          │
           ▼                          ▼
   ┌───────────────┐          ┌──────────────────┐
   │  IN_APP Job   │          │   EMAIL Job      │
   │               │          │                  │
   │  Stored in DB │          │  Template looked │
   │  with status  │          │  up by name +    │
   │  SENT         │          │  channel + locale│
   │               │          │  Variables from  │
   │  Frontend     │          │  payload substi- │
   │  retrieves    │          │  tuted into      │
   │  via GET      │          │  content         │
   │  /recipient/  │          │                  │
   │  ...          │          │  Sent via SMTP   │
   │               │          │                  │
   │  Unread check │          │  NotificationDel-│
   │  via sentAt   │          │  ivery record    │
   │  IS NULL      │          │  created         │
   └───────────────┘          └──────────────────┘
```

---

## Core Components

1. **NotificationEvent** — The raw business event that triggers notifications. Stores what happened, to which entity, and a JSON payload with context variables.
2. **NotificationJob** — One delivery task per recipient per channel. Created automatically when a `NotificationEvent` is persisted.
3. **NotificationTemplate** — Reusable message templates with `{{variable}}` substitution. Looked up by `name + channel + locale` at render time.
4. **NotificationPreference** — Per-principal settings controlling which channels are active, quiet hours, and delivery frequency.
5. **NotificationDelivery** — Tracks the actual delivery attempt for a job, including provider response and delivery status.

---

## Event Types

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
| **Inspections** | `inspection.due` | Vehicle inspection due |
| | `inspection.completed` | Inspection completed |
| | `inspection.failed` | Inspection failed |
| **Tasks** | `task.assigned` | Task assigned to employee |
| | `task.due` | Task due soon |
| | `task.completed` | Task completed |
| **Payments** | `payment.received` | Payment received |
| | `payment.overdue` | Payment overdue |

### Adding Custom Events

```java
// Define the event type constant
public class NotificationEvents {
    public static final String CUSTOM_EVENT = "custom.event";
}

// Send the notification from any service
notificationHelper.notifyEmployee(
    employeeId,
    NotificationEvents.CUSTOM_EVENT,
    "ENTITY_TYPE",
    entityId,
    Map.of("key", value)
);
```

---

## Quick Start

### Recommended — Using the Helper

```java
@Autowired
private NotificationEventHelper notificationHelper;

// Notify a single employee
notificationHelper.notifyEmployee(
    employeeId,
    "inquiry.assigned",
    "INQUIRY",
    inquiryId,
    Map.of(
        "inquiryId",  inquiryId,
        "clientName", "John Doe",
        "assignedBy", "Manager"
    )
);

// Notify a client
notificationHelper.notifyClient(
    clientId,
    "reservation.created",
    "RESERVATION",
    reservationId,
    Map.of("reservationId", reservationId, "vehicleModel", "Honda Activa")
);

// Send a critical notification
notificationHelper.sendCriticalNotification(
    "inspection.failed",
    "CAR",
    carId,
    RecipientType.EMPLOYEE,
    employeeId,
    Map.of("carModel", "Toyota Camry", "reason", "Brake failure")
);
```

### Advanced — Full Control

```java
@Autowired
private NotificationService notificationService;

CreateNotificationEventRequest request = CreateNotificationEventRequest.builder()
    .eventType("sale.completed")
    .entityType("SALE")
    .entityId(saleId)
    .payload(Map.of(
        "recipientType", "EMPLOYEE",
        "recipientId",   employeeId,
        "saleId",        saleId,
        "saleAmount",    185000.00,
        "customerName",  "John Doe",
        "carModel",      "Toyota Camry 2023"
    ))
    .severity(NotificationSeverity.INFO)
    .occurredAt(LocalDateTime.now())
    .build();

notificationService.createNotificationEvent(request);
```

### Reading Notifications

```java
// Paginated list for a recipient
Page<NotificationJobResponse> notifications = notificationService
    .getNotificationsForRecipient(
        RecipientType.EMPLOYEE,
        employeeId,
        NotificationChannel.IN_APP,
        pageable
    );

// Unread count
long unreadCount = notificationService
    .getUnreadCount(RecipientType.EMPLOYEE, employeeId, NotificationChannel.IN_APP);

// Summary stats
NotificationStatsResponse stats = notificationService
    .getNotificationStats(RecipientType.EMPLOYEE, employeeId);

// Mark as read
notificationService.markNotificationAsRead(notificationId);

// Mark all as read
notificationService.markAllNotificationsAsRead(
    RecipientType.EMPLOYEE, employeeId, NotificationChannel.IN_APP
);
```

---

## Integration Examples

### Inquiry Assignment

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

        notificationHelper.notifyEmployee(
            employeeId,
            "inquiry.assigned",
            "INQUIRY",
            inquiryId,
            Map.of(
                "inquiryId",   inquiryId,
                "clientName",  inquiry.getClient().getName(),
                "clientPhone", inquiry.getClient().getPhone(),
                "assignedBy",  currentUser.getName()
            )
        );

        return mapper.toResponse(inquiry);
    }
}
```

### Sale Completion

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
        saleRepository.save(sale);

        notificationHelper.notifyEmployee(
            sale.getEmployee().getId(),
            "sale.completed",
            "SALE",
            saleId,
            Map.of(
                "saleId",       saleId,
                "carModel",     sale.getCar().getModel().getModel(),
                "amount",       sale.getSalePrice(),
                "customerName", sale.getClient().getName()
            )
        );

        return mapper.toResponse(sale);
    }
}
```

---

## Templates

Templates use `{{variableName}}` syntax. The system looks up the matching template by `name + channel + locale` and substitutes variables from the event payload.

### Built-in Templates

#### `inquiry_assigned`
```
Channel: IN_APP
Subject: New Inquiry Assigned
Content: Inquiry #{{inquiryId}} has been assigned to you by {{assignedBy}}.
         Client: {{clientName}}  |  Phone: {{clientPhone}}
```

#### `sale_completed`
```
Channel: IN_APP
Subject: Sale Completed
Content: Sale #{{saleId}} for {{carModel}} completed.
         Amount: ₹{{amount}}  |  Customer: {{customerName}}
```

#### `reservation_expiring`
```
Channel: IN_APP
Subject: Reservation Expiring Soon
Content: Reservation #{{reservationId}} for {{carModel}} expires in {{hoursRemaining}} hours.
         Customer: {{customerName}}  |  Expires: {{expiryDate}}
```

#### `task_assigned`
```
Channel: IN_APP
Subject: New Task Assigned
Content: Task "{{taskTitle}}" assigned to you by {{assignedBy}}.
         Priority: {{priority}}  |  Due: {{dueDate}}
```

#### `inspection_due`
```
Channel: IN_APP
Subject: Inspection Due
Content: Inspection due for {{carModel}} (VIN: {{vinNumber}}).
         Location: {{locationName}}  |  Scheduled: {{scheduledDate}}
```

### Create / Manage via API

```bash
POST /api/v1/notifications/templates
{
  "name":    "inquiry_assigned",
  "channel": "IN_APP",
  "locale":  "en",
  "version": 1,
  "subject": "New Inquiry Assigned",
  "content": "Inquiry #{{inquiryId}} assigned by {{assignedBy}}. Client: {{clientName}}"
}
```

### Variable Reference

| Entity | Variables |
|--------|-----------|
| **Inquiry** | `inquiryId`, `clientName`, `clientPhone`, `clientEmail`, `assignedBy` |
| **Sale** | `saleId`, `carModel`, `amount`, `customerName`, `salesPerson` |
| **Reservation** | `reservationId`, `carModel`, `customerName`, `expiryDate`, `hoursRemaining` |
| **Task** | `taskId`, `taskTitle`, `priority`, `dueDate`, `assignedBy` |
| **Inspection** | `inspectionId`, `carModel`, `vinNumber`, `locationName`, `scheduledDate` |
| **Payment** | `transactionId`, `amount`, `transactionType`, `paymentMethod` |

---

## Preferences

Control which channels are active for a given principal, with quiet hours and frequency options.

```bash
POST /api/v1/notifications/preferences
{
  "principalType":    "EMPLOYEE",
  "principalId":      5,
  "eventType":        "inquiry.assigned",
  "channel":          "IN_APP",
  "enabled":          true,
  "frequency":        "IMMEDIATE",
  "quietHoursStart":  "22:00",
  "quietHoursEnd":    "08:00",
  "severityThreshold": "INFO"
}
```

---

## API Endpoints

### Notifications

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/notifications/events` | Create a notification event |
| `POST` | `/api/v1/notifications/jobs` | Create a notification job directly |
| `GET` | `/api/v1/notifications/recipient/{type}/{id}` | Get notifications for a recipient (param: `channel`) |
| `GET` | `/api/v1/notifications/{id}` | Get notification by ID |
| `PUT` | `/api/v1/notifications/{id}/read` | Mark as read |
| `PUT` | `/api/v1/notifications/recipient/{type}/{id}/read-all` | Mark all as read |
| `GET` | `/api/v1/notifications/recipient/{type}/{id}/stats` | Get stats |
| `GET` | `/api/v1/notifications/recipient/{type}/{id}/unread-count` | Get unread count |
| `GET` | `/api/v1/notifications/events` | List all events (paginated) |

### Templates

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/notifications/templates` | Create template |
| `GET` | `/api/v1/notifications/templates/{id}` | Get by ID |
| `GET` | `/api/v1/notifications/templates/latest` | Get latest version |
| `GET` | `/api/v1/notifications/templates` | List all (paginated) |
| `GET` | `/api/v1/notifications/templates/channel/{channel}` | List by channel |
| `PUT` | `/api/v1/notifications/templates/{id}` | Update template |
| `DELETE` | `/api/v1/notifications/templates/{id}` | Delete template |

### Preferences

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/notifications/preferences` | Create or update preference |
| `GET` | `/api/v1/notifications/preferences/{id}` | Get by ID |
| `GET` | `/api/v1/notifications/preferences/principal/{type}/{id}` | Get all for a principal |
| `GET` | `/api/v1/notifications/preferences` | List all (paginated) |
| `DELETE` | `/api/v1/notifications/preferences/{id}` | Delete preference |

---

## Enums

```java
// Supported delivery channels
enum NotificationChannel  { IN_APP, EMAIL }

// Event severity levels
enum NotificationSeverity { INFO, WARN, CRITICAL }

// Job lifecycle states
enum NotificationStatus   { PENDING, SCHEDULED, SENT, FAILED, CANCELLED }

// Delivery frequency
enum NotificationFrequency { IMMEDIATE, DIGEST }

// Who receives the notification
enum RecipientType  { EMPLOYEE, CLIENT, ROLE }

// Who owns a preference
enum PrincipalType  { EMPLOYEE, CLIENT, ROLE, COMPANY }

// Delivery record outcome
enum DeliveryStatus { SENT, DELIVERED, BOUNCED, FAILED }
```

## Status Flow

```
PENDING ──► SCHEDULED ──► SENT
                │
                └──► FAILED (retried up to max retries)
                          │
                          └──► CANCELLED (if max retries exceeded)
```

Unread detection for IN_APP jobs uses `sentAt IS NULL` — jobs are considered unread until the recipient's frontend reads them.

---

## Implementation Classes

### Services
- `NotificationService` / `NotificationServiceImpl` — core notification logic
- `NotificationEventHelper` — convenience facade (`notifyEmployee`, `notifyClient`, `sendCriticalNotification`, `sendNotification`)
- `NotificationTemplateService` / `NotificationTemplateServiceImpl` — template CRUD and rendering
- `NotificationPreferenceService` / `NotificationPreferenceServiceImpl` — preference management

### Controllers
- `NotificationController` — notification retrieval and read/unread management
- `NotificationTemplateController` — template CRUD
- `NotificationPreferenceController` — preference CRUD

### Repositories
- `NotificationEventRepository`
- `NotificationJobRepository`
- `NotificationDeliveryRepository`
- `NotificationTemplateRepository`
- `NotificationPreferenceRepository`

---

## Best Practices

1. **Always use the helper** — `NotificationEventHelper` handles recipient extraction and payload structure automatically
2. **Consistent variable names** — use the same variable names across events and templates (see Variable Reference above)
3. **Define event type constants** — keep event type strings in a constants class to avoid typos
4. **Async delivery** — notification creation is synchronous; actual sending (especially EMAIL when implemented) should be `@Async`
5. **Template versioning** — increment `version` when making significant content changes; old jobs reference the latest template at render time
6. **Respect preferences** — the system checks `NotificationPreference` before creating jobs; do not bypass this by creating jobs directly

---

## Swagger UI

`http://localhost:8080/api/v1/swagger-ui.html`

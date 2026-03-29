# Notification System

## Overview

Event-driven notification system that keeps employees and clients informed about important business events. Notifications are delivered via two channels:

| Channel | Status | Description |
|---------|--------|-------------|
| **IN_APP** | ✅ Active | Browser notifications delivered to the frontend in real-time |
| **EMAIL** | 📋 Planned | Email delivery via SMTP |

---

## Delivery Rules

Three delivery rule systems control when and how notifications are sent:

| Rule | Status | Implementation | Description |
|------|--------|----------------|-------------|
| **Opt-out** | ✅ Implemented | Job creation | Users can disable specific event types or channels via `NotificationPreference.enabled=false` |
| **Severity threshold** | ✅ Implemented | Job creation | Filter out low-priority events via `NotificationPreference.severityThreshold` (INFO/WARN/CRITICAL) |
| **Quiet hours** | ✅ Implemented | Consumer | Jobs arriving during quiet hours (e.g., 22:00-08:00) are rescheduled to the end of the quiet period |
| **Digest batching** | ✅ Implemented | Scheduler | Jobs with `frequency=DIGEST` are batched hourly and sent as a single summary notification |

### How It Works

**Opt-out & Severity (Phase 1 — Job Creation):**
```java
// NotificationServiceImpl.createJobForRecipient()
// 1. Query preference for recipient + event type + channel
// 2. Skip job creation if preference.enabled = false
// 3. Skip if event.severity < preference.severityThreshold
// 4. Set scheduledFor + status=SCHEDULED if frequency=DIGEST
```

**Quiet Hours (Phase 2 — Consumer):**
```java
// NotificationKafkaConsumer.consumeInApp()
// 1. Check if current time is within recipient's quiet hours
// 2. If yes: reschedule job to quiet hours end + save with status=SCHEDULED
// 3. If no: publish to Redis Pub/Sub for SSE delivery
```

**Digest Batching (Phase 3 — Scheduler):**
```java
// NotificationDigestScheduler.processDigestBatch()  [@Scheduled hourly]
// 1. Find jobs with status=SCHEDULED and scheduledFor <= now
// 2. Group by recipient + channel
// 3. Build single digest message with count summary
// 4. Publish to Kafka + mark all jobs as SENT
```

---

## Entity Structure

Five entities make up the notification system. The diagram below shows how they are connected:

```
┌──────────────────────┐              ┌──────────────────────────────┐
│   NotificationEvent  │              │     NotificationTemplate     │
├──────────────────────┤              ├──────────────────────────────┤
│ id                   │              │ id                           │
│ event_type ──────────┼── lookup ───►│ name  (enum.getTemplateName) │
│  (NotificationEvent  │  (at render) │ channel  (IN_APP | EMAIL)   │
│   Type enum)         │              │ subject                      │
│ entity_type          │              │ content  ({{var}} syntax)   │
│ entity_id            │              │ variables (JSON array)       │
│ payload (JSON)       │              │ locale / version             │
│ severity             │              └──────────────────────────────┘
│ occurred_at          │
│ created_at           │
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
│ channel              │              │  (NotificationEventType      │
│ status               │              │   enum, or ALL)              │
│ dedup_key  (unique)  │              │ channel  (IN_APP | EMAIL)   │
│ retries              │              │ enabled                      │
│ sent_at              │              │ frequency (IMMEDIATE|DIGEST) │
│ scheduled_for        │              │ quiet_hours_start / end      │
│ created_at           │              │ severity_threshold           │
└──────────┬───────────┘              └──────────────────────────────┘
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
          │  notificationHelper.notifyEmployee(employeeId, NotificationEventType.INQUIRY_ASSIGNED, ...)
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
  │     (event_type enum, entity_type, entity_id, payload, severity)│
  │                                                                 │
  │  2. generateNotificationJobs(event)                             │
  │     ├─ Extract recipientType + recipientId from payload         │
  │     ├─ Create an  IN_APP  job  (always)                         │
  │     └─ Dedup check: skip if identical job already exists        │
  │        (dedupKey = "{eventId}-{recipientType}-{recipientId}-    │
  │                     {channel}")                                 │
  │                                                                 │
  │  3. Persist NotificationJob(s)  [status = PENDING]              │
  │                                                                 │
  │  4. kafkaProducer.publishJob(job)  [@Async, non-blocking]       │
  │     ├─ Render title + message from template                     │
  │     │   (lookup by eventType.getTemplateName() + channel)       │
  │     └─ Send NotificationJobMessage to Kafka topic               │
  └─────────────────────┬───────────────────────────────────────────┘
                        │
                        ▼
          ┌─────────────────────────┐
          │   Apache Kafka          │
          │                         │
          │  notification.jobs.     │
          │  inapp  (3 partitions)  │
          │                         │
          │  notification.jobs.     │
          │  email  (3 partitions)  │
          └────────────┬────────────┘
                       │
                       ▼
          ┌─────────────────────────┐
          │  NotificationKafka      │
          │  Consumer               │
          │  (manual-ack)           │
          │                         │
          │  Publishes JSON payload │
          │  to Redis Pub/Sub:      │
          │  notification:          │
          │  {TYPE}:{recipientId}   │
          └────────────┬────────────┘
                       │  Redis Pub/Sub
          ┌────────────┴────────────┐
          │  (all app instances)    │
          ▼                         ▼
  ┌───────────────┐         ┌────────────────────┐
  │  Notification │         │  NotificationSSE   │
  │  Redis        │         │  EmitterManager    │
  │  Message      │────────►│                    │
  │  Listener     │         │  Pushes SSE event  │
  └───────────────┘         │  to connected      │
                            │  browser tabs      │
                            └────────┬───────────┘
                                     │  text/event-stream
                                     ▼
                            ┌────────────────────┐
                            │  Browser           │
                            │                    │
                            │  EventSource       │
                            │  /stream/          │
                            │  EMPLOYEE/42       │
                            │                    │
                            │  User reads →      │
                            │  PUT /{id}/read    │
                            │  sentAt = now      │
                            └────────────────────┘
```

---

## Kafka & Redis Deep Dive

How Apache Kafka and Redis work together to deliver notifications reliably across multiple app instances:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PRODUCER SIDE  (App Instance A)                     │
│                                                                             │
│  NotificationKafkaProducer.publishJob()   [@Async / notificationExecutor]  │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │  NotificationJobMessage                                            │    │
│  │  ─────────────────────────────────────────────────────────────    │    │
│  │  jobId │ eventType │ recipientType │ recipientId │ channel        │    │
│  │  title │ message   │ payload (Map) │ severity    │ occurredAt     │    │
│  └──────────────────────────────┬─────────────────────────────────────┘    │
│                                 │  key = recipientId (ensures ordering)     │
└─────────────────────────────────┼───────────────────────────────────────────┘
                                  │ idempotent producer  acks=all  retries=3
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              APACHE KAFKA                                   │
│                                                                             │
│  Topic: notification.jobs.inapp          Topic: notification.jobs.email    │
│  ┌──────────────────────────────────┐    ┌──────────────────────────────┐  │
│  │  Partition 0  │ key hash % 3     │    │  Partition 0                 │  │
│  │  ─────────────────────────────── │    │  ─────────────────────────── │  │
│  │  [msg][msg][msg]                 │    │  (ready for email workers)   │  │
│  │                                  │    └──────────────────────────────┘  │
│  │  Partition 1                     │                                       │
│  │  ─────────────────────────────── │    • 3 partitions per topic          │
│  │  [msg][msg]                      │    • KRaft mode (no Zookeeper)       │
│  │                                  │    • Auto-create topics enabled      │
│  │  Partition 2                     │    • Messages retained 7 days        │
│  │  ─────────────────────────────── │    • replication factor = 1 (dev)   │
│  │  [msg]                           │                                       │
│  └──────────────────────────────────┘                                       │
└────────────────────────┬────────────────────────────────────────────────────┘
                         │  consumer group: notification-inapp-consumer-group
          ┌──────────────┴──────────────────┐
          │  (any available app instance)   │
          ▼                                 ▼
┌──────────────────────┐         ┌──────────────────────┐
│  App Instance A      │         │  App Instance B      │
│                      │         │                      │
│  NotificationKafka   │         │  NotificationKafka   │
│  Consumer            │         │  Consumer            │
│                      │         │                      │
│  1. Deserialize      │         │  1. Deserialize      │
│     message          │         │     message          │
│  2. Publish to       │         │  2. Publish to       │
│     Redis Pub/Sub    │         │     Redis Pub/Sub    │
│  3. ack.acknowledge()│         │  3. ack.acknowledge()│
│     (manual ACK)     │         │     (manual ACK)     │
└──────────┬───────────┘         └──────────┬───────────┘
           │                                │
           └──────────────┬─────────────────┘
                          │  PUBLISH  notification:EMPLOYEE:42
                          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            REDIS PUB/SUB                                    │
│                                                                             │
│  Channel pattern:  notification:{RECIPIENT_TYPE}:{recipientId}             │
│                                                                             │
│  notification:EMPLOYEE:42    notification:CLIENT:7    notification:*:*     │
│       │                            │                        (subscribed)    │
│       └────────────────────────────┘                                        │
│                          │                                                  │
│     Fanout to ALL app instances simultaneously                              │
└──────────────────────────┬──────────────────────────────────────────────────┘
          ┌─────────────────┴──────────────────┐
          ▼                                    ▼
┌──────────────────────┐            ┌──────────────────────┐
│  App Instance A      │            │  App Instance B      │
│                      │            │                      │
│  Notification        │            │  Notification        │
│  RedisMessage        │            │  RedisMessage        │
│  Listener            │            │  Listener            │
│       │              │            │       │              │
│       ▼              │            │       ▼              │
│  SseEmitter          │            │  SseEmitter          │
│  Manager             │            │  Manager             │
│                      │            │                      │
│  Tab 1 ──► SSE push  │            │  Tab 3 ──► SSE push  │
│  Tab 2 ──► SSE push  │            │  (same user,         │
│                      │            │   different tab)     │
└──────────────────────┘            └──────────────────────┘
```

### Why This Design?

**Kafka handles durability and load distribution:**
- Messages are persisted to disk — if all app instances restart, no notifications are lost
- The consumer group (one group per topic) ensures each message is processed by exactly one instance, preventing duplicate deliveries
- Partitioning by `recipientId` keeps messages for the same recipient ordered and routed to the same partition
- Idempotent producer + `acks=all` guarantees the broker only acknowledges a write after it's fully persisted
- Manual acknowledgement (`MANUAL_IMMEDIATE`) means a message is only acked after it has been successfully handed off to Redis — a crash before ack causes Kafka to redeliver

**Redis Pub/Sub bridges the Kafka consumer to SSE connections:**
- A Kafka consumer may run on Instance B, but the user's browser tab is connected to Instance A via SSE
- Publishing to a Redis channel broadcasts to all subscribers on all instances simultaneously
- The `NotificationRedisMessageListener` (subscribed to `notification:*:*`) picks it up on every instance and calls `SseEmitterManager.sendToRecipient()` — only the instance that has an active emitter for that recipient will actually push the event
- Multiple browser tabs of the same user are all served because `SseEmitterManager` holds a list of emitters per recipient key

**SSE suits the notification use-case better than WebSocket:**
- Notifications are strictly server → client (unidirectional) — no need for bidirectional WebSocket
- Native browser reconnect via `EventSource` — the client automatically re-establishes the connection after a restart or network blip
- Works over plain HTTP/1.1 and HTTP/2; no protocol upgrade required
- Stateless from the server perspective — each reconnect just registers a fresh emitter

**Message flow guarantees:**
```
Producer                Kafka               Consumer            Redis           Browser
   │                      │                    │                  │               │
   │── publish(msg) ──────►│                    │                  │               │
   │   (acks=all)          │── deliver(msg) ────►│                  │               │
   │                       │                    │── publish ───────►│               │
   │                       │                    │   (channel)       │── SSE push ───►│
   │                       │                    │◄─ ack() ──────────│               │
   │                       │◄─ commit offset ───│                  │               │
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

All event types are defined in the `NotificationEventType` enum. The enum name is also used to derive the template lookup key via `getTemplateName()` (e.g. `INQUIRY_ASSIGNED` → `"inquiry_assigned"`).

| Category | Enum Constant | Template Name | Description |
|----------|---------------|---------------|-------------|
| **Inquiries** | `INQUIRY_CREATED` | `inquiry_created` | New inquiry created |
| | `INQUIRY_ASSIGNED` | `inquiry_assigned` | Inquiry assigned to employee |
| | `INQUIRY_UPDATED` | `inquiry_updated` | Inquiry status changed |
| **Reservations** | `RESERVATION_CREATED` | `reservation_created` | New reservation made |
| | `RESERVATION_EXPIRING` | `reservation_expiring` | Reservation about to expire |
| | `RESERVATION_CANCELLED` | `reservation_cancelled` | Reservation cancelled |
| **Sales** | `SALE_COMPLETED` | `sale_completed` | Sale transaction completed |
| | `SALE_PENDING` | `sale_pending` | Sale pending approval |
| **Inspections** | `INSPECTION_DUE` | `inspection_due` | Vehicle inspection due |
| | `INSPECTION_COMPLETED` | `inspection_completed` | Inspection completed |
| | `INSPECTION_FAILED` | `inspection_failed` | Inspection failed |
| **Tasks** | `TASK_ASSIGNED` | `task_assigned` | Task assigned to employee |
| | `TASK_DUE` | `task_due` | Task due soon |
| | `TASK_OVERDUE` | `task_overdue` | Task is overdue |
| | `TASK_COMPLETED` | `task_completed` | Task completed |
| **Payments** | `PAYMENT_RECEIVED` | `payment_received` | Payment received |
| | `PAYMENT_OVERDUE` | `payment_overdue` | Payment overdue |
| **Wildcard** | `ALL` | — | Matches any event type in preferences |

### Adding Custom Events

Add the new constant to `NotificationEventType` and create a matching template:

```java
// 1. Add to NotificationEventType enum
public enum NotificationEventType {
    // ... existing values ...
    CUSTOM_EVENT;
}

// 2. Send the notification from any service
notificationHelper.notifyEmployee(
    employeeId,
    NotificationEventType.CUSTOM_EVENT,
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
    NotificationEventType.INQUIRY_ASSIGNED,
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
    NotificationEventType.RESERVATION_CREATED,
    "RESERVATION",
    reservationId,
    Map.of("reservationId", reservationId, "vehicleModel", "Honda Activa")
);

// Send a critical notification
notificationHelper.sendCriticalNotification(
    NotificationEventType.INSPECTION_FAILED,
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
    .eventType(NotificationEventType.SALE_COMPLETED)
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
            NotificationEventType.INQUIRY_ASSIGNED,
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
            NotificationEventType.SALE_COMPLETED,
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
  "eventType":        "INQUIRY_ASSIGNED",
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
| `GET` | `/api/v1/notifications/stream/{type}/{id}` | SSE stream — real-time push (text/event-stream) |

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
// All supported event types (replaces dot-notation strings)
enum NotificationEventType {
    INQUIRY_CREATED, INQUIRY_ASSIGNED, INQUIRY_UPDATED,
    RESERVATION_CREATED, RESERVATION_EXPIRING, RESERVATION_CANCELLED,
    SALE_COMPLETED, SALE_PENDING,
    INSPECTION_DUE, INSPECTION_COMPLETED, INSPECTION_FAILED,
    TASK_ASSIGNED, TASK_DUE, TASK_OVERDUE, TASK_COMPLETED,
    PAYMENT_RECEIVED, PAYMENT_OVERDUE,
    ALL;  // wildcard for preferences

    // Returns the template table lookup key: INQUIRY_ASSIGNED → "inquiry_assigned"
    public String getTemplateName() { return this.name().toLowerCase(); }
}

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
- `NotificationService` / `NotificationServiceImpl` — core notification logic; persists events+jobs, triggers Kafka publish
- `NotificationEventHelper` — convenience facade (`notifyEmployee`, `notifyClient`, `sendCriticalNotification`, `sendNotification`)
- `NotificationTemplateService` / `NotificationTemplateServiceImpl` — template CRUD and rendering
- `NotificationPreferenceService` / `NotificationPreferenceServiceImpl` — preference management

### Messaging (Kafka + SSE)
- `NotificationKafkaProducer` — `@Async` publisher; renders template and sends `NotificationJobMessage` to the appropriate Kafka topic
- `NotificationKafkaConsumer` — manual-ack Kafka listener; re-publishes payload to Redis Pub/Sub channel
- `NotificationJobMessage` — serializable Kafka message payload (includes pre-rendered title + message)
- `NotificationRedisMessageListener` — Redis Pub/Sub listener; forwards payload to SSE emitters on this instance
- `NotificationSseEmitterManager` — thread-safe registry of active SSE connections; fans out events to browser tabs

### Controllers
- `NotificationController` — notification retrieval, read/unread management, and SSE stream endpoint
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
2. **Use enum constants** — always reference `NotificationEventType.INQUIRY_ASSIGNED` etc.; never pass raw strings
3. **Consistent variable names** — use the same variable names across events and templates (see Variable Reference above)
4. **Kafka is non-blocking** — `NotificationKafkaProducer.publishJob()` runs on the `notificationExecutor` thread pool; the calling transaction is unaffected by Kafka failures
5. **SSE reconnects automatically** — browsers using `EventSource` will reconnect on disconnect; the emitter is registered fresh on each reconnect
6. **Template versioning** — increment `version` when making significant content changes; the producer always uses the latest template at publish time
7. **Respect preferences** — the system checks `NotificationPreference` before creating jobs; do not bypass this by creating jobs directly

---

## Swagger UI

`http://localhost:8080/api/v1/swagger-ui.html`

# Dashboard System

Role-based dashboard providing personalized metrics and actions for each user role.

## API Endpoints

| Role | Endpoint | Access |
|------|----------|--------|
| Admin | `GET /api/v1/dashboard/admin` | `SUPER_ADMIN`, `ADMIN` |
| Sales | `GET /api/v1/dashboard/sales` | `SALES`, `ADMIN`, `SUPER_ADMIN` |
| Inspector | `GET /api/v1/dashboard/inspector` | `INSPECTOR`, `ADMIN`, `SUPER_ADMIN` |
| Finance | `GET /api/v1/dashboard/finance` | `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| Store Manager | `GET /api/v1/dashboard/store-manager` | `STORE_MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| Current User | `GET /api/v1/dashboard/me` | All authenticated users (auto-detects role) |

## Enums Used

### CarStatus
`AVAILABLE`, `RESERVED`, `SOLD`, `MAINTENANCE`, `IN_TRANSIT`

### InquiryStatus
`OPEN`, `IN_PROGRESS`, `RESPONDED`, `CONVERTED`, `CLOSED`

### ReservationStatus
`PENDING`, `CONFIRMED`, `CANCELLED`, `EXPIRED`, `COMPLETED`

### TaskPriority
`LOW`, `MEDIUM`, `HIGH`, `URGENT`

### TaskStatus
`PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

### NotificationSeverity
`INFO`, `WARNING`, `ERROR`, `SUCCESS`

### TransactionType
`PURCHASE`, `SALE`, `DEPOSIT`, `REFUND`, `EXPENSE`

### ScopeType
`LOCATION`, `DEPARTMENT`, `ASSIGNMENT`, `ALL`

## Dashboard Data by Role

### ADMIN Dashboard
**Widgets**: Overview stats, revenue metrics, inventory health, employee performance, system alerts, notifications

**Key Metrics**:
- Total/available/reserved/sold cars
- Monthly/YTD revenue, revenue trends
- Inventory by status, total value, aging inventory
- Employee performance, top performers
- Expiring reservations, inspections due, capacity warnings

### SALES Dashboard
**Widgets**: Personal stats, sales pipeline, performance metrics, quick actions, client activity, available inventory

**Key Metrics**:
- Active inquiries, conversions, sales, commission
- Inquiries by status, follow-ups due
- Monthly sales vs target, conversion rate
- Pending responses, expiring reservations

### INSPECTOR Dashboard
**Widgets**: Inspection queue, personal stats, vehicle status, assigned tasks, locations, recent work

**Key Metrics**:
- Pending/scheduled/overdue inspections
- Inspections completed, pass rate, avg time
- Vehicles needing inspection, failed inspections
- High priority tasks, tasks due today

### FINANCE Dashboard
**Widgets**: Financial overview, transactions, profitability, aging analysis, budget tracking

**Key Metrics**:
- Total revenue, expenses, net profit, profit margin
- Pending transactions, recent payments
- Avg profit per vehicle, margins, ROI
- Overdue payments, pending deposits
- Budget spent vs remaining

### STORE_MANAGER Dashboard
**Widgets**: Location overview, vehicle distribution, movement activity, capacity alerts, maintenance status

**Key Metrics**:
- Total/managed locations, capacity, utilization
- Vehicles by location and status
- Recent movements, pending transfers
- Near-full/underutilized locations
- Vehicles in maintenance, avg maintenance time

## Data Scopes

Dashboards respect employee data scopes:
- **LOCATION**: Only data from assigned locations
- **DEPARTMENT**: Only data from assigned departments  
- **ASSIGNMENT**: Only assigned resources (inquiries, tasks, etc.)
- **ALL**: No restrictions (SUPER_ADMIN only)

## Response Structure

All dashboards return role-specific data objects. Common patterns:

**Notifications** (all roles):
```json
{
  "unreadCount": 12,
  "recent": [{ "id", "type", "subject", "body", "severity", "createdAt", "isRead" }]
}
```

**Activities** (admin roles):
```json
{
  "recentActivities": [{ "id", "type", "description", "entityType", "entityId", "timestamp", "performedBy" }]
}
```

## Authorization

Each endpoint validates:
1. User authentication via JWT
2. Role-based access via `@PreAuthorize`
3. Data scope filtering applied to all queries
4. Resource-level ACLs respected

## Caching

- Overview stats: 5 min TTL
- Revenue metrics: 10 min TTL
- Personal stats: 2 min TTL
- Notifications: Real-time (no cache)
- Cache invalidated on relevant entity changes

# Role-Based Dashboard System

## Overview

WheelShift Pro implements a comprehensive role-based dashboard system that provides personalized views and metrics for different user roles. Each dashboard is optimized to display the most relevant information for the user's responsibilities.

## Architecture

### Components

1. **Dashboard Widgets** - Independent data sections
2. **Role-Based Endpoints** - Customized API responses per role
3. **Real-Time Data** - Live statistics and notifications
4. **Action Items** - Pending tasks and alerts

### Design Principles

- **Role-Specific Data**: Each role sees only relevant metrics
- **Independent API Calls**: Each widget makes its own API call for better performance
- **Granular Permissions**: Data scopes and ACLs respected in all responses
- **Cacheable Metrics**: Statistics cached for performance (5-10 min TTL)

## Dashboard by Role

### SUPER_ADMIN / ADMIN Dashboard

**Endpoint**: `GET /api/v1/dashboard/admin`

**Widgets**:
- **Overview Stats**: Total cars, active reservations, pending inquiries, employee count
- **Revenue Metrics**: Monthly revenue, YTD revenue, revenue trend
- **Inventory Health**: Cars by status, low stock locations, aging inventory
- **Recent Activities**: Latest sales, new cars, status changes
- **Employee Performance**: Top performers, activity summary
- **System Alerts**: Expiring reservations, inspection due, capacity warnings
- **Notifications**: Unread count and recent items

**Response Structure**:
```json
{
  "overview": {
    "totalCars": 150,
    "availableCars": 85,
    "reservedCars": 15,
    "soldCarsThisMonth": 12,
    "activeInquiries": 28,
    "activeReservations": 15,
    "totalEmployees": 25,
    "activeEmployees": 23
  },
  "revenue": {
    "totalRevenue": 2500000.00,
    "monthlyRevenue": 450000.00,
    "ytdRevenue": 3200000.00,
    "averageSalePrice": 185000.00,
    "revenueTrend": [...monthly data...]
  },
  "inventory": {
    "byStatus": {
      "AVAILABLE": 85,
      "RESERVED": 15,
      "SOLD": 12,
      "MAINTENANCE": 8
    },
    "totalValue": 15000000.00,
    "avgAge": 45,
    "agingInventory": [...]
  },
  "recentActivities": [...],
  "topEmployees": [...],
  "alerts": {
    "expiringReservations": 3,
    "inspectionsDue": 5,
    "locationCapacityWarnings": 2
  },
  "notifications": {
    "unreadCount": 12,
    "recent": [...]
  }
}
```

---

### SALES Dashboard

**Endpoint**: `GET /api/v1/dashboard/sales`

**Widgets**:
- **Personal Stats**: My inquiries, conversions, sales, commission
- **Pipeline**: Inquiries by status, active reservations, follow-up needed
- **Performance**: Monthly sales, target vs actual, conversion rate
- **Quick Actions**: Pending responses, follow-ups due today
- **Client Activity**: New clients, top clients, recent interactions
- **Available Inventory**: Cars ready to sell, featured listings
- **Notifications**: My notifications and alerts

**Response Structure**:
```json
{
  "personalStats": {
    "activeInquiries": 12,
    "convertedInquiries": 8,
    "activeReservations": 5,
    "salesThisMonth": 4,
    "commissionEarned": 45000.00,
    "conversionRate": 66.7
  },
  "pipeline": {
    "inquiriesByStatus": {
      "OPEN": 3,
      "IN_PROGRESS": 7,
      "RESPONDED": 2
    },
    "followUpToday": 5,
    "followUpThisWeek": 12
  },
  "performance": {
    "monthlySales": 4,
    "monthlyTarget": 6,
    "targetProgress": 66.7,
    "salesTrend": [...],
    "avgSaleValue": 190000.00
  },
  "quickActions": {
    "pendingResponses": 3,
    "followUpsDue": 5,
    "expiringReservations": 2
  },
  "availableInventory": {
    "totalAvailable": 85,
    "newArrivals": 8,
    "featured": [...]
  },
  "notifications": {
    "unreadCount": 5,
    "recent": [...]
  }
}
```

---

### INSPECTOR Dashboard

**Endpoint**: `GET /api/v1/dashboard/inspector`

**Widgets**:
- **Inspection Queue**: Pending inspections, scheduled today/this week
- **Personal Stats**: Inspections completed, pass rate, avg time
- **Vehicle Status**: Cars needing inspection, failed inspections
- **Assigned Tasks**: My tasks, priority items
- **Locations**: Vehicles by location I manage
- **Recent Work**: Last 10 inspections performed
- **Notifications**: Inspection-related alerts

**Response Structure**:
```json
{
  "inspectionQueue": {
    "pendingInspections": 12,
    "scheduledToday": 4,
    "scheduledThisWeek": 15,
    "overdue": 2
  },
  "personalStats": {
    "completedThisMonth": 28,
    "totalCompleted": 145,
    "passRate": 82.5,
    "avgInspectionTime": 45,
    "avgRepairCost": 15000.00
  },
  "vehicleStatus": {
    "needingInspection": 18,
    "failedInspections": 5,
    "inMaintenance": 8
  },
  "assignedTasks": {
    "total": 8,
    "highPriority": 2,
    "dueToday": 3
  },
  "locationSummary": [...],
  "recentInspections": [...],
  "notifications": {
    "unreadCount": 3,
    "recent": [...]
  }
}
```

---

### FINANCE Dashboard

**Endpoint**: `GET /api/v1/dashboard/finance`

**Widgets**:
- **Financial Overview**: Revenue, expenses, profit, cash flow
- **Transaction Summary**: Pending transactions, recent payments
- **Profitability**: Profit by vehicle, margins, ROI
- **Aging Analysis**: Payment delays, pending deposits
- **Reports Ready**: Recent reports, scheduled exports
- **Budget Tracking**: Expenses vs budget
- **Notifications**: Finance-related alerts

**Response Structure**:
```json
{
  "financialOverview": {
    "totalRevenue": 2500000.00,
    "totalExpenses": 1800000.00,
    "netProfit": 700000.00,
    "profitMargin": 28.0,
    "cashFlow": 450000.00
  },
  "transactions": {
    "pendingCount": 8,
    "pendingAmount": 125000.00,
    "completedThisMonth": 45,
    "recentTransactions": [...]
  },
  "profitability": {
    "avgProfitPerVehicle": 58000.00,
    "avgMargin": 31.5,
    "bestPerformingCategory": "SUV",
    "vehicleProfitability": [...]
  },
  "aging": {
    "overduePayments": 3,
    "overdueAmount": 85000.00,
    "pendingDeposits": 5
  },
  "budgetTracking": {
    "totalBudget": 2000000.00,
    "spent": 1800000.00,
    "remaining": 200000.00,
    "utilizationRate": 90.0
  },
  "notifications": {
    "unreadCount": 7,
    "recent": [...]
  }
}
```

---

### STORE_MANAGER Dashboard

**Endpoint**: `GET /api/v1/dashboard/store-manager`

**Widgets**:
- **Location Overview**: My locations, capacity, utilization
- **Vehicle Distribution**: Cars by location and status
- **Movement Activity**: Recent moves, pending transfers
- **Capacity Alerts**: Near-full locations, space optimization
- **Maintenance Status**: Vehicles in maintenance at my locations
- **Location Performance**: Turnover rate, avg stay duration
- **Notifications**: Location-related alerts

**Response Structure**:
```json
{
  "locationOverview": {
    "totalLocations": 5,
    "managedLocations": 3,
    "totalCapacity": 250,
    "currentOccupancy": 180,
    "utilizationRate": 72.0
  },
  "vehicleDistribution": {
    "byLocation": {
      "Location A": 60,
      "Location B": 55,
      "Location C": 65
    },
    "byStatus": {
      "AVAILABLE": 120,
      "RESERVED": 25,
      "MAINTENANCE": 15,
      "SOLD": 20
    }
  },
  "movements": {
    "todayMovements": 4,
    "thisWeekMovements": 18,
    "pendingTransfers": 3,
    "recentMovements": [...]
  },
  "capacityAlerts": {
    "nearFullLocations": 2,
    "underutilizedLocations": 1,
    "recommendations": [...]
  },
  "maintenanceStatus": {
    "vehiclesInMaintenance": 15,
    "byLocation": {...},
    "avgMaintenanceTime": 5.5
  },
  "performance": {
    "avgTurnoverDays": 42,
    "fastestMovingCategory": "Sedan",
    "slowestMovingCategory": "Luxury"
  },
  "notifications": {
    "unreadCount": 4,
    "recent": [...]
  }
}
```

---

## Common Dashboard Features

### Notifications Widget

All dashboards include a notifications section:

```json
"notifications": {
  "unreadCount": 12,
  "recent": [
    {
      "id": 1,
      "type": "inquiry.assigned",
      "subject": "New inquiry assigned",
      "body": "Inquiry #123 has been assigned to you",
      "entityType": "INQUIRY",
      "entityId": "123",
      "severity": "INFO",
      "createdAt": "2025-12-22T10:30:00",
      "isRead": false
    }
  ]
}
```

### Recent Activities

Common structure for activity logs:

```json
"recentActivities": [
  {
    "id": 1,
    "type": "SALE",
    "description": "Car #CAR-123 sold for $185,000",
    "entityType": "SALE",
    "entityId": "45",
    "timestamp": "2025-12-22T09:15:00",
    "performedBy": "John Doe"
  }
]
```

---

## API Endpoints

### Role-Specific Dashboards

```bash
# Admin Dashboard
GET /api/v1/dashboard/admin

# Sales Dashboard
GET /api/v1/dashboard/sales

# Inspector Dashboard
GET /api/v1/dashboard/inspector

# Finance Dashboard
GET /api/v1/dashboard/finance

# Store Manager Dashboard
GET /api/v1/dashboard/store-manager
```

### Widget-Level Endpoints (Optional Granular Fetching)

```bash
# Get specific widget data
GET /api/v1/dashboard/widgets/overview
GET /api/v1/dashboard/widgets/revenue
GET /api/v1/dashboard/widgets/inventory
GET /api/v1/dashboard/widgets/notifications

# With role parameter
GET /api/v1/dashboard/widgets/pipeline?role=SALES
```

---

## Authorization

### Role Access

- **SUPER_ADMIN**: Access all dashboards
- **ADMIN**: Access admin dashboard + limited access to others
- **SALES**: Access sales dashboard only
- **INSPECTOR**: Access inspector dashboard only
- **FINANCE**: Access finance dashboard only
- **STORE_MANAGER**: Access store manager dashboard only

### Data Scopes

Dashboard data respects data scopes:
- **LOCATION** scope: Only shows data from assigned locations
- **DEPARTMENT** scope: Only shows data from assigned departments
- **ASSIGNMENT** scope: Only shows assigned resources

### Implementation

```java
@PreAuthorize("@authService.hasAnyRole(authentication.name, 'ADMIN', 'SUPER_ADMIN')")
@GetMapping("/admin")
public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
    // Implementation with data scope filtering
}
```

---

## Caching Strategy

### Cache TTLs

- **Overview Stats**: 5 minutes
- **Revenue Metrics**: 10 minutes
- **Inventory Data**: 5 minutes
- **Personal Stats**: 2 minutes
- **Notifications**: No cache (real-time)
- **Recent Activities**: 1 minute

### Cache Keys

```
dashboard:admin:{employeeId}
dashboard:sales:{employeeId}
dashboard:inspector:{employeeId}
dashboard:finance:{employeeId}
dashboard:store-manager:{employeeId}
```

### Invalidation

Cache invalidated on:
- New sale/reservation/inquiry
- Status changes
- Vehicle movements
- Employee actions

---

## Frontend Integration

### React Example

```typescript
// hooks/useDashboard.ts
export function useDashboard(role: RoleType) {
  return useQuery({
    queryKey: ['dashboard', role],
    queryFn: () => fetchDashboard(role),
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchInterval: 5 * 60 * 1000 // Auto-refresh every 5 min
  });
}

// components/AdminDashboard.tsx
function AdminDashboard() {
  const { data, isLoading } = useDashboard('ADMIN');
  
  return (
    <div className="dashboard-grid">
      <OverviewWidget data={data.overview} />
      <RevenueWidget data={data.revenue} />
      <InventoryWidget data={data.inventory} />
      <NotificationsWidget data={data.notifications} />
    </div>
  );
}
```

### Widget Components

Each widget makes independent API calls:

```typescript
function NotificationsWidget({ employeeId }) {
  const { data } = useQuery({
    queryKey: ['notifications', employeeId],
    queryFn: () => fetchNotifications(employeeId),
    refetchInterval: 30000 // 30 seconds
  });
  
  return <NotificationList notifications={data} />;
}
```

---

## Performance Considerations

### Optimization Strategies

1. **Parallel Loading**: All widgets load independently
2. **Lazy Loading**: Below-the-fold widgets load on scroll
3. **Skeleton States**: Show placeholders during loading
4. **Error Boundaries**: Isolated widget failures don't crash dashboard
5. **Debounced Refresh**: Prevent rapid re-fetches

### Database Optimization

- Indexed columns for dashboard queries
- Materialized views for complex aggregations
- Redis caching for expensive calculations
- Read replicas for dashboard queries

---

## Error Handling

### Graceful Degradation

If a widget fails:
- Show fallback UI
- Log error silently
- Don't block other widgets
- Provide retry button

### Example Response

```json
{
  "status": "partial_success",
  "data": {
    "overview": {...},
    "revenue": {...},
    "inventory": null
  },
  "errors": [
    {
      "widget": "inventory",
      "error": "Service temporarily unavailable",
      "retryable": true
    }
  ]
}
```

---

## Monitoring & Analytics

### Metrics to Track

- Dashboard load times per role
- Widget failure rates
- Cache hit rates
- API response times
- User engagement per widget

### Logging

```java
log.info("Dashboard loaded: role={}, employeeId={}, duration={}ms", 
    role, employeeId, duration);
```

---

## Future Enhancements

- **Custom Dashboards**: Allow users to customize widget layouts
- **Export Capabilities**: Export dashboard data to PDF/Excel
- **Real-Time Updates**: WebSocket-based live data updates
- **Mobile Optimization**: Responsive dashboard layouts
- **Dark Mode**: Theme support
- **Drill-Down**: Click widgets to see detailed reports
- **Comparison Views**: Compare periods, locations, employees
- **AI Insights**: Predictive analytics and recommendations

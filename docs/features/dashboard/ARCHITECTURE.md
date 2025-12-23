# Dashboard System Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend Layer                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  Admin   │  │  Sales   │  │Inspector │  │ Finance  │  ...  │
│  │Dashboard │  │Dashboard │  │Dashboard │  │Dashboard │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
└───────┼─────────────┼─────────────┼─────────────┼──────────────┘
        │             │             │             │
        └─────────────┴─────────────┴─────────────┘
                      │
        ┌─────────────▼─────────────────────────┐
        │      API Gateway / Load Balancer       │
        │     /api/v1/dashboard/*                │
        └─────────────┬─────────────────────────┘
                      │
        ┌─────────────▼─────────────────────────┐
        │       Spring Security Filter           │
        │    (Authentication & Authorization)    │
        └─────────────┬─────────────────────────┘
                      │
        ┌─────────────▼─────────────────────────┐
        │      DashboardController              │
        │                                        │
        │  GET /admin      GET /sales           │
        │  GET /inspector  GET /finance         │
        │  GET /store-manager  GET /me          │
        └─────────────┬─────────────────────────┘
                      │
        ┌─────────────▼─────────────────────────┐
        │      DashboardService                  │
        │                                        │
        │  • buildOverviewStats()               │
        │  • buildRevenueMetrics()              │
        │  • buildSalesPipeline()               │
        │  • buildInspectionQueue()             │
        │  • buildFinancialOverview()           │
        │  • buildLocationOverview()            │
        └─────────────┬─────────────────────────┘
                      │
        ┌─────────────▼─────────────────────────┐
        │         Repository Layer               │
        │                                        │
        │  CarRepo  SaleRepo  InquiryRepo       │
        │  ReservationRepo  EmployeeRepo        │
        │  TransactionRepo  LocationRepo        │
        │  InspectionRepo   TaskRepo            │
        └─────────────┬─────────────────────────┘
                      │
        ┌─────────────▼─────────────────────────┐
        │           MySQL Database               │
        │                                        │
        │  Cars  Sales  Inquiries  Reservations │
        │  Employees  Transactions  Locations   │
        │  Inspections  Tasks  Notifications    │
        └────────────────────────────────────────┘
```

## Dashboard Data Flow

```
1. User Request
   └─> GET /api/v1/dashboard/me
       └─> DashboardController.getCurrentUserDashboard()
           │
           ├─> Extract employeeId from Authentication
           ├─> Fetch employee roles from DB
           └─> Detect primary role
               │
               ├─> If ADMIN/SUPER_ADMIN
               │   └─> DashboardService.getAdminDashboard()
               │       ├─> buildOverviewStats()
               │       ├─> buildRevenueMetrics()
               │       ├─> buildInventoryHealth()
               │       ├─> buildRecentActivities()
               │       ├─> buildTopEmployees()
               │       ├─> buildSystemAlerts()
               │       └─> buildNotificationsWidget()
               │
               ├─> If SALES
               │   └─> DashboardService.getSalesDashboard()
               │       ├─> buildSalesPersonalStats()
               │       ├─> buildSalesPipeline()
               │       ├─> buildSalesPerformance()
               │       ├─> buildQuickActions()
               │       ├─> buildInventorySummary()
               │       └─> buildNotificationsWidget()
               │
               └─> Return role-specific dashboard response
```

## Widget Architecture

```
AdminDashboardResponse
├── OverviewStats
│   ├── totalCars
│   ├── availableCars
│   ├── reservedCars
│   ├── soldCarsThisMonth
│   ├── activeInquiries
│   ├── activeReservations
│   ├── totalEmployees
│   └── activeEmployees
│
├── RevenueMetrics
│   ├── totalRevenue
│   ├── monthlyRevenue
│   ├── ytdRevenue
│   ├── averageSalePrice
│   └── revenueTrend[]
│       ├── month
│       ├── monthName
│       ├── revenue
│       └── salesCount
│
├── InventoryHealth
│   ├── byStatus{}
│   ├── totalValue
│   ├── avgAge
│   └── agingInventory[]
│
├── RecentActivities[]
│   ├── id
│   ├── type
│   ├── description
│   ├── entityType
│   ├── entityId
│   ├── timestamp
│   └── performedBy
│
├── TopEmployees[]
│   ├── employeeId
│   ├── employeeName
│   ├── position
│   ├── salesCount
│   ├── totalCommission
│   └── totalRevenue
│
├── SystemAlerts
│   ├── expiringReservations
│   ├── inspectionsDue
│   ├── locationCapacityWarnings
│   └── details[]
│
└── NotificationsWidget
    ├── unreadCount
    └── recent[]
        ├── id
        ├── type
        ├── subject
        ├── body
        ├── entityType
        ├── entityId
        ├── severity
        ├── createdAt
        └── isRead
```

## Role-Based Access Matrix

```
┌─────────────────┬───────┬───────┬──────────┬─────────┬──────────────┐
│ Dashboard       │ ADMIN │ SALES │INSPECTOR │ FINANCE │STORE_MANAGER │
├─────────────────┼───────┼───────┼──────────┼─────────┼──────────────┤
│ /admin          │  ✓    │   ✗   │    ✗     │    ✗    │      ✗       │
│ /sales          │  ✓    │   ✓   │    ✗     │    ✗    │      ✗       │
│ /inspector      │  ✓    │   ✗   │    ✓     │    ✗    │      ✗       │
│ /finance        │  ✓    │   ✗   │    ✗     │    ✓    │      ✗       │
│ /store-manager  │  ✓    │   ✗   │    ✗     │    ✗    │      ✓       │
│ /me (auto)      │  ✓    │   ✓   │    ✓     │    ✓    │      ✓       │
└─────────────────┴───────┴───────┴──────────┴─────────┴──────────────┘

Note: SUPER_ADMIN has access to all dashboards
```

## Caching Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Request Flow with Cache                   │
└─────────────────────────────────────────────────────────────┘

User Request
    │
    ▼
┌─────────────────┐
│ Check Redis     │ Key: dashboard:admin:{employeeId}
│ Cache           │ TTL: 5 minutes
└────┬───┬────────┘
     │   │
  HIT│   │MISS
     │   │
     │   ▼
     │ ┌──────────────────┐
     │ │ Query Database   │
     │ │ Build Dashboard  │
     │ └────┬─────────────┘
     │      │
     │      ▼
     │ ┌──────────────────┐
     │ │ Store in Cache   │
     │ │ Set TTL          │
     │ └────┬─────────────┘
     │      │
     └──────┘
            │
            ▼
     Return Dashboard


Cache Invalidation Triggers:
• New sale created      → Invalidate all dashboards
• Car status changed    → Invalidate admin, sales, store-manager
• Inquiry assigned      → Invalidate sales dashboard
• Reservation created   → Invalidate admin, sales
• Transaction recorded  → Invalidate admin, finance
```

## Performance Optimization

```
Query Optimization Strategy:

1. Indexed Columns
   ├── cars(status)
   ├── cars(purchase_date)
   ├── sales(sale_date, employee_id)
   ├── inquiries(assigned_employee_id, status)
   └── reservations(status, expiry_date)

2. Query Patterns
   ├── Count queries: SELECT COUNT(*) WHERE ...
   ├── Aggregations: SELECT SUM/AVG/GROUP BY ...
   ├── Top-N queries: ORDER BY ... LIMIT 10
   └── Date range: WHERE date BETWEEN ... AND ...

3. Lazy Loading
   ├── Load widgets independently
   ├── Parallel queries where possible
   └── Paginate large result sets

4. Caching Layers
   ├── Application cache (5-10 min)
   ├── Query result cache
   └── Entity cache
```

## Frontend Component Structure

```
Dashboard Component Hierarchy:

App
└── DashboardPage
    ├── DashboardHeader
    │   ├── UserInfo
    │   └── NotificationBell (unreadCount)
    │
    └── DashboardGrid (role-specific layout)
        ├── OverviewWidget
        │   ├── StatCard (totalCars)
        │   ├── StatCard (availableCars)
        │   ├── StatCard (reservedCars)
        │   └── StatCard (soldCarsThisMonth)
        │
        ├── RevenueWidget
        │   ├── RevenueChart (trend)
        │   └── RevenueStats
        │
        ├── InventoryWidget
        │   ├── StatusBreakdown
        │   └── AgingInventoryTable
        │
        ├── ActivitiesWidget
        │   └── ActivityTimeline
        │
        ├── TopEmployeesWidget
        │   └── LeaderboardList
        │
        ├── AlertsWidget
        │   └── AlertList
        │
        └── NotificationsWidget
            └── NotificationList

Each Widget:
• Independent data fetching
• Own loading state
• Error boundary
• Refresh capability
```

## Error Handling Flow

```
Error Scenarios:

1. Authentication Error (401)
   └─> Redirect to login
       └─> After login, return to dashboard

2. Authorization Error (403)
   └─> Show "Access Denied" message
       └─> Suggest contacting admin

3. Widget Load Error
   └─> Show error in that widget only
       └─> Other widgets continue working
           └─> Provide retry button

4. Network Error
   └─> Show retry button
       └─> Use cached data if available
           └─> Indicate data is stale

5. Server Error (500)
   └─> Log error details
       └─> Show user-friendly message
           └─> Provide support contact
```

## Monitoring & Observability

```
Metrics to Track:

1. Performance Metrics
   ├── Dashboard load time (by role)
   ├── Widget load time (by type)
   ├── Cache hit rate
   ├── Database query time
   └── API response time (p50, p95, p99)

2. Usage Metrics
   ├── Dashboard views (by role)
   ├── Widget interactions
   ├── Refresh frequency
   └── Peak usage times

3. Error Metrics
   ├── Error rate (by endpoint)
   ├── Widget failure rate
   ├── Cache miss rate
   └── Timeout errors

Logging Format:
[timestamp] [level] [user] [action] [duration] [status]
2025-12-22 10:30:00 INFO user@example.com dashboard.admin 245ms SUCCESS
```

This architecture ensures scalability, maintainability, and excellent user experience across all dashboard types.

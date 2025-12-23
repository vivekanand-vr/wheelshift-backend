# Dashboard API Response Structures

Complete reference for all dashboard API responses to facilitate frontend integration.

## Table of Contents

1. [Admin Dashboard](#admin-dashboard)
2. [Sales Dashboard](#sales-dashboard)
3. [Inspector Dashboard](#inspector-dashboard)
4. [Finance Dashboard](#finance-dashboard)
5. [Store Manager Dashboard](#store-manager-dashboard)
6. [Current User Dashboard](#current-user-dashboard)

---

## Admin Dashboard

**Endpoint:** `GET /api/v1/dashboard/admin`  
**Authorization:** `ADMIN`, `SUPER_ADMIN`

### Response Structure

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "overview": {
      "totalCars": 450,
      "availableCars": 185,
      "reservedCars": 45,
      "soldCarsThisMonth": 28,
      "activeInquiries": 67,
      "activeReservations": 45,
      "totalEmployees": 42,
      "activeEmployees": 38
    },
    "revenue": {
      "totalRevenue": 15750000.00,
      "monthlyRevenue": 1425000.00,
      "ytdRevenue": 12600000.00,
      "averageSalePrice": 450000.00,
      "revenueTrend": [
        {
          "month": 1,
          "monthName": "Jan",
          "revenue": 1200000.00,
          "salesCount": 25
        },
        {
          "month": 2,
          "monthName": "Feb",
          "revenue": 980000.00,
          "salesCount": 22
        }
        // ... months 3-12
      ]
    },
    "inventory": {
      "byStatus": {
        "AVAILABLE": 185,
        "RESERVED": 45,
        "SOLD": 220,
        "MAINTENANCE": 12,
        "IN_TRANSIT": 8
      },
      "totalValue": 83250000.00,
      "avgAge": 45.5,
      "agingInventory": [
        {
          "carId": 1234,
          "vinNumber": "1HGCM82633A123456",
          "make": "Honda",
          "model": "Accord",
          "year": 2020,
          "daysInInventory": 120,
          "purchasePrice": 380000.00
        }
        // ... more items
      ]
    },
    "recentActivities": [
      {
        "id": 5678,
        "type": "SALE",
        "description": "Car 1HGCM82633A789012 sold for $450,000.00",
        "entityType": "SALE",
        "entityId": "5678",
        "timestamp": "2025-12-23T14:30:00",
        "performedBy": "John Doe"
      }
      // ... more activities
    ],
    "topEmployees": [
      {
        "employeeId": 101,
        "employeeName": "Jane Smith",
        "position": "Sales Executive",
        "salesCount": 8,
        "totalCommission": 120000.00,
        "totalRevenue": 3600000.00
      }
      // ... top 5 employees
    ],
    "alerts": {
      "expiringReservations": 3,
      "inspectionsDue": 8,
      "locationCapacityWarnings": 2,
      "details": [
        {
          "type": "EXPIRING_RESERVATIONS",
          "severity": "WARNING",
          "message": "3 reservation(s) expiring soon",
          "entityType": "RESERVATION",
          "entityId": null
        }
      ]
    },
    "notifications": {
      "unreadCount": 5,
      "recent": [
        {
          "id": 1001,
          "type": "SALE_COMPLETED",
          "subject": "Sale Completed",
          "body": "Sale #5678 completed successfully",
          "entityType": "SALE",
          "entityId": "5678",
          "severity": "INFO",
          "createdAt": "2025-12-23T14:30:00",
          "isRead": false
        }
        // ... up to 5 recent notifications
      ]
    }
  },
  "timestamp": "2025-12-23T15:45:30"
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `overview.totalCars` | Long | Total number of cars in inventory |
| `overview.availableCars` | Long | Cars available for sale |
| `overview.reservedCars` | Long | Cars currently reserved |
| `overview.soldCarsThisMonth` | Long | Sales completed this month |
| `revenue.totalRevenue` | BigDecimal | All-time revenue |
| `revenue.monthlyRevenue` | BigDecimal | Current month revenue |
| `revenue.ytdRevenue` | BigDecimal | Year-to-date revenue |
| `inventory.byStatus` | Map<String, Long> | Car count by status |
| `inventory.totalValue` | BigDecimal | Total inventory value (available cars) |
| `inventory.avgAge` | Double | Average days in inventory |
| `alerts.expiringReservations` | Long | Reservations expiring within 3 days |
| `notifications.unreadCount` | Long | Unread notification count |

---

## Sales Dashboard

**Endpoint:** `GET /api/v1/dashboard/sales`  
**Authorization:** `SALES`, `ADMIN`, `SUPER_ADMIN`

### Response Structure

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "personalStats": {
      "activeInquiries": 12,
      "convertedInquiries": 8,
      "activeReservations": 5,
      "salesThisMonth": 6,
      "commissionEarned": 90000.00,
      "conversionRate": 40.0
    },
    "pipeline": {
      "inquiriesByStatus": {
        "OPEN": 5,
        "IN_PROGRESS": 7,
        "RESPONDED": 8,
        "CLOSED": 15,
        "CONVERTED": 10
      },
      "followUpToday": 3,
      "followUpThisWeek": 8
    },
    "performance": {
      "monthlySales": 6,
      "monthlyTarget": 6,
      "targetProgress": 100.0,
      "salesTrend": [
        {
          "month": 1,
          "monthName": "Jan",
          "salesCount": 5,
          "revenue": 2250000.00
        }
        // ... months 2-12
      ],
      "avgSaleValue": 450000.00
    },
    "quickActions": {
      "pendingResponses": 5,
      "followUpsDue": 7,
      "expiringReservations": 2,
      "items": []
    },
    "availableInventory": {
      "totalAvailable": 185,
      "newArrivals": 12,
      "featured": [
        {
          "carId": 1234,
          "vinNumber": "1HGCM82633A123456",
          "make": "Honda",
          "model": "Accord",
          "year": 2020,
          "sellingPrice": 450000.00,
          "status": "AVAILABLE"
        }
        // ... up to 5 featured cars
      ]
    },
    "notifications": {
      "unreadCount": 3,
      "recent": [
        {
          "id": 2001,
          "type": "INQUIRY_ASSIGNED",
          "subject": "New Inquiry Assigned",
          "body": "Inquiry #456 has been assigned to you",
          "entityType": "INQUIRY",
          "entityId": "456",
          "severity": "INFO",
          "createdAt": "2025-12-23T10:15:00",
          "isRead": false
        }
      ]
    }
  },
  "timestamp": "2025-12-23T15:45:30"
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `personalStats.activeInquiries` | Long | Open and in-progress inquiries |
| `personalStats.convertedInquiries` | Long | Successfully responded inquiries |
| `personalStats.conversionRate` | Double | Conversion percentage |
| `personalStats.commissionEarned` | BigDecimal | Commission earned this month |
| `pipeline.inquiriesByStatus` | Map<String, Long> | Inquiry count by status |
| `pipeline.followUpToday` | Long | Follow-ups due today |
| `performance.monthlySales` | Long | Sales completed this month |
| `performance.monthlyTarget` | Long | Sales target for month |
| `performance.targetProgress` | Double | Progress percentage (0-100) |
| `quickActions.pendingResponses` | Long | Open inquiries needing response |
| `availableInventory.totalAvailable` | Long | Cars available for sale |
| `availableInventory.newArrivals` | Long | Cars added in last 30 days |

---

## Inspector Dashboard

**Endpoint:** `GET /api/v1/dashboard/inspector`  
**Authorization:** `INSPECTOR`, `ADMIN`, `SUPER_ADMIN`

### Response Structure

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "inspectionQueue": {
      "pendingInspections": 15,
      "scheduledToday": 4,
      "scheduledThisWeek": 15,
      "overdue": 2
    },
    "personalStats": {
      "completedThisMonth": 28,
      "totalCompleted": 245,
      "passRate": 87.5,
      "avgInspectionTime": 45.0,
      "avgRepairCost": 35000.00
    },
    "vehicleStatus": {
      "needingInspection": 15,
      "failedInspections": 8,
      "inMaintenance": 12
    },
    "assignedTasks": {
      "total": 18,
      "highPriority": 5,
      "dueToday": 3
    },
    "locationSummary": [
      {
        "locationId": 1,
        "locationName": "Main Warehouse",
        "vehicleCount": 120,
        "pendingInspections": 8
      },
      {
        "locationId": 2,
        "locationName": "Satellite Lot A",
        "vehicleCount": 65,
        "pendingInspections": 7
      }
    ],
    "recentInspections": [
      {
        "inspectionId": 501,
        "carId": 1234,
        "vinNumber": "1HGCM82633A123456",
        "make": "Honda",
        "model": "Accord",
        "inspectionDate": "2025-12-22",
        "inspectionPass": true,
        "estimatedRepairCost": 0.00
      }
      // ... up to 10 recent inspections
    ],
    "notifications": {
      "unreadCount": 4,
      "recent": [
        {
          "id": 3001,
          "type": "INSPECTION_SCHEDULED",
          "subject": "Inspection Scheduled",
          "body": "Vehicle inspection scheduled for today",
          "entityType": "INSPECTION",
          "entityId": "505",
          "severity": "INFO",
          "createdAt": "2025-12-23T08:00:00",
          "isRead": false
        }
      ]
    }
  },
  "timestamp": "2025-12-23T15:45:30"
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `inspectionQueue.pendingInspections` | Long | Cars awaiting inspection |
| `inspectionQueue.scheduledToday` | Long | Inspections scheduled today |
| `inspectionQueue.overdue` | Long | Overdue inspections |
| `personalStats.completedThisMonth` | Long | Inspections completed this month |
| `personalStats.passRate` | Double | Percentage of inspections passed |
| `personalStats.avgInspectionTime` | Double | Average inspection duration (minutes) |
| `personalStats.avgRepairCost` | BigDecimal | Average estimated repair cost |
| `vehicleStatus.needingInspection` | Long | Vehicles needing inspection |
| `vehicleStatus.failedInspections` | Long | Recent failed inspections |
| `assignedTasks.total` | Long | Total assigned tasks |
| `assignedTasks.highPriority` | Long | High priority tasks |
| `locationSummary` | List | Inspection queue by location |
| `recentInspections` | List | Last 10 inspections completed |

---

## Finance Dashboard

**Endpoint:** `GET /api/v1/dashboard/finance`  
**Authorization:** `FINANCE`, `ADMIN`, `SUPER_ADMIN`

### Response Structure

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "financialOverview": {
      "totalRevenue": 15750000.00,
      "totalExpenses": 10800000.00,
      "netProfit": 4950000.00,
      "profitMargin": 31.43,
      "cashFlow": 4950000.00
    },
    "transactions": {
      "pendingCount": 0,
      "pendingAmount": 0.00,
      "completedThisMonth": 45,
      "recentTransactions": [
        {
          "transactionId": 7001,
          "transactionType": "SALE",
          "amount": 450000.00,
          "transactionDate": "2025-12-23T14:30:00",
          "description": "Sale of Honda Accord 2020"
        }
        // ... up to 5 recent transactions
      ]
    },
    "profitability": {
      "avgProfitPerVehicle": 141428.57,
      "avgMargin": 31.5,
      "bestPerformingCategory": "SUV",
      "vehicleProfitability": [
        {
          "carId": 1234,
          "vinNumber": "1HGCM82633A123456",
          "make": "Honda",
          "model": "Accord",
          "purchasePrice": 380000.00,
          "sellingPrice": 450000.00,
          "profit": 70000.00,
          "margin": 15.56
        }
        // ... up to 10 vehicles
      ]
    },
    "aging": {
      "overduePayments": 3,
      "overdueAmount": 85000.00,
      "pendingDeposits": 5
    },
    "budgetTracking": {
      "totalBudget": 2000000.00,
      "spent": 1350000.00,
      "remaining": 650000.00,
      "utilizationRate": 67.5
    },
    "notifications": {
      "unreadCount": 2,
      "recent": [
        {
          "id": 4001,
          "type": "PAYMENT_RECEIVED",
          "subject": "Payment Received",
          "body": "Payment of $450,000 received for Sale #5678",
          "entityType": "TRANSACTION",
          "entityId": "7001",
          "severity": "INFO",
          "createdAt": "2025-12-23T14:35:00",
          "isRead": false
        }
      ]
    }
  },
  "timestamp": "2025-12-23T15:45:30"
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `financialOverview.totalRevenue` | BigDecimal | Total revenue all-time |
| `financialOverview.totalExpenses` | BigDecimal | Total expenses all-time |
| `financialOverview.netProfit` | BigDecimal | Revenue minus expenses |
| `financialOverview.profitMargin` | Double | Profit margin percentage |
| `transactions.pendingCount` | Long | Pending transaction count |
| `transactions.completedThisMonth` | Long | Completed transactions this month |
| `profitability.avgProfitPerVehicle` | BigDecimal | Average profit per sold vehicle |
| `profitability.avgMargin` | Double | Average profit margin percentage |
| `profitability.vehicleProfitability` | List | Top 10 profitable vehicles |
| `aging.overduePayments` | Long | Count of overdue payments |
| `aging.overdueAmount` | BigDecimal | Total overdue amount |
| `budgetTracking.totalBudget` | BigDecimal | Total budget allocated |
| `budgetTracking.utilizationRate` | Double | Budget utilization percentage |

---

## Store Manager Dashboard

**Endpoint:** `GET /api/v1/dashboard/store-manager`  
**Authorization:** `STORE_MANAGER`, `ADMIN`, `SUPER_ADMIN`

### Response Structure

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "locationOverview": {
      "totalLocations": 3,
      "managedLocations": 3,
      "totalCapacity": 250,
      "currentOccupancy": 185,
      "utilizationRate": 74.0
    },
    "vehicleDistribution": {
      "byLocation": {
        "Main Warehouse": 120,
        "Satellite Lot A": 65,
        "Satellite Lot B": 0
      },
      "byStatus": {
        "AVAILABLE": 185,
        "RESERVED": 45,
        "SOLD": 220,
        "MAINTENANCE": 12,
        "IN_TRANSIT": 8
      }
    },
    "movements": {
      "todayMovements": 4,
      "thisWeekMovements": 18,
      "pendingTransfers": 3,
      "recentMovements": []
    },
    "capacityAlerts": {
      "nearFullLocations": 1,
      "underutilizedLocations": 1,
      "recommendations": [
        {
          "type": "NEAR_FULL",
          "locationName": "Main Warehouse",
          "message": "Location is near capacity",
          "currentOccupancy": 120,
          "capacity": 130
        },
        {
          "type": "UNDERUTILIZED",
          "locationName": "Satellite Lot B",
          "message": "Location has excess capacity",
          "currentOccupancy": 0,
          "capacity": 50
        }
      ]
    },
    "maintenanceStatus": {
      "vehiclesInMaintenance": 12,
      "byLocation": {
        "Main Location": 12
      },
      "avgMaintenanceTime": 5.5
    },
    "performance": {
      "avgTurnoverDays": 42.0,
      "fastestMovingCategory": "Sedan",
      "slowestMovingCategory": "Luxury"
    },
    "notifications": {
      "unreadCount": 3,
      "recent": [
        {
          "id": 5001,
          "type": "CAPACITY_WARNING",
          "subject": "Location Near Capacity",
          "body": "Main Warehouse is at 92% capacity",
          "entityType": "LOCATION",
          "entityId": "1",
          "severity": "WARNING",
          "createdAt": "2025-12-23T12:00:00",
          "isRead": false
        }
      ]
    }
  },
  "timestamp": "2025-12-23T15:45:30"
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `locationOverview.totalLocations` | Long | Total storage locations |
| `locationOverview.totalCapacity` | Long | Total storage capacity |
| `locationOverview.currentOccupancy` | Long | Total vehicles stored |
| `locationOverview.utilizationRate` | Double | Utilization percentage |
| `vehicleDistribution.byLocation` | Map<String, Long> | Vehicle count by location |
| `vehicleDistribution.byStatus` | Map<String, Long> | Vehicle count by status |
| `movements.todayMovements` | Long | Vehicle movements today |
| `movements.thisWeekMovements` | Long | Vehicle movements this week |
| `capacityAlerts.nearFullLocations` | Long | Locations >85% capacity |
| `capacityAlerts.underutilizedLocations` | Long | Locations <30% capacity |
| `maintenanceStatus.vehiclesInMaintenance` | Long | Vehicles in maintenance |
| `maintenanceStatus.avgMaintenanceTime` | Double | Average maintenance days |
| `performance.avgTurnoverDays` | Double | Average days to sell |

---

## Current User Dashboard

**Endpoint:** `GET /api/v1/dashboard/me`  
**Authorization:** Any authenticated user

### Response Structure

Returns the appropriate dashboard based on the user's primary role:
- **ADMIN/SUPER_ADMIN** → Returns Admin Dashboard structure
- **SALES** → Returns Sales Dashboard structure
- **INSPECTOR** → Returns Inspector Dashboard structure
- **FINANCE** → Returns Finance Dashboard structure
- **STORE_MANAGER** → Returns Store Manager Dashboard structure

```json
{
  "success": true,
  "message": "Success",
  "data": {
    // Returns one of the above dashboard structures based on role
  },
  "timestamp": "2025-12-23T15:45:30"
}
```

---

## Error Responses

### 401 Unauthorized

```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null,
  "timestamp": "2025-12-23T15:45:30"
}
```

### 403 Forbidden

```json
{
  "success": false,
  "message": "Access denied",
  "data": null,
  "timestamp": "2025-12-23T15:45:30"
}
```

### 404 Not Found

```json
{
  "success": false,
  "message": "Employee not found",
  "data": null,
  "timestamp": "2025-12-23T15:45:30"
}
```

### 500 Internal Server Error

```json
{
  "success": false,
  "message": "Internal server error",
  "data": null,
  "timestamp": "2025-12-23T15:45:30"
}
```

---

## Integration Examples

### React/TypeScript

```typescript
// types.ts
export interface AdminDashboardResponse {
  overview: {
    totalCars: number;
    availableCars: number;
    reservedCars: number;
    soldCarsThisMonth: number;
    activeInquiries: number;
    activeReservations: number;
    totalEmployees: number;
    activeEmployees: number;
  };
  revenue: {
    totalRevenue: number;
    monthlyRevenue: number;
    ytdRevenue: number;
    averageSalePrice: number;
    revenueTrend: Array<{
      month: number;
      monthName: string;
      revenue: number;
      salesCount: number;
    }>;
  };
  inventory: {
    byStatus: Record<string, number>;
    totalValue: number;
    avgAge: number;
    agingInventory: Array<any>;
  };
  recentActivities: Array<any>;
  topEmployees: Array<any>;
  alerts: any;
  notifications: {
    unreadCount: number;
    recent: Array<any>;
  };
}

// api.ts
export const fetchAdminDashboard = async (): Promise<AdminDashboardResponse> => {
  const response = await fetch('/api/v1/dashboard/admin', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  const result = await response.json();
  return result.data;
};
```

### Vue/JavaScript

```javascript
// composables/useDashboard.js
export const useAdminDashboard = () => {
  const dashboard = ref(null);
  const loading = ref(false);
  const error = ref(null);

  const fetchDashboard = async () => {
    loading.value = true;
    try {
      const response = await $fetch('/api/v1/dashboard/admin');
      dashboard.value = response.data;
    } catch (e) {
      error.value = e;
    } finally {
      loading.value = false;
    }
  };

  return { dashboard, loading, error, fetchDashboard };
};
```

### Angular

```typescript
// dashboard.service.ts
@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}

  getAdminDashboard(): Observable<AdminDashboardResponse> {
    return this.http.get<ApiResponse<AdminDashboardResponse>>('/api/v1/dashboard/admin')
      .pipe(map(response => response.data));
  }
}
```

---

## Notes

1. **All numeric values** are returned as numbers/strings based on type (Long, BigDecimal)
2. **Dates** are in ISO 8601 format: `yyyy-MM-dd'T'HH:mm:ss`
3. **BigDecimal fields** (prices, amounts) may have up to 2 decimal places
4. **Empty arrays** are returned as `[]`, never `null`
5. **Missing optional fields** may be `null`
6. **Map fields** return empty objects `{}` when no data exists
7. **Cache duration** is 5-10 minutes; poll accordingly
8. **Rate limiting** applies per user

---

## Changelog

- **2025-12-23**: Initial document created with all 5 role-based dashboards

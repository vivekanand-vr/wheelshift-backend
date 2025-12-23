# Dashboard Quick Start Guide

## Overview

The WheelShift Pro dashboard system provides role-specific, comprehensive views for all users. Each dashboard is tailored to show the most relevant metrics and actions for that role.

## Quick Start

### 1. Get Your Dashboard

The simplest way to get started is to use the auto-detect endpoint:

```bash
GET /api/v1/dashboard/me
Authorization: Bearer <your-session-token>
```

This automatically detects your role and returns the appropriate dashboard.

### 2. Role-Specific Endpoints

Or call the specific endpoint for your role:

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

## Dashboard Widgets by Role

### ADMIN / SUPER_ADMIN

**Overview Widget**
- Total cars in inventory
- Available vs reserved vs sold
- Active inquiries and reservations
- Employee count and active status

**Revenue Widget**
- Total revenue, monthly revenue, YTD
- Average sale price
- Monthly revenue trend (12 months)

**Inventory Widget**
- Cars by status breakdown
- Total inventory value
- Average age of inventory
- Aging inventory (60+ days)

**Activities Widget**
- Recent sales
- New cars added
- Status changes
- Top 10 most recent activities

**Top Employees Widget**
- Top 5 performers this month
- Sales count and commission earned
- Total revenue generated

**Alerts Widget**
- Expiring reservations (next 3 days)
- Inspections due
- Location capacity warnings

**Notifications Widget**
- Unread notification count
- Recent 5 notifications

---

### SALES

**Personal Stats Widget**
- My active inquiries
- Conversion rate
- Active reservations
- Sales this month
- Commission earned

**Pipeline Widget**
- Inquiries by status (Open, In Progress, Responded)
- Follow-ups due today
- Follow-ups due this week

**Performance Widget**
- Monthly sales vs target
- Target progress percentage
- Sales trend (12 months)
- Average sale value

**Quick Actions Widget**
- Pending responses needed
- Follow-ups due
- Expiring reservations to convert

**Available Inventory Widget**
- Total available cars
- New arrivals (last 30 days)
- Featured listings (top 5)

**Notifications Widget**
- Unread notifications
- Recent activity alerts

---

### INSPECTOR

**Inspection Queue Widget**
- Pending inspections
- Scheduled today
- Scheduled this week
- Overdue inspections

**Personal Stats Widget**
- Inspections completed this month
- Total inspections completed
- Pass rate percentage
- Average inspection time
- Average repair cost

**Vehicle Status Widget**
- Cars needing inspection
- Failed inspections requiring attention
- Cars in maintenance

**Assigned Tasks Widget**
- Total tasks assigned to me
- High priority tasks
- Tasks due today

**Location Summary Widget**
- Breakdown by storage location
- Vehicles per location
- Pending inspections per location

**Recent Inspections Widget**
- Last 10 inspections performed
- Pass/fail status
- Estimated repair costs

**Notifications Widget**
- Inspection-related alerts
- Task assignments

---

### FINANCE

**Financial Overview Widget**
- Total revenue
- Total expenses
- Net profit
- Profit margin percentage
- Cash flow

**Transaction Summary Widget**
- Pending transactions count and amount
- Completed transactions this month
- Recent 5 transactions

**Profitability Widget**
- Average profit per vehicle
- Average margin percentage
- Best performing vehicle category
- Top 10 profitable vehicles

**Aging Analysis Widget**
- Overdue payments count and amount
- Pending deposits

**Budget Tracking Widget**
- Total budget
- Amount spent
- Remaining budget
- Utilization rate

**Notifications Widget**
- Finance-related alerts
- Payment reminders

---

### STORE_MANAGER

**Location Overview Widget**
- Total locations managed
- Total capacity across locations
- Current occupancy
- Overall utilization rate

**Vehicle Distribution Widget**
- Cars by location breakdown
- Cars by status breakdown

**Movement Activity Widget**
- Movements today
- Movements this week
- Pending transfers
- Recent movements list

**Capacity Alerts Widget**
- Near-full locations (>85%)
- Underutilized locations (<30%)
- Recommendations for optimization

**Maintenance Status Widget**
- Vehicles currently in maintenance
- Breakdown by location
- Average maintenance time

**Location Performance Widget**
- Average turnover days
- Fastest moving category
- Slowest moving category

**Notifications Widget**
- Location-related alerts
- Capacity warnings

---

## Frontend Integration Examples

### React with TanStack Query

```typescript
// hooks/useDashboard.ts
import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';

export function useDashboard(role?: string) {
  return useQuery({
    queryKey: ['dashboard', role || 'me'],
    queryFn: async () => {
      const endpoint = role ? `/dashboard/${role}` : '/dashboard/me';
      const response = await api.get(endpoint);
      return response.data.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchInterval: 5 * 60 * 1000, // Auto-refresh every 5 min
  });
}

// components/AdminDashboard.tsx
import { useDashboard } from '@/hooks/useDashboard';

function AdminDashboard() {
  const { data, isLoading, error } = useDashboard('admin');

  if (isLoading) return <DashboardSkeleton />;
  if (error) return <ErrorMessage error={error} />;

  return (
    <div className="dashboard-grid">
      <OverviewCard data={data.overview} />
      <RevenueCard data={data.revenue} />
      <InventoryCard data={data.inventory} />
      <ActivitiesCard data={data.recentActivities} />
      <TopEmployeesCard data={data.topEmployees} />
      <AlertsCard data={data.alerts} />
      <NotificationsCard data={data.notifications} />
    </div>
  );
}
```

### Vue with Axios

```vue
<template>
  <div class="dashboard">
    <div v-if="loading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else class="dashboard-grid">
      <OverviewWidget :data="dashboard.overview" />
      <RevenueWidget :data="dashboard.revenue" />
      <!-- More widgets -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const dashboard = ref(null);
const loading = ref(true);
const error = ref(null);

onMounted(async () => {
  try {
    const response = await axios.get('/api/v1/dashboard/me');
    dashboard.value = response.data.data;
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
});
</script>
```

### Angular

```typescript
// dashboard.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  constructor(private http: HttpClient) {}

  getDashboard(role?: string): Observable<any> {
    const endpoint = role ? `/dashboard/${role}` : '/dashboard/me';
    return this.http.get(`/api/v1${endpoint}`)
      .pipe(map(response => response.data));
  }
}

// admin-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { DashboardService } from './dashboard.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  dashboard: any;
  loading = true;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit() {
    this.dashboardService.getDashboard('admin')
      .subscribe({
        next: (data) => {
          this.dashboard = data;
          this.loading = false;
        },
        error: (err) => console.error(err)
      });
  }
}
```

---

## Caching Strategy

### Recommended TTLs

- **Overview Stats**: 5 minutes
- **Revenue Metrics**: 10 minutes
- **Personal Stats**: 2 minutes
- **Notifications**: No cache (real-time)

### Cache Invalidation

Dashboard caches should be invalidated when:
- New sale created
- Car status changed
- Inquiry assigned/updated
- Reservation created/converted
- Financial transaction recorded

---

## Testing

### Swagger UI

1. Navigate to `http://localhost:8080/api/v1/swagger-ui.html`
2. Authenticate using `/api/v1/auth/login`
3. Expand "Dashboard" section
4. Try the `/dashboard/me` endpoint
5. View the response schema and data

### Postman Collection

```json
{
  "info": {
    "name": "Dashboard APIs"
  },
  "item": [
    {
      "name": "Get My Dashboard",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/v1/dashboard/me"
      }
    },
    {
      "name": "Get Admin Dashboard",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/v1/dashboard/admin"
      }
    }
  ]
}
```

---

## Performance Tips

1. **Use Auto-Refresh Wisely**: Set appropriate intervals (5-10 minutes) to avoid overloading the server
2. **Implement Skeleton Loading**: Show placeholders while data loads
3. **Error Boundaries**: Isolate widget failures so one broken widget doesn't crash the entire dashboard
4. **Lazy Load Below-the-Fold**: Load widgets that aren't immediately visible on demand
5. **Optimize Images**: If displaying car images, use thumbnails and lazy loading

---

## Troubleshooting

### Dashboard Not Loading

**Issue**: Dashboard endpoint returns 401 Unauthorized

**Solution**: Ensure you're authenticated and your session is valid. Call `/api/v1/auth/me` to verify.

---

**Issue**: Dashboard returns empty widgets

**Solution**: Check if you have data in the system. Create some test data using the respective CRUD endpoints.

---

**Issue**: Slow dashboard load times

**Solution**: 
- Check database query performance
- Verify indexes are in place
- Enable Redis caching
- Review data scope complexity

---

**Issue**: Notifications widget shows 0 unread

**Solution**: Notifications are event-driven. Trigger some actions (create inquiry, assign task) to generate notifications.

---

## Next Steps

1. **Customize Widgets**: Add/remove widgets based on user preferences
2. **Export Data**: Add export functionality for reports
3. **Real-Time Updates**: Implement WebSocket for live data updates
4. **Mobile Optimization**: Create responsive layouts for mobile devices
5. **Dark Mode**: Add theme support for better UX

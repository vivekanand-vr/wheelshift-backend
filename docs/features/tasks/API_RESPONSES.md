# Task Management API Response Structures

Complete reference for all task API responses to facilitate frontend integration.

## Table of Contents

1. [Create Task](#create-task)
2. [Update Task](#update-task)
3. [Get Task by ID](#get-task-by-id)
4. [Get All Tasks](#get-all-tasks)
5. [Delete Task](#delete-task)
6. [Search Tasks](#search-tasks)
7. [Get Tasks by Employee](#get-tasks-by-employee)
8. [Get Tasks by Status](#get-tasks-by-status)
9. [Get Tasks by Priority](#get-tasks-by-priority)
10. [Get Overdue Tasks](#get-overdue-tasks)
11. [Update Task Status](#update-task-status)
12. [Assign Task](#assign-task)
13. [Get Task Statistics](#get-task-statistics)

---

## Create Task

**Endpoint:** `POST /api/v1/tasks`  
**Authorization:** Required

### Request Body

```json
{
  "title": "Complete vehicle inspection for Honda Accord",
  "description": "Perform comprehensive safety and mechanical inspection. Check brakes, tires, engine, transmission, and all safety features. Document any issues found.",
  "status": "TODO",
  "assigneeId": 15,
  "dueDate": "2025-12-28T17:00:00",
  "priority": "HIGH",
  "tags": ["inspection", "urgent", "pre-sale"]
}
```

### Request Field Details

| Field | Type | Required | Description | Constraints |
|-------|------|----------|-------------|-------------|
| title | String | Yes | Task title | Max 128 characters |
| description | String | No | Detailed description | No limit |
| status | String | No | Initial status | One of: TODO, IN_PROGRESS, REVIEW, DONE |
| assigneeId | Long | No | Employee ID to assign | Must be valid employee ID |
| dueDate | String (ISO 8601) | No | Task deadline | DateTime format: YYYY-MM-DDTHH:mm:ss |
| priority | String | No | Priority level | One of: LOW, MEDIUM, HIGH |
| tags | Array[String] | No | Custom tags | Array of strings |

### Success Response (201 Created)

```json
{
  "success": true,
  "message": "Task created successfully",
  "data": {
    "id": 42,
    "title": "Complete vehicle inspection for Honda Accord",
    "description": "Perform comprehensive safety and mechanical inspection. Check brakes, tires, engine, transmission, and all safety features. Document any issues found.",
    "status": "TODO",
    "assigneeId": 15,
    "assigneeName": "John Smith",
    "dueDate": "2025-12-28T17:00:00",
    "priority": "HIGH",
    "tags": ["inspection", "urgent", "pre-sale"],
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T10:30:00"
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

### Error Response (400 Bad Request)

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required"
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

---

## Update Task

**Endpoint:** `PUT /api/v1/tasks/{id}`  
**Authorization:** Required

### Path Parameters

- `id` (Long) - Task ID

### Request Body

```json
{
  "title": "Complete vehicle inspection for Honda Accord - Updated",
  "description": "Updated description with additional requirements",
  "status": "IN_PROGRESS",
  "assigneeId": 15,
  "dueDate": "2025-12-29T17:00:00",
  "priority": "MEDIUM",
  "tags": ["inspection", "in-progress"]
}
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Task updated successfully",
  "data": {
    "id": 42,
    "title": "Complete vehicle inspection for Honda Accord - Updated",
    "description": "Updated description with additional requirements",
    "status": "IN_PROGRESS",
    "assigneeId": 15,
    "assigneeName": "John Smith",
    "dueDate": "2025-12-29T17:00:00",
    "priority": "MEDIUM",
    "tags": ["inspection", "in-progress"],
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T14:45:00"
  },
  "timestamp": "2025-12-25T14:45:00"
}
```

### Error Response (404 Not Found)

```json
{
  "success": false,
  "message": "Task not found with id: 42",
  "timestamp": "2025-12-25T14:45:00"
}
```

---

## Get Task by ID

**Endpoint:** `GET /api/v1/tasks/{id}`  
**Authorization:** Required

### Path Parameters

- `id` (Long) - Task ID

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 42,
    "title": "Complete vehicle inspection for Honda Accord",
    "description": "Perform comprehensive safety and mechanical inspection.",
    "status": "IN_PROGRESS",
    "assigneeId": 15,
    "assigneeName": "John Smith",
    "dueDate": "2025-12-28T17:00:00",
    "priority": "HIGH",
    "tags": ["inspection", "urgent"],
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T10:30:00"
  },
  "timestamp": "2025-12-25T15:00:00"
}
```

---

## Get All Tasks

**Endpoint:** `GET /api/v1/tasks`  
**Authorization:** Required

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Request

```http
GET /api/v1/tasks?page=0&size=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 42,
        "title": "Complete vehicle inspection for Honda Accord",
        "description": "Perform comprehensive safety inspection",
        "status": "IN_PROGRESS",
        "assigneeId": 15,
        "assigneeName": "John Smith",
        "dueDate": "2025-12-28T17:00:00",
        "priority": "HIGH",
        "tags": ["inspection", "urgent"],
        "createdAt": "2025-12-25T10:30:00",
        "updatedAt": "2025-12-25T10:30:00"
      },
      {
        "id": 43,
        "title": "Process financing documents for customer #1234",
        "description": "Review and finalize loan paperwork",
        "status": "TODO",
        "assigneeId": 22,
        "assigneeName": "Sarah Johnson",
        "dueDate": "2025-12-27T15:00:00",
        "priority": "MEDIUM",
        "tags": ["finance", "paperwork"],
        "createdAt": "2025-12-25T11:00:00",
        "updatedAt": "2025-12-25T11:00:00"
      },
      {
        "id": 44,
        "title": "Follow up with customer inquiry #5678",
        "description": "Contact customer regarding pricing question",
        "status": "TODO",
        "assigneeId": null,
        "assigneeName": null,
        "dueDate": "2025-12-26T12:00:00",
        "priority": "LOW",
        "tags": ["sales", "follow-up"],
        "createdAt": "2025-12-25T12:00:00",
        "updatedAt": "2025-12-25T12:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 3,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-12-25T15:00:00"
}
```

---

## Delete Task

**Endpoint:** `DELETE /api/v1/tasks/{id}`  
**Authorization:** Required

### Path Parameters

- `id` (Long) - Task ID

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Task deleted successfully",
  "data": null,
  "timestamp": "2025-12-25T15:30:00"
}
```

---

## Search Tasks

**Endpoint:** `GET /api/v1/tasks/search`  
**Authorization:** Required

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| assignedToId | Long | No | - | Filter by assigned employee ID |
| status | String | No | - | Filter by status (TODO, IN_PROGRESS, REVIEW, DONE) |
| priority | String | No | - | Filter by priority (LOW, MEDIUM, HIGH) |
| startDate | String | No | - | Filter tasks with dueDate >= startDate (ISO 8601 Date) |
| endDate | String | No | - | Filter tasks with dueDate <= endDate (ISO 8601 Date) |
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Request

```http
GET /api/v1/tasks/search?status=IN_PROGRESS&priority=HIGH&startDate=2025-12-25&endDate=2025-12-31&page=0&size=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 42,
        "title": "Complete vehicle inspection for Honda Accord",
        "description": "Perform comprehensive safety inspection",
        "status": "IN_PROGRESS",
        "assigneeId": 15,
        "assigneeName": "John Smith",
        "dueDate": "2025-12-28T17:00:00",
        "priority": "HIGH",
        "tags": ["inspection", "urgent"],
        "createdAt": "2025-12-25T10:30:00",
        "updatedAt": "2025-12-25T10:30:00"
      },
      {
        "id": 48,
        "title": "Urgent - Prepare vehicle for delivery",
        "description": "Full detail, final inspection, and delivery prep",
        "status": "IN_PROGRESS",
        "assigneeId": 18,
        "assigneeName": "Mike Davis",
        "dueDate": "2025-12-26T14:00:00",
        "priority": "HIGH",
        "tags": ["delivery", "urgent"],
        "createdAt": "2025-12-25T09:00:00",
        "updatedAt": "2025-12-25T13:30:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-12-25T16:00:00"
}
```

---

## Get Tasks by Employee

**Endpoint:** `GET /api/v1/tasks/employee/{employeeId}`  
**Authorization:** Required

### Path Parameters

- `employeeId` (Long) - Employee ID

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Request

```http
GET /api/v1/tasks/employee/15?page=0&size=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 42,
        "title": "Complete vehicle inspection for Honda Accord",
        "description": "Perform comprehensive safety inspection",
        "status": "IN_PROGRESS",
        "assigneeId": 15,
        "assigneeName": "John Smith",
        "dueDate": "2025-12-28T17:00:00",
        "priority": "HIGH",
        "tags": ["inspection", "urgent"],
        "createdAt": "2025-12-25T10:30:00",
        "updatedAt": "2025-12-25T10:30:00"
      },
      {
        "id": 51,
        "title": "Review inspection reports from last week",
        "description": "Quality check on completed inspections",
        "status": "TODO",
        "assigneeId": 15,
        "assigneeName": "John Smith",
        "dueDate": "2025-12-27T16:00:00",
        "priority": "MEDIUM",
        "tags": ["inspection", "review"],
        "createdAt": "2025-12-24T14:00:00",
        "updatedAt": "2025-12-24T14:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-12-25T16:15:00"
}
```

---

## Get Tasks by Status

**Endpoint:** `GET /api/v1/tasks/status/{status}`  
**Authorization:** Required

### Path Parameters

- `status` (String) - Task status (TODO, IN_PROGRESS, REVIEW, DONE)

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Request

```http
GET /api/v1/tasks/status/IN_PROGRESS?page=0&size=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 42,
        "title": "Complete vehicle inspection for Honda Accord",
        "description": "Perform comprehensive safety inspection",
        "status": "IN_PROGRESS",
        "assigneeId": 15,
        "assigneeName": "John Smith",
        "dueDate": "2025-12-28T17:00:00",
        "priority": "HIGH",
        "tags": ["inspection", "urgent"],
        "createdAt": "2025-12-25T10:30:00",
        "updatedAt": "2025-12-25T10:30:00"
      },
      {
        "id": 45,
        "title": "Update inventory database",
        "description": "Sync with latest vehicle additions",
        "status": "IN_PROGRESS",
        "assigneeId": 12,
        "assigneeName": "Emily Brown",
        "dueDate": "2025-12-26T18:00:00",
        "priority": "MEDIUM",
        "tags": ["inventory", "data"],
        "createdAt": "2025-12-25T08:00:00",
        "updatedAt": "2025-12-25T14:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-12-25T16:30:00"
}
```

---

## Get Tasks by Priority

**Endpoint:** `GET /api/v1/tasks/priority/{priority}`  
**Authorization:** Required

### Path Parameters

- `priority` (String) - Task priority (LOW, MEDIUM, HIGH)

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Request

```http
GET /api/v1/tasks/priority/HIGH?page=0&size=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 42,
        "title": "Complete vehicle inspection for Honda Accord",
        "description": "Perform comprehensive safety inspection",
        "status": "IN_PROGRESS",
        "assigneeId": 15,
        "assigneeName": "John Smith",
        "dueDate": "2025-12-28T17:00:00",
        "priority": "HIGH",
        "tags": ["inspection", "urgent"],
        "createdAt": "2025-12-25T10:30:00",
        "updatedAt": "2025-12-25T10:30:00"
      },
      {
        "id": 48,
        "title": "Urgent - Prepare vehicle for delivery",
        "description": "Full detail, final inspection, and delivery prep",
        "status": "TODO",
        "assigneeId": 18,
        "assigneeName": "Mike Davis",
        "dueDate": "2025-12-26T14:00:00",
        "priority": "HIGH",
        "tags": ["delivery", "urgent"],
        "createdAt": "2025-12-25T09:00:00",
        "updatedAt": "2025-12-25T09:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-12-25T16:45:00"
}
```

---

## Get Overdue Tasks

**Endpoint:** `GET /api/v1/tasks/overdue`  
**Authorization:** Required

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Request

```http
GET /api/v1/tasks/overdue?page=0&size=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 38,
        "title": "Submit monthly report",
        "description": "Prepare and submit monthly performance report",
        "status": "TODO",
        "assigneeId": 10,
        "assigneeName": "Robert Wilson",
        "dueDate": "2025-12-20T17:00:00",
        "priority": "HIGH",
        "tags": ["report", "monthly"],
        "createdAt": "2025-12-15T09:00:00",
        "updatedAt": "2025-12-15T09:00:00"
      },
      {
        "id": 40,
        "title": "Complete training module",
        "description": "Finish required safety training",
        "status": "IN_PROGRESS",
        "assigneeId": 25,
        "assigneeName": "Lisa Anderson",
        "dueDate": "2025-12-22T23:59:59",
        "priority": "MEDIUM",
        "tags": ["training", "compliance"],
        "createdAt": "2025-12-18T10:00:00",
        "updatedAt": "2025-12-21T16:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-12-25T17:00:00"
}
```

---

## Update Task Status

**Endpoint:** `PUT /api/v1/tasks/{id}/status`  
**Authorization:** Required

### Path Parameters

- `id` (Long) - Task ID

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | String | Yes | New task status (TODO, IN_PROGRESS, REVIEW, DONE) |

### Example Request

```http
PUT /api/v1/tasks/42/status?status=DONE
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Task status updated successfully",
  "data": null,
  "timestamp": "2025-12-25T17:15:00"
}
```

---

## Assign Task

**Endpoint:** `PUT /api/v1/tasks/{id}/assign`  
**Authorization:** Required

### Path Parameters

- `id` (Long) - Task ID

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| employeeId | Long | Yes | Employee ID to assign task to |

### Example Request

```http
PUT /api/v1/tasks/42/assign?employeeId=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Task assigned successfully",
  "data": null,
  "timestamp": "2025-12-25T17:30:00"
}
```

---

## Get Task Statistics

**Endpoint:** `GET /api/v1/tasks/statistics`  
**Authorization:** Required

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "totalTasks": 125,
    "tasksByStatus": {
      "TODO": 45,
      "IN_PROGRESS": 32,
      "REVIEW": 18,
      "DONE": 30
    },
    "tasksByPriority": {
      "LOW": 35,
      "MEDIUM": 55,
      "HIGH": 35
    },
    "overdueTasks": 8,
    "completionRate": 24.0,
    "averageCompletionTime": "2.5 days",
    "tasksCreatedThisWeek": 15,
    "tasksCompletedThisWeek": 12,
    "unassignedTasks": 10,
    "tasksByEmployee": [
      {
        "employeeId": 15,
        "employeeName": "John Smith",
        "totalTasks": 12,
        "completedTasks": 8,
        "inProgressTasks": 3,
        "overdueTasks": 1
      },
      {
        "employeeId": 22,
        "employeeName": "Sarah Johnson",
        "totalTasks": 10,
        "completedTasks": 7,
        "inProgressTasks": 2,
        "overdueTasks": 1
      }
    ]
  },
  "timestamp": "2025-12-25T17:45:00"
}
```

---

## Common Error Responses

### 400 Bad Request - Validation Error

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "priority": "Priority must be one of: LOW, MEDIUM, HIGH"
  },
  "timestamp": "2025-12-25T18:00:00"
}
```

### 404 Not Found - Task Not Found

```json
{
  "success": false,
  "message": "Task not found with id: 999",
  "timestamp": "2025-12-25T18:00:00"
}
```

### 404 Not Found - Employee Not Found

```json
{
  "success": false,
  "message": "Employee not found with id: 999",
  "timestamp": "2025-12-25T18:00:00"
}
```

### 500 Internal Server Error

```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "error": "InternalServerError",
  "timestamp": "2025-12-25T18:00:00"
}
```

---

## Frontend Integration Notes

### Date Handling
- All dates are in ISO 8601 format
- Use `LocalDateTime` format: `YYYY-MM-DDTHH:mm:ss`
- For date-only filters (startDate, endDate), use: `YYYY-MM-DD`
- Convert to/from user's timezone in frontend

### Null Handling
- `assigneeId` and `assigneeName` can be null (unassigned tasks)
- `dueDate` can be null (no deadline)
- `description` and `tags` can be null or empty
- Always check for null before displaying

### Pagination
- Always use pagination for list views
- Default page size is 20, adjust based on UI needs
- Use `totalElements` to show total count
- Use `last` boolean to determine if more pages exist

### Status Workflow
Recommended task lifecycle:
1. Create task with `TODO` status
2. Update to `IN_PROGRESS` when work starts
3. Update to `REVIEW` when work complete but needs review
4. Update to `DONE` when fully complete

### Search Optimization
- Use specific endpoints (`/status/{status}`, `/employee/{employeeId}`) for simple filters
- Use `/search` endpoint only for multi-criteria searches
- Implement debounce on search inputs to reduce API calls
- Cache search results where appropriate

### Real-time Updates
Consider implementing:
- WebSocket connection for real-time task updates
- Periodic polling of `/overdue` endpoint for alerts
- Refresh statistics dashboard every 5-10 minutes
- Show toast notifications on task assignments

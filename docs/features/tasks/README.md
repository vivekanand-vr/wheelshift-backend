# Task Management System

## Overview

WheelShift Pro implements a comprehensive task management system that enables teams to create, assign, track, and manage tasks efficiently. The system provides robust filtering, search capabilities, and statistics to help teams stay organized and productive.

## Architecture

### Components

1. **Task CRUD Operations** - Create, read, update, and delete tasks
2. **Task Assignment** - Assign tasks to employees
3. **Status Management** - Track task progress through different statuses
4. **Priority Levels** - Categorize tasks by urgency
5. **Advanced Search** - Filter tasks by multiple criteria
6. **Statistics** - View task metrics and insights
7. **Overdue Detection** - Identify and track overdue tasks

### Design Principles

- **Flexible Assignment**: Tasks can be assigned to specific employees or left unassigned
- **Status Tracking**: Clear workflow from TODO → IN_PROGRESS → REVIEW → DONE
- **Priority Management**: Three-tier priority system (LOW, MEDIUM, HIGH)
- **Pagination Support**: All list endpoints support pagination for scalability
- **Date-Based Filtering**: Search tasks by date ranges
- **Comprehensive Search**: Multi-criteria search capabilities

## Task Properties

### Task Status Enum
- `TODO` - Task is pending and not yet started
- `IN_PROGRESS` - Task is currently being worked on
- `REVIEW` - Task is completed and awaiting review
- `DONE` - Task is completed and approved

### Task Priority Enum
- `LOW` - Low priority task
- `MEDIUM` - Medium priority task
- `HIGH` - High priority task

### Task Fields
- **id** (Long) - Unique identifier
- **title** (String) - Task title (required, max 128 characters)
- **description** (String) - Detailed task description (optional)
- **status** (TaskStatus) - Current task status
- **assigneeId** (Long) - ID of assigned employee (optional)
- **assigneeName** (String) - Name of assigned employee (read-only)
- **dueDate** (LocalDateTime) - Task deadline (optional)
- **priority** (TaskPriority) - Task priority level
- **tags** (List<String>) - Custom tags for categorization
- **createdAt** (LocalDateTime) - Creation timestamp (auto-generated)
- **updatedAt** (LocalDateTime) - Last update timestamp (auto-generated)

## API Endpoints

### Core CRUD Operations

1. **Create Task** - `POST /api/v1/tasks`
2. **Update Task** - `PUT /api/v1/tasks/{id}`
3. **Get Task by ID** - `GET /api/v1/tasks/{id}`
4. **Get All Tasks** - `GET /api/v1/tasks`
5. **Delete Task** - `DELETE /api/v1/tasks/{id}`

### Search & Filter Operations

6. **Search Tasks** - `GET /api/v1/tasks/search` (multi-criteria)
7. **Get Tasks by Employee** - `GET /api/v1/tasks/employee/{employeeId}`
8. **Get Tasks by Status** - `GET /api/v1/tasks/status/{status}`
9. **Get Tasks by Priority** - `GET /api/v1/tasks/priority/{priority}`
10. **Get Overdue Tasks** - `GET /api/v1/tasks/overdue`

### Task Management Operations

11. **Update Task Status** - `PUT /api/v1/tasks/{id}/status`
12. **Assign Task** - `PUT /api/v1/tasks/{id}/assign`
13. **Get Task Statistics** - `GET /api/v1/tasks/statistics`

## Common Use Cases

### Creating a Task
```http
POST /api/v1/tasks
Content-Type: application/json

{
  "title": "Inspect Toyota Camry VIN-12345",
  "description": "Complete safety and mechanical inspection",
  "status": "TODO",
  "assigneeId": 5,
  "dueDate": "2025-12-30T17:00:00",
  "priority": "HIGH",
  "tags": ["inspection", "urgent"]
}
```

### Searching Tasks
```http
GET /api/v1/tasks/search?status=IN_PROGRESS&priority=HIGH&page=0&size=20
```

### Getting Employee Tasks
```http
GET /api/v1/tasks/employee/5?page=0&size=20
```

### Updating Task Status
```http
PUT /api/v1/tasks/123/status?status=DONE
```

### Assigning a Task
```http
PUT /api/v1/tasks/123/assign?employeeId=7
```

## Frontend Integration Tips

### Task Board View
Display tasks grouped by status (Kanban-style):
- Use GET `/api/v1/tasks/status/{status}` for each column
- Implement drag-and-drop with PUT `/api/v1/tasks/{id}/status`

### Employee Task Dashboard
Show tasks assigned to specific employee:
- Use GET `/api/v1/tasks/employee/{employeeId}`
- Filter by status for "My Active Tasks"
- Show overdue count with GET `/api/v1/tasks/overdue`

### Task Search Interface
Build advanced search form:
- Use GET `/api/v1/tasks/search` with multiple filters
- Date range pickers for startDate/endDate
- Dropdowns for status and priority

### Task Statistics Dashboard
Display task metrics:
- Use GET `/api/v1/tasks/statistics`
- Show distribution charts by status
- Show distribution charts by priority

## Response Patterns

All responses follow the standard `ApiResponse` wrapper:

```json
{
  "success": true,
  "message": "Success message",
  "data": { /* response data */ },
  "timestamp": "2025-12-25T10:30:00"
}
```

Paginated responses use `PageResponse`:

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [ /* array of items */ ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

## Error Handling

Common error responses:

- **400 Bad Request** - Validation errors (e.g., missing required fields)
- **404 Not Found** - Task or employee not found
- **500 Internal Server Error** - Server-side errors

Example error response:
```json
{
  "success": false,
  "message": "Task not found",
  "error": "ResourceNotFoundException",
  "timestamp": "2025-12-25T10:30:00"
}
```

## Best Practices

1. **Always Paginate** - Use pagination for list endpoints to avoid performance issues
2. **Use Search Endpoint** - Prefer `/search` over multiple individual filter endpoints
3. **Handle Null Values** - assigneeId, dueDate, and tags can be null
4. **Validate Dates** - Ensure dueDate is in future when creating tasks
5. **Update Status Incrementally** - Follow workflow: TODO → IN_PROGRESS → REVIEW → DONE
6. **Display Assignee Names** - Use `assigneeName` field for display (no extra lookup needed)
7. **Show Overdue Alerts** - Regularly check overdue tasks and alert users

## Performance Considerations

- All list endpoints support pagination (default: 20 items per page)
- Use specific filter endpoints when possible (e.g., `/status/{status}` vs. `/search`)
- Statistics endpoint may cache data - consider refresh strategy
- Date range searches are optimized with database indexes

## Security & Permissions

Task endpoints respect the following security rules:
- Authentication required for all endpoints
- Role-based access control (RBAC) applied
- Employees can view their assigned tasks
- Admins/Managers can view and manage all tasks
- Task assignment may be restricted by role

## Related Features

- **Employee Management** - Task assignment requires valid employee IDs
- **Notifications** - Task assignments and status changes may trigger notifications
- **Dashboard** - Task statistics appear on role-based dashboards
- **RBAC** - Task management permissions controlled by roles

## Additional Resources

- [Implementation Guide](./IMPLEMENTATION.md) - Technical implementation details
- [API Response Reference](./API_RESPONSES.md) - Complete API response examples
- [Quick Start Guide](./QUICK_START.md) - Get started quickly

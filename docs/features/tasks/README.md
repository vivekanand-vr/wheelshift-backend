# Task Management System

## Overview

Comprehensive task management system for creating, assigning, tracking, and managing tasks with robust filtering, search capabilities, and statistics.

## Core Components

1. **Task CRUD** - Create, read, update, delete operations
2. **Assignment** - Assign tasks to employees
3. **Status Tracking** - Track progress through workflow stages
4. **Priority Management** - Categorize by urgency (LOW, MEDIUM, HIGH)
5. **Advanced Search** - Multi-criteria filtering
6. **Statistics** - Task metrics and insights
7. **Overdue Detection** - Track and identify overdue tasks

## Task Properties

### Enums

**TaskStatus:**
- `TODO` - Pending, not started
- `IN_PROGRESS` - Currently being worked on
- `REVIEW` - Completed, awaiting review
- `DONE` - Completed and approved

**TaskPriority:**
- `LOW` - Low priority
- `MEDIUM` - Medium priority
- `HIGH` - High priority

### Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | Long | Auto | Unique identifier |
| title | String | Yes | Task title (max 128 chars) |
| description | String | No | Detailed description |
| status | TaskStatus | Yes | Current status |
| assigneeId | Long | No | Assigned employee ID |
| assigneeName | String | Read-only | Assigned employee name |
| dueDate | LocalDateTime | No | Task deadline |
| priority | TaskPriority | Yes | Priority level |
| tags | List\<String\> | No | Custom tags |
| createdAt | LocalDateTime | Auto | Creation timestamp |
| updatedAt | LocalDateTime | Auto | Last update timestamp |

## Quick Start

### Create a Task

```bash
POST /api/v1/tasks
Content-Type: application/json

{
  "title": "Complete vehicle inspection",
  "description": "Full safety and mechanical inspection",
  "status": "TODO",
  "assigneeId": 15,
  "dueDate": "2025-12-30T17:00:00",
  "priority": "HIGH",
  "tags": ["inspection", "urgent"]
}
```

### Search Tasks

```bash
# Multi-criteria search
GET /api/v1/tasks/search?status=IN_PROGRESS&priority=HIGH&page=0&size=20

# By employee
GET /api/v1/tasks/employee/15?page=0&size=20

# By status
GET /api/v1/tasks/status/TODO?page=0&size=20

# Overdue tasks
GET /api/v1/tasks/overdue?page=0&size=20
```

### Update Task

```bash
# Update entire task
PUT /api/v1/tasks/42
Content-Type: application/json
{
  "title": "Updated title",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}

# Update status only
PUT /api/v1/tasks/42/status?status=DONE

# Assign to employee
PUT /api/v1/tasks/42/assign?employeeId=20
```

### Code Examples

```java
@Autowired
private TaskService taskService;

// Create task
TaskRequest request = TaskRequest.builder()
    .title("Complete vehicle inspection")
    .description("Full safety check")
    .status(TaskStatus.TODO)
    .assigneeId(15L)
    .dueDate(LocalDateTime.now().plusDays(3))
    .priority(TaskPriority.HIGH)
    .tags(List.of("inspection", "urgent"))
    .build();
TaskResponse task = taskService.createTask(request);

// Search tasks
PageResponse<TaskResponse> results = taskService.searchTasks(
    15L,                        // assignedToId
    TaskStatus.IN_PROGRESS,     // status
    TaskPriority.HIGH,          // priority
    LocalDate.now(),            // startDate
    LocalDate.now().plusDays(7), // endDate
    0,                          // page
    20                          // size
);

// Update status
taskService.updateTaskStatus(42L, TaskStatus.DONE);

// Assign task
taskService.assignTask(42L, 20L);

// Get statistics
TaskStatisticsResponse stats = taskService.getStatistics();
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| **CRUD Operations** |
| POST | `/api/v1/tasks` | Create task |
| PUT | `/api/v1/tasks/{id}` | Update task |
| GET | `/api/v1/tasks/{id}` | Get task by ID |
| GET | `/api/v1/tasks` | Get all tasks (paginated) |
| DELETE | `/api/v1/tasks/{id}` | Delete task |
| **Search & Filter** |
| GET | `/api/v1/tasks/search` | Multi-criteria search |
| GET | `/api/v1/tasks/employee/{employeeId}` | Get tasks by employee |
| GET | `/api/v1/tasks/status/{status}` | Get tasks by status |
| GET | `/api/v1/tasks/priority/{priority}` | Get tasks by priority |
| GET | `/api/v1/tasks/overdue` | Get overdue tasks |
| **Management** |
| PUT | `/api/v1/tasks/{id}/status` | Update task status |
| PUT | `/api/v1/tasks/{id}/assign` | Assign task to employee |
| GET | `/api/v1/tasks/statistics` | Get task statistics |

### Search Parameters

`GET /api/v1/tasks/search` supports:
- `assignedToId` (Long) - Filter by assignee
- `status` (TaskStatus) - Filter by status
- `priority` (TaskPriority) - Filter by priority
- `startDate` (LocalDate) - Filter by created after
- `endDate` (LocalDate) - Filter by created before
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 20)
Use Cases & Integration

### Kanban Board View
```
Display tasks grouped by status columns
- Fetch: GET /api/v1/tasks/status/{status} for each column
- Drag & drop: PUT /api/v1/tasks/{id}/status
- Visual workflow: TODO → IN_PROGRESS → REVIEW → DONE
```

### Employee Dashboard
```
Show employee's assigned tasks
- My Tasks: GET /api/v1/tasks/employee/{employeeId}
- Overdue Count: GET /api/v1/tasks/overdue
- Filter by status for active tasks
```

### Advanced Search Interface
```
Multi-criteria search form
- Endpoint: GET /api/v1/tasks/search
- Filters: status, priority, date range, assignee
- Results: Paginated task list
```

### Task Statistics Widget
```
Display task metrics
- Endpoint: GET /api/v1/tasks/statistics
- Charts: Status distribution, priority breakdown
- Metrics: Total, overdue, completion rate
```

## Error Handling

| Code | Description | Example |
|------|-------------|---------|
| 400 | Bad Request | Missing required field, invalid enum value |
| 404 | Not Found | Task ID doesn't exist, employee not found |
| 500 | Server Error | Database connection issue |

**Error Response:**
```json
{
  "success": false,
  "message": "Task not found",
  "error": "ResourceNotFoundException",
  "timestamp": "2025-12-25T10:30:00"
}
```

**Validation Error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "priority": "Must be one of: LOW, MEDIUM, HIGH"
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

## Database Schema

```sql
-- Tasks table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    assignee_id BIGINT,
    due_date TIMESTAMP,
    priority VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) 
        REFERENCES employees(id) ON DELETE SET NULL
);

-- Task tags table
CREATE TABLE task_tags (
    task_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (task_id, tag),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) 
        REFERENCES tasks(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
```

## Implementation Classes

### Entities
- `Task` - Main task entity with JPA annotations

### DTOs
- `TaskRequest` - Create/update task request
- `TaskResponse` - Task response with assignee name
- `TaskStatisticsResponse` - Statistics response

### Services
- `TaskService` - Interface
- `TaskServiceImpl` - Implementation with business logic

### Repositories
- `TaskRepository` - JPA repository with custom queries

### Controllers
- `TaskController` - REST endpoints

### Mappers
- `TaskMapper` - Entity ↔ DTO conversion

### Enums
- `TaskStatus` - Task workflow states
- `TaskPriority` - Priority levels

## Response Format

All responses use standard wrapper:

```json
{
  "success": true,
  "message": "Success",
  "data": { /* response data */ },
  "timestamp": "2025-12-25T10:30:00"
}
```

**Paginated Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [ /* array of tasks */ ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

**Task Statistics Response:**
```json
{
  "totalTasks": 150,
  "tasksByStatus": {
    "TODO": 45,
    "IN_PROGRESS": 60,
    "REVIEW": 25,
    "DONE": 20
  },
  "tasksByPriority": {
    "LOW": 30,
    "MEDIUM": 70,
    "HIGH": 50
  },
  "overdueTasks": 12,
  "completedThisMonth": 45,
  "completedThisWeek": 15
}
```

## Best Practices

1. **Always Paginate** - Use pagination for all list endpoints (default: 20 items/page)
2. **Prefer Search Endpoint** - Use `/search` over multiple individual filters
3. **Handle Null Values** - `assigneeId`, `dueDate`, `description`, `tags` can be null
4. **Validate Dates** - Ensure `dueDate` is in future when creating tasks
5. **Follow Workflow** - Update status incrementally: TODO → IN_PROGRESS → REVIEW → DONE
6. **Use Assignee Name** - `assigneeName` field populated automatically, no extra lookup needed
7. **Monitor Overdue** - Regularly check `/overdue` endpoint and alert users
8. **Leverage Tags** - Use tags for custom categorization and filtering
9. **Test Assignment** - Verify employee ID exists before assigning
10. **Implement Retry** - Handle temporary failures with retry logic

## Security & Permissions

- **Authentication** - Required for all endpoints
- **RBAC Applied** - Role-based access control enforced
- **Employee Access** - Users can view their assigned tasks
- **Admin Access** - Admins/managers can view and manage all tasks
- **Assignment Rules** - Task assignment may be restricted by role
- **Data Scopes** - Employees see tasks within their scope (location, department)

## Performance Optimization

- All list endpoints paginated by default
- Database indexes on: status, priority, assignee_id, due_date, created_at
- Use specific filters (e.g., `/status/{status}`) when possible
- Statistics endpoint may cache data (consider refresh strategy)
- Date range searches optimized with indexes

## Testing Example

```java
@Test
public void testTaskCreationAndAssignment() {
    // Create task
    TaskRequest request = TaskRequest.builder()
        .title("Test Task")
        .status(TaskStatus.TODO)
        .priority(TaskPriority.HIGH)
        .build();
    
    TaskResponse task = taskService.createTask(request);
    assertNotNull(task.getId());
    assertEquals(TaskStatus.TODO, task.getStatus());
    
    // Assign task
    taskService.assignTask(task.getId(), employeeId);
    
    TaskResponse updated = taskService.getTaskById(task.getId());
    assertEquals(employeeId, updated.getAssigneeId());
    assertNotNull(updated.getAssigneeName());
}

@Test
public void testSearchTasks() {
    // Search by status and priority
    PageResponse<TaskResponse> results = taskService.searchTasks(
        null, TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
        null, null, 0, 20
    );
    
    assertFalse(results.getContent().isEmpty());
    results.getContent().forEach(task -> {
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(TaskPriority.HIGH, task.getPriority());
    });
}

@Test
public void testOverdueTasks() {
    // Create overdue task
    TaskRequest request = TaskRequest.builder()
        .title("Overdue Task")
        .dueDate(LocalDateTime.now().minusDays(1))
        .status(TaskStatus.TODO)
        .priority(TaskPriority.HIGH)
        .build();
    
    taskService.createTask(request);
    
    // Fetch overdue tasks
    PageResponse<TaskResponse> overdue = taskService.getOverdueTasks(0, 20);
    assertTrue(overdue.getTotalElements() > 0);
}
```

## API Documentation

Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
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
- API Documentation: http://localhost:8080/api/v1/swagger-ui.html

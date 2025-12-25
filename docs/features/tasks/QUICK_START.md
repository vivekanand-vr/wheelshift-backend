# Task Management Quick Start Guide

Get up and running with the Task Management API in minutes.

## Prerequisites

- Java 17+
- Spring Boot 3.x
- PostgreSQL/MySQL database
- Authentication configured

## Quick Setup

### 1. Add Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Database Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 2. Database Migration

Create Flyway migration: `V10__Create_Tasks_Tables.sql`

```sql
-- Create tasks table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    assignee_id BIGINT,
    due_date TIMESTAMP,
    priority VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) 
        REFERENCES employees(id) ON DELETE SET NULL
);

-- Create task tags table
CREATE TABLE task_tags (
    task_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (task_id, tag),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) 
        REFERENCES tasks(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
```

### 3. Basic Usage Examples

#### Create a Task

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Complete vehicle inspection",
    "description": "Full safety and mechanical inspection",
    "status": "TODO",
    "assigneeId": 15,
    "dueDate": "2025-12-28T17:00:00",
    "priority": "HIGH",
    "tags": ["inspection", "urgent"]
  }'
```

#### Get All Tasks

```bash
curl -X GET "http://localhost:8080/api/v1/tasks?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Search Tasks

```bash
curl -X GET "http://localhost:8080/api/v1/tasks/search?status=IN_PROGRESS&priority=HIGH&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Update Task Status

```bash
curl -X PUT "http://localhost:8080/api/v1/tasks/42/status?status=DONE" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Assign Task to Employee

```bash
curl -X PUT "http://localhost:8080/api/v1/tasks/42/assign?employeeId=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get Task Statistics

```bash
curl -X GET "http://localhost:8080/api/v1/tasks/statistics" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Frontend Integration

### JavaScript/TypeScript

```typescript
// Task types
interface TaskRequest {
  title: string;
  description?: string;
  status?: 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';
  assigneeId?: number;
  dueDate?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
  tags?: string[];
}

interface TaskResponse {
  id: number;
  title: string;
  description?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';
  assigneeId?: number;
  assigneeName?: string;
  dueDate?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

// API client
class TaskAPI {
  private baseURL = '/api/v1/tasks';
  
  async createTask(task: TaskRequest): Promise<TaskResponse> {
    const response = await fetch(this.baseURL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
      },
      body: JSON.stringify(task)
    });
    const data = await response.json();
    if (!data.success) throw new Error(data.message);
    return data.data;
  }
  
  async getTasks(page = 0, size = 20): Promise<PageResponse<TaskResponse>> {
    const response = await fetch(
      `${this.baseURL}?page=${page}&size=${size}`,
      {
        headers: { 'Authorization': `Bearer ${getToken()}` }
      }
    );
    const data = await response.json();
    return data.data;
  }
  
  async searchTasks(filters: any): Promise<PageResponse<TaskResponse>> {
    const params = new URLSearchParams(
      Object.entries(filters).filter(([_, v]) => v != null)
    );
    const response = await fetch(
      `${this.baseURL}/search?${params}`,
      {
        headers: { 'Authorization': `Bearer ${getToken()}` }
      }
    );
    const data = await response.json();
    return data.data;
  }
  
  async updateTaskStatus(id: number, status: string): Promise<void> {
    await fetch(
      `${this.baseURL}/${id}/status?status=${status}`,
      {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${getToken()}` }
      }
    );
  }
}

// Usage
const taskAPI = new TaskAPI();

// Create task
const newTask = await taskAPI.createTask({
  title: 'Complete inspection',
  status: 'TODO',
  priority: 'HIGH',
  assigneeId: 15
});

// Get tasks
const tasks = await taskAPI.getTasks(0, 20);
console.log(`Total: ${tasks.totalElements}`);
tasks.content.forEach(task => console.log(task.title));

// Search
const highPriorityTasks = await taskAPI.searchTasks({
  priority: 'HIGH',
  status: 'IN_PROGRESS'
});
```

### React Component Example

```typescript
import React, { useState, useEffect } from 'react';

const TaskList: React.FC = () => {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    loadTasks();
  }, []);
  
  const loadTasks = async () => {
    try {
      const taskAPI = new TaskAPI();
      const response = await taskAPI.getTasks(0, 20);
      setTasks(response.content);
    } catch (error) {
      console.error('Failed to load tasks:', error);
    } finally {
      setLoading(false);
    }
  };
  
  const handleStatusChange = async (taskId: number, newStatus: string) => {
    try {
      const taskAPI = new TaskAPI();
      await taskAPI.updateTaskStatus(taskId, newStatus);
      await loadTasks(); // Reload tasks
    } catch (error) {
      console.error('Failed to update status:', error);
    }
  };
  
  if (loading) return <div>Loading...</div>;
  
  return (
    <div className="task-list">
      <h2>Tasks</h2>
      {tasks.map(task => (
        <div key={task.id} className="task-card">
          <h3>{task.title}</h3>
          <p>{task.description}</p>
          <div>
            <span className={`priority-${task.priority.toLowerCase()}`}>
              {task.priority}
            </span>
            <span className={`status-${task.status.toLowerCase()}`}>
              {task.status}
            </span>
          </div>
          {task.assigneeName && <p>Assigned to: {task.assigneeName}</p>}
          <select 
            value={task.status} 
            onChange={(e) => handleStatusChange(task.id, e.target.value)}
          >
            <option value="TODO">To Do</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="REVIEW">Review</option>
            <option value="DONE">Done</option>
          </select>
        </div>
      ))}
    </div>
  );
};
```

## Common Use Cases

### 1. Task Board (Kanban)

Load tasks grouped by status:

```typescript
const loadTaskBoard = async () => {
  const taskAPI = new TaskAPI();
  const statuses = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE'];
  
  const columns = await Promise.all(
    statuses.map(async status => ({
      status,
      tasks: (await taskAPI.searchTasks({ status })).content
    }))
  );
  
  return columns;
};
```

### 2. Employee Task Dashboard

Load tasks for specific employee:

```typescript
const loadEmployeeTasks = async (employeeId: number) => {
  const taskAPI = new TaskAPI();
  const response = await fetch(
    `/api/v1/tasks/employee/${employeeId}?page=0&size=20`,
    {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    }
  );
  const data = await response.json();
  return data.data.content;
};
```

### 3. Overdue Tasks Alert

Check for overdue tasks:

```typescript
const checkOverdueTasks = async () => {
  const response = await fetch('/api/v1/tasks/overdue?page=0&size=100', {
    headers: { 'Authorization': `Bearer ${getToken()}` }
  });
  const data = await response.json();
  
  if (data.data.totalElements > 0) {
    alert(`You have ${data.data.totalElements} overdue tasks!`);
  }
};
```

### 4. Task Statistics Dashboard

Display task metrics:

```typescript
const TaskStatistics: React.FC = () => {
  const [stats, setStats] = useState<any>(null);
  
  useEffect(() => {
    fetch('/api/v1/tasks/statistics', {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
      .then(res => res.json())
      .then(data => setStats(data.data));
  }, []);
  
  if (!stats) return <div>Loading...</div>;
  
  return (
    <div className="statistics-dashboard">
      <h2>Task Statistics</h2>
      <div className="stat-card">
        <h3>Total Tasks</h3>
        <p>{stats.totalTasks}</p>
      </div>
      <div className="stat-card">
        <h3>Overdue</h3>
        <p className="overdue">{stats.overdueTasks}</p>
      </div>
      <div className="stat-card">
        <h3>Completion Rate</h3>
        <p>{stats.completionRate}%</p>
      </div>
      <div className="status-breakdown">
        <h3>By Status</h3>
        {Object.entries(stats.tasksByStatus).map(([status, count]) => (
          <div key={status}>{status}: {count}</div>
        ))}
      </div>
    </div>
  );
};
```

## Testing

### Test with cURL

```bash
# Set base URL and token
BASE_URL="http://localhost:8080/api/v1/tasks"
TOKEN="your_jwt_token_here"

# Create a task
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Test Task",
    "status": "TODO",
    "priority": "MEDIUM"
  }'

# Get all tasks
curl -X GET "$BASE_URL?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Search tasks
curl -X GET "$BASE_URL/search?status=TODO&priority=HIGH" \
  -H "Authorization: Bearer $TOKEN"

# Get task by ID
curl -X GET "$BASE_URL/1" \
  -H "Authorization: Bearer $TOKEN"

# Update task
curl -X PUT "$BASE_URL/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Updated Task",
    "status": "IN_PROGRESS",
    "priority": "HIGH"
  }'

# Update task status
curl -X PUT "$BASE_URL/1/status?status=DONE" \
  -H "Authorization: Bearer $TOKEN"

# Assign task
curl -X PUT "$BASE_URL/1/assign?employeeId=5" \
  -H "Authorization: Bearer $TOKEN"

# Delete task
curl -X DELETE "$BASE_URL/1" \
  -H "Authorization: Bearer $TOKEN"

# Get statistics
curl -X GET "$BASE_URL/statistics" \
  -H "Authorization: Bearer $TOKEN"
```

### Test with Postman

1. Import the collection from `/docs/postman/tasks-collection.json`
2. Set environment variables:
   - `baseUrl`: `http://localhost:8080`
   - `token`: Your JWT token
3. Run the collection

## Troubleshooting

### Common Issues

**401 Unauthorized**
- Check that JWT token is valid and not expired
- Ensure `Authorization: Bearer {token}` header is present

**400 Bad Request - Validation Error**
- Check that `title` field is present (required)
- Ensure `title` is not longer than 128 characters
- Verify enum values: status (TODO, IN_PROGRESS, REVIEW, DONE), priority (LOW, MEDIUM, HIGH)

**404 Not Found**
- Verify task ID exists
- Check that employee ID exists when assigning tasks

**Date Format Issues**
- Use ISO 8601 format: `2025-12-28T17:00:00`
- For date-only parameters: `2025-12-28`

## Next Steps

- [Full API Documentation](./API_RESPONSES.md)
- [Implementation Details](./IMPLEMENTATION.md)
- [Feature Overview](./README.md)

## Support

For issues or questions:
- Check the [Implementation Guide](./IMPLEMENTATION.md)
- Review error messages in API responses
- Check application logs for detailed error information

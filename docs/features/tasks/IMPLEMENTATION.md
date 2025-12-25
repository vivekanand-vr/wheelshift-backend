# Task Management Implementation Guide

## Quick Reference

### API Endpoints

```bash
# CRUD Operations
POST   /api/v1/tasks                    # Create task
PUT    /api/v1/tasks/{id}               # Update task
GET    /api/v1/tasks/{id}               # Get task by ID
GET    /api/v1/tasks                    # Get all tasks (paginated)
DELETE /api/v1/tasks/{id}               # Delete task

# Search & Filter
GET    /api/v1/tasks/search             # Multi-criteria search
GET    /api/v1/tasks/employee/{employeeId}  # Tasks by employee
GET    /api/v1/tasks/status/{status}    # Tasks by status
GET    /api/v1/tasks/priority/{priority}    # Tasks by priority
GET    /api/v1/tasks/overdue            # Overdue tasks

# Task Management
PUT    /api/v1/tasks/{id}/status        # Update status
PUT    /api/v1/tasks/{id}/assign        # Assign to employee
GET    /api/v1/tasks/statistics         # Get statistics
```

### Quick Start Usage

```java
@Autowired
private TaskService taskService;

// Create a task
TaskRequest request = TaskRequest.builder()
    .title("Complete vehicle inspection")
    .description("Full safety and mechanical check")
    .status(TaskStatus.TODO)
    .assigneeId(15L)
    .dueDate(LocalDateTime.now().plusDays(3))
    .priority(TaskPriority.HIGH)
    .tags(List.of("inspection", "urgent"))
    .build();
TaskResponse task = taskService.createTask(request);

// Search tasks
PageResponse<TaskResponse> results = taskService.searchTasks(
    15L,                    // assignedToId
    TaskStatus.IN_PROGRESS, // status
    TaskPriority.HIGH,      // priority
    LocalDate.now(),        // startDate
    LocalDate.now().plusDays(7), // endDate
    0,                      // page
    20                      // size
);

// Update task status
taskService.updateTaskStatus(42L, TaskStatus.DONE);

// Assign task to employee
taskService.assignTask(42L, 20L);
```

---

## Architecture Overview

### Component Structure

```
TaskController (REST Layer)
    ↓
TaskService (Business Logic)
    ↓
TaskRepository (Data Access)
    ↓
Task Entity (Database)
```

### Core Components

1. **TaskController** - REST API endpoints
2. **TaskService** - Business logic and validation
3. **TaskRepository** - JPA repository for data access
4. **Task Entity** - Database entity
5. **TaskRequest** - DTO for creating/updating tasks
6. **TaskResponse** - DTO for API responses
7. **TaskMapper** - Entity ↔ DTO mapping

---

## Implementation Details

### 1. Entity Layer

**Task.java**
```java
package com.wheelshiftpro.entity;

import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Employee assignee;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @ElementCollection
    @CollectionTable(name = "task_tags", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "tag")
    private List<String> tags;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### 2. Repository Layer

**TaskRepository.java**
```java
package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != 'DONE'")
    Page<Task> findOverdueTasks(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:startDate IS NULL OR t.dueDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.dueDate <= :endDate)")
    Page<Task> searchTasks(
        @Param("assigneeId") Long assigneeId,
        @Param("status") TaskStatus status,
        @Param("priority") TaskPriority priority,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    Long countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.priority = :priority")
    Long countByPriority(@Param("priority") TaskPriority priority);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :now AND t.status != 'DONE'")
    Long countOverdueTasks(@Param("now") LocalDateTime now);

    Long countByAssigneeIsNull();
}
```

### 3. Service Layer

**TaskService.java** (Interface)
```java
package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;

import java.time.LocalDate;

public interface TaskService {
    TaskResponse createTask(TaskRequest request);
    TaskResponse updateTask(Long id, TaskRequest request);
    TaskResponse getTaskById(Long id);
    PageResponse<TaskResponse> getAllTasks(int page, int size);
    void deleteTask(Long id);
    PageResponse<TaskResponse> searchTasks(Long assignedToId, TaskStatus status, 
                                          TaskPriority priority, LocalDate startDate, 
                                          LocalDate endDate, int page, int size);
    PageResponse<TaskResponse> getTasksByEmployee(Long employeeId, int page, int size);
    PageResponse<TaskResponse> getTasksByStatus(TaskStatus status, int page, int size);
    PageResponse<TaskResponse> getTasksByPriority(TaskPriority priority, int page, int size);
    PageResponse<TaskResponse> getOverdueTasks(int page, int size);
    void updateTaskStatus(Long id, TaskStatus status);
    void assignTask(Long id, Long employeeId);
    Object getTaskStatistics();
}
```

**TaskServiceImpl.java** (Implementation)
```java
package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.TaskMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.TaskRepository;
import com.wheelshiftpro.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskMapper taskMapper;

    @Override
    public TaskResponse createTask(TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        
        // Set default status if not provided
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        
        // Set default priority if not provided
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }
        
        // Assign employee if provided
        if (request.getAssigneeId() != null) {
            Employee employee = employeeRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found with id: " + request.getAssigneeId()));
            task.setAssignee(employee);
        }
        
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Update fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setTags(request.getTags());
        
        // Update assignee if provided
        if (request.getAssigneeId() != null) {
            Employee employee = employeeRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found with id: " + request.getAssigneeId()));
            task.setAssignee(employee);
        } else {
            task.setAssignee(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> taskPage = taskRepository.findAll(pageable);
        return PageResponse.of(taskPage.map(taskMapper::toResponse));
    }

    @Override
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> searchTasks(Long assignedToId, TaskStatus status,
                                                  TaskPriority priority, LocalDate startDate,
                                                  LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        Page<Task> taskPage = taskRepository.searchTasks(
            assignedToId, status, priority, startDateTime, endDateTime, pageable);
        
        return PageResponse.of(taskPage.map(taskMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasksByEmployee(Long employeeId, int page, int size) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<Task> taskPage = taskRepository.findByAssigneeId(employeeId, pageable);
        return PageResponse.of(taskPage.map(taskMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasksByStatus(TaskStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("priority").descending()
            .and(Sort.by("dueDate").ascending()));
        Page<Task> taskPage = taskRepository.findByStatus(status, pageable);
        return PageResponse.of(taskPage.map(taskMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasksByPriority(TaskPriority priority, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<Task> taskPage = taskRepository.findByPriority(priority, pageable);
        return PageResponse.of(taskPage.map(taskMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getOverdueTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<Task> taskPage = taskRepository.findOverdueTasks(LocalDateTime.now(), pageable);
        return PageResponse.of(taskPage.map(taskMapper::toResponse));
    }

    @Override
    public void updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        task.setStatus(status);
        taskRepository.save(task);
    }

    @Override
    public void assignTask(Long id, Long employeeId) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with id: " + employeeId));
        
        task.setAssignee(employee);
        taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Total tasks
        long totalTasks = taskRepository.count();
        statistics.put("totalTasks", totalTasks);
        
        // Tasks by status
        Map<String, Long> tasksByStatus = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status.name(), taskRepository.countByStatus(status));
        }
        statistics.put("tasksByStatus", tasksByStatus);
        
        // Tasks by priority
        Map<String, Long> tasksByPriority = new HashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            tasksByPriority.put(priority.name(), taskRepository.countByPriority(priority));
        }
        statistics.put("tasksByPriority", tasksByPriority);
        
        // Overdue tasks
        long overdueTasks = taskRepository.countOverdueTasks(LocalDateTime.now());
        statistics.put("overdueTasks", overdueTasks);
        
        // Unassigned tasks
        long unassignedTasks = taskRepository.countByAssigneeIsNull();
        statistics.put("unassignedTasks", unassignedTasks);
        
        // Completion rate
        long doneTasks = taskRepository.countByStatus(TaskStatus.DONE);
        double completionRate = totalTasks > 0 ? (doneTasks * 100.0 / totalTasks) : 0.0;
        statistics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        
        return statistics;
    }
}
```

### 4. Mapper Layer

**TaskMapper.java**
```java
package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequest request) {
        if (request == null) {
            return null;
        }
        
        return Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus())
            .dueDate(request.getDueDate())
            .priority(request.getPriority())
            .tags(request.getTags())
            .build();
    }

    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }
        
        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
            .assigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null)
            .dueDate(task.getDueDate())
            .priority(task.getPriority())
            .tags(task.getTags())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
```

---

## Database Schema

### Tasks Table

```sql
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    assignee_id BIGINT,
    due_date TIMESTAMP,
    priority VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assignee_id) REFERENCES employees(id) ON DELETE SET NULL,
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_assignee (assignee_id),
    INDEX idx_due_date (due_date),
    INDEX idx_created_at (created_at)
);
```

### Task Tags Table

```sql
CREATE TABLE task_tags (
    task_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (task_id, tag),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
```

---

## Security Configuration

### Method-Level Security (Optional)

```java
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
@PostMapping
public ResponseEntity<ApiResponse<TaskResponse>> createTask(...) {
    // Implementation
}

@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER') or @taskSecurity.canAccessTask(#id)")
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(...) {
    // Implementation
}
```

### Custom Security Evaluator

```java
@Component("taskSecurity")
public class TaskSecurityEvaluator {
    
    @Autowired
    private TaskRepository taskRepository;
    
    public boolean canAccessTask(Long taskId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentEmployeeId = getCurrentEmployeeId(auth);
        
        return taskRepository.findById(taskId)
            .map(task -> task.getAssignee() != null && 
                        task.getAssignee().getId().equals(currentEmployeeId))
            .orElse(false);
    }
    
    private Long getCurrentEmployeeId(Authentication auth) {
        // Extract employee ID from authentication
        return ((CustomUserDetails) auth.getPrincipal()).getEmployeeId();
    }
}
```

---

## Validation Rules

### TaskRequest Validation

```java
public class TaskRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 128, message = "Title must not exceed 128 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
    
    // Other fields...
}
```

---

## Error Handling

### Custom Exceptions

```java
// ResourceNotFoundException - when task or employee not found
throw new ResourceNotFoundException("Task not found with id: " + id);

// ValidationException - when validation fails
throw new ValidationException("Invalid task status transition");

// BusinessException - for business rule violations
throw new BusinessException("Cannot assign task to inactive employee");
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", errors));
    }
}
```

---

## Testing

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @InjectMocks
    private TaskServiceImpl taskService;
    
    @Test
    void createTask_Success() {
        // Arrange
        TaskRequest request = TaskRequest.builder()
            .title("Test Task")
            .status(TaskStatus.TODO)
            .priority(TaskPriority.HIGH)
            .build();
        
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        
        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(new TaskResponse());
        
        // Act
        TaskResponse response = taskService.createTask(request);
        
        // Assert
        assertNotNull(response);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void getTaskById_NotFound_ThrowsException() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> taskService.getTaskById(taskId));
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createTask_Success() throws Exception {
        TaskRequest request = TaskRequest.builder()
            .title("Integration Test Task")
            .status(TaskStatus.TODO)
            .priority(TaskPriority.MEDIUM)
            .build();
        
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("Integration Test Task"));
    }
}
```

---

## Performance Optimization

### Caching Strategy

```java
@Service
public class TaskServiceImpl implements TaskService {
    
    @Cacheable(value = "taskStatistics", key = "'stats'")
    public Object getTaskStatistics() {
        // Statistics are cached for 10 minutes
        // ...
    }
    
    @CacheEvict(value = "taskStatistics", allEntries = true)
    public TaskResponse createTask(TaskRequest request) {
        // Evict statistics cache when new task created
        // ...
    }
}
```

### Query Optimization

```java
// Use JOIN FETCH to avoid N+1 queries
@Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignee WHERE t.id = :id")
Optional<Task> findByIdWithAssignee(@Param("id") Long id);

// Use projection for statistics
@Query("SELECT new com.wheelshiftpro.dto.TaskCountDTO(t.status, COUNT(t)) " +
       "FROM Task t GROUP BY t.status")
List<TaskCountDTO> getTaskCountsByStatus();
```

---

## Frontend Integration Examples

### React Example

```typescript
// API service
const taskApi = {
  createTask: async (task: TaskRequest): Promise<TaskResponse> => {
    const response = await fetch('/api/v1/tasks', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(task)
    });
    const data = await response.json();
    return data.data;
  },
  
  searchTasks: async (filters: TaskFilters, page: number = 0): Promise<PageResponse<TaskResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: '20',
      ...Object.fromEntries(
        Object.entries(filters).filter(([_, v]) => v != null)
      )
    });
    const response = await fetch(`/api/v1/tasks/search?${params}`);
    const data = await response.json();
    return data.data;
  },
  
  updateTaskStatus: async (id: number, status: TaskStatus): Promise<void> => {
    await fetch(`/api/v1/tasks/${id}/status?status=${status}`, {
      method: 'PUT'
    });
  }
};

// Component usage
const TaskBoard: React.FC = () => {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  
  useEffect(() => {
    taskApi.searchTasks({ status: 'IN_PROGRESS' }).then(response => {
      setTasks(response.content);
    });
  }, []);
  
  const handleStatusChange = (taskId: number, newStatus: TaskStatus) => {
    taskApi.updateTaskStatus(taskId, newStatus).then(() => {
      // Refresh tasks
    });
  };
  
  return (
    <div>
      {tasks.map(task => (
        <TaskCard key={task.id} task={task} onStatusChange={handleStatusChange} />
      ))}
    </div>
  );
};
```

### Angular Example

```typescript
// Task service
@Injectable()
export class TaskService {
  private apiUrl = '/api/v1/tasks';
  
  constructor(private http: HttpClient) {}
  
  createTask(task: TaskRequest): Observable<TaskResponse> {
    return this.http.post<ApiResponse<TaskResponse>>(this.apiUrl, task)
      .pipe(map(response => response.data));
  }
  
  getTasksByEmployee(employeeId: number, page: number = 0): Observable<PageResponse<TaskResponse>> {
    return this.http.get<ApiResponse<PageResponse<TaskResponse>>>(
      `${this.apiUrl}/employee/${employeeId}?page=${page}&size=20`
    ).pipe(map(response => response.data));
  }
  
  assignTask(taskId: number, employeeId: number): Observable<void> {
    return this.http.put<void>(
      `${this.apiUrl}/${taskId}/assign?employeeId=${employeeId}`, {}
    );
  }
}
```

---

## Monitoring & Logging

### Logging Configuration

```java
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
    
    @Override
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating task with title: {}", request.getTitle());
        try {
            TaskResponse response = // ... implementation
            log.info("Task created successfully with id: {}", response.getId());
            return response;
        } catch (Exception e) {
            log.error("Error creating task: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### Metrics

```java
@Component
public class TaskMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public TaskMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordTaskCreated() {
        meterRegistry.counter("tasks.created").increment();
    }
    
    public void recordTaskCompleted(Duration duration) {
        meterRegistry.timer("tasks.completion.time").record(duration);
    }
}
```

---

## Best Practices

1. **Always Use Pagination** - Never load all tasks at once
2. **Validate Input** - Use Bean Validation annotations
3. **Handle Nulls** - Check for null assignees and dates
4. **Use Transactions** - Mark service methods with @Transactional
5. **Optimize Queries** - Use indexes and fetch strategies appropriately
6. **Cache Statistics** - Statistics endpoint results should be cached
7. **Log Important Actions** - Log task creation, updates, and assignments
8. **Version Your API** - Use /api/v1/ prefix for future compatibility
9. **Document Changes** - Keep audit trail of task modifications
10. **Test Thoroughly** - Write unit and integration tests

---

## Troubleshooting

### Common Issues

**Issue**: N+1 query problem when loading tasks with assignees
- **Solution**: Use JOIN FETCH in repository queries

**Issue**: Date parsing errors from frontend
- **Solution**: Ensure dates are in ISO 8601 format

**Issue**: Tasks not showing up in search
- **Solution**: Check filter parameters and ensure proper null handling

**Issue**: Permission denied errors
- **Solution**: Verify RBAC configuration and security rules

---

## Additional Resources

- [README](./README.md) - Feature overview
- [API Responses](./API_RESPONSES.md) - Complete API reference
- [Quick Start Guide](./QUICK_START.md) - Get started quickly
- Spring Boot Documentation
- JPA/Hibernate Documentation

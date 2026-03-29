package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.EmployeeStatus;
import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.TaskMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.TaskRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Override
    public TaskResponse createTask(TaskRequest request) {
        log.debug("Creating task: {}", request.getTitle());

        // Validate due date if provided
        if (request.getDueDate() != null && request.getDueDate().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("Due date must be in the future", "INVALID_DUE_DATE");
        }

        // Validate assignee if provided (before mapping)
        Employee assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = employeeRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getAssigneeId()));
            
            // Validate employee is ACTIVE
            if (assignee.getStatus() != EmployeeStatus.ACTIVE) {
                throw new BusinessException("Assignee must be ACTIVE", "ASSIGNEE_NOT_ACTIVE");
            }
        }

        Task task = taskMapper.toEntity(request);
        
        // Set default status if not provided
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        
        // Set default priority if not provided
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        // Set assignee after validation
        if (assignee != null) {
            task.setAssignee(assignee);
        }

        Task saved = taskRepository.save(task);

        auditService.log(AuditCategory.TASK, saved.getId(), "CREATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "Task: " + saved.getTitle() + (saved.getAssignee() != null ? ", Assigned to: " + saved.getAssignee().getName() : ""));

        log.info("Created task with ID: {}", saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.debug("Updating task ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Validate due date if being updated
        if (request.getDueDate() != null && request.getDueDate().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("Due date must be in the future", "INVALID_DUE_DATE");
        }

        // Update basic fields via mapper
        taskMapper.updateEntityFromRequest(request, task);
        
        // Handle employee assignment separately
        if (request.getAssigneeId() != null) {
            Employee assignee = employeeRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getAssigneeId()));
            
            // Validate employee is ACTIVE
            if (assignee.getStatus() != EmployeeStatus.ACTIVE) {
                throw new BusinessException("Assignee must be an active employee", "ASSIGNEE_NOT_ACTIVE");
            }
            
            task.setAssignee(assignee);
        } else if (request.getAssigneeId() == null && task.getAssignee() != null) {
            // If assigneeId is explicitly null in request, unassign the task
            task.setAssignee(null);
        }
        
        Task updated = taskRepository.save(task);

        auditService.log(AuditCategory.TASK, id, "UPDATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "Task updated");

        log.info("Updated task ID: {}", id);
        return taskMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getAllTasks(String search, int page, int size) {
        log.debug("Fetching all tasks - search: {}, page: {}, size: {}", search, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> tasksPage;
        
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            Specification<Task> spec = (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), searchPattern),
                cb.like(cb.lower(root.get("description")), searchPattern)
            );
            tasksPage = taskRepository.findAll(spec, pageable);
        } else {
            tasksPage = taskRepository.findAll(pageable);
        }

        return buildPageResponse(tasksPage);
    }

    @Override
    public void deleteTask(Long id) {
        log.debug("Deleting task ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Block deletion of DONE tasks (future: check for linked inspection/sale records)
        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessException(
                "Cannot delete a DONE task. Once the Task entity has relationships to Inspection/Sale, " +
                "this guard will check for linked records to preserve audit trail.",
                "TASK_DONE_CANNOT_DELETE"
            );
        }

        auditService.log(AuditCategory.TASK, id, "DELETE", AuditLevel.HIGH,
                resolveCurrentEmployee(), "Task deleted: " + task.getTitle());

        taskRepository.delete(task);
        log.info("Deleted task ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> searchTasks(String search, Long assignedToId, TaskStatus status, TaskPriority priority,
                                                   LocalDate startDate, LocalDate endDate, int page, int size) {
        log.debug("Searching tasks with filters - search: {}, assignedTo: {}, status: {}, priority: {}, startDate: {}, endDate: {}",
                search, assignedToId, status, priority, startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Build specification for filtering
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), searchPattern),
                cb.like(cb.lower(root.get("description")), searchPattern)
            ));
        }

        if (assignedToId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assignedToId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }

        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
        }

        if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), endDateTime));
        }

        Page<Task> tasksPage = taskRepository.findAll(spec, pageable);

        return buildPageResponse(tasksPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasksByEmployee(Long employeeId, String search, int page, int size) {
        log.debug("Fetching tasks for employee ID: {} with search: {}", employeeId, search);

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Build specification with employee filter and optional text search
        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("assignee").get("id"), employeeId);
        
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), searchPattern),
                cb.like(cb.lower(root.get("description")), searchPattern)
            ));
        }
        
        Page<Task> tasksPage = taskRepository.findAll(spec, pageable);

        return buildPageResponse(tasksPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasksByStatus(TaskStatus status, int page, int size) {
        log.debug("Fetching tasks by status: {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> tasksPage = taskRepository.findByStatus(status, pageable);

        return buildPageResponse(tasksPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasksByPriority(TaskPriority priority, int page, int size) {
        log.debug("Fetching tasks by priority: {}", priority);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> tasksPage = taskRepository.findByPriority(priority, pageable);

        return buildPageResponse(tasksPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getOverdueTasks(int page, int size) {
        log.debug("Fetching overdue tasks");

        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate"));
        Specification<Task> spec = (root, query, cb) -> cb.and(
                cb.lessThan(root.get("dueDate"), LocalDateTime.now()),
                cb.notEqual(root.get("status"), TaskStatus.DONE)
        );
        Page<Task> tasksPage = taskRepository.findAll(spec, pageable);

        return buildPageResponse(tasksPage);
    }

    @Override
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        log.debug("Updating task ID: {} to status: {}", id, status);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        TaskStatus previousStatus = task.getStatus();
        task.setStatus(status);
        Task updated = taskRepository.save(task);

        auditService.log(AuditCategory.TASK, id, "STATUS_CHANGE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "From " + previousStatus + " to " + status);

        log.info("Updated task ID: {} to status: {}", id, status);
        return taskMapper.toResponse(updated);
    }

    @Override
    public TaskResponse assignTask(Long taskId, Long employeeId) {
        log.debug("Assigning task ID: {} to employee ID: {}", taskId, employeeId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        // Set assignee using employee reference
        task.setAssignee(employeeRepository.findById(employeeId).get());
        Task updated = taskRepository.save(task);

        log.info("Assigned task ID: {} to employee ID: {}", taskId, employeeId);
        return taskMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getTaskStatistics() {
        log.debug("Fetching task statistics");

        Map<String, Object> statistics = new HashMap<>();

        // Get counts by status
        Map<String, Long> statusCounts = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("status"), status);
            long count = taskRepository.count(spec);
            statusCounts.put(status.name(), count);
        }

        // Get counts by priority
        Map<String, Long> priorityCounts = new HashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("priority"), priority);
            long count = taskRepository.count(spec);
            priorityCounts.put(priority.name(), count);
        }

        statistics.put("statusCounts", statusCounts);
        statistics.put("priorityCounts", priorityCounts);
        statistics.put("totalTasks", taskRepository.count());

        // Get overdue count
        Specification<Task> overdueSpec = (root, query, cb) -> cb.and(
                cb.lessThan(root.get("dueDate"), LocalDateTime.now()),
                cb.notEqual(root.get("status"), TaskStatus.DONE)
        );
        long overdueCount = taskRepository.count(overdueSpec);
        statistics.put("overdueTasks", overdueCount);

        log.info("Retrieved task statistics");
        return statistics;
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }

    private PageResponse<TaskResponse> buildPageResponse(Page<Task> page) {
        List<TaskResponse> content = taskMapper.toResponseList(page.getContent());

        return PageResponse.<TaskResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}

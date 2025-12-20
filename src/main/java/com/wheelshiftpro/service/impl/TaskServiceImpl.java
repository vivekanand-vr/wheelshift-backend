package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.TaskMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.TaskRepository;
import com.wheelshiftpro.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    public TaskResponse createTask(TaskRequest request) {
        log.debug("Creating task: {}", request.getTitle());

        if (request.getAssigneeId() != null && !employeeRepository.existsById(request.getAssigneeId())) {
            throw new ResourceNotFoundException("Employee", "id", request.getAssigneeId());
        }

        Task task = taskMapper.toEntity(request);
        task.setStatus(TaskStatus.TODO);

        Task saved = taskRepository.save(task);

        log.info("Created task with ID: {}", saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.debug("Updating task ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        taskMapper.updateEntityFromRequest(request, task);
        Task updated = taskRepository.save(task);

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
    public PageResponse<TaskResponse> getAllTasks(int page, int size) {
        log.debug("Fetching all tasks - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> tasksPage = taskRepository.findAll(pageable);

        return buildPageResponse(tasksPage);
    }

    @Override
    public void deleteTask(Long id) {
        log.debug("Deleting task ID: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", "id", id);
        }

        taskRepository.deleteById(id);
        log.info("Deleted task ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> searchTasks(Long assignedToId, TaskStatus status, TaskPriority priority,
                                                   LocalDate startDate, LocalDate endDate, int page, int size) {
        log.debug("Searching tasks with filters - assignedTo: {}, status: {}, priority: {}, startDate: {}, endDate: {}",
                assignedToId, status, priority, startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Build specification for filtering
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();

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
    public PageResponse<TaskResponse> getTasksByEmployee(Long employeeId, int page, int size) {
        log.debug("Fetching tasks for employee ID: {}", employeeId);

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> tasksPage = taskRepository.findByAssigneeId(employeeId, pageable);

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

        task.setStatus(status);
        Task updated = taskRepository.save(task);

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

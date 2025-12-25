package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;

import java.time.LocalDate;

/**
 * Service interface for task management operations.
 */
public interface TaskService {

    /**
     * Creates a new task.
     *
     * @param request the task creation request
     * @return the created task response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if assigned employee not found
     */
    TaskResponse createTask(TaskRequest request);

    /**
     * Updates an existing task.
     *
     * @param id the task ID
     * @param request the update request
     * @return the updated task response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if task not found
     */
    TaskResponse updateTask(Long id, TaskRequest request);

    /**
     * Retrieves a task by ID.
     *
     * @param id the task ID
     * @return the task response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if task not found
     */
    TaskResponse getTaskById(Long id);

    /**
     * Retrieves all tasks with pagination.
     *
     * @param search optional search text to filter by title/description
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated task responses
     */
    PageResponse<TaskResponse> getAllTasks(String search, int page, int size);

    /**
     * Deletes a task.
     *
     * @param id the task ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if task not found
     */
    void deleteTask(Long id);

    /**
     * Searches tasks with multiple filters.
     *
     * @param search optional search text to filter by title/description
     * @param assignedToId optional assigned employee ID filter
     * @param status optional task status filter
     * @param priority optional task priority filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param page the page number
     * @param size the page size
     * @return paginated search results
     */
    PageResponse<TaskResponse> searchTasks(String search, Long assignedToId, TaskStatus status, TaskPriority priority,
                                           LocalDate startDate, LocalDate endDate, int page, int size);

    /**
     * Retrieves tasks assigned to a specific employee.
     *
     * @param employeeId the employee ID
     * @param search optional search text to filter by title/description (regex pattern)
     * @param page the page number
     * @param size the page size
     * @return paginated tasks assigned to the employee
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if employee not found
     */
    PageResponse<TaskResponse> getTasksByEmployee(Long employeeId, String search, int page, int size);

    /**
     * Retrieves tasks by status.
     *
     * @param status the task status
     * @param page the page number
     * @param size the page size
     * @return paginated tasks with the specified status
     */
    PageResponse<TaskResponse> getTasksByStatus(TaskStatus status, int page, int size);

    /**
     * Retrieves tasks by priority.
     *
     * @param priority the task priority
     * @param page the page number
     * @param size the page size
     * @return paginated tasks with the specified priority
     */
    PageResponse<TaskResponse> getTasksByPriority(TaskPriority priority, int page, int size);

    /**
     * Retrieves overdue tasks (due date passed and not completed).
     *
     * @param page the page number
     * @param size the page size
     * @return paginated overdue tasks
     */
    PageResponse<TaskResponse> getOverdueTasks(int page, int size);

    /**
     * Updates task status.
     *
     * @param id the task ID
     * @param status the new status
     * @return the updated task response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if task not found
     */
    TaskResponse updateTaskStatus(Long id, TaskStatus status);

    /**
     * Assigns a task to an employee.
     *
     * @param taskId the task ID
     * @param employeeId the employee ID
     * @return the updated task response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if task or employee not found
     */
    TaskResponse assignTask(Long taskId, Long employeeId);

    /**
     * Retrieves task statistics including counts by status and priority.
     *
     * @return task statistics
     */
    Object getTaskStatistics();
}

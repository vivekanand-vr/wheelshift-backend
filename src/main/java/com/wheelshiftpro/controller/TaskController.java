package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;
import com.wheelshiftpro.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "APIs for managing tasks and assignments")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates an existing task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks with pagination")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getAllTasks(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<TaskResponse> response = taskService.getAllTasks(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Deletes a task by ID")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Task deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search tasks", description = "Search tasks with multiple filter criteria")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> searchTasks(
            @Parameter(description = "Assigned employee ID filter") @RequestParam(required = false) Long assignedToId,
            @Parameter(description = "Task status filter") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Task priority filter") @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Start date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<TaskResponse> response = taskService.searchTasks(assignedToId, status, priority, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get tasks by employee", description = "Retrieves all tasks assigned to a specific employee")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasksByEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<TaskResponse> response = taskService.getTasksByEmployee(employeeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status", description = "Retrieves all tasks with a specific status")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasksByStatus(
            @Parameter(description = "Task status") @PathVariable TaskStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<TaskResponse> response = taskService.getTasksByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get tasks by priority", description = "Retrieves all tasks with a specific priority")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasksByPriority(
            @Parameter(description = "Task priority") @PathVariable TaskPriority priority,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<TaskResponse> response = taskService.getTasksByPriority(priority, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks", description = "Retrieves all overdue tasks")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getOverdueTasks(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageResponse<TaskResponse> response = taskService.getOverdueTasks(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Updates the status of a task")
    public ResponseEntity<ApiResponse<Void>> updateTaskStatus(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(description = "New task status") @RequestParam TaskStatus status) {
        taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<Void>success("Task status updated successfully", null));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign task", description = "Assigns a task to an employee")
    public ResponseEntity<ApiResponse<Void>> assignTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(description = "Employee ID") @RequestParam Long employeeId) {
        taskService.assignTask(id, employeeId);
        return ResponseEntity.ok(ApiResponse.<Void>success("Task assigned successfully", null));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get task statistics", description = "Retrieves task statistics by status and priority")
    public ResponseEntity<ApiResponse<Object>> getTaskStatistics() {
        Object statistics = taskService.getTaskStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}

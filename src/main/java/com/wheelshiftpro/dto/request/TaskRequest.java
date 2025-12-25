package com.wheelshiftpro.dto.request;

import com.wheelshiftpro.enums.TaskPriority;
import com.wheelshiftpro.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating or updating a task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 128, message = "Title must not exceed 128 characters")
    private String title;

    private String description;

    private TaskStatus status;

    private Long assigneeId;

    private LocalDateTime dueDate;

    private TaskPriority priority;

    private List<String> tags;
}

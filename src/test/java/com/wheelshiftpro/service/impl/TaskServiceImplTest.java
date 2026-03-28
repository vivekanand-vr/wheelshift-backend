package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.TaskRequest;
import com.wheelshiftpro.dto.response.TaskResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.EmployeeStatus;
import com.wheelshiftpro.enums.TaskStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.TaskMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.TaskRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl Tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TaskServiceImpl taskService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setUpAuthenticatedEmployee(Long employeeId) {
        Employee e = new Employee();
        e.setId(employeeId);
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(e);
    }

    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("should create task successfully with default status and priority")
        void happyPath() {
            Long assigneeId = 10L;
            LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
            TaskRequest request = TaskRequest.builder()
                    .title("Complete inspection")
                    .assigneeId(assigneeId)
                    .dueDate(futureDate)
                    .build();

            Employee assignee = new Employee();
            assignee.setId(assigneeId);
            assignee.setStatus(EmployeeStatus.ACTIVE);

            Task task = new Task();
            Task saved = new Task();
            saved.setId(100L);
            saved.setTitle("Complete inspection");

            TaskResponse response = TaskResponse.builder()
                    .id(100L)
                    .title("Complete inspection")
                    .build();

            when(employeeRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
            when(taskMapper.toEntity(request)).thenReturn(task);
            when(taskRepository.save(task)).thenReturn(saved);
            when(taskMapper.toResponse(saved)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            TaskResponse result = taskService.createTask(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(task.getAssignee()).isEqualTo(assignee);
            verify(auditService).log(eq(AuditCategory.TASK), eq(100L), eq("CREATE"), 
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should create task without assignee")
        void withoutAssignee() {
            TaskRequest request = TaskRequest.builder()
                    .title("General task")
                    .build();

            Task task = new Task();
            Task saved = new Task();
            saved.setId(100L);

            when(taskMapper.toEntity(request)).thenReturn(task);
            when(taskRepository.save(task)).thenReturn(saved);
            when(taskMapper.toResponse(saved)).thenReturn(new TaskResponse());

            verify(employeeRepository, never()).findById(any());
            assertThat(task.getAssignee()).isNull();
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("should throw exception when due date is in the past")
        void dueDateInPast() {
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
            TaskRequest request = TaskRequest.builder()
                    .title("Task")
                    .dueDate(pastDate)
                    .build();

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Due date must be in the future");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when assignee not found")
        void assigneeNotFound() {
            TaskRequest request = TaskRequest.builder()
                    .title("Task")
                    .assigneeId(999L)
                    .build();

            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee")
                    .hasMessageContaining("999");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when assignee is not ACTIVE")
        void assigneeNotActive() {
            Long assigneeId = 10L;
            TaskRequest request = TaskRequest.builder()
                    .title("Task")
                    .assigneeId(assigneeId)
                    .build();

            Employee assignee = new Employee();
            assignee.setId(assigneeId);
            assignee.setStatus(EmployeeStatus.INACTIVE);

            when(employeeRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ACTIVE");

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTask {

        @Test
        @DisplayName("should update task successfully")
        void happyPath() {
            Long taskId = 1L;
            Long assigneeId = 10L;
            LocalDateTime futureDate = LocalDateTime.now().plusDays(5);
            TaskRequest request = TaskRequest.builder()
                    .title("Updated task")
                    .assigneeId(assigneeId)
                    .dueDate(futureDate)
                    .build();

            Task task = new Task();
            task.setId(taskId);

            Employee assignee = new Employee();
            assignee.setId(assigneeId);
            assignee.setStatus(EmployeeStatus.ACTIVE);

            TaskResponse response = TaskResponse.builder()
                    .id(taskId)
                    .title("Updated task")
                    .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(employeeRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            TaskResponse result = taskService.updateTask(taskId, request);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated task");
            verify(taskMapper).updateEntityFromRequest(request, task);
            assertThat(task.getAssignee()).isEqualTo(assignee);
            verify(auditService).log(eq(AuditCategory.TASK), eq(taskId), eq("UPDATE"), 
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should clear assignee when null in request")
        void clearAssignee() {
            Long taskId = 1L;
            TaskRequest request = TaskRequest.builder()
                    .title("Task")
                    .assigneeId(null)
                    .build();

            Task task = new Task();
            task.setId(taskId);
            Employee oldAssignee = new Employee();
            task.setAssignee(oldAssignee);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(new TaskResponse());

            taskService.updateTask(taskId, request);

            assertThat(task.getAssignee()).isNull();
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void taskNotFound() {
            TaskRequest request = TaskRequest.builder().build();

            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when due date is in the past")
        void dueDateInPast() {
            Long taskId = 1L;
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
            TaskRequest request = TaskRequest.builder()
                    .dueDate(pastDate)
                    .build();

            Task task = new Task();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> taskService.updateTask(taskId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Due date must be in the future");

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("should delete task successfully")
        void happyPath() {
            Long taskId = 1L;
            Task task = new Task();
            task.setId(taskId);
            task.setStatus(TaskStatus.TODO);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            setUpAuthenticatedEmployee(50L);

            taskService.deleteTask(taskId);

            verify(auditService).log(eq(AuditCategory.TASK), eq(taskId), eq("DELETE"), 
                    eq(AuditLevel.HIGH), any(), any());
            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void taskNotFound() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deleteTask(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task");

            verify(taskRepository, never()).delete(any(Task.class));
        }

        @Test
        @DisplayName("should allow deletion of DONE task with warning in future")
        void doneTaskDeletion() {
            Long taskId = 1L;
            Task task = new Task();
            task.setId(taskId);
            task.setStatus(TaskStatus.DONE);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            taskService.deleteTask(taskId);

            verify(taskRepository).delete(task);
        }
    }

    @Nested
    @DisplayName("updateTaskStatus")
    class UpdateTaskStatus {

        @Test
        @DisplayName("should update status successfully")
        void happyPath() {
            Long taskId = 1L;
            Task task = new Task();
            task.setId(taskId);
            task.setStatus(TaskStatus.TODO);

            TaskResponse response = TaskResponse.builder()
                    .id(taskId)
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            TaskResponse result = taskService.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);

            assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
            verify(auditService).log(eq(AuditCategory.TASK), eq(taskId), eq("STATUS_CHANGE"), 
                    eq(AuditLevel.REGULAR), any(), detailsCaptor.capture());
            assertThat(detailsCaptor.getValue()).contains("TODO").contains("IN_PROGRESS");
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void taskNotFound() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTaskStatus(999L, TaskStatus.DONE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task");

            verify(taskRepository, never()).save(any());
        }
    }
}

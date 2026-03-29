package com.wheelshiftpro.scheduler;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.enums.TaskStatus;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.TaskRepository;
import com.wheelshiftpro.service.notifications.NotificationEventHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OverdueTaskScheduler")
class OverdueTaskSchedulerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private NotificationEventHelper notificationEventHelper;

    @InjectMocks
    private OverdueTaskScheduler scheduler;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Employee buildEmployee(Long id) {
        Employee e = new Employee();
        e.setId(id);
        e.setName("Employee " + id);
        return e;
    }

    private Task buildTask(Long id, Employee assignee) {
        Task t = new Task();
        t.setId(id);
        t.setTitle("Task " + id);
        t.setStatus(TaskStatus.IN_PROGRESS);
        t.setDueDate(LocalDateTime.now().minusDays(1));
        t.setAssignee(assignee);
        return t;
    }

    // ── test cases ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("detectOverdueTasks")
    class DetectOverdueTasks {

        @Test
        @DisplayName("no overdue tasks — no notifications dispatched")
        void noOverdueTasks_noNotifications() {
            when(taskRepository.findOverdueTasks(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            scheduler.detectOverdueTasks();

            verify(notificationEventHelper, never())
                    .notifyEmployee(anyLong(), any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("overdue task with assignee — assignee notified with TASK_OVERDUE")
        void overdueTask_assigneeNotified() {
            Employee assignee = buildEmployee(5L);
            Task task = buildTask(1L, assignee);
            when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(task));
            when(employeeRepository.findByRoles_Name(any())).thenReturn(Collections.emptyList());

            scheduler.detectOverdueTasks();

            verify(notificationEventHelper).notifyEmployee(
                    eq(5L), eq(NotificationEventType.TASK_OVERDUE), eq("TASK"), eq(1L), any());
        }

        @Test
        @DisplayName("overdue task without assignee — only managers notified")
        void overdueTask_noAssignee_onlyManagersNotified() {
            Task task = buildTask(2L, null);
            when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(task));

            Employee admin = buildEmployee(10L);
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(List.of(admin));
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(Collections.emptyList());

            scheduler.detectOverdueTasks();

            verify(notificationEventHelper, times(1))
                    .notifyEmployee(eq(10L), eq(NotificationEventType.TASK_OVERDUE), eq("TASK"), eq(2L), any());
        }

        @Test
        @DisplayName("manager who is also the assignee — notified only once (no duplicate)")
        void managerIsAssignee_notifiedOnce() {
            Employee managerAssignee = buildEmployee(20L);
            Task task = buildTask(3L, managerAssignee);
            when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(task));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(List.of(managerAssignee));
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(Collections.emptyList());

            scheduler.detectOverdueTasks();

            // Assignee path notifies once; manager-is-assignee path is skipped
            verify(notificationEventHelper, times(1))
                    .notifyEmployee(eq(20L), eq(NotificationEventType.TASK_OVERDUE), eq("TASK"), eq(3L), any());
        }

        @Test
        @DisplayName("payload contains taskId, taskTitle, dueDate, assigneeName")
        void payloadContainsRequiredFields() {
            Employee assignee = buildEmployee(7L);
            Task task = buildTask(4L, assignee);
            when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(task));
            when(employeeRepository.findByRoles_Name(any())).thenReturn(Collections.emptyList());

            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            scheduler.detectOverdueTasks();

            verify(notificationEventHelper).notifyEmployee(
                    eq(7L), eq(NotificationEventType.TASK_OVERDUE), eq("TASK"), eq(4L), captor.capture());

            assertThat(captor.getValue())
                    .containsKey("taskId")
                    .containsKey("taskTitle")
                    .containsKey("dueDate")
                    .containsEntry("assigneeName", "Employee 7");
        }

        @Test
        @DisplayName("multiple overdue tasks — notifications dispatched per task")
        void multipleOverdueTasks_allNotified() {
            Employee assignee = buildEmployee(1L);
            Task t1 = buildTask(10L, assignee);
            Task t2 = buildTask(11L, assignee);
            when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(t1, t2));
            when(employeeRepository.findByRoles_Name(any())).thenReturn(Collections.emptyList());

            scheduler.detectOverdueTasks();

            verify(notificationEventHelper, times(1))
                    .notifyEmployee(eq(1L), eq(NotificationEventType.TASK_OVERDUE), eq("TASK"), eq(10L), any());
            verify(notificationEventHelper, times(1))
                    .notifyEmployee(eq(1L), eq(NotificationEventType.TASK_OVERDUE), eq("TASK"), eq(11L), any());
        }

        @Test
        @DisplayName("deduplication: employee in multiple manager roles — notified once per task")
        void managerInMultipleRoles_deduplicatedPerTask() {
            Task task = buildTask(5L, null);
            when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(List.of(task));

            Employee multiRole = buildEmployee(50L);
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(List.of(multiRole));
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(List.of(multiRole));
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(Collections.emptyList());

            scheduler.detectOverdueTasks();

            verify(notificationEventHelper, times(1))
                    .notifyEmployee(eq(50L), eq(NotificationEventType.TASK_OVERDUE), eq("TASK"), eq(5L), any());
        }
    }
}

package com.wheelshiftpro.scheduler;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Task;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.TaskRepository;
import com.wheelshiftpro.service.notifications.NotificationEventHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scheduled job that runs daily at 08:00 and detects tasks whose
 * {@code dueDate} has passed while still not in {@code DONE} status.
 *
 * For each overdue task the job dispatches a {@code TASK_OVERDUE} notification
 * to the task assignee (if set) and to every ADMIN, SUPER_ADMIN, and
 * STORE_MANAGER employee.
 *
 * BL 13.3 — Overdue task detection.
 *
 * Protected by ShedLock so only one node fires the job in a clustered
 * deployment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueTaskScheduler {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationEventHelper notificationEventHelper;

    @Scheduled(cron = "${task.overdue.cron:0 0 8 * * *}")
    @SchedulerLock(name = "OverdueTaskScheduler", lockAtMostFor = "1h", lockAtLeastFor = "5m")
    @Transactional(readOnly = true)
    public void detectOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskRepository.findOverdueTasks(now);

        if (overdueTasks.isEmpty()) {
            log.debug("No overdue tasks found at {}", now);
            return;
        }

        log.info("Found {} overdue task(s) — dispatching notifications", overdueTasks.size());

        List<Employee> managers = resolveManagerRecipients();

        for (Task task : overdueTasks) {
            Map<String, Object> payload = buildPayload(task);

            // Notify the task assignee
            if (task.getAssignee() != null) {
                notificationEventHelper.notifyEmployee(
                        task.getAssignee().getId(),
                        NotificationEventType.TASK_OVERDUE,
                        "TASK",
                        task.getId(),
                        payload
                );
            }

            // Notify all Admin / Manager employees (BL 13.3 — "Notify Admin/Manager")
            for (Employee manager : managers) {
                // Skip if this manager is also the assignee to avoid duplicate notification
                if (task.getAssignee() != null && manager.getId().equals(task.getAssignee().getId())) {
                    continue;
                }
                notificationEventHelper.notifyEmployee(
                        manager.getId(),
                        NotificationEventType.TASK_OVERDUE,
                        "TASK",
                        task.getId(),
                        payload
                );
            }
        }

        log.info("Overdue task notifications dispatched for {} task(s)", overdueTasks.size());
    }

    private Map<String, Object> buildPayload(Task task) {
        String assigneeName = task.getAssignee() != null ? task.getAssignee().getName() : null;
        return Map.of(
                "taskId", task.getId(),
                "taskTitle", task.getTitle(),
                "dueDate", task.getDueDate() != null ? task.getDueDate().toString() : "",
                "assigneeName", assigneeName != null ? assigneeName : "Unassigned"
        );
    }

    /**
     * Collects the unique set of employees with ADMIN or SUPER_ADMIN or STORE_MANAGER
     * roles (BL 13.3 — "Notify Admin/Manager").
     */
    private List<Employee> resolveManagerRecipients() {
        List<Employee> recipients = new ArrayList<>();
        for (RoleType role : List.of(RoleType.ADMIN, RoleType.SUPER_ADMIN, RoleType.STORE_MANAGER)) {
            for (Employee e : employeeRepository.findByRoles_Name(role)) {
                if (recipients.stream().noneMatch(r -> r.getId().equals(e.getId()))) {
                    recipients.add(e);
                }
            }
        }
        return recipients;
    }
}

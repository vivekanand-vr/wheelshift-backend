package com.wheelshiftpro.scheduler;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Event;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.EventRepository;
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
 * Scheduled job that runs every hour and sends {@code EVENT_REMINDER}
 * notifications for events starting within the next 24 hours and within
 * the next 1 hour.
 *
 * BL 14.2 — Event reminders.
 *
 * Two sliding windows are evaluated on each run:
 * <ul>
 *   <li><b>24 h window</b> — events whose {@code startTime} is in
 *       [now + 23 h, now + 25 h)</li>
 *   <li><b>1 h window</b> — events whose {@code startTime} is in
 *       [now + 30 m, now + 90 m)</li>
 * </ul>
 *
 * The {@code Event} entity does not carry a direct creator / linked-employee
 * relationship. Reminders are therefore broadcast to all employees with
 * STORE_MANAGER, ADMIN, and SUPER_ADMIN roles — the roles authorised to
 * create and manage events per BL 14.1.
 *
 * Protected by ShedLock so only one node fires the job in a clustered
 * deployment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventReminderScheduler {

    private final EventRepository eventRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationEventHelper notificationEventHelper;

    @Scheduled(fixedDelayString = "${event.reminder.check-interval-ms:3600000}")
    @SchedulerLock(name = "EventReminderScheduler", lockAtMostFor = "55m", lockAtLeastFor = "5m")
    @Transactional(readOnly = true)
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();

        sendRemindersForWindow(now.plusHours(23), now.plusHours(25), "24 h");
        sendRemindersForWindow(now.plusMinutes(30), now.plusMinutes(90), "1 h");
    }

    private void sendRemindersForWindow(LocalDateTime windowStart, LocalDateTime windowEnd,
                                        String windowLabel) {
        List<Event> events = eventRepository.findEventsBetween(windowStart, windowEnd);
        if (events.isEmpty()) {
            log.debug("No events in the {} reminder window ({} – {})", windowLabel, windowStart, windowEnd);
            return;
        }

        log.info("Sending {} reminder notification(s) for {} upcoming event(s) in the {} window",
                events.size(), events.size(), windowLabel);

        List<Employee> recipients = resolveManagementRecipients();
        if (recipients.isEmpty()) {
            log.debug("No management employees found — skipping event reminder notifications");
            return;
        }

        for (Event event : events) {
            Map<String, Object> payload = Map.of(
                    "eventId", event.getId(),
                    "eventTitle", event.getTitle() != null ? event.getTitle() : event.getName() != null ? event.getName() : "",
                    "eventType", event.getType(),
                    "startTime", event.getStartTime().toString(),
                    "reminderWindow", windowLabel
            );

            for (Employee recipient : recipients) {
                notificationEventHelper.notifyEmployee(
                        recipient.getId(),
                        NotificationEventType.EVENT_REMINDER,
                        "EVENT",
                        event.getId(),
                        payload
                );
            }
        }
    }

    /**
     * Collects the unique set of STORE_MANAGER, ADMIN, and SUPER_ADMIN employees
     * who should receive event reminders (holders of the roles that can create
     * events per BL 14.1 — "SA, AD, SM, SL").
     */
    private List<Employee> resolveManagementRecipients() {
        List<Employee> recipients = new ArrayList<>();
        for (RoleType role : List.of(RoleType.STORE_MANAGER, RoleType.ADMIN, RoleType.SUPER_ADMIN)) {
            for (Employee e : employeeRepository.findByRoles_Name(role)) {
                if (recipients.stream().noneMatch(r -> r.getId().equals(e.getId()))) {
                    recipients.add(e);
                }
            }
        }
        return recipients;
    }
}

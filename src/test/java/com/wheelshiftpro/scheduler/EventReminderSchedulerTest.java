package com.wheelshiftpro.scheduler;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Event;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.EventRepository;
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
@DisplayName("EventReminderScheduler")
class EventReminderSchedulerTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private NotificationEventHelper notificationEventHelper;

    @InjectMocks
    private EventReminderScheduler scheduler;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Employee buildEmployee(Long id) {
        Employee e = new Employee();
        e.setId(id);
        e.setName("Employee " + id);
        return e;
    }

    private Event buildEvent(Long id, String title, LocalDateTime startTime) {
        Event e = new Event();
        e.setId(id);
        e.setTitle(title);
        e.setType("CUSTOM");
        e.setStartTime(startTime);
        return e;
    }

    // ── test cases ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendEventReminders")
    class SendEventReminders {

        @Test
        @DisplayName("no events in either window — no notifications dispatched")
        void noEvents_noNotifications() {
            when(eventRepository.findEventsBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            scheduler.sendEventReminders();

            verify(notificationEventHelper, never())
                    .notifyEmployee(anyLong(), any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("event in 24h window — notifies all management employees")
        void eventIn24hWindow_notifiesManagement() {
            LocalDateTime startIn24h = LocalDateTime.now().plusHours(24);
            Event event = buildEvent(1L, "Review Meeting", startIn24h);

            // 24h window returns this event; 1h window returns nothing
            when(eventRepository.findEventsBetween(any(), any()))
                    .thenReturn(List.of(event))
                    .thenReturn(Collections.emptyList());

            Employee sm = buildEmployee(10L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(List.of(sm));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            scheduler.sendEventReminders();

            verify(notificationEventHelper).notifyEmployee(
                    eq(10L), eq(NotificationEventType.EVENT_REMINDER), eq("EVENT"), eq(1L), any());
        }

        @Test
        @DisplayName("event in 1h window — notifies all management employees")
        void eventIn1hWindow_notifiesManagement() {
            LocalDateTime startIn1h = LocalDateTime.now().plusHours(1);
            Event event = buildEvent(2L, "Vehicle Handover", startIn1h);

            // 24h window empty; 1h window returns the event
            when(eventRepository.findEventsBetween(any(), any()))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(List.of(event));

            Employee admin = buildEmployee(20L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(List.of(admin));
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            scheduler.sendEventReminders();

            verify(notificationEventHelper).notifyEmployee(
                    eq(20L), eq(NotificationEventType.EVENT_REMINDER), eq("EVENT"), eq(2L), any());
        }

        @Test
        @DisplayName("no management employees — no notifications even if events exist")
        void noManagementEmployees_noNotifications() {
            Event event = buildEvent(3L, "Test Drive", LocalDateTime.now().plusHours(24));
            when(eventRepository.findEventsBetween(any(), any()))
                    .thenReturn(List.of(event))
                    .thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(any())).thenReturn(Collections.emptyList());

            scheduler.sendEventReminders();

            verify(notificationEventHelper, never())
                    .notifyEmployee(anyLong(), any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("payload contains eventId, eventTitle, startTime, reminderWindow")
        void payloadContainsRequiredFields() {
            LocalDateTime startTime = LocalDateTime.now().plusHours(24);
            Event event = buildEvent(4L, "Inspection", startTime);

            when(eventRepository.findEventsBetween(any(), any()))
                    .thenReturn(List.of(event))
                    .thenReturn(Collections.emptyList());

            Employee recipient = buildEmployee(30L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(List.of(recipient));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            scheduler.sendEventReminders();

            verify(notificationEventHelper).notifyEmployee(
                    eq(30L), eq(NotificationEventType.EVENT_REMINDER), eq("EVENT"), eq(4L), captor.capture());

            assertThat(captor.getValue())
                    .containsEntry("eventId", 4L)
                    .containsEntry("eventTitle", "Inspection")
                    .containsKey("startTime")
                    .containsKey("reminderWindow");
        }

        @Test
        @DisplayName("event with null title falls back to name field")
        void eventWithNullTitle_fallsBackToName() {
            LocalDateTime startTime = LocalDateTime.now().plusHours(24);
            Event event = buildEvent(5L, null, startTime);
            event.setName("Fallback Name");

            when(eventRepository.findEventsBetween(any(), any()))
                    .thenReturn(List.of(event))
                    .thenReturn(Collections.emptyList());

            Employee recipient = buildEmployee(40L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(List.of(recipient));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            scheduler.sendEventReminders();

            verify(notificationEventHelper).notifyEmployee(
                    eq(40L), eq(NotificationEventType.EVENT_REMINDER), eq("EVENT"), eq(5L), captor.capture());

            assertThat(captor.getValue()).containsEntry("eventTitle", "Fallback Name");
        }

        @Test
        @DisplayName("employee in multiple roles — notified only once per event")
        void deduplicatedRecipient_notifiedOncePerEvent() {
            Event event = buildEvent(6L, "Duplicate Test", LocalDateTime.now().plusHours(24));
            when(eventRepository.findEventsBetween(any(), any()))
                    .thenReturn(List.of(event))
                    .thenReturn(Collections.emptyList());

            Employee dual = buildEmployee(99L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(List.of(dual));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(List.of(dual));
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            scheduler.sendEventReminders();

            verify(notificationEventHelper, times(1))
                    .notifyEmployee(eq(99L), eq(NotificationEventType.EVENT_REMINDER),
                            eq("EVENT"), eq(6L), any());
        }

        @Test
        @DisplayName("events in both windows — each window produces its own notifications")
        void eventsInBothWindows_bothWindowsNotified() {
            Event event24h = buildEvent(7L, "24h Event", LocalDateTime.now().plusHours(24));
            Event event1h  = buildEvent(8L, "1h Event",  LocalDateTime.now().plusHours(1));

            when(eventRepository.findEventsBetween(any(), any()))
                    .thenReturn(List.of(event24h))
                    .thenReturn(List.of(event1h));

            Employee recipient = buildEmployee(50L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(List.of(recipient));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            scheduler.sendEventReminders();

            // One notification per event per recipient = 2 total
            verify(notificationEventHelper, times(2))
                    .notifyEmployee(eq(50L), eq(NotificationEventType.EVENT_REMINDER), eq("EVENT"), anyLong(), any());
        }
    }
}

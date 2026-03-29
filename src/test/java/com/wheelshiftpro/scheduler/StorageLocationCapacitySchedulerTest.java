package com.wheelshiftpro.scheduler;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.StorageLocationRepository;
import com.wheelshiftpro.service.notifications.NotificationEventHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@DisplayName("StorageLocationCapacityScheduler")
class StorageLocationCapacitySchedulerTest {

    @Mock
    private StorageLocationRepository storageLocationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private NotificationEventHelper notificationEventHelper;

    @InjectMocks
    private StorageLocationCapacityScheduler scheduler;

    // ── helpers ──────────────────────────────────────────────────────────────

    private StorageLocation buildLocation(Long id, int total, int cars, int motos) {
        StorageLocation loc = new StorageLocation();
        loc.setId(id);
        loc.setName("Location " + id);
        loc.setTotalCapacity(total);
        loc.setCurrentCarCount(cars);
        loc.setCurrentMotorcycleCount(motos);
        return loc;
    }

    private Employee buildEmployee(Long id) {
        Employee e = new Employee();
        e.setId(id);
        e.setName("Employee " + id);
        return e;
    }

    // ── test cases ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkCapacityThresholds")
    class CheckCapacityThresholds {

        @Test
        @DisplayName("no locations — no notifications dispatched")
        void noLocations_noNotifications() {
            when(storageLocationRepository.findAll()).thenReturn(Collections.emptyList());

            scheduler.checkCapacityThresholds();

            verify(notificationEventHelper, never())
                    .notifyEmployee(anyLong(), any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("locations all below 80% threshold — no notifications dispatched")
        void belowThreshold_noNotifications() {
            // 70 % utilisation = 7/10
            StorageLocation below = buildLocation(1L, 10, 5, 2);
            when(storageLocationRepository.findAll()).thenReturn(List.of(below));

            scheduler.checkCapacityThresholds();

            verify(notificationEventHelper, never())
                    .notifyEmployee(anyLong(), any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("location at exactly 80% — notifies all management employees")
        void atEightyPercent_notifiesManagement() {
            // 8/10 = 80 %
            StorageLocation atThreshold = buildLocation(1L, 10, 6, 2);
            when(storageLocationRepository.findAll()).thenReturn(List.of(atThreshold));

            Employee sm = buildEmployee(10L);
            Employee admin = buildEmployee(20L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(List.of(sm));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(List.of(admin));
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            scheduler.checkCapacityThresholds();

            verify(notificationEventHelper, times(2))
                    .notifyEmployee(anyLong(), eq(NotificationEventType.LOCATION_NEAR_CAPACITY),
                            eq("STORAGE_LOCATION"), eq(1L), any());
        }

        @Test
        @DisplayName("location at 100% — payload contains isFull=true")
        void atFullCapacity_payloadContainsIsFull() {
            // 10/10 = 100 %
            StorageLocation full = buildLocation(1L, 10, 8, 2);
            when(storageLocationRepository.findAll()).thenReturn(List.of(full));

            Employee manager = buildEmployee(10L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(List.of(manager));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(Collections.emptyList());
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            scheduler.checkCapacityThresholds();

            verify(notificationEventHelper).notifyEmployee(
                    eq(10L), eq(NotificationEventType.LOCATION_NEAR_CAPACITY),
                    eq("STORAGE_LOCATION"), eq(1L), payloadCaptor.capture());

            assertThat(payloadCaptor.getValue())
                    .containsEntry("isFull", true)
                    .containsEntry("currentCount", 10)
                    .containsEntry("totalCapacity", 10);
        }

        @Test
        @DisplayName("no management employees — no notifications dispatched even above threshold")
        void noManagementEmployees_noNotifications() {
            // 9/10 = 90 % — above threshold
            StorageLocation above = buildLocation(1L, 10, 7, 2);
            when(storageLocationRepository.findAll()).thenReturn(List.of(above));
            when(employeeRepository.findByRoles_Name(any())).thenReturn(Collections.emptyList());

            scheduler.checkCapacityThresholds();

            verify(notificationEventHelper, never())
                    .notifyEmployee(anyLong(), any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("employee appears in multiple roles — notified only once per location")
        void deduplicatedRecipient_notifiedOnce() {
            // 9/10 = 90 %
            StorageLocation above = buildLocation(1L, 10, 7, 2);
            when(storageLocationRepository.findAll()).thenReturn(List.of(above));

            Employee dual = buildEmployee(99L);
            when(employeeRepository.findByRoles_Name(RoleType.STORE_MANAGER)).thenReturn(List.of(dual));
            when(employeeRepository.findByRoles_Name(RoleType.ADMIN)).thenReturn(List.of(dual));
            when(employeeRepository.findByRoles_Name(RoleType.SUPER_ADMIN)).thenReturn(Collections.emptyList());

            scheduler.checkCapacityThresholds();

            verify(notificationEventHelper, times(1))
                    .notifyEmployee(eq(99L), eq(NotificationEventType.LOCATION_NEAR_CAPACITY),
                            eq("STORAGE_LOCATION"), eq(1L), any());
        }
    }
}

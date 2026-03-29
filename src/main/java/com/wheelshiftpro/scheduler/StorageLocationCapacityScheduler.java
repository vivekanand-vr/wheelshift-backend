package com.wheelshiftpro.scheduler;

import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.StorageLocation;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.rbac.RoleType;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.StorageLocationRepository;
import com.wheelshiftpro.service.notifications.NotificationEventHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scheduled job that evaluates storage-location utilisation every hour and
 * dispatches {@code LOCATION_NEAR_CAPACITY} notifications to all
 * STORE_MANAGER, ADMIN, and SUPER_ADMIN employees when a location reaches
 * 80 % or 100 % of its {@code totalCapacity}.
 *
 * BL 6.4 — Capacity threshold alerts.
 *
 * Protected by ShedLock so only one node fires the job in a clustered
 * deployment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StorageLocationCapacityScheduler {

    private static final double NEAR_CAPACITY_THRESHOLD = 0.80;

    private final StorageLocationRepository storageLocationRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationEventHelper notificationEventHelper;

    @Scheduled(fixedDelayString = "${storage.capacity.check-interval-ms:3600000}")
    @SchedulerLock(name = "StorageLocationCapacityScheduler", lockAtMostFor = "55m", lockAtLeastFor = "5m")
    @Transactional(readOnly = true)
    public void checkCapacityThresholds() {
        List<StorageLocation> locations = storageLocationRepository.findAll();
        if (locations.isEmpty()) {
            log.debug("No storage locations found — skipping capacity check");
            return;
        }

        List<Employee> recipients = resolveManagementRecipients();
        if (recipients.isEmpty()) {
            log.debug("No management employees found — skipping capacity notifications");
            return;
        }

        int alertCount = 0;
        for (StorageLocation location : locations) {
            int total = location.getTotalCapacity();
            if (total <= 0) {
                continue;
            }

            int current = location.getCurrentVehicleCount();
            double utilisation = (double) current / total;

            if (utilisation < NEAR_CAPACITY_THRESHOLD) {
                continue;
            }

            boolean isFull = current >= total;
            log.info("Storage location id={} '{}' is {}% full ({}/{}) — notifying {} management employees",
                    location.getId(), location.getName(),
                    Math.round(utilisation * 100), current, total, recipients.size());

            Map<String, Object> payload = Map.of(
                    "locationId", location.getId(),
                    "locationName", location.getName(),
                    "currentCount", current,
                    "totalCapacity", total,
                    "isFull", isFull
            );

            for (Employee recipient : recipients) {
                notificationEventHelper.notifyEmployee(
                        recipient.getId(),
                        NotificationEventType.LOCATION_NEAR_CAPACITY,
                        "STORAGE_LOCATION",
                        location.getId(),
                        payload
                );
            }
            alertCount++;
        }

        if (alertCount > 0) {
            log.info("Capacity threshold alerts dispatched for {} location(s)", alertCount);
        } else {
            log.debug("All storage locations are below the {}% capacity threshold", (int) (NEAR_CAPACITY_THRESHOLD * 100));
        }
    }

    /**
     * Collects the unique set of employees with STORE_MANAGER, ADMIN, or SUPER_ADMIN
     * roles who should receive capacity alerts (BL 6.4 — "notify SM and AD").
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

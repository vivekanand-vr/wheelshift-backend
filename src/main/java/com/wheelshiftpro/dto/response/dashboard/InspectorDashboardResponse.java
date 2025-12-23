package com.wheelshiftpro.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inspector dashboard response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectorDashboardResponse {
    private InspectionQueue inspectionQueue;
    private PersonalStats personalStats;
    private VehicleStatus vehicleStatus;
    private AssignedTasks assignedTasks;
    private List<LocationSummary> locationSummary;
    private List<RecentInspection> recentInspections;
    private AdminDashboardResponse.NotificationsWidget notifications;

    /**
     * Inspection queue metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectionQueue {
        private Long pendingInspections;
        private Long scheduledToday;
        private Long scheduledThisWeek;
        private Long overdue;
    }

    /**
     * Personal statistics for inspector
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalStats {
        private Long completedThisMonth;
        private Long totalCompleted;
        private Double passRate;
        private Double avgInspectionTime;
        private BigDecimal avgRepairCost;
    }

    /**
     * Vehicle status metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleStatus {
        private Long needingInspection;
        private Long failedInspections;
        private Long inMaintenance;
    }

    /**
     * Assigned tasks summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedTasks {
        private Long total;
        private Long highPriority;
        private Long dueToday;
    }

    /**
     * Location summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationSummary {
        private Long locationId;
        private String locationName;
        private Long vehicleCount;
        private Long pendingInspections;
    }

    /**
     * Recent inspection
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentInspection {
        private Long inspectionId;
        private Long carId;
        private String vinNumber;
        private String make;
        private String model;
        private String inspectionDate;
        private Boolean inspectionPass;
        private BigDecimal estimatedRepairCost;
    }
}

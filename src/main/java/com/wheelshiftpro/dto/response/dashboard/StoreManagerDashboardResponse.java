package com.wheelshiftpro.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Store Manager dashboard response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreManagerDashboardResponse {
    private LocationOverview locationOverview;
    private VehicleDistribution vehicleDistribution;
    private MovementActivity movements;
    private CapacityAlerts capacityAlerts;
    private MaintenanceStatus maintenanceStatus;
    private LocationPerformance performance;
    private AdminDashboardResponse.NotificationsWidget notifications;

    /**
     * Location overview metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationOverview {
        private Long totalLocations;
        private Long managedLocations;
        private Long totalCapacity;
        private Long currentOccupancy;
        private Double utilizationRate;
    }

    /**
     * Vehicle distribution metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleDistribution {
        private Map<String, Long> byLocation;
        private Map<String, Long> byStatus;
    }

    /**
     * Movement activity
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovementActivity {
        private Long todayMovements;
        private Long thisWeekMovements;
        private Long pendingTransfers;
        private List<RecentMovement> recentMovements;
    }

    /**
     * Recent movement
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentMovement {
        private Long movementId;
        private Long carId;
        private String vinNumber;
        private String fromLocation;
        private String toLocation;
        private String movedAt;
        private String movedBy;
    }

    /**
     * Capacity alerts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacityAlerts {
        private Long nearFullLocations;
        private Long underutilizedLocations;
        private List<CapacityRecommendation> recommendations;
    }

    /**
     * Capacity recommendation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacityRecommendation {
        private String type;
        private String locationName;
        private String message;
        private Long currentOccupancy;
        private Long capacity;
    }

    /**
     * Maintenance status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceStatus {
        private Long vehiclesInMaintenance;
        private Map<String, Long> byLocation;
        private Double avgMaintenanceTime;
    }

    /**
     * Location performance
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationPerformance {
        private Double avgTurnoverDays;
        private String fastestMovingCategory;
        private String slowestMovingCategory;
    }
}

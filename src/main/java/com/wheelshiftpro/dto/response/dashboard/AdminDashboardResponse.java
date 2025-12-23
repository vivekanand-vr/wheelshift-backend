package com.wheelshiftpro.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Admin dashboard response containing all dashboard widgets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private OverviewStats overview;
    private RevenueMetrics revenue;
    private InventoryHealth inventory;
    private List<ActivityLog> recentActivities;
    private List<EmployeePerformance> topEmployees;
    private SystemAlerts alerts;
    private NotificationsWidget notifications;

    /**
     * Overview statistics widget
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewStats {
        private Long totalCars;
        private Long availableCars;
        private Long reservedCars;
        private Long soldCarsThisMonth;
        private Long activeInquiries;
        private Long activeReservations;
        private Long totalEmployees;
        private Long activeEmployees;
    }

    /**
     * Revenue metrics widget
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueMetrics {
        private BigDecimal totalRevenue;
        private BigDecimal monthlyRevenue;
        private BigDecimal ytdRevenue;
        private BigDecimal averageSalePrice;
        private List<MonthlyRevenue> revenueTrend;
    }

    /**
     * Monthly revenue data point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private Integer month;
        private String monthName;
        private BigDecimal revenue;
        private Long salesCount;
    }

    /**
     * Inventory health metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryHealth {
        private Map<String, Long> byStatus;
        private BigDecimal totalValue;
        private Double avgAge;
        private List<AgingInventoryItem> agingInventory;
    }

    /**
     * Aging inventory item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingInventoryItem {
        private Long carId;
        private String vinNumber;
        private String make;
        private String model;
        private Integer year;
        private Integer daysInInventory;
        private BigDecimal purchasePrice;
    }

    /**
     * Activity log entry
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLog {
        private Long id;
        private String type;
        private String description;
        private String entityType;
        private String entityId;
        private String timestamp;
        private String performedBy;
    }

    /**
     * Employee performance metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeePerformance {
        private Long employeeId;
        private String employeeName;
        private String position;
        private Long salesCount;
        private BigDecimal totalCommission;
        private BigDecimal totalRevenue;
    }

    /**
     * System alerts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemAlerts {
        private Long expiringReservations;
        private Long inspectionsDue;
        private Long locationCapacityWarnings;
        private List<AlertDetail> details;
    }

    /**
     * Alert detail
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertDetail {
        private String type;
        private String severity;
        private String message;
        private String entityType;
        private String entityId;
        private String actionUrl;
    }

    /**
     * Notifications widget
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationsWidget {
        private Long unreadCount;
        private List<NotificationItem> recent;
    }

    /**
     * Notification item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationItem {
        private Long id;
        private String type;
        private String subject;
        private String body;
        private String entityType;
        private String entityId;
        private String severity;
        private String createdAt;
        private Boolean isRead;
    }
}

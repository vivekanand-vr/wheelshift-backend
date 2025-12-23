package com.wheelshiftpro.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Sales dashboard response for sales role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDashboardResponse {
    private PersonalStats personalStats;
    private SalesPipeline pipeline;
    private PerformanceMetrics performance;
    private QuickActions quickActions;
    private InventorySummary availableInventory;
    private AdminDashboardResponse.NotificationsWidget notifications;

    /**
     * Personal statistics for sales employee
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalStats {
        private Long activeInquiries;
        private Long convertedInquiries;
        private Long activeReservations;
        private Long salesThisMonth;
        private BigDecimal commissionEarned;
        private Double conversionRate;
    }

    /**
     * Sales pipeline metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesPipeline {
        private Map<String, Long> inquiriesByStatus;
        private Long followUpToday;
        private Long followUpThisWeek;
    }

    /**
     * Performance metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private Long monthlySales;
        private Long monthlyTarget;
        private Double targetProgress;
        private List<MonthlySalesData> salesTrend;
        private BigDecimal avgSaleValue;
    }

    /**
     * Monthly sales data point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySalesData {
        private Integer month;
        private String monthName;
        private Long salesCount;
        private BigDecimal revenue;
    }

    /**
     * Quick actions for sales employee
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickActions {
        private Long pendingResponses;
        private Long followUpsDue;
        private Long expiringReservations;
        private List<ActionItem> items;
    }

    /**
     * Action item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionItem {
        private String type;
        private String priority;
        private String description;
        private String entityType;
        private String entityId;
        private String dueDate;
    }

    /**
     * Available inventory summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventorySummary {
        private Long totalAvailable;
        private Long newArrivals;
        private List<FeaturedCar> featured;
    }

    /**
     * Featured car
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeaturedCar {
        private Long carId;
        private String vinNumber;
        private String make;
        private String model;
        private Integer year;
        private BigDecimal sellingPrice;
        private String status;
    }
}

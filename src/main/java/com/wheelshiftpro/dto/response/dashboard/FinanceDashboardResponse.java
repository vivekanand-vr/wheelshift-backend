package com.wheelshiftpro.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Finance dashboard response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDashboardResponse {
    private FinancialOverview financialOverview;
    private TransactionSummary transactions;
    private Profitability profitability;
    private AgingAnalysis aging;
    private BudgetTracking budgetTracking;
    private AdminDashboardResponse.NotificationsWidget notifications;

    /**
     * Financial overview metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialOverview {
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
        private Double profitMargin;
        private BigDecimal cashFlow;
    }

    /**
     * Transaction summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionSummary {
        private Long pendingCount;
        private BigDecimal pendingAmount;
        private Long completedThisMonth;
        private List<RecentTransaction> recentTransactions;
    }

    /**
     * Recent transaction
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTransaction {
        private Long transactionId;
        private String transactionType;
        private BigDecimal amount;
        private String transactionDate;
        private String description;
    }

    /**
     * Profitability metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profitability {
        private BigDecimal avgProfitPerVehicle;
        private Double avgMargin;
        private String bestPerformingCategory;
        private List<VehicleProfitability> vehicleProfitability;
    }

    /**
     * Vehicle profitability
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleProfitability {
        private Long carId;
        private String vinNumber;
        private String make;
        private String model;
        private BigDecimal purchasePrice;
        private BigDecimal sellingPrice;
        private BigDecimal profit;
        private Double margin;
    }

    /**
     * Aging analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingAnalysis {
        private Long overduePayments;
        private BigDecimal overdueAmount;
        private Long pendingDeposits;
    }

    /**
     * Budget tracking
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetTracking {
        private BigDecimal totalBudget;
        private BigDecimal spent;
        private BigDecimal remaining;
        private Double utilizationRate;
    }
}

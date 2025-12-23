package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.response.dashboard.*;
import com.wheelshiftpro.dto.response.dashboard.AdminDashboardResponse.*;
import com.wheelshiftpro.dto.response.dashboard.SalesDashboardResponse.*;
import com.wheelshiftpro.dto.response.dashboard.InspectorDashboardResponse.*;
import com.wheelshiftpro.dto.response.dashboard.FinanceDashboardResponse.*;
import com.wheelshiftpro.dto.response.dashboard.StoreManagerDashboardResponse.*;
import com.wheelshiftpro.entity.*;
import com.wheelshiftpro.enums.*;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.repository.*;
import com.wheelshiftpro.service.DashboardService;
import com.wheelshiftpro.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of DashboardService for role-based dashboards
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    
    private final CarRepository carRepository;
    private final SaleRepository saleRepository;
    private final InquiryRepository inquiryRepository;
    private final ReservationRepository reservationRepository;
    private final EmployeeRepository employeeRepository;
    private final CarInspectionRepository carInspectionRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    @Override
    public AdminDashboardResponse getAdminDashboard(Long employeeId) {
        log.info("Fetching admin dashboard for employee: {}", employeeId);
        
        return AdminDashboardResponse.builder()
                .overview(buildOverviewStats())
                .revenue(buildRevenueMetrics())
                .inventory(buildInventoryHealth())
                .recentActivities(buildRecentActivities())
                .topEmployees(buildTopEmployees())
                .alerts(buildSystemAlerts())
                .notifications(buildNotificationsWidget(employeeId))
                .build();
    }
    
    @Override
    public SalesDashboardResponse getSalesDashboard(Long employeeId) {
        log.info("Fetching sales dashboard for employee: {}", employeeId);
        
        return SalesDashboardResponse.builder()
                .personalStats(buildSalesPersonalStats(employeeId))
                .pipeline(buildSalesPipeline(employeeId))
                .performance(buildSalesPerformance(employeeId))
                .quickActions(buildQuickActions(employeeId))
                .availableInventory(buildInventorySummary())
                .notifications(buildNotificationsWidget(employeeId))
                .build();
    }
    
    @Override
    public InspectorDashboardResponse getInspectorDashboard(Long employeeId) {
        log.info("Fetching inspector dashboard for employee: {}", employeeId);
        
        return InspectorDashboardResponse.builder()
                .inspectionQueue(buildInspectionQueue())
                .personalStats(buildInspectorPersonalStats(employeeId))
                .vehicleStatus(buildVehicleStatus())
                .assignedTasks(buildAssignedTasks(employeeId))
                .locationSummary(buildLocationSummary())
                .recentInspections(buildRecentInspections(employeeId))
                .notifications(buildNotificationsWidget(employeeId))
                .build();
    }
    
    @Override
    public FinanceDashboardResponse getFinanceDashboard(Long employeeId) {
        log.info("Fetching finance dashboard for employee: {}", employeeId);
        
        return FinanceDashboardResponse.builder()
                .financialOverview(buildFinancialOverview())
                .transactions(buildTransactionSummary())
                .profitability(buildProfitability())
                .aging(buildAgingAnalysis())
                .budgetTracking(buildBudgetTracking())
                .notifications(buildNotificationsWidget(employeeId))
                .build();
    }
    
    @Override
    public StoreManagerDashboardResponse getStoreManagerDashboard(Long employeeId) {
        log.info("Fetching store manager dashboard for employee: {}", employeeId);
        
        return StoreManagerDashboardResponse.builder()
                .locationOverview(buildLocationOverview())
                .vehicleDistribution(buildVehicleDistribution())
                .movements(buildMovementActivity())
                .capacityAlerts(buildCapacityAlerts())
                .maintenanceStatus(buildMaintenanceStatus())
                .performance(buildLocationPerformance())
                .notifications(buildNotificationsWidget(employeeId))
                .build();
    }
    
    @Override
    public Object getDashboardForCurrentUser(Long employeeId) {
        log.info("Fetching dashboard for current user: {}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        // Get primary role (first role)
        if (employee.getRoles().isEmpty()) {
            throw new ResourceNotFoundException("Employee has no assigned roles");
        }
        
        Role primaryRole = employee.getRoles().iterator().next();
        RoleType roleType = primaryRole.getName();
        
        log.info("Auto-detected role for employee {}: {}", employeeId, roleType);
        
        return switch (roleType) {
            case SUPER_ADMIN, ADMIN -> getAdminDashboard(employeeId);
            case SALES -> getSalesDashboard(employeeId);
            case INSPECTOR -> getInspectorDashboard(employeeId);
            case FINANCE -> getFinanceDashboard(employeeId);
            case STORE_MANAGER -> getStoreManagerDashboard(employeeId);
        };
    }
    
    // ===== ADMIN DASHBOARD BUILDERS =====
    
    private OverviewStats buildOverviewStats() {
        long totalCars = carRepository.count();
        long availableCars = carRepository.countByStatus(CarStatus.AVAILABLE);
        long reservedCars = carRepository.countByStatus(CarStatus.RESERVED);
        long soldCarsThisMonth = countSalesThisMonth();
        long activeInquiries = inquiryRepository.countByStatus(InquiryStatus.OPEN) +
                              inquiryRepository.countByStatus(InquiryStatus.IN_PROGRESS);
        long activeReservations = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        
        return OverviewStats.builder()
                .totalCars(totalCars)
                .availableCars(availableCars)
                .reservedCars(reservedCars)
                .soldCarsThisMonth(soldCarsThisMonth)
                .activeInquiries(activeInquiries)
                .activeReservations(activeReservations)
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .build();
    }
    
    private RevenueMetrics buildRevenueMetrics() {
        List<Sale> allSales = saleRepository.findAll();
        
        BigDecimal totalRevenue = allSales.stream()
                .map(Sale::getSalePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        LocalDate now = LocalDate.now();
        BigDecimal monthlyRevenue = allSales.stream()
                .filter(s -> s.getSaleDate().toLocalDate().getMonth() == now.getMonth() &&
                            s.getSaleDate().toLocalDate().getYear() == now.getYear())
                .map(Sale::getSalePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal ytdRevenue = allSales.stream()
                .filter(s -> s.getSaleDate().toLocalDate().getYear() == now.getYear())
                .map(Sale::getSalePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageSalePrice = allSales.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(allSales.size()), 2, RoundingMode.HALF_UP);
        
        List<MonthlyRevenue> revenueTrend = buildMonthlyRevenueTrend();
        
        return RevenueMetrics.builder()
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .ytdRevenue(ytdRevenue)
                .averageSalePrice(averageSalePrice)
                .revenueTrend(revenueTrend)
                .build();
    }
    
    private List<MonthlyRevenue> buildMonthlyRevenueTrend() {
        LocalDate now = LocalDate.now();
        List<Sale> ytdSales = saleRepository.findAll().stream()
                .filter(s -> s.getSaleDate().toLocalDate().getYear() == now.getYear())
                .collect(Collectors.toList());
        
        Map<Integer, List<Sale>> salesByMonth = ytdSales.stream()
                .collect(Collectors.groupingBy(s -> s.getSaleDate().toLocalDate().getMonthValue()));
        
        List<MonthlyRevenue> trend = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            List<Sale> monthSales = salesByMonth.getOrDefault(month, Collections.emptyList());
            BigDecimal revenue = monthSales.stream()
                    .map(Sale::getSalePrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            trend.add(MonthlyRevenue.builder()
                    .month(month)
                    .monthName(java.time.Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .revenue(revenue)
                    .salesCount((long) monthSales.size())
                    .build());
        }
        
        return trend;
    }
    
    private InventoryHealth buildInventoryHealth() {
        Map<String, Long> byStatus = new HashMap<>();
        for (CarStatus status : CarStatus.values()) {
            byStatus.put(status.name(), carRepository.countByStatus(status));
        }
        
        List<Car> availableCars = carRepository.findByStatus(CarStatus.AVAILABLE);
        BigDecimal totalValue = availableCars.stream()
                .map(Car::getSellingPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Double avgAge = calculateAverageInventoryAge();
        List<AgingInventoryItem> agingInventory = buildAgingInventory();
        
        return InventoryHealth.builder()
                .byStatus(byStatus)
                .totalValue(totalValue)
                .avgAge(avgAge)
                .agingInventory(agingInventory)
                .build();
    }
    
    private List<AgingInventoryItem> buildAgingInventory() {
        LocalDate cutoffDate = LocalDate.now().minusDays(60);
        Pageable pageable = PageRequest.of(0, 10);
        
        return carRepository.findByStatusAndPurchaseDateBefore(CarStatus.AVAILABLE, cutoffDate, pageable)
                .stream()
                .map(car -> AgingInventoryItem.builder()
                        .carId(car.getId())
                        .vinNumber(car.getVinNumber())
                        .make(car.getCarModel().getMake())
                        .model(car.getCarModel().getModel())
                        .year(car.getYear())
                        .daysInInventory(calculateDaysInInventory(car.getPurchaseDate()))
                        .purchasePrice(car.getPurchasePrice())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<ActivityLog> buildRecentActivities() {
        List<ActivityLog> activities = new ArrayList<>();
        
        // Recent sales
        List<Sale> recentSales = saleRepository.findAll(PageRequest.of(0, 5)).getContent();
        recentSales.forEach(sale -> activities.add(ActivityLog.builder()
                .id(sale.getId())
                .type("SALE")
                .description(String.format("Car %s sold for $%,.2f", 
                        sale.getCar().getVinNumber(), sale.getSalePrice()))
                .entityType("SALE")
                .entityId(sale.getId().toString())
                .timestamp(sale.getSaleDate().format(DATE_FORMATTER))
                .performedBy(sale.getEmployee().getName())
                .build()));
        
        // Sort by timestamp descending
        activities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        
        return activities.stream().limit(10).collect(Collectors.toList());
    }
    
    private List<EmployeePerformance> buildTopEmployees() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        
        return saleRepository.findAll().stream()
                .filter(s -> !s.getSaleDate().toLocalDate().isBefore(startOfMonth))
                .collect(Collectors.groupingBy(Sale::getEmployee))
                .entrySet().stream()
                .map(entry -> {
                    Employee employee = entry.getKey();
                    List<Sale> sales = entry.getValue();
                    
                    BigDecimal totalRevenue = sales.stream()
                            .map(Sale::getSalePrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal totalCommission = sales.stream()
                            .map(Sale::getTotalCommission)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return EmployeePerformance.builder()
                            .employeeId(employee.getId())
                            .employeeName(employee.getName())
                            .position(employee.getPosition())
                            .salesCount((long) sales.size())
                            .totalCommission(totalCommission)
                            .totalRevenue(totalRevenue)
                            .build();
                })
                .sorted((e1, e2) -> e2.getSalesCount().compareTo(e1.getSalesCount()))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    private SystemAlerts buildSystemAlerts() {
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        long expiringReservations = reservationRepository.findByStatusAndExpiryDateBefore(
                ReservationStatus.CONFIRMED, threeDaysFromNow).size();
        
        // Simplified inspection due calculation
        long inspectionsDue = carRepository.countByStatus(CarStatus.MAINTENANCE);
        
        // Check locations near capacity
        List<StorageLocation> locations = storageLocationRepository.findAll();
        long locationCapacityWarnings = locations.stream()
                .filter(loc -> {
                    double utilization = (double) loc.getCurrentVehicleCount() / loc.getTotalCapacity();
                    return utilization > 0.9;
                })
                .count();
        
        List<AlertDetail> details = new ArrayList<>();
        
        if (expiringReservations > 0) {
            details.add(AlertDetail.builder()
                    .type("EXPIRING_RESERVATIONS")
                    .severity("WARNING")
                    .message(expiringReservations + " reservation(s) expiring soon")
                    .entityType("RESERVATION")
                    .build());
        }
        
        return SystemAlerts.builder()
                .expiringReservations(expiringReservations)
                .inspectionsDue(inspectionsDue)
                .locationCapacityWarnings(locationCapacityWarnings)
                .details(details)
                .build();
    }
    
    // ===== SALES DASHBOARD BUILDERS =====
    
    private SalesDashboardResponse.PersonalStats buildSalesPersonalStats(Long employeeId) {
        long activeInquiries = inquiryRepository.countByAssignedEmployeeIdAndStatusIn(
                employeeId, List.of(InquiryStatus.OPEN, InquiryStatus.IN_PROGRESS));
        
        long convertedInquiries = inquiryRepository.countByAssignedEmployeeIdAndStatus(
                employeeId, InquiryStatus.RESPONDED);
        
        long activeReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .count();
        
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        List<Sale> monthlySales = saleRepository.findByEmployeeIdAndSaleDateAfter(
                employeeId, startOfMonth.atStartOfDay());
        
        BigDecimal commissionEarned = monthlySales.stream()
                .map(Sale::getTotalCommission)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double conversionRate = (convertedInquiries + activeInquiries) > 0 ?
                (convertedInquiries * 100.0) / (convertedInquiries + activeInquiries) : 0.0;
        
        return SalesDashboardResponse.PersonalStats.builder()
                .activeInquiries(activeInquiries)
                .convertedInquiries(convertedInquiries)
                .activeReservations(activeReservations)
                .salesThisMonth((long) monthlySales.size())
                .commissionEarned(commissionEarned)
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .build();
    }
    
    private SalesPipeline buildSalesPipeline(Long employeeId) {
        List<Inquiry> inquiries = inquiryRepository.findByAssignedEmployeeId(employeeId);
        
        Map<String, Long> inquiriesByStatus = inquiries.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getStatus().name(),
                        Collectors.counting()
                ));
        
        // Simplified follow-up calculation
        long followUpToday = inquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.IN_PROGRESS)
                .count();
        
        long followUpThisWeek = followUpToday * 2; // Simplified
        
        return SalesPipeline.builder()
                .inquiriesByStatus(inquiriesByStatus)
                .followUpToday(followUpToday)
                .followUpThisWeek(followUpThisWeek)
                .build();
    }
    
    private PerformanceMetrics buildSalesPerformance(Long employeeId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        List<Sale> monthlySales = saleRepository.findByEmployeeIdAndSaleDateAfter(
                employeeId, startOfMonth.atStartOfDay());
        
        BigDecimal avgSaleValue = monthlySales.isEmpty() ? BigDecimal.ZERO :
                monthlySales.stream()
                        .map(Sale::getSalePrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(monthlySales.size()), 2, RoundingMode.HALF_UP);
        
        return PerformanceMetrics.builder()
                .monthlySales((long) monthlySales.size())
                .monthlyTarget(6L) // Could be configurable
                .targetProgress(monthlySales.size() * 100.0 / 6.0)
                .salesTrend(buildMonthlySalesTrend(employeeId))
                .avgSaleValue(avgSaleValue)
                .build();
    }
    
    private List<MonthlySalesData> buildMonthlySalesTrend(Long employeeId) {
        LocalDate now = LocalDate.now();
        List<Sale> ytdSales = saleRepository.findAll().stream()
                .filter(s -> s.getEmployee().getId().equals(employeeId))
                .filter(s -> s.getSaleDate().toLocalDate().getYear() == now.getYear())
                .collect(Collectors.toList());
        
        Map<Integer, List<Sale>> salesByMonth = ytdSales.stream()
                .collect(Collectors.groupingBy(s -> s.getSaleDate().toLocalDate().getMonthValue()));
        
        List<MonthlySalesData> trend = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            List<Sale> monthSales = salesByMonth.getOrDefault(month, Collections.emptyList());
            BigDecimal revenue = monthSales.stream()
                    .map(Sale::getSalePrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            trend.add(MonthlySalesData.builder()
                    .month(month)
                    .monthName(java.time.Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .salesCount((long) monthSales.size())
                    .revenue(revenue)
                    .build());
        }
        
        return trend;
    }
    
    private QuickActions buildQuickActions(Long employeeId) {
        long pendingResponses = inquiryRepository.countByAssignedEmployeeIdAndStatus(
                employeeId, InquiryStatus.OPEN);
        
        long followUpsDue = inquiryRepository.countByAssignedEmployeeIdAndStatus(
                employeeId, InquiryStatus.IN_PROGRESS);
        
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        long expiringReservations = reservationRepository
                .findByStatusAndExpiryDateBefore(ReservationStatus.CONFIRMED, threeDaysFromNow)
                .size();
        
        return QuickActions.builder()
                .pendingResponses(pendingResponses)
                .followUpsDue(followUpsDue)
                .expiringReservations(expiringReservations)
                .items(new ArrayList<>())
                .build();
    }
    
    private InventorySummary buildInventorySummary() {
        List<Car> availableCars = carRepository.findByStatus(CarStatus.AVAILABLE);
        
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long newArrivals = availableCars.stream()
                .filter(car -> car.getPurchaseDate() != null && 
                              !car.getPurchaseDate().isBefore(thirtyDaysAgo))
                .count();
        
        List<FeaturedCar> featured = availableCars.stream()
                .limit(5)
                .map(car -> FeaturedCar.builder()
                        .carId(car.getId())
                        .vinNumber(car.getVinNumber())
                        .make(car.getCarModel().getMake())
                        .model(car.getCarModel().getModel())
                        .year(car.getYear())
                        .sellingPrice(car.getSellingPrice())
                        .status(car.getStatus().name())
                        .build())
                .collect(Collectors.toList());
        
        return InventorySummary.builder()
                .totalAvailable((long) availableCars.size())
                .newArrivals(newArrivals)
                .featured(featured)
                .build();
    }
    
    // ===== INSPECTOR DASHBOARD BUILDERS =====
    
    private InspectionQueue buildInspectionQueue() {
        long pendingInspections = carRepository.countByStatus(CarStatus.AVAILABLE);
        
        // Simplified - in real scenario, track scheduled inspections
        long scheduledToday = 4L;
        long scheduledThisWeek = 15L;
        long overdue = 2L;
        
        return InspectionQueue.builder()
                .pendingInspections(pendingInspections)
                .scheduledToday(scheduledToday)
                .scheduledThisWeek(scheduledThisWeek)
                .overdue(overdue)
                .build();
    }
    
    private InspectorDashboardResponse.PersonalStats buildInspectorPersonalStats(Long employeeId) {
        List<CarInspection> allInspections = carInspectionRepository.findAll();
        
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        long completedThisMonth = allInspections.stream()
                .filter(i -> !i.getInspectionDate().isBefore(startOfMonth))
                .count();
        
        long passCount = allInspections.stream()
                .filter(i -> i.getInspectionPass() != null && i.getInspectionPass())
                .count();
        
        double passRate = allInspections.isEmpty() ? 0.0 :
                (passCount * 100.0) / allInspections.size();
        
        BigDecimal avgRepairCost = allInspections.stream()
                .map(CarInspection::getEstimatedRepairCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(allInspections.size(), 1)), 2, RoundingMode.HALF_UP);
        
        return InspectorDashboardResponse.PersonalStats.builder()
                .completedThisMonth(completedThisMonth)
                .totalCompleted((long) allInspections.size())
                .passRate(Math.round(passRate * 100.0) / 100.0)
                .avgInspectionTime(45.0) // Simplified
                .avgRepairCost(avgRepairCost)
                .build();
    }
    
    private InspectorDashboardResponse.VehicleStatus buildVehicleStatus() {
        long needingInspection = carRepository.countByStatus(CarStatus.AVAILABLE);
        long failedInspections = carInspectionRepository.findAll().stream()
                .filter(i -> i.getInspectionPass() != null && !i.getInspectionPass())
                .count();
        long inMaintenance = carRepository.countByStatus(CarStatus.MAINTENANCE);
        
        return InspectorDashboardResponse.VehicleStatus.builder()
                .needingInspection(needingInspection)
                .failedInspections(failedInspections)
                .inMaintenance(inMaintenance)
                .build();
    }
    
    private AssignedTasks buildAssignedTasks(Long employeeId) {
        List<Task> tasks = taskRepository.findByAssigneeId(employeeId, PageRequest.of(0, 100)).getContent();
        
        long highPriority = tasks.stream()
                .filter(t -> t.getPriority() == TaskPriority.HIGH)
                .count();
        
        LocalDate today = LocalDate.now();
        long dueToday = tasks.stream()
                .filter(t -> t.getDueDate() != null && 
                            t.getDueDate().toLocalDate().equals(today))
                .count();
        
        return AssignedTasks.builder()
                .total((long) tasks.size())
                .highPriority(highPriority)
                .dueToday(dueToday)
                .build();
    }
    
    private List<InspectorDashboardResponse.LocationSummary> buildLocationSummary() {
        return storageLocationRepository.findAll().stream()
                .map(location -> InspectorDashboardResponse.LocationSummary.builder()
                        .locationId(location.getId())
                        .locationName(location.getName())
                        .vehicleCount((long) location.getCurrentVehicleCount())
                        .pendingInspections(2L) // Simplified
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<RecentInspection> buildRecentInspections(Long employeeId) {
        Pageable pageable = PageRequest.of(0, 10);
        return carInspectionRepository.findAll(pageable).stream()
                .map(inspection -> RecentInspection.builder()
                        .inspectionId(inspection.getId())
                        .carId(inspection.getCar().getId())
                        .vinNumber(inspection.getCar().getVinNumber())
                        .make(inspection.getCar().getCarModel().getMake())
                        .model(inspection.getCar().getCarModel().getModel())
                        .inspectionDate(inspection.getInspectionDate().toString())
                        .inspectionPass(inspection.getInspectionPass())
                        .estimatedRepairCost(inspection.getEstimatedRepairCost())
                        .build())
                .collect(Collectors.toList());
    }
    
    // ===== FINANCE DASHBOARD BUILDERS =====
    
    private FinancialOverview buildFinancialOverview() {
        List<FinancialTransaction> allTransactions = financialTransactionRepository.findAll();
        
        BigDecimal totalRevenue = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.SALE)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.PURCHASE ||
                            t.getTransactionType() == TransactionType.REPAIR)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        
        double profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
        
        return FinancialOverview.builder()
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .profitMargin(Math.round(profitMargin * 100.0) / 100.0)
                .cashFlow(netProfit)
                .build();
    }
    
    private TransactionSummary buildTransactionSummary() {
        // Simplified - in real scenario, have a status field
        long pendingCount = 0L;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        long completedThisMonth = financialTransactionRepository.findAll().stream()
                .filter(t -> !t.getTransactionDate().toLocalDate().isBefore(startOfMonth))
                .count();
        
        List<RecentTransaction> recentTransactions = financialTransactionRepository
                .findAll(PageRequest.of(0, 5))
                .stream()
                .map(transaction -> RecentTransaction.builder()
                        .transactionId(transaction.getId())
                        .transactionType(transaction.getTransactionType().name())
                        .amount(transaction.getAmount())
                        .transactionDate(transaction.getTransactionDate().format(DATE_FORMATTER))
                        .description(transaction.getDescription())
                        .build())
                .collect(Collectors.toList());
        
        return TransactionSummary.builder()
                .pendingCount(pendingCount)
                .pendingAmount(pendingAmount)
                .completedThisMonth(completedThisMonth)
                .recentTransactions(recentTransactions)
                .build();
    }
    
    private Profitability buildProfitability() {
        List<Sale> allSales = saleRepository.findAll();
        
        BigDecimal totalProfit = BigDecimal.ZERO;
        int count = 0;
        
        List<VehicleProfitability> vehicleProfitability = new ArrayList<>();
        
        for (Sale sale : allSales) {
            Car car = sale.getCar();
            BigDecimal purchasePrice = car.getPurchasePrice() != null ? 
                    car.getPurchasePrice() : BigDecimal.ZERO;
            BigDecimal sellingPrice = sale.getSalePrice();
            BigDecimal profit = sellingPrice.subtract(purchasePrice);
            
            double margin = purchasePrice.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                    profit.divide(sellingPrice, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
            
            totalProfit = totalProfit.add(profit);
            count++;
            
            if (vehicleProfitability.size() < 10) {
                vehicleProfitability.add(VehicleProfitability.builder()
                        .carId(car.getId())
                        .vinNumber(car.getVinNumber())
                        .make(car.getCarModel().getMake())
                        .model(car.getCarModel().getModel())
                        .purchasePrice(purchasePrice)
                        .sellingPrice(sellingPrice)
                        .profit(profit)
                        .margin(Math.round(margin * 100.0) / 100.0)
                        .build());
            }
        }
        
        BigDecimal avgProfitPerVehicle = count == 0 ? BigDecimal.ZERO :
                totalProfit.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        
        return Profitability.builder()
                .avgProfitPerVehicle(avgProfitPerVehicle)
                .avgMargin(31.5) // Simplified
                .bestPerformingCategory("SUV") // Simplified
                .vehicleProfitability(vehicleProfitability)
                .build();
    }
    
    private AgingAnalysis buildAgingAnalysis() {
        // Simplified - in real scenario, track payment due dates
        return AgingAnalysis.builder()
                .overduePayments(3L)
                .overdueAmount(BigDecimal.valueOf(85000))
                .pendingDeposits(5L)
                .build();
    }
    
    private BudgetTracking buildBudgetTracking() {
        BigDecimal totalBudget = BigDecimal.valueOf(2000000);
        
        BigDecimal spent = financialTransactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.PURCHASE ||
                            t.getTransactionType() == TransactionType.REPAIR)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal remaining = totalBudget.subtract(spent);
        
        double utilizationRate = totalBudget.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                spent.divide(totalBudget, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
        
        return BudgetTracking.builder()
                .totalBudget(totalBudget)
                .spent(spent)
                .remaining(remaining)
                .utilizationRate(Math.round(utilizationRate * 100.0) / 100.0)
                .build();
    }
    
    // ===== STORE MANAGER DASHBOARD BUILDERS =====
    
    private LocationOverview buildLocationOverview() {
        List<StorageLocation> locations = storageLocationRepository.findAll();
        
        long totalLocations = locations.size();
        long managedLocations = totalLocations; // Simplified - could filter by manager
        
        long totalCapacity = locations.stream()
                .mapToLong(StorageLocation::getTotalCapacity)
                .sum();
        
        long currentOccupancy = locations.stream()
                .mapToLong(StorageLocation::getCurrentVehicleCount)
                .sum();
        
        double utilizationRate = totalCapacity == 0 ? 0.0 :
                (currentOccupancy * 100.0) / totalCapacity;
        
        return LocationOverview.builder()
                .totalLocations(totalLocations)
                .managedLocations(managedLocations)
                .totalCapacity(totalCapacity)
                .currentOccupancy(currentOccupancy)
                .utilizationRate(Math.round(utilizationRate * 100.0) / 100.0)
                .build();
    }
    
    private VehicleDistribution buildVehicleDistribution() {
        List<StorageLocation> locations = storageLocationRepository.findAll();
        Map<String, Long> byLocation = locations.stream()
                .collect(Collectors.toMap(
                        StorageLocation::getName,
                        loc -> (long) loc.getCurrentVehicleCount()
                ));
        
        Map<String, Long> byStatus = new HashMap<>();
        for (CarStatus status : CarStatus.values()) {
            byStatus.put(status.name(), carRepository.countByStatus(status));
        }
        
        return VehicleDistribution.builder()
                .byLocation(byLocation)
                .byStatus(byStatus)
                .build();
    }
    
    private MovementActivity buildMovementActivity() {
        // Simplified - would use actual car_movements table
        return MovementActivity.builder()
                .todayMovements(4L)
                .thisWeekMovements(18L)
                .pendingTransfers(3L)
                .recentMovements(new ArrayList<>())
                .build();
    }
    
    private CapacityAlerts buildCapacityAlerts() {
        List<StorageLocation> locations = storageLocationRepository.findAll();
        
        long nearFullLocations = locations.stream()
                .filter(loc -> {
                    double utilization = (double) loc.getCurrentVehicleCount() / loc.getTotalCapacity();
                    return utilization > 0.85;
                })
                .count();
        
        long underutilizedLocations = locations.stream()
                .filter(loc -> {
                    double utilization = (double) loc.getCurrentVehicleCount() / loc.getTotalCapacity();
                    return utilization < 0.3;
                })
                .count();
        
        List<CapacityRecommendation> recommendations = locations.stream()
                .filter(loc -> {
                    double utilization = (double) loc.getCurrentVehicleCount() / loc.getTotalCapacity();
                    return utilization > 0.85 || utilization < 0.3;
                })
                .map(loc -> {
                    double utilization = (double) loc.getCurrentVehicleCount() / loc.getTotalCapacity();
                    String type = utilization > 0.85 ? "NEAR_FULL" : "UNDERUTILIZED";
                    String message = utilization > 0.85 ?
                            "Location is near capacity" :
                            "Location has excess capacity";
                    
                    return CapacityRecommendation.builder()
                            .type(type)
                            .locationName(loc.getName())
                            .message(message)
                            .currentOccupancy((long) loc.getCurrentVehicleCount())
                            .capacity((long) loc.getTotalCapacity())
                            .build();
                })
                .collect(Collectors.toList());
        
        return CapacityAlerts.builder()
                .nearFullLocations(nearFullLocations)
                .underutilizedLocations(underutilizedLocations)
                .recommendations(recommendations)
                .build();
    }
    
    private MaintenanceStatus buildMaintenanceStatus() {
        long vehiclesInMaintenance = carRepository.countByStatus(CarStatus.MAINTENANCE);
        
        // Simplified location breakdown
        Map<String, Long> byLocation = new HashMap<>();
        byLocation.put("Main Location", vehiclesInMaintenance);
        
        return MaintenanceStatus.builder()
                .vehiclesInMaintenance(vehiclesInMaintenance)
                .byLocation(byLocation)
                .avgMaintenanceTime(5.5)
                .build();
    }
    
    private LocationPerformance buildLocationPerformance() {
        // Simplified performance metrics
        return LocationPerformance.builder()
                .avgTurnoverDays(42.0)
                .fastestMovingCategory("Sedan")
                .slowestMovingCategory("Luxury")
                .build();
    }
    
    // ===== COMMON BUILDERS =====
    
    private NotificationsWidget buildNotificationsWidget(Long employeeId) {
        try {
            Pageable pageable = PageRequest.of(0, 5);
            long unreadCount = notificationService.getUnreadCount(RecipientType.EMPLOYEE, employeeId, null);
            
            List<NotificationItem> recent = notificationService
                    .getNotificationsForRecipient(RecipientType.EMPLOYEE, employeeId, null, pageable)
                    .getContent()
                    .stream()
                    .map(jobResponse -> NotificationItem.builder()
                            .id(jobResponse.getId())
                            .type(jobResponse.getEventType() != null ? jobResponse.getEventType() : "NOTIFICATION")
                            .subject(jobResponse.getTitle() != null ? jobResponse.getTitle() : "Notification")
                            .body(jobResponse.getMessage() != null ? jobResponse.getMessage() : "")
                            .entityType(jobResponse.getEntityType() != null ? jobResponse.getEntityType() : "")
                            .entityId(jobResponse.getEntityId() != null ? jobResponse.getEntityId().toString() : null)
                            .severity("INFO")
                            .createdAt(jobResponse.getCreatedAt() != null ? 
                                    jobResponse.getCreatedAt().format(DATE_FORMATTER) : "")
                            .isRead(jobResponse.getSentAt() != null)
                            .build())
                    .collect(Collectors.toList());
            
            return NotificationsWidget.builder()
                    .unreadCount(unreadCount)
                    .recent(recent)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to fetch notifications for employee {}: {}", employeeId, e.getMessage());
            return NotificationsWidget.builder()
                    .unreadCount(0L)
                    .recent(new ArrayList<>())
                    .build();
        }
    }
    
    // ===== HELPER METHODS =====
    
    private long countSalesThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return saleRepository.findAll().stream()
                .filter(s -> !s.getSaleDate().toLocalDate().isBefore(startOfMonth))
                .count();
    }
    
    private Double calculateAverageInventoryAge() {
        List<Car> availableCars = carRepository.findByStatus(CarStatus.AVAILABLE);
        if (availableCars.isEmpty()) return 0.0;
        
        double totalDays = availableCars.stream()
                .filter(car -> car.getPurchaseDate() != null)
                .mapToInt(car -> calculateDaysInInventory(car.getPurchaseDate()))
                .average()
                .orElse(0.0);
        
        return Math.round(totalDays * 100.0) / 100.0;
    }
    
    private int calculateDaysInInventory(LocalDate purchaseDate) {
        if (purchaseDate == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(purchaseDate, LocalDate.now());
    }
}

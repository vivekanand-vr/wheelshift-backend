# Dashboard Implementation Guide

## Quick Reference

### API Endpoints

```bash
# Role-specific dashboards
GET /api/v1/dashboard/admin
GET /api/v1/dashboard/sales
GET /api/v1/dashboard/inspector
GET /api/v1/dashboard/finance
GET /api/v1/dashboard/store-manager

# Current user's dashboard (auto-detects role)
GET /api/v1/dashboard/me
```

### Usage Example

```java
@Autowired
private DashboardService dashboardService;

// Get admin dashboard
AdminDashboardResponse dashboard = dashboardService.getAdminDashboard(employeeId);

// Get sales dashboard with data scopes applied
SalesDashboardResponse salesDash = dashboardService.getSalesDashboard(employeeId);
```

---

## Implementation Details

### Service Layer

**DashboardService.java**
```java
public interface DashboardService {
    AdminDashboardResponse getAdminDashboard(Long employeeId);
    SalesDashboardResponse getSalesDashboard(Long employeeId);
    InspectorDashboardResponse getInspectorDashboard(Long employeeId);
    FinanceDashboardResponse getFinanceDashboard(Long employeeId);
    StoreManagerDashboardResponse getStoreManagerDashboard(Long employeeId);
    Object getDashboardForCurrentUser(Long employeeId);
}
```

**DashboardServiceImpl.java**
```java
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
    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;
    
    @Override
    public AdminDashboardResponse getAdminDashboard(Long employeeId) {
        log.info("Fetching admin dashboard for employee: {}", employeeId);
        
        // Apply data scopes if applicable
        Specification<Car> carSpec = authorizationService.applyDataScopes(
            employeeId, ResourceType.CAR
        );
        
        // Build dashboard response with all widgets
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
    
    private OverviewStats buildOverviewStats() {
        // Implementation
    }
    
    private RevenueMetrics buildRevenueMetrics() {
        // Implementation
    }
    
    // ... other helper methods
}
```

---

## Controller Implementation

```java
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Role-based dashboard endpoints")
public class DashboardController {
    
    private final DashboardService dashboardService;
    private final EmployeeRepository employeeRepository;
    
    @GetMapping("/admin")
    @PreAuthorize("@authService.hasAnyRole(authentication.name, 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get admin dashboard", 
               description = "Retrieves comprehensive dashboard data for administrators")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard(
            Authentication authentication) {
        
        Long employeeId = getCurrentEmployeeId(authentication);
        AdminDashboardResponse response = dashboardService.getAdminDashboard(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/sales")
    @PreAuthorize("@authService.hasAnyRole(authentication.name, 'SALES', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get sales dashboard",
               description = "Retrieves sales-specific dashboard with pipeline and performance metrics")
    public ResponseEntity<ApiResponse<SalesDashboardResponse>> getSalesDashboard(
            Authentication authentication) {
        
        Long employeeId = getCurrentEmployeeId(authentication);
        SalesDashboardResponse response = dashboardService.getSalesDashboard(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's dashboard",
               description = "Auto-detects user role and returns appropriate dashboard")
    public ResponseEntity<ApiResponse<Object>> getCurrentUserDashboard(
            Authentication authentication) {
        
        Long employeeId = getCurrentEmployeeId(authentication);
        Object response = dashboardService.getDashboardForCurrentUser(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    private Long getCurrentEmployeeId(Authentication authentication) {
        String email = authentication.getName();
        return employeeRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"))
            .getId();
    }
}
```

---

## DTO Structures

### AdminDashboardResponse

```java
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
}

@Data
@Builder
public class OverviewStats {
    private Long totalCars;
    private Long availableCars;
    private Long reservedCars;
    private Long soldCarsThisMonth;
    private Long activeInquiries;
    private Long activeReservations;
    private Long totalEmployees;
    private Long activeEmployees;
}

@Data
@Builder
public class RevenueMetrics {
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal ytdRevenue;
    private BigDecimal averageSalePrice;
    private List<MonthlyRevenue> revenueTrend;
}

@Data
@Builder
public class InventoryHealth {
    private Map<String, Long> byStatus;
    private BigDecimal totalValue;
    private Double avgAge;
    private List<AgingInventoryItem> agingInventory;
}

@Data
@Builder
public class SystemAlerts {
    private Long expiringReservations;
    private Long inspectionsDue;
    private Long locationCapacityWarnings;
    private List<AlertDetail> details;
}
```

### SalesDashboardResponse

```java
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
    private NotificationsWidget notifications;
}

@Data
@Builder
public class PersonalStats {
    private Long activeInquiries;
    private Long convertedInquiries;
    private Long activeReservations;
    private Long salesThisMonth;
    private BigDecimal commissionEarned;
    private Double conversionRate;
}

@Data
@Builder
public class SalesPipeline {
    private Map<String, Long> inquiriesByStatus;
    private Long followUpToday;
    private Long followUpThisWeek;
}

@Data
@Builder
public class QuickActions {
    private Long pendingResponses;
    private Long followUpsDue;
    private Long expiringReservations;
    private List<ActionItem> items;
}
```

---

## Repository Queries

### Custom Repository Methods

```java
public interface CarRepository extends JpaRepository<Car, Long>, 
                                       JpaSpecificationExecutor<Car> {
    
    // Dashboard-specific queries
    
    @Query("SELECT COUNT(c) FROM Car c WHERE c.status = :status")
    Long countByStatus(@Param("status") CarStatus status);
    
    @Query("SELECT c.status, COUNT(c) FROM Car c GROUP BY c.status")
    List<Object[]> countGroupedByStatus();
    
    @Query("SELECT SUM(c.sellingPrice) FROM Car c WHERE c.status = 'AVAILABLE'")
    BigDecimal calculateTotalInventoryValue();
    
    @Query("SELECT AVG(DATEDIFF(CURRENT_DATE, c.purchaseDate)) FROM Car c " +
           "WHERE c.status = 'AVAILABLE'")
    Double calculateAverageInventoryAge();
    
    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE' " +
           "AND DATEDIFF(CURRENT_DATE, c.purchaseDate) > :days " +
           "ORDER BY c.purchaseDate ASC")
    List<Car> findAgingInventory(@Param("days") int days, Pageable pageable);
}

public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    @Query("SELECT COUNT(s) FROM Sale s " +
           "WHERE MONTH(s.saleDate) = MONTH(CURRENT_DATE) " +
           "AND YEAR(s.saleDate) = YEAR(CURRENT_DATE)")
    Long countSalesThisMonth();
    
    @Query("SELECT SUM(s.salePrice) FROM Sale s " +
           "WHERE s.employee.id = :employeeId " +
           "AND MONTH(s.saleDate) = MONTH(CURRENT_DATE) " +
           "AND YEAR(s.saleDate) = YEAR(CURRENT_DATE)")
    BigDecimal sumSalesPriceThisMonthByEmployee(@Param("employeeId") Long employeeId);
    
    @Query("SELECT MONTH(s.saleDate), SUM(s.salePrice) FROM Sale s " +
           "WHERE YEAR(s.saleDate) = YEAR(CURRENT_DATE) " +
           "GROUP BY MONTH(s.saleDate) ORDER BY MONTH(s.saleDate)")
    List<Object[]> getMonthlyRevenueTrend();
    
    @Query("SELECT e, COUNT(s), SUM(s.totalCommission) FROM Sale s " +
           "JOIN s.employee e " +
           "WHERE MONTH(s.saleDate) = MONTH(CURRENT_DATE) " +
           "AND YEAR(s.saleDate) = YEAR(CURRENT_DATE) " +
           "GROUP BY e ORDER BY COUNT(s) DESC")
    List<Object[]> findTopPerformersThisMonth(Pageable pageable);
}

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    
    @Query("SELECT i.status, COUNT(i) FROM Inquiry i " +
           "WHERE i.assignedEmployee.id = :employeeId " +
           "GROUP BY i.status")
    List<Object[]> countByStatusForEmployee(@Param("employeeId") Long employeeId);
    
    @Query("SELECT COUNT(i) FROM Inquiry i " +
           "WHERE i.assignedEmployee.id = :employeeId " +
           "AND i.status IN ('OPEN', 'IN_PROGRESS')")
    Long countActiveInquiriesForEmployee(@Param("employeeId") Long employeeId);
}
```

---

## Caching Implementation

### Redis Cache Configuration

```java
@Configuration
@EnableCaching
public class DashboardCacheConfig {
    
    @Bean
    public RedisCacheManager dashboardCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Different TTLs for different data
        cacheConfigurations.put("dashboard:overview", 
            config.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboard:revenue", 
            config.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("dashboard:personal", 
            config.entryTtl(Duration.ofMinutes(2)));
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
```

### Service-Level Caching

```java
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    @Cacheable(value = "dashboard:admin", key = "#employeeId")
    public AdminDashboardResponse getAdminDashboard(Long employeeId) {
        // Implementation
    }
    
    @Cacheable(value = "dashboard:sales", key = "#employeeId")
    public SalesDashboardResponse getSalesDashboard(Long employeeId) {
        // Implementation
    }
    
    // Cache eviction on data changes
    @CacheEvict(value = {"dashboard:admin", "dashboard:sales"}, allEntries = true)
    public void invalidateDashboardCache() {
        log.info("Dashboard cache invalidated");
    }
}
```

---

## Testing

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    
    @Mock
    private CarRepository carRepository;
    
    @Mock
    private SaleRepository saleRepository;
    
    @InjectMocks
    private DashboardServiceImpl dashboardService;
    
    @Test
    void testGetAdminDashboard() {
        // Setup
        when(carRepository.countByStatus(CarStatus.AVAILABLE))
            .thenReturn(85L);
        when(saleRepository.countSalesThisMonth())
            .thenReturn(12L);
        
        // Execute
        AdminDashboardResponse response = dashboardService.getAdminDashboard(1L);
        
        // Assert
        assertNotNull(response);
        assertEquals(85L, response.getOverview().getAvailableCars());
        assertEquals(12L, response.getOverview().getSoldCarsThisMonth());
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.overview").exists())
            .andExpect(jsonPath("$.data.revenue").exists());
    }
}
```

---

## Performance Optimization

### Database Indexing

```sql
-- Indexes for dashboard queries
CREATE INDEX idx_car_status ON cars(status);
CREATE INDEX idx_car_purchase_date ON cars(purchase_date);
CREATE INDEX idx_sale_date ON sales(sale_date);
CREATE INDEX idx_sale_employee ON sales(employee_id, sale_date);
CREATE INDEX idx_inquiry_employee_status ON inquiries(assigned_employee_id, status);
CREATE INDEX idx_reservation_status ON reservations(status, expiry_date);
```

### Query Optimization

```java
// Use projections for lightweight queries
public interface CarCountProjection {
    CarStatus getStatus();
    Long getCount();
}

@Query("SELECT c.status as status, COUNT(c) as count FROM Car c GROUP BY c.status")
List<CarCountProjection> getCarCountsByStatus();

// Batch fetching
@EntityGraph(attributePaths = {"employee", "roles"})
List<Employee> findTopPerformers();
```

---

## Monitoring

### Metrics

```java
@Service
@RequiredArgsConstructor
public class DashboardMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    public void recordDashboardLoad(String role, long durationMs) {
        Timer.builder("dashboard.load")
            .tag("role", role)
            .register(meterRegistry)
            .record(Duration.ofMillis(durationMs));
    }
    
    public void recordWidgetError(String widget) {
        Counter.builder("dashboard.widget.error")
            .tag("widget", widget)
            .register(meterRegistry)
            .increment();
    }
}
```

---

## Troubleshooting

### Common Issues

1. **Slow Dashboard Load**
   - Check database query performance
   - Verify cache hit rates
   - Review data scope complexity

2. **Stale Data**
   - Verify cache invalidation on updates
   - Check cache TTL settings
   - Review transaction boundaries

3. **Permission Errors**
   - Verify role assignments
   - Check data scope configuration
   - Review ACL settings

### Debug Logging

```java
log.debug("Dashboard load started: role={}, employeeId={}", role, employeeId);
log.debug("Overview stats calculated: cars={}, sales={}", carCount, saleCount);
log.debug("Dashboard load completed: duration={}ms", duration);
```

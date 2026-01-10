# Cache Invalidation Quick Reference

**Purpose:** Quick guide for when and how to invalidate caches in WheelShift Pro

---

## When to Invalidate Caches

### Rule of Thumb
**Invalidate caches whenever you CREATE, UPDATE, or DELETE data that is cached.**

---

## Quick Decision Tree

```
Did you modify data?
    ├─ YES → Invalidate related caches
    └─ NO  → No invalidation needed

Is the change critical (financial, sales)?
    ├─ YES → Invalidate immediately + dashboards
    └─ NO  → Can use async invalidation
```

---

## Common Scenarios

### 1. Car Operations

```java
@Service
@RequiredArgsConstructor
public class CarServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // CREATE CAR
    public CarDTO createCar(CarCreateDTO dto) {
        Car saved = carRepository.save(car);
        
        // Invalidate:
        cacheInvalidation.invalidateCarCaches();
        // Clears: cars, carDetails, carStatistics, all dashboards
        
        return carMapper.toDTO(saved);
    }
    
    // UPDATE CAR
    @CacheEvict(value = {"carDetails", "cars", "carStatistics"}, key = "#carId")
    public CarDTO updateCar(Long carId, CarUpdateDTO dto) {
        // Update logic
        cacheInvalidation.invalidateDashboards();
        return carMapper.toDTO(updated);
    }
    
    // DELETE CAR
    @CacheEvict(value = {"carDetails", "cars", "carStatistics"}, allEntries = true)
    public void deleteCar(Long carId) {
        carRepository.deleteById(carId);
        cacheInvalidation.invalidateDashboards();
    }
    
    // CHANGE CAR STATUS
    public CarDTO updateCarStatus(Long carId, CarStatus status) {
        Car car = carRepository.findById(carId).orElseThrow();
        car.setCurrentStatus(status);
        carRepository.save(car);
        
        // Invalidate specific car + dashboards
        cacheInvalidation.invalidateCarCache(carId);
        return carMapper.toDTO(car);
    }
}
```

### 2. Sale Operations

```java
@Service
@RequiredArgsConstructor
public class SaleServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // CREATE SALE
    @Caching(evict = {
        @CacheEvict(value = "sales", allEntries = true),
        @CacheEvict(value = "revenueMetrics", allEntries = true)
    })
    public SaleDTO createSale(SaleCreateDTO dto) {
        Sale saved = saleRepository.save(sale);
        
        // Also invalidate:
        cacheInvalidation.invalidateCarCache(dto.getCarId());
        cacheInvalidation.invalidateDashboards();
        
        return saleMapper.toDTO(saved);
    }
    
    // UPDATE SALE
    public SaleDTO updateSale(Long saleId, SaleUpdateDTO dto) {
        // Update logic
        cacheInvalidation.invalidateSaleCaches();
        return saleMapper.toDTO(updated);
    }
}
```

### 3. Client Operations

```java
@Service
@RequiredArgsConstructor
public class ClientServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // CREATE/UPDATE CLIENT
    @CacheEvict(value = "clients", allEntries = true)
    public ClientDTO saveClient(ClientDTO dto) {
        Client saved = clientRepository.save(client);
        return clientMapper.toDTO(saved);
    }
    
    // When client makes purchase
    public void recordPurchase(Long clientId) {
        // Update purchase count
        cacheInvalidation.invalidateClientCaches();
        cacheInvalidation.invalidateDashboards();
    }
}
```

### 4. Inquiry Operations

```java
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // CREATE INQUIRY
    @CacheEvict(value = "inquiries", allEntries = true)
    public InquiryDTO createInquiry(InquiryCreateDTO dto) {
        Inquiry saved = inquiryRepository.save(inquiry);
        cacheInvalidation.invalidateDashboards(); // For sales dashboard
        return inquiryMapper.toDTO(saved);
    }
    
    // ASSIGN INQUIRY TO EMPLOYEE
    public InquiryDTO assignInquiry(Long inquiryId, Long employeeId) {
        // Assign logic
        cacheInvalidation.invalidateInquiryCaches();
        cacheInvalidation.invalidateEmployeeDashboards(employeeId);
        return inquiryMapper.toDTO(updated);
    }
    
    // UPDATE INQUIRY STATUS
    @CacheEvict(value = "inquiries", key = "#inquiryId")
    public InquiryDTO updateStatus(Long inquiryId, InquiryStatus status) {
        // Update logic
        cacheInvalidation.invalidateDashboards();
        return inquiryMapper.toDTO(updated);
    }
}
```

### 5. Reservation Operations

```java
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // CREATE RESERVATION
    public ReservationDTO createReservation(ReservationCreateDTO dto) {
        Reservation saved = reservationRepository.save(reservation);
        
        // Invalidate car (status changed), reservations, dashboards
        cacheInvalidation.invalidateCarCache(dto.getCarId());
        cacheInvalidation.invalidateReservationCaches();
        
        return reservationMapper.toDTO(saved);
    }
    
    // CANCEL/EXPIRE RESERVATION
    public void cancelReservation(Long reservationId) {
        // Cancel logic
        cacheInvalidation.invalidateReservationCaches();
        cacheInvalidation.invalidateDashboards();
    }
}
```

### 6. Financial Transaction Operations

```java
@Service
@RequiredArgsConstructor
public class FinancialTransactionServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // CREATE TRANSACTION
    @Caching(evict = {
        @CacheEvict(value = "financialTransactions", allEntries = true),
        @CacheEvict(value = "revenueMetrics", allEntries = true)
    })
    public FinancialTransactionDTO createTransaction(FinancialTransactionCreateDTO dto) {
        FinancialTransaction saved = transactionRepository.save(transaction);
        
        // Finance dashboard relies heavily on this
        cacheInvalidation.invalidateDashboards();
        
        return transactionMapper.toDTO(saved);
    }
}
```

### 7. Employee Operations

```java
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // UPDATE EMPLOYEE ROLES
    public EmployeeDTO updateRoles(Long employeeId, List<Long> roleIds) {
        // Update roles
        cacheInvalidation.invalidateEmployeeCaches();
        cacheInvalidation.invalidateEmployeeDashboards(employeeId);
        return employeeMapper.toDTO(updated);
    }
}
```

### 8. Storage Location Operations

```java
@Service
@RequiredArgsConstructor
public class StorageLocationServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // UPDATE LOCATION CAPACITY
    @CacheEvict(value = {"storageLocations", "locationCapacity"}, key = "#locationId")
    public StorageLocationDTO updateCapacity(Long locationId, Integer newCapacity) {
        // Update logic
        cacheInvalidation.invalidateDashboards();
        return storageLocationMapper.toDTO(updated);
    }
    
    // MOVE CAR TO LOCATION
    public void moveCarToLocation(Long carId, Long locationId) {
        // Move logic
        cacheInvalidation.invalidateLocationCaches();
        cacheInvalidation.invalidateCarCache(carId);
        cacheInvalidation.invalidateDashboards();
    }
}
```

### 9. Task Operations

```java
@Service
@RequiredArgsConstructor
public class TaskServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // CREATE/UPDATE TASK
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskDTO saveTask(TaskDTO dto) {
        Task saved = taskRepository.save(task);
        cacheInvalidation.invalidateDashboards();
        return taskMapper.toDTO(saved);
    }
    
    // COMPLETE TASK
    public TaskDTO completeTask(Long taskId) {
        // Complete logic
        cacheInvalidation.invalidateTaskCaches();
        return taskMapper.toDTO(completed);
    }
}
```

### 10. Inspection Operations

```java
@Service
@RequiredArgsConstructor
public class CarInspectionServiceImpl {
    private final CacheInvalidationService cacheInvalidation;
    
    // CREATE INSPECTION
    @CacheEvict(value = "inspections", allEntries = true)
    public CarInspectionDTO createInspection(CarInspectionCreateDTO dto) {
        CarInspection saved = inspectionRepository.save(inspection);
        
        // Inspector dashboard needs this
        cacheInvalidation.invalidateDashboards();
        
        return inspectionMapper.toDTO(saved);
    }
}
```

---

## Annotation Quick Reference

### @CacheEvict Examples

```java
// Single cache, single key
@CacheEvict(value = "carDetails", key = "#carId")

// Single cache, all entries
@CacheEvict(value = "cars", allEntries = true)

// Multiple caches
@CacheEvict(value = {"cars", "carDetails", "carStatistics"}, key = "#carId")

// Multiple caches, all entries
@CacheEvict(value = {"cars", "carStatistics"}, allEntries = true)

// Evict before method execution
@CacheEvict(value = "sales", key = "#saleId", beforeInvocation = true)
```

### @Caching Examples

```java
// Combine multiple operations
@Caching(
    evict = {
        @CacheEvict(value = "carDetails", key = "#carId"),
        @CacheEvict(value = "cars", allEntries = true)
    }
)

// Evict multiple caches + update one
@Caching(
    evict = {
        @CacheEvict(value = "cars", allEntries = true)
    },
    put = {
        @CachePut(value = "carDetails", key = "#result.id")
    }
)
```

---

## CacheInvalidationService API

### Individual Resource Invalidation

```java
cacheInvalidation.invalidateCarCache(carId);          // Single car
cacheInvalidation.invalidateCarCaches();              // All cars
cacheInvalidation.invalidateSaleCaches();             // All sales
cacheInvalidation.invalidateClientCaches();           // All clients
cacheInvalidation.invalidateEmployeeCaches();         // All employees
cacheInvalidation.invalidateInquiryCaches();          // All inquiries
cacheInvalidation.invalidateReservationCaches();      // All reservations
cacheInvalidation.invalidateFinancialCaches();        // All financial data
cacheInvalidation.invalidateLocationCaches();         // All locations
cacheInvalidation.invalidateInspectionCaches();       // All inspections
cacheInvalidation.invalidateTaskCaches();             // All tasks
cacheInvalidation.invalidateNotificationCaches();     // All notifications
```

### Dashboard Invalidation

```java
cacheInvalidation.invalidateDashboards();                    // All dashboards
cacheInvalidation.invalidateEmployeeDashboards(employeeId);  // Specific employee
```

### Global Operations

```java
cacheInvalidation.clearCache("cacheName");      // Clear specific cache
cacheInvalidation.clearAllCaches();             // Nuclear option
cacheInvalidation.asyncClearAllCaches();        // Async nuclear option
```

---

## Best Practices Checklist

- [ ] Always invalidate after data modification
- [ ] Invalidate all related caches, not just one
- [ ] Invalidate dashboards if data affects them
- [ ] Use `@CacheEvict` for automatic invalidation
- [ ] Use `CacheInvalidationService` for complex scenarios
- [ ] Consider async invalidation for non-critical operations
- [ ] Test cache invalidation in staging first
- [ ] Monitor cache hit ratios after changes
- [ ] Document custom invalidation logic

---

## Testing Cache Invalidation

### Manual Testing

```bash
# Before operation - check cache exists
redis-cli GET "carDetails::123"

# Perform operation (update car 123)
curl -X PUT http://localhost:8080/api/v1/cars/123 ...

# After operation - cache should be gone
redis-cli GET "carDetails::123"
# Should return (nil)
```

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureTestDatabase
class CacheInvalidationTest {
    
    @Autowired
    private CarService carService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Test
    void shouldInvalidateCacheOnUpdate() {
        // Given: Car is cached
        CarDTO car = carService.getCarById(1L);
        Cache cache = cacheManager.getCache("carDetails");
        assertThat(cache.get(1L)).isNotNull();
        
        // When: Car is updated
        carService.updateCar(1L, updateDTO);
        
        // Then: Cache is invalidated
        assertThat(cache.get(1L)).isNull();
    }
}
```

---

## Troubleshooting

### Problem: Cache not invalidating

**Check:**
1. Is `@EnableCaching` present in config?
2. Is method `public`?
3. Is method being called from outside the class (not self-invocation)?
4. Is cache name correct in annotation?
5. Is cache key correct?

### Problem: Stale data after update

**Solution:**
```java
// Add explicit invalidation
@CacheEvict(value = "cars", allEntries = true)
public CarDTO updateCar(...) {
    // Also manually invalidate related caches
    cacheInvalidation.invalidateDashboards();
}
```

### Problem: Too many cache invalidations

**Solution:**
```java
// Batch operations instead of individual invalidations
public void bulkUpdateCars(List<CarUpdateDTO> updates) {
    updates.forEach(update -> {
        // Update database
        carRepository.save(car);
    });
    
    // Single invalidation at the end
    cacheInvalidation.invalidateCarCaches();
}
```

---

## When NOT to Invalidate

1. **Read-only operations** - No invalidation needed
2. **Cache misses** - If cache doesn't exist, don't try to invalidate
3. **Failed operations** - If transaction rolls back, cache stays valid
4. **Background jobs** - May need delayed/async invalidation

---

**Related Documentation:**
- [Redis Caching Guide](REDIS_CACHING_GUIDE.md)
- [Dashboard Documentation](features/dashboard/README.md)

---

**Quick Help:**

```java
// Generic invalidation pattern
@CacheEvict(value = {"cache1", "cache2"}, allEntries = true)
public void modifyData() {
    // Your update logic
    cacheInvalidationService.invalidateDashboards();
}
```

---

**Version:** 1.0  
**Last Updated:** January 2026

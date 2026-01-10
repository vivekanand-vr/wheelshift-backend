# Redis Caching Implementation Guide

**Version:** 1.0  
**Last Updated:** January 2026  
**Author:** WheelShift Pro Development Team

---

## Table of Contents

1. [Overview](#overview)
2. [Configuration](#configuration)
3. [Cache Regions & TTL](#cache-regions--ttl)
4. [Using @Cacheable](#using-cacheable)
5. [Cache Invalidation](#cache-invalidation)
6. [Manual Cache Operations](#manual-cache-operations)
7. [Best Practices](#best-practices)
8. [Monitoring & Debugging](#monitoring--debugging)
9. [Common Patterns](#common-patterns)
10. [Troubleshooting](#troubleshooting)

---

## Overview

### What is Redis Caching?

Redis caching in WheelShift Pro provides:
- **Fast data retrieval** - Sub-millisecond response times
- **Reduced database load** - Fewer queries to MySQL
- **Improved user experience** - Faster dashboard and API responses
- **Scalability** - Better performance under load

### Architecture

```
Client Request
     ↓
Controller
     ↓
Service Layer
     ↓
Cache Check (Redis) ←──┐
     ↓                  │
Cache Hit? ───YES──────┘
     ↓ NO
Database Query
     ↓
Store in Cache
     ↓
Return Response
```

---

## Configuration

### Application Properties

Add to `application.properties`:

```properties
# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.database=0
spring.redis.timeout=2000ms

# Connection Pool
spring.redis.jedis.pool.max-active=8
spring.redis.jedis.pool.max-idle=8
spring.redis.jedis.pool.min-idle=0
spring.redis.jedis.pool.max-wait=-1ms

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=30m
spring.cache.redis.cache-null-values=false
```

### Docker Compose

Redis is configured in `docker-compose.yml`:

```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  volumes:
    - redis-data:/data
  command: redis-server --appendonly yes
```

### Starting Redis

```bash
# Start Redis with Docker Compose
docker-compose up -d redis

# Verify Redis is running
docker-compose ps redis

# Connect to Redis CLI
docker-compose exec redis redis-cli

# Test connection
redis-cli ping
# Expected: PONG
```

---

## Cache Regions & TTL

### Cache Configuration

Each cache region has a specific Time-To-Live (TTL) based on data volatility:

| Cache Name | TTL | Use Case |
|------------|-----|----------|
| `adminDashboard` | 5 min | Admin dashboard data |
| `salesDashboard` | 5 min | Sales dashboard data |
| `inspectorDashboard` | 5 min | Inspector dashboard data |
| `financeDashboard` | 5 min | Finance dashboard data |
| `storeManagerDashboard` | 5 min | Store manager dashboard |
| `cars` | 15 min | Car list queries |
| `carDetails` | 15 min | Individual car details |
| `carModels` | 1 hour | Car model catalog (rarely changes) |
| `carStatistics` | 10 min | Car statistics and aggregates |
| `clients` | 30 min | Client profiles |
| `employees` | 30 min | Employee profiles |
| `sales` | 10 min | Sales records |
| `financialTransactions` | 10 min | Financial data |
| `revenueMetrics` | 5 min | Revenue calculations |
| `inquiries` | 10 min | Customer inquiries |
| `reservations` | 10 min | Vehicle reservations |
| `storageLocations` | 30 min | Storage facilities |
| `locationCapacity` | 5 min | Real-time capacity data |
| `tasks` | 10 min | Task management |
| `events` | 15 min | Calendar events |
| `inspections` | 15 min | Inspection records |
| `roles` | 2 hours | RBAC roles (rarely change) |
| `permissions` | 2 hours | RBAC permissions |
| `employeeRoles` | 30 min | Employee role assignments |
| `notifications` | 5 min | Notification records |
| `notificationTemplates` | 1 hour | Notification templates |

### TTL Guidelines

**Short TTL (5-10 minutes):**
- Dashboards with real-time metrics
- Financial data
- Frequently updated records

**Medium TTL (15-30 minutes):**
- User profiles
- Vehicle inventory
- Static data with occasional updates

**Long TTL (1-2 hours):**
- Configuration data
- Rarely changing reference data
- RBAC settings

---

## Using @Cacheable

### Basic Usage

```java
import org.springframework.cache.annotation.Cacheable;

@Service
public class CarServiceImpl implements CarService {
    
    @Cacheable(value = "carDetails", key = "#carId")
    public CarDetailDTO getCarById(Long carId) {
        // This method will be cached
        // First call: queries database
        // Subsequent calls: returns from cache
        return carRepository.findById(carId)
                .map(carMapper::toDetailDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
    }
}
```

### Cache Key Strategies

**1. Single Parameter Key:**
```java
@Cacheable(value = "carDetails", key = "#carId")
public CarDetailDTO getCarById(Long carId) { ... }
```

**2. Multiple Parameter Key:**
```java
@Cacheable(value = "cars", key = "#status + '_' + #location")
public List<CarDTO> findByStatusAndLocation(CarStatus status, Long location) { ... }
```

**3. Complex Key with SpEL:**
```java
@Cacheable(value = "sales", key = "#filter.startDate + '_' + #filter.endDate + '_' + #filter.employeeId")
public List<SaleDTO> findSalesByFilter(SaleFilter filter) { ... }
```

**4. Default Key (method parameters):**
```java
@Cacheable(value = "clients")
public List<ClientDTO> findAllClients() { ... }
```

### Conditional Caching

```java
// Only cache if result is not null
@Cacheable(value = "cars", key = "#vinNumber", unless = "#result == null")
public CarDTO findByVinNumber(String vinNumber) { ... }

// Only cache if certain condition is met
@Cacheable(value = "sales", key = "#year", condition = "#year >= 2020")
public List<SaleDTO> getSalesByYear(int year) { ... }
```

---

## Cache Invalidation

### Using @CacheEvict

**1. Evict Single Entry:**
```java
import org.springframework.cache.annotation.CacheEvict;

@Service
public class CarServiceImpl implements CarService {
    
    @CacheEvict(value = "carDetails", key = "#carId")
    public CarDTO updateCar(Long carId, CarUpdateDTO updateDTO) {
        // Update the car
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
        
        carMapper.updateEntityFromDTO(updateDTO, car);
        Car savedCar = carRepository.save(car);
        
        // Cache entry for this carId is automatically evicted
        return carMapper.toDTO(savedCar);
    }
}
```

**2. Evict Multiple Caches:**
```java
@CacheEvict(value = {"carDetails", "cars", "carStatistics"}, key = "#carId")
public void deleteCar(Long carId) {
    carRepository.deleteById(carId);
}
```

**3. Evict All Entries in Cache:**
```java
@CacheEvict(value = "cars", allEntries = true)
public CarDTO createCar(CarCreateDTO createDTO) {
    // Create new car
    // All entries in 'cars' cache are cleared
    return ...;
}
```

**4. Evict After Method Execution:**
```java
@CacheEvict(value = "sales", key = "#saleId", beforeInvocation = false)
public void processSale(Long saleId) {
    // Cache evicted AFTER successful execution
    // If exception is thrown, cache is NOT evicted
}
```

**5. Evict Before Method Execution:**
```java
@CacheEvict(value = "sales", key = "#saleId", beforeInvocation = true)
public void cancelSale(Long saleId) {
    // Cache evicted BEFORE method execution
    // Useful when method might throw exceptions
}
```

### Using @CachePut

Updates cache without skipping method execution:

```java
@CachePut(value = "carDetails", key = "#result.id")
public CarDTO updateCarPrice(Long carId, BigDecimal newPrice) {
    // Method always executes
    // Result is stored in cache, replacing old value
    Car car = carRepository.findById(carId).orElseThrow();
    car.setSellingPrice(newPrice);
    return carMapper.toDTO(carRepository.save(car));
}
```

### Using @Caching

Combine multiple cache annotations:

```java
@Caching(
    evict = {
        @CacheEvict(value = "carDetails", key = "#carId"),
        @CacheEvict(value = "cars", allEntries = true)
    },
    put = {
        @CachePut(value = "carDetails", key = "#result.id")
    }
)
public CarDTO updateCarStatus(Long carId, CarStatus newStatus) {
    // Complex cache operations
    return ...;
}
```

### Using CacheInvalidationService

For manual cache invalidation:

```java
@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    
    private final CacheInvalidationService cacheInvalidationService;
    
    public CarDTO createCar(CarCreateDTO createDTO) {
        // Save car to database
        Car savedCar = carRepository.save(car);
        
        // Manually invalidate caches
        cacheInvalidationService.invalidateCarCaches();
        
        return carMapper.toDTO(savedCar);
    }
    
    public void updateCar(Long carId, CarUpdateDTO updateDTO) {
        // Update car
        carRepository.save(car);
        
        // Invalidate specific car and related caches
        cacheInvalidationService.invalidateCarCache(carId);
        cacheInvalidationService.invalidateDashboards();
    }
}
```

---

## Manual Cache Operations

### Using RedisTemplate

For advanced cache operations:

```java
@Service
@RequiredArgsConstructor
public class CustomCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Store value with custom TTL
    public void cacheWithCustomTTL(String key, Object value, long minutes) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(minutes));
    }
    
    // Get cached value
    public Object getCachedValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    // Check if key exists
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    // Delete key
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
    
    // Set expiration time
    public void setExpiration(String key, long minutes) {
        redisTemplate.expire(key, Duration.ofMinutes(minutes));
    }
    
    // Get remaining TTL
    public Long getTimeToLive(String key) {
        return redisTemplate.getExpire(key);
    }
    
    // Store hash (map)
    public void cacheHash(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }
    
    // Get hash value
    public Object getHashValue(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }
    
    // Store list
    public void cacheList(String key, List<Object> values) {
        redisTemplate.opsForList().rightPushAll(key, values);
    }
    
    // Get list
    public List<Object> getList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }
}
```

---

## Best Practices

### 1. Choose Appropriate TTL

```java
// ❌ BAD: Too short TTL (defeats purpose of caching)
@Cacheable(value = "cars") // Default 30 min might be too short

// ✅ GOOD: Appropriate TTL for static data
@Cacheable(value = "carModels") // 1 hour - catalog rarely changes
```

### 2. Use Meaningful Cache Keys

```java
// ❌ BAD: Unclear cache key
@Cacheable(value = "data", key = "#id")

// ✅ GOOD: Clear, specific cache key
@Cacheable(value = "carDetails", key = "'car_' + #carId")
```

### 3. Invalidate Related Caches

```java
// ❌ BAD: Only invalidating one cache
@CacheEvict(value = "sales", key = "#saleId")
public void updateSale(Long saleId, SaleUpdateDTO dto) { ... }

// ✅ GOOD: Invalidate all related caches
@Caching(evict = {
    @CacheEvict(value = "sales", key = "#saleId"),
    @CacheEvict(value = "salesDashboard", allEntries = true),
    @CacheEvict(value = "revenueMetrics", allEntries = true)
})
public void updateSale(Long saleId, SaleUpdateDTO dto) { ... }
```

### 4. Handle Null Values Carefully

```java
// ❌ BAD: Caching null values
@Cacheable(value = "cars", key = "#carId")
public CarDTO findCar(Long carId) {
    return carRepository.findById(carId).orElse(null); // Null cached
}

// ✅ GOOD: Don't cache null values
@Cacheable(value = "cars", key = "#carId", unless = "#result == null")
public CarDTO findCar(Long carId) {
    return carRepository.findById(carId).orElse(null);
}
```

### 5. Cache Read-Heavy Operations

```java
// ✅ GOOD: Cache expensive dashboard queries
@Cacheable(value = "adminDashboard", key = "#employeeId")
public AdminDashboardResponse getAdminDashboard(Long employeeId) {
    // Complex aggregation queries
    return buildDashboard();
}

// ❌ BAD: Don't cache write operations
@Cacheable(value = "sales") // Don't do this!
public void createSale(SaleDTO sale) { ... }
```

### 6. Avoid Caching Large Objects

```java
// ❌ BAD: Caching huge result sets
@Cacheable(value = "allCars")
public List<CarDTO> findAllCars() { 
    // Could return 10,000+ records
}

// ✅ GOOD: Cache paginated results
@Cacheable(value = "cars", key = "#page + '_' + #size")
public Page<CarDTO> findCars(int page, int size) { ... }
```

### 7. Use Async Invalidation for Non-Critical Operations

```java
@Service
@RequiredArgsConstructor
public class BackgroundCacheService {
    
    private final CacheInvalidationService cacheInvalidationService;
    
    public void updateStatistics() {
        // Update statistics
        updateDatabase();
        
        // Invalidate caches asynchronously (non-blocking)
        cacheInvalidationService.asyncInvalidateDashboards();
    }
}
```

---

## Monitoring & Debugging

### Redis CLI Commands

```bash
# Connect to Redis
docker-compose exec redis redis-cli

# View all keys
KEYS *

# View keys in specific cache
KEYS carDetails::*

# Get specific cache value
GET "carDetails::123"

# Check TTL (time to live)
TTL "carDetails::123"

# Delete specific key
DEL "carDetails::123"

# Delete all keys (DANGEROUS!)
FLUSHALL

# View Redis info
INFO

# Monitor real-time commands
MONITOR

# View memory usage
MEMORY USAGE "carDetails::123"

# View all keys with pattern
SCAN 0 MATCH carDetails::* COUNT 100
```

### Application Logging

Add to service methods:

```java
@Service
@Slf4j
public class CarServiceImpl {
    
    @Cacheable(value = "carDetails", key = "#carId")
    public CarDTO getCarById(Long carId) {
        log.debug("Cache MISS - Fetching car from database: {}", carId);
        // Database query
        return ...;
    }
}
```

Cache hits won't log the message (method not executed).

### Cache Statistics

```java
@RestController
@RequestMapping("/api/v1/admin/cache")
@RequiredArgsConstructor
public class CacheAdminController {
    
    private final CacheManager cacheManager;
    
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                stats.put(cacheName, cache.getNativeCache());
            }
        });
        
        return stats;
    }
    
    @GetMapping("/names")
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
}
```

---

## Common Patterns

### Pattern 1: Cache-Aside (Lazy Loading)

```java
@Service
public class CarService {
    
    // Spring automatically implements cache-aside pattern
    @Cacheable(value = "carDetails", key = "#carId")
    public CarDTO getCarById(Long carId) {
        // 1. Check cache first
        // 2. If miss, query database
        // 3. Store result in cache
        // 4. Return result
        return findCarFromDatabase(carId);
    }
}
```

### Pattern 2: Write-Through

```java
@Service
public class CarService {
    
    @CachePut(value = "carDetails", key = "#result.id")
    public CarDTO updateCar(Long carId, CarUpdateDTO dto) {
        // Update database
        Car updated = carRepository.save(car);
        
        // Automatically update cache with new value
        return carMapper.toDTO(updated);
    }
}
```

### Pattern 3: Write-Behind (with invalidation)

```java
@Service
public class CarService {
    
    @CacheEvict(value = "carDetails", key = "#carId")
    public void updateCarAsync(Long carId, CarUpdateDTO dto) {
        // Invalidate cache immediately
        // Update database asynchronously
        asyncUpdateDatabase(carId, dto);
    }
}
```

### Pattern 4: Cache Warming

```java
@Service
@RequiredArgsConstructor
public class CacheWarmingService {
    
    private final CarService carService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @PostConstruct
    public void warmUpCaches() {
        log.info("Warming up caches...");
        
        // Pre-load frequently accessed data
        List<Car> popularCars = carRepository.findTop100ByOrderByViewCountDesc();
        
        popularCars.forEach(car -> {
            CarDTO dto = carMapper.toDTO(car);
            String cacheKey = "carDetails::" + car.getId();
            redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofMinutes(15));
        });
        
        log.info("Cache warming completed");
    }
}
```

### Pattern 5: Cache Refresh

```java
@Service
public class CarService {
    
    @Scheduled(cron = "0 */30 * * * *") // Every 30 minutes
    @CacheEvict(value = "carStatistics", allEntries = true)
    public void refreshStatisticsCache() {
        log.info("Refreshing car statistics cache");
        // Cache will be repopulated on next request
    }
}
```

---

## Troubleshooting

### Issue 1: Cache Not Working

**Symptoms:** Methods still hitting database

**Solutions:**
```java
// 1. Ensure @EnableCaching is present
@Configuration
@EnableCaching  // ← Add this
public class RedisConfig { ... }

// 2. Check method is public
@Cacheable(value = "cars", key = "#carId")
public CarDTO getCar(Long carId) { ... }  // ✅ Public

@Cacheable(value = "cars", key = "#carId")
private CarDTO getCar(Long carId) { ... } // ❌ Won't work

// 3. Don't call cached method from same class
@Service
public class CarService {
    @Cacheable("cars")
    public CarDTO getCar(Long id) { ... }
    
    public void process() {
        this.getCar(1L); // ❌ Cache bypassed (self-invocation)
    }
}
```

### Issue 2: Serialization Errors

**Error:** `SerializationException: Could not read JSON`

**Solution:**
```java
// Ensure DTOs are serializable
@Data
@JsonSerialize
@JsonDeserialize
public class CarDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    // fields...
}

// Or use String keys and manual serialization
@Cacheable(value = "cars", key = "#carId")
public String getCarJson(Long carId) {
    CarDTO car = findCar(carId);
    return objectMapper.writeValueAsString(car);
}
```

### Issue 3: Stale Data

**Symptoms:** Old data returned from cache

**Solutions:**
```java
// 1. Reduce TTL
@Cacheable(value = "realTimeData") // Uses short TTL from config

// 2. Add manual invalidation
@Service
@RequiredArgsConstructor
public class CarService {
    private final CacheInvalidationService cacheInvalidation;
    
    public void updateCar(Long carId, CarUpdateDTO dto) {
        // Update database
        carRepository.save(car);
        
        // Invalidate cache
        cacheInvalidation.invalidateCarCache(carId);
    }
}

// 3. Use @CacheEvict on update methods
@CacheEvict(value = "cars", allEntries = true)
public void updateCar(...) { ... }
```

### Issue 4: Memory Issues

**Symptoms:** Redis running out of memory

**Solutions:**
```bash
# Check Redis memory
redis-cli INFO memory

# Set max memory and eviction policy
redis-cli CONFIG SET maxmemory 512mb
redis-cli CONFIG SET maxmemory-policy allkeys-lru

# Clear all caches
redis-cli FLUSHALL
```

```java
// In application: Clear caches programmatically
@Service
@RequiredArgsConstructor
public class CacheManagementService {
    private final CacheInvalidationService cacheInvalidation;
    
    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    public void clearOldCaches() {
        log.info("Clearing old caches");
        cacheInvalidation.clearAllCaches();
    }
}
```

---

## Performance Tips

1. **Monitor cache hit ratio** - Aim for >80% hit ratio
2. **Use appropriate TTLs** - Balance freshness vs performance
3. **Cache expensive operations** - Complex queries, aggregations
4. **Don't cache everything** - Only read-heavy, expensive operations
5. **Invalidate proactively** - Don't wait for TTL expiration
6. **Use pagination** - Don't cache massive result sets
7. **Consider cache warming** - Pre-load popular data
8. **Monitor Redis memory** - Set limits and eviction policies

---

## Quick Reference

### Common Annotations

| Annotation | Purpose |
|------------|---------|
| `@Cacheable` | Cache method result |
| `@CacheEvict` | Remove from cache |
| `@CachePut` | Update cache |
| `@Caching` | Combine multiple cache operations |
| `@EnableCaching` | Enable caching support |

### CacheInvalidationService Methods

| Method | Use Case |
|--------|----------|
| `invalidateDashboards()` | Clear all dashboard caches |
| `invalidateCarCaches()` | Clear car-related caches |
| `invalidateSaleCaches()` | Clear sale caches |
| `clearAllCaches()` | Nuclear option - clear everything |

### Redis CLI Commands

| Command | Purpose |
|---------|---------|
| `KEYS *` | List all keys |
| `GET key` | Get value |
| `DEL key` | Delete key |
| `TTL key` | Check time to live |
| `FLUSHALL` | Delete all keys |

---

**For more information, see:**
- [Dashboard Documentation](dashboard/README.md)
- [API Documentation](http://localhost:8080/api/v1/swagger-ui.html)
- [Redis Official Documentation](https://redis.io/documentation)

---

**Version History:**
- v1.0 (Jan 2026) - Initial documentation

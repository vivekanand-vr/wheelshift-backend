# WheelShift Pro — Architecture Review & Improvement Roadmap

**Author:** Senior Architecture Review  
**Date:** March 2026  
**Status:** Living Document — update as features are implemented

---

## Table of Contents

1. [Current State Assessment](#1-current-state-assessment)
2. [Critical Issues (Fix Immediately)](#2-critical-issues-fix-immediately)
3. [Architecture Improvements](#3-architecture-improvements)
4. [Design Patterns to Apply](#4-design-patterns-to-apply)
5. [Tools & Libraries to Add](#5-tools--libraries-to-add)
6. [Testing Strategy](#6-testing-strategy)
7. [Performance & Resilience](#7-performance--resilience)
8. [Observability Stack](#8-observability-stack)
9. [Security Hardening](#9-security-hardening)
10. [Feature Roadmap (PM Perspective)](#10-feature-roadmap-pm-perspective)
11. [Configuration Management](#11-configuration-management)
12. [DevOps & Deployment](#12-devops--deployment)

---

## 1. Current State Assessment

### Strengths ✅

| Area | What's Good |
|------|-------------|
| **Layer separation** | Clean Controller → Service → Repository design with interface+impl pattern |
| **Entity design** | Good use of JPA annotations, polymorphic vehicle support (CAR/MOTORCYCLE), BaseEntity |
| **Security** | JWT-based stateless auth, Spring Security 6, method-level `@PreAuthorize` |
| **Validation** | Custom validators (`ConditionalVehicleId`, `VehicleIdRequired`), Bean Validation groups (OnCreate/OnUpdate) |
| **Mapping** | MapStruct for zero-runtime-cost DTO mapping |
| **Error handling** | RFC 7807 Problem Details error responses, centralized `@RestControllerAdvice` |
| **Schema management** | Flyway versioned migrations with rollback scripts |
| **Caching** | Redis with multi-TTL cache regions, `@EnableCaching`, `CacheInvalidationService` |
| **File storage** | Abstracted file storage (local/S3 hybrid), UUID-based file IDs |
| **RBAC** | Fine-grained roles + permissions + data scopes + per-resource ACLs |
| **Notifications** | Well-designed notification system schema (events → jobs → deliveries) |
| **Observability** | Logback with async file appenders, separate error log |
| **Docker** | Docker Compose for dev with debugging port, Redis Insights, phpMyAdmin |

### Gaps & Issues ❌

| Priority | Issue | Impact |
|----------|-------|--------|
| 🔴 **CRITICAL** | JWT secret and DB credentials stored in plaintext in `application.properties` | Security breach risk |
| 🔴 **CRITICAL** | `IllegalArgumentException` mapped to HTTP 500 (should be 400) | Incorrect API contract |
| 🟠 **HIGH** | `BaseEntity` has both JPA `AuditingEntityListener` AND manual `@PrePersist/@PreUpdate` — dual population | Data inconsistency risk |
| 🟠 **HIGH** | `FileMetadata` doesn't extend `BaseEntity` — inconsistent auditing pattern | Code inconsistency |
| 🟠 **HIGH** | `ConditionalVehicleIdValidator` uses reflection (`field.setAccessible(true)`) — fragile and slow | Runtime errors on obfuscation |
| 🟠 **HIGH** | `AuthController` directly injects `EmployeeRepository` — violates service layer | Architecture violation |
| 🟠 **HIGH** | No `@EnableAsync`, no `TaskExecutor` bean — `@Async` won't work | Silent failures |
| 🟡 **MEDIUM** | `FileUrlBuilder` uses static fields populated via `@PostConstruct` — not testable | Testing difficulty |
| 🟡 **MEDIUM** | `Sale` table `@UniqueConstraint` only covers `car_id`, not `motorcycle_id` | Data integrity gap |
| 🟡 **MEDIUM** | Missing `spring-boot-starter-actuator` in pom.xml (referenced in docker health check) | Docker health check fails |
| 🟡 **MEDIUM** | No rate limiting on public endpoints | DDoS/brute-force vulnerability |
| 🟡 **MEDIUM** | No async notification processing | Blocking request threads |
| 🟡 **MEDIUM** | No integration tests — only `contextLoads()` | Zero test confidence |
| 🟡 **MEDIUM** | `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")` — MANAGER role doesn't exist | Auth bypass gap |
| 🟢 **LOW** | No structured JSON logging for production | Log aggregation difficulty |
| 🟢 **LOW** | No Resilience4j circuit breaker | Cascade failure risk |
| 🟢 **LOW** | No API response compression | Bandwidth waste |
| 🟢 **LOW** | No `@Scheduled` reservation expiry processor | Reservations never auto-expire |

---

## 2. Critical Issues (Fix Immediately)

### 2.1 Secrets Management

Never store secrets in version-controlled property files. Move all credentials to environment variables.

**Current (INSECURE):**
```properties
jwt.secret=4db30fb122f51e32a6e0d34d10905cef
spring.datasource.password=wheelshift_pass_2025
```

**Fixed:**
```properties
jwt.secret=${JWT_SECRET}
spring.datasource.password=${DB_PASSWORD}
```

Add to `.gitignore`:
```
.env
application-local.properties
*.secret
```

Use a minimum 256-bit (32-byte) randomly generated JWT secret in production:
```bash
openssl rand -hex 32
```

### 2.2 IllegalArgumentException → HTTP 400

`IllegalArgumentException` is a business validation failure, not a server error. The current handler returns HTTP 500. Change to 400 Bad Request.

### 2.3 BaseEntity Duplicate Auditing

`BaseEntity` has `@EntityListeners(AuditingEntityListener.class)` for JPA auditing AND manual `@PrePersist/@PreUpdate` hooks — both write to the same fields. Remove the manual hooks entirely; JPA auditing covers it.

### 2.4 Enable @Async

Without an `@EnableAsync` annotated configuration class and a `TaskExecutor` bean, all `@Async` annotations are silently ignored — every "async" call blocks the caller thread. Add an `AsyncConfig` class.

---

## 3. Architecture Improvements

### 3.1 Current Architecture

```
Client → Controller → Service (interface) → ServiceImpl
                    → Repository (JPA)
                    → Mapper (MapStruct)
                    → Cache (Redis)
```

The existing interface+impl pattern in the service layer is correct. The main issue is some code bypasses the service layer (e.g., `AuthController` → `EmployeeRepository`).

### 3.2 Recommended Enhancements

#### Domain Event Pattern
Decouple business logic from side effects using Spring Application Events:

```java
// Publish domain events from service layer
@Service
public class ReservationServiceImpl {
    @Autowired ApplicationEventPublisher eventPublisher;
    
    public ReservationResponse createReservation(ReservationRequest req) {
        Reservation reservation = save(req);
        // Don't call NotificationService directly — publish an event
        eventPublisher.publishEvent(new ReservationCreatedEvent(reservation));
        return mapper.toResponse(reservation);
    }
}

// Listen in the notification component
@Component
public class ReservationNotificationListener {
    @Async
    @EventListener
    public void onReservationCreated(ReservationCreatedEvent event) {
        notificationService.sendReservationCreatedNotification(event);
    }
}
```

**Benefits:** Services don't depend on notification logic. Notifications can fail without rolling back the reservation. Easy to add more listeners (analytics, webhooks) without modifying core logic.

#### UseCase / Command Pattern (optional, for complex workflows)
For complex multi-step operations like "convert reservation to sale," encapsulate in a dedicated use case class:

```
service/
├── ReservationService.java          (CRUD operations)
├── impl/ReservationServiceImpl.java
└── usecase/
    └── ConvertReservationToSaleUseCase.java  (orchestrates multiple services)
```

#### Repository Specification Pattern
For complex dynamic queries, use JPA Specifications instead of proliferating custom query methods:

```java
// CarSpecification.java
public class CarSpecification {
    public static Specification<Car> hasStatus(CarStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
    public static Specification<Car> yearBetween(Integer min, Integer max) { ... }
}

// CarRepository.java
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {}
```

### 3.3 Layered Dependency Rule (enforce strictly)

```
Controller  →  Service Interface  →  Repository
     ↓               ↓
   DTOs          Domain Events
     ↓               ↓
  Validators    Event Listeners
```

**Rules to enforce:**
- Controllers **never** inject Repositories directly
- Services **never** reference HTTP types (`HttpServletRequest`, `HttpStatus`)
- Entities are **never** serialized to the client — always go through DTOs
- Mappers are **only** called from the service layer, not controllers

---

## 4. Design Patterns to Apply

### 4.1 Strategy Pattern — Notification Channels ✅ (Design exists, wire it up)

The notification channel enum is already in place. The dispatcher should select the channel strategy at runtime:

```java
public interface NotificationChannelStrategy {
    NotificationChannel getChannel();
    void send(NotificationJob job);
}

@Component public class EmailChannelStrategy implements NotificationChannelStrategy { ... }
@Component public class SmsChannelStrategy implements NotificationChannelStrategy { ... }
@Component public class InAppChannelStrategy implements NotificationChannelStrategy { ... }

@Service
public class NotificationDispatcher {
    private final Map<NotificationChannel, NotificationChannelStrategy> strategies;
    
    public NotificationDispatcher(List<NotificationChannelStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(NotificationChannelStrategy::getChannel, identity()));
    }
    
    public void dispatch(NotificationJob job) {
        strategies.get(job.getChannel()).send(job);
    }
}
```

### 4.2 Template Method — Vehicle Status Transitions

Cars and motorcycles share the same lifecycle (AVAILABLE → RESERVED → SOLD). Extract the common logic:

```java
public abstract class VehicleStatusManager<V> {
    public final void reserve(V vehicle) {
        validateCanReserve(vehicle);
        doReserve(vehicle);
        afterReserve(vehicle);
    }
    protected abstract void validateCanReserve(V vehicle);
    protected abstract void doReserve(V vehicle);
    protected void afterReserve(V vehicle) {} // Optional hook
}

@Component public class CarStatusManager extends VehicleStatusManager<Car> { ... }
@Component public class MotorcycleStatusManager extends VehicleStatusManager<Motorcycle> { ... }
```

### 4.3 Builder Pattern — Complex Query Filters

Already partially using `Pageable`. Consolidate all search filter classes into proper filter objects:

```java
@Builder
public record CarSearchFilter(
    CarStatus status,
    String brand,
    Integer minYear,
    Integer maxYear,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Long storageLocationId
) {}
```

### 4.4 Factory Pattern — Notification Event Creation

```java
@Component
public class NotificationEventFactory {
    public NotificationEvent forReservationCreated(Reservation r) { ... }
    public NotificationEvent forSaleCompleted(Sale s) { ... }
    public NotificationEvent forTaskAssigned(Task t, Employee assignee) { ... }
}
```

### 4.5 Decorator Pattern — Caching Layer

Currently caching is implemented with annotations. For complex scenarios, consider explicit cache decorators:

```java
@Primary
@Service("cachedCarService")
public class CachedCarService implements CarService {
    private final CarServiceImpl delegate;
    private final CacheManager cacheManager;
    // Override only cache-worthy methods, delegate rest
}
```

### 4.6 Saga Pattern — Reservation-to-Sale Workflow

The reservation-to-sale conversion involves multiple entities. If any step fails, all should roll back:

```java
@Service
@Transactional
public class ConvertReservationToSaleUseCase {
    // 1. Validate reservation is CONFIRMED
    // 2. Create Sale record
    // 3. Update vehicle status to SOLD
    // 4. Update reservation status to COMPLETED
    // 5. Update client purchase count
    // 6. Publish SaleCompletedEvent
    // All in one @Transactional boundary = atomicity
}
```

---

## 5. Tools & Libraries to Add

### 5.1 Testing

| Library | Maven Artifact | Purpose |
|---------|---------------|---------|
| **Testcontainers** | `org.testcontainers:mysql`, `org.testcontainers:redis` | Real database/Redis in tests |
| **REST Assured** | `io.rest-assured:rest-assured` | Fluent HTTP integration testing |
| **ArchUnit** | `com.tngtech.archunit:archunit-junit5` | Enforce architecture rules in tests |
| **JaCoCo** | Maven plugin | Code coverage reports |

```xml
<!-- Testcontainers BOM -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-bom</artifactId>
    <version>1.19.7</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

**ArchUnit example — enforce no repo injection in controllers:**
```java
@AnalyzeClasses(packages = "com.wheelshiftpro")
class ArchitectureRulesTest {
    @ArchTest
    static final ArchRule noRepoInController = noClasses()
        .that().resideInAPackage("..controller..")
        .should().dependOnClassesThat().resideInAPackage("..repository..");
}
```

### 5.2 Resilience & Rate Limiting

| Library | Purpose | Key Feature |
|---------|---------|-------------|
| **Resilience4j** | Circuit breaker, retry, rate limiter | Prevent cascade failures |
| **Bucket4j** | Token bucket rate limiting | Protect public endpoints from brute-force |

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

**Bucket4j rate limiting on login endpoint:**
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        if (request.getRequestURI().equals("/api/v1/auth/login")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = buckets.computeIfAbsent(ip, k -> 
                Bucket.builder()
                    .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                    .build()
            );
            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### 5.3 HTTP Logging

| Library | Purpose |
|---------|---------|
| **Zalando Logbook** | Structured request/response HTTP logging with masking for sensitive fields |

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-spring-boot-starter</artifactId>
    <version>3.9.0</version>
</dependency>
```

```properties
logbook.filter.enabled=true
logbook.secure-filter.enabled=true
logbook.format.style=json
logbook.obfuscate.headers=Authorization,X-Secret
logbook.obfuscate.parameters=password,secret
```

### 5.4 Observability

| Library | Purpose |
|---------|---------|
| **Micrometer Prometheus** | Export metrics for Prometheus/Grafana |
| **Spring Boot Actuator** | Health checks, info endpoint, metrics |
| **OpenTelemetry** | Distributed tracing (future) |

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

### 5.5 Validation & Utility

| Library | Purpose |
|---------|---------|
| **Hibernate Validator** | Already present — ensure latest version |
| **Apache Commons Lang 3** | Already present |
| **Guava** | Immutable collections, caching, string utilities |

### 5.6 Development Tools

| Tool | Purpose |
|------|---------|
| **Spring Boot DevTools** | Already present |
| **Spotless** | Auto-format code on build |
| **Checkstyle** | Style enforcement |
| **SpotBugs** | Static analysis |
| **OWASP Dependency Check** | Scan for known CVEs in dependencies |

```xml
<!-- OWASP Dependency Check Plugin -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
</plugin>
```

### 5.7 Scheduled Jobs — ShedLock

For the reservation expiry scheduler, use ShedLock to prevent duplicate execution if the app scales to multiple instances:

```xml
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.13.0</version>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-redis-spring</artifactId>
    <version>5.13.0</version>
</dependency>
```

---

## 6. Testing Strategy

### 6.1 Test Pyramid

```
         /\
        /  \     E2E Tests (few, slow)
       /    \    — Full HTTP round-trips via REST Assured
      /------\
     /        \  Integration Tests (moderate)
    /  Tests   \ — @SpringBootTest + Testcontainers
   /            \ — @WebMvcTest for controllers
  /--------------\
 /                \ Unit Tests (many, fast)
/    Unit Tests    \ — @ExtendWith(MockitoExtension.class)
/------------------\ — Pure service logic
```

### 6.2 Unit Tests (Priority)

Every service method should have a unit test with Mockito mocks:

```java
@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock CarRepository carRepository;
    @Mock CarMapper carMapper;
    @Mock StorageLocationRepository storageRepo;
    @InjectMocks CarServiceImpl carService;

    @Test
    void createCar_shouldSaveAndReturnResponse() {
        CarRequest req = CarTestFactory.buildRequest();
        Car entity = CarTestFactory.buildEntity();
        CarResponse expected = CarTestFactory.buildResponse();

        when(carMapper.toEntity(req)).thenReturn(entity);
        when(carRepository.save(entity)).thenReturn(entity);
        when(carMapper.toResponse(entity)).thenReturn(expected);

        CarResponse result = carService.createCar(req);
        assertThat(result).isEqualTo(expected);
    }
    
    @Test
    void deleteCar_whenCarIsSold_shouldThrowBusinessException() {
        Car soldCar = Car.builder().status(CarStatus.SOLD).build();
        when(carRepository.findById(1L)).thenReturn(Optional.of(soldCar));
        
        assertThatThrownBy(() -> carService.deleteCar(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("sold");
    }
}
```

### 6.3 Integration Tests with Testcontainers

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class CarApiIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("wheelshift_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    void createCar_shouldReturn201() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + getTestToken())
            .body(CarTestFactory.buildRequest())
        .when()
            .post("/api/v1/cars")
        .then()
            .statusCode(201)
            .body("data.vinNumber", equalTo("17CHARVINTEST0001"));
    }
}
```

### 6.4 Architecture Tests with ArchUnit

```java
@AnalyzeClasses(packages = "com.wheelshiftpro", importOptions = DoNotIncludeTests.class)
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule servicesShouldNotDependOnControllers = noClasses()
        .that().resideInAPackage("..service..")
        .should().dependOnClassesThat().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule controllersNeverAccessRepositories = noClasses()
        .that().resideInAPackage("..controller..")
        .should().dependOnClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule entitiesNotReturnedFromControllers = noMethods()
        .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
        .should().haveReturnType(assignableTo(BaseEntity.class));
}
```

### 6.5 Security Tests

```java
@WebMvcTest(CarController.class)
@Import(SecurityConfig.class)
class CarControllerSecurityTest {

    @Test
    @WithMockUser(roles = "SALES")
    void deleteCar_asSalesRole_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/cars/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getCar_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/cars/1"))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## 7. Performance & Resilience

### 7.1 Database Optimizations

**Connection Pool Tuning (add to application-prod.properties):**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.pool-name=WheelShiftHikariPool
```

**Query Optimization:**
- Use `@EntityGraph` or `JOIN FETCH` on hotspot queries to avoid N+1 problems
- Use projections (interfaces/records) instead of full entity fetches for list endpoints:

```java
// Instead of loading full Car with all lazy associations:
public interface CarSummary {
    Long getId();
    String getVinNumber();
    CarStatus getStatus();
    BigDecimal getSellingPrice();
}
// Repository:
List<CarSummary> findAllProjectedBy(Pageable pageable);
```

- Add composite indexes for most common filter combinations in upcoming migrations

**Lazy Loading Discipline:**
- Always use `FetchType.LAZY` on all relationships (already done ✅)
- Never call `.size()` or iterate collections outside a `@Transactional` context

### 7.2 Caching Improvements

**Cache Warming:**
```java
@Component
public class CacheWarmupListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        carModelService.getAllCarModels();       // warm car models
        storageLocationService.getAllLocations(); // warm storage locations
    }
}
```

**Cache Stampede Prevention:**
Use Redisson with `RLock` for expensive cache-miss computations to prevent multiple threads from rebuilding the same cache simultaneously.

### 7.3 Async Processing

**Current state:** `@Async` annotations in code won't work without `@EnableAsync`.

**Add `AsyncConfig`:**
```java
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {
    
    @Bean(name = "notificationExecutor")
    public TaskExecutor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("notification-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "reportExecutor")  
    public TaskExecutor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("report-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
            log.error("Async method '{}' threw: {}", method.getName(), ex.getMessage(), ex);
    }
}
```

### 7.4 Scheduled Jobs (Missing)

**Reservation Expiry Processor:**
```java
@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Scheduled(fixedRate = 300_000) // Every 5 minutes
    @SchedulerLock(name = "reservation-expiry", lockAtMostFor = "4m")
    @Transactional
    public void expireStaleReservations() {
        List<Reservation> expired = reservationRepository
            .findAllByStatusAndExpiryDateBefore(ReservationStatus.CONFIRMED, LocalDateTime.now());
        
        expired.forEach(r -> {
            r.setStatus(ReservationStatus.EXPIRED);
            // Revert vehicle to AVAILABLE
            getVehicle(r).makeAvailable();
            eventPublisher.publishEvent(new ReservationExpiredEvent(r));
        });
    }
}
```

### 7.5 Response Compression

```properties
# application.properties
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/plain
server.compression.min-response-size=1024
```

### 7.6 Resilience4j Circuit Breaker

Protect external calls (email, SMS providers) from cascade failures:

```java
@Service
public class EmailChannelStrategy implements NotificationChannelStrategy {
    
    @CircuitBreaker(name = "emailProvider", fallbackMethod = "emailFallback")
    @Retry(name = "emailProvider")
    public void send(NotificationJob job) {
        emailProvider.send(job);
    }
    
    private void emailFallback(NotificationJob job, Exception ex) {
        log.error("Email circuit open, queuing for retry: {}", job.getId());
        retryQueue.add(job);
    }
}
```

---

## 8. Observability Stack

### 8.1 Metrics with Micrometer + Prometheus

**Custom business metrics:**
```java
@Component
public class BusinessMetrics {
    private final Counter saleCounter;
    private final Counter reservationCounter;
    private final Gauge inventoryGauge;
    
    public BusinessMetrics(MeterRegistry registry, CarRepository carRepo) {
        this.saleCounter = Counter.builder("wheelshift.sales.total")
            .description("Total sales recorded")
            .register(registry);
        
        this.inventoryGauge = Gauge.builder("wheelshift.inventory.available", 
            carRepo, r -> r.countByStatus(CarStatus.AVAILABLE))
            .description("Currently available cars")
            .register(registry);
    }
    
    public void recordSale() { saleCounter.increment(); }
}
```

**Grafana Dashboard configuration** (add to `docker-compose-dev.yml`):
```yaml
grafana:
  image: grafana/grafana:latest
  ports: ["3000:3000"]
  environment:
    GF_SECURITY_ADMIN_PASSWORD: admin
  volumes:
    - grafana_data:/var/lib/grafana

prometheus:
  image: prom/prometheus:latest
  ports: ["9090:9090"]
  volumes:
    - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
```

### 8.2 Structured JSON Logging (Production)

Add a production Logback profile with JSON output for ELK Stack ingestion:

```xml
<!-- logback-spring.xml — add profile-specific JSON appender -->
<springProfile name="prod">
    <appender name="JSON_STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyNames>traceId,spanId,employeeId,requestId</includeMdcKeyNames>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON_STDOUT"/>
        <appender-ref ref="ASYNC_FILE"/>
    </root>
</springProfile>
```

Add dependency:
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### 8.3 Request Correlation IDs

Add a filter that assigns a unique `X-Request-Id` to every request and stores it in MDC:

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String requestId = Optional.ofNullable(req.getHeader("X-Request-Id"))
            .orElse(UUID.randomUUID().toString());
        MDC.put("requestId", requestId);
        res.setHeader("X-Request-Id", requestId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}
```

---

## 9. Security Hardening

### 9.1 Security Headers

Add security headers via `SecurityConfig`:
```java
http.headers(headers -> headers
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
    .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable) // browser handles it
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000))
    .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
);
```

### 9.2 Rate Limiting on Auth Endpoints

Protect `/api/v1/auth/login` with IP-based rate limiting (10 requests/minute per IP).

### 9.3 Input Sanitization

The `ConditionalVehicleIdValidator` uses raw Java reflection with `setAccessible(true)`. Replace with an interface-based approach:

```java
public interface VehicleRequest {
    Long getCarId();
    Long getMotorcycleId();
    VehicleType getVehicleType();
}

// Validator becomes simple and safe:
public class ConditionalVehicleIdValidator implements ConstraintValidator<ConditionalVehicleId, VehicleRequest> {
    public boolean isValid(VehicleRequest value, ConstraintValidatorContext ctx) {
        if (value.getVehicleType() == VehicleType.CAR) return value.getCarId() != null;
        if (value.getVehicleType() == VehicleType.MOTORCYCLE) return value.getMotorcycleId() != null;
        return false;
    }
}
```

### 9.4 JWT Hardening

- Use **RS256** (RSA asymmetric) instead of HS256 for JWT signing in production — enables token verification without sharing the signing key
- Implement **token blacklisting** in Redis for logout on all devices
- Add **JWT expiry** claim validation explicitly
- Minimum key length: **2048-bit RSA** or **256-bit HMAC**

### 9.5 OWASP Dependency Scanning

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <suppressionFile>owasp-suppressions.xml</suppressionFile>
    </configuration>
</plugin>
```

Run: `mvn dependency-check:check`

### 9.6 File Upload Security

The file storage service should:
- Validate file content (magic bytes), not just extension/MIME
- Generate random UUIDs for stored filenames (already done ✅)
- Never execute uploaded files
- Set `Content-Disposition: attachment` for downloads to prevent XSS via SVG/HTML files
- Implement virus scanning for production (ClamAV integration)

---

## 10. Feature Roadmap (PM Perspective)

### Phase 1 — Stability & Quality (1-2 weeks) 🔴 NOW

| # | Feature | Business Value |
|---|---------|---------------|
| P1.1 | Fix all critical bugs listed in Section 2 | Prevents production incidents |
| P1.2 | Add Testcontainers + integration test suite | Catch regressions before deploy |
| P1.3 | Move secrets to environment variables | Security compliance |
| P1.4 | Add reservation expiry scheduler | Vehicles stuck as "reserved" permanently |
| P1.5 | Wire up `@Async` for notifications | Notification sends blocking API responses |

### Phase 2 — Observability & Operations (2-4 weeks) 🟠

| # | Feature | Business Value |
|---|---------|---------------|
| P2.1 | Prometheus + Grafana dashboard | Visibility into system health |
| P2.2 | Logbook HTTP logging | Debug production API issues |
| P2.3 | Request correlation IDs | Trace individual request flows |
| P2.4 | Custom business metrics (sales/day, inventory count) | Business KPI tracking |
| P2.5 | Actuator health endpoint hardening | Infrastructure health monitoring |

### Phase 3 — Resilience & Performance (4-6 weeks) 🟡

| # | Feature | Business Value |
|---|---------|---------------|
| P3.1 | Rate limiting on public endpoints | Brute-force protection |
| P3.2 | Resilience4j on external service calls | Prevent cascade failures |
| P3.3 | JPA specification-based querying | Better performance, flexible filters |
| P3.4 | Query projection interfaces | Reduce data transfer for list endpoints |
| P3.5 | Database index tuning (based on slow query log) | Faster queries |
| P3.6 | ShedLock for distributed scheduling | Multi-instance safety |

### Phase 4 — Features (6-10 weeks) 🟢

| # | Feature | Business Value |
|---|---------|---------------|
| P4.1 | **Email notifications** — real JavaMail/SendGrid integration | Customer communication |
| P4.2 | **WhatsApp Integration** — Meta Cloud API | Indian market preference |
| P4.3 | **PDF Report Generation** — JasperReports or iText7 | Sales invoices, inspection reports |
| P4.4 | **Excel Export** — Apache POI | Finance exports, inventory reports |
| P4.5 | **Vehicle Price History** — track price changes over time | Price trend analytics |
| P4.6 | **Commission Calculator** — configurable rules per role | Finance accuracy |
| P4.7 | **Bulk Import** — CSV/Excel upload for inventory | Rapid stock ingestion |
| P4.8 | **Customer Portal** (separate service) — view vehicle inquiry/purchase history | Customer self-service |
| P4.9 | **Comparison Tool** — compare up to 3 vehicles side-by-side | Sales enablement |
| P4.10 | **Vehicle Valuation Engine** — depreciation model | Pricing intelligence |

### Phase 5 — Advanced (10+ weeks) ⚪

| # | Feature | Business Value |
|---|---------|---------------|
| P5.1 | **Real-time WebSocket updates** — live inventory changes | Better UX for sales team |
| P5.2 | **Multi-tenant architecture** — support multiple dealerships | B2B SaaS expansion |
| P5.3 | **AI/ML price prediction** — based on market data | Competitive pricing |
| P5.4 | **Marketplace integration** — push listings to Cars24, Cardekho | Demand generation |
| P5.5 | **Mobile app backend** — FCM push notifications, offline sync | Mobile sales app |

---

## 11. Configuration Management

### 11.1 Profile Strategy

```
application.properties          — base configuration (no secrets)
application-dev.properties      — local development overrides
application-prod.properties     — production overrides (secrets via env vars)
application-test.properties     — test profile (H2 / Testcontainers)
```

### 11.2 Environment Variables Reference

All secrets must come from environment variables:

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET` | 256-bit base64 secret for JWT signing | PROD |
| `DB_URL` | JDBC connection URL | PROD |
| `DB_USERNAME` | Database username | PROD |
| `DB_PASSWORD` | Database password | PROD |
| `REDIS_HOST` | Redis hostname | PROD |
| `REDIS_PASSWORD` | Redis password | PROD |
| `AWS_ACCESS_KEY` | S3 access key | PROD (if S3) |
| `AWS_SECRET_KEY` | S3 secret key | PROD (if S3) |
| `MAIL_HOST` | SMTP server | PROD |
| `MAIL_PASSWORD` | SMTP password | PROD |
| `SEND_GRID_API_KEY` | SendGrid API key | PROD |
| `TWILIO_AUTH_TOKEN` | Twilio SMS token | PROD |

---

## 12. DevOps & Deployment

### 12.1 Docker Improvements

**Multi-stage Dockerfile for smaller production image:**
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Production stage — minimal JRE, not full JDK
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser  # Don't run as root
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

### 12.2 Kubernetes Readiness

When moving to Kubernetes, ensure:
- `/actuator/health/liveness` → liveness probe
- `/actuator/health/readiness` → readiness probe
- Graceful shutdown: `server.shutdown=graceful` + `spring.lifecycle.timeout-per-shutdown-phase=30s`
- Resource limits and requests defined

### 12.3 CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/ci.yml
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - run: mvn verify           # Runs tests + JaCoCo
      - run: mvn dependency-check:check  # OWASP CVE scan
      - uses: codecov/codecov-action@v4
```

---

## Quick Reference — Architecture Decision Log

| Decision | Chosen | Alternatives Considered | Rationale |
|----------|--------|------------------------|-----------|
| Auth mechanism | JWT (stateless) | Session-based | Scalability, no sticky sessions needed |
| Cache | Redis | Caffeine | Distributed, survives restarts, pub/sub |
| DB migration | Flyway | Liquibase | Simpler SQL-based, version-controlled |
| Mapping | MapStruct | ModelMapper | Compile-time, type-safe, zero runtime cost |
| Notification dispatch | Strategy pattern | If/else chain | Open/closed — add channels without modifying dispatcher |
| File IDs | UUID | Sequential ID | No enumeration attack, S3 compatible |
| Error format | RFC 7807 Problem Details | Custom format | Standard, tooling compatible |
| Polymorphic vehicles | CAR/MOTORCYCLE + vehicle_type discriminator | Inheritance table-per-class | Simpler SQL, no JOINs required |

---

*This document should be revised each sprint as architecture decisions are made and implemented.*

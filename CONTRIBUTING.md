# Contributing to WheelShift Pro

This guide covers everything you need to contribute to the WheelShift Pro backend:
architecture overview, folder conventions, how to add features, database migrations,
and development workflow.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Project Structure](#2-project-structure)
3. [Layered Architecture Rules](#3-layered-architecture-rules)
4. [Domain Map](#4-domain-map)
5. [Adding a New Feature — Step-by-Step](#5-adding-a-new-feature--step-by-step)
6. [Database Migrations](#6-database-migrations)
7. [Security & RBAC](#7-security--rbac)
8. [Running Locally](#8-running-locally)
9. [Testing](#9-testing)
10. [Code Conventions](#10-code-conventions)

---

## 1. Architecture Overview

### Request Lifecycle

```
  HTTP Client
      │
      ▼
┌─────────────────────────────────────────────────────────────────┐
│  Spring Security Filter Chain                                   │
│                                                                 │
│   RequestCorrelationFilter  ──► injects X-Request-Id into MDC  │
│           │                                                     │
│   JwtAuthenticationFilter  ──► validates Bearer token          │
│           │                                                     │
│   CommonsRequestLoggingFilter ──► logs request (dev only)      │
└───────────────────┬─────────────────────────────────────────────┘
                    │
                    ▼
          ┌─────────────────┐
          │   Controller    │  @RestController  /api/v1/{resource}
          │                 │  @PreAuthorize — method-level RBAC
          └────────┬────────┘
                   │  DTO (Request)
                   ▼
          ┌─────────────────┐
          │    Service      │  interface  ◄── injected by type
          │   (interface)   │
          └────────┬────────┘
                   │  entity / domain objects
                   ▼
          ┌─────────────────┐
          │  ServiceImpl    │  @Service @Transactional
          │                 │  business logic, validation,
          │                 │  cache management
          └──────┬──────────┘
                 │
        ┌────────┴──────────┐
        │                   │
        ▼                   ▼
┌──────────────┐   ┌────────────────┐
│  Repository  │   │  Other Service │  (cross-service calls allowed)
│ (JPA/Spring) │   │   (interface)  │
└──────┬───────┘   └────────────────┘
       │
       ▼
┌──────────────┐
│    MySQL     │  via HikariCP connection pool
│  (Flyway)    │  schema managed by versioned migration scripts
└──────────────┘

  Async / Scheduled paths:
  ┌─────────────────────────────────────────────┐
  │  scheduler/ReservationExpiryScheduler       │
  │       │                                     │
  │       ├── @SchedulerLock (Redis/ShedLock)   │
  │       └── Repository (direct — schedulers   │
  │           are allowed to use repositories)  │
  └─────────────────────────────────────────────┘

  Caching layer (Redis):
  ┌──────────────────────────────────────────────┐
  │  @Cacheable / @CacheEvict on ServiceImpl     │
  │  CacheInvalidationService for manual evict   │
  │  RedisConfig — named TTL regions             │
  └──────────────────────────────────────────────┘
```

### Technology Stack

```
┌─────────────────────────────────────────────────────────┐
│               WheelShift Pro — Stack                    │
├─────────────────┬───────────────────────────────────────┤
│ Runtime         │ Java 17, Spring Boot 4.x               │
│ Web             │ Spring MVC (servlet stack)             │
│ Security        │ Spring Security 6, JWT (JJWT 0.12)    │
│ Persistence     │ Spring Data JPA, Hibernate, MySQL 8    │
│ Schema          │ Flyway (versioned migrations)          │
│ Caching         │ Redis 7 (Spring Cache abstraction)     │
│ Mapping         │ MapStruct 1.5 + Lombok                 │
│ Scheduling      │ @Scheduled + ShedLock (Redis lock)     │
│ Observability   │ Spring Actuator, Micrometer/Prometheus │
│ Docs            │ SpringDoc OpenAPI 3 / Swagger UI       │
│ Validation      │ Jakarta Bean Validation (JSR-380)      │
│ Build           │ Maven, JaCoCo, OWASP dependency-check  │
│ Containers      │ Docker, Docker Compose                 │
└─────────────────┴───────────────────────────────────────┘
```

---

## 2. Project Structure

```
wheelshift-backend/
├── Dockerfile
├── docker-compose.yml           ← production compose
├── docker-compose-dev.yml       ← development compose (DB + Redis)
├── .env                         ← local secrets (gitignored — never commit)
├── pom.xml
│
├── src/
│   ├── main/
│   │   ├── java/com/wheelshiftpro/
│   │   │   │
│   │   │   ├── WheelShiftProApplication.java
│   │   │   │
│   │   │   ├── config/              ← Spring @Configuration classes
│   │   │   │   ├── AsyncConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── HttpLoggingConfig.java
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   ├── JpaAuditingConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── ShedLockConfig.java
│   │   │   │
│   │   │   ├── controller/          ← REST controllers (@RestController)
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CarController.java
│   │   │   │   ├── CarInspectionController.java
│   │   │   │   ├── CarModelController.java
│   │   │   │   ├── ClientController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── EmployeeController.java
│   │   │   │   ├── EventController.java
│   │   │   │   ├── FileStorageController.java
│   │   │   │   ├── FinancialTransactionController.java
│   │   │   │   ├── InquiryController.java
│   │   │   │   ├── MotorcycleController.java
│   │   │   │   ├── MotorcycleInspectionController.java
│   │   │   │   ├── MotorcycleModelController.java
│   │   │   │   ├── ReservationController.java
│   │   │   │   ├── SaleController.java
│   │   │   │   ├── StorageLocationController.java
│   │   │   │   ├── TaskController.java
│   │   │   │   ├── notifications/
│   │   │   │   │   ├── NotificationController.java
│   │   │   │   │   ├── NotificationPreferenceController.java
│   │   │   │   │   └── NotificationTemplateController.java
│   │   │   │   └── rbac/
│   │   │   │       ├── DataScopeController.java
│   │   │   │       ├── EmployeePermissionController.java
│   │   │   │       ├── EmployeeRoleController.java
│   │   │   │       ├── PermissionController.java
│   │   │   │       ├── ResourceACLController.java
│   │   │   │       └── RoleController.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/         ← inbound payloads (validated)
│   │   │   │   │   ├── notifications/
│   │   │   │   │   └── rbac/
│   │   │   │   └── response/        ← outbound payloads (never expose entities)
│   │   │   │       ├── dashboard/
│   │   │   │       ├── notifications/
│   │   │   │       └── rbac/
│   │   │   │
│   │   │   ├── entity/              ← @Entity JPA classes (extend BaseEntity)
│   │   │   │   ├── BaseEntity.java  ← createdAt / updatedAt via JPA auditing
│   │   │   │   ├── notifications/
│   │   │   │   └── rbac/
│   │   │   │
│   │   │   ├── enums/               ← all Java enums
│   │   │   │   ├── notifications/
│   │   │   │   └── rbac/
│   │   │   │
│   │   │   ├── exception/           ← custom exceptions + GlobalExceptionHandler
│   │   │   │
│   │   │   ├── mapper/              ← MapStruct interfaces (never write impl)
│   │   │   │   └── rbac/
│   │   │   │
│   │   │   ├── repository/          ← Spring Data JPA interfaces only
│   │   │   │   ├── notifications/
│   │   │   │   └── rbac/
│   │   │   │
│   │   │   ├── scheduler/           ← @Scheduled jobs (always use @SchedulerLock)
│   │   │   │
│   │   │   ├── security/            ← JWT, UserDetails, filters
│   │   │   │
│   │   │   ├── service/             ← interfaces only (one per domain)
│   │   │   │   ├── impl/            ← @Service implementations
│   │   │   │   │   ├── notifications/
│   │   │   │   │   └── rbac/
│   │   │   │   ├── notifications/
│   │   │   │   └── rbac/
│   │   │   │
│   │   │   ├── utils/               ← stateless helper utilities
│   │   │   └── validation/          ← custom @Constraint annotations + validators
│   │   │
│   │   └── resources/
│   │       ├── application.properties          ← shared base (no secrets)
│   │       ├── application-dev.properties      ← dev overrides
│   │       ├── application-prod.properties     ← prod overrides (all env vars)
│   │       ├── application-test.properties     ← test profile
│   │       ├── logback-spring.xml              ← logging config
│   │       ├── db/
│   │       │   ├── migration/   ← V{n}__{Description}.sql  (Flyway-managed)
│   │       │   └── rollbacks/   ← rollback_{Vn}.sql        (manual, documented)
│   │       ├── META-INF/
│   │       │   └── additional-spring-configuration-metadata.json
│   │       └── templates/error/   ← Thymeleaf error pages
│   │
│   └── test/java/com/wheelshiftpro/
│       ├── ArchitectureRulesTest.java   ← ArchUnit layer rules (run on every build)
│       └── WheelShiftProApplicationTests.java
│
└── docs/                        ← architecture and feature documentation
```

---

## 3. Layered Architecture Rules

These rules are **enforced automatically** by `ArchitectureRulesTest` on every `mvn install`.
A violation is a **build failure** — fix the code, never suppress the test.

```
┌──────────────────────────────────────────────────────────┐
│                      ALLOWED DEPENDENCIES                │
│                                                          │
│  Controller ──► Service (interface only)                 │
│  Controller ──► DTO                                      │
│  Controller ──► Exception                                │
│                                                          │
│  Service ──► Repository                                  │
│  Service ──► Entity                                      │
│  Service ──► DTO                                         │
│  Service ──► another Service (interface only)            │
│                                                          │
│  Mapper ──► Entity                                       │
│  Mapper ──► DTO                                          │
│                                                          │
│  Scheduler ──► Repository  (schedulers are exempt from   │
│  Scheduler ──► Service      the controller→service rule) │
│                                                          │
├──────────────────────────────────────────────────────────┤
│                      FORBIDDEN                           │
│                                                          │
│  Controller ──✗── Repository  (NEVER bypass the service) │
│  Controller ──✗── Entity      (always map to DTO first)  │
│  Service    ──✗── Controller                             │
│  Service    ──✗── jakarta.servlet.*                      │
└──────────────────────────────────────────────────────────┘
```

**Naming conventions (also enforced):**
- All `@RestController` classes → must end with `Controller`
- All classes in `service/impl` → must end with `Impl`

---

## 4. Domain Map

```
┌─────────────────────────────────────────────────────────────────────┐
│                         DOMAIN OVERVIEW                             │
│                                                                     │
│  ┌──────────┐   inspected by   ┌──────────────┐                    │
│  │   Car    │──────────────────│CarInspection │                    │
│  │          │   has model      └──────────────┘                    │
│  │          │──────────────────┌──────────────┐                    │
│  │          │   stored at      │   CarModel   │                    │
│  └──────────┘──────────────────└──────────────┘                    │
│       │  sold via                                                   │
│       │           ┌──────────┐  handled by  ┌──────────┐           │
│       └──────────►│   Sale   │─────────────►│ Employee │           │
│                   └──────────┘              └──────────┘           │
│  ┌────────────┐       │ bought by                │                 │
│  │Motorcycle  │       ▼                          │ assigned to     │
│  │ (mirrors   │   ┌──────────┐                   ▼                 │
│  │  Car dom.) │   │  Client  │              ┌──────────┐           │
│  └────────────┘   └──────────┘              │   Task   │           │
│                                             └──────────┘           │
│  ┌─────────────────┐   ┌──────────────────┐  ┌────────────────┐   │
│  │ StorageLocation │   │   Reservation    │  │    Inquiry     │   │
│  └─────────────────┘   └──────────────────┘  └────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  RBAC subsystem                                             │   │
│  │  Role ──► Permission ──► Employee                           │   │
│  │  ResourceACL  EmployeePermission  EmployeeDataScope         │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Notifications subsystem                                    │   │
│  │  NotificationTemplate ──► NotificationJob                   │   │
│  │  NotificationEvent ──► NotificationDelivery                 │   │
│  │  NotificationPreference (per-employee channel settings)     │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### API Endpoint Map

```
/api/v1/
├── auth/                    AuthController
│   ├── POST /login
│   ├── GET  /me
│   ├── GET  /validate-session
│   └── POST /logout
│
├── cars/                    CarController
├── car-models/              CarModelController
├── car-inspections/         CarInspectionController
├── motorcycles/             MotorcycleController
├── motorcycle-models/       MotorcycleModelController
├── motorcycle-inspections/  MotorcycleInspectionController
├── clients/                 ClientController
├── employees/               EmployeeController
├── sales/                   SaleController
├── reservations/            ReservationController
├── financial-transactions/  FinancialTransactionController
├── storage-locations/       StorageLocationController
├── tasks/                   TaskController
├── events/                  EventController
├── inquiries/               InquiryController
├── files/                   FileStorageController
│
├── dashboard/               DashboardController
│   ├── GET /admin
│   ├── GET /sales
│   ├── GET /inspector
│   ├── GET /finance
│   └── GET /store-manager
│
├── notifications/
│   ├── NotificationController           /api/v1/notifications
│   ├── NotificationPreferenceController /api/v1/notifications/preferences
│   └── NotificationTemplateController   /api/v1/notifications/templates
│
└── rbac/
    ├── RoleController               /api/v1/rbac/roles
    ├── PermissionController         /api/v1/rbac/permissions
    ├── EmployeeRoleController       /api/v1/rbac/employees/{id}/roles
    ├── EmployeePermissionController /api/v1/rbac/employee-permissions
    ├── ResourceACLController        /api/v1/rbac/acl
    └── DataScopeController          /api/v1/rbac/employees/{id}/scope
```

---

## 5. Adding a New Feature — Step-by-Step

The checklist below uses a hypothetical **`ServiceRecord`** feature as the running example.

### Step 1 — Database Migration

Create the next versioned script in `src/main/resources/db/migration/`:

```sql
-- File: V17__Add_Service_Records_Table.sql
CREATE TABLE service_records (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    car_id         BIGINT       NOT NULL,
    serviced_by    BIGINT       NOT NULL,
    service_date   DATE         NOT NULL,
    description    TEXT,
    cost           DECIMAL(12,2),
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)           DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_sr_car      FOREIGN KEY (car_id)       REFERENCES cars(id),
    CONSTRAINT fk_sr_employee FOREIGN KEY (serviced_by)  REFERENCES employees(id)
);
```

Rules:
- File name: `V{next_number}__{Description_In_Title_Case}.sql` (double underscore)
- **Never modify** an existing migration — create a new one instead
- Always include `created_at` / `updated_at` columns if the entity will extend `BaseEntity`
- Add a matching rollback in `db/rollbacks/rollback_V17.sql`

### Step 2 — Entity

```
src/main/java/com/wheelshiftpro/entity/ServiceRecord.java
```

```java
@Entity
@Table(name = "service_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceRecord extends BaseEntity {      // ← always extend BaseEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviced_by", nullable = false)
    private Employee servicedBy;

    @NotNull
    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;
}
```

Rules:
- **Always** extend `BaseEntity` — provides `createdAt`/`updatedAt` via JPA auditing
- Use `FetchType.LAZY` on all `@ManyToOne` / `@OneToMany` by default — avoids N+1
- Never put business logic in entities; use service layer

### Step 3 — Enum (if needed)

```
src/main/java/com/wheelshiftpro/enums/ServiceType.java
```

Place general enums in `enums/`, domain-specific sub-system enums in `enums/notifications/` or `enums/rbac/`.

### Step 4 — Repository

```
src/main/java/com/wheelshiftpro/repository/ServiceRecordRepository.java
```

```java
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    Page<ServiceRecord> findByCarId(Long carId, Pageable pageable);

    @Query("SELECT s FROM ServiceRecord s WHERE s.car.id = :carId ORDER BY s.serviceDate DESC")
    List<ServiceRecord> findLatestByCarId(@Param("carId") Long carId, Pageable pageable);
}
```

Rules:
- Extend `JpaRepository<Entity, ID>` — that's it, no `@Repository` annotation needed
- Prefer `@Query` JPQL over native SQL unless performance requires it
- Return `Page<T>` for list endpoints, `Optional<T>` for single lookups

### Step 5 — DTOs

**Request:** `src/main/java/com/wheelshiftpro/dto/request/ServiceRecordRequest.java`

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceRecordRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    @Size(max = 2000)
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal cost;
}
```

**Response:** `src/main/java/com/wheelshiftpro/dto/response/ServiceRecordResponse.java`

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceRecordResponse {
    private Long id;
    private Long carId;
    private String carRegistration;
    private LocalDate serviceDate;
    private String description;
    private BigDecimal cost;
    private LocalDateTime createdAt;
}
```

Rules:
- Keep requests and responses in separate classes — they often diverge over time
- Never return entity classes directly from controllers
- Sub-system DTOs go in their respective sub-package (e.g., `dto/request/notifications/`)

### Step 6 — Mapper

```
src/main/java/com/wheelshiftpro/mapper/ServiceRecordMapper.java
```

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceRecordMapper {

    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.registrationNumber", target = "carRegistration")
    ServiceRecordResponse toResponse(ServiceRecord entity);

    List<ServiceRecordResponse> toResponseList(List<ServiceRecord> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)    // set in service after lookup
    @Mapping(target = "servicedBy", ignore = true)
    ServiceRecord toEntity(ServiceRecordRequest request);
}
```

Rules:
- Never write implementation classes — MapStruct generates them at compile time
- Use `@Mapping(target = "...", ignore = true)` for fields set by the service (IDs, relations)
- `componentModel = "spring"` is mandatory — it makes the mapper a Spring bean

### Step 7 — Service Interface

```
src/main/java/com/wheelshiftpro/service/ServiceRecordService.java
```

```java
public interface ServiceRecordService {
    ServiceRecordResponse createServiceRecord(ServiceRecordRequest request);
    ServiceRecordResponse getServiceRecordById(Long id);
    PageResponse<ServiceRecordResponse> getServiceRecordsByCarId(Long carId, int page, int size);
    ServiceRecordResponse updateServiceRecord(Long id, ServiceRecordRequest request);
    void deleteServiceRecord(Long id);
}
```

### Step 8 — Service Implementation

```
src/main/java/com/wheelshiftpro/service/impl/ServiceRecordServiceImpl.java
```

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceRecordServiceImpl implements ServiceRecordService {

    private final ServiceRecordRepository repository;
    private final CarRepository carRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRecordMapper mapper;

    @Override
    public ServiceRecordResponse createServiceRecord(ServiceRecordRequest request) {
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", request.getCarId()));

        ServiceRecord record = mapper.toEntity(request);
        record.setCar(car);
        ServiceRecord saved = repository.save(record);

        log.info("Created service record id={} for car id={}", saved.getId(), car.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRecordResponse getServiceRecordById(Long id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRecord", "id", id)));
    }

    // ... remaining methods
}
```

Rules:
- Class name **must** end with `Impl` (enforced by ArchUnit)
- Use `@Transactional(readOnly = true)` on read methods — improves DB performance
- Always log creates/updates at `INFO`, reads at `DEBUG`
- Throw typed exceptions from `exception/` (`ResourceNotFoundException`, `BusinessException`, etc.)

### Step 9 — Controller

```
src/main/java/com/wheelshiftpro/controller/ServiceRecordController.java
```

```java
@RestController
@RequestMapping("/api/v1/service-records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Records", description = "Vehicle service history management")
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;  // ← inject interface, not impl

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    @Operation(summary = "Create service record")
    public ResponseEntity<ServiceRecordResponse> create(
            @Valid @RequestBody ServiceRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceRecordService.createServiceRecord(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service record by ID")
    public ResponseEntity<ServiceRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceRecordService.getServiceRecordById(id));
    }

    @GetMapping("/car/{carId}")
    @Operation(summary = "Get all service records for a car")
    public ResponseEntity<PageResponse<ServiceRecordResponse>> getByCarId(
            @PathVariable Long carId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(serviceRecordService.getServiceRecordsByCarId(carId, page, size));
    }
}
```

Rules:
- Class name **must** end with `Controller` (enforced by ArchUnit)
- Always inject the **interface**, never the `Impl` class
- Use `@Valid` on all `@RequestBody` parameters
- Use `@PreAuthorize` for method-level access control — see [roles below](#7-security--rbac)
- Return `ResponseEntity` with the correct HTTP status (201 for create, 200 for read, 204 for delete)
- Add `@Tag` and `@Operation` for Swagger documentation

### Full Checklist Summary

```
New feature checklist:
  [ ] V{n}__Description.sql    in db/migration/
  [ ] rollback_V{n}.sql        in db/rollbacks/
  [ ] {Domain}Entity.java      in entity/          (extends BaseEntity)
  [ ] {Domain}Enum.java        in enums/            (if needed)
  [ ] {Domain}Repository.java  in repository/
  [ ] {Domain}Request.java     in dto/request/
  [ ] {Domain}Response.java    in dto/response/
  [ ] {Domain}Mapper.java      in mapper/
  [ ] {Domain}Service.java     in service/          (interface)
  [ ] {Domain}ServiceImpl.java in service/impl/     (ends with Impl)
  [ ] {Domain}Controller.java  in controller/       (ends with Controller)
```

---

## 6. Database Migrations

### Naming Convention

```
V{version}__{TitleCase_Description}.sql
└─┬──────┘└──┬────────────────────┘
  │          └── Words separated by single underscore, Title Case
  └── Sequential integer, never reuse or skip
```

Examples:
```
V1__Initial_Schema.sql
V4__Add_RBAC_Tables.sql
V17__Add_Service_Records_Table.sql   ← next one
```

### Rules

1. **Never modify** a committed migration. Flyway checksums every file — a change causes startup failure.
2. Create a **new migration** for any schema change (add a column → new `ALTER TABLE` script).
3. Migrations run **automatically** on application startup in all environments.
4. Write a matching `db/rollbacks/rollback_V{n}.sql` for destructive migrations so rollback steps are documented.
5. Test migrations locally by starting the app with Docker Compose — the DB is fresh on first run.

### Seed Data

Place initial reference/seed data in a dedicated migration:
```
V2__Seed_Data.sql        ← existing roles, permissions, default configs
V5__Seed_RBAC_Data.sql   ← RBAC seed data
```

Keep DDL (schema) and DML (seed) in separate files — easier to review and roll back.

### Column Naming

| Java field | DB column |
|------------|-----------|
| `registrationNumber` | `registration_number` |
| `createdAt` | `created_at` |
| `fuelType` (enum) | `fuel_type VARCHAR(20)` |
| Boolean | `TINYINT(1)` or `BIT(1)` |
| Money | `DECIMAL(12, 2)` |
| Text > 255 chars | `TEXT` |

---

## 7. Security & RBAC

### Available Roles

```java
// RoleType enum
SUPER_ADMIN    // full system access
ADMIN          // store administration
SALES          // sales operations
STORE_MANAGER  // inventory / storage management
INSPECTOR      // vehicle inspection
FINANCE        // financial reporting
```

### Using `@PreAuthorize` on Controllers

```java
// Single role
@PreAuthorize("hasRole('ADMIN')")

// Multiple roles (OR)
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

// Permission-based (more granular than roles)
@PreAuthorize("hasAuthority('car:write')")

// Combined
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER') or hasAuthority('car:write')")
```

### Public Endpoints

Add public endpoints to the `SecurityConfig` permit list:

```java
// In SecurityConfig.java — requestMatchers section
.requestMatchers("/api/v1/auth/**").permitAll()
.requestMatchers("/api/v1/your-new-public-path/**").permitAll()
```

### JWT Claims

The JWT token contains the employee's email as the subject. Use `Authentication.getName()` in controllers to get the email, then call `employeeService.findByEmail(email)` to resolve the employee.

---

## 8. Running Locally

### Prerequisites

- Java 17
- Docker + Docker Compose
- Maven 3.9+ (or use `./mvnw`)

### Environment Variables

Copy the `.env` file (already created, gitignored) — it contains all dev defaults:

```bash
# View the variables (they already have dev defaults)
cat .env
```

### Start Infrastructure

```bash
# Start MySQL 8 and Redis 7 in the background
docker-compose -f docker-compose-dev.yml up -d
```

### Start the Application

**IntelliJ IDEA:**
1. Install the *EnvFile* plugin
2. Edit Run Configuration → *EnvFile* tab → enable → add `.env`
3. Run `WheelShiftProApplication`

**VS Code:**
The Spring Boot Dashboard picks up `.env` automatically when the extension is configured.

**Terminal:**
```bash
# Export .env vars then run
export $(cat .env | grep -v '^#' | xargs)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Verify

- API: http://localhost:8080/api/v1/auth/validate-session
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/health

---

## 9. Testing

### Running Tests

```bash
# All tests + coverage report + architecture rules
mvn clean install

# Skip tests (faster feedback during development)
mvn install -DskipTests

# Run a specific test class
mvn test -Dtest=ArchitectureRulesTest

# Coverage report (opens in browser)
open target/site/jacoco/index.html
```

### Test Profiles

The `test` profile (`application-test.properties`) is activated automatically by Spring Boot tests.
It connects to a real database — use Testcontainers (already in `pom.xml`) for isolated DB tests:

```java
@SpringBootTest
@Testcontainers
class ServiceRecordServiceImplTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("wheelshift_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```

### Architecture Rules (ArchUnit)

`ArchitectureRulesTest` runs on every build and enforces:
- Controllers never access Repositories directly
- Services never depend on Controllers
- Services never import `jakarta.servlet.*`
- All `@RestController` classes end with `Controller`
- All classes in `service/impl` end with `Impl`

**A failing ArchUnit test = build failure. Fix the architecture, do not skip the test.**

---

## 10. Code Conventions

### General

| Rule | Detail |
|------|--------|
| Use Lombok | `@Getter @Setter @Builder @RequiredArgsConstructor @Slf4j` |
| No `System.out.println` | Use `log.info/debug/warn/error` |
| No `@Autowired` fields | Use constructor injection (Lombok `@RequiredArgsConstructor`) |
| Validate at the boundary | `@Valid` on controller request bodies, nothing deeper |
| Secrets → env vars | Never hardcode passwords, keys, or tokens in any file |

### Logging Levels

```
log.error  ← unexpected exceptions, data integrity issues
log.warn   ← business rule violations, auth failures, retries
log.info   ← creates, updates, deletes, scheduled job starts
log.debug  ← reads, cache hits/misses, detailed flow (dev only)
log.trace  ← SQL, HTTP payloads (never in prod)
```

### Exception Handling

Throw from `exception/` package — `GlobalExceptionHandler` maps these to HTTP responses:

| Exception | HTTP Status |
|-----------|-------------|
| `ResourceNotFoundException` | 404 Not Found |
| `DuplicateResourceException` | 409 Conflict |
| `BusinessException` | 422 Unprocessable Entity |
| `InsufficientPermissionException` | 403 Forbidden |
| `SessionExpiredException` | 401 Unauthorized |
| `IllegalArgumentException` | 400 Bad Request |
| `IllegalStateException` | 409 Conflict |

### Caching

Use Spring Cache annotations on `ServiceImpl` methods (never on controllers):

```java
@Cacheable(value = "carModels", key = "#id")
public CarModelResponse getCarModelById(Long id) { ... }

@CacheEvict(value = "carModels", key = "#id")
public CarModelResponse updateCarModel(Long id, CarModelRequest request) { ... }
```

Cache regions and TTLs are configured in `RedisConfig.java`.

### Adding a Scheduled Job

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MyScheduledJob {

    @Scheduled(fixedDelayString = "${my.job.interval-ms:60000}")
    @SchedulerLock(name = "MyScheduledJob", lockAtMostFor = "9m", lockAtLeastFor = "30s")
    @Transactional
    public void run() {
        log.info("MyScheduledJob started");
        // ...
    }
}
```

- Place in `scheduler/` package
- Always add `@SchedulerLock` — prevents duplicate execution in multi-node deployments
- `lockAtMostFor` should be less than your `fixedDelay` to prevent lock starvation

### Sub-system Packages

Features that span multiple layers (controller + service + entity + dto) and belong to a bounded sub-system get their own sub-package at each layer:

```
controller/notifications/
service/notifications/          ← interfaces
service/impl/notifications/     ← implementations
entity/notifications/
dto/request/notifications/
dto/response/notifications/
repository/notifications/
enums/notifications/
```

Follow the same pattern for any new bounded sub-system (e.g., `controller/reports/`).

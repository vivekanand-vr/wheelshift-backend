# WheelShift Pro – Copilot Agent Instructions

These instructions apply to **all code** in this repository.
Always read [`CONTRIBUTING.md`](../CONTRIBUTING.md) for the full architecture overview,
project structure, layered architecture rules, feature-addition workflow, migration conventions,
security/RBAC guidance, testing strategy, and code conventions before implementing anything.

---

## 1. Project Context

WheelShift Pro is a **Java 17 / Spring Boot 4.x** vehicle dealership management backend.  
Key stack: MySQL 8+ · JPA/Hibernate · Flyway · Spring Security + JWT · Redis · MapStruct · Lombok.

Package root: `com.wheelshiftpro`

> **Full architecture, folder layout, domain map, API endpoint map, and step-by-step feature guide**
> are documented in [`CONTRIBUTING.md`](../CONTRIBUTING.md). Always consult it when adding a
> new feature, migration, or scheduled job.

---

## 2. Layered Architecture (Non-Negotiable)

Dependency direction is enforced by `ArchitectureRulesTest` on every build — violations are
**build failures**. The layer contract from `CONTRIBUTING.md §3` in brief:

```
Controller → Service (interface) → Repository → DB
Controller → DTO only (never expose entities)
Service    → may call another Service (interface only)
Scheduler  → Repository / Service directly (exempt from controller rule)
```

- Class names: all `@RestController` → end with `Controller`; all `service/impl` classes → end with `Impl`.
- Inject **interfaces**, never `Impl` classes.
- No `@Autowired` on fields — use constructor injection via Lombok `@RequiredArgsConstructor`.

---

## 3. Validation

- Bean validation (`@NotBlank`, `@Size`, `@NotNull`, etc.) lives on **DTO fields**, not service methods.
- Use `@Validated(OnCreate.class)` in controllers for create-only constraints.
- Domain-specific constraints (VIN = exactly 17 chars, unique registration, capacity, status
  transitions) belong in the **service layer** as explicit guard clauses with typed exceptions.

---

## 4. Error Handling

| Exception | HTTP | When |
|-----------|------|------|
| `ResourceNotFoundException` | 404 | Entity not found by ID/field |
| `DuplicateResourceException` | 409 | Uniqueness constraint violated |
| `BusinessException` | 422 | Business rule violated — always pass a machine-readable error code |

```java
throw new ResourceNotFoundException("Car", "id", id);
throw new DuplicateResourceException("Car", "VIN", vin);
throw new BusinessException("Human readable message", "MACHINE_ERROR_CODE");
```

- Never swallow exceptions with try/catch returning a default value.
- Let all exceptions propagate to `GlobalExceptionHandler`.

---

## 5. Transactional / Saga Pattern

Every write operation must be all-or-nothing:

```java
@Transactional(rollbackFor = Exception.class)  // ALL write methods
@Transactional(readOnly = true)                // ALL read methods
```

- `rollbackFor = Exception.class` ensures checked exceptions trigger rollback.
- Never call a `@Transactional` method from within the same class — self-invocation bypasses the proxy.
- Order within every write transaction:
  1. Validate all business rules (throw before touching the DB)
  2. Adjust related counters / linked records
  3. Save audit/movement records
  4. Persist the main entity
  5. Return the mapped response DTO

---

## 6. Role-Based Access Control (RBAC)

Every controller endpoint **must** have `@PreAuthorize`. See `CONTRIBUTING.md §7` for the full
role list (`SUPER_ADMIN`, `ADMIN`, `SALES`, `STORE_MANAGER`, `INSPECTOR`, `FINANCE`).

Standard patterns:

```java
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STORE_MANAGER')")  // create / update / move
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")                 // delete / financial reports
@PreAuthorize("isAuthenticated()")                                  // any authenticated read
```

- Never use `hasRole('ADMIN')` alone — always include `SUPER_ADMIN`.
- `@PreAuthorize` goes on the **controller method**, not the service.

---

## 7. MapStruct Mapper Conventions

- `toEntity(Request)` — creates a new entity; relationships (`carModel`, `storageLocation`, etc.)
  are **ignored** and must be set explicitly in the service after lookup.
- `updateEntityFromRequest(Request, @MappingTarget Entity)` — updates scalar fields only; never
  touches relationships.
- Always set relationships manually after mapping:

```java
Car car = carMapper.toEntity(request);
car.setCarModel(carModelRepository.findById(request.getCarModelId())…);
car.setStorageLocation(storageLocationRepository.findById(request.getStorageLocationId())…);
```

---

## 8. Repository Conventions

Use Spring Data JPA derived-query naming — do NOT write JPQL for simple existence checks:

| Need | Method signature |
|------|-----------------|
| Uniqueness on create | `existsByVinNumber(String vin)` |
| Uniqueness on update (exclude self) | `existsByVinNumberAndIdNot(String vin, Long id)` |
| Guard before delete | `existsByCarId(Long carId)` |

---

## 9. Unit Test Conventions

Tooling: JUnit 5 · Mockito (`@ExtendWith(MockitoExtension.class)`) · AssertJ.

```java
@Nested class CreateFoo {
    @Test void happyPath() { … }
    @Test void duplicateKey_throws() { … }
    @Test void notFound_throws() { … }
    @Test void capacityFull_throws() { … }
}
```

Every write service method needs coverage for:
1. Happy path — side effects verified (counts, records saved, return value)
2. Uniqueness violation → `DuplicateResourceException`
3. Not-found → `ResourceNotFoundException`
4. Capacity/guard violation → `BusinessException`
5. Forbidden status transition → `BusinessException`
6. Financial-transaction guard on delete → `BusinessException` containing "financial transactions"
7. `movedBy` populated when authenticated — `ArgumentCaptor` verifies the field is non-null
8. `movedBy` null when no authenticated user — clear `SecurityContextHolder`, assert null

Security context helper (paste into test class):

```java
private void setUpAuthenticatedEmployee(Long employeeId) {
    Employee employee = new Employee(); employee.setId(employeeId);
    EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
    when(principal.getId()).thenReturn(employeeId);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    when(employeeRepository.getReferenceById(employeeId)).thenReturn(employee);
}

@AfterEach void clearSecurityContext() { SecurityContextHolder.clearContext(); }
```

---

## 10. Pre-Submit Checklist

- [ ] Service write methods annotated `@Transactional(rollbackFor = Exception.class)`
- [ ] Service read methods annotated `@Transactional(readOnly = true)`
- [ ] Uniqueness checked on create AND on update (excluding self)
- [ ] Storage-location counts adjusted on create / move / delete
- [ ] Movement records created with `movedBy` set on every location change
- [ ] Financial-transaction guard checked before delete
- [ ] Status transition validated; SOLD not settable via status API
- [ ] Every controller endpoint has `@PreAuthorize` with correct roles
- [ ] Unit tests cover happy path + all guard/error cases per service method
- [ ] No dead or commented-out code in production files
- [ ] Migration file added for any schema change (see `CONTRIBUTING.md §6`)
- [ ] `ArchitectureRulesTest` passes locally (`mvn test -Dtest=ArchitectureRulesTest`)

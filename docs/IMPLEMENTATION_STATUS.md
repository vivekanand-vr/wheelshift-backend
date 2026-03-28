# WheelShift Pro — Implementation & Test Status

**Last Updated:** March 28, 2026  
**Spec Reference:** [`BUSINESS_LOGIC.md`](./BUSINESS_LOGIC.md)

Track progress for every business logic operation defined in the spec.  
The **WheelShift Developer** agent reads this file to know which areas need attention.

---

## Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Complete — all MUST rules implemented / all required test cases written |
| ⚠️ | Partial — some rules implemented or some test cases written |
| ❌ | Not started |
| N/A | Not applicable (auto-generated, read-only, or out of scope) |

---

## Vehicle Management — Cars

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 1.1 | Add a new car | `POST /api/v1/cars` | ✅ | VIN/reg uniqueness, capacity, model guard, location count, `@PreAuthorize` | ✅ | `CarServiceImplTest` — happy path, duplicate VIN, duplicate reg, model not found, location full, location not found |
| 1.2 | Update a car | `PUT /api/v1/cars/{id}` | ✅ | VIN/reg uniqueness on update, move routed through `applyCarMove`, `@PreAuthorize` | ✅ | Duplicate VIN on update, reg on update, not found |
| 1.3 | Delete a car | `DELETE /api/v1/cars/{id}` | ✅ | RESERVED/SOLD guard, financial-tx guard, location count decrement, `@PreAuthorize` | ✅ | All guards covered, happy path |
| 1.4 | Update car status | `PUT /api/v1/cars/{id}/status` | ✅ | Status-transition validation, SOLD blocked, `@PreAuthorize` | ✅ | Forbidden transitions, valid transitions |
| 1.5 | Move car to location | `POST /api/v1/cars/{id}/move` | ✅ | Capacity check, old/new count, `CarMovement` record, `movedBy` from security context, `@PreAuthorize` | ✅ | Happy path, location full, `movedBy` with/without auth |

---

## Vehicle Management — Motorcycles

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 2.1 | Add a new motorcycle | `POST /api/v1/motorcycles` | ✅ | VIN/reg uniqueness, capacity, model guard, location count, model+location set after mapper, `@PreAuthorize` | ✅ | Same coverage as BL 1.1 |
| 2.2 | Update a motorcycle | `PUT /api/v1/motorcycles/{id}` | ✅ | VIN/reg uniqueness on update, move routed through `applyMotorcycleMove`, `@PreAuthorize` | ✅ | |
| 2.3 | Delete a motorcycle | `DELETE /api/v1/motorcycles/{id}` | ✅ | RESERVED/SOLD guard, financial-tx guard, location count decrement, `@PreAuthorize` | ✅ | |
| 2.4 | Update motorcycle status | `PUT /api/v1/motorcycles/{id}/status` | ✅ | Status-transition validation, `@PreAuthorize` | ✅ | |
| 2.5 | Move motorcycle to location | `POST /api/v1/motorcycles/{id}/move` | ✅ | Capacity check, old/new count, `MotorcycleMovement` record, `movedBy`, `@PreAuthorize` | ✅ | |

---

## Vehicle Model Catalog

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 3.1 | Create car model | `POST /api/v1/car-models` | ✅ | make+model+variant uniqueness, `@Transactional(rollbackFor)`, audit `REGULAR`, `@PreAuthorize(SA,AD)` | ✅ | `CarModelServiceImplTest` — happy path, duplicate make+model+variant, audit level REGULAR |
| 3.1 | Update car model | `PUT /api/v1/car-models/{id}` | ✅ | uniqueness-on-update (`existsByMakeAndModelAndVariantAndIdNot`), not-found guard, audit `REGULAR`, `@PreAuthorize(SA,AD)` | ✅ | duplicate on update, not found |
| 3.1 | Create motorcycle model | `POST /api/v1/motorcycle-models` | ✅ | Same as car model | ✅ | `MotorcycleModelServiceImplTest` — same coverage |
| 3.1 | Update motorcycle model | `PUT /api/v1/motorcycle-models/{id}` | ✅ | Same as car model | ✅ | |
| 3.2 | Delete car model | `DELETE /api/v1/car-models/{id}` | ✅ | `MODEL_HAS_VEHICLES` guard (`getCars().isEmpty()`), audit `HIGH`, `@PreAuthorize(SA,AD)` | ✅ | model has cars, not found, audit level HIGH, `resolveCurrentEmployee` with/without auth |
| 3.2 | Delete motorcycle model | `DELETE /api/v1/motorcycle-models/{id}` | ✅ | `MODEL_HAS_VEHICLES` guard (`getMotorcycles().isEmpty()`), audit `HIGH`, `@PreAuthorize(SA,AD)` | ✅ | Same coverage |

---

## Vehicle Inspections

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 4.1 | Create inspection | `POST /api/v1/*-inspections` | ❌ | Future-date guard, vehicle status update, repair task creation not verified | ❌ | |
| 4.2 | Update inspection | `PUT /api/v1/*-inspections/{id}` | ❌ | Future-date block not verified | ❌ | |
| 4.3 | Delete inspection | `DELETE /api/v1/*-inspections/{id}` | ❌ | | ❌ | |

---

## Vehicle Movements

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 5.1 | Auto-created movement record | (internal — triggered by BL 1.5 / 2.5) | ✅ | `fromLocation` nullable, `movedAt` set to now, `movedBy` from security context | ✅ | Covered under BL 1.5 / 2.5 tests |

---

## Storage Location Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 6.1 | Create storage location | `POST /api/v1/storage-locations` | ❌ | Name uniqueness, initial counts not verified | ❌ | |
| 6.2 | Update storage location | `PUT /api/v1/storage-locations/{id}` | ❌ | Capacity-below-current-count block not verified | ❌ | |
| 6.3 | Delete storage location | `DELETE /api/v1/storage-locations/{id}` | ❌ | Has-vehicles guard not verified | ❌ | |
| 6.4 | Capacity threshold alerts (80% / 100%) | (automated side effect) | ❌ | Notification dispatch on count update not verified | ❌ | |

---

## Client Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 7.1 | Create client | `POST /api/v1/clients` | ❌ | Email uniqueness, default status/totalPurchases not verified | ❌ | |
| 7.2 | Update client | `PUT /api/v1/clients/{id}` | ❌ | Email uniqueness on update not verified | ❌ | |
| 7.3 | Delete client | `DELETE /api/v1/clients/{id}` | ❌ | Soft-delete preference, inquiry/reservation guard not verified | ❌ | |
| 7.4 | Increment purchase count (internal) | (triggered by BL 11.1) | ❌ | Not verified as a side effect of sale creation | ❌ | |

---

## Employee Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 8.1 | Create employee | `POST /api/v1/employees` | ❌ | Email uniqueness, BCrypt hash, default ACTIVE status not verified | ❌ | |
| 8.2 | Assign role | `POST /api/v1/employees/{id}/roles` | ❌ | Role existence check not verified | ❌ | |
| 8.3 | Update employee status | `PUT /api/v1/employees/{id}/status` | ❌ | SUPER_ADMIN guard, self-status guard not verified | ❌ | |
| 8.4 | Delete employee | `DELETE /api/v1/employees/{id}` | ❌ | Sale/task/last-SA guards not verified | ❌ | |

---

## Inquiry Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 9.1 | Create inquiry | `POST /api/v1/inquiries` | ❌ | Active-client check, single-vehicle discriminator, default OPEN not verified | ❌ | |
| 9.2 | Assign inquiry | `PUT /api/v1/inquiries/{id}/assign` | ❌ | | ❌ | |
| 9.3 | Update inquiry status | `PUT /api/v1/inquiries/{id}/status` | ❌ | Transition guard, RESPONDED requires response text not verified | ❌ | |
| 9.4 | Convert inquiry to reservation | `POST /api/v1/inquiries/{id}/reserve` | ❌ | Vehicle AVAILABLE check, deposit required not verified | ❌ | |
| 9.5 | Delete inquiry | `DELETE /api/v1/inquiries/{id}` | ❌ | CLOSED-with-sale guard not verified | ❌ | |

---

## Reservation Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 10.1 | Create reservation | `POST /api/v1/reservations` | ❌ | Vehicle AVAILABLE + unique-active-reservation, expiry future, vehicle status → RESERVED not verified | ❌ | |
| 10.2 | Cancel reservation | `POST /api/v1/reservations/{id}/cancel` | ❌ | EXPIRED/CANCELLED block, vehicle status revert not verified | ❌ | |
| 10.3 | Expire reservations (scheduler) | (automated) | ❌ | Status → EXPIRED, vehicle status revert not verified | ❌ | |
| 10.4 | Update deposit status | `PUT /api/v1/reservations/{id}/deposit` | ❌ | | ❌ | |
| 10.5 | Convert reservation to sale | `POST /api/v1/reservations/{id}/convert-to-sale` | ❌ | CONFIRMED status + depositPaid guard not verified | ❌ | |

---

## Sales Processing

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 11.1 | Create sale | `POST /api/v1/sales` | ❌ | Vehicle-not-SOLD guard, commission calc, vehicle → SOLD, client purchase count, reservation fulfillment, location count decrement not verified | ❌ | |
| 11.2 | Update sale | `PUT /api/v1/sales/{id}` | ❌ | Vehicle-ID immutability, commission re-calc not verified | ❌ | |
| 11.3 | Delete sale | `DELETE /api/v1/sales/{id}` (`SA` only) | ❌ | Vehicle status revert, client count decrement, settled-tx guard not verified | ❌ | |

---

## Financial Transactions

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 12.1 | Create financial transaction | `POST /api/v1/financial-transactions` | ❌ | Amount > 0, future-date block, auto-creation triggers not verified | ❌ | |
| 12.2 | Update financial transaction | `PUT /api/v1/financial-transactions/{id}` | ❌ | SALE-type edit block not verified | ❌ | |
| 12.3 | Delete financial transaction | `DELETE /api/v1/financial-transactions/{id}` (`SA` only) | ❌ | | ❌ | |

---

## Task Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 13.1 | Create task | `POST /api/v1/tasks` | ❌ | Assignee ACTIVE check, default TODO/MEDIUM, future dueDate not verified | ❌ | |
| 13.2 | Update task status | `PUT /api/v1/tasks/{id}/status` | ❌ | Transition rules, role restrictions per transition not verified | ❌ | |
| 13.3 | Overdue task detection (scheduler) | (automated) | ❌ | | ❌ | |
| 13.4 | Delete task | `DELETE /api/v1/tasks/{id}` | ❌ | DONE+linked-record guard not verified | ❌ | |

---

## Event Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 14.1 | Create event | `POST /api/v1/events` | ❌ | endTime > startTime, single-vehicle rule, TEST_DRIVE → RESERVED not verified | ❌ | |
| 14.2 | Event reminders (scheduler) | (automated) | ❌ | | ❌ | |

---

## File Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 15.1 | Upload file | `POST /api/v1/files` | ❌ | Type/size validation, UUID generation, access log not verified | ❌ | |
| 15.2 | Delete file | `DELETE /api/v1/files/{id}` | ❌ | Soft-delete, reference nullification not verified | ❌ | |
| 15.3 | Access file | `GET /api/v1/files/{id}` | ❌ | Access log entry not verified | ❌ | |

---

## RBAC Administration

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 16.1 | Create custom role | `POST /api/v1/rbac/roles` | ❌ | Name uniqueness, `isSystem = false` not verified | ❌ | |
| 16.2 | Delete role | `DELETE /api/v1/rbac/roles/{id}` | ❌ | System-role + assigned-to-employee guards not verified | ❌ | |
| 16.3 | Add/remove permission | `POST/DELETE /api/v1/rbac/roles/{id}/permissions` | ❌ | Session invalidation not verified | ❌ | |
| 16.4 | Create data scope | `POST /api/v1/rbac/data-scopes` | ❌ | | ❌ | |
| 16.5 | Grant/revoke resource ACL | `POST/DELETE /api/v1/rbac/acl` | ❌ | | ❌ | |

---

## Notification System

| BL Ref | Operation | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|:-----------:|------------|:-----------:|------------|
| 17.1 | Notification generation (triggered by events) | ❌ | Dispatched on BL 1.1, 2.1, 10.1, 11.1, etc. — not verified per trigger | ❌ | |
| 17.2 | Delivery rules (quiet hours, opt-out, digest) | ❌ | | ❌ | |
| 17.3 | Mark notification as read | `PUT /api/v1/notifications/{id}/read` | ❌ | Recipient-only guard not verified | ❌ | |

---

## Audit Logging (Cross-Cutting)

| Area | Impl Status | Impl Notes | Test Status | Test Notes |
|------|:-----------:|------------|:-----------:|------------|
| `audit_logs` table + Flyway migration (V22) | ✅ | `category`, `level`, `entity_id`, `action`, `performed_by_id`, `details`, `created_at` | N/A | |
| `AuditService` interface + `AuditServiceImpl` | ✅ | `log()` + `getAuditLogs()` with Specification filtering | ❌ | Unit tests not yet written |
| `AuditLogController` — `GET /api/v1/audit-logs` | ✅ | `@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")`, filterable by category/level/action/entityId/performedById | ❌ | |
| Audit calls in `CarServiceImpl` | ✅ | CREATE/UPDATE/MOVE → `REGULAR`; DELETE/STATUS_CHANGE → `HIGH` | ✅ | Covered in `CarServiceImplTest` |
| Audit calls in `MotorcycleServiceImpl` | ✅ | CREATE/UPDATE/MOVE → `REGULAR`; DELETE/STATUS_CHANGE → `HIGH` | ✅ | Covered in `MotorcycleServiceImplTest` |
| Audit calls in all other service impls | ❌ | Not yet added | ❌ | |

# WheelShift Pro — Implementation & Test Status

**Last Updated:** March 29, 2026 (Notification System BL 17.x — service fixed and tested)  
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
| 4.1 | Create inspection | `POST /api/v1/*-inspections` | ✅ | Future-date guard, vehicle/inspector existence, relationship wiring via `getReferenceById`, audit `REGULAR`, `@PreAuthorize(SA,AD,IN,SM)` | ✅ | happy path, car/motorcycle not found, inspector not found, future date, audit level, auth/no-auth audit field |
| 4.2 | Update inspection | `PUT /api/v1/*-inspections/{id}` | ✅ | Future-date block on non-null date, inspector re-wired on update, audit `REGULAR`, `@PreAuthorize(SA,AD,IN)` | ✅ | happy path, not found, future date, null date (no throw), inspector not found on update, inspector wired |
| 4.3 | Delete inspection | `DELETE /api/v1/*-inspections/{id}` | ✅ | Existence check, audit `HIGH`, `@PreAuthorize(SA,AD)` | ✅ | happy path, not found, audit HIGH |

---

## Vehicle Movements

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 5.1 | Auto-created movement record | (internal — triggered by BL 1.5 / 2.5) | ✅ | `fromLocation` nullable, `movedAt` set to now, `movedBy` from security context | ✅ | Covered under BL 1.5 / 2.5 tests |

---

## Storage Location Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 6.1 | Create storage location | `POST /api/v1/storage-locations` | ✅ | Name uniqueness, audit `REGULAR`, `@PreAuthorize(SA,AD)` | ✅ | happy path, duplicate name, audit level, auth/no-auth audit field |
| 6.2 | Update storage location | `PUT /api/v1/storage-locations/{id}` | ✅ | Name uniqueness-on-update (`existsByNameAndIdNot`), capacity-below-current-count block, audit `REGULAR`, `@PreAuthorize(SA,AD,SM)` | ✅ | happy path, not found, duplicate name on update, capacity below current |
| 6.3 | Delete storage location | `DELETE /api/v1/storage-locations/{id}` | ✅ | Has-vehicles guard, audit `HIGH`, `@PreAuthorize(SA,AD)` | ✅ | happy path, not found, location has vehicles, audit HIGH |
| 6.4 | Capacity threshold alerts (80% / 100%) | (automated side effect) | ✅ | `StorageLocationCapacityScheduler` — hourly scan; locations ≥80% utilisation trigger `LOCATION_NEAR_CAPACITY` to all SM, AD, SA employees; `isFull=true` payload key at 100%; deduplicates recipients across roles; ShedLock (55 m / 5 m) | ✅ | `StorageLocationCapacitySchedulerTest` — noLocations_noNotifications, belowThreshold_noNotifications, atEightyPercent_notifiesManagement, atFullCapacity_payloadContainsIsFull, noManagementEmployees_noNotifications, deduplicatedRecipient_notifiedOnce |

---

## Client Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 7.1 | Create client | `POST /api/v1/clients` | ✅ | Email uniqueness, audit `REGULAR`, `@PreAuthorize(SA,AD,SALES)` | ✅ | `ClientServiceImplTest` — happy path, duplicate email, audit REGULAR, auth/no-auth audit field |
| 7.2 | Update client | `PUT /api/v1/clients/{id}` | ✅ | Email uniqueness on update (`existsByEmailAndIdNot`), audit `REGULAR`, `@PreAuthorize(SA,AD,SALES)` | ✅ | happy path, not found, duplicate email on update, null email skips check |
| 7.3 | Delete client | `DELETE /api/v1/clients/{id}` | ✅ | Open-inquiry guard (`OPEN`/`IN_PROGRESS`), active-reservation guard (`PENDING`/`CONFIRMED`), audit `HIGH`, `@PreAuthorize(SA,AD)` | ✅ | happy path, not found, open inquiry throws, active reservation throws, audit HIGH |
| 7.4 | Increment purchase count (internal) | (triggered by BL 11.1) | ✅ | Increments `totalPurchases`, sets `lastPurchase` to now | ✅ | happy path increments count and sets lastPurchase, not found |

---

## Employee Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 8.1 | Create employee | `POST /api/v1/employees` | ✅ | Email uniqueness, BCrypt password hash, audit `CRITICAL`, `@PreAuthorize(SA,AD)` | ✅ | `EmployeeServiceImplTest` — happy path, duplicate email, null password (no hash), audit CRITICAL, auth audit field |
| 8.2 | Assign role | `POST /api/v1/employees/{id}/roles` | ❌ | Role existence check not verified | ❌ | |
| 8.3 | Update employee status | `PUT /api/v1/employees/{id}/status` | ✅ | Self-INACTIVE/SUSPENDED guard (`SELF_STATUS_CHANGE_FORBIDDEN`), audit `CRITICAL`, `@PreAuthorize(SA,AD)` | ✅ | happy path, not found, self→INACTIVE throws, self→SUSPENDED throws, self→ACTIVE allowed, other employee→INACTIVE allowed, audit CRITICAL |
| 8.4 | Delete employee | `DELETE /api/v1/employees/{id}` | ✅ | Sales guard (`EMPLOYEE_HAS_SALES`), tasks guard (`EMPLOYEE_HAS_TASKS`), last-SUPER_ADMIN guard (`LAST_SUPER_ADMIN`), audit `CRITICAL`, `@PreAuthorize(SA,AD)` | ✅ | happy path, not found, has sales throws, has tasks throws, last SA throws, not-last SA deletes, audit CRITICAL |

---

## Inquiry Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 9.1 | Create inquiry | `POST /api/v1/inquiries` | ✅ | Client ACTIVE check, single-vehicle discriminator (car XOR motorcycle), default OPEN, relationship wiring, audit `REGULAR`, `@Transactional(rollbackFor)` | ✅ | `InquiryServiceImplTest` — happy path, client not found, client not ACTIVE, car not found, both vehicles provided throws, relationship wired |
| 9.2 | Assign inquiry | `PUT /api/v1/inquiries/{id}/assign` | ⚠️ | Endpoint exists but business rule validation not fully verified | ❌ | |
| 9.3 | Update inquiry status | `PUT /api/v1/inquiries/{id}/status` | ✅ | Status transition validation (OPEN→IN_PROGRESS→RESPONDED→CLOSED, OPEN→CLOSED), RESPONDED requires response text check, audit `HIGH` | ✅ | valid transitions, invalid transitions throw, RESPONDED without response text throws, audit HIGH |
| 9.4 | Convert inquiry to reservation | `POST /api/v1/inquiries/{id}/reserve` | ⚠️ | Status validation (OPEN/IN_PROGRESS), deposit > 0 check; actual reservation creation stubbed | ⚠️ | validation guards tested |
| 9.5 | Delete inquiry | `DELETE /api/v1/inquiries/{id}` | ✅ | CLOSED inquiry with associated sale guard (`saleRepository.existsByCarId`), audit `HIGH` | ✅ | happy path, not found, closed inquiry with sale throws |

---

## Reservation Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 10.1 | Create reservation | `POST /api/v1/reservations` | ✅ | Vehicle AVAILABLE check, unique-active-reservation, expiry future validation, depositAmount ≥ 0, vehicle status → RESERVED, relationship wiring, audit `REGULAR`, `@Transactional(rollbackFor)` | ✅ | `ReservationServiceImplTest` — happy path, validations (expiry date, deposit amount), car not available, duplicate reservation, relationships wired, audit logged |
| 10.2 | Cancel reservation | `POST /api/v1/reservations/{id}/cancel` | ✅ | CONFIRMED/PENDING status required, vehicle status revert to AVAILABLE, audit `HIGH` | ✅ | happy path (confirmed & pending), expired/cancelled throw, not found, audit HIGH |
| 10.3 | Expire reservations (scheduler) | (automated) | ✅ | Status → EXPIRED, vehicle status revert (only if RESERVED), audit `HIGH` with null performedBy | ✅ | batch expiry, car status guard (only saves if RESERVED) |
| 10.4 | Update deposit status | `PUT /api/v1/reservations/{id}/deposit` | ✅ | Updates `depositPaid` boolean, audit `REGULAR` | ✅ | happy path, not found |
| 10.5 | Convert reservation to sale | `POST /api/v1/reservations/{id}/convert-to-sale` | ⚠️ | CONFIRMED status + depositPaid validation, employeeId existence check; actual sale creation throws `NOT_IMPLEMENTED` | ✅ | all validation guards tested (status, depositPaid, employee not found) |

---

## Sales Processing

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 11.1 | Create sale | `POST /api/v1/sales` | ✅ | Vehicle-not-SOLD guard, salePrice > 0, saleDate not-in-future, commission calc (`calculateCommission()`), vehicle → SOLD, client purchase count increment, reservation status → CONFIRMED, storage location count decrement, auto-create `FinancialTransaction` (type SALE), relationship wiring, audit `CRITICAL`, `@Transactional(rollbackFor)` | ✅ | `SaleServiceImplTest` — happy path with all side effects, price validations (zero/negative/null/future date), car not found, car already sold, client/employee not found, commission calculation, financial transaction creation, reservation update, no storage location handling |
| 11.2 | Update sale | `PUT /api/v1/sales/{id}` | ✅ | Vehicle-ID immutability guard (`IMMUTABLE_VEHICLE`), commission re-calc on price change, audit `HIGH` | ✅ | happy path, price change triggers recalc, changing car throws, not found |
| 11.3 | Delete sale | `DELETE /api/v1/sales/{id}` (`SA` only) | ✅ | Financial-transaction guard (`existsBySaleCarId`), vehicle status revert to AVAILABLE, client.totalPurchases decrement (if > 0), audit `CRITICAL` | ✅ | happy path with reverts, has financial transactions throws, not found, zero purchases edge case |

---

## Financial Transactions

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 12.1 | Create financial transaction | `POST /api/v1/financial-transactions` | ✅ | Amount > 0 validation, transactionDate not-in-future validation, car existence check (optional — can be null for overhead), car relationship wiring, audit `CRITICAL`, `@Transactional(rollbackFor)`; auto-creation triggered by BL 11.1 (Sale) | ✅ | `FinancialTransactionServiceImplTest` — happy path, amount validations (zero/negative/null), date in future throws, car not found, relationship wired, overhead transaction without car |
| 12.2 | Update financial transaction | `PUT /api/v1/financial-transactions/{id}` | ✅ | SALE-type immutability guard (`IMMUTABLE_TRANSACTION` — requires SA authorization), audit `CRITICAL` | ✅ | happy path, SALE type update throws, not found |
| 12.3 | Delete financial transaction | `DELETE /api/v1/financial-transactions/{id}` (`SA` only) | ✅ | Existence check, audit `CRITICAL` | ✅ | happy path, not found, audit before deletion |

---

## Task Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 13.1 | Create task | `POST /api/v1/tasks` | ✅ | Assignee ACTIVE check, default status=TODO/priority=MEDIUM, future dueDate validation, relationship wiring, audit `REGULAR`, `@Transactional(rollbackFor)` | ✅ | `TaskServiceImplTest` — happy path, without assignee, due date in past throws, assignee not found, assignee not ACTIVE throws, audit REGULAR |
| 13.2 | Update task status | `PUT /api/v1/tasks/{id}/status` | ✅ | Status update with previous status tracking, audit `REGULAR` with transition details | ✅ | happy path with transition details, not found |
| 13.3 | Overdue task detection (scheduler) | (automated) | ✅ | `OverdueTaskScheduler` — daily at 08:00 (configurable via `task.overdue.cron`); finds tasks via `TaskRepository.findOverdueTasks(now)`; notifies assignee (if set) and all AD/SA/SM employees with `TASK_OVERDUE`; skips duplicate notification when manager equals assignee; deduplicates recipients across roles; ShedLock (1 h / 5 m) | ✅ | `OverdueTaskSchedulerTest` — noOverdueTasks_noNotifications, assigneeNotified, noAssignee_onlyManagersNotified, managerIsAssignee_notifiedOnce, payloadContainsRequiredFields, multipleOverdueTasks_allNotified, deduplicatedRecipient_notifiedOncePerTask |
| 13.4 | Delete task | `DELETE /api/v1/tasks/{id}` | ⚠️ | Existence check, audit `HIGH`; DONE+linked-record guard TODO (Task entity lacks taskId in Inspection/Sale) | ✅ | happy path, not found, audit HIGH |

---

## Event Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 14.1 | Create event | `POST /api/v1/events` | ✅ | endTime > startTime validation, single-vehicle discriminator (car XOR motorcycle), TEST_DRIVE events auto-update vehicle status to RESERVED (if AVAILABLE), relationship wiring, audit `REGULAR`, `@Transactional(rollbackFor)` | ✅ | `EventServiceImplTest` — happy path, endTime before startTime throws, both vehicles throws, car not found, motorcycle not found, TEST_DRIVE reserves car (AVAILABLE→RESERVED), TEST_DRIVE skips RESERVED/SOLD cars, non-TEST_DRIVE doesn't reserve, relationship wired, audit REGULAR |
| 14.2 | Event reminders (scheduler) | (automated) | ✅ | `EventReminderScheduler` — hourly (configurable via `event.reminder.check-interval-ms`); 24 h window [now+23h, now+25h] and 1 h window [now+30m, now+90m] via `EventRepository.findEventsBetween`; dispatches `EVENT_REMINDER` to all SM, AD, SA employees (Event entity has no creator/linked-employee relationship — best-effort broadcast to authorised roles per BL 14.1); title falls back to `name` field; deduplicates recipients across roles; ShedLock (55 m / 5 m) | ✅ | `EventReminderSchedulerTest` — noEvents_noNotifications, eventIn24hWindow_notifiesManagement, eventIn1hWindow_notifiesManagement, noManagementEmployees_noNotifications, payloadContainsRequiredFields, eventWithNullTitle_fallsBackToName, deduplicatedRecipient_notifiedOncePerEvent, eventsInBothWindows_bothWindowsNotified |

---

## File Management

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 15.1 | Upload file | `POST /api/v1/files` | ✅ | Type/size validation, UUID generation, ACTIVE status set, REGULAR audit log; `@PreAuthorize("isAuthenticated()")` on controller; `FileAccessLog` entity not yet created (pending migration) | ✅ | happyPath, emptyFile, fileTooLarge, statusActive, auditLogged, authenticated/unauthenticated movedBy |
| 15.2 | Delete file | `DELETE /api/v1/files/{id}` | ✅ | Soft-delete sets DELETED status (HIGH audit); hard-delete removes physical file + DB record (HIGH audit, audited before delete); `@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")` on controller | ✅ | softDelete happyPath/notFound/auditLevel; hardDelete happyPath/auditOrder/notFound |
| 15.3 | Access file | `GET /api/v1/files/{id}` | ⚠️ | File served via Resource; `@PreAuthorize("isAuthenticated()")` on controller; `FileAccessLog` entity does not exist — access log entry per BL 15.3 not implemented (pending entity + migration) | ✅ | notFoundInDb, physicalFileMissing, happyPath |

---

## RBAC Administration

| BL Ref | Operation | Endpoint | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|----------|:-----------:|------------|:-----------:|------------|
| 16.1 | Create custom role | `POST /api/v1/rbac/roles` | ✅ | Name uniqueness (`existsByName`), `isSystem=false` forced, audit `CRITICAL`, `@PreAuthorize(SA,AD)` | ✅ | `RoleServiceImplTest` — happyPath, duplicateName, auditLevelCritical, auth/no-auth audit field |
| 16.1 | Update role | `PUT /api/v1/rbac/roles/{id}` | ✅ | System-role guard (`SYSTEM_ROLE_MODIFY`), audit `CRITICAL` | ✅ | happyPath, systemRole throws, notFound |
| 16.2 | Delete role | `DELETE /api/v1/rbac/roles/{id}` | ✅ | System-role guard (`SYSTEM_ROLE_DELETE`), has-employees guard (`ROLE_HAS_EMPLOYEES`), audit `CRITICAL`, `@PreAuthorize(SA,AD)` | ✅ | happyPath, systemRole throws, roleHasEmployees throws, notFound, auditLevelCritical |
| 16.2 | Assign/remove role to employee | `POST/DELETE /api/v1/rbac/employees/{id}/roles/{roleId}` | ✅ | Employee + role existence checks, audit `CRITICAL`, cache eviction on permission change | ✅ | assignRole happyPath/employeeNotFound/roleNotFound; removeRole happyPath/employeeNotFound |
| 16.3 | Add/remove permission | `POST/DELETE /api/v1/rbac/roles/{id}/permissions/{permId}` | ✅ | Role + permission existence, cache evict per assigned employee (`cacheInvalidationService`), audit `CRITICAL`, `@PreAuthorize(SA,AD)` | ✅ | addPermission happyPath/permissionNotFound; removePermission happyPath/roleNotFound/permissionNotFound |
| 16.3 | Create/delete permission | `POST/DELETE /api/v1/rbac/permissions` | ✅ | Name uniqueness (`existsByName`), has-roles guard (`PERMISSION_HAS_ROLES`) on delete, audit `CRITICAL` | ✅ | `PermissionServiceImplTest` — createPermission: happyPath/duplicate/auditLevel; deletePermission: happyPath/hasRoles throws/notFound/auditLevel |
| 16.4 | Create data scope | `POST /api/v1/rbac/data-scopes` | ✅ | Employee existence check, duplicate scope guard, audit `CRITICAL`, `DATA_SCOPE_CHANGED` notification dispatched, `@PreAuthorize(SA,AD)` | ✅ | `DataScopeServiceImplTest` — addScope: happyPath/employeeNotFound/duplicateScope/auditLevel/auth/no-auth; updateScope: happyPath/notFound; removeScope: happyPath/notFound/auditLevel; hasScope: include/exclude/missing; getLocationScopes |
| 16.5 | Grant resource ACL | `POST /api/v1/rbac/acl` | ✅ | Duplicate ACL guard, audit `CRITICAL` (`AuditCategory.SYSTEM`), `@PreAuthorize(SA,AD)` | ✅ | `ResourceACLServiceImplTest` — addACL: happyPath/duplicate/auditLevel/auth/no-auth; removeACL: happyPath/notFound/auditLevel; getACLByResource; hasACLAccess: access/noAccess/employeeNotFound; removeAllACL: happyPath/auditLevel |

---

## Notification System

| BL Ref | Operation | Impl Status | Impl Notes | Test Status | Test Notes |
|--------|-----------|:-----------:|------------|:-----------:|------------|
| 17.1 | Notification generation (triggered by events) | ✅ | `createNotificationEvent` persists event, auto-generates `IN_APP` job from payload `recipientId`/`recipientType`, publishes to Kafka via `kafkaProducer.publishJob`; dedup key prevents duplicate jobs; audit `REGULAR`; `@Transactional(rollbackFor)`; `@PreAuthorize(SA,AD)` on create-event endpoint | ✅ | `NotificationServiceImplTest` — happyPath (event+job+Kafka), noRecipientInPayload (no job), dedupKeyExists (no 2nd job), auditLevelRegular, auth/no-auth audit field |
| 17.2 | Delivery rules (quiet hours, opt-out, digest) | ❌ | Not implemented in `NotificationServiceImpl`; Kafka consumer layer is out of scope for this audit | ❌ | |
| 17.3 | Mark notification as read | `PUT /api/v1/notifications/{id}/read` | ✅ | Fetches job, enforces recipient-only ownership (`validateNotificationOwnership` — `NOT_NOTIFICATION_RECIPIENT`); SA/ADMIN bypass; sets `sentAt`+`status=SENT` when unread; audit `REGULAR`; `@PreAuthorize(isAuthenticated())` on controller (was broken `EMPLOYEE` role — fixed) | ✅ | happyPath, alreadyRead_noSave, notFound, ownershipViolation throws, adminCanMarkAny, auditLevelRegular, unauthenticatedSkipsOwnershipCheck |

---

## Audit Logging (Cross-Cutting)

| Area | Impl Status | Impl Notes | Test Status | Test Notes |
|------|:-----------:|------------|:-----------:|------------|
| `audit_logs` table + Flyway migration (V22) | ✅ | `category`, `level`, `entity_id`, `action`, `performed_by_id`, `details`, `created_at` | N/A | |
| `AuditService` interface + `AuditServiceImpl` | ✅ | `log()` + `getAuditLogs()` with Specification filtering | ❌ | Unit tests not yet written |
| `AuditLogController` — `GET /api/v1/audit-logs` | ✅ | `@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")`, filterable by category/level/action/entityId/performedById | ❌ | |
| Audit calls in `CarServiceImpl` | ✅ | CREATE/UPDATE/MOVE → `REGULAR`; DELETE/STATUS_CHANGE → `HIGH` | ✅ | Covered in `CarServiceImplTest` |
| Audit calls in `MotorcycleServiceImpl` | ✅ | CREATE/UPDATE/MOVE → `REGULAR`; DELETE/STATUS_CHANGE → `HIGH` | ✅ | Covered in `MotorcycleServiceImplTest` |
| Audit calls in `ClientServiceImpl` | ✅ | CREATE/UPDATE → `REGULAR`; DELETE/STATUS_CHANGE → `HIGH` | ✅ | Covered in `ClientServiceImplTest` |
| Audit calls in `EmployeeServiceImpl` | ✅ | All writes → `CRITICAL` (security-sensitive) | ✅ | Covered in `EmployeeServiceImplTest` |
| Audit calls in `ReservationServiceImpl` | ✅ | CREATE/UPDATE/DEPOSIT → `REGULAR`; DELETE/STATUS_CHANGE/CANCEL → `HIGH` | ✅ | Covered in `ReservationServiceImplTest` |
| Audit calls in `SaleServiceImpl` | ✅ | CREATE/DELETE → `CRITICAL`; UPDATE → `HIGH` | ✅ | Covered in `SaleServiceImplTest` |
| Audit calls in `FinancialTransactionServiceImpl` | ✅ | All writes → `CRITICAL` (financial operations) | ✅ | Covered in `FinancialTransactionServiceImplTest` |
| Audit calls in `InquiryServiceImpl` | ✅ | CREATE/UPDATE → `REGULAR`; DELETE/STATUS_CHANGE → `HIGH` | ✅ | Covered in `InquiryServiceImplTest` |
| Audit calls in `TaskServiceImpl` | ✅ | CREATE/UPDATE/STATUS_CHANGE → `REGULAR`; DELETE → `HIGH` | ✅ | Covered in `TaskServiceImplTest` |
| Audit calls in `EventServiceImpl` | ✅ | CREATE/UPDATE → `REGULAR`; DELETE → `HIGH` | ✅ | Covered in `EventServiceImplTest` |
| Audit calls in all other service impls | ✅ | RBAC services: `RoleServiceImpl` (all writes → `CRITICAL`), `PermissionServiceImpl` (all writes → `CRITICAL`), `DataScopeServiceImpl` (all writes → `CRITICAL`), `ResourceACLServiceImpl` (all writes → `CRITICAL`, `AuditCategory.SYSTEM`); `NotificationServiceImpl` (CREATE_EVENT/CREATE_JOB/MARK_AS_READ/MARK_ALL_AS_READ → `REGULAR`, `AuditCategory.SYSTEM`) | ✅ | Covered in RoleServiceImplTest, PermissionServiceImplTest, DataScopeServiceImplTest, ResourceACLServiceImplTest, NotificationServiceImplTest |

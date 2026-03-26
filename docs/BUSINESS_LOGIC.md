# WheelShift Pro — Business Logic Specification

**Version:** 1.0  
**Last Updated:** March 26, 2026  
**Status:** Living Document — update as features are implemented or revised

---

## Table of Contents

1. [Overview](#overview)
2. [Notation & Conventions](#notation--conventions)
3. [Vehicle Management — Cars](#1-vehicle-management--cars)
4. [Vehicle Management — Motorcycles](#2-vehicle-management--motorcycles)
5. [Vehicle Model Catalog](#3-vehicle-model-catalog)
6. [Vehicle Inspections](#4-vehicle-inspections)
7. [Vehicle Movements](#5-vehicle-movements)
8. [Storage Location Management](#6-storage-location-management)
9. [Client Management](#7-client-management)
10. [Employee Management](#8-employee-management)
11. [Inquiry Management (Leads)](#9-inquiry-management-leads)
12. [Reservation Management](#10-reservation-management)
13. [Sales Processing](#11-sales-processing)
14. [Financial Transactions](#12-financial-transactions)
15. [Task Management](#13-task-management)
16. [Event Management](#14-event-management)
17. [File Management](#15-file-management)
18. [RBAC Administration](#16-rbac-administration)
19. [Notification System](#17-notification-system)
20. [Status Transition Rules](#status-transition-rules)
21. [Cross-Cutting Rules](#cross-cutting-rules)

---

## Overview

This document defines all business rules, workflows, side effects, validations, and role-based access control that must be enforced throughout the WheelShift Pro backend. Each section covers a domain and lists every meaningful lifecycle event — what must happen when it is triggered, what is blocked, and who is authorized.

**Both cars and motorcycles share the same business logic patterns.** Where the two differ, they are documented separately.

---

## Notation & Conventions

| Symbol | Meaning |
|--------|---------|
| ✅ **MUST** | Required business logic — must be implemented |
| ⚠️ **SHOULD** | Strongly recommended — implement unless explicitly excluded |
| 🔔 **NOTIFY** | Trigger a notification event |
| 📝 **AUDIT** | Write an audit log entry |
| 🔒 **GUARD** | Security / authorization check |
| ❌ **BLOCK** | Operation must be rejected with an error |

**Roles used throughout this document:**

| Short Name | Full Role | Description |
|------------|-----------|-------------|
| `SA` | SUPER_ADMIN | Unrestricted access |
| `AD` | ADMIN | Administrative operations |
| `SM` | STORE_MANAGER | Inventory and storage |
| `SL` | SALES | Sales and inquiries |
| `IN` | INSPECTOR | Inspections only |
| `FN` | FINANCE | Financial records |

---

## 1. Vehicle Management — Cars

### 1.1 Add a New Car

**Triggered by:** `POST /api/v1/cars`

**Who can perform:** `SA`, `AD`, `SM`

**Validations:**
- ✅ **MUST** — VIN number must be globally unique across all cars
- ✅ **MUST** — Registration number must be globally unique if provided
- ✅ **MUST** — `carModelId` must reference an existing, valid car model
- ✅ **MUST** — If `storageLocationId` is provided, the location must exist and have available capacity (`currentCarCount + currentMotorcycleCount < totalCapacity`)
- ✅ **MUST** — `purchasePrice` must be greater than 0
- ⚠️ **SHOULD** — `sellingPrice` should be greater than `purchasePrice`
- ✅ **MUST** — `description` must not exceed 600 characters if provided

**Side Effects (all must execute within the same transaction):**
1. ✅ Car record is persisted with status `AVAILABLE` by default
2. ✅ **Storage Location Count** — `storageLocation.currentCarCount` is incremented by 1
3. ⚠️ **Purchase Financial Transaction** — A `FinancialTransaction` of type `PURCHASE` should be automatically created with `amount = purchasePrice`, linked to the new car
4. 🔔 **Notify Store Manager** — Notification event `VEHICLE_ADDED` should be dispatched to all employees with the `SM` role at the target storage location
5. 🔔 **Notify Admin** — `VEHICLE_ADDED` notification should be sent to `AD` roles for visibility
6. 📝 **Audit Log** — Entry: `{actorId} created car {carId} (VIN: {vin}) and assigned to location {locationId}`
7. ⚠️ **Cache Invalidation** — Clear inventory cache keys for cars and the affected storage location

**Error Responses:**
- `409 DUPLICATE_VIN` — VIN already exists
- `404 CAR_MODEL_NOT_FOUND` — Invalid car model ID
- `404 LOCATION_NOT_FOUND` — Invalid storage location ID
- `409 LOCATION_FULL` — Storage location at capacity

---

### 1.2 Update a Car

**Triggered by:** `PUT /api/v1/cars/{id}`

**Who can perform:** `SA`, `AD`, `SM`

**Validations:**
- ✅ **MUST** — Car must exist
- ✅ **MUST** — If VIN is changed, the new VIN must be globally unique
- ✅ **MUST** — If `carModelId` is changed, the new model must exist
- ✅ **MUST** — If `storageLocationId` is changed, use the **Move Car** logic (BL 1.5), not a direct update
- ✅ **MUST** — Status changes must follow the allowed transition rules (see [Status Transitions](#status-transition-rules))
- ❌ **BLOCK** — Cannot change `purchasePrice` if a sale has already been recorded for this car

**Side Effects:**
1. ✅ Car record is updated
2. 📝 **Audit Log** — Entry: `{actorId} updated car {carId}` with the list of changed fields
3. ⚠️ **Cache Invalidation** — Clear car detail cache and inventory list cache

---

### 1.3 Delete a Car

**Triggered by:** `DELETE /api/v1/cars/{id}`

**Who can perform:** `SA`, `AD`

**Validations:**
- ✅ **MUST** — Car must exist
- ❌ **BLOCK** — Cannot delete a car with status `RESERVED` (an active reservation exists)
- ❌ **BLOCK** — Cannot delete a car with status `SOLD` (sale record exists)
- ❌ **BLOCK** — Cannot delete if any `FinancialTransaction` is linked to this car
- ⚠️ **SHOULD** — Warn if pending inspections are linked to this car

**Side Effects:**
1. ✅ Car record is permanently deleted
2. ✅ **Storage Location Count** — `storageLocation.currentCarCount` is decremented by 1 (if car had a location)
3. 📝 **Audit Log** — Entry: `{actorId} deleted car {carId} (VIN: {vin})`
4. ⚠️ **Cache Invalidation** — Clear all related cache keys

---

### 1.4 Update Car Status

**Triggered by:** `PUT /api/v1/cars/{id}/status`

**Who can perform:**

| Status Change | Allowed Roles |
|---------------|---------------|
| `AVAILABLE` → `UNDER_INSPECTION` | `SA`, `AD`, `SM`, `IN` |
| `AVAILABLE` → `IN_TRANSIT` | `SA`, `AD`, `SM` |
| `UNDER_INSPECTION` → `AVAILABLE` | `SA`, `AD`, `SM`, `IN` |
| `RESERVED` → `AVAILABLE` (cancel reservation) | Handled via Reservation Cancel endpoint |
| `RESERVED` → `SOLD` | Handled via Sale creation endpoint |
| Any → `SOLD` | ❌ BLOCKED — only via Sale creation |

**Side Effects:**
1. ✅ Car status is updated
2. 🔔 **Notify Store Manager** if status changes to `UNDER_INSPECTION`
3. 🔔 **Notify Inspector** when status changes to `UNDER_INSPECTION` — prompt them to schedule inspection
4. 📝 **Audit Log** — Entry: `{actorId} changed car {carId} status from {old} to {new}`
5. ⚠️ **Cache Invalidation** — Clear car detail and list caches

---

### 1.5 Move Car to Location

**Triggered by:** `POST /api/v1/cars/{id}/move?locationId={id}`

**Who can perform:** `SA`, `AD`, `SM`

**Validations:**
- ✅ **MUST** — Car must exist
- ✅ **MUST** — Target location must exist
- ✅ **MUST** — Target location must have available capacity
- ❌ **BLOCK** — Cannot move a car with status `SOLD`
- ⚠️ **SHOULD** — Warn if moving a car that is `RESERVED`

**Side Effects:**
1. ✅ `car.storageLocation` is updated to the new location
2. ✅ **Old Location** — `oldLocation.currentCarCount` is decremented by 1
3. ✅ **New Location** — `newLocation.currentCarCount` is incremented by 1
4. ✅ **Movement Record** — A `CarMovement` record is created with `fromLocation`, `toLocation`, `movedAt`, and `movedByEmployeeId`
5. 🔔 **Notify Store Manager** of both old and new locations
6. 📝 **Audit Log** — Entry: `{actorId} moved car {carId} from location {fromId} to location {toId}`
7. ⚠️ **Cache Invalidation** — Clear car cache and both affected location caches

---

## 2. Vehicle Management — Motorcycles

All business logic mirrors the car section (1.1–1.5) with the following differences:

### Key Differences from Cars

| Rule | Cars | Motorcycles |
|------|------|-------------|
| Storage count field | `currentCarCount` | `currentMotorcycleCount` |
| Additional uniqueness check | VIN only | VIN + Registration Number (both must be unique) |
| Extra fields to validate | — | `engineNumber`, `chassisNumber`, `insuranceExpiryDate`, `pollutionCertificateExpiry` |
| Status blocker on delete | `RESERVED`, `SOLD` | `RESERVED`, `SOLD` |
| Movement record entity | `CarMovement` | `MotorcycleMovement` |

### 2.1 Add a New Motorcycle

**Who can perform:** `SA`, `AD`, `SM`

**Additional Validations:**
- ✅ **MUST** — `registrationNumber` must be globally unique across all motorcycles (if provided)
- ⚠️ **SHOULD** — Warn when `insuranceExpiryDate` is within 30 days of today
- ⚠️ **SHOULD** — Warn when `pollutionCertificateExpiry` is within 30 days of today

**Side Effects** (same as 1.1, plus):
1. ⚠️ **Attention Alert** — If motorcycle has `isAccidental = true` or `requiresRepair = true`, create a notification to assign an inspection
2. All other side effects are identical to car addition (financial transaction, storage count, notifications, audit log)

---

## 3. Vehicle Model Catalog

### 3.1 Create a Car/Motorcycle Model

**Triggered by:** `POST /api/v1/car-models` or `POST /api/v1/motorcycle-models`

**Who can perform:** `SA`, `AD`

**Validations:**
- ✅ **MUST** — The combination of `make + model + variant` must be unique
- ✅ **MUST** — `exShowroomPrice` must be positive if provided

**Side Effects:**
1. ✅ Model record is persisted
2. 📝 **Audit Log** — Entry: `{actorId} created model {make} {model} {variant}`

---

### 3.2 Delete a Car/Motorcycle Model

**Who can perform:** `SA`, `AD`

**Validations:**
- ❌ **BLOCK** — Cannot delete a model if any vehicle (`Car` or `Motorcycle`) references it
  - Error: `409 MODEL_HAS_VEHICLES`

---

## 4. Vehicle Inspections

### 4.1 Create an Inspection

**Triggered by:** `POST /api/v1/car-inspections` or `POST /api/v1/motorcycle-inspections`

**Who can perform:** `SA`, `AD`, `IN`, `SM`

**Validations:**
- ✅ **MUST** — Target vehicle must exist
- ✅ **MUST** — `inspectorId` must reference a valid employee (motorcycles only; cars use `inspectorName` text field)
- ✅ **MUST** — `inspectionDate` cannot be a future date

**Side Effects:**
1. ✅ Inspection record is persisted
2. ✅ **Vehicle Status Update** — If inspection passes (`passed = false`), update vehicle status to `UNDER_INSPECTION`; if passed (`passed = true`), optionally revert to `AVAILABLE`
3. ⚠️ **Repair Task Creation** — If `requiresRepair = true`, automatically create a `Task` with title `"Repair required for {vehicle}"`, priority `HIGH`, and assign it to the inspector or store manager
4. 🔔 **Notify Store Manager** with inspection result (pass/fail, estimated repair cost)
5. 🔔 **Notify Admin** if inspection fails or accident history is recorded
6. 📝 **Audit Log** — Entry: `{inspectorId} completed inspection {inspectionId} for vehicle {vehicleId} — Result: {pass/fail}`
7. ⚠️ **Cache Invalidation** — Clear vehicle detail cache

---

### 4.2 Update an Inspection

**Who can perform:** `SA`, `AD`, `IN`

**Validations:**
- ✅ **MUST** — Inspection record must exist
- ❌ **BLOCK** — Should not allow modifying `inspectionDate` to a future date

**Side Effects:**
1. ✅ Inspection record is updated
2. ⚠️ Re-evaluate vehicle status if pass/fail result changes
3. 📝 **Audit Log**

---

### 4.3 Delete an Inspection

**Who can perform:** `SA`, `AD`

**Validations:**
- ✅ **MUST** — Inspection must exist
- ⚠️ **SHOULD** — Warn if deleting the only inspection for a vehicle

---

## 5. Vehicle Movements

### 5.1 Record a Movement

Movement records are automatically created by the **Move Car** (`BL 1.5`) and **Move Motorcycle** operations. They should not be created manually via API.

**Who can view movement history:** `SA`, `AD`, `SM`, `IN`

**Validations on auto-creation:**
- ✅ **MUST** — `fromLocation` can be null (vehicle had no previous location)
- ✅ **MUST** — `toLocation` must not be null
- ✅ **MUST** — `movedAt` is always set to the current system timestamp
- ✅ **MUST** — `movedByEmployeeId` is set to the authenticated employee performing the move

---

## 6. Storage Location Management

### 6.1 Create a Storage Location

**Triggered by:** `POST /api/v1/storage-locations`

**Who can perform:** `SA`, `AD`

**Validations:**
- ✅ **MUST** — `name` must be unique across all storage locations
- ✅ **MUST** — `totalCapacity` must be at least 1
- ✅ **MUST** — `currentCarCount` and `currentMotorcycleCount` initialize to 0

**Side Effects:**
1. ✅ Location record is persisted with `currentCarCount = 0` and `currentMotorcycleCount = 0`
2. 📝 **Audit Log**

---

### 6.2 Update a Storage Location

**Who can perform:** `SA`, `AD`, `SM`

**Validations:**
- ✅ **MUST** — Location must exist
- ❌ **BLOCK** — `totalCapacity` cannot be reduced below the current total vehicle count (`currentCarCount + currentMotorcycleCount`)
  - Error: `409 CAPACITY_BELOW_CURRENT_COUNT`

**Side Effects:**
1. ✅ Location record is updated
2. 🔔 **Notify Store Manager** if capacity is being reduced (warn about tight space)
3. 📝 **Audit Log**

---

### 6.3 Delete a Storage Location

**Who can perform:** `SA`, `AD`

**Validations:**
- ❌ **BLOCK** — Cannot delete a location that currently has vehicles (`currentCarCount + currentMotorcycleCount > 0`)
  - Error: `409 LOCATION_HAS_VEHICLES`

---

### 6.4 Capacity Threshold Alerts

**Business Rule (automated):**
- 🔔 **At 80% capacity** — Send a notification to `SM` and `AD` that the location is nearing full
- 🔔 **At 100% capacity** — Send a notification to `SM` and `AD` that the location is full and no new vehicles can be assigned

These alerts trigger whenever `currentCarCount` or `currentMotorcycleCount` is updated.

---

## 7. Client Management

### 7.1 Create a Client

**Triggered by:** `POST /api/v1/clients`

**Who can perform:** `SA`, `AD`, `SL`

**Validations:**
- ✅ **MUST** — `email` must be globally unique
- ✅ **MUST** — `phone` should be validated for format
- ✅ **MUST** — A new client starts with `status = ACTIVE`, `totalPurchases = 0`

**Side Effects:**
1. ✅ Client record is persisted
2. 📝 **Audit Log**

---

### 7.2 Update a Client

**Who can perform:** `SA`, `AD`, `SL`

**Validations:**
- ✅ **MUST** — Client must exist
- ✅ **MUST** — If email is changed, the new email must be unique

---

### 7.3 Delete a Client

**Who can perform:** `SA`, `AD`

**Validations:**
- ⚠️ **SHOULD** — Warn (or block) if the client has open inquiries or active reservations
- ⚠️ **SHOULD** — Prefer deactivating (setting `status = INACTIVE`) over hard deletion to preserve historical sales data

**Side Effects:**
1. ✅ Client record is deleted or deactivated
2. 📝 **Audit Log**

---

### 7.4 Increment Client Purchase Count

**Triggered automatically when:** A sale is completed for this client

**Who triggers:** Internal — called by `SaleService`, not via direct API

**Side Effects:**
1. ✅ `client.totalPurchases` is incremented by 1
2. ✅ `client.lastPurchase` is set to today's date

---

## 8. Employee Management

### 8.1 Create an Employee

**Triggered by:** `POST /api/v1/employees`

**Who can perform:** `SA`, `AD`

**Validations:**
- ✅ **MUST** — `email` must be globally unique
- ✅ **MUST** — `password` must be BCrypt-hashed before storage — **never stored in plain text**
- ✅ **MUST** — Employee starts with `status = ACTIVE`
- ⚠️ **SHOULD** — Enforce a minimum password length (e.g., 8 characters)

**Side Effects:**
1. ✅ Employee record is persisted with hashed password
2. ⚠️ **Welcome Notification** — Send an in-app notification welcoming the new employee and prompting them to log in
3. 📝 **Audit Log** — Entry: `{actorId} created employee {employeeId} ({email})`

---

### 8.2 Assign Role to Employee

**Triggered by:** `POST /api/v1/employees/{id}/roles`

**Who can perform:** `SA`, `AD`

**Validations:**
- ✅ **MUST** — Employee must exist
- ✅ **MUST** — All role IDs must reference valid roles

**Side Effects:**
1. ✅ `EmployeeRole` join entries are created
2. 🔔 **Notify Employee** — Notify the employee that their roles have been updated
3. 📝 **Audit Log** — Entry: `{actorId} assigned roles {roleNames} to employee {employeeId}`

---

### 8.3 Update Employee Status

**Who can perform:** `SA`, `AD`

**Validations:**
- ✅ **MUST** — Employee must exist
- ❌ **BLOCK** — SUPER_ADMIN accounts cannot be suspended or deactivated by ADMIN
- ❌ **BLOCK** — An employee cannot change their own status to `INACTIVE` or `SUSPENDED`

**Side Effects:**
1. ✅ Employee status is updated
2. ⚠️ **SUSPENDED** — Revoke all active sessions / invalidate JWT tokens for that employee
3. 🔔 **Notify Employee** — Notify about status change
4. 📝 **Audit Log**

---

### 8.4 Delete an Employee

**Who can perform:** `SA`, `AD`

**Validations:**
- ❌ **BLOCK** — Cannot delete if employee has associated `Sale` records (historical integrity)
- ❌ **BLOCK** — Cannot delete if employee has open `Task` assignments
- ❌ **BLOCK** — Cannot delete the last SUPER_ADMIN account
- ⚠️ **SHOULD** — Prefer `status = INACTIVE` over deletion for employees with any history

---

## 9. Inquiry Management (Leads)

### 9.1 Create an Inquiry

**Triggered by:** `POST /api/v1/inquiries`

**Who can perform:** `SA`, `AD`, `SL`

**Validations:**
- ✅ **MUST** — `clientId` must reference a valid, active client
- ✅ **MUST** — If `carId` is provided, the car must exist
- ✅ **MUST** — If `motorcycleId` is provided, the motorcycle must exist
- ✅ **MUST** — Only one of `carId` or `motorcycleId` should be set; `vehicleType` discriminator must match
- ✅ **MUST** — Inquiry starts with `status = OPEN`

**Side Effects:**
1. ✅ Inquiry record is persisted
2. 🔔 **Notify Assigned Employee** — If `assignedEmployeeId` is set, notify that employee that a new inquiry has been assigned to them
3. 🔔 **Notify Sales Team** — Send a `INQUIRY_RECEIVED` notification to all employees with the `SL` role (if no specific assignment)
4. 📝 **Audit Log** — Entry: `{actorId} created inquiry {inquiryId} for client {clientId}`

---

### 9.2 Assign Inquiry to Employee

**Who can perform:** `SA`, `AD`, `SL` (can self-assign or reassign within team)

**Side Effects:**
1. ✅ `inquiry.assignedEmployeeId` is updated
2. 🔔 **Notify Newly Assigned Employee** — `INQUIRY_ASSIGNED` notification
3. 🔔 **Notify Previously Assigned Employee** if the inquiry is being reassigned
4. 📝 **Audit Log** — Entry: `{actorId} assigned inquiry {inquiryId} to employee {employeeId}`

---

### 9.3 Update Inquiry Status

**Who can perform:** `SA`, `AD`, `SL`

**Allowed Transitions:**
```
OPEN → IN_PROGRESS → RESPONDED → CLOSED
OPEN → CLOSED (with valid reason)
```

**Side Effects:**
1. ✅ Status is updated; if `RESPONDED`, `response` text and `responseDate` must be set
2. 📝 **Audit Log**

---

### 9.4 Convert Inquiry to Reservation

**Who can perform:** `SA`, `AD`, `SL`

**Validations:**
- ✅ **MUST** — Inquiry must be in `OPEN` or `IN_PROGRESS` status
- ✅ **MUST** — The vehicle must be `AVAILABLE`
- ✅ **MUST** — A deposit amount must be provided

**Side Effects:**
1. ✅ `Reservation` is created (see BL 10.1)
2. ✅ Inquiry status changes to `IN_PROGRESS`
3. 📝 **Audit Log**

---

### 9.5 Delete an Inquiry

**Who can perform:** `SA`, `AD`

**Validations:**
- ⚠️ **SHOULD** — Block deletion of `CLOSED` inquiries that led to a sale (data integrity)

---

## 10. Reservation Management

### 10.1 Create a Reservation

**Triggered by:** `POST /api/v1/reservations`

**Who can perform:** `SA`, `AD`, `SL`

**Validations:**
- ✅ **MUST** — Vehicle must be `AVAILABLE` (status check)
- ✅ **MUST** — Vehicle must not already have an active reservation (unique constraint)
- ✅ **MUST** — `clientId` must reference a valid client
- ✅ **MUST** — `expiryDate` must be in the future
- ✅ **MUST** — `depositAmount` must be ≥ 0

**Side Effects:**
1. ✅ Reservation record is persisted with `status = CONFIRMED`
2. ✅ **Vehicle Status** — Vehicle status is updated to `RESERVED`
3. 🔔 **Notify Client** — (Future: via email) Notify client that their reservation is confirmed
4. 🔔 **Notify Sales Employee** — `RESERVATION_CREATED` notification to the employee who created it (or the assigned sales person)
5. 🔔 **Notify Store Manager** — Inform that a vehicle has been reserved
6. 📝 **Audit Log** — Entry: `{actorId} created reservation {reservationId} for vehicle {vehicleId} by client {clientId}`
7. ⚠️ **Cache Invalidation** — Clear vehicle availability cache

---

### 10.2 Cancel a Reservation

**Triggered by:** `POST /api/v1/reservations/{id}/cancel`

**Who can perform:** `SA`, `AD`, `SL`

**Validations:**
- ✅ **MUST** — Reservation must be in `CONFIRMED` or `PENDING` status
- ❌ **BLOCK** — Cannot cancel an `EXPIRED` or already `CANCELLED` reservation

**Side Effects:**
1. ✅ Reservation status → `CANCELLED`
2. ✅ **Vehicle Status** — Vehicle status reverts to `AVAILABLE`
3. 🔔 **Notify Client** — Cancellation confirmation
4. 🔔 **Notify Store Manager** — Vehicle is available again
5. 📝 **Audit Log** — Entry: `{actorId} cancelled reservation {reservationId}`
6. ⚠️ **Cache Invalidation** — Clear vehicle and reservation caches

---

### 10.3 Expire Reservations (Automated)

**Triggered by:** Background scheduler (runs periodically — e.g., every hour)

**Logic:**
- Find all reservations where `expiryDate < NOW()` and `status = CONFIRMED or PENDING`

**Side Effects (per reservation):**
1. ✅ Reservation status → `EXPIRED`
2. ✅ **Vehicle Status** — Vehicle status reverts to `AVAILABLE`
3. 🔔 **Notify Client** — Reservation has expired
4. 🔔 **Notify Sales Employee** assigned to the inquiry (if any) — Reservation expired, follow up with client
5. 📝 **Audit Log**

---

### 10.4 Update Deposit Status

**Who can perform:** `SA`, `AD`, `SL`, `FN`

**Side Effects:**
1. ✅ `reservation.depositPaid` is updated
2. 🔔 **Notify Finance team** when deposit is marked as paid
3. 📝 **Audit Log**

---

### 10.5 Convert Reservation to Sale

**Triggered by:** `POST /api/v1/reservations/{id}/convert-to-sale`

**Who can perform:** `SA`, `AD`, `SL`

**Validations:**
- ✅ **MUST** — Reservation must be in `CONFIRMED` status
- ✅ **MUST** — `depositPaid` must be `true` before conversion is allowed
- ✅ **MUST** — `employeeId` performing the sale must be provided and valid

**Side Effects:** Identical to creating a sale (see BL 11.1) plus:
1. ✅ Reservation status → `CONFIRMED` (fulfilled — or add a new `CONVERTED` status)
2. Follows all Sale creation side effects

---

## 11. Sales Processing

### 11.1 Create a Sale

**Triggered by:** `POST /api/v1/sales`

**Who can perform:** `SA`, `AD`, `SL`

**Validations:**
- ✅ **MUST** — Vehicle must exist
- ✅ **MUST** — Vehicle must NOT already have a `SOLD` status (cannot sell twice)
- ✅ **MUST** — `clientId` must reference a valid client
- ✅ **MUST** — `employeeId` (sales rep) must reference a valid, active employee
- ✅ **MUST** — `salePrice` must be greater than 0
- ✅ **MUST** — `saleDate` must not be in the future
- ⚠️ **SHOULD** — `salePrice` should not be below `vehicle.minimumPrice` (if defined); warn or block

**Side Effects (all within the same transaction):**
1. ✅ `Sale` record is persisted
2. ✅ **Commission Calculation** — `totalCommission = salePrice × commissionRate / 100`; auto-calculated and stored on the `Sale`
3. ✅ **Vehicle Status** — Vehicle status → `SOLD`
4. ✅ **Client Purchase Count** — `client.totalPurchases` is incremented by 1; `client.lastPurchase` is set to today
5. ✅ **Active Reservation** — If an active reservation exists for this vehicle, update its status to `CONFIRMED` (fulfilled)
6. ✅ **Storage Location Count** — Vehicle is considered sold; `storageLocation.currentCarCount` (or motorcycle count) is decremented by 1
7. ⚠️ **Financial Transaction** — Automatically create a `FinancialTransaction` of type `SALE` with `amount = salePrice`, linked to the sale
8. 🔔 **Notify Finance Team** — `SALE_COMPLETED` notification for recording and commission confirmation
9. 🔔 **Notify Admin/Store Manager** — Vehicle is sold and leaves inventory
10. 🔔 **Notify Sales Employee** — Confirm sale and their commission amount
11. 📝 **Audit Log** — Entry: `{employeeId} completed sale {saleId} for vehicle {vehicleId} at price {salePrice}, commission {totalCommission}`
12. ⚠️ **Cache Invalidation** — Clear vehicle, sales, and inventory caches

**Error Responses:**
- `409 CAR_ALREADY_SOLD` — Vehicle already has a SOLD status
- `404 CLIENT_NOT_FOUND`
- `404 EMPLOYEE_NOT_FOUND`
- `400 PRICE_BELOW_MINIMUM` — If sale price is below minimum price threshold

---

### 11.2 Update a Sale

**Who can perform:** `SA`, `AD`

**Validations:**
- ✅ **MUST** — Sale must exist
- ❌ **BLOCK** — Changing `carId` or `motorcycleId` is not permitted once a sale is recorded
- ⚠️ **SHOULD** — Changing `salePrice` after the fact should require `AD` or `SA` approval and trigger re-calculation of `totalCommission`
- 📝 **AUDIT** — Any price change must be logged with old and new values

---

### 11.3 Delete a Sale

**Who can perform:** `SA` only

**Validations:**
- ✅ **MUST** — Sale must exist
- ⚠️ **SHOULD** — Deleting a sale should revert the vehicle status to `AVAILABLE`
- ⚠️ **SHOULD** — Deleting a sale should decrement `client.totalPurchases`
- ⚠️ **SHOULD** — Block if financial transactions have been settled against this sale

**Side Effects:**
1. ✅ Sale is deleted
2. ✅ Vehicle status reverts to `AVAILABLE`
3. ✅ `client.totalPurchases` is decremented
4. 📝 **Audit Log** — Entry: `{actorId} deleted sale {saleId}` with justification required

---

## 12. Financial Transactions

### 12.1 Create a Financial Transaction

**Triggered by:** `POST /api/v1/financial-transactions` (manual) or auto-created by business events

**Who can perform:** `SA`, `AD`, `FN`

**Auto-Created Events:**
| Trigger | Transaction Type | Amount |
|---------|-----------------|--------|
| Car/Motorcycle added | `PURCHASE` | `purchasePrice` |
| Sale completed | `SALE` | `salePrice` |
| Inspection with repair cost | `REPAIR` | `estimatedRepairCost` |

**Validations:**
- ✅ **MUST** — `amount` must be greater than 0
- ✅ **MUST** — `transactionDate` must not be in the future
- ✅ **MUST** — `transactionType` must be a valid enum value
- ⚠️ **SHOULD** — At least one of `carId` or `motorcycleId` should be set (unless it is a general overhead transaction)

**Side Effects:**
1. ✅ Transaction record is persisted
2. 📝 **Audit Log** — Entry: `{actorId} created transaction {transactionId} of type {type} for amount {amount}`
3. ⚠️ **Cache Invalidation** — Clear financial summary caches

---

### 12.2 Update a Financial Transaction

**Who can perform:** `SA`, `FN`

**Validations:**
- ✅ **MUST** — Transaction must exist
- ❌ **BLOCK** — Auto-created transactions (type `SALE`) should not be freely editable; require `SA` authorization

---

### 12.3 Delete a Financial Transaction

**Who can perform:** `SA` only

**Validations:**
- ✅ **MUST** — Transaction must exist
- 📝 **AUDIT** — Mandatory audit entry with justification

---

## 13. Task Management

### 13.1 Create a Task

**Triggered by:** `POST /api/v1/tasks`

**Who can perform:** `SA`, `AD`, `SM` (create and assign to others); All roles (create for themselves)

**Validations:**
- ✅ **MUST** — `title` is required and must not exceed 128 characters
- ✅ **MUST** — If `assigneeId` is provided, the employee must exist and be `ACTIVE`
- ✅ **MUST** — Task starts with `status = TODO` by default
- ✅ **MUST** — `priority` defaults to `MEDIUM` if not specified
- ⚠️ **SHOULD** — `dueDate` must be in the future if provided

**Side Effects:**
1. ✅ Task record is persisted
2. 🔔 **Notify Assigned Employee** — `TASK_ASSIGNED` notification with task details and due date
3. 📝 **Audit Log** — Entry: `{actorId} created task {taskId} assigned to {assigneeId}`

---

### 13.2 Update Task Status

**Who can perform:**

| Status Change | Allowed Roles |
|---------------|---------------|
| `TODO` → `IN_PROGRESS` | Task assignee, `AD`, `SM`, `SA` |
| `IN_PROGRESS` → `REVIEW` | Task assignee, `AD`, `SM`, `SA` |
| `REVIEW` → `DONE` | `AD`, `SM`, `SA` (not the assignee themselves) |
| `REVIEW` → `IN_PROGRESS` | `AD`, `SM`, `SA` (send back for rework) |
| Any → `DONE` | `AD`, `SM`, `SA` |

**Side Effects:**
1. ✅ Task status is updated
2. 🔔 **Notify Assignee** when status is moved to `REVIEW` (awaiting review)
3. 🔔 **Notify Task Creator** when status moves to `DONE`
4. 📝 **Audit Log**

---

### 13.3 Overdue Task Detection (Automated)

**Triggered by:** Scheduled job (e.g., runs daily)

**Logic:** Find tasks where `dueDate < NOW()` and `status != DONE`

**Side Effects:**
1. 🔔 **Notify Assignee** — `TASK_OVERDUE` notification
2. 🔔 **Notify Admin/Manager** — Overdue task alert with employee name and due date

---

### 13.4 Delete a Task

**Who can perform:** `SA`, `AD`, `SM`

**Validations:**
- ❌ **BLOCK** — Cannot delete a `DONE` task that is linked to an inspection or sale (audit trail)

---

## 14. Event Management

### 14.1 Create an Event

**Triggered by:** `POST /api/v1/events`

**Who can perform:** `SA`, `AD`, `SM`, `SL`

**Validations:**
- ✅ **MUST** — `title` and `startTime` are required
- ✅ **MUST** — `endTime` must be after `startTime`
- ✅ **MUST** — If `carId` is set, the car must exist; same for `motorcycleId`
- ✅ **MUST** — Only one of `carId` or `motorcycleId` should be linked per event

**Side Effects:**
1. ✅ Event record is persisted
2. 🔔 **Notify Participants** — If linked to an employee or vehicle, notify relevant parties
3. ⚠️ **Test Drive Events** — If `eventType = TEST_DRIVE`, automatically update vehicle status to `RESERVED` if not already reserved
4. 📝 **Audit Log**

---

### 14.2 Event Reminders (Automated)

**Triggered by:** Scheduled job

**Logic:** Find events scheduled within the next 24 hours (and 1 hour)

**Side Effects:**
1. 🔔 **Notify Creator and Linked Employees** — Reminder notification

---

## 15. File Management

### 15.1 Upload a File

**Triggered by:** `POST /api/v1/files`

**Who can perform:** All authenticated roles (within their domain)

**Validations:**
- ✅ **MUST** — File type must be allowed: `IMAGE`, `PDF`, `EXCEL`, `CSV`, `DOCUMENT`, `OTHER`
- ✅ **MUST** — File size must not exceed the configured maximum (10 MB for images, 20 MB for documents)
- ✅ **MUST** — A unique UUID is generated as the file ID
- ⚠️ **SHOULD** — Virus/malware scan for uploaded files (if scan service is available)

**Side Effects:**
1. ✅ File is stored in the configured storage backend (local or S3)
2. ✅ `FileMetadata` record is persisted with `status = ACTIVE`
3. ✅ `FileAccessLog` entry is created for the upload event
4. 📝 **Audit Log** — Entry: `{actorId} uploaded file {fileId} (type: {type}, size: {size})`

---

### 15.2 Delete a File

**Who can perform:** `SA`, `AD`, or the employee who uploaded it

**Side Effects:**
1. ✅ `FileMetadata.status` → `DELETED` (soft delete preferred)
2. ✅ Physical file may be removed from storage or archived
3. ⚠️ **Nullify References** — Check if any entity references this `fileId` and handle gracefully (do not hard-fail on read — return a placeholder)

---

### 15.3 Access File

**Who can perform:** Any authenticated employee, subject to RBAC on the parent entity

**Side Effects:**
1. ✅ `FileAccessLog` entry is created with `accessedBy`, `accessType`, timestamp, and IP

---

## 16. RBAC Administration

### 16.1 Create a Custom Role

**Who can perform:** `SA` only

**Validations:**
- ✅ **MUST** — Role name must be unique
- ✅ **MUST** — `isSystem = false` for custom roles (system roles are seeded)

**Side Effects:**
1. ✅ Role is persisted
2. 📝 **Audit Log** — Entry: `{actorId} created role {roleName}`

---

### 16.2 Delete a Role

**Who can perform:** `SA` only

**Validations:**
- ❌ **BLOCK** — Cannot delete system roles (`isSystem = true`)
  - Error: `409 SYSTEM_ROLE_DELETE`
- ❌ **BLOCK** — Cannot delete a role that is currently assigned to any employee

---

### 16.3 Add/Remove Permission from Role

**Who can perform:** `SA` only

**Side Effects:**
1. ✅ Permission is added/removed from the role
2. ⚠️ **Session Invalidation** — All active JWT tokens for employees with this role should expire (force re-login to pick up new permissions)
3. 📝 **Audit Log**

---

### 16.4 Create a Data Scope

**Triggered by:** `POST /api/v1/rbac/data-scopes`

**Who can perform:** `SA`, `AD`

**Side Effects:**
1. ✅ `EmployeeDataScope` is persisted
2. 🔔 **Notify Employee** — Their data visibility has been updated
3. 📝 **Audit Log** — Entry: `{actorId} applied {scopeType} scope ({effect}) to employee {employeeId}`

---

### 16.5 Grant / Revoke Resource ACL

**Who can perform:** `SA`, `AD`

**Side Effects:**
1. ✅ `ResourceACL` entry is created/deleted
2. 📝 **Audit Log** — Entry: `{actorId} granted {accessLevel} on {resourceType}/{resourceId} to {subjectType}/{subjectId}`

---

## 17. Notification System

### 17.1 Notification Generation

Notifications are created by business events listed throughout this document. Each notification must include:

| Field | Description |
|-------|-------------|
| `recipientId` | Employee ID who receives the notification |
| `eventType` | Enum: `VEHICLE_ADDED`, `SALE_COMPLETED`, `RESERVATION_CREATED`, `RESERVATION_EXPIRED`, `TASK_ASSIGNED`, `TASK_OVERDUE`, `INQUIRY_ASSIGNED`, `INSPECTION_COMPLETED`, `LOCATION_NEAR_CAPACITY`, etc. |
| `title` | Short notification title |
| `message` | Full message text (may use templates with `{{variable}}` placeholders) |
| `resourceType` | Type of related resource (e.g., `CAR`, `SALE`, `TASK`) |
| `resourceId` | ID of the related resource |

---

### 17.2 Notification Delivery Rules

- ✅ Respect employee **quiet hours** — do not deliver notifications outside configured working hours; queue instead
- ✅ Respect **opt-out preferences** — if an employee has opted out of a notification type, do not send it
- ✅ **Digest Mode** — if employee has digest mode enabled, batch notifications into periodic summaries
- ⚠️ **Retry** — if delivery fails, retry up to 3 times with exponential backoff

---

### 17.3 Mark Notification as Read

**Who can perform:** The notification recipient only (and `SA` for admin view)

---

## Status Transition Rules

### Car / Motorcycle Status Transitions

```
AVAILABLE  ──────────────────────────────────────────────────────────────┐
    │                                                                     │
    ├──[ Inspection scheduled ]──► UNDER_INSPECTION ──[ Pass ]──► AVAILABLE
    │                                                                     │
    ├──[ Reservation created ]──► RESERVED ──────────[ Sale created ]──► SOLD
    │                          └──[ Cancelled/Expired ]──► AVAILABLE     │
    │                                                                     │
    ├──[ Vehicle movement ]──► IN_TRANSIT ──[ Arrived ]──► AVAILABLE      │
    │                                                                     │
    └──[ Maintenance ]──► MAINTENANCE ──[ Fixed ]──► AVAILABLE            │
                                                                          │
                                              SOLD (terminal — no exit) ◄─┘
```

**Key Rules:**
- `SOLD` is a **terminal state** — a sold vehicle cannot change status without deleting the sale
- `RESERVED` → `AVAILABLE` only happens on reservation **cancel** or **expiry**
- `RESERVED` → `SOLD` only happens via **Sale creation**

---

### Reservation Status Transitions

```
PENDING ──► CONFIRMED ──► (converted) SOLD
                       └──[ Cancel ]──► CANCELLED
                       └──[ Expire ]──► EXPIRED
```

---

### Inquiry Status Transitions

```
OPEN ──► IN_PROGRESS ──► RESPONDED ──► CLOSED
OPEN ──────────────────────────────► CLOSED
```

---

### Task Status Transitions

```
TODO ──► IN_PROGRESS ──► REVIEW ──► DONE
                    └──────────────────────[ send back ]──► IN_PROGRESS
```

---

## Cross-Cutting Rules

### Authentication & Authorization

- 🔒 Every API call (except `/auth/login`) must include a valid JWT Bearer token
- 🔒 Token must not be expired; return `401 UNAUTHORIZED` if expired
- 🔒 Role checks run before business logic; return `403 FORBIDDEN` if role is insufficient
- 🔒 Data scope checks filter which records the employee may see, applied at the repository query level
- 🔒 Resource ACL overrides role-based checks for specific records (per the RBAC hierarchy)
- 🔒 `SUPER_ADMIN` bypasses all role and scope checks

---

### Audit Logging

- 📝 Every **create**, **update**, **delete**, and **status change** must produce an audit log entry
- 📝 Audit entries must capture: `actorId`, `actionType`, `entityType`, `entityId`, `timestamp`, `changedFields` (for updates)
- 📝 Audit logs are **immutable** — they must not be editable or deletable through any API endpoint

---

### Transactional Integrity

- ✅ All side effects listed under a business event (status updates, count changes, financial transactions, movement records) **must execute within the same database transaction**
- ✅ If any side effect fails, the entire operation must be rolled back
- ✅ Notifications are dispatched **after** the transaction commits (to avoid notifying on rollbacks)

---

### Cache Invalidation

Caches must be invalidated on any write that affects the cached data:

| Cache Key Pattern | Invalidated When |
|-------------------|-----------------|
| `cars:list:*` | Car created, updated, deleted, status changed |
| `cars:detail:{id}` | Car updated, status changed, moved |
| `motorcycles:list:*` | Motorcycle created, updated, deleted |
| `location:{id}:count` | Vehicle added/moved/sold at that location |
| `dashboard:*` | Any sale, reservation, or inventory change |
| `employee:{id}:permissions` | Role or permission change |

---

### Duplicate Prevention

- ✅ VIN numbers are globally unique across all cars (enforced at DB level)
- ✅ VIN numbers are globally unique across all motorcycles (enforced at DB level)
- ✅ Registration numbers are unique per vehicle type
- ✅ Employee email is globally unique
- ✅ Client email is globally unique
- ✅ Only one active reservation per vehicle at any time (unique constraint on `car_id` in `reservations`)
- ✅ Only one sale per vehicle (unique constraint on `car_id` in `sales`)
- ✅ Storage location name is globally unique

---

### Soft-Delete Guidelines

The following entities should prefer soft-delete (status change) over hard-delete:

| Entity | Preferred Approach |
|--------|-------------------|
| Clients | Set `status = INACTIVE` |
| Employees | Set `status = INACTIVE` |
| Files | Set `status = DELETED` |
| Vehicles | **Hard delete only** if no linked records |

---

*This document should be reviewed and updated whenever new features are added or existing business rules are modified.*

**Document Owner:** WheelShift Pro Development Team  
**Version:** 1.0  
**Last Updated:** March 26, 2026

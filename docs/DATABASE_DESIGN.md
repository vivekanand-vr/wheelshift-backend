# WheelShift Pro - Database Design Diagram (Updated with File Storage)

## Complete Database Schema with Motorcycle Support and File Storage Integration

```mermaid
erDiagram
    %% Core Entities - Cars
    CAR_MODELS {
        BIGINT id PK
        VARCHAR make
        VARCHAR model
        VARCHAR variant
        VARCHAR emission_norm
        VARCHAR body_type
        VARCHAR fuel_type
        VARCHAR transmission_type
        INT gears
        DECIMAL ex_showroom_price "Ex-showroom price in INR"
        VARCHAR model_image_id "File ID for model image"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    CARS {
        BIGINT id PK
        VARCHAR vin_number UK
        VARCHAR registration_number UK
        BIGINT car_model_id FK
        VARCHAR color
        INT mileage_km
        INT year
        INT engine_cc
        VARCHAR status
        BIGINT storage_location_id FK
        DECIMAL purchase_price
        DATE purchase_date
        DECIMAL selling_price
        VARCHAR primary_image_id "Primary car image"
        TEXT gallery_image_ids "Gallery images (comma-separated)"
        TEXT document_file_ids "Documents (comma-separated)"
        INT doors "Merged from car_detailed_specs"
        INT seats "Merged from car_detailed_specs"
        INT cargo_capacity_liters "Merged from car_detailed_specs"
        DECIMAL acceleration_0_100 "Merged from car_detailed_specs"
        INT top_speed_kmh "Merged from car_detailed_specs"
        JSON features "Merged from car_features as JSON"
        VARCHAR description "Optional vehicle description (max 600 chars)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% Core Entities - Motorcycles
    MOTORCYCLE_MODELS {
        BIGINT id PK
        VARCHAR make
        VARCHAR model
        VARCHAR variant
        INT year
        INT engine_capacity
        VARCHAR fuel_type
        VARCHAR transmission_type
        VARCHAR vehicle_type
        DECIMAL ex_showroom_price "Ex-showroom price in INR"
        VARCHAR model_image_id "File ID for model image"
        BOOLEAN is_active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    MOTORCYCLES {
        BIGINT id PK
        VARCHAR vin_number UK
        VARCHAR registration_number UK
        VARCHAR engine_number
        VARCHAR chassis_number
        BIGINT motorcycle_model_id FK
        VARCHAR color
        INT mileage_km
        INT manufacture_year
        DATE registration_date
        VARCHAR status
        BIGINT storage_location_id FK
        DECIMAL purchase_price
        DATE purchase_date
        DECIMAL selling_price
        DECIMAL minimum_price
        INT previous_owners
        DATE insurance_expiry_date
        DATE pollution_certificate_expiry
        BOOLEAN is_financed
        BOOLEAN is_accidental
        TEXT description
        VARCHAR primary_image_id "Primary motorcycle image"
        TEXT gallery_image_ids "Gallery images (comma-separated)"
        TEXT document_file_ids "Documents (comma-separated)"
        VARCHAR engine_type "Merged from motorcycle_detailed_specs"
        DECIMAL max_power_bhp "Merged from motorcycle_detailed_specs"
        DECIMAL max_torque_nm "Merged from motorcycle_detailed_specs"
        VARCHAR cooling_system "Merged from motorcycle_detailed_specs"
        DECIMAL fuel_tank_capacity "Merged from motorcycle_detailed_specs"
        DECIMAL claimed_mileage_kmpl "Merged from motorcycle_detailed_specs"
        INT length_mm "Merged from motorcycle_detailed_specs"
        INT width_mm "Merged from motorcycle_detailed_specs"
        INT height_mm "Merged from motorcycle_detailed_specs"
        INT wheelbase_mm "Merged from motorcycle_detailed_specs"
        INT ground_clearance_mm "Merged from motorcycle_detailed_specs"
        INT kerb_weight_kg "Merged from motorcycle_detailed_specs"
        VARCHAR front_brake_type "Merged from motorcycle_detailed_specs"
        VARCHAR rear_brake_type "Merged from motorcycle_detailed_specs"
        BOOLEAN abs_available "Merged from motorcycle_detailed_specs"
        VARCHAR front_suspension "Merged from motorcycle_detailed_specs"
        VARCHAR rear_suspension "Merged from motorcycle_detailed_specs"
        VARCHAR front_tyre_size "Merged from motorcycle_detailed_specs"
        VARCHAR rear_tyre_size "Merged from motorcycle_detailed_specs"
        BOOLEAN has_electric_start "Merged from motorcycle_detailed_specs"
        BOOLEAN has_kick_start "Merged from motorcycle_detailed_specs"
        BOOLEAN has_digital_console "Merged from motorcycle_detailed_specs"
        BOOLEAN has_usb_charging "Merged from motorcycle_detailed_specs"
        BOOLEAN has_led_lights "Merged from motorcycle_detailed_specs"
        TEXT additional_features "Merged from motorcycle_detailed_specs"
        VARCHAR description "Optional vehicle description (max 600 chars)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    MOTORCYCLE_INSPECTIONS {
        BIGINT id PK
        BIGINT motorcycle_id FK
        DATE inspection_date
        BIGINT inspector_id FK
        VARCHAR overall_condition
        VARCHAR engine_condition
        VARCHAR transmission_condition
        VARCHAR suspension_condition
        VARCHAR brake_condition
        VARCHAR tyre_condition
        VARCHAR electrical_condition
        VARCHAR body_condition
        BOOLEAN has_accident_history
        BOOLEAN requires_repair
        DECIMAL estimated_repair_cost
        TEXT repair_notes
        TEXT inspection_image_ids "Inspection photos (comma-separated)"
        VARCHAR inspection_report_file_id "Inspection report PDF"
        BOOLEAN passed
        TEXT notes
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    CAR_INSPECTIONS {
        BIGINT id PK
        BIGINT car_id FK
        DATE inspection_date
        VARCHAR inspector_name "Inspector name (not a FK)"
        TEXT exterior_condition
        TEXT interior_condition
        TEXT mechanical_condition
        TEXT electrical_condition
        TEXT accident_history
        TEXT required_repairs
        DECIMAL estimated_repair_cost
        BOOLEAN inspection_pass
        VARCHAR report_url
        TEXT inspection_image_ids "Inspection photos (comma-separated)"
        VARCHAR inspection_report_file_id "Inspection report PDF"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    CAR_MOVEMENTS {
        BIGINT id PK
        BIGINT car_id FK
        BIGINT from_location_id FK
        BIGINT to_location_id FK
        DATETIME moved_at
        BIGINT moved_by_employee_id FK
    }

    MOTORCYCLE_MOVEMENTS {
        BIGINT id PK
        BIGINT motorcycle_id FK
        BIGINT from_location_id FK
        BIGINT to_location_id FK
        DATETIME moved_at
        BIGINT moved_by_employee_id FK
        TEXT notes
    }

    %% Storage & Location
    STORAGE_LOCATIONS {
        BIGINT id PK
        VARCHAR name
        VARCHAR address
        VARCHAR contact_person
        VARCHAR contact_number
        INT total_capacity
        INT current_car_count "Active (non-SOLD) cars at this location"
        INT current_motorcycle_count "Active (non-SOLD) motorcycles at this location"
        VARCHAR location_image_id "Location photo"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% Clients & Employees
    CLIENTS {
        BIGINT id PK
        VARCHAR name
        VARCHAR email UK
        VARCHAR phone
        VARCHAR location
        VARCHAR status
        INT total_purchases
        DATE last_purchase
        VARCHAR profile_image_id "Client profile photo"
        TEXT document_file_ids "Client documents (comma-separated)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    EMPLOYEES {
        BIGINT id PK
        VARCHAR name
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR phone
        VARCHAR department
        VARCHAR position
        DATE join_date
        VARCHAR status
        DATETIME last_login
        VARCHAR profile_image_id "Employee profile photo"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% Transactions - Inquiries
    INQUIRIES {
        BIGINT id PK
        BIGINT car_id FK
        BIGINT motorcycle_id FK
        VARCHAR vehicle_type
        BIGINT client_id FK
        BIGINT assigned_employee_id FK
        VARCHAR inquiry_type
        TEXT message
        VARCHAR status
        TEXT response
        DATETIME response_date
        TEXT attachment_file_ids "Inquiry attachments (comma-separated)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% Transactions - Reservations
    RESERVATIONS {
        BIGINT id PK
        BIGINT car_id FK
        BIGINT motorcycle_id FK
        VARCHAR vehicle_type
        BIGINT client_id FK
        DATETIME reservation_date
        DATETIME expiry_date
        VARCHAR status
        DECIMAL deposit_amount
        BOOLEAN deposit_paid
        TEXT notes
        TEXT reservation_document_ids "Reservation docs (comma-separated)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% Transactions - Sales
    SALES {
        BIGINT id PK
        BIGINT car_id FK
        BIGINT motorcycle_id FK
        VARCHAR vehicle_type
        BIGINT client_id FK
        BIGINT employee_id FK
        DATETIME sale_date
        DECIMAL sale_price
        DECIMAL commission_rate
        DECIMAL total_commission
        VARCHAR payment_method
        TEXT sale_document_ids "Sale documents (comma-separated)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% Financial Transactions
    FINANCIAL_TRANSACTIONS {
        BIGINT id PK
        BIGINT car_id FK
        BIGINT motorcycle_id FK
        VARCHAR vehicle_type
        VARCHAR transaction_type
        DECIMAL amount
        DATETIME transaction_date
        TEXT description
        VARCHAR vendor_name
        TEXT transaction_file_ids "Transaction files (comma-separated)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% Events & Tasks
    EVENTS {
        BIGINT id PK
        VARCHAR type
        VARCHAR name
        BIGINT car_id FK
        BIGINT motorcycle_id FK
        VARCHAR vehicle_type
        VARCHAR title
        DATETIME start_time
        DATETIME end_time
        TEXT attachment_file_ids "Event attachments (comma-separated)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    TASKS {
        BIGINT id PK
        VARCHAR title
        TEXT description
        VARCHAR status
        VARCHAR priority
        BIGINT assignee_id FK
        DATE due_date
        JSON tags
        TEXT attachment_file_ids "Task attachments (comma-separated)"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% File Storage System
    FILE_METADATA {
        BIGINT id PK
        VARCHAR file_id UK "UUID identifier"
        VARCHAR original_filename
        VARCHAR stored_filename
        VARCHAR file_type "IMAGE, PDF, EXCEL, CSV, DOCUMENT, OTHER"
        VARCHAR mime_type
        BIGINT file_size
        VARCHAR file_extension
        VARCHAR storage_path
        VARCHAR public_url
        VARCHAR upload_source
        VARCHAR uploaded_by
        VARCHAR status "ACTIVE, DELETED, ARCHIVED"
        TEXT metadata_json
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% RBAC System
    ROLES {
        BIGINT id PK
        VARCHAR name UK
        VARCHAR description
        BOOLEAN is_system
        TIMESTAMP created_at
        TIMESTAMP updated_at
        VARCHAR created_by
        VARCHAR updated_by
    }

    PERMISSIONS {
        BIGINT id PK
        VARCHAR name UK
        VARCHAR resource
        VARCHAR action
        VARCHAR description
        TIMESTAMP created_at
        TIMESTAMP updated_at
        VARCHAR created_by
        VARCHAR updated_by
    }

    ROLE_PERMISSIONS {
        BIGINT role_id FK
        BIGINT permission_id FK
    }

    EMPLOYEE_ROLES {
        BIGINT employee_id FK
        BIGINT role_id FK
    }

    EMPLOYEE_DATA_SCOPES {
        BIGINT id PK
        BIGINT employee_id FK
        VARCHAR scope_type "LOCATION, DEPARTMENT, ASSIGNMENT"
        VARCHAR scope_value
        VARCHAR effect "INCLUDE or EXCLUDE"
        VARCHAR description
        TIMESTAMP created_at
        TIMESTAMP updated_at
        VARCHAR created_by
        VARCHAR updated_by
    }

    RESOURCE_ACL {
        BIGINT id PK
        VARCHAR resource_type "CAR, CLIENT, INQUIRY, RESERVATION, SALE, TRANSACTION"
        BIGINT resource_id
        VARCHAR subject_type "ROLE or EMPLOYEE"
        BIGINT subject_id
        VARCHAR access "READ, WRITE, ADMIN"
        VARCHAR reason
        BIGINT granted_by "Admin employee ID"
        TIMESTAMP created_at
        TIMESTAMP updated_at
        VARCHAR created_by
        VARCHAR updated_by
    }

    EMPLOYEE_PERMISSIONS {
        BIGINT id PK
        BIGINT employee_id FK
        BIGINT permission_id FK
        BIGINT granted_by "Admin employee ID"
        VARCHAR reason
        DATETIME created_at
        DATETIME updated_at
    }

    %% Notifications System
    NOTIFICATION_EVENTS {
        BIGINT id PK
        VARCHAR event_type
        VARCHAR entity_type
        BIGINT entity_id
        JSON payload
        VARCHAR severity "INFO, WARN, CRITICAL"
        DATETIME occurred_at
        DATETIME created_at
    }

    NOTIFICATION_JOBS {
        BIGINT id PK
        BIGINT event_id FK
        VARCHAR recipient_type "EMPLOYEE, CLIENT, ROLE"
        BIGINT recipient_id
        VARCHAR channel "EMAIL, SMS, WHATSAPP, PUSH, IN_APP, WEBHOOK"
        VARCHAR status "PENDING, SCHEDULED, SENT, FAILED, CANCELLED"
        DATETIME scheduled_for
        VARCHAR dedup_key UK
        INT retries
        VARCHAR last_error
        DATETIME sent_at
        DATETIME created_at
        DATETIME updated_at
    }

    NOTIFICATION_DELIVERIES {
        BIGINT id PK
        BIGINT job_id FK
        VARCHAR provider
        VARCHAR provider_message_id
        VARCHAR status "SENT, DELIVERED, BOUNCED, FAILED"
        DATETIME sent_at
        DATETIME delivered_at
        VARCHAR error_message
        DATETIME created_at
    }

    NOTIFICATION_TEMPLATES {
        BIGINT id PK
        VARCHAR name
        VARCHAR channel "EMAIL, SMS, WHATSAPP, PUSH, IN_APP, WEBHOOK"
        VARCHAR locale
        INT version
        VARCHAR subject
        TEXT content
        JSON variables
        BIGINT created_by_employee_id FK
        DATETIME created_at
        DATETIME updated_at
    }

    NOTIFICATION_PREFERENCES {
        BIGINT id PK
        VARCHAR principal_type "EMPLOYEE, CLIENT, ROLE, COMPANY"
        BIGINT principal_id
        VARCHAR event_type
        VARCHAR channel "EMAIL, SMS, WHATSAPP, PUSH, IN_APP, WEBHOOK"
        BOOLEAN enabled
        VARCHAR frequency "IMMEDIATE, DIGEST"
        TIME quiet_hours_start
        TIME quiet_hours_end
        VARCHAR severity_threshold
        DATETIME created_at
        DATETIME updated_at
    }

    NOTIFICATION_PROVIDERS {
        BIGINT id PK
        VARCHAR channel "EMAIL, SMS, WHATSAPP, PUSH, WEBHOOK"
        VARCHAR name
        JSON config
        BOOLEAN is_primary
        INT priority
        BOOLEAN enabled
        DATETIME created_at
        DATETIME updated_at
    }

    NOTIFICATION_DIGESTS {
        BIGINT id PK
        VARCHAR recipient_type "EMPLOYEE, CLIENT, ROLE"
        BIGINT recipient_id
        DATETIME window_start
        DATETIME window_end
        TEXT compiled_content
        VARCHAR channel "EMAIL, SMS, PUSH, IN_APP"
        VARCHAR status "PENDING, SENT"
        DATETIME sent_at
        DATETIME created_at
    }

    FILE_ACCESS_LOGS {
        BIGINT id PK
        VARCHAR file_id FK
        VARCHAR access_type "VIEW, DOWNLOAD, DELETE"
        VARCHAR accessed_by
        VARCHAR ip_address
        TEXT user_agent
        DATETIME accessed_at
    }

    %% Relationships - Cars
    CARS }o--|| CAR_MODELS : "is model"
    CARS }o--o| STORAGE_LOCATIONS : "stored at"
    CARS ||--o{ CAR_INSPECTIONS : "inspected"

    %% Relationships - Motorcycles
    MOTORCYCLES }o--|| MOTORCYCLE_MODELS : "is model"
    MOTORCYCLES }o--o| STORAGE_LOCATIONS : "stored at"
    MOTORCYCLES ||--o{ MOTORCYCLE_INSPECTIONS : "inspected"

    %% Relationships - Inquiries
    INQUIRIES }o--o| CARS : "about car"
    INQUIRIES }o--o| MOTORCYCLES : "about motorcycle"
    INQUIRIES }o--|| CLIENTS : "from"
    INQUIRIES }o--o| EMPLOYEES : "assigned to"

    %% Relationships - Reservations
    RESERVATIONS }o--o| CARS : "reserves car"
    RESERVATIONS }o--o| MOTORCYCLES : "reserves motorcycle"
    RESERVATIONS }o--|| CLIENTS : "by"

    %% Relationships - Sales
    SALES }o--o| CARS : "sells car"
    SALES }o--o| MOTORCYCLES : "sells motorcycle"
    SALES }o--|| CLIENTS : "to"
    SALES }o--|| EMPLOYEES : "by"

    %% Relationships - Financial
    FINANCIAL_TRANSACTIONS }o--o| CARS : "for car"
    FINANCIAL_TRANSACTIONS }o--o| MOTORCYCLES : "for motorcycle"

    %% Relationships - Events
    EVENTS }o--o| CARS : "related to car"
    EVENTS }o--o| MOTORCYCLES : "related to motorcycle"

    %% Relationships - Inspections
    MOTORCYCLE_INSPECTIONS }o--|| EMPLOYEES : "inspected by"

    %% Relationships - Movements
    CAR_MOVEMENTS }o--|| CARS : "tracks car"
    CAR_MOVEMENTS }o--o| STORAGE_LOCATIONS : "from location"
    CAR_MOVEMENTS }o--|| STORAGE_LOCATIONS : "to location"
    CAR_MOVEMENTS }o--o| EMPLOYEES : "moved by"
    
    MOTORCYCLE_MOVEMENTS }o--|| MOTORCYCLES : "tracks motorcycle"
    MOTORCYCLE_MOVEMENTS }o--o| STORAGE_LOCATIONS : "from location"
    MOTORCYCLE_MOVEMENTS }o--|| STORAGE_LOCATIONS : "to location"
    MOTORCYCLE_MOVEMENTS }o--o| EMPLOYEES : "moved by"

    %% Relationships - Tasks
    TASKS }o--o| EMPLOYEES : "assigned to"

    %% Relationships - RBAC
    ROLE_PERMISSIONS }o--|| ROLES : "has"
    ROLE_PERMISSIONS }o--|| PERMISSIONS : "grants"
    EMPLOYEE_ROLES }o--|| EMPLOYEES : "has"
    EMPLOYEE_ROLES }o--|| ROLES : "assigned"
    EMPLOYEES ||--o{ EMPLOYEE_DATA_SCOPES : "has scope"
    EMPLOYEES ||--o{ EMPLOYEE_PERMISSIONS : "direct permission"
    PERMISSIONS ||--o{ EMPLOYEE_PERMISSIONS : "granted to"

    %% Relationships - Notifications
    NOTIFICATION_EVENTS ||--o{ NOTIFICATION_JOBS : "triggers"
    NOTIFICATION_JOBS ||--o{ NOTIFICATION_DELIVERIES : "attempts"
    NOTIFICATION_TEMPLATES }o--o| EMPLOYEES : "created by"

    %% Relationships - File Access
    FILE_METADATA ||--o{ FILE_ACCESS_LOGS : "access tracked"
```

## Key Design Principles

### 1. **Dual Vehicle Support**
- Separate but parallel structures for cars and motorcycles
- Shared entities (Storage, Clients, Employees) support both
- Transaction entities use polymorphic relationships via `vehicle_type` discriminator

### 2. **File Storage Integration**
- **No direct relationships** between entities and file_metadata table
- Files referenced by **UUID file IDs** stored as VARCHAR or TEXT columns
- Single files: VARCHAR(64) for file ID (e.g., `primary_image_id`, `profile_image_id`)
- Multiple files: TEXT for comma-separated file IDs (e.g., `gallery_image_ids`, `document_file_ids`)
- **Benefits:**
  - Simple implementation without junction tables
  - Easy migration from local storage to S3/cloud
  - Centralized file metadata management
  - Flexible URL generation based on storage backend

### 3. **Referential Integrity**
- Foreign key constraints ensure data consistency
- Cascade rules prevent orphaned records
- Check constraints validate business rules
- File IDs are **not** foreign keys (allows flexibility for deleted files)

### 4. **Denormalization for Performance** (Updated)
- Separate model catalogs (car_models, motorcycle_models)
- **Detailed specs merged into main tables** (cars, motorcycles) for better query performance
- **Car features stored as JSON** in cars table for flexibility
- Transaction history preserved independently
- File metadata centralized in file_metadata table

### 5. **Audit Trail**
- All tables include `created_at` and `updated_at` timestamps
- Extends from `BaseEntity` for automatic auditing
- JPA auditing enabled via `@EntityListeners`
- File access logs tracked separately in file_access_logs table

### 6. **Flexible Transactions**
- Inquiries, Sales, Reservations, and Financial Transactions support both vehicles
- `vehicle_type` enum distinguishes car vs motorcycle
- Check constraints ensure only one vehicle reference is set

### 7. **RBAC Integration**
- Role-based permissions apply across all entities
- `employee_data_scopes` allows location/department-based data filtering per employee
- `resource_acl` supports per-resource ACLs for CAR, CLIENT, INQUIRY, RESERVATION, SALE, TRANSACTION
- `employee_permissions` allows custom permissions assigned directly to individual employees
- Data scopes can filter by vehicle type
- Resource ACLs work for both cars and motorcycles

## File Storage Column Summary

### Single Image/File Columns (VARCHAR 64)
| Entity | Column Name | Purpose |
|--------|-------------|---------|
| car_models | model_image_id | Representative model image |
| motorcycle_models | model_image_id | Representative model image |
| cars | primary_image_id | Main/featured car image |
| motorcycles | primary_image_id | Main/featured motorcycle image |
| car_inspections | inspection_report_file_id | PDF inspection report |
| motorcycle_inspections | inspection_report_file_id | PDF inspection report |
| employees | profile_image_id | Employee profile photo |
| clients | profile_image_id | Client profile photo |
| storage_locations | location_image_id | Storage facility photo |

### Multiple Files Columns (TEXT - Comma-separated IDs)
| Entity | Column Name | Purpose |
|--------|-------------|---------|
| cars | gallery_image_ids | Car photo gallery |
| cars | document_file_ids | RC, insurance, etc. |
| motorcycles | gallery_image_ids | Motorcycle photo gallery |
| motorcycles | document_file_ids | RC, insurance, PUC, etc. |
| car_inspections | inspection_image_ids | Inspection photos |
| motorcycle_inspections | inspection_image_ids | Inspection photos |
| clients | document_file_ids | ID proof, address proof |
| sales | sale_document_ids | Invoice, receipt, agreement |
| financial_transactions | transaction_file_ids | Receipts, invoices |
| events | attachment_file_ids | Event-related files |
| inquiries | attachment_file_ids | Inquiry attachments |
| reservations | reservation_document_ids | Deposit receipt, agreement |
| tasks | attachment_file_ids | Task-related files |

## Entity Relationships Summary

| Entity | Related To |
|--------|------------|
| **Cars** | → Car Models, Storage Locations, Inspections, Movements |
| **Motorcycles** | → Motorcycle Models, Storage Locations, Inspections, Movements |
| **Inquiries** | → Cars OR Motorcycles, Clients, Employees |
| **Reservations** | → Cars OR Motorcycles, Clients |
| **Sales** | → Cars OR Motorcycles, Clients, Employees |
| **Financial Transactions** | → Cars OR Motorcycles |
| **Events** | → Cars OR Motorcycles (optional) |
| **Storage Locations** | ← Cars, Motorcycles |
| **Car Movements** | → Cars, Storage Locations, Employees |
| **Motorcycle Movements** | → Motorcycles, Storage Locations, Employees |
| **Employees** | ← Inquiries, Sales, Tasks, Inspections, Employee Roles, Employee Permissions, Employee Data Scopes, Movements |
| **Clients** | ← Inquiries, Reservations, Sales |
| **File Metadata** | Referenced by all entities with file storage columns (no FK); ← File Access Logs |
| **Roles** | ← Role Permissions, Employee Roles |
| **Permissions** | ← Role Permissions, Employee Permissions |
| **Notification Events** | → Notification Jobs → Notification Deliveries |
| **Notification Templates** | Referenced by notification jobs at send time |

## Database Statistics (After Seeding)

### Vehicles
- **Car Models**: ~1,201 models seeded from `car-models.csv` (V16) across 40+ manufacturers
- **Motorcycle Models**: ~361 models seeded from `motorcycle-models.csv` (V17) across 25+ brands
- **Sample Cars**: 20+ cars with complete details
- **Sample Motorcycles**: 15 motorcycles with complete details
- **Vehicle Types**: Sedan, SUV, Hatchback, Motorcycle, Scooter, Sport Bike, Cruiser, Off-Road, Dirt Bike, Touring, Naked, Café Racer
- **Price Coverage**: Ex-showroom prices included for all seeded car and motorcycle models
- **Manufacturers**: Honda, Hero, Yamaha, Royal Enfield, TVS, Bajaj, Suzuki, KTM, Ather, Ola Electric, Toyota, Maruti, Hyundai, Tata, Mahindra, etc.

### File Storage
- **Supported File Types**: IMAGE, PDF, EXCEL, CSV, DOCUMENT, OTHER
- **Max File Sizes**: 10-20 MB depending on type
- **Storage Segregation**: Files organized by type in separate directories
- **File Statuses**: ACTIVE, DELETED, ARCHIVED

### Transactions
- **Fuel Types**: Petrol, Diesel, Electric, CNG, Hybrid
- **Transmission Types**: Manual, Automatic, CVT, AMT
- **Status Types**: AVAILABLE, RESERVED, SOLD, UNDER_INSPECTION, IN_TRANSIT

## S3 Migration Considerations

The current design stores file IDs (UUIDs) rather than direct URLs, making cloud migration straightforward:

### Current Architecture (Local Storage)
```
Entity → File ID (VARCHAR) → File Metadata Table → Local Storage Path
                                                  → Public URL Generation
```

### Future Architecture (S3/Cloud)
```
Entity → File ID (VARCHAR) → File Metadata Table → S3 Storage Path
                                                  → S3 Public URL Generation
```

**Migration Steps:**
1. Update `file_metadata.storage_path` to S3 paths
2. Update `file_metadata.public_url` to S3 URLs
3. Migrate physical files from local storage to S3
4. Update `FileStorageServiceImpl` to use S3 client
5. **No changes needed** in entity files or business logic!

**Benefits:**
- Zero downtime migration possible
- Gradual migration (file by file or type by type)
- Easy rollback if needed
- Application code remains unchanged
- URLs updated automatically via file_metadata table

## Recent Schema Changes (V15–V21)

### V17: Add Ex-Showroom Prices + Drop seating_capacity
**Date:** March 25, 2026

**Changes:**
1. **Added `ex_showroom_price DECIMAL(12,2)` to `car_models` table** (V1 schema)
   - Prices sourced from `car-models.csv` (`Ex-Showroom_Price` column)
   - Format parsed from Indian locale: "Rs. 2,92,667" → 292667
   - Populated via V16 seeder (1,201 rows)

2. **Added `ex_showroom_price DECIMAL(12,2)` to `motorcycle_models` table** (V9 schema)
   - Prices sourced from `motorcycle-models.csv` (`price` column — plain integer)
   - Populated via V17 seeder (361 rows)

3. **Removed `seating_capacity` from `motorcycle_models` table**
   - Column not tracked or relevant for dealership use-case
   - V17 migration runs `ALTER TABLE motorcycle_models DROP COLUMN seating_capacity`

**Benefits:**
- Enables price-based filtering and sorting of model catalogs
- Provides reference pricing for sales comparisons
- Cleaner motorcycle model schema

### V15: Car Table Structure Simplification
**Date:** March 15, 2026

**Changes:**
1. **Merged `car_detailed_specs` into `cars` table**
   - Added: `doors`, `seats`, `cargo_capacity_liters`, `acceleration_0_100`, `top_speed_kmh`
   - Eliminated separate one-to-one relationship
   - Improved query performance (no joins needed)

2. **Merged `car_features` into `cars` table as JSON**
   - Added: `features` (JSON column)
   - Replaced key-value table with flexible JSON storage
   - Simplified feature management

**Benefits:**
- Fewer joins in queries → Better performance
- Simpler codebase (no separate entities/DTOs)
- JSON features allow dynamic attributes
- Single table for complete car information

### V16: Motorcycle Table Structure Simplification
**Date:** March 15, 2026

**Changes:**
1. **Merged `motorcycle_detailed_specs` into `motorcycles` table**
   - Added 21 fields: engine specs, dimensions, braking, suspension, tires, features
   - Eliminated separate one-to-one relationship
   - Consistent with car table structure

**Benefits:**
- Consistent data model between cars and motorcycles
- Improved query performance
- Simplified mapper and service layer code
- Single source of truth for motorcycle data

---

### V20: Split Vehicle Count By Type
**Date:** March 26, 2026

**Changes:**
1. **Replaced `current_vehicle_count` with `current_car_count` and `current_motorcycle_count`** in `storage_locations`
   - Enables separate tracking of cars and motorcycles per location
   - Counts back-filled from live vehicle data on migration
   - Old CHECK constraints (`chk_capacity`, `chk_vehicle_count_positive`) dropped and replaced with type-specific ones

2. **Removed all database triggers** (V19 triggers dropped, none recreated)
   - `trg_cars_after_insert/update/delete` — removed
   - `trg_motorcycles_after_insert/update/delete` — removed
   - Counts are now maintained entirely at the **application/service layer** on vehicle add, delete, status change, and storage location transfer

**Benefits:**
- Eliminates MySQL error 1442 (trigger conflict with `storage_locations` subqueries in seed scripts)
- Finer-grained capacity reporting per vehicle type
- Simpler, more predictable count management in application code

---

### V21: Add Vehicle Description
**Date:** March 26, 2026

**Changes:**
1. **Added `description VARCHAR(600)` to `cars` table**
2. **Resized `description` on `motorcycles` from `TEXT` to `VARCHAR(600)`**
   - 600-character limit enforced via `@Size` validation at the application layer

---

**Last Updated:** March 26, 2026
**Version:** 2.5 (Added description to cars; capped motorcycles description to VARCHAR(600))
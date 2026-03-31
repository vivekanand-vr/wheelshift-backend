# WheelShift Pro — Product Documentation

**Version:** 1.2.0
**Last Updated:** March 31, 2026
**Status:** Production Ready

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Target Users](#2-target-users)
3. [Feature Overview](#3-feature-overview)
4. [Core Features](#4-core-features)
5. [Platform Features](#5-platform-features)
6. [Security & Authentication](#6-security--authentication)
7. [Technology Stack](#7-technology-stack)
8. [Database Overview](#8-database-overview)
9. [API Overview](#9-api-overview)
10. [Planned Features](#10-planned-features)

---

## 1. Introduction

WheelShift Pro is a comprehensive business management platform built for used vehicle dealerships. It covers the complete lifecycle of both cars and motorcycles — from purchase and inspection through inventory management, customer engagement, and final sale — all in one system.

The platform is designed for **multi-user dealership teams**, providing each role with a focused view of the tools and data relevant to their work, while giving management full visibility through powerful dashboards and reports.

---

## 2. Target Users

| Role | What They Do |
|------|--------------|
| **Super Admin** | Full system access — user management, role configuration, system settings |
| **Admin** | Day-to-day business operations, employee oversight, reporting |
| **Sales** | Client interactions, inquiry management, reservations and sales |
| **Inspector** | Vehicle inspection records and condition assessments |
| **Finance** | Financial transactions, pricing, commissions |
| **Store Manager** | Inventory management, storage allocation, vehicle movements |

---

## 3. Feature Overview

### Currently Available

| # | Feature | Category |
|---|---------|----------|
| 1 | Car Inventory Management | Core |
| 2 | Motorcycle Inventory Management | Core |
| 3 | Vehicle Inspections (Cars & Motorcycles) | Core |
| 4 | Vehicle Movement Tracking | Core |
| 5 | Storage Location Management | Core |
| 6 | Client (Customer) Management | Core |
| 7 | Employee Management | Core |
| 8 | Lead Management (Inquiries) | Core |
| 9 | Reservation System | Core |
| 10 | Sales Processing | Core |
| 11 | Financial Transaction Management | Core |
| 12 | Task Management (Kanban) | Core |
| 13 | Event Calendar | Core |
| 14 | File Storage & Management | Platform |
| 15 | Role-Based Access Control (RBAC) | Platform |
| 16 | In-App Notification System | Platform |
| 17 | Role-Specific Dashboards | Platform |
| 18 | Performance Caching (Redis) | Platform |
| 19 | JWT Authentication | Security |
| 20 | Data Scoping | Security |
| 21 | Resource-Level ACLs | Security |
| 22 | Audit Logging | Security |
| 23 | AI Similar Vehicle Recommendations | AI / Platform |

### Planned

| # | Feature | Category | Status |
|---|---------|----------|--------|
| 24 | Frontend Web Application (React/Next.js) | Product | 🚧 In Progress |
| 25 | Email Notifications (SMTP) | Platform | 📋 Planned |
| 26 | Real-Time Updates (WebSocket) | Platform | 📋 Planned |
| 27 | Export Reports (PDF / Excel) | Platform | 📋 Planned |
| 28 | Two-Factor Authentication (2FA) | Security | 📋 Planned |
| 29 | OAuth2 / Social Login | Security | 📋 Planned |
| 30 | QR Code Generation for Vehicles | Core | 💡 Suggested |
| 31 | Customer Self-Service Portal | Core | 💡 Suggested |
| 32 | Bulk Import / Export (CSV) | Core | 💡 Suggested |
| 33 | AI-Powered Pricing Recommendations | Advanced | 💡 Suggested |
| 34 | Vehicle Price History Tracking | Core | 💡 Suggested |
| 35 | Appointment Booking System | Core | 💡 Suggested |
| 36 | Mobile App (iOS / Android) | Product | 💡 Suggested |
| 37 | Multi-Tenancy (Multiple Dealerships) | Platform | 💡 Suggested |
| 38 | WhatsApp Chatbot Integration | Platform | 💡 Suggested |

---

## 4. Core Features

### 4.1 Car Inventory Management

Track every car from purchase to sale.

- Add cars with full details — VIN, registration, model, year, color, mileage, engine specs
- Manage status: Available, Reserved, Sold, Under Inspection, In Transit
- Assign cars to storage locations with live capacity validation
- Track purchase price, selling price, and margin
- Store extended specifications: doors, seats, cargo capacity, acceleration, top speed
- Add features as a flexible list (e.g., sunroof, ABS, parking sensors)
- Attach photos (primary + gallery) and documents (RC, insurance)
- Advanced search and filtering by make, model, price range, status, location
- Full history trail — inspection records, movement logs, financial transactions

### 4.2 Motorcycle Inventory Management

Full inventory management for 2-wheelers with motorcycle-specific attributes.

- All the same lifecycle tracking as cars, tailored for motorcycles
- Motorcycle-specific fields: engine number, chassis number, pollution certificate expiry, insurance expiry
- Accident history and finance status tracking, previous owners count
- Vehicle categories: Motorcycle, Scooter, Sport Bike, Cruiser, Off-Road, Touring, Naked, Cafe Racer
- Fuel types: Petrol, Electric, Hybrid
- Engine specs: type, power (BHP), torque (Nm), cooling system, fuel tank capacity
- Dimensions: length, width, height, wheelbase, ground clearance, kerb weight
- Braking: front/rear brake type, ABS availability
- Suspension: front and rear suspension type
- Features: electric start, kick start, digital console, USB charging, LED lights
- 80+ pre-loaded models across 10+ brands:
  - Honda, Hero, Yamaha, Royal Enfield, TVS, Bajaj, Suzuki, KTM, Ather, Ola Electric

### 4.3 Vehicle Inspections

Structured condition assessment records for both cars and motorcycles.

- Log condition across multiple categories (exterior, interior, mechanical, electrical, engine, brakes, tyres, suspension, body, etc.)
- Record accident history and required repairs with cost estimates
- Pass/fail status per inspection
- Attach inspection photos and upload report PDF
- Full inspection history per vehicle with timestamps

### 4.4 Vehicle Movement Tracking

Know where every vehicle is and how it got there.

- Log each time a vehicle moves between storage locations
- Record the employee who performed the move and the date/time
- Optional movement notes
- Full movement history per vehicle

### 4.5 Storage Location Management

Manage multiple physical locations where vehicles are stored.

- Add facilities with address and contact information
- Set total capacity and track real-time occupancy
- Capacity automatically adjusts as vehicles are assigned or moved
- Vehicles cannot be assigned to locations at full capacity
- Attach a photo of the facility
- View all vehicles currently at any location

### 4.6 Client (Customer) Management

Centralized customer database with complete interaction history.

- Store client details: name, email, phone, location
- Automatic tracking of purchase count and last purchase date
- View all inquiries, reservations, and sales linked to a client
- Client status management — Active / Inactive
- Store profile photo and documents (ID proof, address proof)
- Advanced filtering by name, status, and location
- Identify top buyers and segment customers

### 4.7 Employee Management

Manage your dealership team and their system access.

- Employee profiles with department, position, and join date
- Status management — Active, Inactive, Suspended
- Assign roles for access control
- Track last login activity
- Profile photo storage
- Prevent accidental deletion of employees with active work assignments

### 4.8 Lead Management (Inquiries)

Capture and track every customer inquiry from first contact to sale.

- Record inquiries for cars or motorcycles
- Categorize by inquiry type and assign to a sales employee
- Status workflow: Open → In Progress → Responded → Closed
- Log responses with timestamps
- Reassign inquiries between employees
- Filter by status, employee, vehicle, or client
- Attach files (brochures, quotes, etc.)

### 4.9 Reservation System

Hold vehicles for interested customers with deposit management.

- Create reservations with expiry dates
- Record deposit amount and payment status
- Status workflow: Pending → Confirmed → Expired → Cancelled
- Background scheduler automatically detects and expires overdue reservations
- One active reservation per vehicle enforced at all times
- Vehicle status automatically updates on reservation changes
- Convert a confirmed reservation directly to a sale
- Attach reservation documents (deposit receipts, agreements)

### 4.10 Sales Processing

Record and manage every completed vehicle sale.

- Record sale price, payment method, and handling employee
- Automatic commission calculation based on a configurable rate
- Vehicle status automatically moves to SOLD
- Client purchase count automatically updated on sale
- Attach sale documents (invoice, receipt, agreement)
- Date-range filtering and sales analytics
- Commission summaries per employee

### 4.11 Financial Transaction Management

Track all income and expenses linked to vehicles.

- Log transactions by category: Purchase, Sale, Repair, Insurance, Maintenance, and more
- Link every transaction to a specific car or motorcycle
- Attach vendor details and receipt/invoice files
- Filter by type, date range, or vehicle
- Per-vehicle profitability view
- Expense summaries and financial reporting

### 4.12 Task Management

Assign and track internal work with a Kanban-style workflow.

- Create tasks with title, description, priority (Low / Medium / High / Critical), and due date
- Assign tasks to any employee
- Status workflow: To Do → In Progress → Review → Done
- Tags for grouping and filtering
- Overdue task detection and alerts
- Attachment support
- Employee workload overview

### 4.13 Event Calendar

Schedule and manage business events and appointments.

- Create events with type, title, date, and start/end time
- Optionally link an event to a specific car or motorcycle
- Event types: Inspection, Test Drive, Sale, Reservation, Meeting, Servicing, and more
- Attach files and documents to events
- Notification integration for event reminders

### 4.14 AI Similar Vehicle Recommendations

Hybrid AI-powered similarity engine that surfaces vehicles a customer is likely to be interested in.

- Retrieve up to 20 similar cars or motorcycles for any vehicle in inventory
- Powered by the WheelShift AI Service (FastAPI microservice) using hybrid similarity scoring — combines structured attributes (make, model, year, price, mileage) with semantic embeddings
- Results enriched server-side with live DB data: primary image URL, storage location name, and current status
- Graceful degradation — returns an empty list with a warning flag when the AI service is unavailable, so inventory pages never break
- Cached results propagated from the AI service layer for low-latency repeated requests
- Configurable result limit (`limit` query param, 1–20, default 5)
- Requires authentication; available to all authenticated roles

---

## 5. Platform Features

### 5.1 File Storage & Management

Centralized file management for everything in the system.

- Upload images, PDFs, Word documents, Excel files, and CSVs
- Each file gets a unique UUID for safe referencing independent of storage location
- File status: Active, Archived, Deleted
- Track who uploaded a file, when, and from where
- File access logs — every view, download, or delete is recorded
- Storage architecture supports seamless migration to cloud (AWS S3) without changing business logic
- Every entity in the system supports file attachments where relevant

### 5.2 Role-Based Access Control (RBAC)

Layered access control that ensures each person sees and does only what they should.

**6 Built-in Roles:**

| Role | Access Level |
|------|-------------|
| SUPER_ADMIN | All permissions, unrestricted |
| ADMIN | Employee management, reporting, location management |
| SALES | Inquiries, reservations, sales, client interactions |
| INSPECTOR | Vehicle inspections, movements |
| FINANCE | Financial transactions, sales reporting, commissions |
| STORE_MANAGER | Vehicle inventory, storage, movements, tasks |

**Permission System:**
- 40+ individual permissions in `resource:action` format
- Roles can be assigned multiple permissions
- Employees can have multiple roles
- Custom per-employee permissions for exceptional cases (independent of roles)

**Data Scopes** — Control what data each employee can see:
- **Location Scope** — Restrict visibility to specific storage locations
- **Department Scope** — Filter to department-relevant records
- **Assignment Scope** — Show only records personally assigned to the employee

**Resource-Level ACLs** — Per-record access control for sensitive data:
- Override role permissions for an individual car, client, inquiry, or sale
- Grant READ, WRITE, or ADMIN access per record per employee or role

📖 See [RBAC Guide](rbac/RBAC_USAGE_GUIDE.md) for full details.

### 5.3 Notification System

Automated, event-driven notifications to keep the team informed.

**Currently Available:**
- In-app notifications with badge counts and read/unread state
- Per-employee preferences — opt in or out per event type
- Quiet hours support — no notifications outside configured working hours
- Digest mode — batch notifications into periodic summaries

**Planned Channels:** Email (SMTP)

**Automatic Notification Triggers:**
- Inquiry assigned to an employee
- Reservation created or about to expire
- Sale completed
- Task assigned or overdue
- Vehicle inspection due
- Storage location nearing full capacity

**Template System:**
- Reusable message templates with variable placeholders (e.g., `{{clientName}}`, `{{vehicleModel}}`)
- Templates are channel-specific and versioned
- Full delivery tracking with retry on failure

📖 See [Notifications Guide](features/notifications/README.md) for full details.

### 5.4 Role-Specific Dashboards

Every role gets a dashboard showing the metrics most relevant to their work.

| Role | Key Metrics |
|------|-------------|
| **Admin** | Total vehicles, revenue, employee performance, low-stock alerts, recent activity |
| **Sales** | Personal sales, active inquiries, assigned reservations, commission tracking, conversion rates |
| **Inspector** | Pending inspections, failed inspection alerts, repair cost analysis |
| **Finance** | Revenue and profit summary, expense breakdown, commission summaries |
| **Store Manager** | Inventory by location, storage utilization, vehicle movements |

All dashboards support date-range filtering and are cached for fast load times.

📖 See [Dashboard Guide](features/dashboard/README.md) for full details.

### 5.5 Performance Caching

A caching layer powered by Redis keeps the platform fast as data grows.

- Dashboards, inventory lists, and reference data are cached with per-region TTLs
- Cache is automatically invalidated when related data changes
- Significantly faster page loads for read-heavy views
- Supports Redis Cluster for high-availability deployments

📖 See [Redis Caching Guide](caching/REDIS_CACHING_GUIDE.md) for full details.

---

## 6. Security & Authentication

### JWT Authentication

WheelShift Pro uses **JSON Web Token (JWT)** based authentication.

1. Employee submits email and password to `/api/v1/auth/login`
2. Credentials are verified and a signed JWT is returned
3. The client includes the token via `Authorization: Bearer <token>` on every request
4. The token carries the employee's identity and roles — no server-side session required
5. Token expiry is configurable (default: 8 hours)
6. On logout, the client discards the token

### Password Security

- Passwords are stored as BCrypt hashes — never in plain text
- Password strength requirements enforced at registration

### Authorization Layers

Access is evaluated in order, and denied if no rule grants it:

1. **Super Admin override** — always granted full access
2. **Resource ACL** — explicit per-record access rules
3. **Data scope** — location / department / assignment filters
4. **Role permissions** — role-based permission evaluation
5. **Default deny** — access denied if no rule matches

### What's Protected

- Parameterized queries throughout (no SQL injection risk)
- All inputs validated at the API boundary
- HTTPS enforced in production
- All secrets (DB password, JWT key) via environment variables — never hardcoded
- System roles are immutable and cannot be deleted or downgraded
- Super admin account is protected from modification by other admins

### Planned Security Enhancements

- Two-Factor Authentication (2FA) via TOTP
- OAuth2 / Social login (Google, Microsoft)
- Per-endpoint API rate limiting
- IP-based access restrictions for admin endpoints

---

## 7. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 4.0.1 |
| **Database** | MySQL | 8.0+ |
| **Cache** | Redis | 7.x |
| **Authentication** | JWT (jjwt) | 0.12.x |
| **ORM** | Hibernate / JPA | 6.x |
| **Schema Migrations** | Flyway | 9.x |
| **Build tool** | Maven | 3.9+ |
| **Containerization** | Docker + Docker Compose | — |
| **API Documentation** | SpringDoc OpenAPI (Swagger UI) | 2.7.0 || **AI Integration** | WheelShift AI Service (WebClient / FastAPI) | — || **Frontend** *(Planned)* | React + Next.js | — |

---

## 8. Database Overview

The database has **30+ tables** managed through Flyway versioned migrations (V1–V16).

| Group | Tables |
|-------|--------|
| **Cars** | `car_models`, `cars`, `car_inspections`, `car_movements` |
| **Motorcycles** | `motorcycle_models`, `motorcycles`, `motorcycle_inspections`, `motorcycle_movements` |
| **Operations** | `storage_locations`, `clients`, `employees`, `inquiries`, `reservations`, `sales`, `financial_transactions`, `tasks`, `events` |
| **RBAC** | `roles`, `permissions`, `role_permissions`, `employee_roles`, `employee_permissions`, `employee_data_scopes`, `resource_acl` |
| **Notifications** | `notification_events`, `notification_jobs`, `notification_deliveries`, `notification_templates`, `notification_preferences`, `notification_providers`, `notification_digests` |
| **Files** | `file_metadata`, `file_access_logs` |

📊 Full Entity-Relationship diagram: [DATABASE_DESIGN.md](DATABASE_DESIGN.md)

---

## 9. API Overview

All endpoints are under `/api/v1/` and require a `Authorization: Bearer <token>` header (except `/auth/login`).

| Category | Base Path | Description |
|----------|-----------|-------------|
| **Authentication** | `/api/v1/auth` | Login, logout, current user |
| **Car Models** | `/api/v1/car-models` | Car model catalog |
| **Cars** | `/api/v1/cars` | Car inventory |
| **Motorcycle Models** | `/api/v1/motorcycle-models` | Motorcycle model catalog |
| **Motorcycles** | `/api/v1/motorcycles` | Motorcycle inventory |
| **Car Inspections** | `/api/v1/car-inspections` | Car inspection records |
| **Motorcycle Inspections** | `/api/v1/motorcycle-inspections` | Motorcycle inspection records |
| **Storage Locations** | `/api/v1/storage-locations` | Facility management |
| **Clients** | `/api/v1/clients` | Customer profiles |
| **Employees** | `/api/v1/employees` | Staff management |
| **Inquiries** | `/api/v1/inquiries` | Lead tracking |
| **Reservations** | `/api/v1/reservations` | Vehicle holds |
| **Sales** | `/api/v1/sales` | Sale transactions |
| **Financial Transactions** | `/api/v1/financial-transactions` | Expense and income records |
| **Tasks** | `/api/v1/tasks` | Task management |
| **Events** | `/api/v1/events` | Calendar events |
| **Files** | `/api/v1/files` | File upload and retrieval |
| **RBAC** | `/api/v1/rbac/*` | Roles, permissions, scopes, ACLs |
| **Notifications** | `/api/v1/notifications/*` | Notification management and preferences |
| **Dashboard** | `/api/v1/dashboard/*` | Role-specific dashboard data |
| **Recommendations** | `/api/v1/recommendations/*` | AI-powered similar vehicle recommendations |

Interactive API documentation: `http://localhost:8080/api/v1/swagger-ui.html`

---

## 10. Planned Features

### In Progress

| Feature | Details |
|---------|---------|
| **Frontend Application** | React + Next.js web app covering all roles and dashboards |
| **Test Coverage >80%** | Expanding the automated test suite |
| **AI Service (Extended)** | Additional AI capabilities — smart pricing, lead scoring, inventory health, demand forecasting |

📖 See [AI Service Overview](AI_SERVICE_OVERVIEW.md) for architecture and implementation plan.

### Planned (Committed)

| Feature | Details |
|---------|---------|
| **Email Notifications** | SMTP-based email delivery for all notification events |
| **Real-Time Updates** | WebSocket-based live updates for dashboards and notifications |
| **Export Reports** | Download dashboards and reports as PDF or Excel |
| **Two-Factor Authentication** | TOTP-based 2FA for all employee accounts |
| **OAuth2 / Social Login** | Login with Google or Microsoft work accounts |
| **API Rate Limiting** | Per-endpoint throttling to prevent abuse |
| **Integration Tests** | Full end-to-end API test suite |

### Suggested (Under Consideration)

| Feature | Details |
|---------|---------|
| **QR Code for Vehicles** | Generate scannable QR codes linking directly to a vehicle profile |
| **Customer Portal** | Allow clients to view their inquiries, reservations, and purchase history |
| **Bulk Import / Export (CSV)** | Import inventory and client data from CSV; export records to CSV/Excel |
| **Appointment Booking** | Let clients schedule test drives or service appointments online |
| **Vehicle Price History** | Log and display every price change made to a vehicle over its lifetime |
| **Mobile App** | Native iOS and Android app for field use by sales and inspection staff |
| **Multi-Tenancy** | Run multiple dealership branches from a single installation with data isolation |
| **WhatsApp Chatbot** | Automated chatbot for client inquiries, reservation status, and updates |
| **Predictive Maintenance Alerts** | Alert staff when a vehicle is likely due for servicing based on mileage/age |
| **CRM Integrations** | Sync leads and clients with Salesforce, HubSpot, or similar platforms |
| **Accounting Integrations** | Push financial data to QuickBooks, Xero, or Tally |
| **VIN Auto-Fill** | Fetch vehicle specs automatically from a VIN lookup service |
| **AI Smart Pricing** | Suggest optimal selling prices based on historical sales, condition, and market trends (AI Service) |
| **AI Lead Scoring** | Automatically score and prioritize inquiries by conversion likelihood (AI Service) |
| **AI Inventory Health Score** | Flag slow-moving vehicles with suggested actions before they stall too long (AI Service) |
| **AI Vehicle Description Generator** | Auto-generate ready-to-publish listing descriptions from vehicle specs (AI Service) |
| **AI Demand Forecasting** | Predict which vehicle categories will sell well next quarter to guide purchasing (AI Service) |
| **AI Transaction Anomaly Detection** | Flag unusual financial patterns like underpriced sales or suspicious discounts (AI Service) |

---

## Appendix: Glossary

| Term | Definition |
|------|------------|
| **VIN** | Vehicle Identification Number — unique 17-character code identifying a vehicle |
| **JWT** | JSON Web Token — a signed, self-contained token used for stateless authentication |
| **RBAC** | Role-Based Access Control — permissions assigned and enforced through roles |
| **ACL** | Access Control List — per-record access rules that can override role permissions |
| **Data Scope** | Restriction on which records an employee can see (by location, department, or assignment) |
| **API** | Application Programming Interface — the backend endpoints clients communicate with |
| **KPI** | Key Performance Indicator — a measurable business metric displayed on dashboards |
| **TTL** | Time To Live — how long a cached value is kept before it is refreshed |
| **Flyway** | Database migration tool that applies and tracks versioned SQL schema changes |

---

**Document Version:** 1.2.0
**Last Updated:** March 31, 2026
**Next Review:** June 2026

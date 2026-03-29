# WheelShift Pro — Backend

> Production-ready Spring Boot backend for a used-vehicle trading enterprise.
> Covers the full lifecycle of a sale: inventory, inspections, reservations, clients,
> employees, financials, tasks, events, and a multi-channel notification system —
> all secured by a fine-grained RBAC layer.

---

## Quick Start

### Option 1 — Full Docker (Recommended)

```bash
git clone <repository-url>
cd wheelshift-backend

# Start MySQL, Redis, Kafka, Kafka UI, phpMyAdmin, Redis Commander
docker-compose -f docker-compose-dev.yml up -d

# Follow application logs
docker-compose -f docker-compose-dev.yml logs -f ws-pro

# Tear down (keeps data)
docker-compose -f docker-compose-dev.yml down

# Tear down + wipe all data
docker-compose -f docker-compose-dev.yml down -v
```

**Dev services:**

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Actuator / Health | http://localhost:8080/actuator/health |
| phpMyAdmin | http://localhost:8081 |
| Redis Commander | http://localhost:8082 |
| Kafka UI | http://localhost:8083 |
| Redis Insights | http://localhost:5540 |

---

### Option 2 — Local (IDE / Terminal)

**Prerequisites:** Java 17, Maven 3.9+, Docker

```bash
# 1. Start infrastructure only
docker-compose -f docker-compose-dev.yml up -d mysql redis kafka

# 2. Set environment variables
#    IntelliJ: EnvFile plugin → add .env to Run Configuration
#    Terminal:
export $(cat .env | grep -v '^#' | xargs)

# 3. Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Environment Variables

All secrets are injected via environment variables. The `.env` file at the project root
(gitignored — never committed) holds local dev values:

```bash
# Database
DB_URL=jdbc:mysql://localhost:3307/wheelshift_db?...
DB_USERNAME=wheelshift_user
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka
# localhost:9092 maps to the broker's EXTERNAL listener when running outside Docker
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT  (generate with: openssl rand -hex 32)
JWT_SECRET=change_this_in_production
JWT_EXPIRATION_MS=86400000

# AWS S3 (leave blank to use local file storage)
AWS_ACCESS_KEY=
AWS_SECRET_KEY=
```

Never hardcode secrets in `application.properties` or any committed file.

---

## Technology Stack

| Category | Technology |
|----------|------------|
| Framework | Spring Boot 4.x |
| Language | Java 17 |
| Build | Maven 3.9+, JaCoCo, OWASP dependency-check |
| Database | MySQL 8.0 |
| ORM / Schema | Spring Data JPA (Hibernate) + Flyway |
| Mapping | MapStruct 1.5.5 + Lombok |
| Security | Spring Security 6, JWT (JJWT 0.12) |
| Caching | Redis 7 (Spring Cache) || Async Messaging | Apache Kafka 4.x, KRaft mode (spring-kafka) |
| Real-time Push | Server-Sent Events (SSE) || Scheduling | `@Scheduled` + ShedLock (Redis distributed lock) |
| Observability | Spring Actuator, Micrometer / Prometheus |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Resilience | Resilience4j |
| Testing | JUnit 5, Testcontainers, ArchUnit, REST Assured |
| Logging | Logback (console + file; JSON via Logstash encoder in prod) |
| Containers | Docker, Docker Compose |

---

## Features

### Core Business
- **Vehicle Inventory** — full car & motorcycle lifecycle (available → reserved → sold)
- **Client Management** — profiles, purchase history, status tracking
- **Employee Management** — staff profiles, roles, performance metrics
- **Lead Management** — inquiry tracking and conversion pipeline
- **Reservation System** — vehicle holds with deposit management; auto-expiry via scheduler
- **Sales Processing** — transaction recording, commission tracking
- **Financial Management** — transaction ledger and reporting
- **Storage Locations** — multi-facility management with capacity tracking
- **Inspections** — car and motorcycle inspection records
- **Task Management** — task assignment, priorities, Kanban-style workflow
- **Event Calendar** — appointments and event scheduling

### Role-Based Dashboards
Five tailored dashboard views: Admin, Sales, Inspector, Finance, Store Manager.

### Security & RBAC
- 6 built-in roles: `SUPER_ADMIN`, `ADMIN`, `SALES`, `STORE_MANAGER`, `INSPECTOR`, `FINANCE`
- 40+ fine-grained permissions in `resource:action` format
- Data scoping (location / department-based filtering)
- Resource-level ACLs for per-record access control
- Stateless JWT authentication

### Notifications
- Multi-channel: In-App, Email, SMS, WhatsApp, Push, Webhook
- Template engine with variable substitution
- Per-employee channel preferences
- **Delivery rules** — opt-out preferences, severity threshold filtering, quiet hours (auto-reschedule), digest batching (hourly scheduler)
- **Async event-driven delivery via Apache Kafka** — producer publishes `NotificationJobMessage` to `notification.jobs.inapp` / `notification.jobs.email` topics; consumer processes with manual acknowledgement
- **Real-time push via SSE** — consumers forward processed jobs to Redis Pub/Sub; a `MessageListener` picks them up and pushes to all active `SseEmitter` connections for the recipient
- Stream endpoint: `GET /api/v1/notifications/stream/{recipientType}/{recipientId}` (`text/event-stream`)
- Typed event catalogue via `NotificationEventType` enum (e.g. `INQUIRY_ASSIGNED`, `TASK_DUE`, `SALE_COMPLETED`)
- Status tracking per delivery attempt

---

## API Reference

**Base URL:** `http://localhost:8080/api/v1`
**Interactive docs:** http://localhost:8080/swagger-ui.html

| Group | Base Path | Controller |
|-------|-----------|------------|
| Auth | `/auth` | `AuthController` |
| Cars | `/cars` | `CarController` |
| Car Models | `/car-models` | `CarModelController` |
| Car Inspections | `/car-inspections` | `CarInspectionController` |
| Motorcycles | `/motorcycles` | `MotorcycleController` |
| Motorcycle Models | `/motorcycle-models` | `MotorcycleModelController` |
| Motorcycle Inspections | `/motorcycle-inspections` | `MotorcycleInspectionController` |
| Clients | `/clients` | `ClientController` |
| Employees | `/employees` | `EmployeeController` |
| Sales | `/sales` | `SaleController` |
| Reservations | `/reservations` | `ReservationController` |
| Financial Transactions | `/financial-transactions` | `FinancialTransactionController` |
| Storage Locations | `/storage-locations` | `StorageLocationController` |
| Tasks | `/tasks` | `TaskController` |
| Events | `/events` | `EventController` |
| Inquiries | `/inquiries` | `InquiryController` |
| Audit Logs | `/audit-logs` | `AuditLogController` |
| Files | `/files` | `FileStorageController` |
| Dashboards | `/dashboard` | `DashboardController` |
| Notifications | `/notifications` | `NotificationController` |
| Notification Preferences | `/notifications/preferences` | `NotificationPreferenceController` |
| Notification Templates | `/notifications/templates` | `NotificationTemplateController` |
| Roles | `/rbac/roles` | `RoleController` |
| Permissions | `/rbac/permissions` | `PermissionController` |
| Employee Roles | `/rbac/employees/{id}/roles` | `EmployeeRoleController` |
| Employee Permissions | `/rbac/employee-permissions` | `EmployeePermissionController` |
| Resource ACLs | `/rbac/acl` | `ResourceACLController` |
| Data Scopes | `/rbac/employees/{id}/scope` | `DataScopeController` |

---

## Database

Schema is managed by **Flyway** — migrations run automatically on startup.

| Version | Description |
|---------|-------------|
| V1 | Initial schema |
| V2 | Seed data |
| V3 | Column type fixes |
| V4 | RBAC tables |
| V5 | RBAC seed data |
| V6 | Notifications tables |
| V7 | Assign employee roles |
| V8 | Fix roles + super admin |
| V9 | Motorcycle tables |
| V10 | Motorcycle movements |
| V11 | Employee custom permissions |
| V12 | File storage tables |
| V13 | File storage columns |
| V14 | Merge car-related tables |
| V15 | Merge motorcycle-related tables |
| V16 | Seed car models from dataset |
| V17 | Seed motorcycle models from dataset |
| V18 | Align notification event types to enum |
| V19 | Storage location triggers |
| V20 | Split vehicle count by type |
| V21 | Add vehicle description |
| V22 | Audit logs table |
| V23 | Notification digest support (compound index) |

Migration files: `src/main/resources/db/migration/`
Rollback scripts (manual): `src/main/resources/db/rollbacks/`

**Never modify a committed migration.** Create a new `V{n+1}__...sql` for any schema change.

### Running Migrations Manually

Migrations run automatically on every app startup. The Flyway Maven plugin is pre-configured in `pom.xml` and reads `DB_USERNAME` / `DB_PASSWORD` from your environment, so you can run any Flyway goal with a single command:

```bash
# Source your credentials first (once per shell session)
export $(cat .env | grep -v '^#' | xargs)   # Linux/macOS
# or load .env via your IDE's EnvFile plugin on Windows

# Apply all pending migrations
mvn flyway:migrate

# Check current migration status
mvn flyway:info

# Validate applied migrations match the local scripts
mvn flyway:validate

# Repair the schema history table (use after fixing a failed migration)
mvn flyway:repair
```

To target a different host or database, override only what you need:

```bash
mvn flyway:migrate -Dflyway.url=jdbc:mysql://other-host:3306/wheelshift_db
```

> Ensure the MySQL container is up (`docker-compose -f docker-compose-dev.yml up -d mysql`) before running any Flyway commands.

---

## Testing

```bash
# Full build: compile → test → coverage check → architecture rules
mvn clean install

# Tests only
mvn test

# Skip tests for fast iteration
mvn install -DskipTests

# Architecture rules only
mvn test -Dtest=ArchitectureRulesTest

# Coverage report → open target/site/jacoco/index.html after running
mvn test jacoco:report
```

`ArchitectureRulesTest` enforces layered architecture rules on every build via ArchUnit.
A failing architecture test is a **build failure** — fix the architecture, never skip the test.

---

## Development Status

| Module | Status |
|--------|--------|
| Core Business APIs | ✅ Complete |
| RBAC System | ✅ Complete |
| Notification System | ✅ Complete |
| Notification Delivery Rules | ✅ Complete |
| Kafka Async Delivery | ✅ Complete |
| SSE Real-time Push | ✅ Complete |
| Dashboard System | ✅ Complete |
| Redis Caching | ✅ Complete |
| Task Management | ✅ Complete |
| JWT Authentication | ✅ Complete |
| Swagger / OpenAPI | ✅ Complete |
| Error Handling | ✅ Complete |
| Audit Logging | ✅ Complete |
| File Storage (Local + S3) | ✅ Complete |
| Database Migrations (V23) | ✅ Complete |
| Docker Setup | ✅ Complete |
| Observability (Actuator + Prometheus) | ✅ Complete |
| Request Correlation (X-Request-Id) | ✅ Complete |
| Distributed Scheduling (ShedLock) | ✅ Complete |
| Architecture Tests (ArchUnit) | ✅ Complete |
| Unit Tests | 🔄 In Progress |
| Integration Tests (Testcontainers) | 📋 Planned |
| End-to-End Tests (REST Assured) | 📋 Planned |

---

## Documentation

| Doc | Description |
|-----|-------------|
| [CONTRIBUTING.md](CONTRIBUTING.md) | Architecture diagrams, folder conventions, how to add features and migrations |
| [docs/BUSINESS_LOGIC.md](docs/BUSINESS_LOGIC.md) | Complete business logic specification for all operations |
| [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md) | Implementation and test coverage status tracker |
| [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md) | Entity-relationship diagrams and database schema |
| [docs/AI_SERVICE_OVERVIEW.md](docs/AI_SERVICE_OVERVIEW.md) | AI service architecture and implementation plan |
| [docs/ARCHITECTURE_REVIEW.md](docs/ARCHITECTURE_REVIEW.md) | Architecture review, design patterns, improvement roadmap |
| [docs/PRODUCT_DOCUMENTATION.md](docs/PRODUCT_DOCUMENTATION.md) | Full system and product overview |
| [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) | Detailed development workflow |
| [docs/rbac/RBAC_COMPLETE_GUIDE.md](docs/rbac/RBAC_COMPLETE_GUIDE.md) | RBAC system deep dive |
| [docs/caching/REDIS_CACHING_GUIDE.md](docs/caching/REDIS_CACHING_GUIDE.md) | Redis caching implementation |
| [docs/file-handling/README.md](docs/file-handling/README.md) | File storage and S3 migration |
| [docs/features/notifications/README.md](docs/features/notifications/README.md) | Notification system |
| [docs/features/dashboard/README.md](docs/features/dashboard/README.md) | Dashboard system |
| [docs/features/tasks/README.md](docs/features/tasks/README.md) | Task management |
| Swagger UI | http://localhost:8080/swagger-ui.html *(requires app running)* |

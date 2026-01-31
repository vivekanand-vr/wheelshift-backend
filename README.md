# WheelShift Pro

> A production-ready Spring Boot application for used car trading and inventory management with comprehensive RBAC and notification systems.

## Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd WheelShiftPro

# Start MySQL and Redis with Docker Compose
docker-compose up -d mysql redis

# Build and run the application
./mvnw clean install
./mvnw spring-boot:run

# Access API documentation
http://localhost:8080/api/v1/swagger-ui.html

# Stop services when done
docker-compose down
```

## Features

### Core Business Modules
- ✅ **Vehicle Inventory Management** - Complete car lifecycle tracking
- ✅ **Customer (Client) Management** - Client profiles and purchase history
- ✅ **Employee Management** - Staff tracking and performance metrics
- ✅ **Lead Management (Inquiries)** - Inquiry tracking and conversion
- ✅ **Reservation System** - Vehicle holds with deposit management
- ✅ **Sales Processing** - Transaction recording and commissions
- ✅ **Financial Management** - Transaction tracking and reporting
- ✅ **Storage Locations** - Multi-facility management with capacity tracking
- ✅ **Car Inspections** - Vehicle inspection records and reports
- ✅ **Task Management** - Task assignment and tracking (Kanban board)
- ✅ **Event Calendar** - Appointment and event scheduling
- ✅ **Dashboard System** - Role-based dashboard views for all user types

### Security & System Features
- ✅ **Role-Based Access Control (RBAC)** - Comprehensive permission system
- ✅ **Notification System** - Multi-channel notifications with templates
- ✅ **Redis Caching** - High-performance caching with configurable TTLs
- ✅ **Audit Logging** - Automatic change tracking
- ✅ **File Logging** - Application logging with rotation
- ✅ **Error Handling** - Custom error pages
- ✅ **JWT Authentication** - Secure token-based authentication

> **Note:** Check individual feature documentation in `docs/features/` for detailed implementation guides.

## 🛡️ Security & Authorization

- **6 Built-in Roles** with hierarchical permissions
- **40+ Fine-grained Permissions** (resource:action format)
- **Data Scoping** for location/department-based filtering
- **Resource-level ACLs** for individual access control
- **JWT Authentication** with secure token management

[View RBAC Documentation](docs/features/rbac/README.md)

## 🔔 Notification System

- **Multi-channel Support**: In-App, Email, SMS, WhatsApp, Push, Webhooks
- **Template Engine** with variable substitution
- **User Preferences** per channel
- **Event-driven Architecture** for automatic notifications
- **Delivery Tracking** and status monitoring

[View Notifications Documentation](docs/features/notifications/README.md)
[View Dashboard Documentation](docs/features/dashboard/README.md)

## Technology Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 4.0.1 |
| **Language** | Java 17 |
| **Build Tool** | Maven 3.9+ |
| **Database** | MySQL 8.0+ |
| **ORM** | Spring Data JPA (Hibernate) |
| **Migrations** | Flyway |
| **Mapping** | MapStruct 1.5.5 |
| **Security** | Spring Security + JWT |
| **API Docs** | SpringDoc OpenAPI 2.7.0 |
| **Validation** | Jakarta Bean Validation |
| **Caching** | Redis |
| **Logging** | Logback with file rotation |

## API Overview

Base URL: `http://localhost:8080/api/v1`

**Total Endpoints:** 150+ REST endpoints organized by resource

### Resource Categories

| Category | Endpoints | Description |
|----------|-----------|-------------|
| **Core Business** | 90+ | Vehicle, Client, Employee, Sales, Inquiries, etc. |
| **RBAC & Auth** | 25+ | Authentication, Roles, Permissions, ACLs |
| **Notifications** | 20+ | Notifications, Preferences, Templates |
| **Task & Calendar** | 15+ | Tasks, Events, Calendar operations |

**Interactive API Documentation:** [Swagger UI](http://localhost:8080/api/v1/swagger-ui.html)

## 🗄️ Database Schema

The application uses Flyway for database migrations with a comprehensive schema:

- **Core Tables**: 15 main business entities
- **RBAC Tables**: 6 tables for security and permissions
- **Notification Tables**: 7 tables for notification system
- **Total**: 28+ tables with proper relationships and indexes

Migration files located in: `src/main/resources/db/migration/`

## Configuration

Key configuration in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/wheelshiftpro
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true

# JWT Authentication
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# API Documentation
springdoc.api-docs.path=/api/v1/api-docs
springdoc.swagger-ui.path=/api/v1/swagger-ui.html
```

## Project Structure

```
WheelShiftPro/
├── src/main/java/com/wheelshiftpro/
│   ├── config/          # Application configuration
│   ├── controller/      # REST API controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA entities
│   ├── enums/          # Enumerations
│   ├── exception/      # Exception handling
│   ├── mapper/         # MapStruct mappers
│   ├── repository/     # Spring Data repositories
│   ├── security/       # Security configuration
│   └── service/        # Business logic
├── src/main/resources/
│   ├── db/migration/   # Flyway migrations
│   ├── templates/      # Error page templates
│   ├── application.properties
│   └── logback-spring.xml
└── docs/               # Documentation
    ├── features/       # Feature-specific docs
    └── guides/         # Developer guides
```

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw clean test jacoco:report
```

## Development Status

| Module | Status |
|--------|--------|
| Core Business APIs | ✅ Complete |
| RBAC System | ✅ Complete |
| Notification System | ✅ Complete |
| Dashboard System | ✅ Complete |
| Redis Caching | ✅ Complete |
| Task Management | ✅ Complete |
| JWT Authentication | ✅ Complete |
| Swagger Documentation | ✅ Complete |
| Error Handling | ✅ Complete |
| Audit Logging | ✅ Complete |
| File Logging | ✅ Complete |
| Database Migrations | ✅ Complete |
| Unit Tests | 🚧 In Progress |
| Integration Tests | 📋 Planned |
| End-to-End Tests | 📋 Planned |

**Overall Progress**: 95% Complete

## Documentation

- [Product Documentation](docs/PRODUCT_DOCUMENTATION.md) - Complete system overview and design
- [Developer Guide](docs/guides/DEVELOPER_GUIDE.md) - Setup and development workflow
- [Redis Caching Guide](docs/REDIS_CACHING_GUIDE.md) - Comprehensive caching implementation guide
- [Cache Invalidation Reference](docs/CACHE_INVALIDATION_REFERENCE.md) - Quick reference for cache management
- [RBAC Guide](docs/features/rbac/README.md) - Role-Based Access Control
- [Notifications Guide](docs/features/notifications/README.md) - Notification system
- [Dashboard Guide](docs/features/dashboard/README.md) - Dashboard implementation
- [Tasks Guide](docs/features/tasks/README.md) - Task management system
- [Documentation Standard](docs/features/DOCUMENTATION_STANDARD.md) - How to document features
- [API Reference](http://localhost:8080/api/v1/swagger-ui.html) - Interactive API docs

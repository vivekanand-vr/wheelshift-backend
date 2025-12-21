# WheelShift Pro

> A production-ready Spring Boot application for used car trading and inventory management with comprehensive RBAC and notification systems.

## 🚀 Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd WheelShiftPro

# Configure database (MySQL 8.0+)
# Update src/main/resources/application.properties

# Build and run
./mvnw clean install
./mvnw spring-boot:run

# Access API documentation
http://localhost:8080/api/v1/swagger-ui.html
```

## 📋 Features

### Core Business Modules
- **Vehicle Inventory Management** - Track cars from purchase to sale with complete lifecycle management
- **Customer (Client) Management** - Maintain client profiles, purchase history, and interactions
- **Employee Management** - Staff tracking with role assignments and performance metrics
- **Lead Management (Inquiries)** - Track and convert customer inquiries into sales
- **Reservation System** - Vehicle holds with deposit management and expiration tracking
- **Sales Processing** - Complete sales transaction recording and commission tracking
- **Financial Management** - Track all financial transactions with reporting capabilities
- **Storage Locations** - Manage multiple facilities with capacity tracking
- **Car Inspections** - Vehicle health check records and reports
- **Task Management** - Internal task assignment and tracking system
- **Event Calendar** - Appointment and event scheduling

### Advanced Features
- **Role-Based Access Control (RBAC)** - Hierarchical permissions with data scopes and resource-level ACLs
- **Notification System** - Multi-channel notifications (In-App, Email, SMS, Push) with template engine
- **Audit Logging** - Automatic tracking of all create/update operations
- **File Logging** - Comprehensive application logging with rotation
- **Error Handling** - Custom error pages for 403, 404, 500 and generic errors

## 🛡️ Security & Authorization

WheelShift Pro implements a comprehensive RBAC system:

- **6 Built-in Roles**: Super Admin, Admin, Sales, Inspector, Finance, Store Manager
- **40+ Permissions**: Fine-grained resource:action format (e.g., `cars:read`, `sales:write`)
- **Data Scopes**: Filter data by location, department, or assignment
- **Resource ACLs**: Individual resource-level access control
- **JWT Authentication**: Secure token-based authentication

[📖 RBAC Documentation](docs/features/rbac/README.md)

## 🔔 Notification System

Multi-channel notification system with:

- **Channels**: In-App, Email, SMS, WhatsApp, Push Notifications, Webhooks
- **Templates**: Reusable message templates with variable substitution
- **Preferences**: User-defined notification preferences per channel
- **Event-Driven**: Automatic notifications based on business events
- **Delivery Tracking**: Monitor notification delivery status

[📖 Notifications Documentation](docs/features/notifications/README.md)

## 🏗️ Technology Stack

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

## 📡 API Overview

Base URL: `http://localhost:8080/api/v1`

### Core Resources (120+ Endpoints)

| Resource | Endpoints | Description |
|----------|-----------|-------------|
| `/car-models` | 12 | Car make/model/variant catalog |
| `/cars` | 14 | Vehicle inventory management |
| `/clients` | 11 | Customer management |
| `/employees` | 11 | Staff management |
| `/inquiries` | 12 | Lead tracking |
| `/reservations` | 13 | Vehicle reservations |
| `/sales` | 12 | Sales transactions |
| `/financial-transactions` | 12 | Financial records |
| `/car-inspections` | 10 | Vehicle inspections |
| `/storage-locations` | 8 | Facility management |
| `/tasks` | 13 | Task management |
| `/events` | 11 | Event scheduling |

### RBAC Endpoints

| Resource | Endpoints | Description |
|----------|-----------|-------------|
| `/auth` | 2 | Login and authentication |
| `/roles` | 7 | Role management |
| `/permissions` | 5 | Permission management |
| `/employee-roles` | 3 | Employee role assignments |
| `/data-scopes` | 5 | Data scope management |
| `/resource-acl` | 5 | Resource ACL management |

### Notification Endpoints

| Resource | Endpoints | Description |
|----------|-----------|-------------|
| `/notifications` | 10 | Notification management |
| `/notifications/preferences` | 5 | User preferences |
| `/notifications/templates` | 7 | Template management |

## 🗄️ Database Schema

The application uses Flyway for database migrations with a comprehensive schema:

- **Core Tables**: 15 main business entities
- **RBAC Tables**: 6 tables for security and permissions
- **Notification Tables**: 7 tables for notification system
- **Total**: 28+ tables with proper relationships and indexes

Migration files located in: `src/main/resources/db/migration/`

## 🔧 Configuration

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

# API Documentation
springdoc.api-docs.path=/api/v1/api-docs
springdoc.swagger-ui.path=/api/v1/swagger-ui.html
```

## 📁 Project Structure

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

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw clean test jacoco:report
```

## 📊 Development Status

| Feature | Status |
|---------|--------|
| Core Business APIs | ✅ Complete |
| RBAC System | ✅ Complete |
| Notification System | ✅ Complete |
| JWT Authentication | ✅ Complete |
| Swagger Documentation | ✅ Complete |
| Error Handling | ✅ Complete |
| File Logging | ✅ Complete |
| Database Migrations | ✅ Complete |
| Unit Tests | 🚧 In Progress |
| Integration Tests | 📋 Planned |

**Overall Progress**: 95% Complete

## 📖 Documentation

- [Developer Guide](docs/guides/DEVELOPER_GUIDE.md) - Setup and development workflow
- [RBAC Guide](docs/features/rbac/README.md) - Role-Based Access Control implementation
- [Notifications Guide](docs/features/notifications/README.md) - Notification system usage
- [API Reference](http://localhost:8080/api/v1/swagger-ui.html) - Interactive API documentation

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: add some amazing feature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is proprietary software. All rights reserved.

## 📧 Contact

For questions or support, please contact the development team.

---

**Built with ❤️ using Spring Boot**

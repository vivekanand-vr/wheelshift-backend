# WheelShift Pro - Backend

A production-ready Spring Boot backend for WheelShift Used Car Trading Management System.

## 🏗️ Project Structure

```
src/main/java/com/wheelshiftpro/
├── config/                  # Configuration classes
│   ├── JpaAuditingConfig.java
│   └── OpenApiConfig.java
├── dto/                     # Data Transfer Objects
│   ├── request/            # Request DTOs
│   └── response/           # Response DTOs
├── entity/                  # JPA Entities
│   ├── BaseEntity.java
│   ├── Car.java
│   ├── CarModel.java
│   ├── CarDetailedSpecs.java
│   ├── CarFeature.java
│   ├── CarInspection.java
│   ├── CarMovement.java
│   ├── Client.java
│   ├── Employee.java
│   ├── Event.java
│   ├── FinancialTransaction.java
│   ├── Inquiry.java
│   ├── Reservation.java
│   ├── Sale.java
│   ├── StorageLocation.java
│   └── Task.java
├── enums/                   # Enumerations
│   ├── CarStatus.java
│   ├── ClientStatus.java
│   ├── EmployeeStatus.java
│   ├── FuelType.java
│   ├── InquiryStatus.java
│   ├── PaymentMethod.java
│   ├── ReservationStatus.java
│   ├── TaskPriority.java
│   ├── TaskStatus.java
│   ├── TransactionType.java
│   └── TransmissionType.java
├── exception/               # Exception handling
│   ├── BusinessException.java
│   ├── DuplicateResourceException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── mapper/                  # MapStruct mappers
│   ├── CarMapper.java
│   ├── CarModelMapper.java
│   ├── ClientMapper.java
│   ├── InquiryMapper.java
│   ├── ReservationMapper.java
│   ├── SaleMapper.java
│   └── StorageLocationMapper.java
└── repository/              # Spring Data JPA repositories
    ├── CarRepository.java
    ├── CarModelRepository.java
    ├── CarInspectionRepository.java
    ├── ClientRepository.java
    ├── EmployeeRepository.java
    ├── EventRepository.java
    ├── FinancialTransactionRepository.java
    ├── InquiryRepository.java
    ├── ReservationRepository.java
    ├── SaleRepository.java
    ├── StorageLocationRepository.java
    └── TaskRepository.java

src/main/resources/
├── application.properties
└── db/migration/           # Flyway migrations
    ├── V1__Initial_Schema.sql
    └── V2__Seed_Data.sql
```

## ✅ What's Included

### 1. **Dependencies & Configuration**
- Spring Boot 4.0.1
- Spring Data JPA
- Spring Security (configured)
- MySQL Connector
- Flyway for database migrations
- MapStruct for DTO mapping
- Springdoc OpenAPI (Swagger UI)
- Redis for caching
- Lombok for boilerplate reduction
- Bean Validation

### 2. **Database Design**
- Complete schema with 15 tables
- Foreign key relationships
- Proper indexes for performance
- Check constraints for data integrity
- Audit fields (created_at, updated_at)

### 3. **Entity Models**
- **BaseEntity**: Abstract class with auditing fields
- **CarModel**: Vehicle specifications
- **Car**: Core inventory entity
- **CarDetailedSpecs**: Extended car specifications
- **CarFeature**: Dynamic key-value features
- **CarInspection**: Inspection records
- **CarMovement**: Movement audit trail
- **StorageLocation**: Warehouse management
- **Employee**: Staff management
- **Client**: Customer records
- **Inquiry**: Customer inquiries
- **Reservation**: Vehicle reservations
- **Sale**: Sales transactions
- **FinancialTransaction**: Financial records
- **Task**: Kanban board tasks
- **Event**: Calendar events

### 4. **DTOs**
- Separate Request and Response DTOs
- Validation annotations
- Clean API contracts
- PageResponse for pagination
- ApiResponse wrapper

### 5. **Repositories**
- Spring Data JPA repositories
- Custom query methods
- Complex search queries
- Statistical aggregations
- Optimized with indexes

### 6. **Mappers**
- MapStruct for type-safe mapping
- Entity ↔ DTO conversions
- Handles nested objects
- Custom mapping logic

### 7. **Exception Handling**
- Global exception handler
- RFC7807 Problem Details format
- Custom business exceptions
- Validation error handling
- Standardized error responses

### 8. **API Features**
- Versioning via context path `/api/v1`
- Swagger UI documentation
- Pagination support
- Advanced filtering
- Sorting capabilities

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis (optional, for caching)

### Database Setup
1. Create MySQL database:
```sql
CREATE DATABASE wheelshift CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wheelshift
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Build & Run

```bash
# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run

# Or build JAR and run
mvn clean package
java -jar target/WheelShiftPro-0.0.1-SNAPSHOT.jar
```

### Access Swagger UI
Once the application is running, access the API documentation at:
```
http://localhost:8080/api/v1/swagger-ui.html
```

API Docs JSON:
```
http://localhost:8080/api/v1/api-docs
```

## 📋 Next Steps (To Be Implemented)

### 1. Service Layer
Create service interfaces and implementations:
- `CarModelService`
- `CarService`
- `StorageLocationService`
- `ClientService`
- `EmployeeService`
- `InquiryService`
- `ReservationService`
- `SaleService`
- `FinancialTransactionService`
- `TaskService`
- `EventService`

### 2. REST Controllers
Implement REST endpoints:
- `CarModelController`
- `CarController`
- `StorageLocationController`
- `ClientController`
- `EmployeeController`
- `InquiryController`
- `ReservationController`
- `SaleController`
- `FinancialTransactionController`
- `TaskController`
- `EventController`
- `DashboardController`
- `ReportController`

### 3. Security
- JWT authentication
- Role-based access control (RBAC)
- Password encryption
- Security configuration

### 4. Business Logic
- Capacity management for storage locations
- Car status workflow
- Reservation expiry handling
- Commission calculation
- Financial reporting

### 5. Testing
- Unit tests for services
- Integration tests for controllers
- Repository tests with TestContainers

### 6. Additional Features
- File upload for documents/images
- Email notifications
- Scheduled jobs (expiry checks)
- Caching strategies
- Metrics and monitoring

## 🎯 Key Features

### Data Integrity
- Unique constraints on VIN and registration
- One reservation per car
- One sale per car
- Storage capacity validation
- Foreign key relationships

### Performance
- Strategic indexes on frequently queried columns
- Lazy loading for associations
- Pagination for large datasets
- Redis caching support

### Maintainability
- Clean architecture
- Separation of concerns
- Comprehensive documentation
- Consistent naming conventions

### API Design
- RESTful principles
- Standardized error responses
- Validation at multiple layers
- Swagger/OpenAPI documentation

## 📊 Database Schema Highlights

### Core Relationships
```
CarModel (1) ─── (N) Car ──┬── (1) CarDetailedSpecs
                           ├── (N) CarFeature
                           ├── (N) CarInspection
                           ├── (N) FinancialTransaction
                           ├── (0..1) Reservation
                           └── (0..1) Sale

StorageLocation (1) ─── (N) Car

Employee ──┬── (N) Inquiry (assigned)
           ├── (N) Sale (handled)
           └── (N) Task (assigned)

Client ──┬── (N) Inquiry
         ├── (N) Reservation
         └── (N) Sale
```

### Business Rules Enforced
1. VIN must be exactly 17 characters and unique
2. Car can have only one active reservation
3. Car can have only one sale record
4. Storage location cannot exceed capacity
5. Year must be between 1980 and 2100
6. All financial amounts must be non-negative

## 🔧 Configuration

### Application Properties
Key configurations in `application.properties`:
- Server port and context path
- Database connection
- JPA/Hibernate settings
- Flyway migration settings
- Redis configuration
- JWT settings
- Logging levels
- Swagger UI settings

## 📝 Coding Standards

- **Lombok**: Reduces boilerplate code
- **MapStruct**: Type-safe bean mapping
- **Jakarta Validation**: Request validation
- **JPA Auditing**: Automatic timestamp management
- **Documentation**: JavaDoc on all public methods
- **Naming**: Consistent and descriptive

## 🎨 API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2025-12-20T10:30:00"
}
```

### Error Response (RFC7807)
```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Car not found with id: '123'",
  "instance": "/api/v1/cars/123",
  "code": "RESOURCE_NOT_FOUND",
  "timestamp": "2025-12-20T10:30:00"
}
```

## 📦 Maven Build

```bash
# Compile
mvn compile

# Test
mvn test

# Package
mvn package

# Install
mvn install

# Run Flyway migration
mvn flyway:migrate
```

## 🔐 Security Notes

- Passwords are hashed using BCrypt
- JWT tokens for authentication (to be implemented)
- CORS configuration (to be added)
- Rate limiting (to be implemented)
- Input validation at all layers

## 📚 References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [MapStruct Documentation](https://mapstruct.org/)
- [Springdoc OpenAPI](https://springdoc.org/)

---

**Version**: 1.0.0  
**Last Updated**: December 20, 2025  
**Built with**: ❤️ and ☕

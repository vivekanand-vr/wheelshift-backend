# WheelShift Pro - Development Guide

## Overview

This guide provides a comprehensive reference for building new features and endpoints in WheelShift Pro. It covers the complete development workflow from database design to API implementation, following the established architectural patterns.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Development Workflow](#development-workflow)
3. [Database Layer](#database-layer)
4. [Entity Layer](#entity-layer)
5. [Repository Layer](#repository-layer)
6. [DTO Layer](#dto-layer)
7. [Mapper Layer](#mapper-layer)
8. [Service Layer](#service-layer)
9. [Controller Layer](#controller-layer)
10. [Exception Handling](#exception-handling)
11. [Enums](#enums)
12. [Complete Feature Example](#complete-feature-example)
13. [Best Practices](#best-practices)

---

## Architecture Overview

WheelShift Pro follows a **layered architecture** pattern:

```
┌─────────────────────────────────────┐
│         Controller Layer            │  ← REST API endpoints
├─────────────────────────────────────┤
│          Service Layer              │  ← Business logic
├─────────────────────────────────────┤
│         Repository Layer            │  ← Data access
├─────────────────────────────────────┤
│          Entity Layer               │  ← JPA entities
├─────────────────────────────────────┤
│         Database (MySQL)            │
└─────────────────────────────────────┘

Supporting Components:
├── DTOs (Request/Response)
├── Mappers (Entity ↔ DTO conversion)
├── Enums (Constants and types)
└── Exception Handlers (Error handling)
```

**Key Principles:**
- **Single Responsibility**: Each layer has a specific purpose
- **Separation of Concerns**: Business logic stays in service layer
- **Dependency Injection**: Use Spring's `@Autowired` or constructor injection
- **DTO Pattern**: Never expose entities directly to API
- **MapStruct**: Automated entity-DTO mapping

---

## Development Workflow

### Step-by-Step Process for Adding a New Feature

1. **Define Requirements** → What data and operations are needed?
2. **Create Database Migration** → Define schema with Flyway migration
3. **Create Seed Data (Optional)** → Add initial/test data
4. **Create Entity** → JPA entity representing the table
5. **Create Enums** → Define status codes, types, etc.
6. **Create DTOs** → Request and Response objects
7. **Create Repository** → Data access interface
8. **Create Mapper** → Entity ↔ DTO conversion
9. **Create Service Interface** → Define business operations
10. **Implement Service** → Business logic implementation
11. **Create Controller** → REST API endpoints
12. **Test** → Write unit and integration tests
13. **Document** → Update API documentation and feature docs

---

## Database Layer

### 1. Creating Migrations

**Location:** `src/main/resources/db/migration/`

**Naming Convention:** `V{version}__{Description}.sql`
- `V1__Initial_Schema.sql`
- `V2__Seed_Data.sql`
- `V3__Add_Notifications_Tables.sql`

**Example: Create a New Feature Table**

```sql
-- V9__Add_Warranty_System.sql
-- Warranty tracking system for vehicles

CREATE TABLE warranties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    warranty_type VARCHAR(32) NOT NULL COMMENT 'MANUFACTURER, EXTENDED, DEALER',
    provider_name VARCHAR(128) NOT NULL,
    policy_number VARCHAR(64) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    coverage_details TEXT,
    mileage_limit_km INT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_warranty_car FOREIGN KEY (car_id) 
        REFERENCES cars(id) ON DELETE CASCADE,
    
    -- Unique Constraints
    CONSTRAINT uk_policy_number UNIQUE (policy_number),
    
    -- Check Constraints
    CONSTRAINT chk_dates CHECK (end_date > start_date),
    
    -- Indexes
    INDEX idx_car_id (car_id),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Migration Best Practices:**
- ✅ Use meaningful constraint names (`fk_warranty_car`, not `fk_1`)
- ✅ Add comments for complex fields
- ✅ Create indexes on foreign keys and frequently queried columns
- ✅ Use appropriate data types (VARCHAR vs TEXT, INT vs BIGINT)
- ✅ Set default values where applicable
- ✅ Use check constraints for data validation
- ❌ Never modify existing migrations (create new ones instead)
- ❌ Don't use reserved keywords as column names

### 2. Creating Seed Data

**Location:** Same migration folder, separate file or combined

**Naming:** `V{version}__Seed_{Feature}_Data.sql`

**Example: Seed Initial Warranty Types**

```sql
-- V10__Seed_Warranty_Data.sql
-- Insert sample warranty data for testing

-- Insert warranty templates
INSERT INTO warranty_templates (name, duration_months, mileage_limit_km) VALUES
('Standard Manufacturer 1Y/20K', 12, 20000),
('Standard Manufacturer 3Y/60K', 36, 60000),
('Extended Warranty 5Y/100K', 60, 100000);

-- Insert sample warranties (only for development)
INSERT INTO warranties (car_id, warranty_type, provider_name, policy_number, start_date, end_date, mileage_limit_km, status) 
SELECT 
    id, 
    'MANUFACTURER', 
    'Toyota Warranty Services', 
    CONCAT('TW-', LPAD(id, 8, '0')),
    DATE_SUB(CURDATE(), INTERVAL 6 MONTH),
    DATE_ADD(CURDATE(), INTERVAL 6 MONTH),
    60000,
    'ACTIVE'
FROM cars 
WHERE car_model_id IN (SELECT id FROM car_models WHERE make = 'Toyota')
LIMIT 5;
```

**Seeding Best Practices:**
- ✅ Use `INSERT IGNORE` or `ON DUPLICATE KEY UPDATE` for idempotency
- ✅ Keep seed data minimal (only essential reference data)
- ✅ Use realistic data for testing
- ✅ Document the purpose of seed data
- ❌ Don't seed sensitive data (passwords, keys)
- ❌ Avoid massive data inserts in migrations

---

## Entity Layer

**Location:** `src/main/java/com/wheelshiftpro/entity/`

### Creating an Entity

**Template:**

```java
package com.wheelshiftpro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing a warranty record for a vehicle.
 * Tracks warranty coverage, provider details, and validity period.
 */
@Entity
@Table(name = "warranties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warranty extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_warranty_car"))
    private Car car;

    // Basic Fields
    @NotBlank(message = "Warranty type is required")
    @Column(name = "warranty_type", length = 32, nullable = false)
    private String warrantyType;

    @NotBlank(message = "Provider name is required")
    @Size(max = 128, message = "Provider name must not exceed 128 characters")
    @Column(name = "provider_name", length = 128, nullable = false)
    private String providerName;

    @NotBlank(message = "Policy number is required")
    @Column(name = "policy_number", length = 64, nullable = false, unique = true)
    private String policyNumber;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "coverage_details", columnDefinition = "TEXT")
    private String coverageDetails;

    @Column(name = "mileage_limit_km")
    private Integer mileageLimitKm;

    @NotNull(message = "Status is required")
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    // Helper Methods
    public boolean isActive() {
        return "ACTIVE".equals(status) && 
               LocalDate.now().isBefore(endDate);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }
}
```

**Entity Best Practices:**
- ✅ Extend `BaseEntity` for automatic `createdAt`/`updatedAt` fields
- ✅ Use Lombok annotations (`@Getter`, `@Setter`, `@Builder`)
- ✅ Add JavaDoc comments explaining the entity's purpose
- ✅ Use appropriate fetch types (`LAZY` for relationships)
- ✅ Add validation annotations (`@NotNull`, `@Size`, etc.)
- ✅ Name foreign key constraints explicitly
- ✅ Use `@Builder.Default` for default values
- ✅ Add helper methods for common business logic
- ❌ Don't use `@Data` (causes issues with lazy loading)
- ❌ Avoid bidirectional relationships unless necessary
- ❌ Don't expose entities directly in controllers

---

## Repository Layer

**Location:** `src/main/java/com/wheelshiftpro/repository/`

### Creating a Repository

**Template:**

```java
package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Warranty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Warranty entity.
 * Provides data access methods for warranty operations.
 */
@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long>, 
                                            JpaSpecificationExecutor<Warranty> {

    // Derived Query Methods (Spring Data JPA generates implementation)
    
    /**
     * Find warranty by policy number.
     */
    Optional<Warranty> findByPolicyNumber(String policyNumber);

    /**
     * Find all warranties for a specific car.
     */
    List<Warranty> findByCarId(Long carId);

    /**
     * Find warranties by status with pagination.
     */
    Page<Warranty> findByStatus(String status, Pageable pageable);

    /**
     * Find active warranties by warranty type.
     */
    Page<Warranty> findByWarrantyTypeAndStatus(String warrantyType, 
                                                String status, 
                                                Pageable pageable);

    /**
     * Check if a policy number exists.
     */
    boolean existsByPolicyNumber(String policyNumber);

    // Custom JPQL Queries
    
    /**
     * Find warranties expiring within a date range.
     */
    @Query("SELECT w FROM Warranty w WHERE w.endDate BETWEEN :startDate AND :endDate " +
           "AND w.status = 'ACTIVE'")
    List<Warranty> findExpiringSoon(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Find expired warranties that are still marked as active.
     */
    @Query("SELECT w FROM Warranty w WHERE w.endDate < :currentDate " +
           "AND w.status = 'ACTIVE'")
    List<Warranty> findExpiredActiveWarranties(@Param("currentDate") LocalDate currentDate);

    /**
     * Get warranty statistics by provider.
     */
    @Query("SELECT w.providerName, COUNT(w) as total, " +
           "SUM(CASE WHEN w.status = 'ACTIVE' THEN 1 ELSE 0 END) as active " +
           "FROM Warranty w GROUP BY w.providerName")
    List<Object[]> getWarrantyStatsByProvider();

    /**
     * Find warranties by car ID with active status.
     */
    @Query("SELECT w FROM Warranty w WHERE w.car.id = :carId " +
           "AND w.status = 'ACTIVE' AND w.endDate >= :currentDate")
    List<Warranty> findActiveWarrantiesByCarId(@Param("carId") Long carId,
                                                @Param("currentDate") LocalDate currentDate);
}
```

**Repository Best Practices:**
- ✅ Extend `JpaRepository<Entity, ID>` for basic CRUD
- ✅ Add `JpaSpecificationExecutor` for complex dynamic queries
- ✅ Use derived query methods for simple queries (Spring generates them)
- ✅ Use `@Query` with JPQL for complex queries
- ✅ Add JavaDoc comments for all methods
- ✅ Return `Optional<T>` for single results that might not exist
- ✅ Use `Page<T>` for paginated results
- ✅ Name query parameters with `@Param` annotation
- ❌ Don't put business logic in repositories
- ❌ Avoid native SQL queries unless absolutely necessary

---

## DTO Layer

**Location:** 
- Request DTOs: `src/main/java/com/wheelshiftpro/dto/request/`
- Response DTOs: `src/main/java/com/wheelshiftpro/dto/response/`

### Creating Request DTOs

**Template:**

```java
package com.wheelshiftpro.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating or updating a warranty.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotBlank(message = "Warranty type is required")
    @Size(max = 32, message = "Warranty type must not exceed 32 characters")
    private String warrantyType;

    @NotBlank(message = "Provider name is required")
    @Size(max = 128, message = "Provider name must not exceed 128 characters")
    private String providerName;

    @NotBlank(message = "Policy number is required")
    @Size(max = 64, message = "Policy number must not exceed 64 characters")
    private String policyNumber;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @Size(max = 2000, message = "Coverage details must not exceed 2000 characters")
    private String coverageDetails;

    @Min(value = 0, message = "Mileage limit must be non-negative")
    private Integer mileageLimitKm;

    private String status;
}
```

### Creating Response DTOs

**Template:**

```java
package com.wheelshiftpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for warranty response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyResponse {

    private Long id;
    private Long carId;
    private String carVinNumber;
    private String carMakeModel;
    private String warrantyType;
    private String providerName;
    private String policyNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverageDetails;
    private Integer mileageLimitKm;
    private String status;
    private Boolean isActive;
    private Boolean isExpired;
    private Integer daysRemaining;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**DTO Best Practices:**
- ✅ Use `@Data` for DTOs (equals/hashCode are safe here)
- ✅ Add `@Builder` for easy object construction
- ✅ Use validation annotations on request DTOs
- ✅ Include only necessary fields in response DTOs
- ✅ Flatten nested objects in responses (e.g., `carVinNumber` instead of `car.vinNumber`)
- ✅ Add computed fields in responses (e.g., `isActive`, `daysRemaining`)
- ✅ Keep request DTOs simple and focused
- ❌ Don't include sensitive data in responses
- ❌ Don't reuse request DTOs for responses
- ❌ Avoid circular references

---

## Mapper Layer

**Location:** `src/main/java/com/wheelshiftpro/mapper/`

### Creating a Mapper

**Template:**

```java
package com.wheelshiftpro.mapper;

import com.wheelshiftpro.dto.request.WarrantyRequest;
import com.wheelshiftpro.dto.response.WarrantyResponse;
import com.wheelshiftpro.entity.Warranty;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * MapStruct mapper for Warranty entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarrantyMapper {

    /**
     * Converts Warranty entity to WarrantyResponse DTO.
     */
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.vinNumber", target = "carVinNumber")
    @Mapping(target = "carMakeModel", 
             expression = "java(warranty.getCar() != null && warranty.getCar().getCarModel() != null ? " +
                         "warranty.getCar().getCarModel().getMake() + \" \" + " +
                         "warranty.getCar().getCarModel().getModel() : null)")
    @Mapping(target = "isActive", expression = "java(warranty.isActive())")
    @Mapping(target = "isExpired", expression = "java(warranty.isExpired())")
    @Mapping(target = "daysRemaining", 
             expression = "java(calculateDaysRemaining(warranty.getEndDate()))")
    WarrantyResponse toResponse(Warranty warranty);

    /**
     * Converts WarrantyRequest DTO to Warranty entity.
     * Note: car must be set manually in service layer
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    Warranty toEntity(WarrantyRequest request);

    /**
     * Converts list of Warranty entities to list of WarrantyResponse DTOs.
     */
    List<WarrantyResponse> toResponseList(List<Warranty> warranties);

    /**
     * Updates Warranty entity from WarrantyRequest DTO.
     * Ignores null values in the request.
     * Note: car must be set manually in service layer if changed
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    void updateEntityFromRequest(WarrantyRequest request, @MappingTarget Warranty warranty);

    /**
     * Helper method to calculate days remaining.
     */
    default Integer calculateDaysRemaining(LocalDate endDate) {
        if (endDate == null) return null;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return (int) Math.max(0, days);
    }
}
```

**Mapper Best Practices:**
- ✅ Use MapStruct for automated mapping
- ✅ Set `componentModel = "spring"` for dependency injection
- ✅ Use `@Mapping` to handle complex field mappings
- ✅ Use `expression` for calculated fields
- ✅ Create separate methods for entity → DTO and DTO → entity
- ✅ Add `updateEntityFromRequest` for PATCH/PUT operations
- ✅ Use `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` for updates
- ✅ Add default methods for complex transformations
- ✅ Document which fields must be set manually (relationships)
- ❌ Don't map relationships automatically (set in service layer)
- ❌ Avoid business logic in mappers

---

## Service Layer

**Location:** `src/main/java/com/wheelshiftpro/service/`

### Creating Service Interface

**Template:**

```java
package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.WarrantyRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.WarrantyResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for warranty management operations.
 */
public interface WarrantyService {

    /**
     * Creates a new warranty.
     *
     * @param request the warranty creation request
     * @return the created warranty response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     * @throws com.wheelshiftpro.exception.DuplicateResourceException if policy number exists
     */
    WarrantyResponse createWarranty(WarrantyRequest request);

    /**
     * Updates an existing warranty.
     *
     * @param id the warranty ID
     * @param request the update request
     * @return the updated warranty response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if warranty not found
     */
    WarrantyResponse updateWarranty(Long id, WarrantyRequest request);

    /**
     * Retrieves a warranty by ID.
     *
     * @param id the warranty ID
     * @return the warranty response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if warranty not found
     */
    WarrantyResponse getWarrantyById(Long id);

    /**
     * Retrieves all warranties with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return paginated warranty responses
     */
    PageResponse<WarrantyResponse> getAllWarranties(int page, int size);

    /**
     * Deletes a warranty.
     *
     * @param id the warranty ID
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if warranty not found
     */
    void deleteWarranty(Long id);

    /**
     * Retrieves warranties for a specific car.
     *
     * @param carId the car ID
     * @return list of warranties for the car
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car not found
     */
    List<WarrantyResponse> getWarrantiesByCarId(Long carId);

    /**
     * Retrieves active warranties.
     *
     * @param page the page number
     * @param size the page size
     * @return paginated active warranties
     */
    PageResponse<WarrantyResponse> getActiveWarranties(int page, int size);

    /**
     * Retrieves warranties expiring soon (within specified days).
     *
     * @param days number of days to look ahead
     * @return list of warranties expiring soon
     */
    List<WarrantyResponse> getWarrantiesExpiringSoon(int days);

    /**
     * Updates expired warranties status.
     *
     * @return number of warranties updated
     */
    int updateExpiredWarranties();
}
```

### Creating Service Implementation

**Template:**

```java
package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.WarrantyRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.WarrantyResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Warranty;
import com.wheelshiftpro.exception.DuplicateResourceException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.WarrantyMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.WarrantyRepository;
import com.wheelshiftpro.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of WarrantyService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final CarRepository carRepository;
    private final WarrantyMapper warrantyMapper;

    @Override
    public WarrantyResponse createWarranty(WarrantyRequest request) {
        log.info("Creating warranty for car ID: {}", request.getCarId());

        // Validate car exists
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car", "id", request.getCarId()));

        // Check for duplicate policy number
        if (warrantyRepository.existsByPolicyNumber(request.getPolicyNumber())) {
            throw new DuplicateResourceException(
                    "Warranty with policy number '" + request.getPolicyNumber() + 
                    "' already exists");
        }

        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Create warranty entity
        Warranty warranty = warrantyMapper.toEntity(request);
        warranty.setCar(car);

        // Save and return
        Warranty savedWarranty = warrantyRepository.save(warranty);
        log.info("Warranty created successfully with ID: {}", savedWarranty.getId());

        return warrantyMapper.toResponse(savedWarranty);
    }

    @Override
    public WarrantyResponse updateWarranty(Long id, WarrantyRequest request) {
        log.info("Updating warranty with ID: {}", id);

        // Find existing warranty
        Warranty warranty = warrantyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warranty", "id", id));

        // Update car if changed
        if (request.getCarId() != null && 
            !request.getCarId().equals(warranty.getCar().getId())) {
            Car car = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Car", "id", request.getCarId()));
            warranty.setCar(car);
        }

        // Check for duplicate policy number (excluding current)
        if (request.getPolicyNumber() != null && 
            !request.getPolicyNumber().equals(warranty.getPolicyNumber()) &&
            warrantyRepository.existsByPolicyNumber(request.getPolicyNumber())) {
            throw new DuplicateResourceException(
                    "Warranty with policy number '" + request.getPolicyNumber() + 
                    "' already exists");
        }

        // Update fields
        warrantyMapper.updateEntityFromRequest(request, warranty);

        // Validate dates
        if (warranty.getEndDate().isBefore(warranty.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Save and return
        Warranty updatedWarranty = warrantyRepository.save(warranty);
        log.info("Warranty updated successfully with ID: {}", id);

        return warrantyMapper.toResponse(updatedWarranty);
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyResponse getWarrantyById(Long id) {
        log.debug("Fetching warranty with ID: {}", id);

        Warranty warranty = warrantyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warranty", "id", id));

        return warrantyMapper.toResponse(warranty);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarrantyResponse> getAllWarranties(int page, int size) {
        log.debug("Fetching all warranties - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, 
                Sort.by("endDate").descending());
        Page<Warranty> warrantyPage = warrantyRepository.findAll(pageable);

        List<WarrantyResponse> responses = warrantyMapper.toResponseList(
                warrantyPage.getContent());

        return new PageResponse<>(
                responses,
                warrantyPage.getNumber(),
                warrantyPage.getSize(),
                warrantyPage.getTotalElements(),
                warrantyPage.getTotalPages(),
                warrantyPage.isLast()
        );
    }

    @Override
    public void deleteWarranty(Long id) {
        log.info("Deleting warranty with ID: {}", id);

        if (!warrantyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Warranty", "id", id);
        }

        warrantyRepository.deleteById(id);
        log.info("Warranty deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarrantyResponse> getWarrantiesByCarId(Long carId) {
        log.debug("Fetching warranties for car ID: {}", carId);

        // Validate car exists
        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car", "id", carId);
        }

        List<Warranty> warranties = warrantyRepository.findByCarId(carId);
        return warrantyMapper.toResponseList(warranties);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarrantyResponse> getActiveWarranties(int page, int size) {
        log.debug("Fetching active warranties - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, 
                Sort.by("endDate").ascending());
        Page<Warranty> warrantyPage = warrantyRepository.findByStatus("ACTIVE", pageable);

        List<WarrantyResponse> responses = warrantyMapper.toResponseList(
                warrantyPage.getContent());

        return new PageResponse<>(
                responses,
                warrantyPage.getNumber(),
                warrantyPage.getSize(),
                warrantyPage.getTotalElements(),
                warrantyPage.getTotalPages(),
                warrantyPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarrantyResponse> getWarrantiesExpiringSoon(int days) {
        log.debug("Fetching warranties expiring within {} days", days);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        List<Warranty> warranties = warrantyRepository.findExpiringSoon(
                startDate, endDate);

        return warrantyMapper.toResponseList(warranties);
    }

    @Override
    public int updateExpiredWarranties() {
        log.info("Updating expired warranties");

        List<Warranty> expiredWarranties = warrantyRepository
                .findExpiredActiveWarranties(LocalDate.now());

        expiredWarranties.forEach(warranty -> warranty.setStatus("EXPIRED"));
        warrantyRepository.saveAll(expiredWarranties);

        log.info("Updated {} expired warranties", expiredWarranties.size());
        return expiredWarranties.size();
    }
}
```

**Service Best Practices:**
- ✅ Use `@Service` annotation
- ✅ Use constructor injection with `@RequiredArgsConstructor`
- ✅ Add `@Slf4j` for logging
- ✅ Use `@Transactional` at class level (write operations)
- ✅ Use `@Transactional(readOnly = true)` for read operations
- ✅ Log important operations (create, update, delete)
- ✅ Validate input and business rules
- ✅ Throw appropriate exceptions with clear messages
- ✅ Keep methods focused (single responsibility)
- ❌ Don't expose entities in return types
- ❌ Don't catch exceptions unless you can handle them
- ❌ Avoid complex business logic in controllers

---

## Controller Layer

**Location:** `src/main/java/com/wheelshiftpro/controller/`

### Creating a Controller

**Template:**

```java
package com.wheelshiftpro.controller;

import com.wheelshiftpro.dto.request.WarrantyRequest;
import com.wheelshiftpro.dto.response.ApiResponse;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.WarrantyResponse;
import com.wheelshiftpro.service.WarrantyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for warranty management.
 */
@RestController
@RequestMapping("/api/v1/warranties")
@RequiredArgsConstructor
@Tag(name = "Warranty Management", description = "APIs for managing vehicle warranties")
public class WarrantyController {

    private final WarrantyService warrantyService;

    @PostMapping
    @Operation(summary = "Create a new warranty", 
               description = "Creates a new warranty record for a vehicle")
    @PreAuthorize("hasAuthority('warranty:create')")
    public ResponseEntity<ApiResponse<WarrantyResponse>> createWarranty(
            @Valid @RequestBody WarrantyRequest request) {
        WarrantyResponse response = warrantyService.createWarranty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Warranty created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a warranty", 
               description = "Updates an existing warranty by ID")
    @PreAuthorize("hasAuthority('warranty:update')")
    public ResponseEntity<ApiResponse<WarrantyResponse>> updateWarranty(
            @Parameter(description = "Warranty ID") @PathVariable Long id,
            @Valid @RequestBody WarrantyRequest request) {
        WarrantyResponse response = warrantyService.updateWarranty(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Warranty updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warranty by ID", 
               description = "Retrieves a specific warranty by its ID")
    @PreAuthorize("hasAuthority('warranty:read')")
    public ResponseEntity<ApiResponse<WarrantyResponse>> getWarrantyById(
            @Parameter(description = "Warranty ID") @PathVariable Long id) {
        WarrantyResponse response = warrantyService.getWarrantyById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all warranties", 
               description = "Retrieves all warranties with pagination")
    @PreAuthorize("hasAuthority('warranty:read')")
    public ResponseEntity<ApiResponse<PageResponse<WarrantyResponse>>> getAllWarranties(
            @Parameter(description = "Page number (0-indexed)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<WarrantyResponse> response = 
                warrantyService.getAllWarranties(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a warranty", 
               description = "Deletes a warranty by ID")
    @PreAuthorize("hasAuthority('warranty:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteWarranty(
            @Parameter(description = "Warranty ID") @PathVariable Long id) {
        warrantyService.deleteWarranty(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>success("Warranty deleted successfully", null));
    }

    @GetMapping("/car/{carId}")
    @Operation(summary = "Get warranties by car", 
               description = "Retrieves all warranties for a specific car")
    @PreAuthorize("hasAuthority('warranty:read')")
    public ResponseEntity<ApiResponse<List<WarrantyResponse>>> getWarrantiesByCarId(
            @Parameter(description = "Car ID") @PathVariable Long carId) {
        List<WarrantyResponse> responses = 
                warrantyService.getWarrantiesByCarId(carId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active warranties", 
               description = "Retrieves all active warranties with pagination")
    @PreAuthorize("hasAuthority('warranty:read')")
    public ResponseEntity<ApiResponse<PageResponse<WarrantyResponse>>> getActiveWarranties(
            @Parameter(description = "Page number") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<WarrantyResponse> response = 
                warrantyService.getActiveWarranties(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/expiring-soon")
    @Operation(summary = "Get warranties expiring soon", 
               description = "Retrieves warranties expiring within specified days")
    @PreAuthorize("hasAuthority('warranty:read')")
    public ResponseEntity<ApiResponse<List<WarrantyResponse>>> getWarrantiesExpiringSoon(
            @Parameter(description = "Number of days to look ahead") 
            @RequestParam(defaultValue = "30") int days) {
        List<WarrantyResponse> responses = 
                warrantyService.getWarrantiesExpiringSoon(days);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/update-expired")
    @Operation(summary = "Update expired warranties", 
               description = "Marks expired warranties as expired (admin operation)")
    @PreAuthorize("hasAuthority('warranty:update')")
    public ResponseEntity<ApiResponse<String>> updateExpiredWarranties() {
        int count = warrantyService.updateExpiredWarranties();
        return ResponseEntity.ok(
                ApiResponse.success("Updated " + count + " expired warranties", null));
    }
}
```

**Controller Best Practices:**
- ✅ Use `@RestController` and `@RequestMapping` for base path
- ✅ Use `@RequiredArgsConstructor` for dependency injection
- ✅ Add Swagger annotations (`@Operation`, `@Tag`, `@Parameter`)
- ✅ Use `@Valid` for request body validation
- ✅ Return `ResponseEntity<ApiResponse<T>>` for consistent responses
- ✅ Use appropriate HTTP status codes (201 for POST, 200 for GET, etc.)
- ✅ Add `@PreAuthorize` for RBAC security
- ✅ Provide default values for query parameters
- ✅ Keep controllers thin (delegate to service layer)
- ❌ Don't put business logic in controllers
- ❌ Don't directly access repositories
- ❌ Avoid try-catch blocks (use global exception handler)

---

## Exception Handling

**Location:** `src/main/java/com/wheelshiftpro/exception/`

### Existing Exceptions

1. **ResourceNotFoundException** - When a resource doesn't exist
2. **DuplicateResourceException** - When creating a duplicate resource
3. **BusinessException** - General business rule violations

### Creating Custom Exceptions

**Template:**

```java
package com.wheelshiftpro.exception;

/**
 * Exception thrown when warranty validation fails.
 */
public class WarrantyValidationException extends RuntimeException {
    
    public WarrantyValidationException(String message) {
        super(message);
    }
    
    public WarrantyValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Using Exceptions in Service

```java
// Resource not found
throw new ResourceNotFoundException("Warranty", "id", id);
throw new ResourceNotFoundException("Warranty with policy number '" + policyNumber + "' not found");

// Duplicate resource
throw new DuplicateResourceException("Warranty with policy number '" + policyNumber + "' already exists");

// Business validation
throw new BusinessException("Cannot delete warranty that is still active");

// Custom exception
throw new WarrantyValidationException("Warranty end date must be after start date");
```

**Exception Handling Best Practices:**
- ✅ Throw specific exceptions from service layer
- ✅ Use meaningful error messages
- ✅ Let `GlobalExceptionHandler` handle exceptions
- ✅ Create custom exceptions for specific domains
- ✅ Include relevant context in exception messages
- ❌ Don't catch exceptions unless you can handle them
- ❌ Don't swallow exceptions
- ❌ Avoid generic exceptions (Exception, RuntimeException)

---

## Enums

**Location:** `src/main/java/com/wheelshiftpro/enums/`

### Creating an Enum

**Template:**

```java
package com.wheelshiftpro.enums;

import lombok.Getter;

/**
 * Enum representing the status of a warranty.
 */
@Getter
public enum WarrantyStatus {
    ACTIVE("Active", "Warranty is currently valid"),
    EXPIRED("Expired", "Warranty has expired"),
    CANCELLED("Cancelled", "Warranty was cancelled"),
    CLAIMED("Claimed", "Warranty claim was made"),
    SUSPENDED("Suspended", "Warranty is temporarily suspended");

    private final String displayName;
    private final String description;

    WarrantyStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Check if warranty is currently valid.
     */
    public boolean isValid() {
        return this == ACTIVE;
    }

    /**
     * Check if warranty can be claimed.
     */
    public boolean canBeClaimed() {
        return this == ACTIVE;
    }
}
```

**Enum Best Practices:**
- ✅ Use enums for fixed sets of constants
- ✅ Add display names and descriptions
- ✅ Include helper methods for common checks
- ✅ Use `@Enumerated(EnumType.STRING)` in entities (not ORDINAL)
- ✅ Document each enum value
- ❌ Don't use magic strings in code (use enums)
- ❌ Avoid changing enum order (breaks ORDINAL mapping)

---

## Complete Feature Example

Let's walk through adding a complete "Maintenance Record" feature:

### 1. Database Migration

```sql
-- V11__Add_Maintenance_Records.sql
CREATE TABLE maintenance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    service_type VARCHAR(64) NOT NULL,
    service_date DATE NOT NULL,
    mileage_at_service INT NOT NULL,
    service_provider VARCHAR(128),
    cost DECIMAL(10, 2),
    notes TEXT,
    next_service_date DATE,
    next_service_mileage INT,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_maintenance_car FOREIGN KEY (car_id) 
        REFERENCES cars(id) ON DELETE CASCADE,
    INDEX idx_car_date (car_id, service_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Entity

```java
@Entity
@Table(name = "maintenance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotBlank
    @Column(name = "service_type", length = 64)
    private String serviceType;

    @NotNull
    @Column(name = "service_date")
    private LocalDate serviceDate;

    @NotNull
    @Column(name = "mileage_at_service")
    private Integer mileageAtService;

    @Column(name = "service_provider", length = 128)
    private String serviceProvider;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Column(name = "next_service_mileage")
    private Integer nextServiceMileage;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "COMPLETED";
}
```

### 3. Enum

```java
public enum MaintenanceType {
    OIL_CHANGE,
    TIRE_ROTATION,
    BRAKE_SERVICE,
    ENGINE_TUNE_UP,
    TRANSMISSION_SERVICE,
    BATTERY_REPLACEMENT,
    GENERAL_INSPECTION
}
```

### 4. DTOs

```java
// Request
@Data
@Builder
public class MaintenanceRecordRequest {
    @NotNull
    private Long carId;
    
    @NotBlank
    private String serviceType;
    
    @NotNull
    private LocalDate serviceDate;
    
    @NotNull
    @Min(0)
    private Integer mileageAtService;
    
    private String serviceProvider;
    private BigDecimal cost;
    private String notes;
    private LocalDate nextServiceDate;
    private Integer nextServiceMileage;
}

// Response
@Data
@Builder
public class MaintenanceRecordResponse {
    private Long id;
    private Long carId;
    private String carVinNumber;
    private String serviceType;
    private LocalDate serviceDate;
    private Integer mileageAtService;
    private String serviceProvider;
    private BigDecimal cost;
    private String notes;
    private LocalDate nextServiceDate;
    private Integer nextServiceMileage;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 5. Repository

```java
@Repository
public interface MaintenanceRecordRepository 
        extends JpaRepository<MaintenanceRecord, Long> {
    
    List<MaintenanceRecord> findByCarIdOrderByServiceDateDesc(Long carId);
    
    Page<MaintenanceRecord> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT m FROM MaintenanceRecord m WHERE m.car.id = :carId " +
           "AND m.serviceDate BETWEEN :startDate AND :endDate")
    List<MaintenanceRecord> findByCarAndDateRange(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
```

### 6. Mapper

```java
@Mapper(componentModel = "spring")
public interface MaintenanceRecordMapper {
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.vinNumber", target = "carVinNumber")
    MaintenanceRecordResponse toResponse(MaintenanceRecord record);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    MaintenanceRecord toEntity(MaintenanceRecordRequest request);
    
    List<MaintenanceRecordResponse> toResponseList(List<MaintenanceRecord> records);
}
```

### 7. Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {
    
    private final MaintenanceRecordRepository maintenanceRepository;
    private final CarRepository carRepository;
    private final MaintenanceRecordMapper mapper;
    
    @Override
    public MaintenanceRecordResponse createMaintenanceRecord(
            MaintenanceRecordRequest request) {
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car", "id", request.getCarId()));
        
        MaintenanceRecord record = mapper.toEntity(request);
        record.setCar(car);
        
        MaintenanceRecord saved = maintenanceRepository.save(record);
        return mapper.toResponse(saved);
    }
    
    // ... other methods
}
```

### 8. Controller

```java
@RestController
@RequestMapping("/api/v1/maintenance-records")
@RequiredArgsConstructor
@Tag(name = "Maintenance Records")
public class MaintenanceRecordController {
    
    private final MaintenanceRecordService maintenanceService;
    
    @PostMapping
    @Operation(summary = "Create maintenance record")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> create(
            @Valid @RequestBody MaintenanceRecordRequest request) {
        MaintenanceRecordResponse response = 
                maintenanceService.createMaintenanceRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Created successfully", response));
    }
    
    // ... other endpoints
}
```

---

## Best Practices

### General Coding Standards

1. **Naming Conventions**
   - Classes: PascalCase (e.g., `WarrantyService`)
   - Methods: camelCase (e.g., `createWarranty`)
   - Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
   - Packages: lowercase (e.g., `com.wheelshiftpro.service`)

2. **Code Organization**
   - One public class per file
   - Group related methods together
   - Order: constants → fields → constructors → public methods → private methods
   - Keep methods short (< 50 lines)

3. **Documentation**
   - Add JavaDoc for all public classes and methods
   - Document parameters, return values, and exceptions
   - Explain "why", not "what"
   - Keep comments up to date

4. **Error Handling**
   - Never swallow exceptions
   - Use specific exception types
   - Provide meaningful error messages
   - Log errors with appropriate levels

5. **Testing**
   - Write unit tests for service layer
   - Write integration tests for repositories
   - Test happy paths and error cases
   - Aim for > 80% code coverage

### Performance Considerations

1. **Database**
   - Use appropriate indexes
   - Avoid N+1 queries (use JOIN FETCH)
   - Use pagination for large result sets
   - Consider database connection pooling

2. **JPA/Hibernate**
   - Use `@Transactional(readOnly = true)` for read operations
   - Use LAZY loading for relationships
   - Avoid loading entire collections
   - Use projections for specific fields

3. **Caching**
   - Cache frequently accessed data
   - Use Redis for distributed caching
   - Set appropriate TTLs
   - Invalidate cache on updates

### Security Best Practices

1. **Input Validation**
   - Always validate user input
   - Use `@Valid` with DTOs
   - Sanitize data before persistence
   - Check for SQL injection vulnerabilities

2. **Authentication & Authorization**
   - Use JWT tokens for authentication
   - Implement RBAC with `@PreAuthorize`
   - Never expose sensitive data in responses
   - Log security events

3. **Data Protection**
   - Hash passwords (BCrypt)
   - Encrypt sensitive data at rest
   - Use HTTPS for data in transit
   - Implement rate limiting

### API Design

1. **RESTful Principles**
   - Use proper HTTP methods (GET, POST, PUT, DELETE)
   - Use meaningful URLs (`/api/v1/warranties`, not `/getWarranty`)
   - Return appropriate status codes
   - Version your APIs

2. **Response Format**
   - Always use `ApiResponse` wrapper
   - Include timestamps
   - Provide meaningful messages
   - Use pagination for lists

3. **Error Responses**
   - Return consistent error format
   - Include error codes
   - Provide helpful error messages
   - Don't expose stack traces in production

---

## Quick Reference Checklist

When adding a new feature, ensure you have:

- [ ] Created database migration with proper constraints and indexes
- [ ] Created seed data (if applicable)
- [ ] Created entity extending `BaseEntity`
- [ ] Created necessary enums with proper documentation
- [ ] Created request and response DTOs with validation
- [ ] Created repository with standard and custom queries
- [ ] Created MapStruct mapper with all necessary mappings
- [ ] Created service interface with JavaDoc
- [ ] Implemented service with business logic and error handling
- [ ] Created controller with Swagger annotations and security
- [ ] Added appropriate exception handling
- [ ] Written unit tests
- [ ] Updated feature documentation
- [ ] Tested all endpoints manually
- [ ] Verified RBAC permissions work correctly

---

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Project RBAC Guide](./rbac/RBAC_USAGE_GUIDE.md)
- [Project Caching Guide](./caching/REDIS_CACHING_GUIDE.md)
- [Project Documentation Standard](./features/DOCUMENTATION_STANDARD.md)

---

**Last Updated:** January 14, 2026
**Version:** 1.0

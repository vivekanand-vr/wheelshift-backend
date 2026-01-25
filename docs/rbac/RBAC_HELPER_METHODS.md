# 🛠️ RBAC Helper Methods Guide

**Version:** 1.0  
**Last Updated:** January 18, 2026  

---

## 📋 Overview

This guide documents the helper methods available for checking permissions, roles, access levels, and data scopes in the WheelShiftPro RBAC system. These methods make it easy to implement authorization checks throughout your application.

---

## 🎯 Table of Contents

- [AuthorizationHelperService](#authorizationhelperservice)
- [Permission Checks](#permission-checks)
- [Role Checks](#role-checks)
- [Resource Access Checks](#resource-access-checks)
- [Data Scope Checks](#data-scope-checks)
- [Bulk Authorization Checks](#bulk-authorization-checks)
- [Usage Examples](#usage-examples)

---

## 🔧 AuthorizationHelperService

The `AuthorizationHelperService` provides convenient helper methods for authorization checks throughout the application. It builds upon the core `AuthorizationService` with additional utility methods.

### Service Location
```
com.wheelshiftpro.service.AuthorizationHelperService
com.wheelshiftpro.service.impl.AuthorizationHelperServiceImpl
```

---

## 🔑 Permission Checks

### hasPermission
Check if an employee has a specific permission.

```java
boolean hasPermission(Long employeeId, String permissionName);
```

**Example:**
```java
if (authHelper.hasPermission(employeeId, "cars:write")) {
    // Allow car modification
}
```

### hasAnyPermission
Check if an employee has ANY of the specified permissions.

```java
boolean hasAnyPermission(Long employeeId, String... permissionNames);
```

**Example:**
```java
if (authHelper.hasAnyPermission(employeeId, "cars:write", "cars:delete")) {
    // Allow if user can write OR delete cars
}
```

### hasAllPermissions
Check if an employee has ALL of the specified permissions.

```java
boolean hasAllPermissions(Long employeeId, String... permissionNames);
```

**Example:**
```java
if (authHelper.hasAllPermissions(employeeId, "sales:write", "transactions:write")) {
    // Allow only if user can do both
}
```

### getEmployeePermissions
Get all permissions for an employee.

```java
Set<String> getEmployeePermissions(Long employeeId);
```

**Example:**
```java
Set<String> permissions = authHelper.getEmployeePermissions(employeeId);
// Returns: ["cars:read", "cars:write", "clients:read", ...]
```

---

## 👥 Role Checks

### hasRole
Check if an employee has a specific role.

```java
boolean hasRole(Long employeeId, RoleType roleName);
```

**Example:**
```java
if (authHelper.hasRole(employeeId, RoleType.SALES)) {
    // Show sales-specific UI
}
```

### hasAnyRole
Check if an employee has ANY of the specified roles.

```java
boolean hasAnyRole(Long employeeId, RoleType... roleNames);
```

**Example:**
```java
if (authHelper.hasAnyRole(employeeId, RoleType.ADMIN, RoleType.SUPER_ADMIN)) {
    // Allow admin functions
}
```

### hasAllRoles
Check if an employee has ALL of the specified roles.

```java
boolean hasAllRoles(Long employeeId, RoleType... roleNames);
```

**Example:**
```java
if (authHelper.hasAllRoles(employeeId, RoleType.SALES, RoleType.INSPECTOR)) {
    // Employee has both roles
}
```

### isSuperAdmin
Check if an employee is a super admin.

```java
boolean isSuperAdmin(Long employeeId);
```

**Example:**
```java
if (authHelper.isSuperAdmin(employeeId)) {
    // Show system administration features
}
```

### isAdmin
Check if an employee is an admin or super admin.

```java
boolean isAdmin(Long employeeId);
```

**Example:**
```java
if (authHelper.isAdmin(employeeId)) {
    // Show administrative features
}
```

### getEmployeeRoles
Get all roles for an employee.

```java
Set<RoleType> getEmployeeRoles(Long employeeId);
```

**Example:**
```java
Set<RoleType> roles = authHelper.getEmployeeRoles(employeeId);
// Returns: [SALES, INSPECTOR]
```

---

## 🔒 Resource Access Checks

### canAccessResource
Check if an employee can access a specific resource with a minimum access level.

```java
boolean canAccessResource(Long employeeId, ResourceType resourceType, 
                         Long resourceId, AccessLevel minAccess);
```

**Example:**
```java
if (authHelper.canAccessResource(employeeId, ResourceType.CAR, carId, AccessLevel.WRITE)) {
    // Allow car editing
}
```

### canReadResource
Check if an employee can read a specific resource.

```java
boolean canReadResource(Long employeeId, ResourceType resourceType, Long resourceId);
```

**Example:**
```java
if (authHelper.canReadResource(employeeId, ResourceType.CLIENT, clientId)) {
    // Show client details
}
```

### canWriteResource
Check if an employee can write to a specific resource.

```java
boolean canWriteResource(Long employeeId, ResourceType resourceType, Long resourceId);
```

**Example:**
```java
if (authHelper.canWriteResource(employeeId, ResourceType.INQUIRY, inquiryId)) {
    // Allow inquiry editing
}
```

### canDeleteResource
Check if an employee can delete a specific resource.

```java
boolean canDeleteResource(Long employeeId, ResourceType resourceType, Long resourceId);
```

**Example:**
```java
if (authHelper.canDeleteResource(employeeId, ResourceType.RESERVATION, reservationId)) {
    // Show delete button
}
```

### hasResourceACL
Check if an employee has an ACL entry for a specific resource.

```java
boolean hasResourceACL(Long employeeId, ResourceType resourceType, Long resourceId);
```

**Example:**
```java
if (authHelper.hasResourceACL(employeeId, ResourceType.CAR, carId)) {
    // Employee has special ACL for this car
}
```

---

## 📍 Data Scope Checks

### hasLocationAccess
Check if an employee has access to a specific location.

```java
boolean hasLocationAccess(Long employeeId, String locationId);
```

**Example:**
```java
if (authHelper.hasLocationAccess(employeeId, "LOC-001")) {
    // Show location-specific data
}
```

### hasDepartmentAccess
Check if an employee has access to a specific department.

```java
boolean hasDepartmentAccess(Long employeeId, String department);
```

**Example:**
```java
if (authHelper.hasDepartmentAccess(employeeId, "FINANCE")) {
    // Show finance department data
}
```

### hasAssignmentAccess
Check if a resource is assigned to an employee.

```java
boolean hasAssignmentAccess(Long employeeId, ResourceType resourceType, Long resourceId);
```

**Example:**
```java
if (authHelper.hasAssignmentAccess(employeeId, ResourceType.INQUIRY, inquiryId)) {
    // This inquiry is assigned to the employee
}
```

### getLocationScopes
Get all location scopes for an employee.

```java
Set<String> getLocationScopes(Long employeeId);
```

**Example:**
```java
Set<String> locations = authHelper.getLocationScopes(employeeId);
// Returns: ["LOC-001", "LOC-003"]
```

### getDepartmentScopes
Get all department scopes for an employee.

```java
Set<String> getDepartmentScopes(Long employeeId);
```

**Example:**
```java
Set<String> departments = authHelper.getDepartmentScopes(employeeId);
// Returns: ["FINANCE", "ACCOUNTING"]
```

### hasDataScopes
Check if an employee has any data scopes defined.

```java
boolean hasDataScopes(Long employeeId, ScopeType scopeType);
```

**Example:**
```java
if (authHelper.hasDataScopes(employeeId, ScopeType.LOCATION)) {
    // Employee has location restrictions
}
```

---

## 📦 Bulk Authorization Checks

### canAccessMultipleResources
Check if an employee can access multiple resources.

```java
Map<Long, Boolean> canAccessMultipleResources(Long employeeId, 
                                              ResourceType resourceType, 
                                              List<Long> resourceIds, 
                                              AccessLevel minAccess);
```

**Example:**
```java
Map<Long, Boolean> accessMap = authHelper.canAccessMultipleResources(
    employeeId, 
    ResourceType.CAR, 
    Arrays.asList(1L, 2L, 3L), 
    AccessLevel.READ
);
// Returns: {1=true, 2=false, 3=true}
```

### filterAccessibleResources
Filter a list of resources to only those the employee can access.

```java
<T> List<T> filterAccessibleResources(Long employeeId, 
                                     List<T> resources, 
                                     Function<T, Long> idExtractor, 
                                     ResourceType resourceType, 
                                     AccessLevel minAccess);
```

**Example:**
```java
List<Car> accessibleCars = authHelper.filterAccessibleResources(
    employeeId,
    allCars,
    Car::getId,
    ResourceType.CAR,
    AccessLevel.READ
);
```

### hasAnyResourceAccess
Check if an employee has access to at least one of the specified resources.

```java
boolean hasAnyResourceAccess(Long employeeId, 
                            ResourceType resourceType, 
                            List<Long> resourceIds, 
                            AccessLevel minAccess);
```

**Example:**
```java
if (authHelper.hasAnyResourceAccess(employeeId, ResourceType.CLIENT, clientIds, AccessLevel.READ)) {
    // Employee can see at least one client
}
```

---

## 💡 Usage Examples

### Example 1: Controller with Authorization

```java
@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {
    
    private final CarService carService;
    private final AuthorizationHelperService authHelper;
    
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCar(@PathVariable Long id,
                                             @AuthenticationPrincipal Long employeeId) {
        // Check if user can read this car
        if (!authHelper.canReadResource(employeeId, ResourceType.CAR, id)) {
            throw new ForbiddenException("Access denied");
        }
        
        CarResponse car = carService.getCarById(id);
        return ResponseEntity.ok(car);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CarResponse> updateCar(@PathVariable Long id,
                                                @RequestBody CarRequest request,
                                                @AuthenticationPrincipal Long employeeId) {
        // Check if user can modify this car
        if (!authHelper.canWriteResource(employeeId, ResourceType.CAR, id)) {
            throw new ForbiddenException("Access denied");
        }
        
        CarResponse car = carService.updateCar(id, request);
        return ResponseEntity.ok(car);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id,
                                         @AuthenticationPrincipal Long employeeId) {
        // Check if user can delete this car
        if (!authHelper.canDeleteResource(employeeId, ResourceType.CAR, id)) {
            throw new ForbiddenException("Access denied");
        }
        
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Example 2: Service Layer with Permission Checks

```java
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {
    
    private final InquiryRepository inquiryRepository;
    private final AuthorizationHelperService authHelper;
    
    @Override
    public List<InquiryResponse> getInquiriesForEmployee(Long employeeId) {
        // Get all inquiries
        List<Inquiry> allInquiries = inquiryRepository.findAll();
        
        // Filter based on data scopes and permissions
        if (authHelper.isSuperAdmin(employeeId)) {
            // Super admin sees everything
            return allInquiries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        }
        
        // Filter by location scopes
        Set<String> allowedLocations = authHelper.getLocationScopes(employeeId);
        if (!allowedLocations.isEmpty()) {
            allInquiries = allInquiries.stream()
                .filter(inquiry -> allowedLocations.contains(inquiry.getLocation()))
                .collect(Collectors.toList());
        }
        
        // Further filter by assignment if ASSIGNMENT scope exists
        if (authHelper.hasDataScopes(employeeId, ScopeType.ASSIGNMENT)) {
            allInquiries = allInquiries.stream()
                .filter(inquiry -> inquiry.getAssignedEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
        }
        
        return allInquiries.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public InquiryResponse assignInquiry(Long inquiryId, Long assigneeId, Long requesterId) {
        // Check if requester has permission to assign
        if (!authHelper.hasPermission(requesterId, "inquiries:assign")) {
            throw new ForbiddenException("You don't have permission to assign inquiries");
        }
        
        // Check if requester can access this inquiry
        if (!authHelper.canWriteResource(requesterId, ResourceType.INQUIRY, inquiryId)) {
            throw new ForbiddenException("You cannot access this inquiry");
        }
        
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));
        
        inquiry.setAssignedEmployeeId(assigneeId);
        inquiry = inquiryRepository.save(inquiry);
        
        return mapToResponse(inquiry);
    }
}
```

### Example 3: UI Component with Role-Based Rendering

```java
@Component
@RequiredArgsConstructor
public class DashboardBuilder {
    
    private final AuthorizationHelperService authHelper;
    
    public DashboardResponse buildDashboard(Long employeeId) {
        DashboardResponse dashboard = new DashboardResponse();
        
        // Add sections based on roles and permissions
        if (authHelper.hasPermission(employeeId, "sales:read")) {
            dashboard.addSection(buildSalesSection(employeeId));
        }
        
        if (authHelper.hasPermission(employeeId, "transactions:read")) {
            dashboard.addSection(buildFinanceSection(employeeId));
        }
        
        if (authHelper.hasAnyRole(employeeId, RoleType.ADMIN, RoleType.SUPER_ADMIN)) {
            dashboard.addSection(buildAdminSection(employeeId));
        }
        
        if (authHelper.isSuperAdmin(employeeId)) {
            dashboard.addSection(buildSystemSection());
        }
        
        // Add location-specific widgets
        Set<String> locations = authHelper.getLocationScopes(employeeId);
        if (!locations.isEmpty()) {
            dashboard.setLocationFilter(locations);
        }
        
        return dashboard;
    }
}
```

### Example 4: Bulk Resource Filtering

```java
@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    
    private final CarRepository carRepository;
    private final AuthorizationHelperService authHelper;
    
    @Override
    public List<CarResponse> getAccessibleCars(Long employeeId) {
        // Get all cars
        List<Car> allCars = carRepository.findAll();
        
        // Filter to only accessible cars
        List<Car> accessibleCars = authHelper.filterAccessibleResources(
            employeeId,
            allCars,
            Car::getId,
            ResourceType.CAR,
            AccessLevel.READ
        );
        
        return accessibleCars.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public Map<Long, Boolean> checkBulkAccess(Long employeeId, List<Long> carIds) {
        return authHelper.canAccessMultipleResources(
            employeeId,
            ResourceType.CAR,
            carIds,
            AccessLevel.WRITE
        );
    }
}
```

### Example 5: Complex Authorization Logic

```java
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final AuthorizationHelperService authHelper;
    private final CarRepository carRepository;
    
    @Override
    public ReservationResponse convertToSale(Long reservationId, Long employeeId) {
        // Check base permission
        if (!authHelper.hasPermission(employeeId, "reservations:convert")) {
            throw new ForbiddenException("Missing permission: reservations:convert");
        }
        
        // Load reservation
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        
        // Check resource-level access
        if (!authHelper.canWriteResource(employeeId, ResourceType.RESERVATION, reservationId)) {
            throw new ForbiddenException("Cannot access this reservation");
        }
        
        // Check if employee can create sales
        if (!authHelper.hasPermission(employeeId, "sales:write")) {
            throw new ForbiddenException("Missing permission: sales:write");
        }
        
        // Check location access for the car
        Car car = carRepository.findById(reservation.getCarId())
            .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
        
        if (!authHelper.hasLocationAccess(employeeId, car.getLocation())) {
            throw new ForbiddenException("Cannot access cars in this location");
        }
        
        // Perform conversion
        // ... business logic ...
        
        return mapToResponse(reservation);
    }
}
```

---

## 🔍 Best Practices

1. **Check at Multiple Layers**
   - Controller: Basic permission checks
   - Service: Business logic authorization
   - Repository: Data-level filtering

2. **Use Specific Methods**
   ```java
   // Good - specific and clear
   if (authHelper.canWriteResource(employeeId, ResourceType.CAR, carId))
   
   // Avoid - too generic
   if (authHelper.hasPermission(employeeId, "cars:write"))
   ```

3. **Fail Fast**
   ```java
   // Check authorization first
   if (!authHelper.canAccessResource(...)) {
       throw new ForbiddenException("Access denied");
   }
   
   // Then proceed with business logic
   // ...
   ```

4. **Cache When Appropriate**
   ```java
   // For repeated checks in a loop, cache the result
   boolean canEdit = authHelper.canWriteResource(employeeId, ResourceType.CAR, carId);
   
   for (CarPart part : car.getParts()) {
       if (canEdit) {
           // Allow editing
       }
   }
   ```

5. **Use Bulk Operations**
   ```java
   // Good - single call
   Map<Long, Boolean> accessMap = authHelper.canAccessMultipleResources(...);
   
   // Avoid - multiple calls
   for (Long id : ids) {
       boolean access = authHelper.canAccessResource(...);
   }
   ```

---

## 📚 See Also

- [Complete Beginner's Guide](./RBAC_COMPLETE_GUIDE.md)
- [Endpoint Reference](./RBAC_ENDPOINTS.md)
- [Implementation Summary](../../rbac/RBAC_IMPLEMENTATION_SUMMARY.md)
- [Usage Guide](../../rbac/RBAC_USAGE_GUIDE.md)

---

**Note:** All helper methods build upon the core `AuthorizationService`. They provide convenient shortcuts and additional functionality for common authorization scenarios.

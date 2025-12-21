# RBAC Quick Start

## 5-Minute Setup

### 1. Login

```bash
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@wheelshift.com",
  "password": "admin123"
}
```

Response includes JWT token and permissions.

### 2. Assign Role to Employee

```bash
POST http://localhost:8080/api/v1/employee-roles
Authorization: Bearer <token>
Content-Type: application/json

{
  "employeeId": 5,
  "roleId": 3
}
```

### 3. Check Permissions in Code

```java
@Autowired
private AuthorizationService authService;

// Check if employee can write cars
boolean canEdit = authService.hasPermission(employeeId, "cars:write");

// Check resource-level access
boolean canAccessCar = authService.canAccessResource(
    employeeId, 
    ResourceType.CAR, 
    "CAR-123", 
    AccessLevel.WRITE
);
```

### 4. Apply Data Scopes

```java
// Get cars filtered by employee's data scopes
Specification<Car> spec = authService.applyDataScopes(
    employeeId, 
    ResourceType.CAR
);
List<Car> cars = carRepository.findAll(spec);
```

### 5. Protect Endpoints

```java
@GetMapping("/cars")
@PreAuthorize("@authService.hasPermission(authentication.name, 'cars:read')")
public List<CarResponse> getCars() {
    return carService.getAllCars();
}
```

## Built-in Roles

| Role | ID | Use Case |
|------|----|----|
| SUPER_ADMIN | 1 | Full system access |
| ADMIN | 2 | Admin operations |
| SALES | 3 | Sales operations |
| INSPECTOR | 4 | Vehicle inspections |
| FINANCE | 5 | Financial operations |
| STORE_MANAGER | 6 | Location management |

## Common Operations

### Get Employee Roles
```bash
GET /api/v1/employee-roles/employee/{employeeId}
```

### Create Data Scope
```bash
POST /api/v1/data-scopes
{
  "employeeId": 5,
  "scopeType": "LOCATION",
  "scopeValue": "LOC-001",
  "effect": "INCLUDE"
}
```

### Grant Resource Access
```bash
POST /api/v1/resource-acl
{
  "resourceType": "CAR",
  "resourceId": "CAR-123",
  "subjectType": "EMPLOYEE",
  "subjectId": 5,
  "accessLevel": "WRITE"
}
```

## Default Credentials

Seeded in `V5__Seed_RBAC_Data.sql`:

- **Admin**: admin@wheelshift.com / admin123
- **Sales**: sales@wheelshift.com / sales123
- **Inspector**: inspector@wheelshift.com / inspector123

## Next Steps

- [Full Documentation](README.md)
- [Implementation Details](IMPLEMENTATION.md)
- [API Reference](http://localhost:8080/api/v1/swagger-ui.html)

# Role-Based Access Control (RBAC)

## Overview

Comprehensive RBAC system with hierarchical roles, fine-grained permissions, data scopes, and resource-level ACLs.

## Core Components

1. **Roles** - User roles in the system (SUPER_ADMIN, ADMIN, SALES, etc.)
2. **Permissions** - Fine-grained access controls (`resource:action` format)
3. **Custom Employee Permissions** - ⭐ **NEW!** Direct permission assignment to individual employees
4. **Data Scopes** - Filter data by location, department, or assignment
5. **Resource ACLs** - Individual resource-level access control

## Authorization Precedence

```
Resource ACL > Custom Employee Permissions > Data Scope > Role Permission
```

**Explanation:**
- **Resource ACL** (highest): Override all other permissions for specific resources
- **Custom Employee Permissions**: Direct permissions assigned to individual employees by super admins
- **Data Scope**: Filter what data employees can see
- **Role Permission** (base): Default permissions from employee's assigned roles

## Built-in Roles & Permissions

## Built-in Roles & Permissions

| Role | ID | Permissions | Use Case |
|------|-------|------------|----------|
| **SUPER_ADMIN** | 1 | `*:*` (all permissions) | Full system access |
| **ADMIN** | 2 | `employees:*`, `roles:*`, `permissions:*`, `locations:*`, `reports:read` | Administrative access |
| **SALES** | 3 | `inquiries:*`, `reservations:*`, `sales:*`, `clients:read`, `cars:read` | Sales operations |
| **INSPECTOR** | 4 | `inspections:*`, `cars:read`, `movements:write` | Vehicle inspections |
| **FINANCE** | 5 | `transactions:*`, `sales:read`, `reports:*` | Financial operations |
| *Quick Start

### 1. Login & Get Token

```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@wheelshift.com",
  "password": "admin123"
}

Response:
{
  "token": "eyJhbGc...",
  "employee": {...},
  "roles": ["ADMIN"],
  "permissions": ["cars:read", "cars:write", ...]
}
```

### 2. Check Permissions in Code

```java
@Autowired
private AuthorizationService authService;

// Basic permission check
boolean canEdit = authService.hasPermission(employeeId, "cars:write");

// Resource-level access check
boolean canAccessCar = authService.canAccessResource(
    employeeId, 
    ResourceType.CAR, 
    "CAR-123", 
    AccessLevel.WRITE
);

// Apply data scopes to queries
Specification<Car> spec = authService.applyDataScopes(
    employeeId, 
    ResourceType.CAR
);
List<Car> cars = carRepository.findAll(spec);
```

### 3. Protect Endpoints
**Example:**
```bash
POST /api/v1/data-scopes
{
  "employeeId": 5,
  "scopeType": "LOCATION",
  "scopeValue": "LOC-001",
  "effect": "INCLUDE"
}
```
Employee can only see resources in location LOC-001.

**Usage in Code:**
```java
// Apply data scopes to query
Specification<Car> spec = authService.applyDataScopes(
    employeeId, 
    ResourceType.CAR
);
List<Car> filteredCars = carRepository.findAll(spec);
```
}
```

## *STORE_MANAGER** | 6 | `cars:*`, `movements:*`, `locations:read`, `inspections:read`, `tasks:*` | Location management |

### Default Credentials

**Example:**
```bash
POST /api/v1/resource-acl
{
  "resourceType": "CAR",
  "resourceId": "CAR-123",
  "subjectType": "EMPLOYEE",
  "subjectId": 5,
  "accessLevel": "ADMIN"
}
```
Employee 5 has admin access to CAR-123, regardless of role permissions.

**Check in Code:**
```java
boolean canEdit = authService.canAccessResource(
    employeeId, 
    ResourceType.CAR, 
    carId, 
    AccessLevel.WRITE
);
```
inquiries:*     - All inquiry operations
```

### Available Resources

- `cars`, `car-models`, `clients`, `employees`, `inquiries`, `reservations`
- `sales`, `transactions`, `inspections`, `locations`, `tasks`, `events`
- `roles`, `permissions`, `acl`, `notifications`

### Available Actions

- `read` - View operations
- `write` - Create/update operations
- `delete` - Delete operations
- `*` - All actions (wildcard)

## Data Scopes

Filter data access based on organizational boundaries:

### Scope Types

1. **LOCATION** - Access limited to specific storage locations
2. **DEPARTMENT** - Access limited to department resources
3. **ASSIGNMENT** - Access only to assigned resources

### Scope Effects

- **INCLUDE** - Grant access to specified scope
- **EXCLUDE** - Deny access to specified scope

### Example

```json
{
  "employeeId": 1,
  "scopeType": "LOCATION",
  "scopeValue": "LOC-001",
  "effect": "INCLUDE"
}
```

Employee can only see cars, inquiries, etc. in location LOC-001.

## ⭐ Custom Employee Permissions (NEW!)

Assign permissions **directly to individual employees**, independent of their roles.

### Use Case

**Example:** A SALES employee needs temporary access to manage cars (normally only STORE_MANAGER has this).

**Solution:** Super admin assigns `cars:write` permission directly to the employee:

```bash
POST /api/v1/rbac/employee-permissions/employees/5
{
  "permissionId": 15,  // cars:write
  "reason": "Temporary access for Q4 sales campaign"
}
```

### How It Works

```
Employee's Final Permissions = Role Permissions + Custom Permissions
```

The system automatically merges both when checking permissions:
- **Role gives:** `["cars:read", "reservations:write"]`
- **Custom adds:** `["cars:write", "transactions:read"]`
- **Total access:** All 4 permissions

### Benefits

✅ **Flexible** - Grant any permission to any employee  
✅ **Temporary** - Easy to add/remove when needed  
✅ **Auditable** - Tracks who granted it and why  
✅ **Independent** - Doesn't affect role definitions  

**📚 See [Custom Permissions Guide](CUSTOM_PERMISSIONS_GUIDE.md) for complete documentation**

## Resource ACLs

Override role permissions for specific resources:

### Access Levels

- **READ** - View only
- **WRITE** - View and edit
- **ADMIN** - Full control

### Subject Types
Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | Login and get JWT token |
| POST | `/api/v1/auth/logout` | Logout |

### Roles
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/roles` | Get all roles |
| GET | `/api/v1/roles/{id}` | Get role by ID |
| POST | `/api/v1/roles` | Create role |
| PUT | `/api/v1/roles/{id}` | Update role |
| DELETE | `/api/v1/roles/{id}` | Delete role |

### Employee Roles
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/employee-roles/employee/{employeeId}` | Get employee roles |
| POST | `/api/v1/employee-roles` | Assign role to employee |
| DELETE | `/api/v1/employee-roles` | Remove role from employee |

### Permissions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/permissions` | Get all permissions |
| GET | `/api/v1/permissions/{id}` | Get permission by ID |
| POST | `/api/v1/permissions` | Create permission |

### Data Scopes
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/data-scopes/employee/{employeeId}` | Get employee scopes |
| POST | `/api/v1/data-scopes` | Create data scope |
| DELETE | `/api/v1/data-scopes/{id}` | Delete data scope |

### Resource ACLs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/resource-acl/resource/{type}/{id}` | Get resource ACLs |
| POST | `/api/v1/resource-acl` | Grant resource access |
| DELETE | `/api/v1/resource-acl/{id}` | Revoke resource access |

### ⭐ Custom Employee Permissions (NEW!)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/rbac/employee-permissions/employees/{employeeId}` | Assign custom permission to employee |
| DELETE | `/api/v1/rbac/employee-permissions/employees/{employeeId}/permissions/{permissionId}` | Remove custom permission |
| GET | `/api/v1/rbac/employee-permissions/employees/{employeeId}` | Get employee's custom permissions |
| GET | `/api/v1/rbac/employee-permissions/employees/{employeeId}/permission-names` | Get custom permission names |
| DELETE | `/api/v1/rbac/employee-permissions/employees/{employeeId}` | Remove all custom permissions |

**📚 See [Custom Permissions Guide](CUSTOM_PERMISSIONS_GUIDE.md) for detailed documentation**
```java
@RestController
@RequestMapping("/api/v1/cars")
public class CarController {
    
    @GetMapping("/{id}")
    @PreAuthorize("@authService.hasPermission(authentication.name, 'cars:read')")
    public ResponseEntity<CarResponse> getCar(@PathVariable Long id) {
        // Method implementation
    }
    
    @PostMapping
    @PreAuthorize("@authService.hasPermission(authentication.name, 'cars:write')")
    public ResponseEntity<CarResponse> createCar(@RequestBody CarRequest request) {
        // Method implementation
    }
}
```

## Database Schema

### Tables

- `roles` - Role definitions
- `permissions` - Permission definitions
- `role_permissions` - Role-permission mappings
- `employee_roles` - Employee-role assignments
- `employee_data_scopes` - Employee data scope filters
- `resource_acl` - Resource-level access control

### Migrations

- `V4__Add_RBAC_Tables.sql` - Creates RBAC tables
- `V5__Seed_RBAC_Data.sql` - Seeds default roles and permissions

## Best Practices

1. **Use Resource ACLs Sparingly** - Reserve for exceptional cases
2. **Prefer Role Permissions** - Use roles for common access patterns
3. **Document Custom Roles** - When creating custom roles, document their purpose
4. **Regular Audits** - Review permissions and ACLs periodically
5. **Least Privilege** - Grant minimum necessary permissions
6. **Test Authorization** - Verify permissions in integration tests

## Common Scenarios

### Scenario 1: Sales Person Access
- Role: SALES
- Permissions: inquiries:*, reservations:*, sales:*, clients:read
- Data Scope: Only assigned inquiries

### Scenario 2: Store Manager
- Role: STORE_MANAGER
- Permissions: cars:*, inspections:*, movements:*
- Data Scope: Location-based (their store only)

### Scenario 3: Finance Officer
- Role: FINANCE
- Permissions: transactions:*, sales:read, reports:*
- Data Scope: Department-based (finance department)

### Scenario 4: Special Access
- Employee needs temporary access to a specific car
- Solution: Create Resource ACL granting WRITE access for duration

## Implementation Files

### Enums (6)
- `RoleType`, `ScopeType`, `ScopeEffect`, `ResourceType`, `SubjectType`, `AccessLevel`

### Entities (4)
- `Role`, `Permission`, `EmployeeDataScope`, `ResourceACL`

### Services (6)
- `RoleService`, `PermissionService`, `DataScopeService`, `ResourceACLService`, `AuthorizationService`, `AuthorizationHelperService`

### Controllers (6)
- `AuthController`, `RoleController`, `PermissionController`, `EmployeeRoleController`, `DataScopeController`, `ResourceACLController`

### Security (2)
- `EmployeeUserDetails`, `EmployeeUserDetailsService`

---

## 📚 Documentation

### Complete Guides
- **[RBAC Complete Beginner's Guide](./RBAC_COMPLETE_GUIDE.md)** - Comprehensive guide with ASCII diagrams
- **[API Endpoints Reference](./RBAC_ENDPOINTS.md)** - Complete list of all RBAC endpoints
- **[Helper Methods Guide](./RBAC_HELPER_METHODS.md)** - Authorization helper methods documentation
- **⭐ [Custom Employee Permissions Guide](./CUSTOM_PERMISSIONS_GUIDE.md)** - Direct permission assignment to employees (NEW!)

### Technical Documentation
- **[Implementation Summary](../../rbac/RBAC_IMPLEMENTATION_SUMMARY.md)** - Detailed implementation overview
- **[Usage Guide](../../rbac/RBAC_USAGE_GUIDE.md)** - How to use the RBAC system

---

## Troubleshooting

### Access Denied (403)

1. Check employee has required role
2. Verify role has required permission
```sql
-- Roles
CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    role_type VARCHAR(20),
    is_system_role BOOLEAN DEFAULT FALSE
);

-- Permissions
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL
);

-- Role-Permission Mapping
CREATE TABLE role_permissions (
    role_id BIGINT,
    permission_id BIGINT,
    PRIMARY KEY (role_id, permission_id)
);

-- Employee-Role Assignment only
2. **Prefer Role Permissions** - Use roles for common access patterns
3. **Least Privilege Principle** - Grant minimum necessary permissions
4. **Document Custom Roles** - Clearly document purpose and permissions
5. **Regular Audits** - Review permissions and ACLs periodically
6. **Test Authorization** - Verify permissions in integration tests
7. **Data Scope Hierarchy** - Most specific scope takes precedence
8. **Avoid Permission Conflicts** - Don't mix INCLUDE/EXCLUDE for same resource

-- Data Scopes
CREATE TABLE employee_data_scopes (
    iales Person with Assignment Scope
```
Role: SALES
Permissions: inquiries:*, reservations:*, sales:*, clients:read
Data Scope: ASSIGNMENT (only assigned inquiries)
Result: Can manage own inquiries and reservations
```

### Store Manager with Location Scope
```
Role: STORE_MANAGER  
Permissions: cars:*, inspections:*, movements:*
Data Scope: LOCATION (LOC-001)
Result: Full car management for their store only
```

### Finance Officer with Department Scope
```
Role: FINANCE
Permissions: transactions:*, sales:read, reports:*
Data Scope: DEPARTMENT (Finance)
Result: All financial operations, read-only sales
```

### Temporary Special Access
```
Scenario: Employee needs temporary access to specific car
Solution: Create Resource ACL
Resource: CAR-123
Subject: EMPLOYEE-5
Access Level: WRITE
Duration: Set expiry in application logic
``
5. Apply data scope filters to query results
```

## Implementation Classes
1. Check employee has required role: `GET /api/v1/employee-roles/employee/{id}`
2. Verify role has permission: `GET /api/v1/roles/{roleId}`
3. Check data scopes: `GET /api/v1/data-scopes/employee/{id}`
4. Review resource ACLs: `GET /api/v1/resource-acl/resource/{type}/{id}`
5. Verify JWT token is valid and not expired

### Permission Not Working
1. Confirm permission exists: `GET /api/v1/permissions`
2. Check role-permission mapping in database
3. Ensure employee-role assignment is active
4. Clear security context and re-authenticate
5. Check `@PreAuthorize` annotation syntax

### Data Scope Issues
1. Verify scope type and value are correct
2. Check effect (INCLUDE vs EXCLUDE)
3. Ensure resource entity has the scope field (location, department)
4. Multiple scopes: most specific takes precedence
5. Review query specification logic in `AuthorizationService.applyDataScopes()`

## API Documentation

Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
- `RoleType`: `SYSTEM`, `CUSTOM`
- `ScopeType`: `LOCATION`, `DEPARTMENT`, `ASSIGNMENT`, `ALL`
- `ScopeEffect`: `INCLUDE`, `EXCLUDE`
- `ResourceType`: `CAR`, `CLIENT`, `INQUIRY`, `RESERVATION`, `SALE`, etc.
- `SubjectType`: `ROLE`, `EMPLOYEE`
- `AccessLevel`: `READ`, `WRITE`, `ADMIN`

## Security Configuration

```java
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(STATELESS))
            .addFilterBefore(jwtAuthFilter, 
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

**Features:**
- JWT token authentication
- Token expiration: 24 hours (configurable)
- BCrypt password encoding
- Stateless session management
- Method-level security with `@PreAuthorize`

## Testing Examples

```java
@Test
public void testRolePermission() {
    // Create employee with SALES role
    Employee employee = createEmployee();
    Role salesRole = roleRepository.findByName("SALES");
    employee.setRoles(Set.of(salesRole));
    
    // Check permission
    boolean hasAccess = authService.hasPermission(
        employee.getId(), "inquiries:write"
    );
    
    assertTrue(hasAccess);
}

@Test
public void testDataScope() {
    // Create employee with location scope
    EmployeeDataScope scope = new EmployeeDataScope();
    scope.setEmployeeId(employeeId);
    scope.setScopeType(ScopeType.LOCATION);
    scope.setScopeValue("LOC-001");
    scope.setEffect(ScopeEffect.INCLUDE);
    dataScopeRepository.save(scope);
    
    // Apply scopes to query
    Specification<Car> spec = authService.applyDataScopes(
        employeeId, ResourceType.CAR
    );
    List<Car> cars = carRepository.findAll(spec);
    
    // Verify only LOC-001 cars returned
    assertTrue(cars.stream()
        .allMatch(car -> "LOC-001".equals(car.getLocation().getCode())));
}

@Test
public void testResourceACL() {
    // Grant employee access to specific car
    ResourceACL acl = new ResourceACL();
    acl.setResourceType(ResourceType.CAR);
    acl.setResourceId("CAR-123");
    acl.setSubjectType(SubjectType.EMPLOYEE);
    acl.setSubjectId(employeeId);
    acl.setAccessLevel(AccessLevel.WRITE);
    aclRepository.save(acl);
    
    // Check access
    boolean canEdit = authService.canAccessResource(
        employeeId, ResourceType.CAR, "CAR-123", AccessLevel.WRITE
    );
    
    assertTrue(canEdit);
}
```
2. Check effect (INCLUDE vs EXCLUDE)
3. Ensure resource has the scope field
4. Review scope precedence (most specific wins)

## Additional Resources
- API Documentation: http://localhost:8080/api/v1/swagger-ui.html

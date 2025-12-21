# Role-Based Access Control (RBAC)

## Overview

WheelShift Pro implements a comprehensive RBAC system with hierarchical roles, fine-grained permissions, data scopes, and resource-level access control lists (ACLs).

## Architecture

### Components

1. **Roles** - Define user roles in the system
2. **Permissions** - Fine-grained access controls (resource:action format)
3. **Data Scopes** - Filter data by location, department, or assignment
4. **Resource ACLs** - Individual resource-level access control

### Authorization Precedence

```
Resource ACL > Data Scope > Role Permission
```

## Built-in Roles

| Role | Description | Key Permissions |
|------|-------------|-----------------|
| **SUPER_ADMIN** | Full system access | All permissions |
| **ADMIN** | Administrative access | Manage employees, roles, locations |
| **SALES** | Sales operations | Inquiries, reservations, sales |
| **INSPECTOR** | Vehicle inspections | Car inspections, movements |
| **FINANCE** | Financial operations | Transactions, reports |
| **STORE_MANAGER** | Location management | Location-specific operations |

## Permission Format

Permissions follow the `resource:action` pattern:

```
cars:read       - Read car information
cars:write      - Create/update cars
cars:delete     - Delete cars
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

## Resource ACLs

Override role permissions for specific resources:

### Access Levels

- **READ** - View only
- **WRITE** - View and edit
- **ADMIN** - Full control

### Subject Types

- **ROLE** - Grant access to all users with a role
- **EMPLOYEE** - Grant access to a specific employee

### Example

```json
{
  "resourceType": "CAR",
  "resourceId": "CAR-123",
  "subjectType": "EMPLOYEE",
  "subjectId": 5,
  "accessLevel": "ADMIN"
}
```

Employee 5 has admin access to CAR-123, regardless of role permissions.

## API Usage

### Authentication

```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@wheelshift.com",
  "password": "password"
}

Response:
{
  "token": "eyJhbGc...",
  "employee": {...},
  "roles": ["ADMIN"],
  "permissions": ["cars:read", "cars:write", ...]
}
```

### Check Permissions

```java
@Autowired
private AuthorizationService authService;

// Check permission
boolean hasAccess = authService.hasPermission(employeeId, "cars:write");

// Check resource access
boolean canEdit = authService.canAccessResource(
    employeeId, 
    ResourceType.CAR, 
    carId, 
    AccessLevel.WRITE
);

// Get filtered query with data scopes
Specification<Car> spec = authService.applyDataScopes(
    employeeId, 
    ResourceType.CAR
);
```

### Role Management

```bash
# Create role
POST /api/v1/roles
{
  "name": "MANAGER",
  "description": "Store manager role",
  "permissionIds": [1, 2, 3, 4]
}

# Assign role to employee
POST /api/v1/employee-roles
{
  "employeeId": 5,
  "roleId": 2
}

# Get employee roles
GET /api/v1/employee-roles/employee/5
```

### Data Scope Management

```bash
# Create data scope
POST /api/v1/data-scopes
{
  "employeeId": 5,
  "scopeType": "LOCATION",
  "scopeValue": "LOC-001",
  "effect": "INCLUDE"
}

# Get employee scopes
GET /api/v1/data-scopes/employee/5
```

### Resource ACL Management

```bash
# Grant access to resource
POST /api/v1/resource-acl
{
  "resourceType": "CAR",
  "resourceId": "CAR-123",
  "subjectType": "EMPLOYEE",
  "subjectId": 5,
  "accessLevel": "WRITE"
}

# Get resource ACLs
GET /api/v1/resource-acl/resource/CAR/CAR-123
```

## Security Configuration

The system uses Spring Security with JWT tokens:

- **Token Expiration**: Configurable (default: 24 hours)
- **Password Encoding**: BCrypt
- **Method Security**: Enabled with `@PreAuthorize` and `@Secured`

### Protecting Endpoints

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

### Services (5)
- `RoleService`, `PermissionService`, `DataScopeService`, `ResourceACLService`, `AuthorizationService`

### Controllers (6)
- `AuthController`, `RoleController`, `PermissionController`, `EmployeeRoleController`, `DataScopeController`, `ResourceACLController`

### Security (2)
- `EmployeeUserDetails`, `EmployeeUserDetailsService`

## Troubleshooting

### Access Denied (403)

1. Check employee has required role
2. Verify role has required permission
3. Check data scopes aren't blocking access
4. Review resource ACLs for conflicts

### Permission Not Working

1. Verify permission exists in database
2. Check role-permission mapping
3. Ensure employee has the role assigned
4. Clear security context and re-authenticate

### Data Scope Issues

1. Verify scope type and value are correct
2. Check effect (INCLUDE vs EXCLUDE)
3. Ensure resource has the scope field
4. Review scope precedence (most specific wins)

## Additional Resources

- [Quick Start Guide](QUICK_START.md)
- [Implementation Details](IMPLEMENTATION.md)
- API Documentation: http://localhost:8080/api/v1/swagger-ui.html

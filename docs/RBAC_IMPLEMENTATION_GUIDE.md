# WheelShift RBAC Implementation Guide

## Overview

This document describes the Role-Based Access Control (RBAC) implementation in the WheelShift application. The system implements a comprehensive authorization model with hierarchical roles, fine-grained permissions, data scopes, and resource-level ACLs.

## Architecture

The RBAC system follows a hierarchical precedence model:

1. **SUPER_ADMIN Override** - Super admins have unrestricted access to all resources
2. **Explicit Resource ACL** - Per-record access control for exceptions
3. **Data Scope Match** - Location, department, or assignment-based filtering
4. **Role Permission Check** - Standard role-based permissions
5. **Deny** - Default deny if none of the above match

## Role Hierarchy

### System Roles

1. **SUPER_ADMIN**
   - Full system control
   - Manage admins, global settings, feature flags
   - Access audit logs
   - Override all access controls
   - Manage ACLs

2. **ADMIN**
   - Manage employees (non-admin)
   - Manage inventory, inquiries, reservations, sales
   - **Operates within assigned data scopes**
   - Cannot modify global settings
   - Cannot delete immutable financial records

3. **SALES**
   - Manage inquiries, reservations, sales
   - Limited to their scope or assignments
   - View and update clients
   - Access cars (read-only)

4. **INSPECTOR**
   - Create and update inspections
   - View cars
   - Limited inspection data access

5. **FINANCE**
   - View and record transactions
   - Generate financial reports
   - Access limited to department scope
   - View-only access to sales

6. **STORE_MANAGER**
   - Manage storage locations and movements
   - Move cars between locations
   - Limited to assigned locations

## Database Schema

### Core RBAC Tables

```sql
roles
├── id (BIGINT, PK)
├── name (VARCHAR(64), UNIQUE) -- RoleType enum
├── description (VARCHAR(256))
├── is_system (BOOLEAN)
└── timestamps

permissions
├── id (BIGINT, PK)
├── resource (VARCHAR(64))
├── action (VARCHAR(32))
├── name (VARCHAR(96), UNIQUE) -- format: "resource:action"
├── description (VARCHAR(256))
└── timestamps

role_permissions (junction table)
├── role_id (FK → roles.id)
└── permission_id (FK → permissions.id)

employee_roles (junction table)
├── employee_id (FK → employees.id)
└── role_id (FK → roles.id)

employee_data_scopes
├── id (BIGINT, PK)
├── employee_id (FK → employees.id)
├── scope_type (ENUM: LOCATION, DEPARTMENT, ASSIGNMENT)
├── scope_value (VARCHAR(128))
├── effect (ENUM: INCLUDE, EXCLUDE)
├── description (VARCHAR(512))
└── timestamps

resource_acl
├── id (BIGINT, PK)
├── resource_type (ENUM: CAR, CLIENT, INQUIRY, RESERVATION, SALE, TRANSACTION)
├── resource_id (BIGINT)
├── subject_type (ENUM: ROLE, EMPLOYEE)
├── subject_id (BIGINT)
├── access (ENUM: READ, WRITE, ADMIN)
├── reason (VARCHAR(512))
├── granted_by (BIGINT)
└── timestamps
```

## Permission Model

Permissions follow the format: `resource:action`

### Permission Examples

```
cars:read          - View car information
cars:write         - Create and update cars
cars:delete        - Delete cars
cars:manage        - Full car management

inquiries:read     - View inquiries
inquiries:write    - Create and update inquiries
inquiries:assign   - Assign inquiries to employees

reservations:convert - Convert reservations to sales

transactions:write - Record financial transactions

employees:manage   - Full employee management including role assignment

settings:manage    - Manage global settings (Super Admin only)
acl:manage        - Manage resource ACLs (Super Admin only)
audit:read        - View audit logs (Super Admin only)
```

## Data Scopes

Data scopes restrict employee access to specific subsets of data:

### Scope Types

1. **LOCATION** - Restrict to specific storage locations
   - Example: `LOCATION:Warehouse-1`
   - Admin can only access cars in Warehouse-1

2. **DEPARTMENT** - Restrict to specific departments
   - Example: `DEPARTMENT:Sales`
   - Finance analyst sees only Sales department transactions

3. **ASSIGNMENT** - Restrict to assigned records only
   - Example: `ASSIGNMENT:self`
   - Sales rep sees only their own inquiries and reservations

### Scope Effects

- **INCLUDE** - Allow access to matching data
- **EXCLUDE** - Deny access to matching data

## Resource ACLs

Resource ACLs provide fine-grained, per-record access control:

### Access Levels

- **READ** - View-only access
- **WRITE** - Read and modify access
- **ADMIN** - Full administrative access

### Example Use Cases

1. **Sensitive Car**: Restrict access to specific admins
   ```json
   {
     "resourceType": "CAR",
     "resourceId": 123,
     "subjectType": "EMPLOYEE",
     "subjectId": 456,
     "access": "READ"
   }
   ```

2. **High-Value Transaction**: Limit access to Finance team
   ```json
   {
     "resourceType": "TRANSACTION",
     "resourceId": 789,
     "subjectType": "ROLE",
     "subjectId": <FINANCE_ROLE_ID>,
     "access": "WRITE"
   }
   ```

## API Endpoints

### Authentication

```
POST /api/v1/auth/login
  Body: { email, password }
  Returns: { employeeId, email, name, roles, permissions }

GET /api/v1/auth/me
  Returns: Current user details

POST /api/v1/auth/logout
```

### Role Management (Super Admin Only)

```
GET    /api/v1/rbac/roles
POST   /api/v1/rbac/roles
GET    /api/v1/rbac/roles/{id}
PUT    /api/v1/rbac/roles/{id}
DELETE /api/v1/rbac/roles/{id}

POST   /api/v1/rbac/roles/{roleId}/permissions/{permissionId}
DELETE /api/v1/rbac/roles/{roleId}/permissions/{permissionId}
```

### Permission Management (Super Admin Only)

```
GET    /api/v1/rbac/permissions
POST   /api/v1/rbac/permissions
GET    /api/v1/rbac/permissions/{id}
PUT    /api/v1/rbac/permissions/{id}
DELETE /api/v1/rbac/permissions/{id}

GET /api/v1/rbac/permissions/role/{roleId}
GET /api/v1/rbac/permissions/employee/{employeeId}
```

### Employee Role Assignment (Admin/Super Admin)

```
GET    /api/v1/rbac/employees/{employeeId}/roles
POST   /api/v1/rbac/employees/{employeeId}/roles/{roleId}
DELETE /api/v1/rbac/employees/{employeeId}/roles/{roleId}
```

### Data Scope Management (Admin/Super Admin)

```
GET    /api/v1/rbac/employees/{employeeId}/scopes
POST   /api/v1/rbac/employees/{employeeId}/scopes
PUT    /api/v1/rbac/scopes/{scopeId}
DELETE /api/v1/rbac/scopes/{scopeId}
```

### Resource ACL Management (Super Admin/Resource Owner)

```
GET    /api/v1/rbac/acl/{resourceType}/{resourceId}
POST   /api/v1/rbac/acl/{resourceType}/{resourceId}
DELETE /api/v1/rbac/acl/{aclId}
DELETE /api/v1/rbac/acl/{resourceType}/{resourceId}
```

## Usage Examples

### 1. Create a Super Admin User

First, you need to run the migrations to create the RBAC tables and seed initial data:

```bash
# The migrations will automatically run on application startup
# V4__Add_RBAC_Tables.sql creates the tables
# V5__Seed_RBAC_Data.sql creates roles and permissions
```

Then create a super admin employee:

```sql
-- Create employee with super admin role
INSERT INTO employees (name, email, password_hash, status) 
VALUES ('Super Admin', 'admin@wheelshift.com', '$2a$10$...', 'ACTIVE');

-- Assign SUPER_ADMIN role
INSERT INTO employee_roles (employee_id, role_id)
SELECT e.id, r.id 
FROM employees e, roles r 
WHERE e.email = 'admin@wheelshift.com' AND r.name = 'SUPER_ADMIN';
```

### 2. Assign Roles to Employees

```bash
# Assign SALES role to employee
POST /api/v1/rbac/employees/123/roles/3
```

### 3. Add Location Scope to Admin

```bash
# Restrict admin to Warehouse-1 location
POST /api/v1/rbac/employees/456/scopes
{
  "scopeType": "LOCATION",
  "scopeValue": "Warehouse-1",
  "effect": "INCLUDE",
  "description": "Warehouse 1 manager"
}
```

### 4. Create Resource ACL

```bash
# Restrict car #789 to specific employee
POST /api/v1/rbac/acl/CAR/789
{
  "subjectType": "EMPLOYEE",
  "subjectId": 456,
  "access": "WRITE",
  "reason": "VIP client car - restricted access"
}
```

## Authorization Service

The `AuthorizationService` provides methods for checking permissions:

```java
@Autowired
private AuthorizationService authorizationService;

// Check permission
boolean canWrite = authorizationService.hasPermission(employeeId, "cars:write");

// Check role
boolean isSuperAdmin = authorizationService.isSuperAdmin(employeeId);

// Check location access
boolean hasAccess = authorizationService.hasLocationAccess(employeeId, "Warehouse-1");

// Check resource access
boolean canAccessCar = authorizationService.canAccessCar(employeeId, carId, "write");
```

## Security Configuration

The `SecurityConfig` class enables:

- Method-level security with `@PreAuthorize` and `@Secured` annotations
- BCrypt password encoding
- Stateless session management (ready for JWT)
- Custom UserDetailsService for loading employee data

### Using Method Security

```java
@PreAuthorize("hasRole('SUPER_ADMIN')")
public void deleteSensitiveData() {
    // Only super admins can execute this
}

@PreAuthorize("hasAuthority('cars:write')")
public void updateCar(Car car) {
    // Requires cars:write permission
}

@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public void manageEmployees() {
    // Requires admin or super admin role
}
```

## Migration and Seeding

### V4__Add_RBAC_Tables.sql
Creates all RBAC-related tables with proper constraints and indexes.

### V5__Seed_RBAC_Data.sql
Seeds:
- 6 system roles (SUPER_ADMIN, ADMIN, SALES, INSPECTOR, FINANCE, STORE_MANAGER)
- 40+ permissions covering all resources
- Role-permission mappings for each role

## Future Enhancements

1. **JWT Token Implementation**
   - Replace session-based auth with JWT
   - Add token refresh mechanism
   - Implement token blacklist for logout

2. **Audit Logging**
   - Track all permission changes
   - Log ACL modifications
   - Record role assignments

3. **Permission Caching**
   - Redis cache for permission lookups
   - Cache invalidation on role/permission changes

4. **Dynamic Permissions**
   - Allow admins to create custom permissions
   - Permission groups and categories

5. **Time-based Access**
   - Temporary role assignments
   - Scheduled permission grants

## Testing

### Manual Testing Steps

1. **Create test users with different roles**
2. **Test login with each user**
3. **Verify permission enforcement**:
   - Super Admin can access all endpoints
   - Admin can manage resources within scope
   - Sales can only access their assignments
   - Inspector can only create inspections
4. **Test data scopes**:
   - Create location-scoped admin
   - Verify they can only see cars in their location
5. **Test ACLs**:
   - Create resource ACL for a car
   - Verify only authorized users can access

## Troubleshooting

### Issue: Cannot login

**Solution**: Ensure password is properly hashed using BCrypt
```java
String hashedPassword = passwordEncoder.encode("plainPassword");
```

### Issue: 403 Forbidden

**Solution**: Check:
1. User has the required role/permission
2. Data scopes are properly configured
3. Resource ACL is not blocking access
4. Super Admin override is working

### Issue: Permissions not loading

**Solution**: 
1. Verify Flyway migrations ran successfully
2. Check employee_roles and role_permissions tables
3. Enable SQL logging to debug queries

## Best Practices

1. **Always use Super Admin for initial setup**
2. **Assign specific permissions, not just roles**
3. **Use data scopes for multi-location deployments**
4. **Document ACL reasons for audit purposes**
5. **Regularly review and clean up unused permissions**
6. **Test authorization logic thoroughly**
7. **Use the AuthorizationService for complex checks**
8. **Never hardcode permissions in business logic**

## Support

For questions or issues, refer to:
- API Documentation: `/swagger-ui.html`
- Product Requirements: `WheelShift - Product Requirements Document.md`
- RBAC Model: `WheelShift - RBAC Model.md`
- API Design: `WheelShift - API Design.md`

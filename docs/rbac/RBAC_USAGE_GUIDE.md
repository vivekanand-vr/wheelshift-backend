# RBAC Usage Guide

**Version:** 1.0  
**Last Updated:** January 2026  
**Author:** WheelShift Pro Development Team

---

## Table of Contents

1. [Overview](#overview)
2. [Core Concepts](#core-concepts)
3. [Getting Started](#getting-started)
4. [Managing Roles](#managing-roles)
5. [Managing Permissions](#managing-permissions)
6. [Data Scopes](#data-scopes)
7. [Resource ACLs](#resource-acls)
8. [Authorization in Code](#authorization-in-code)
9. [Common Use Cases](#common-use-cases)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Overview

### What is RBAC?

Role-Based Access Control (RBAC) is a security model that restricts system access based on roles assigned to users. WheelShift Pro implements a comprehensive RBAC system with:

- **Hierarchical Roles** - Predefined and custom roles
- **Fine-Grained Permissions** - Resource:action format (e.g., `cars:write`)
- **Data Scopes** - Location, department, and assignment-based filtering
- **Resource ACLs** - Per-record access control
- **Multi-Layer Authorization** - Flexible permission checking

### Authorization Hierarchy

```
┌─────────────────────────────────┐
│      SUPER_ADMIN Override       │  ← Always Granted
├─────────────────────────────────┤
│      Resource ACL Check         │  ← Explicit per-resource access
├─────────────────────────────────┤
│      Data Scope Match           │  ← Location/Department/Assignment
├─────────────────────────────────┤
│      Role Permission Check      │  ← Role-based permissions
├─────────────────────────────────┤
│           DENY                  │  ← Default deny
└─────────────────────────────────┘
```

---

## Core Concepts

### 1. Roles

**Definition:** Roles are collections of permissions assigned to employees.

**Built-in Roles:**
| Role | ID | Description | Typical Use |
|------|-------|-------------|-------------|
| **SUPER_ADMIN** | 1 | Full system access (`*:*`) | System administrator |
| **ADMIN** | 2 | Administrative functions | Office manager |
| **SALES** | 3 | Sales operations | Sales team |
| **INSPECTOR** | 4 | Vehicle inspections | QA team |
| **FINANCE** | 5 | Financial operations | Accounting |
| **STORE_MANAGER** | 6 | Location management | Site manager |

**Key Properties:**
- `name` - Role identifier (RoleType enum)
- `description` - Human-readable description
- `isSystem` - System role (cannot be deleted)
- `permissions` - Associated permissions

### 2. Permissions

**Definition:** Permissions define specific actions on resources.

**Format:** `resource:action`

**Examples:**
- `cars:read` - View cars
- `cars:write` - Create/update cars
- `cars:delete` - Delete cars
- `cars:*` - All car operations
- `*:*` - All operations (SUPER_ADMIN only)

**Available Resources:**
```
cars, car-models, clients, employees, inquiries, reservations,
sales, transactions, inspections, locations, tasks, events,
roles, permissions, acl, notifications
```

**Available Actions:**
```
read, write, delete, * (wildcard)
```

### 3. Data Scopes

**Definition:** Scopes restrict employee access to specific organizational boundaries.

**Scope Types:**

| Type | Description | Example Use Case |
|------|-------------|------------------|
| **LOCATION** | Limit to specific storage locations | Store manager sees only their location's inventory |
| **DEPARTMENT** | Limit to department resources | Finance sees only finance-related transactions |
| **ASSIGNMENT** | Limit to assigned resources | Sales rep sees only their assigned inquiries |

**Scope Effects:**
- **INCLUDE** - Grant access to specified scope
- **EXCLUDE** - Deny access to specified scope

### 4. Resource ACLs

**Definition:** Access Control Lists provide fine-grained, per-record access control.

**Access Levels:**
- **READ** - View only
- **WRITE** - View and modify
- **ADMIN** - Full control (including ACL management)

**Subject Types:**
- **EMPLOYEE** - Individual employee
- **ROLE** - All employees with a role
- **DEPARTMENT** - All employees in a department

---

## Getting Started

### Authentication & Token

All RBAC operations require authentication:

```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@wheelshift.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "employee": {
    "id": 1,
    "name": "Admin User",
    "email": "admin@wheelshift.com"
  },
  "roles": ["SUPER_ADMIN"],
  "permissions": ["*:*"]
}
```

**Use the token in subsequent requests:**
```bash
Authorization: Bearer <token>
```

### Default Credentials

| Role | Email | Password | Purpose |
|------|-------|----------|---------|
| Super Admin | super.admin@wheelshift.com | superadmin123 | Full system access |
| Admin | admin@wheelshift.com | admin123 | Administrative tasks |
| Sales | john.sales@wheelshift.com | sales123 | Sales operations |
| Inspector | mike.inspector@wheelshift.com | inspector123 | Inspections |
| Finance | sarah.finance@wheelshift.com | finance123 | Financial operations |

---

## Managing Roles

### Create a New Role

```bash
POST /api/v1/rbac/roles
Authorization: Bearer <super-admin-token>
Content-Type: application/json

{
  "name": "INVENTORY_MANAGER",
  "description": "Manages inventory and stock levels",
  "isSystem": false
}
```

**Response:**
```json
{
  "id": 7,
  "name": "INVENTORY_MANAGER",
  "description": "Manages inventory and stock levels",
  "isSystem": false,
  "permissions": [],
  "createdAt": "2026-01-10T10:00:00"
}
```

### Get All Roles

```bash
GET /api/v1/rbac/roles
Authorization: Bearer <token>
```

### Get Role by ID

```bash
GET /api/v1/rbac/roles/3
Authorization: Bearer <token>
```

### Update a Role

```bash
PUT /api/v1/rbac/roles/7
Authorization: Bearer <super-admin-token>
Content-Type: application/json

{
  "name": "INVENTORY_MANAGER",
  "description": "Manages inventory, stock levels, and locations"
}
```

### Delete a Role

```bash
DELETE /api/v1/rbac/roles/7
Authorization: Bearer <super-admin-token>
```

**Note:** System roles (isSystem=true) cannot be deleted.

### Assign Role to Employee

```bash
POST /api/v1/employees/{employeeId}/roles
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "roleIds": [3, 6]
}
```

This assigns SALES and STORE_MANAGER roles to the employee.

---

## Managing Permissions

### Create a Permission

```bash
POST /api/v1/rbac/permissions
Authorization: Bearer <super-admin-token>
Content-Type: application/json

{
  "resource": "inventory",
  "action": "audit",
  "description": "Perform inventory audits"
}
```

The permission name (`inventory:audit`) is automatically generated.

### Add Permission to Role

```bash
POST /api/v1/rbac/roles/7/permissions/15
Authorization: Bearer <super-admin-token>
```

This grants the permission (ID 15) to role INVENTORY_MANAGER (ID 7).

### Remove Permission from Role

```bash
DELETE /api/v1/rbac/roles/7/permissions/15
Authorization: Bearer <super-admin-token>
```

### Get All Permissions

```bash
GET /api/v1/rbac/permissions
Authorization: Bearer <token>
```

### Check Employee Permissions

```bash
GET /api/v1/employees/{employeeId}/permissions
Authorization: Bearer <token>
```

Returns all permissions the employee has (through all their roles).

---

## Data Scopes

### Create a Data Scope

#### Location Scope Example

```bash
POST /api/v1/rbac/data-scopes
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "employeeId": 5,
  "scopeType": "LOCATION",
  "scopeValue": "LOC-001",
  "effect": "INCLUDE",
  "description": "Access to Main Warehouse only"
}
```

**Result:** Employee 5 can only see resources (cars, inquiries, etc.) in location LOC-001.

#### Department Scope Example

```bash
POST /api/v1/rbac/data-scopes
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "employeeId": 8,
  "scopeType": "DEPARTMENT",
  "scopeValue": "FINANCE",
  "effect": "INCLUDE",
  "description": "Finance department only"
}
```

**Result:** Employee 8 can only see finance-related resources.

#### Assignment Scope Example

```bash
POST /api/v1/rbac/data-scopes
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "employeeId": 10,
  "scopeType": "ASSIGNMENT",
  "scopeValue": "SELF",
  "effect": "INCLUDE",
  "description": "Only assigned inquiries and reservations"
}
```

**Result:** Employee 10 can only see inquiries/reservations assigned to them.

### Get Employee's Data Scopes

```bash
GET /api/v1/rbac/data-scopes/employee/{employeeId}
Authorization: Bearer <token>
```

### Delete a Data Scope

```bash
DELETE /api/v1/rbac/data-scopes/{scopeId}
Authorization: Bearer <admin-token>
```

### Using Data Scopes in Queries

Data scopes are automatically applied in service layer:

```java
// In service implementation
Specification<Car> spec = authService.applyDataScopes(employeeId, ResourceType.CAR);
List<Car> cars = carRepository.findAll(spec);
```

---

## Resource ACLs

### Grant Resource Access

#### Grant Employee Access to a Specific Car

```bash
POST /api/v1/rbac/acl/CAR/123
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "subjectType": "EMPLOYEE",
  "subjectId": 10,
  "accessLevel": "WRITE",
  "reason": "Assigned to manage this vehicle"
}
```

**Result:** Employee 10 can read and modify car ID 123, regardless of their role permissions.

#### Grant Role Access to an Inquiry

```bash
POST /api/v1/rbac/acl/INQUIRY/456
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "subjectType": "ROLE",
  "subjectId": 3,
  "accessLevel": "READ",
  "reason": "Sales team oversight"
}
```

**Result:** All employees with SALES role can view inquiry ID 456.

### Get ACL for a Resource

```bash
GET /api/v1/rbac/acl/CAR/123
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "resourceType": "CAR",
    "resourceId": 123,
    "subjectType": "EMPLOYEE",
    "subjectId": 10,
    "accessLevel": "WRITE",
    "reason": "Assigned to manage this vehicle",
    "grantedBy": 1,
    "createdAt": "2026-01-10T10:00:00"
  }
]
```

### Remove ACL Entry

```bash
DELETE /api/v1/rbac/acl/1
Authorization: Bearer <admin-token>
```

### Remove All ACLs for a Resource

```bash
DELETE /api/v1/rbac/acl/CAR/123
Authorization: Bearer <super-admin-token>
```

**Use Case:** When deleting a resource or resetting permissions.

---

## Authorization in Code

### Using AuthorizationService

#### Basic Permission Check

```java
@Autowired
private AuthorizationService authService;

public void performAction(Long employeeId) {
    if (!authService.hasPermission(employeeId, "cars:write")) {
        throw new ForbiddenException("You don't have permission to modify cars");
    }
    
    // Perform the action
    carService.updateCar(carId, updateData);
}
```

#### Role Check

```java
if (authService.hasRole(employeeId, RoleType.SUPER_ADMIN)) {
    // Super admin specific logic
}

if (authService.hasAnyRole(employeeId, RoleType.ADMIN, RoleType.SUPER_ADMIN)) {
    // Admin or super admin logic
}
```

#### Resource-Level Access Check

```java
boolean canEdit = authService.hasResourceAccess(
    employeeId, 
    ResourceType.CAR, 
    carId, 
    AccessLevel.WRITE
);

if (!canEdit) {
    throw new ForbiddenException("You don't have write access to this car");
}
```

#### Location Access Check

```java
Car car = carRepository.findById(carId).orElseThrow();

if (!authService.hasLocationAccess(employeeId, car.getStorageLocation().getId())) {
    throw new ForbiddenException("You don't have access to this location");
}
```

#### Domain-Specific Authorization

```java
// Check car access (considers permissions, scopes, and ACLs)
boolean canAccessCar = authService.canAccessCar(employeeId, carId, "write");

// Check inquiry access (considers assignment)
boolean canAccessInquiry = authService.canAccessInquiry(employeeId, inquiryId, "read");

// Check transaction access (considers department scopes)
boolean canAccessTransaction = authService.canAccessTransaction(employeeId, transactionId, "delete");
```

### Securing Endpoints with @PreAuthorize

```java
@RestController
@RequestMapping("/api/v1/cars")
public class CarController {

    // Only users with cars:read permission
    @PreAuthorize("hasAuthority('cars:read')")
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {
        // ...
    }

    // Only admins or super admins
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<CarResponse> createCar(@RequestBody CarRequest request) {
        // ...
    }

    // Only super admins
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        // ...
    }
}
```

### Custom Security Expression

For more complex checks, use `AuthorizationService` in the method:

```java
@PutMapping("/{id}")
public ResponseEntity<CarResponse> updateCar(
        @PathVariable Long id,
        @RequestBody CarRequest request,
        @AuthenticationPrincipal EmployeeDetails employeeDetails) {
    
    Long employeeId = employeeDetails.getEmployeeId();
    
    // Check if employee can access this car
    if (!authService.canAccessCar(employeeId, id, "write")) {
        throw new ForbiddenException("You don't have permission to modify this car");
    }
    
    CarResponse response = carService.updateCar(id, request);
    return ResponseEntity.ok(response);
}
```

---

## Common Use Cases

### Use Case 1: Onboarding a New Employee

**Scenario:** New sales representative joins, needs access to sales operations.

**Steps:**

1. **Create employee account:**
```bash
POST /api/v1/employees
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@wheelshift.com",
  "password": "temp123",
  "position": "Sales Representative"
}
```

2. **Assign SALES role:**
```bash
POST /api/v1/employees/15/roles
{
  "roleIds": [3]
}
```

3. **Set location scope (if needed):**
```bash
POST /api/v1/rbac/data-scopes
{
  "employeeId": 15,
  "scopeType": "LOCATION",
  "scopeValue": "LOC-002",
  "effect": "INCLUDE",
  "description": "Downtown showroom only"
}
```

**Result:** Jane can perform sales operations but only sees cars/inquiries in location LOC-002.

### Use Case 2: Temporary Access to a Specific Resource

**Scenario:** Inspector needs temporary access to review a specific car's inspection history.

**Steps:**

1. **Grant ACL access:**
```bash
POST /api/v1/rbac/acl/CAR/789
{
  "subjectType": "EMPLOYEE",
  "subjectId": 12,
  "accessLevel": "READ",
  "reason": "Review for certification renewal"
}
```

**Result:** Employee 12 can view car 789 even if it's outside their normal scope.

2. **Revoke access after review:**
```bash
DELETE /api/v1/rbac/acl/{aclId}
```

### Use Case 3: Department-Restricted Access

**Scenario:** Finance team should only see financial transactions, not sales or inquiries.

**Steps:**

1. **Assign FINANCE role:**
```bash
POST /api/v1/employees/20/roles
{
  "roleIds": [5]
}
```

2. **Set department scope:**
```bash
POST /api/v1/rbac/data-scopes
{
  "employeeId": 20,
  "scopeType": "DEPARTMENT",
  "scopeValue": "FINANCE",
  "effect": "INCLUDE"
}
```

**Result:** Employee sees only finance-related resources.

### Use Case 4: Multi-Role Employee

**Scenario:** Store manager also handles inspections at their location.

**Steps:**

1. **Assign multiple roles:**
```bash
POST /api/v1/employees/25/roles
{
  "roleIds": [4, 6]
}
```

Employee now has:
- INSPECTOR permissions (`inspections:*`, `cars:read`, `movements:write`)
- STORE_MANAGER permissions (`cars:*`, `movements:*`, `locations:read`, `tasks:*`)

2. **Set location scope:**
```bash
POST /api/v1/rbac/data-scopes
{
  "employeeId": 25,
  "scopeType": "LOCATION",
  "scopeValue": "LOC-003",
  "effect": "INCLUDE"
}
```

**Result:** Employee can perform both inspection and management tasks, but only for location LOC-003.

### Use Case 5: Creating a Custom Role

**Scenario:** Need a "Customer Service" role with specific permissions.

**Steps:**

1. **Create the role:**
```bash
POST /api/v1/rbac/roles
{
  "name": "CUSTOMER_SERVICE",
  "description": "Customer support and inquiry management"
}
```

2. **Grant required permissions:**
```bash
POST /api/v1/rbac/roles/7/permissions/{permissionId}
```

Grant these permissions:
- `inquiries:read`
- `inquiries:write`
- `clients:read`
- `clients:write`
- `reservations:read`

3. **Assign role to employees:**
```bash
POST /api/v1/employees/30/roles
{
  "roleIds": [7]
}
```

---

## Best Practices

### 1. Principle of Least Privilege

✅ **DO:**
- Grant only the minimum permissions needed
- Use specific permissions (`cars:read`) over wildcards (`cars:*`)
- Apply data scopes to limit visibility

❌ **DON'T:**
- Give everyone SUPER_ADMIN or ADMIN roles
- Use `*:*` permission except for SUPER_ADMIN
- Skip permission checks in code

### 2. Use Data Scopes Appropriately

✅ **DO:**
- Apply location scopes for multi-site operations
- Use assignment scopes for sales/service tracking
- Combine scopes with permissions for fine control

❌ **DON'T:**
- Over-restrict with too many exclude scopes
- Forget to apply scopes in query methods
- Mix conflicting INCLUDE/EXCLUDE scopes

### 3. Resource ACLs for Exceptions

✅ **DO:**
- Use ACLs for temporary access grants
- Document ACL reason in the `reason` field
- Remove ACLs when no longer needed

❌ **DON'T:**
- Use ACLs as the primary authorization method
- Create ACLs without a specific justification
- Forget to audit ACL entries periodically

### 4. Role Management

✅ **DO:**
- Use system roles for standard positions
- Create custom roles for unique needs
- Document role purposes clearly
- Review role-permission mappings regularly

❌ **DON'T:**
- Modify system roles in production
- Create too many overlapping roles
- Assign roles without understanding permissions

### 5. Security in Code

✅ **DO:**
- Always check authorization before operations
- Use `AuthorizationService` for complex checks
- Apply data scopes in repository queries
- Handle authorization failures gracefully

❌ **DON'T:**
- Rely solely on `@PreAuthorize` for complex logic
- Skip authorization checks in service methods
- Expose resource IDs without access checks
- Trust client-provided employee IDs

### 6. Audit and Monitoring

✅ **DO:**
- Log permission denials
- Monitor ACL grants/revocations
- Audit role assignments periodically
- Track super admin actions

❌ **DON'T:**
- Ignore authorization failures
- Leave temporary ACLs indefinitely
- Skip logging in authorization code

---

## Troubleshooting

### Problem: "Access Denied" Error

**Symptom:**
```json
{
  "status": 403,
  "title": "Forbidden",
  "detail": "Access Denied",
  "code": "ACCESS_DENIED"
}
```

**Solutions:**

1. **Check employee roles:**
```bash
GET /api/v1/employees/{employeeId}
```

2. **Check permissions:**
```bash
GET /api/v1/employees/{employeeId}/permissions
```

3. **Check data scopes:**
```bash
GET /api/v1/rbac/data-scopes/employee/{employeeId}
```

4. **Check resource ACL:**
```bash
GET /api/v1/rbac/acl/{resourceType}/{resourceId}
```

### Problem: Employee Can't See Resources

**Symptom:** Employee has permission but resources don't appear in lists.

**Likely Cause:** Data scope is filtering resources.

**Solution:**

1. **Check location scopes:**
```bash
GET /api/v1/rbac/data-scopes/employee/{employeeId}?scopeType=LOCATION
```

2. **Verify resource location matches scope:**
```bash
GET /api/v1/cars/{carId}
# Check storageLocation.id matches scope value
```

3. **Add or remove scope as needed:**
```bash
POST /api/v1/rbac/data-scopes  # Add new scope
DELETE /api/v1/rbac/data-scopes/{scopeId}  # Remove restrictive scope
```

### Problem: Permission Denied Despite Correct Role

**Symptom:** Employee has the right role but still gets "Access Denied".

**Likely Cause:** Role doesn't have the specific permission.

**Solution:**

1. **Check role permissions:**
```bash
GET /api/v1/rbac/roles/{roleId}
```

2. **Verify permission exists:**
```bash
GET /api/v1/rbac/permissions?resource=cars&action=write
```

3. **Add permission to role:**
```bash
POST /api/v1/rbac/roles/{roleId}/permissions/{permissionId}
```

### Problem: Unable to Delete Role

**Symptom:**
```json
{
  "status": 409,
  "title": "Conflict",
  "detail": "Cannot delete system role",
  "code": "SYSTEM_ROLE_DELETE"
}
```

**Cause:** Attempting to delete a system role (isSystem=true).

**Solution:** System roles cannot be deleted. Modify permissions instead or create a custom role.

### Problem: ACL Not Taking Effect

**Symptom:** ACL entry exists but authorization still fails.

**Solutions:**

1. **Verify ACL access level:**
   - READ < WRITE < ADMIN
   - Ensure access level is sufficient for the operation

2. **Check authorization code:**
```java
// Make sure code checks ACL
boolean hasAccess = authService.hasResourceAccess(
    employeeId, resourceType, resourceId, AccessLevel.WRITE
);
```

3. **Verify ACL subject:**
   - EMPLOYEE ACLs apply directly
   - ROLE ACLs apply to all employees with that role
   - Check subject ID matches employee/role

### Problem: Super Admin Still Denied

**Symptom:** Employee with SUPER_ADMIN role gets "Access Denied".

**Likely Cause:** Code doesn't check for SUPER_ADMIN override.

**Solution:** Ensure authorization code includes:
```java
if (authService.isSuperAdmin(employeeId)) {
    return true;  // Grant access
}
```

---

## Additional Resources

- **Implementation Summary:** See `RBAC_IMPLEMENTATION_SUMMARY.md` for technical details
- **API Reference:** Check OpenAPI documentation at `/swagger-ui.html`
- **Database Schema:** Review migration files in `src/main/resources/db/migration/V4__Add_RBAC_Tables.sql`
- **Source Code:** 
  - Entities: `src/main/java/com/wheelshiftpro/entity/`
  - Services: `src/main/java/com/wheelshiftpro/service/`
  - Controllers: `src/main/java/com/wheelshiftpro/controller/`

---

**Need Help?** Contact the development team or check the RBAC implementation summary for technical details.

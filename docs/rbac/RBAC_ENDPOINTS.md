# 🌐 RBAC API Endpoints Reference

**Version:** 1.0  
**Last Updated:** January 18, 2026  
**Base URL:** `/api/v1`

---

## 📋 Table of Contents

- [Authentication](#authentication)
- [Roles](#roles)
- [Permissions](#permissions)
- [Employee Roles](#employee-roles)
- [Data Scopes](#data-scopes)
- [Resource ACLs](#resource-acls)
- [Authorization Checks](#authorization-checks)

---

## 🔐 Authentication

### Login
```
POST /api/v1/auth/login
```

### Logout
```
POST /api/v1/auth/logout
```

---

## 👥 Roles

### Create Role
```
POST /api/v1/rbac/roles
```
**Required:** Super Admin

### Update Role
```
PUT /api/v1/rbac/roles/{roleId}
```
**Required:** Super Admin

### Delete Role
```
DELETE /api/v1/rbac/roles/{roleId}
```
**Required:** Super Admin

### Get Role by ID
```
GET /api/v1/rbac/roles/{roleId}
```

### Get Role by Name
```
GET /api/v1/rbac/roles/name/{name}
```

### Get All Roles
```
GET /api/v1/rbac/roles
```

### Add Permission to Role
```
POST /api/v1/rbac/roles/{roleId}/permissions/{permissionId}
```
**Required:** Super Admin

### Remove Permission from Role
```
DELETE /api/v1/rbac/roles/{roleId}/permissions/{permissionId}
```
**Required:** Super Admin

---

## 🔑 Permissions

### Create Permission
```
POST /api/v1/rbac/permissions
```
**Required:** Super Admin

### Update Permission
```
PUT /api/v1/rbac/permissions/{permissionId}
```
**Required:** Super Admin

### Delete Permission
```
DELETE /api/v1/rbac/permissions/{permissionId}
```
**Required:** Super Admin

### Get Permission by ID
```
GET /api/v1/rbac/permissions/{permissionId}
```

### Get Permission by Name
```
GET /api/v1/rbac/permissions/name/{name}
```

### Get All Permissions
```
GET /api/v1/rbac/permissions
```

### Get Permissions by Role
```
GET /api/v1/rbac/permissions/role/{roleId}
```

### Get Permissions by Employee
```
GET /api/v1/rbac/permissions/employee/{employeeId}
```

---

## 👤 Employee Roles

### Get Employee Roles
```
GET /api/v1/rbac/employees/{employeeId}/roles
```

### Assign Role to Employee
```
POST /api/v1/rbac/employees/{employeeId}/roles/{roleId}
```
**Required:** Admin or Super Admin

### Remove Role from Employee
```
DELETE /api/v1/rbac/employees/{employeeId}/roles/{roleId}
```
**Required:** Admin or Super Admin

---

## 📍 Data Scopes

### Get Employee Scopes
```
GET /api/v1/rbac/employees/{employeeId}/scopes
```

### Get Scope by ID
```
GET /api/v1/rbac/employees/scopes/{scopeId}
```

### Add Scope to Employee
```
POST /api/v1/rbac/employees/{employeeId}/scopes
```
**Required:** Admin or Super Admin

### Update Scope
```
PUT /api/v1/rbac/employees/scopes/{scopeId}
```
**Required:** Admin or Super Admin

### Delete Scope
```
DELETE /api/v1/rbac/employees/scopes/{scopeId}
```
**Required:** Admin or Super Admin

---

## 🔒 Resource ACLs

### Get ACL for Resource
```
GET /api/v1/rbac/acl/{resourceType}/{resourceId}
```
**Example:** `GET /api/v1/rbac/acl/CAR/123`

### Add ACL Entry
```
POST /api/v1/rbac/acl/{resourceType}/{resourceId}
```
**Required:** Super Admin or Resource Owner

### Remove ACL Entry
```
DELETE /api/v1/rbac/acl/{aclId}
```
**Required:** Super Admin or Resource Owner

### Remove All ACLs for Resource
```
DELETE /api/v1/rbac/acl/{resourceType}/{resourceId}
```
**Required:** Super Admin

---

## ✅ Authorization Checks

### Check Permission (in code)
```java
authService.hasPermission(employeeId, "cars:write")
```

### Check Role (in code)
```java
authService.hasRole(employeeId, RoleType.SALES)
```

### Check Resource Access (in code)
```java
authService.canAccessCar(employeeId, carId, "write")
```

### Check Location Access (in code)
```java
authService.hasLocationAccess(employeeId, "LOC-001")
```

---

## 📝 Resource Types

```
CAR
CAR_MODEL
CLIENT
EMPLOYEE
INQUIRY
RESERVATION
SALE
TRANSACTION
INSPECTION
LOCATION
TASK
EVENT
ROLE
PERMISSION
ACL
NOTIFICATION
MOTORCYCLE
MOTORCYCLE_MODEL
```

---

## 🎯 Access Levels

```
READ    - View only
WRITE   - View and modify
ADMIN   - Full control
```

---

## 🏷️ Scope Types

```
LOCATION    - Limit by storage location
DEPARTMENT  - Limit by department
ASSIGNMENT  - Limit to assigned items
```

---

## 🔄 Scope Effects

```
INCLUDE - Whitelist (only show these)
EXCLUDE - Blacklist (hide these)
```

---

## 👥 Subject Types

```
EMPLOYEE    - Specific employee
ROLE        - All employees with a role
DEPARTMENT  - All employees in a department
```

---

## 📊 Built-in Roles

```
SUPER_ADMIN     (ID: 1)
ADMIN           (ID: 2)
SALES           (ID: 3)
INSPECTOR       (ID: 4)
FINANCE         (ID: 5)
STORE_MANAGER   (ID: 6)
```

---

## 🔗 Complete Endpoint List (Copy-Paste Ready)

### Authentication
```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
```

### Roles
```
POST   /api/v1/rbac/roles
PUT    /api/v1/rbac/roles/{roleId}
DELETE /api/v1/rbac/roles/{roleId}
GET    /api/v1/rbac/roles/{roleId}
GET    /api/v1/rbac/roles/name/{name}
GET    /api/v1/rbac/roles
POST   /api/v1/rbac/roles/{roleId}/permissions/{permissionId}
DELETE /api/v1/rbac/roles/{roleId}/permissions/{permissionId}
```

### Permissions
```
POST   /api/v1/rbac/permissions
PUT    /api/v1/rbac/permissions/{permissionId}
DELETE /api/v1/rbac/permissions/{permissionId}
GET    /api/v1/rbac/permissions/{permissionId}
GET    /api/v1/rbac/permissions/name/{name}
GET    /api/v1/rbac/permissions
GET    /api/v1/rbac/permissions/role/{roleId}
GET    /api/v1/rbac/permissions/employee/{employeeId}
```

### Employee Roles
```
GET    /api/v1/rbac/employees/{employeeId}/roles
POST   /api/v1/rbac/employees/{employeeId}/roles/{roleId}
DELETE /api/v1/rbac/employees/{employeeId}/roles/{roleId}
```

### Data Scopes
```
GET    /api/v1/rbac/employees/{employeeId}/scopes
GET    /api/v1/rbac/employees/scopes/{scopeId}
POST   /api/v1/rbac/employees/{employeeId}/scopes
PUT    /api/v1/rbac/employees/scopes/{scopeId}
DELETE /api/v1/rbac/employees/scopes/{scopeId}
```

### Resource ACLs
```
GET    /api/v1/rbac/acl/{resourceType}/{resourceId}
POST   /api/v1/rbac/acl/{resourceType}/{resourceId}
DELETE /api/v1/rbac/acl/{aclId}
DELETE /api/v1/rbac/acl/{resourceType}/{resourceId}
```

---

## 📖 See Also

- [Complete Beginner's Guide](./RBAC_COMPLETE_GUIDE.md)
- [Implementation Summary](../../rbac/RBAC_IMPLEMENTATION_SUMMARY.md)
- [Usage Guide](../../rbac/RBAC_USAGE_GUIDE.md)
- [Helper Methods](./RBAC_HELPER_METHODS.md)

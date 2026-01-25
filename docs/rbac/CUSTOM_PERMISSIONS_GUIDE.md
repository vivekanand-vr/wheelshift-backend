# Custom Employee Permissions Guide

## 📋 Overview

The **Custom Employee Permissions** feature allows super admins to assign permissions **directly to individual employees**, independent of their roles. This provides ultimate flexibility for edge cases where an employee needs special access beyond their role's default permissions.

### Use Cases

- **Cross-functional access**: A SALES employee needs temporary access to manage cars
- **Special projects**: An employee needs finance permissions for a specific task
- **Emergency access**: Grant temporary elevated permissions without role changes
- **Training**: Give trainees specific permissions without full role access

---

## 🏗️ System Architecture

### How It Works

```
┌──────────────────────────────────────────────────────────────┐
│                    Employee Permissions                       │
│                     (Final Computed)                          │
└──────────────────────────────────────────────────────────────┘
                            ↑
                            │
        ┌───────────────────┴───────────────────┐
        │                                       │
┌───────┴────────┐                   ┌─────────┴──────────┐
│  Role-Based    │                   │  Custom Employee   │
│  Permissions   │                   │   Permissions      │
│                │                   │                    │
│ • From roles   │      MERGED       │ • Directly         │
│   assigned to  │  ─────────────>   │   assigned by      │
│   employee     │                   │   super admin      │
│ • Standard     │                   │ • Independent      │
│   permissions  │                   │   of roles         │
└────────────────┘                   └────────────────────┘
```

### Permission Resolution Flow

1. **Employee makes a request** to access a resource
2. **System checks role-based permissions** (from employee's assigned roles)
3. **System checks custom permissions** (directly assigned to employee)
4. **Permission granted** if found in EITHER role-based OR custom permissions

---

## 📊 Database Schema

### `employee_permissions` Table

```sql
CREATE TABLE employee_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_by BIGINT NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id),
    FOREIGN KEY (granted_by) REFERENCES employees(id),
    
    UNIQUE KEY unique_employee_permission (employee_id, permission_id)
);
```

**Fields Explained:**
- `employee_id`: The employee receiving the custom permission
- `permission_id`: The permission being granted
- `granted_by`: The super admin who granted the permission (audit trail)
- `reason`: Optional explanation for why this permission was granted
- Timestamps automatically track when permissions are created/updated

---

## 🔧 API Endpoints

### Base URL
```
/api/v1/rbac/employee-permissions
```

### 1. Assign Custom Permission to Employee

**Endpoint:** `POST /employees/{employeeId}`

**Authorization:** `rbac:write` (Super Admin only)

**Request Body:**
```json
{
  "permissionId": 15,
  "reason": "Temporary access for Q4 sales campaign"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "employeeId": 5,
  "employeeFirstName": "John",
  "employeeLastName": "Doe",
  "permissionId": 15,
  "permissionName": "cars:write",
  "permissionDescription": "Create and update cars",
  "grantedById": 1,
  "grantedByFirstName": "Admin",
  "grantedByLastName": "User",
  "reason": "Temporary access for Q4 sales campaign",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

**Example:**
```bash
# Assign "cars:write" permission to employee #5
curl -X POST http://localhost:8080/api/v1/rbac/employee-permissions/employees/5 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionId": 15,
    "reason": "Temporary access for Q4 sales campaign"
  }'
```

---

### 2. Remove Custom Permission from Employee

**Endpoint:** `DELETE /employees/{employeeId}/permissions/{permissionId}`

**Authorization:** `rbac:write` (Super Admin only)

**Response:** `204 No Content`

**Example:**
```bash
# Remove "cars:write" permission from employee #5
curl -X DELETE http://localhost:8080/api/v1/rbac/employee-permissions/employees/5/permissions/15 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Get Employee's Custom Permissions

**Endpoint:** `GET /employees/{employeeId}`

**Authorization:** `rbac:read`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "employeeId": 5,
    "employeeFirstName": "John",
    "employeeLastName": "Doe",
    "permissionId": 15,
    "permissionName": "cars:write",
    "permissionDescription": "Create and update cars",
    "grantedById": 1,
    "grantedByFirstName": "Admin",
    "grantedByLastName": "User",
    "reason": "Temporary access for Q4 sales campaign",
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  },
  {
    "id": 2,
    "employeeId": 5,
    "permissionId": 22,
    "permissionName": "transactions:read",
    "permissionDescription": "View transaction records",
    "grantedById": 1,
    "grantedByFirstName": "Admin",
    "grantedByLastName": "User",
    "reason": "Training on finance processes",
    "createdAt": "2025-01-16T14:20:00",
    "updatedAt": "2025-01-16T14:20:00"
  }
]
```

---

### 4. Get Employee's Custom Permission Names

**Endpoint:** `GET /employees/{employeeId}/permission-names`

**Authorization:** `rbac:read`

**Response:** `200 OK`
```json
[
  "cars:write",
  "transactions:read",
  "reservations:delete"
]
```

**Use Case:** Quick check to see what custom permissions an employee has (just the names).

---

### 5. Remove All Custom Permissions

**Endpoint:** `DELETE /employees/{employeeId}`

**Authorization:** `rbac:write` (Super Admin only)

**Response:** `204 No Content`

**Example:**
```bash
# Remove all custom permissions from employee #5
curl -X DELETE http://localhost:8080/api/v1/rbac/employee-permissions/employees/5 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 6. Get Employee Permission by ID

**Endpoint:** `GET /{id}`

**Authorization:** `rbac:read`

**Response:** `200 OK`
```json
{
  "id": 1,
  "employeeId": 5,
  "employeeFirstName": "John",
  "employeeLastName": "Doe",
  "permissionId": 15,
  "permissionName": "cars:write",
  "permissionDescription": "Create and update cars",
  "grantedById": 1,
  "grantedByFirstName": "Admin",
  "grantedByLastName": "User",
  "reason": "Temporary access for Q4 sales campaign",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

---

## 💻 Code Examples

### Backend - Check if Employee Has Permission

The system automatically checks BOTH role-based and custom permissions:

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final PermissionService permissionService;
    
    public void performAction(Long employeeId) {
        // This checks BOTH role permissions AND custom permissions
        boolean hasAccess = permissionService.hasPermission(employeeId, "cars:write");
        
        if (!hasAccess) {
            throw new AccessDeniedException("You don't have permission to write cars");
        }
        
        // Proceed with action
    }
}
```

### Get All Employee Permissions (Role + Custom)

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final PermissionService permissionService;
    
    public Set<String> getEmployeeAllPermissions(Long employeeId) {
        // Returns role-based + custom permissions combined
        return permissionService.getEmployeePermissions(employeeId);
        
        // Example output: 
        // ["cars:read", "cars:write", "transactions:read", "reservations:delete"]
        //  ↑ from role   ↑ from role   ↑ custom          ↑ custom
    }
}
```

### Assign Custom Permission Programmatically

```java
@Service
@RequiredArgsConstructor
public class AdminService {
    private final EmployeePermissionService employeePermissionService;
    
    public void grantTemporaryAccess(Long employeeId, Long permissionId, Long adminId) {
        EmployeePermissionRequest request = new EmployeePermissionRequest();
        request.setPermissionId(permissionId);
        request.setReason("Temporary access for special project");
        
        EmployeePermissionResponse response = employeePermissionService
            .assignPermissionToEmployee(employeeId, request, adminId);
            
        log.info("Granted permission {} to employee {}", 
                 response.getPermissionName(), response.getEmployeeId());
    }
}
```

---

## 🎯 Practical Examples

### Example 1: Grant SALES Employee Finance Access

**Scenario:** John (SALES employee) needs to view transactions for a special project.

**Steps:**

1. **Find the permission ID** for `transactions:read`:
```bash
GET /api/v1/rbac/permissions
# Look for: { "id": 22, "name": "transactions:read" }
```

2. **Assign the permission**:
```bash
POST /api/v1/rbac/employee-permissions/employees/5
{
  "permissionId": 22,
  "reason": "Special project: Q4 revenue analysis"
}
```

3. **Verify**:
```bash
GET /api/v1/rbac/employee-permissions/employees/5/permission-names
# Response includes: ["transactions:read"]
```

Now John can view transactions even though his SALES role doesn't include this permission!

---

### Example 2: Temporary Car Management Access

**Scenario:** Sarah (FINANCE employee) needs to manage cars during a transition period.

**Steps:**

1. **Assign multiple car permissions**:
```bash
# Assign cars:read
POST /api/v1/rbac/employee-permissions/employees/8
{ "permissionId": 14, "reason": "Transition period" }

# Assign cars:write
POST /api/v1/rbac/employee-permissions/employees/8
{ "permissionId": 15, "reason": "Transition period" }

# Assign cars:delete
POST /api/v1/rbac/employee-permissions/employees/8
{ "permissionId": 16, "reason": "Transition period" }
```

2. **After transition, remove all**:
```bash
DELETE /api/v1/rbac/employee-permissions/employees/8
```

---

### Example 3: Emergency Access

**Scenario:** Emergency! Regular admin is unavailable. Grant another employee temporary admin access.

```bash
# Grant rbac:write permission
POST /api/v1/rbac/employee-permissions/employees/12
{
  "permissionId": 3,
  "reason": "Emergency: Regular admin unavailable until 2025-01-20"
}
```

---

## ⚠️ Important Notes

### 1. **Custom Permissions are ADDITIVE**

Custom permissions are **added to** (not replacing) role-based permissions:

```
Employee's Final Permissions = Role Permissions + Custom Permissions
```

**Example:**
- Role gives: `["cars:read", "reservations:read"]`
- Custom adds: `["cars:write", "transactions:read"]`
- **Total**: `["cars:read", "cars:write", "reservations:read", "transactions:read"]`

### 2. **Duplicates are Prevented**

You cannot assign a custom permission if the employee already has it through their role:

```bash
# Employee already has "cars:read" from SALES role
POST /api/v1/rbac/employee-permissions/employees/5
{ "permissionId": 14 }  # cars:read

# Response: 400 Bad Request
# "Permission cars:read is already assigned to employee John Doe"
```

### 3. **Super Admin Only**

Only employees with `rbac:write` permission (typically Super Admins) can:
- Assign custom permissions
- Remove custom permissions
- View custom permissions (requires `rbac:read`)

### 4. **Audit Trail**

Every custom permission includes:
- Who granted it (`grantedById`)
- Why it was granted (`reason`)
- When it was granted (`createdAt`)

This ensures full accountability!

### 5. **No Role Required**

Custom permissions work even if an employee has **no roles assigned**. This is useful for very specific access scenarios.

---

## 🔍 Troubleshooting

### Permission Not Taking Effect

**Problem:** Assigned custom permission but employee still can't access resource.

**Solutions:**
1. Check if permission name is correct:
   ```bash
   GET /api/v1/rbac/employee-permissions/employees/{id}/permission-names
   ```

2. Verify permission exists:
   ```bash
   GET /api/v1/rbac/permissions/{permissionId}
   ```

3. Check @PreAuthorize annotation:
   ```java
   // Make sure controller uses correct permission name
   @PreAuthorize("hasAuthority('cars:write')")
   ```

4. Clear cache if using caching (future enhancement)

---

### Cannot Assign Permission

**Problem:** Getting error when trying to assign permission.

**Possible causes:**
- Employee doesn't exist (404)
- Permission doesn't exist (404)
- Permission already assigned (400 - DuplicateResourceException)
- Not authorized (403)

**Check:**
```bash
# Verify employee exists
GET /api/v1/employees/{employeeId}

# Verify permission exists
GET /api/v1/rbac/permissions/{permissionId}

# Check if already assigned
GET /api/v1/rbac/employee-permissions/employees/{employeeId}
```

---

## 📚 Related Documentation

- [RBAC Complete Guide](RBAC_COMPLETE_GUIDE.md) - Core RBAC concepts
- [RBAC Endpoints](RBAC_ENDPOINTS.md) - All RBAC API endpoints
- [RBAC Helper Methods](RBAC_HELPER_METHODS.md) - Convenience methods for authorization

---

## 🎓 Best Practices

### ✅ DO:
- **Document the reason** when assigning custom permissions
- **Review custom permissions regularly** and remove when no longer needed
- **Use custom permissions sparingly** - prefer role-based access when possible
- **Set expiration reminders** for temporary custom permissions
- **Audit custom permissions** periodically for security compliance

### ❌ DON'T:
- **Don't use as default approach** - roles should handle most cases
- **Don't forget to remove** when access is no longer needed
- **Don't grant without reason** - always explain why in the `reason` field
- **Don't bypass role management** - if many employees need a permission, add it to a role
- **Don't grant excessive permissions** - follow principle of least privilege

---

## 🚀 Future Enhancements

Possible future improvements:
- **Expiration dates** for custom permissions (auto-revoke after date)
- **Permission request workflow** (employees request, admins approve)
- **Bulk assignment** (assign multiple permissions at once)
- **Permission templates** (common permission sets for quick assignment)
- **Activity log** (track when custom permissions are used)

---

## 📞 Support

For questions or issues:
1. Check the [RBAC Complete Guide](RBAC_COMPLETE_GUIDE.md)
2. Review [Your Questions Answered](YOUR_QUESTIONS_ANSWERED.md)
3. Contact your system administrator

---

*Last Updated: January 2025*
*Version: 1.0*

# 🔐 WheelShiftPro RBAC - Complete Beginner's Guide

**Version:** 2.0  
**Last Updated:** January 18, 2026  
**Difficulty Level:** Beginner to Intermediate

---

## 📋 Table of Contents

1. [Introduction](#introduction)
2. [What is RBAC?](#what-is-rbac)
3. [Core Concepts Explained](#core-concepts-explained)
4. [How Authorization Works](#how-authorization-works)
5. [Your Questions Answered](#your-questions-answered)
6. [Managing the System](#managing-the-system)
7. [Practical Examples](#practical-examples)
8. [Implementation Details](#implementation-details)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Introduction

Welcome to the WheelShiftPro RBAC (Role-Based Access Control) system! This guide will help you understand how security and permissions work in the application, even if you're completely new to these concepts.

### What You'll Learn

- ✅ How roles and permissions work
- ✅ What ACLs (Access Control Lists) are and when to use them
- ✅ How data scopes restrict what employees can see
- ✅ How to manage roles, permissions, and access
- ✅ Best practices for security

---

## 🤔 What is RBAC?

RBAC stands for **Role-Based Access Control**. Think of it like security at a building:

```
┌─────────────────────────────────────────────────────────────┐
│                         Building                             │
├─────────────────────────────────────────────────────────────┤
│  🚪 Main Entrance (Everyone can enter)                      │
│  🚪 Office Floor   (Only employees with badge)              │
│  🚪 Server Room    (Only IT staff)                          │
│  🚪 Executive Suite (Only executives)                       │
│  🚪 Vault          (Only authorized personnel)              │
└─────────────────────────────────────────────────────────────┘
```

In RBAC:
- **Employees** = People using the system
- **Roles** = Job titles (like "Sales Manager", "Inspector")
- **Permissions** = Specific things you can do (like "view cars", "edit prices")
- **Resources** = Things in the system (cars, clients, sales)

---

## 📚 Core Concepts Explained

### 1. 👥 Roles

**What is a Role?**  
A role is like a job title that comes with a set of permissions.

```
┌────────────────────────────────────────────────────────┐
│                     ROLE: SALES                        │
├────────────────────────────────────────────────────────┤
│  Permissions:                                          │
│  ✓ View inquiries                                      │
│  ✓ Create inquiries                                    │
│  ✓ Update inquiries                                    │
│  ✓ View reservations                                   │
│  ✓ Create reservations                                 │
│  ✓ View clients                                        │
│  ✓ View cars                                           │
│  ✗ Delete cars (NO ACCESS)                            │
│  ✗ Manage employees (NO ACCESS)                       │
└────────────────────────────────────────────────────────┘
```

**Built-in Roles:**

| Role | What They Do | Example User |
|------|-------------|--------------|
| **SUPER_ADMIN** | Everything - full system access | System Administrator |
| **ADMIN** | Manage employees, roles, settings | Office Manager |
| **SALES** | Handle inquiries, reservations, sales | Sales Team |
| **INSPECTOR** | Inspect vehicles, update status | Quality Assurance |
| **FINANCE** | Manage transactions, view reports | Accountant |
| **STORE_MANAGER** | Manage location inventory | Warehouse Manager |

**Visual Representation:**

```
         ┌──────────────┐
         │ SUPER_ADMIN  │  ← Can do EVERYTHING
         └──────┬───────┘
                │
         ┌──────▼───────┐
         │    ADMIN     │  ← Can manage users & system
         └──────┬───────┘
                │
    ┌───────────┼───────────┬────────────┐
    │           │           │            │
┌───▼───┐  ┌───▼────┐  ┌──▼─────┐  ┌──▼──────┐
│ SALES │  │INSPECTOR│  │FINANCE │  │STORE_   │
│       │  │        │  │        │  │MANAGER  │
└───────┘  └────────┘  └────────┘  └─────────┘
    ↑           ↑          ↑            ↑
  Regular employees with specific responsibilities
```

### 2. 🔑 Permissions

**What is a Permission?**  
A permission is a specific action you can perform on a resource.

**Format:** `resource:action`

**Examples:**
- `cars:read` = Can view cars
- `cars:write` = Can create/edit cars
- `cars:delete` = Can delete cars
- `cars:*` = Can do anything with cars
- `*:*` = Can do everything (SUPER_ADMIN only)

```
┌──────────────────────────────────────────────────┐
│           Permission Breakdown                   │
├──────────────────────────────────────────────────┤
│                                                  │
│   "cars:write"                                   │
│     │     │                                      │
│     │     └──> ACTION (what to do)              │
│     │          • read   = view                  │
│     │          • write  = create/update         │
│     │          • delete = remove                │
│     │          • *      = all actions           │
│     │                                            │
│     └──> RESOURCE (what to act on)              │
│          • cars                                  │
│          • clients                               │
│          • inquiries                             │
│          • reservations                          │
│          • etc.                                  │
│                                                  │
└──────────────────────────────────────────────────┘
```

**Available Resources:**
```
cars          car-models      clients        employees
inquiries     reservations    sales          transactions
inspections   locations       tasks          events
roles         permissions     acl            notifications
motorcycles   motorcycle-models
```

**Available Actions:**
```
read     - View/retrieve data
write    - Create and update data
delete   - Remove data
*        - All actions (wildcard)
```

### 3. 📍 Data Scopes

**What are Data Scopes?**  
Data scopes limit WHAT DATA an employee can see, even if they have the right permissions.

**Think of it like this:**
```
Employee A has SALES role → Can view all inquiries (permission)
    BUT
Employee A has LOCATION scope "LOC-001" → Can ONLY see inquiries in LOC-001
```

**Visual Example:**

```
┌────────────────────────────────────────────────────────────┐
│                    Without Data Scope                      │
├────────────────────────────────────────────────────────────┤
│  Employee: John (SALES role)                               │
│  Permission: inquiries:read ✓                              │
│                                                            │
│  Can See:                                                  │
│    📋 Inquiry #1 - Location LOC-001                        │
│    📋 Inquiry #2 - Location LOC-002                        │
│    📋 Inquiry #3 - Location LOC-003                        │
│    📋 Inquiry #4 - Location LOC-001                        │
│    📋 Inquiry #5 - Location LOC-002                        │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                    With Data Scope                         │
├────────────────────────────────────────────────────────────┤
│  Employee: John (SALES role)                               │
│  Permission: inquiries:read ✓                              │
│  Data Scope: LOCATION = "LOC-001" (INCLUDE)                │
│                                                            │
│  Can See:                                                  │
│    📋 Inquiry #1 - Location LOC-001                        │
│    📋 Inquiry #4 - Location LOC-001                        │
│                                                            │
│  Cannot See:                                               │
│    ⛔ Inquiry #2 - Location LOC-002                        │
│    ⛔ Inquiry #3 - Location LOC-003                        │
│    ⛔ Inquiry #5 - Location LOC-002                        │
└────────────────────────────────────────────────────────────┘
```

**Types of Data Scopes:**

1. **LOCATION Scope** - Limit by physical location
   ```
   Example: Store Manager only sees inventory at their store
   Scope Type: LOCATION
   Scope Value: "LOC-001"
   Effect: INCLUDE
   ```

2. **DEPARTMENT Scope** - Limit by department
   ```
   Example: Finance employee only sees finance transactions
   Scope Type: DEPARTMENT
   Scope Value: "FINANCE"
   Effect: INCLUDE
   ```

3. **ASSIGNMENT Scope** - Limit to assigned items
   ```
   Example: Sales rep only sees their own inquiries
   Scope Type: ASSIGNMENT
   Scope Value: "SELF"
   Effect: INCLUDE
   ```

**Scope Effects:**

```
┌─────────────────────────────────────────────────────┐
│  INCLUDE Effect (Whitelist)                         │
├─────────────────────────────────────────────────────┤
│  "Only show me LOC-001"                             │
│  ✓ LOC-001 items are visible                        │
│  ✗ Everything else is hidden                        │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  EXCLUDE Effect (Blacklist)                         │
├─────────────────────────────────────────────────────┤
│  "Don't show me LOC-003"                            │
│  ✓ LOC-001, LOC-002, LOC-004, etc. are visible      │
│  ✗ LOC-003 items are hidden                         │
└─────────────────────────────────────────────────────┘
```

### 4. 🔒 Access Control Lists (ACLs)

**What are ACLs?**  
ACLs give specific access to individual resources, overriding normal role permissions.

**Think of it like this:**
```
Normally: You need a "SALES" role to edit cars
With ACL: You can give John access to edit Car #123 specifically,
          even if he doesn't have SALES role
```

**Visual Example:**

```
┌────────────────────────────────────────────────────────────┐
│                     Resource ACL                           │
├────────────────────────────────────────────────────────────┤
│                                                            │
│   Resource: CAR #123 (2024 Toyota Camry)                  │
│                                                            │
│   Normal Access:                                           │
│   └─> SALES role → Can edit                               │
│   └─> INSPECTOR role → Can view only                      │
│   └─> FINANCE role → Can view only                        │
│                                                            │
│   Special ACL Grants:                                      │
│   ┌────────────────────────────────────┐                  │
│   │ Employee: Mike (ID: 25)            │                  │
│   │ Access Level: WRITE                │                  │
│   │ Reason: Temporary assignment       │                  │
│   └────────────────────────────────────┘                  │
│   → Mike can edit Car #123 even without SALES role        │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**Access Levels:**

```
READ    →  👀 Can view only
            └─> Can get details, view information
            └─> Cannot make any changes

WRITE   →  ✏️ Can view and modify
            └─> Can get details, view information
            └─> Can update, create related items
            └─> Cannot delete or manage ACL

ADMIN   →  👑 Full control
            └─> Can get details, view information
            └─> Can update, create, delete
            └─> Can manage ACL (grant/revoke access)
```

**Subject Types (Who gets access):**

1. **EMPLOYEE** - Specific person
   ```
   Grant access to Employee ID: 25
   ```

2. **ROLE** - Everyone with a role
   ```
   Grant access to all employees with INSPECTOR role
   ```

3. **DEPARTMENT** - Everyone in a department
   ```
   Grant access to all employees in FINANCE department
   ```

---

## ⚙️ How Authorization Works

When you try to do something in the system, here's how it checks if you're allowed:

```
┌──────────────────────────────────────────────────────────┐
│              Authorization Check Flow                     │
└──────────────────────────────────────────────────────────┘

   Employee tries to edit Car #123
              │
              ▼
   ┌─────────────────────────┐
   │ 1. Are you SUPER_ADMIN? │ ──YES──> ✅ ALLOWED
   └────────┬────────────────┘
            │ NO
            ▼
   ┌─────────────────────────────────────┐
   │ 2. Do you have ACL for this car?    │ ──YES──> ✅ ALLOWED
   │    (Special permission for Car #123)│
   └────────┬────────────────────────────┘
            │ NO
            ▼
   ┌─────────────────────────────────────┐
   │ 3. Do you have the permission?      │ ──NO──> ❌ DENIED
   │    (cars:write)                     │
   └────────┬────────────────────────────┘
            │ YES
            ▼
   ┌─────────────────────────────────────┐
   │ 4. Does car match your data scope?  │ ──NO──> ❌ DENIED
   │    (If car is in your location)     │
   └────────┬────────────────────────────┘
            │ YES
            ▼
          ✅ ALLOWED
```

**Priority Order (Highest to Lowest):**

1. **SUPER_ADMIN** - Always has access to everything
2. **Resource ACL** - Specific grants for individual items
3. **Data Scope** - Location/department/assignment filters
4. **Role Permission** - General permissions from your role
5. **DENY** - Default if nothing matches

---

## ❓ Your Questions Answered

### Q1: Can I set roles and assign permissions to them?

**Answer: YES! ✅**

```
┌──────────────────────────────────────────────────────┐
│  Step 1: Create or use existing role                 │
│  ┌──────────────┐                                    │
│  │ SALES        │  (built-in role)                   │
│  └──────────────┘                                    │
│         │                                             │
│  Step 2: Assign permissions to role                  │
│         │                                             │
│         ├──> inquiries:read                          │
│         ├──> inquiries:write                         │
│         ├──> reservations:*                          │
│         ├──> sales:*                                 │
│         ├──> clients:read                            │
│         └──> cars:read                               │
│                                                      │
│  Step 3: Assign role to employee                     │
│  ┌──────────────┐                                    │
│  │ John Smith   │ ──assigned──> SALES role           │
│  └──────────────┘                                    │
│         │                                             │
│  Result: John has all SALES permissions              │
└──────────────────────────────────────────────────────┘
```

**How to do it:**
```bash
# Create a custom role
POST /api/v1/rbac/roles
{
  "name": "CUSTOM_SALES_LEAD",
  "description": "Sales team lead with extra permissions"
}

# Add permissions to the role
POST /api/v1/rbac/roles/{roleId}/permissions/{permissionId}

# Assign role to employee
POST /api/v1/rbac/employees/{employeeId}/roles/{roleId}
```

### Q2: There are resources and actions - how do they work together?

**Answer: They form permissions in `resource:action` format**

```
┌─────────────────────────────────────────────────────────┐
│               Resources + Actions                       │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Resources (WHAT):           Actions (HOW):            │
│  • cars                       • read                    │
│  • clients                    • write                   │
│  • inquiries                  • delete                  │
│  • sales                      • * (all)                 │
│  • employees                                            │
│                                                         │
│  Combined into Permissions:                             │
│  ┌──────────┐     ┌─────────┐                         │
│  │   cars   │  +  │  write  │  =  cars:write          │
│  └──────────┘     └─────────┘                         │
│                                                         │
│  ┌──────────┐     ┌─────────┐                         │
│  │ clients  │  +  │  read   │  =  clients:read        │
│  └──────────┘     └─────────┘                         │
│                                                         │
│  ┌──────────┐     ┌─────────┐                         │
│  │  sales   │  +  │    *    │  =  sales:*             │
│  └──────────┘     └─────────┘     (all actions)       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Q3: As admin, can I add or update roles?

**Answer: YES! ✅**

```
┌──────────────────────────────────────────────────────┐
│            Who Can Manage Roles                      │
├──────────────────────────────────────────────────────┤
│                                                      │
│  SUPER_ADMIN:                                        │
│    ✓ Create custom roles                            │
│    ✓ Update any role (including system roles)       │
│    ✓ Delete custom roles                            │
│    ✓ Add/remove permissions to/from roles           │
│    ✓ Assign/remove roles to/from employees          │
│                                                      │
│  ADMIN:                                              │
│    ✗ Create custom roles (SUPER_ADMIN only)         │
│    ✗ Update roles (SUPER_ADMIN only)                │
│    ✗ Delete roles (SUPER_ADMIN only)                │
│    ✗ Manage permissions (SUPER_ADMIN only)          │
│    ✓ Assign/remove roles to/from employees          │
│                                                      │
│  Protection:                                         │
│    ⚠️ System roles (isSystem=true) cannot be deleted │
│    ⚠️ SUPER_ADMIN role cannot be modified            │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Q4: Can an employee have more than one role?

**Answer: YES, but use carefully! ⚠️**

```
┌──────────────────────────────────────────────────────┐
│         Multiple Roles for One Employee              │
├──────────────────────────────────────────────────────┤
│                                                      │
│  Employee: Sarah                                     │
│  ┌────────────┐                                      │
│  │   SALES    │ → inquiries:*, reservations:*       │
│  └────────────┘                                      │
│  ┌────────────┐                                      │
│  │  INSPECTOR │ → inspections:*, cars:read          │
│  └────────────┘                                      │
│                                                      │
│  Combined Permissions:                               │
│    • inquiries:*                                     │
│    • reservations:*                                  │
│    • inspections:*                                   │
│    • cars:read                                       │
│                                                      │
└──────────────────────────────────────────────────────┘

⚠️ BEST PRACTICE:
   • Ideally, one employee = one role
   • Multiple roles can cause confusion
   • Only use when truly necessary
   • Example: Small business where person wears many hats
```

**When it makes sense:**
- Small team where one person does multiple jobs
- Temporary coverage for another employee
- Transitioning between roles

**When to avoid:**
- Normal operations (create a new role instead)
- Long-term assignments
- When permissions conflict

### Q5: Can I give extra permissions to a specific employee?

**Answer: YES - Use ACLs! ✅**

```
┌──────────────────────────────────────────────────────┐
│      Giving Extra Permissions to One Employee       │
├──────────────────────────────────────────────────────┤
│                                                      │
│  Scenario:                                           │
│    Mike (INSPECTOR role) needs to edit Car #123     │
│    but INSPECTOR role only has cars:read            │
│                                                      │
│  Solution: Create Resource ACL                       │
│  ┌────────────────────────────────────┐            │
│  │ Resource: CAR #123                 │            │
│  │ Subject: EMPLOYEE (Mike, ID: 25)   │            │
│  │ Access Level: WRITE                │            │
│  │ Reason: "Special project car"      │            │
│  └────────────────────────────────────┘            │
│                                                      │
│  Result:                                             │
│    ✓ Mike can edit Car #123                         │
│    ✗ Mike cannot edit other cars                    │
│    ✓ Mike keeps normal INSPECTOR permissions        │
│                                                      │
└──────────────────────────────────────────────────────┘
```

**Alternative: Add permission to role or create data scope**

```
Option 1: Use ACL (Recommended for specific resources)
   └─> Best for: One specific car, client, or inquiry

Option 2: Assign additional role
   └─> Best for: Broader access needs

Option 3: Create data scope
   └─> Best for: Location or department-based access
```

### Q6: What are ACLs and when should I use them?

**Answer: ACLs are special permissions for specific items**

```
┌──────────────────────────────────────────────────────┐
│               When to Use ACLs                       │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ✅ USE ACLs for:                                    │
│    • Temporary access to specific item              │
│    • Special project or assignment                   │
│    • Exception to normal role permissions           │
│    • Delegation to non-standard user                 │
│                                                      │
│  Example Use Cases:                                  │
│    1. "Give John access to this one VIP client"     │
│    2. "Let Sarah manage this special car sale"      │
│    3. "Allow Mike to view this inquiry"             │
│                                                      │
│  ❌ DON'T USE ACLs for:                              │
│    • Regular, ongoing access (use roles)            │
│    • Large groups of items (use data scopes)        │
│    • Standard permissions (add to role)             │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Q7: What are data scopes and when should I use them?

**Answer: Data scopes limit WHAT you can see based on location/department**

```
┌──────────────────────────────────────────────────────┐
│            When to Use Data Scopes                   │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ✅ USE DATA SCOPES for:                             │
│    • Location-based access                           │
│    • Department-based access                         │
│    • "Only my assigned items"                        │
│                                                      │
│  Example Use Cases:                                  │
│                                                      │
│  1. LOCATION Scope:                                  │
│     Store Manager at LOC-001                         │
│     └─> Only sees inventory at their location       │
│                                                      │
│  2. DEPARTMENT Scope:                                │
│     Finance Employee                                 │
│     └─> Only sees finance transactions              │
│                                                      │
│  3. ASSIGNMENT Scope:                                │
│     Sales Rep                                        │
│     └─> Only sees inquiries assigned to them        │
│                                                      │
│  ❌ DON'T USE DATA SCOPES for:                       │
│    • Specific single items (use ACL instead)        │
│    • Permission restrictions (use roles)            │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Q8: How do I manage ACLs and data scopes?

**Answer: Through REST API endpoints**

```
┌──────────────────────────────────────────────────────┐
│            Managing ACLs                             │
├──────────────────────────────────────────────────────┤
│                                                      │
│  View ACLs for a resource:                           │
│  GET /api/v1/rbac/acl/{resourceType}/{resourceId}   │
│                                                      │
│  Grant access:                                       │
│  POST /api/v1/rbac/acl/{resourceType}/{resourceId}  │
│  {                                                   │
│    "subjectType": "EMPLOYEE",                        │
│    "subjectId": 25,                                  │
│    "accessLevel": "WRITE",                           │
│    "reason": "Special assignment"                    │
│  }                                                   │
│                                                      │
│  Remove access:                                      │
│  DELETE /api/v1/rbac/acl/{aclId}                     │
│                                                      │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│            Managing Data Scopes                      │
├──────────────────────────────────────────────────────┤
│                                                      │
│  View employee's scopes:                             │
│  GET /api/v1/rbac/employees/{employeeId}/scopes      │
│                                                      │
│  Add scope:                                          │
│  POST /api/v1/rbac/employees/{employeeId}/scopes     │
│  {                                                   │
│    "scopeType": "LOCATION",                          │
│    "scopeValue": "LOC-001",                          │
│    "effect": "INCLUDE",                              │
│    "description": "Main warehouse only"              │
│  }                                                   │
│                                                      │
│  Update scope:                                       │
│  PUT /api/v1/rbac/employees/scopes/{scopeId}         │
│                                                      │
│  Remove scope:                                       │
│  DELETE /api/v1/rbac/employees/scopes/{scopeId}      │
│                                                      │
└──────────────────────────────────────────────────────┘
```

---

## 🛠️ Managing the System

### Creating Roles

```bash
# Create a custom role
POST /api/v1/rbac/roles
Authorization: Bearer <super-admin-token>
Content-Type: application/json

{
  "name": "INVENTORY_MANAGER",
  "description": "Manages inventory and stock levels",
  "isSystem": false
}
```

### Adding Permissions to Roles

```bash
# First, find the permission ID you want to add
GET /api/v1/rbac/permissions

# Then add it to the role
POST /api/v1/rbac/roles/{roleId}/permissions/{permissionId}
Authorization: Bearer <super-admin-token>
```

### Assigning Roles to Employees

```bash
# Assign a role to an employee
POST /api/v1/rbac/employees/{employeeId}/roles/{roleId}
Authorization: Bearer <admin-token>
```

### Adding Data Scopes

```bash
# Add location scope to employee
POST /api/v1/rbac/employees/{employeeId}/scopes
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "scopeType": "LOCATION",
  "scopeValue": "LOC-001",
  "effect": "INCLUDE",
  "description": "Main warehouse access only"
}
```

### Granting Resource Access (ACL)

```bash
# Grant employee access to specific car
POST /api/v1/rbac/acl/CAR/{carId}
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "subjectType": "EMPLOYEE",
  "subjectId": 25,
  "accessLevel": "WRITE",
  "reason": "Special project assignment"
}
```

---

## 💡 Practical Examples

### Example 1: New Store Manager

**Scenario:** You hire a new store manager for location LOC-002

```
Step 1: Create employee account
Step 2: Assign STORE_MANAGER role
   POST /api/v1/rbac/employees/{employeeId}/roles/6

Step 3: Add location scope
   POST /api/v1/rbac/employees/{employeeId}/scopes
   {
     "scopeType": "LOCATION",
     "scopeValue": "LOC-002",
     "effect": "INCLUDE"
   }

Result:
✓ Manager has all STORE_MANAGER permissions
✓ Manager only sees LOC-002 inventory
✓ Manager cannot access other locations
```

### Example 2: Sales Rep with Assigned Leads

**Scenario:** Sales rep should only see their assigned inquiries

```
Step 1: Assign SALES role
   POST /api/v1/rbac/employees/{employeeId}/roles/3

Step 2: Add assignment scope
   POST /api/v1/rbac/employees/{employeeId}/scopes
   {
     "scopeType": "ASSIGNMENT",
     "scopeValue": "SELF",
     "effect": "INCLUDE"
   }

Result:
✓ Has all SALES permissions
✓ Only sees inquiries assigned to them
✓ Cannot see other sales reps' inquiries
```

### Example 3: Temporary Access for Special Project

**Scenario:** Inspector needs to manage one specific car for a special project

```
Current State:
  Employee: Mike (INSPECTOR role)
  Permissions: inspections:*, cars:read
  Problem: Cannot edit car details

Solution: Add ACL
   POST /api/v1/rbac/acl/CAR/123
   {
     "subjectType": "EMPLOYEE",
     "subjectId": 25,
     "accessLevel": "WRITE",
     "reason": "Special restoration project"
   }

Result:
✓ Mike can edit Car #123
✓ Mike keeps normal INSPECTOR permissions
✓ Mike cannot edit other cars
✓ Access can be revoked when project completes
```

### Example 4: Finance Department Access

**Scenario:** Finance team should only see financial transactions

```
Step 1: Assign FINANCE role
   POST /api/v1/rbac/employees/{employeeId}/roles/5

Step 2: Add department scope
   POST /api/v1/rbac/employees/{employeeId}/scopes
   {
     "scopeType": "DEPARTMENT",
     "scopeValue": "FINANCE",
     "effect": "INCLUDE"
   }

Result:
✓ Has all FINANCE permissions (transactions, reports)
✓ Only sees finance department transactions
✓ Cannot see HR or other department data
```

---

## 🔧 Implementation Details

### Database Tables

```
roles
├── id
├── name (RoleType enum)
├── description
├── is_system
└── timestamps

permissions
├── id
├── resource
├── action
├── name (auto-generated: resource:action)
├── description
└── timestamps

role_permissions (join table)
├── role_id
└── permission_id

employee_roles (join table)
├── employee_id
└── role_id

employee_data_scopes
├── id
├── employee_id
├── scope_type (LOCATION, DEPARTMENT, ASSIGNMENT)
├── scope_value
├── effect (INCLUDE, EXCLUDE)
├── description
└── timestamps

resource_acl
├── id
├── resource_type (enum)
├── resource_id
├── subject_type (EMPLOYEE, ROLE, DEPARTMENT)
├── subject_id
├── access_level (READ, WRITE, ADMIN)
├── reason
├── granted_by
└── timestamps
```

### Key Services

1. **AuthorizationService** - Main authorization logic
   - `hasPermission()` - Check if employee has a permission
   - `hasRole()` - Check if employee has a role
   - `canAccessCar()` - Domain-specific authorization
   - `hasLocationAccess()` - Data scope checking

2. **RoleService** - Role management
   - Create, update, delete roles
   - Assign/remove roles to/from employees
   - Manage role-permission mappings

3. **PermissionService** - Permission management
   - Create, update, delete permissions
   - Get permissions for employee or role

4. **DataScopeService** - Data scope management
   - Add, update, remove scopes
   - Get scopes for employee
   - Check scope access

5. **ResourceACLService** - ACL management
   - Grant/revoke resource access
   - Check ACL access
   - Get ACLs for resource

### Security Annotations

Controllers use `@PreAuthorize` for method-level security:

```java
@GetMapping("/{id}")
@PreAuthorize("@authService.hasPermission(authentication.name, 'cars:read')")
public ResponseEntity<Car> getCar(@PathVariable Long id) {
    // Implementation
}

@PostMapping
@PreAuthorize("@authService.hasPermission(authentication.name, 'cars:write')")
public ResponseEntity<Car> createCar(@RequestBody CarRequest request) {
    // Implementation
}
```

---

## 🐛 Troubleshooting

### Problem: Employee can't access a resource

**Checklist:**

1. ✅ Does employee have a role assigned?
   ```
   GET /api/v1/rbac/employees/{employeeId}/roles
   ```

2. ✅ Does the role have the required permission?
   ```
   GET /api/v1/rbac/roles/{roleId}
   → Check permissions list
   ```

3. ✅ Are there any data scopes blocking access?
   ```
   GET /api/v1/rbac/employees/{employeeId}/scopes
   → Check if EXCLUDE scopes exist
   ```

4. ✅ Is the JWT token valid?
   ```
   → Check token expiration
   → Re-login if needed
   ```

### Problem: Data scope not working

**Checklist:**

1. ✅ Is the scope type correct?
   - LOCATION for storage locations
   - DEPARTMENT for department-based access
   - ASSIGNMENT for assigned items

2. ✅ Is the scope value matching the data?
   - Check spelling/case sensitivity
   - Verify the value exists in the database

3. ✅ Is the effect correct?
   - INCLUDE = whitelist (only show these)
   - EXCLUDE = blacklist (hide these)

4. ✅ Are there conflicting scopes?
   - Multiple INCLUDE scopes = show items matching ANY
   - EXCLUDE takes precedence over INCLUDE

### Problem: ACL not granting access

**Checklist:**

1. ✅ Is the resource type correct?
   - CAR, CLIENT, INQUIRY, etc.
   - Must match exactly

2. ✅ Is the access level sufficient?
   - READ < WRITE < ADMIN
   - Need WRITE to modify, ADMIN for full control

3. ✅ Is the subject correct?
   - EMPLOYEE + employeeId
   - ROLE + roleId
   - DEPARTMENT + departmentId

---

## 📖 Additional Resources

- [Complete Implementation Summary](../RBAC_IMPLEMENTATION_SUMMARY.md)
- [API Usage Guide](../RBAC_USAGE_GUIDE.md)
- [Endpoint Reference](./RBAC_ENDPOINTS.md)
- [Helper Methods Guide](./RBAC_HELPER_METHODS.md)

---

## 🎓 Quick Reference Card

```
┌────────────────────────────────────────────────────────┐
│             RBAC Quick Reference                       │
├────────────────────────────────────────────────────────┤
│                                                        │
│  ROLES → Job titles with permissions                   │
│  Example: SALES, INSPECTOR, ADMIN                      │
│                                                        │
│  PERMISSIONS → Specific actions                        │
│  Format: resource:action                               │
│  Example: cars:write, clients:read                     │
│                                                        │
│  DATA SCOPES → Limit what you see                      │
│  Types: LOCATION, DEPARTMENT, ASSIGNMENT               │
│  Example: Only see LOC-001 cars                        │
│                                                        │
│  ACLs → Special access to specific items               │
│  Levels: READ, WRITE, ADMIN                            │
│  Example: Let John edit Car #123                       │
│                                                        │
│  AUTHORIZATION ORDER:                                  │
│  1. SUPER_ADMIN (always yes)                           │
│  2. Resource ACL                                       │
│  3. Data Scope                                         │
│  4. Role Permission                                    │
│  5. Deny (default)                                     │
│                                                        │
└────────────────────────────────────────────────────────┘
```

---

**Need Help?** Check the troubleshooting section or contact your system administrator.

**Remember:** 
- One role per employee is best practice
- Use ACLs sparingly for special cases
- Data scopes for location/department restrictions
- Always test permissions before going live

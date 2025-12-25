# Employee API Reference for Task Management

Quick reference guide for employee-related API endpoints needed when working with tasks.

## Overview

When managing tasks, you'll often need to:
- Get a list of employees to assign tasks to
- Search for specific employees
- Get employee details to display assignee information

This document covers the essential employee endpoints for task management integration.

---

## Get All Employees

**Endpoint:** `GET /api/v1/employees`  
**Authorization:** Required

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| search | String | No | - | Search pattern to filter by name, email, position, or department |
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Requests

```bash
# Get all employees (first page)
GET /api/v1/employees?page=0&size=20

# Search employees by name
GET /api/v1/employees?search=john&page=0&size=20

# Search employees by department
GET /api/v1/employees?search=sales&page=0&size=20

# Search employees by email domain
GET /api/v1/employees?search=@company.com&page=0&size=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 15,
        "name": "John Smith",
        "email": "john.smith@wheelshiftpro.com",
        "phone": "+1-555-0123",
        "position": "Senior Inspector",
        "department": "Quality Assurance",
        "joinDate": "2023-01-15",
        "status": "ACTIVE",
        "lastLogin": "2025-12-25T09:30:00",
        "createdAt": "2023-01-15T10:00:00",
        "updatedAt": "2025-12-25T09:30:00"
      },
      {
        "id": 22,
        "name": "Sarah Johnson",
        "email": "sarah.johnson@wheelshiftpro.com",
        "phone": "+1-555-0124",
        "position": "Finance Manager",
        "department": "Finance",
        "joinDate": "2022-06-10",
        "status": "ACTIVE",
        "lastLogin": "2025-12-25T08:15:00",
        "createdAt": "2022-06-10T09:00:00",
        "updatedAt": "2025-12-25T08:15:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 42,
    "totalPages": 3,
    "last": false
  },
  "timestamp": "2025-12-25T10:00:00"
}
```

---

## Search Employees

**Endpoint:** `GET /api/v1/employees/search`  
**Authorization:** Required

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| name | String | No | - | Filter by employee name (partial match) |
| role | String | No | - | Filter by role |
| status | String | No | - | Filter by status (ACTIVE, INACTIVE, ON_LEAVE, TERMINATED) |
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Requests

```bash
# Search active employees
GET /api/v1/employees/search?status=ACTIVE&page=0&size=20

# Search by name and status
GET /api/v1/employees/search?name=smith&status=ACTIVE&page=0&size=20

# Search by role
GET /api/v1/employees/search?role=INSPECTOR&page=0&size=20
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 15,
        "name": "John Smith",
        "email": "john.smith@wheelshiftpro.com",
        "phone": "+1-555-0123",
        "position": "Senior Inspector",
        "department": "Quality Assurance",
        "joinDate": "2023-01-15",
        "status": "ACTIVE",
        "lastLogin": "2025-12-25T09:30:00",
        "createdAt": "2023-01-15T10:00:00",
        "updatedAt": "2025-12-25T09:30:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-12-25T10:00:00"
}
```

---

## Get Employee by ID

**Endpoint:** `GET /api/v1/employees/{id}`  
**Authorization:** Required

### Path Parameters

- `id` (Long) - Employee ID

### Example Request

```bash
GET /api/v1/employees/15
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 15,
    "name": "John Smith",
    "email": "john.smith@wheelshiftpro.com",
    "phone": "+1-555-0123",
    "position": "Senior Inspector",
    "department": "Quality Assurance",
    "joinDate": "2023-01-15",
    "status": "ACTIVE",
    "lastLogin": "2025-12-25T09:30:00",
    "createdAt": "2023-01-15T10:00:00",
    "updatedAt": "2025-12-25T09:30:00"
  },
  "timestamp": "2025-12-25T10:00:00"
}
```

---

## Get Active Employees

**Endpoint:** `GET /api/v1/employees/active`  
**Authorization:** Required

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Request

```bash
GET /api/v1/employees/active?page=0&size=20
```

### Success Response (200 OK)

Returns paginated list of only active employees (same response structure as "Get All Employees").

---

## Get Employees by Role

**Endpoint:** `GET /api/v1/employees/role/{role}`  
**Authorization:** Required

### Path Parameters

- `role` (String) - Employee role

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 20 | Page size |

### Example Request

```bash
GET /api/v1/employees/role/INSPECTOR?page=0&size=20
```

### Success Response (200 OK)

Returns paginated list of employees with the specified role (same response structure as "Get All Employees").

---

## Employee Status Enum Values

- `ACTIVE` - Employee is currently active
- `INACTIVE` - Employee is inactive
- `ON_LEAVE` - Employee is on leave
- `TERMINATED` - Employee has been terminated

---

## Frontend Integration Examples

### JavaScript/TypeScript

```typescript
// Fetch all employees for dropdown
async function fetchEmployeesForDropdown(): Promise<EmployeeResponse[]> {
  const response = await fetch('/api/v1/employees/active?page=0&size=100', {
    headers: { 'Authorization': `Bearer ${getToken()}` }
  });
  const data = await response.json();
  return data.data.content;
}

// Search employees as user types
async function searchEmployees(searchTerm: string): Promise<EmployeeResponse[]> {
  const response = await fetch(
    `/api/v1/employees?search=${encodeURIComponent(searchTerm)}&page=0&size=20`,
    {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    }
  );
  const data = await response.json();
  return data.data.content;
}

// Get employee details
async function getEmployee(id: number): Promise<EmployeeResponse> {
  const response = await fetch(`/api/v1/employees/${id}`, {
    headers: { 'Authorization': `Bearer ${getToken()}` }
  });
  const data = await response.json();
  return data.data;
}
```

### React Component - Employee Selector

```typescript
import React, { useState, useEffect } from 'react';

interface EmployeeSelectProps {
  value?: number;
  onChange: (employeeId: number) => void;
}

const EmployeeSelect: React.FC<EmployeeSelectProps> = ({ value, onChange }) => {
  const [employees, setEmployees] = useState<EmployeeResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadEmployees();
  }, [searchTerm]);

  const loadEmployees = async () => {
    setLoading(true);
    try {
      const url = searchTerm 
        ? `/api/v1/employees?search=${encodeURIComponent(searchTerm)}&page=0&size=20`
        : '/api/v1/employees/active?page=0&size=100';
      
      const response = await fetch(url, {
        headers: { 'Authorization': `Bearer ${getToken()}` }
      });
      const data = await response.json();
      setEmployees(data.data.content);
    } catch (error) {
      console.error('Failed to load employees:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="employee-select">
      <input
        type="text"
        placeholder="Search employees..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="search-input"
      />
      {loading ? (
        <div>Loading...</div>
      ) : (
        <select 
          value={value || ''} 
          onChange={(e) => onChange(Number(e.target.value))}
          className="employee-dropdown"
        >
          <option value="">Select an employee</option>
          {employees.map(emp => (
            <option key={emp.id} value={emp.id}>
              {emp.name} - {emp.position}
            </option>
          ))}
        </select>
      )}
    </div>
  );
};
```

### React Component - Employee Autocomplete

```typescript
import React, { useState, useEffect } from 'react';

const EmployeeAutocomplete: React.FC = () => {
  const [query, setQuery] = useState('');
  const [employees, setEmployees] = useState<EmployeeResponse[]>([]);
  const [showDropdown, setShowDropdown] = useState(false);

  useEffect(() => {
    if (query.length > 2) {
      searchEmployees();
    } else {
      setEmployees([]);
      setShowDropdown(false);
    }
  }, [query]);

  const searchEmployees = async () => {
    try {
      const response = await fetch(
        `/api/v1/employees?search=${encodeURIComponent(query)}&page=0&size=10`,
        {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        }
      );
      const data = await response.json();
      setEmployees(data.data.content);
      setShowDropdown(true);
    } catch (error) {
      console.error('Search failed:', error);
    }
  };

  const selectEmployee = (employee: EmployeeResponse) => {
    console.log('Selected:', employee);
    setQuery(employee.name);
    setShowDropdown(false);
  };

  return (
    <div className="autocomplete">
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Type to search employees..."
        className="autocomplete-input"
      />
      {showDropdown && employees.length > 0 && (
        <ul className="autocomplete-dropdown">
          {employees.map(emp => (
            <li 
              key={emp.id}
              onClick={() => selectEmployee(emp)}
              className="autocomplete-item"
            >
              <div className="employee-info">
                <strong>{emp.name}</strong>
                <span>{emp.position} - {emp.department}</span>
                <span className="email">{emp.email}</span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};
```

---

## Common Use Cases

### 1. Task Assignment Dropdown

Load active employees for a task assignment dropdown:

```typescript
const loadActiveEmployees = async () => {
  const response = await fetch('/api/v1/employees/active?page=0&size=100', {
    headers: { 'Authorization': `Bearer ${getToken()}` }
  });
  const data = await response.json();
  return data.data.content;
};
```

### 2. Filter Employees by Department

Get employees from a specific department:

```typescript
const loadDepartmentEmployees = async (department: string) => {
  const response = await fetch(
    `/api/v1/employees?search=${encodeURIComponent(department)}&page=0&size=50`,
    {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    }
  );
  const data = await response.json();
  return data.data.content;
};
```

### 3. Validate Employee Before Assignment

Check if an employee exists and is active:

```typescript
const validateEmployee = async (employeeId: number): Promise<boolean> => {
  try {
    const response = await fetch(`/api/v1/employees/${employeeId}`, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    });
    const data = await response.json();
    return data.success && data.data.status === 'ACTIVE';
  } catch (error) {
    return false;
  }
};
```

---

## Error Responses

### 404 Not Found - Employee Not Found

```json
{
  "success": false,
  "message": "Employee not found with id: 999",
  "timestamp": "2025-12-25T10:00:00"
}
```

### 400 Bad Request - Invalid Parameters

```json
{
  "success": false,
  "message": "Invalid page or size parameter",
  "timestamp": "2025-12-25T10:00:00"
}
```

---

## Best Practices

1. **Use Active Employees Filter** - When showing assignment dropdowns, filter to only active employees
2. **Implement Search** - For large organizations, implement search/autocomplete rather than loading all employees
3. **Cache Results** - Cache employee lists for short periods to reduce API calls
4. **Pagination** - Always use pagination for employee lists
5. **Display Full Context** - Show employee position and department along with name for clarity
6. **Validate Before Assignment** - Verify employee exists and is active before assigning tasks
7. **Handle Null Assignees** - Tasks can have null assigneeId (unassigned), handle this in UI

---

## Related Documentation

- [Task Management API](./API_RESPONSES.md) - Complete task API reference
- [Task Management Implementation](./IMPLEMENTATION.md) - Technical implementation guide
- [Task Management Quick Start](./QUICK_START.md) - Getting started guide

---

## Quick Reference Table

| Endpoint | Method | Purpose | Use Case |
|----------|--------|---------|----------|
| `/api/v1/employees` | GET | Get all employees with search | Load employee list, search functionality |
| `/api/v1/employees/search` | GET | Advanced search | Filter by multiple criteria |
| `/api/v1/employees/{id}` | GET | Get single employee | Display assignee details |
| `/api/v1/employees/active` | GET | Get active employees only | Task assignment dropdowns |
| `/api/v1/employees/role/{role}` | GET | Get by role | Role-based filtering |

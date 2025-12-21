# API Integration Guide - Phased Approach

## Overview
This guide provides a systematic approach to integrating the WheelShift APIs into your frontend application. Follow these phases in order for a smooth implementation.

---

## 📋 Integration Phases

### **Phase 1: Foundation & Authentication** (Week 1)
**Goal:** Set up API client and implement complete authentication flow

1. Set up API client with Axios
2. Configure environment variables
3. Implement login endpoint
4. Implement logout endpoint
5. Implement "get current user" endpoint
6. Set up authentication state management
7. Create protected route wrapper
8. Handle token storage and refresh

**Dependencies:** None  
**Estimated Time:** 3-5 days

---

### **Phase 2: User Profile & Session Management** (Week 1)
**Goal:** Display user information and manage session

1. Create user profile display component
2. Show user roles and permissions
3. Implement session timeout handling
4. Add "Remember Me" functionality
5. Create user menu dropdown

**Dependencies:** Phase 1  
**Estimated Time:** 2-3 days

---

### **Phase 3: RBAC & Permissions** (Week 2)
**Goal:** Implement role-based access control in frontend

1. Fetch user roles from backend
2. Fetch user permissions from backend
3. Create permission checking hooks
4. Implement role-based route protection
5. Show/hide UI elements based on permissions
6. Implement data scope filtering

**Dependencies:** Phase 1, 2  
**Estimated Time:** 3-4 days

---

### **Phase 4: Car Models Management** (Week 2)
**Goal:** Manage car makes and models

1. Fetch car models list
2. Create car model
3. Update car model
4. Delete car model
5. Search/filter car models

**Dependencies:** Phase 1, 3  
**Estimated Time:** 2-3 days

---

### **Phase 5: Storage Locations** (Week 3)
**Goal:** Manage warehouse/storage locations

1. Fetch storage locations
2. Create storage location
3. Update storage location
4. Delete storage location

**Dependencies:** Phase 1, 3  
**Estimated Time:** 2 days

---

### **Phase 6: Inventory Management (Cars)** (Week 3-4)
**Goal:** Complete car inventory CRUD operations

1. Fetch cars list with pagination
2. Search and filter cars
3. Create new car entry
4. Update car details
5. Delete car
6. View car details
7. Upload car images
8. Car inspection records

**Dependencies:** Phase 4, 5  
**Estimated Time:** 5-6 days

---

### **Phase 7: Client Management** (Week 4)
**Goal:** Manage customer database

1. Fetch clients list
2. Create new client
3. Update client details
4. Search clients
5. View client history
6. Client status management

**Dependencies:** Phase 1, 3  
**Estimated Time:** 3-4 days

---

### **Phase 8: Inquiry Management** (Week 5)
**Goal:** Handle customer inquiries

1. Fetch inquiries list
2. Create new inquiry
3. Assign inquiry to employee
4. Update inquiry status
5. Add notes to inquiry
6. Filter by status/priority
7. Link inquiry to car

**Dependencies:** Phase 6, 7  
**Estimated Time:** 4-5 days

---

### **Phase 9: Reservation Management** (Week 5-6)
**Goal:** Manage car reservations

1. Fetch reservations list
2. Create reservation
3. Update reservation
4. Cancel reservation
5. Convert reservation to sale
6. Reservation calendar view

**Dependencies:** Phase 6, 7, 8  
**Estimated Time:** 4-5 days

---

### **Phase 10: Sales & Financial Transactions** (Week 6-7)
**Goal:** Complete sales process and financial tracking

1. Create sale from reservation
2. Record financial transactions
3. Commission calculations
4. Payment tracking
5. Generate invoices
6. Sales reports

**Dependencies:** Phase 9  
**Estimated Time:** 5-6 days

---

### **Phase 11: Employee Management** (Week 7)
**Goal:** Manage employees and assignments

1. Fetch employees list
2. Create employee
3. Update employee
4. Assign roles to employee
5. Set data scopes
6. View employee activity

**Dependencies:** Phase 3  
**Estimated Time:** 3-4 days

---

### **Phase 12: Tasks & Events** (Week 8)
**Goal:** Task management and calendar

1. Fetch tasks list
2. Create task
3. Assign tasks
4. Update task status
5. Calendar/event management
6. Task notifications

**Dependencies:** Phase 11  
**Estimated Time:** 4-5 days

---

### **Phase 13: Reports & Analytics** (Week 8-9)
**Goal:** Business intelligence and reporting

1. Sales reports
2. Inventory reports
3. Financial reports
4. Performance analytics
5. Export functionality

**Dependencies:** All previous phases  
**Estimated Time:** 5-6 days

---

### **Phase 14: Polish & Optimization** (Week 9-10)
**Goal:** Finalize and optimize

1. Error handling improvements
2. Loading states optimization
3. Performance tuning
4. Responsive design fixes
5. User experience enhancements
6. Documentation

**Dependencies:** All previous phases  
**Estimated Time:** 7-10 days

---

## 🎯 Current Phase: Phase 1 - Authentication

---

# Phase 1: Authentication API Documentation

## Base Configuration

```javascript
// .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
```

---

## 1. Login Endpoint

### **POST** `/auth/login`

**Description:** Authenticate user with email and password

**Authentication Required:** No

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "admin@wheelshift.com",
  "password": "admin123"
}
```

**Request Body Schema:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | User's email address (valid email format) |
| password | string | Yes | User's password (min 6 characters) |

**Success Response (200 OK):**
```json
{
  "employeeId": 1,
  "email": "admin@wheelshift.com",
  "name": "Super Admin",
  "roles": [
    "SUPER_ADMIN"
  ],
  "permissions": [
    "cars:read",
    "cars:write",
    "cars:delete",
    "inquiries:read",
    "inquiries:write",
    "inquiries:assign",
    "reservations:read",
    "reservations:write",
    "sales:read",
    "sales:write",
    "transactions:read",
    "transactions:write",
    "employees:read",
    "employees:write",
    "settings:manage",
    "acl:manage",
    "audit:read"
  ],
  "message": "Login successful"
}
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| employeeId | number | Unique employee identifier |
| email | string | User's email address |
| name | string | User's full name |
| roles | string[] | Array of assigned role names |
| permissions | string[] | Array of permission strings (resource:action format) |
| message | string | Success message |

**Error Responses:**

**401 Unauthorized** - Invalid credentials
```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Invalid email or password",
  "instance": "/api/v1/auth/login",
  "timestamp": "2025-12-21T10:00:00"
}
```

**400 Bad Request** - Validation error
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/v1/auth/login",
  "timestamp": "2025-12-21T10:00:00",
  "errors": [
    {
      "field": "email",
      "message": "Email is required",
      "rejectedValue": null
    }
  ]
}
```

**Frontend Implementation Example:**
```typescript
// lib/api/auth.ts
import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  employeeId: number;
  email: string;
  name: string;
  roles: string[];
  permissions: string[];
  message: string;
}

export const login = async (data: LoginRequest): Promise<LoginResponse> => {
  const response = await axios.post(`${API_BASE_URL}/auth/login`, data);
  return response.data;
};
```

**Usage:**
```typescript
try {
  const response = await login({
    email: 'admin@wheelshift.com',
    password: 'admin123'
  });
  
  // Store user data
  localStorage.setItem('user', JSON.stringify(response));
  
  // Redirect to dashboard
  router.push('/dashboard');
} catch (error) {
  console.error('Login failed:', error);
}
```

---

## 2. Get Current User Endpoint

### **GET** `/auth/me`

**Description:** Get currently authenticated user's information

**Authentication Required:** Yes (Session-based, user must be logged in)

**Request Headers:**
```
Cookie: JSESSIONID=<session-id>
```

**No Request Body Required**

**Success Response (200 OK):**
```json
{
  "employeeId": 1,
  "email": "admin@wheelshift.com",
  "name": "Super Admin",
  "roles": [
    "SUPER_ADMIN"
  ],
  "permissions": [
    "cars:read",
    "cars:write",
    "cars:delete",
    "inquiries:read",
    "inquiries:write",
    "inquiries:assign",
    "reservations:read",
    "reservations:write",
    "sales:read",
    "sales:write",
    "transactions:read",
    "transactions:write",
    "employees:read",
    "employees:write",
    "settings:manage",
    "acl:manage",
    "audit:read"
  ]
}
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| employeeId | number | Unique employee identifier |
| email | string | User's email address |
| name | string | User's full name |
| roles | string[] | Array of assigned role names |
| permissions | string[] | Array of permission strings |

**Error Responses:**

**401 Unauthorized** - Not authenticated
```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Full authentication is required to access this resource",
  "instance": "/api/v1/auth/me",
  "timestamp": "2025-12-21T10:00:00"
}
```

**Frontend Implementation Example:**
```typescript
// lib/api/auth.ts
export const getCurrentUser = async (): Promise<LoginResponse> => {
  const response = await axios.get(`${API_BASE_URL}/auth/me`, {
    withCredentials: true // Important for session cookies
  });
  return response.data;
};
```

**Usage:**
```typescript
// In a React component or hook
useEffect(() => {
  const fetchUser = async () => {
    try {
      const user = await getCurrentUser();
      setUser(user);
    } catch (error) {
      // User not authenticated, redirect to login
      router.push('/login');
    }
  };
  
  fetchUser();
}, []);
```

---

## 3. Logout Endpoint

### **POST** `/auth/logout`

**Description:** End user session and logout

**Authentication Required:** Yes

**Request Headers:**
```
Cookie: JSESSIONID=<session-id>
```

**No Request Body Required**

**Success Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| message | string | Success message |

**Error Responses:**

Usually returns 200 OK even if not authenticated

**Frontend Implementation Example:**
```typescript
// lib/api/auth.ts
export const logout = async (): Promise<void> => {
  await axios.post(`${API_BASE_URL}/auth/logout`, {}, {
    withCredentials: true
  });
  
  // Clear local storage
  localStorage.removeItem('user');
};
```

**Usage:**
```typescript
const handleLogout = async () => {
  try {
    await logout();
    router.push('/login');
  } catch (error) {
    console.error('Logout failed:', error);
    // Clear local data anyway
    localStorage.removeItem('user');
    router.push('/login');
  }
};
```

---

## Session Management Notes

### Important: Session-Based Authentication
This backend uses **session-based authentication** with cookies, not JWT tokens. Key points:

1. **Cookies Are Automatic:** After successful login, the server sets a `JSESSIONID` cookie
2. **Use withCredentials:** Always include `withCredentials: true` in axios requests
3. **No Token Storage:** You don't need to store or send tokens manually
4. **Session Persists:** The session cookie persists across requests automatically

### Axios Configuration for Sessions
```typescript
// lib/api/client.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
  withCredentials: true, // CRITICAL: Enable cookies
  headers: {
    'Content-Type': 'application/json',
  },
});

// Response interceptor for handling auth errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Session expired, redirect to login
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

---

## Testing with curl

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@wheelshift.com","password":"admin123"}' \
  -c cookies.txt
```

### Get Current User (using saved cookies)
```bash
curl http://localhost:8080/api/v1/auth/me \
  -b cookies.txt
```

### Logout
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -b cookies.txt
```

---

## Testing with Swagger UI

1. Open http://localhost:8080/swagger-ui.html
2. Find "Auth Controller" section
3. Try the `/auth/login` endpoint
4. After successful login, you can test other endpoints

---

## Next Steps

Once Phase 1 is complete:
1. ✅ Users can login
2. ✅ Session is maintained across requests
3. ✅ Users can logout
4. ✅ Protected routes work correctly

**Ready for Phase 2?** Ask for the next phase documentation!

---

## Common Issues & Solutions

### Issue: 401 errors on all requests after login
**Solution:** Ensure `withCredentials: true` is set in axios config

### Issue: CORS errors
**Solution:** Backend must allow credentials in CORS config. Check `SecurityConfig.java`

### Issue: Session expires quickly
**Solution:** Check Spring Session timeout configuration in `application.properties`

### Issue: Can't access protected routes
**Solution:** Verify the session cookie is being sent. Check browser DevTools > Network > Cookies

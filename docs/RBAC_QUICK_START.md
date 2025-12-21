# RBAC Quick Start Guide

## Prerequisites

- Java 17 or higher
- MySQL database running
- Maven installed
- Application properties configured

## Step 1: Run the Application

```bash
# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The Flyway migrations will automatically create and seed the RBAC tables.

## Step 2: Create a Super Admin User

You can either:

### Option A: Direct SQL Insert

```sql
-- Insert employee (note: password is BCrypt hash of 'admin123')
INSERT INTO employees (name, email, password_hash, position, department, status, join_date, created_at, updated_at) 
VALUES (
    'Super Admin', 
    'admin@wheelshift.com', 
    '$2a$10$rI8qs3Y3KZl8F9qwL.hLPuqF5xB9jXMQQ6z6vYHy/5sZpYK1nE7JW',
    'System Administrator',
    'IT',
    'ACTIVE',
    CURRENT_DATE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Assign SUPER_ADMIN role
INSERT INTO employee_roles (employee_id, role_id)
SELECT e.id, r.id 
FROM employees e, roles r 
WHERE e.email = 'admin@wheelshift.com' AND r.name = 'SUPER_ADMIN';
```

### Option B: Using Application API (After Creating First User Manually)

Once you have one super admin, you can create others via the API.

## Step 3: Test Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@wheelshift.com",
    "password": "admin123"
  }'
```

Expected response:
```json
{
  "employeeId": 1,
  "email": "admin@wheelshift.com",
  "name": "Super Admin",
  "roles": ["SUPER_ADMIN"],
  "permissions": ["cars:read", "cars:write", ...],
  "message": "Login successful"
}
```

## Step 4: Create Other Users

### Create an Admin User

```bash
# 1. Create employee
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Admin",
    "email": "john@wheelshift.com",
    "passwordHash": "$2a$10$rI8qs3Y3KZl8F9qwL.hLPuqF5xB9jXMQQ6z6vYHy/5sZpYK1nE7JW",
    "position": "Manager",
    "department": "Sales",
    "status": "ACTIVE"
  }'

# 2. Assign ADMIN role (assuming employee ID is 2 and ADMIN role ID is 2)
curl -X POST http://localhost:8080/api/v1/rbac/employees/2/roles/2
```

### Create a Sales Rep

```bash
# 1. Create employee
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Sales",
    "email": "jane@wheelshift.com",
    "passwordHash": "$2a$10$rI8qs3Y3KZl8F9qwL.hLPuqF5xB9jXMQQ6z6vYHy/5sZpYK1nE7JW",
    "position": "Sales Representative",
    "department": "Sales",
    "status": "ACTIVE"
  }'

# 2. Assign SALES role (assuming employee ID is 3 and SALES role ID is 3)
curl -X POST http://localhost:8080/api/v1/rbac/employees/3/roles/3
```

## Step 5: Add Data Scopes

### Restrict Admin to Specific Location

```bash
curl -X POST http://localhost:8080/api/v1/rbac/employees/2/scopes \
  -H "Content-Type: application/json" \
  -d '{
    "scopeType": "LOCATION",
    "scopeValue": "1",
    "effect": "INCLUDE",
    "description": "Main warehouse access only"
  }'
```

### Restrict Sales Rep to Their Own Assignments

```bash
curl -X POST http://localhost:8080/api/v1/rbac/employees/3/scopes \
  -H "Content-Type: application/json" \
  -d '{
    "scopeType": "ASSIGNMENT",
    "scopeValue": "self",
    "effect": "INCLUDE",
    "description": "Access only assigned inquiries and reservations"
  }'
```

## Step 6: Create Resource ACLs (Optional)

### Restrict a Car to Specific Employees

```bash
curl -X POST http://localhost:8080/api/v1/rbac/acl/CAR/1 \
  -H "Content-Type: application/json" \
  -H "X-Employee-Id: 1" \
  -d '{
    "subjectType": "EMPLOYEE",
    "subjectId": 2,
    "access": "WRITE",
    "reason": "VIP client vehicle - restricted access"
  }'
```

## Step 7: View API Documentation

Visit: http://localhost:8080/swagger-ui.html

You'll see all RBAC endpoints organized under:
- RBAC - Roles
- RBAC - Permissions
- RBAC - Employee Roles
- RBAC - Data Scopes
- RBAC - ACL
- Authentication

## Common Operations

### List All Roles

```bash
curl http://localhost:8080/api/v1/rbac/roles
```

### List All Permissions

```bash
curl http://localhost:8080/api/v1/rbac/permissions
```

### Get Employee's Roles

```bash
curl http://localhost:8080/api/v1/rbac/employees/2/roles
```

### Get Employee's Permissions

```bash
curl http://localhost:8080/api/v1/rbac/permissions/employee/2
```

### Get Employee's Data Scopes

```bash
curl http://localhost:8080/api/v1/rbac/employees/2/scopes
```

### Get ACL for a Car

```bash
curl http://localhost:8080/api/v1/rbac/acl/CAR/1
```

## Password Hashing

To generate BCrypt password hashes for new users:

```java
// Java code
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("plainPassword");
System.out.println(hashedPassword);
```

Or use an online BCrypt generator (for development only):
- https://bcrypt-generator.com/
- Use rounds: 10

## Default Passwords

For the quick start, all users use password: `admin123`

**Hash**: `$2a$10$rI8qs3Y3KZl8F9qwL.hLPuqF5xB9jXMQQ6z6vYHy/5sZpYK1nE7JW`

**⚠️ IMPORTANT**: Change these passwords in production!

## Testing Role-Based Access

### Test as Super Admin
```bash
# Login as super admin
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@wheelshift.com","password":"admin123"}' \
  | jq -r '.token')

# Access any resource
curl http://localhost:8080/api/v1/cars
```

### Test as Sales Rep
```bash
# Login as sales rep
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@wheelshift.com","password":"admin123"}'

# Try to access cars (should work - read only)
curl http://localhost:8080/api/v1/cars

# Try to delete a car (should fail - no permission)
curl -X DELETE http://localhost:8080/api/v1/cars/1
```

## Troubleshooting

### Issue: Migrations Don't Run

Check Flyway configuration in `application.properties`:
```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

### Issue: Cannot Login

1. Verify employee exists in database
2. Check password hash is correct
3. Verify employee status is 'ACTIVE'
4. Check role assignment in employee_roles table

### Issue: 403 Forbidden

1. Verify user has required role/permission
2. Check data scopes
3. Check resource ACLs
4. Review SecurityConfig rules

### Issue: Roles Not Loading

1. Check employee_roles table
2. Verify role exists in roles table
3. Check join_date and status fields

## Development Tips

1. **Use Swagger UI** for interactive API testing
2. **Check application logs** for authorization decisions
3. **Enable SQL logging** to debug queries:
   ```properties
   spring.jpa.show-sql=true
   logging.level.org.hibernate.SQL=DEBUG
   ```
4. **Use Postman** for organized API testing
5. **Create test data scripts** for consistent testing

## Next Steps

1. ✅ Basic RBAC setup complete
2. 🔄 Add `@PreAuthorize` annotations to existing controllers
3. 🔄 Implement data scope filtering in service layer
4. 🔄 Add JWT token authentication
5. 🔄 Implement audit logging
6. 🔄 Add permission caching
7. 🔄 Create comprehensive tests

## Support

For detailed documentation, see:
- `RBAC_IMPLEMENTATION_GUIDE.md` - Full implementation details
- `RBAC_IMPLEMENTATION_SUMMARY.md` - Summary of changes
- API Docs: http://localhost:8080/swagger-ui.html

## Security Reminder

🔒 **Remember to**:
- Change default passwords
- Use strong passwords in production
- Enable HTTPS
- Configure CORS properly
- Implement rate limiting
- Add audit logging
- Regular security reviews

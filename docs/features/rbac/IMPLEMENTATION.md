# RBAC Implementation Details

## Quick Reference

### Authorization Check Examples

```java
// Basic permission check
@PreAuthorize("@authService.hasPermission(authentication.name, 'cars:write')")
public ResponseEntity<?> createCar() { }

// Resource access check
boolean canEdit = authorizationService.canAccessResource(
    employeeId, ResourceType.CAR, carId, AccessLevel.WRITE
);

// Apply data scopes to query
Specification<Car> spec = authorizationService.applyDataScopes(
    employeeId, ResourceType.CAR
);
List<Car> cars = carRepository.findAll(spec);
```

### Permission Format

```
resource:action

Examples:
- cars:read
- cars:write
- cars:delete
- cars:*
- inquiries:*
```

### System Roles & Permissions

#### SUPER_ADMIN (ID: 1)
- All permissions (`*:*`)

#### ADMIN (ID: 2)
- `employees:*`, `roles:*`, `permissions:*`
- `locations:*`, `reports:read`

#### SALES (ID: 3)
- `inquiries:*`, `reservations:*`, `sales:*`
- `clients:read`, `cars:read`

#### INSPECTOR (ID: 4)
- `inspections:*`, `cars:read`, `movements:write`

#### FINANCE (ID: 5)
- `transactions:*`, `sales:read`, `reports:*`

#### STORE_MANAGER (ID: 6)
- `cars:*`, `movements:*`, `locations:read`
- `inspections:read`, `tasks:*`

## Database Schema

### Tables

```sql
-- Roles
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    role_type VARCHAR(20),
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Role-Permission Mapping
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

-- Employee-Role Assignment
CREATE TABLE employee_roles (
    employee_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (employee_id, role_id)
);

-- Data Scopes
CREATE TABLE employee_data_scopes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    scope_type VARCHAR(20) NOT NULL,
    scope_value VARCHAR(255) NOT NULL,
    effect VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Resource ACLs
CREATE TABLE resource_acl (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100) NOT NULL,
    subject_type VARCHAR(20) NOT NULL,
    subject_id BIGINT NOT NULL,
    access_level VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Authorization Flow

```
1. User makes request with JWT token
2. SecurityFilter validates token and extracts employee ID
3. @PreAuthorize checks permission
   └─> AuthorizationService.hasPermission()
       ├─> Check Resource ACL (highest precedence)
       ├─> Check Data Scopes (medium precedence)
       └─> Check Role Permissions (lowest precedence)
4. If authorized, proceed to method
5. Apply data scope filters to query results
```

## Implementation Classes

### Core Classes

- **AuthorizationService** - Main authorization logic
- **RoleService** - Role management
- **PermissionService** - Permission management
- **DataScopeService** - Data scope management
- **ResourceACLService** - ACL management

### Security Configuration

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

## Migration Files

- **V4__Add_RBAC_Tables.sql** - Creates tables
- **V5__Seed_RBAC_Data.sql** - Seeds roles and permissions

## Testing

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
    Employee employee = createEmployee();
    EmployeeDataScope scope = new EmployeeDataScope();
    scope.setEmployee(employee);
    scope.setScopeType(ScopeType.LOCATION);
    scope.setScopeValue("LOC-001");
    scope.setEffect(ScopeEffect.INCLUDE);
    dataScopeRepository.save(scope);
    
    // Apply scopes to query
    Specification<Car> spec = authService.applyDataScopes(
        employee.getId(), ResourceType.CAR
    );
    List<Car> cars = carRepository.findAll(spec);
    
    // Verify only LOC-001 cars returned
    assertTrue(cars.stream()
        .allMatch(car -> "LOC-001".equals(car.getLocation().getCode())));
}
```

## API Examples

See [README.md](README.md) for complete API documentation.

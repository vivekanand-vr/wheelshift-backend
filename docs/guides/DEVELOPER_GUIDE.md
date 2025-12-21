# Developer Guide

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- MySQL 8.0+
- Redis (optional, for caching)
- Git

### Setup

1. **Clone Repository**
```bash
git clone <repository-url>
cd WheelShiftPro
```

2. **Configure Database**

Create MySQL database:
```sql
CREATE DATABASE wheelshiftpro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'wheelshift'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON wheelshiftpro.* TO 'wheelshift'@'localhost';
FLUSH PRIVILEGES;
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wheelshiftpro
spring.datasource.username=wheelshift
spring.datasource.password=your_password
```

3. **Build Project**
```bash
./mvnw clean install
```

4. **Run Application**
```bash
./mvnw spring-boot:run
```

5. **Access Application**
- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/api/v1/swagger-ui.html

## Project Structure

```
src/main/java/com/wheelshiftpro/
├── config/              # Configuration classes
│   ├── JpaAuditingConfig.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/          # REST controllers (versioned /api/v1)
├── dto/                 # Request/Response DTOs
│   ├── request/
│   └── response/
├── entity/              # JPA entities
├── enums/               # Enumerations
├── exception/           # Exception handling
│   └── GlobalExceptionHandler.java
├── mapper/              # MapStruct mappers
├── repository/          # Spring Data repositories
├── security/            # Security components
│   ├── EmployeeUserDetails.java
│   └── EmployeeUserDetailsService.java
└── service/             # Business logic
    ├── (interfaces)
    └── impl/            # Implementations
```

## Development Workflow

### 1. Creating a New Feature

```bash
# Create feature branch
git checkout -b feature/new-feature

# Make changes
# ... code ...

# Commit changes
git add .
git commit -m "feat: add new feature"

# Push to remote
git push origin feature/new-feature
```

### 2. Adding a New Entity

1. Create entity in `entity/` package
2. Create repository in `repository/` package
3. Create DTOs in `dto/request/` and `dto/response/`
4. Create mapper in `mapper/` package
5. Create service interface and implementation
6. Create controller with REST endpoints
7. Create Flyway migration for database schema

Example:
```java
// 1. Entity
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Column(nullable = false)
    private String name;
    private BigDecimal price;
}

// 2. Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContaining(String name);
}

// 3. DTOs
@Data
public class ProductRequest {
    @NotBlank
    private String name;
    @NotNull
    private BigDecimal price;
}

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
}

// 4. Mapper
@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toEntity(ProductRequest request);
    ProductResponse toResponse(Product product);
}

// 5. Service
public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProduct(Long id);
}

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    // Implementation
}

// 6. Controller
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }
}

// 7. Migration (VX__Add_Products_Table.sql)
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3. Database Migrations

Flyway migrations are in `src/main/resources/db/migration/`

Naming convention: `V{version}__{description}.sql`

Example:
```sql
-- V7__Add_New_Feature.sql
CREATE TABLE new_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO new_table (name) VALUES ('Initial Data');
```

### 4. Testing

Run tests:
```bash
./mvnw test
```

Write tests:
```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Test\",\"price\":100}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test"));
    }
}
```

## Code Standards

### Naming Conventions

- **Classes**: PascalCase (e.g., `CarService`)
- **Methods**: camelCase (e.g., `getCar()`)
- **Variables**: camelCase (e.g., `carId`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_PRICE`)
- **Packages**: lowercase (e.g., `com.wheelshiftpro.service`)

### REST API Conventions

- **Base URL**: `/api/v1`
- **Resource naming**: Plural nouns (e.g., `/cars`, `/clients`)
- **HTTP Methods**:
  - GET - Retrieve resources
  - POST - Create resources
  - PUT - Update resources
  - DELETE - Delete resources
- **Status Codes**:
  - 200 OK - Success
  - 201 Created - Resource created
  - 204 No Content - Success with no body
  - 400 Bad Request - Validation error
  - 404 Not Found - Resource not found
  - 500 Internal Server Error - Server error

### Documentation

- Use Javadoc for public methods
- Use `@ApiOperation` for Swagger documentation
- Include examples in Swagger

```java
@GetMapping("/{id}")
@Operation(summary = "Get car by ID", description = "Returns car details")
@ApiResponse(responseCode = "200", description = "Success")
@ApiResponse(responseCode = "404", description = "Car not found")
public ResponseEntity<CarResponse> getCar(@PathVariable Long id) {
    return ResponseEntity.ok(carService.getCar(id));
}
```

## Common Tasks

### Add New Permission

1. Add to `V5__Seed_RBAC_Data.sql`:
```sql
INSERT INTO permissions (name, description, resource, action) 
VALUES ('products:read', 'View products', 'products', 'read');
```

2. Assign to role:
```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE name = 'products:read';
```

### Add Notification Event

1. Define event type constant
2. Create template in database
3. Trigger in service:
```java
notificationHelper.notifyEmployee(
    employeeId, "product.created", "PRODUCT", productId, data
);
```

### Protect Endpoint

```java
@GetMapping
@PreAuthorize("@authService.hasPermission(authentication.name, 'products:read')")
public List<ProductResponse> getProducts() {
    return productService.getAllProducts();
}
```

## Debugging

### Enable Debug Logging

```properties
logging.level.com.wheelshiftpro=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### View Logs

Logs are in `logs/` directory:
- `application.log` - All logs
- `error.log` - Error logs only

### Database Issues

Check Flyway migrations:
```sql
SELECT * FROM flyway_schema_history;
```

Manually repair if needed:
```bash
./mvnw flyway:repair
```

## Deployment

### Build Production JAR

```bash
./mvnw clean package -DskipTests
```

Output: `target/WheelShiftPro-0.0.1-SNAPSHOT.jar`

### Run Production

```bash
java -jar target/WheelShiftPro-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://prod-db:3306/wheelshiftpro
```

### Docker Deployment

```bash
# Build image
docker build -t wheelshiftpro:latest .

# Run container
docker-compose up -d
```

## Troubleshooting

### Build Fails

```bash
# Clean and rebuild
./mvnw clean
./mvnw install -U
```

### Database Connection Error

- Check MySQL is running
- Verify credentials in `application.properties`
- Check firewall settings

### Port Already in Use

Change port in `application.properties`:
```properties
server.port=8081
```

### Flyway Migration Error

```bash
# Check migration status
./mvnw flyway:info

# Repair if needed
./mvnw flyway:repair
```

## Useful Commands

```bash
# Clean build
./mvnw clean install

# Run tests
./mvnw test

# Run specific test
./mvnw test -Dtest=CarServiceTest

# Skip tests
./mvnw install -DskipTests

# Generate Javadoc
./mvnw javadoc:javadoc

# Check dependencies
./mvnw dependency:tree

# Update dependencies
./mvnw versions:display-dependency-updates
```

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [MapStruct](https://mapstruct.org/)
- [Flyway](https://flywaydb.org/)
- [Swagger/OpenAPI](https://swagger.io/)

## Getting Help

- Check [README.md](../README.md) for overview
- Review [API Documentation](http://localhost:8080/api/v1/swagger-ui.html)
- See feature guides in [docs/features/](../features/)
- Contact development team

# Redis Dependencies - Add to pom.xml

Add these dependencies to your `pom.xml` file if not already present:

```xml
<!-- Redis and Caching Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Redis Connection Pool (Lettuce - default) -->
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>

<!-- OR use Jedis instead of Lettuce (uncomment if preferred) -->
<!--
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>
-->

<!-- Jackson for JSON serialization (usually already present) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

## Verification

After adding dependencies, run:

```bash
./mvnw clean install
```

Check that there are no dependency conflicts in the build output.

## application.properties

Add Redis configuration:

```properties
# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.database=0
spring.redis.timeout=2000ms

# Connection Pool (Lettuce)
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=0
spring.redis.lettuce.pool.max-wait=-1ms

# OR Connection Pool (Jedis) - uncomment if using Jedis
# spring.redis.jedis.pool.max-active=8
# spring.redis.jedis.pool.max-idle=8
# spring.redis.jedis.pool.min-idle=0
# spring.redis.jedis.pool.max-wait=-1ms

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=30m
spring.cache.redis.cache-null-values=false

# Enable caching
spring.cache.cache-names=adminDashboard,salesDashboard,inspectorDashboard,financeDashboard,storeManagerDashboard,cars,carDetails,carModels,carStatistics,clients,employees,sales,financialTransactions,revenueMetrics,inquiries,reservations,storageLocations,locationCapacity,tasks,events,inspections,roles,permissions,employeeRoles,notifications,notificationTemplates
```

## Testing Redis Connection

Create a simple test:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
class RedisConnectionTest {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    void testRedisConnection() {
        // Test connection
        redisTemplate.opsForValue().set("test:key", "test-value");
        String value = (String) redisTemplate.opsForValue().get("test:key");
        
        assertThat(value).isEqualTo("test-value");
        
        // Cleanup
        redisTemplate.delete("test:key");
    }
}
```

Run the test:

```bash
# Start Redis first
docker-compose up -d redis

# Run test
./mvnw test -Dtest=RedisConnectionTest
```

## Troubleshooting

### Issue: Could not find spring-boot-starter-data-redis

**Solution:** Update Spring Boot version in pom.xml:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version> <!-- or later -->
</parent>
```

### Issue: NoClassDefFoundError for Lettuce/Jedis

**Solution:** Explicitly add the connection pool dependency:

```xml
<!-- For Lettuce (default) -->
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>

<!-- OR for Jedis -->
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>
```

### Issue: Jackson serialization errors

**Solution:** Ensure all DTOs are properly serializable:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

---

## Complete pom.xml Example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.wheelshiftpro</groupId>
    <artifactId>wheelshiftpro</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>WheelShift Pro</name>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- Redis and Caching -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        
        <!-- Other dependencies... -->
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

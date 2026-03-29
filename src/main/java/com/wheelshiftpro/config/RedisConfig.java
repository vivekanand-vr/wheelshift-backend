package com.wheelshiftpro.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wheelshiftpro.notification.NotificationRedisMessageListener;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for caching and session management
 * 
 * Features:
 * - Centralized cache configuration with TTL
 * - JSON serialization for complex objects
 * - Multiple cache regions with different TTLs
 * - RedisTemplate for manual cache operations
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configure cache manager with different TTL for different cache regions
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default TTL: 30 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(createJsonSerializer()))
                .disableCachingNullValues();

        // Cache-specific configurations with custom TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Dashboard caches - refresh frequently for real-time data
        cacheConfigurations.put("adminDashboard", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("salesDashboard", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("inspectorDashboard", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("financeDashboard", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("storeManagerDashboard", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Car and inventory caches - moderate refresh rate
        cacheConfigurations.put("cars", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("carDetails", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("carModels", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("carStatistics", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Client and employee caches - less frequent changes
        cacheConfigurations.put("clients", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("employees", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Sales and financial caches - critical data
        cacheConfigurations.put("sales", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("financialTransactions", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("revenueMetrics", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Inquiry and reservation caches - frequently updated
        cacheConfigurations.put("inquiries", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("reservations", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Storage location caches - relatively static
        cacheConfigurations.put("storageLocations", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("locationCapacity", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Task and event caches
        cacheConfigurations.put("tasks", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("events", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Inspection caches
        cacheConfigurations.put("inspections", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // RBAC caches - rarely change, cache longer
        cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("permissions", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("employeeRoles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Notification caches
        cacheConfigurations.put("notifications", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("notificationTemplates", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * RedisTemplate for manual cache operations and custom use cases
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        @SuppressWarnings("removal")
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Create JSON serializer with Java 8 time support
     */
    @SuppressWarnings("removal")
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register Java 8 time module for LocalDate, LocalDateTime, etc.
        objectMapper.registerModule(new JavaTimeModule());
        
        // Enable type information for polymorphic deserialization
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * Plain String Redis template used for Pub/Sub publishing in the Kafka consumer.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * Redis Pub/Sub listener container.
     * Subscribes to the pattern {@code notification:*:*} so every notification channel
     * is routed to {@link NotificationRedisMessageListener} on this instance.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            NotificationRedisMessageListener listener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // Pattern matches e.g. notification:EMPLOYEE:42, notification:CLIENT:7
        container.addMessageListener(listener, new PatternTopic("notification:*:*"));
        return container;
    }
}

package com.wheelshiftpro.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Configures ShedLock to use Redis as the distributed lock store.
 *
 * This prevents multiple instances of the application (in a clustered deployment)
 * from executing the same @Scheduled job simultaneously. Every scheduled method
 * annotated with @SchedulerLock will acquire a Redis-backed lock before running.
 *
 * defaultLockAtMostFor: maximum time a lock is held even if the node crashes.
 * defaultLockAtLeastFor: minimum time to hold the lock, preventing re-runs on fast jobs.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m", defaultLockAtLeastFor = "5s")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory, "wheelshift");
    }
}

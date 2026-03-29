package com.wheelshiftpro.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

/**
 * Async and scheduling configuration.
 *
 * <p>This class is REQUIRED for {@code @Async} annotations to work. Without
 * {@code @EnableAsync}, all async method calls block the calling thread.
 *
 * <p>Multiple named executors are provided for different workload types:
 * <ul>
 *   <li>{@code notificationExecutor} — fire-and-forget notification delivery</li>
 *   <li>{@code reportExecutor} — heavy report/export generation</li>
 *   <li>{@code scheduledTaskExecutor} — background scheduled tasks</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * @Async("notificationExecutor")
 * public CompletableFuture<Void> sendNotificationAsync(NotificationJob job) { ... }
 * }</pre>
 */
@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Executor for notification dispatch — fast, high-concurrency, fire-and-forget.
     * Uses CallerRunsPolicy so the calling thread handles the task if the queue is full,
     * preventing notification drops.
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("notification-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Executor for heavy background tasks (PDF generation, Excel exports, bulk imports).
     * Lower concurrency to avoid overloading the database.
     */
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("report-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }

    /**
     * Default executor used when {@code @Async} has no executor name specified.
     * Moderately sized for general-purpose async work.
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Global uncaught exception handler for all {@code @Async} methods.
     * Logs the failure — the caller will NOT see this exception since async
     * methods return void or CompletableFuture.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) ->
            log.error("Uncaught exception in @Async method '{}': {}",
                method.getDeclaringClass().getSimpleName() + "." + method.getName(),
                throwable.getMessage(),
                throwable);
    }
}

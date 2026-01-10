package com.wheelshiftpro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for managing cache invalidation across the application.
 * 
 * This service provides methods to:
 * - Clear specific caches
 * - Clear all caches
 * - Clear cache entries by key
 * - Invalidate dashboard caches when data changes
 * 
 * Usage:
 * - Inject this service and call eviction methods after data modifications
 * - Use @CacheEvict annotation on service methods that modify data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {

    private final CacheManager cacheManager;

    /**
     * Evict a specific key from a specific cache
     */
    public void evictCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Evicted cache: {} with key: {}", cacheName, key);
        }
    }

    /**
     * Clear all entries in a specific cache
     */
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cleared cache: {}", cacheName);
        }
    }

    /**
     * Clear all caches in the application
     */
    public void clearAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                });
        log.info("Cleared all caches");
    }

    /**
     * Invalidate all dashboard caches
     * Call this when critical data changes that affect dashboards
     */
    public void invalidateDashboards() {
        clearCache("adminDashboard");
        clearCache("salesDashboard");
        clearCache("inspectorDashboard");
        clearCache("financeDashboard");
        clearCache("storeManagerDashboard");
        log.info("Invalidated all dashboard caches");
    }

    /**
     * Invalidate dashboard for a specific employee
     */
    public void invalidateEmployeeDashboards(Long employeeId) {
        evictCache("adminDashboard", employeeId);
        evictCache("salesDashboard", employeeId);
        evictCache("inspectorDashboard", employeeId);
        evictCache("financeDashboard", employeeId);
        evictCache("storeManagerDashboard", employeeId);
        log.debug("Invalidated dashboards for employee: {}", employeeId);
    }

    /**
     * Invalidate car-related caches
     * Call when cars are created, updated, or deleted
     */
    public void invalidateCarCaches() {
        clearCache("cars");
        clearCache("carDetails");
        clearCache("carStatistics");
        invalidateDashboards(); // Dashboards show car data
        log.info("Invalidated car caches");
    }

    /**
     * Invalidate car cache for specific car ID
     */
    public void invalidateCarCache(Long carId) {
        evictCache("carDetails", carId);
        clearCache("cars");
        clearCache("carStatistics");
        invalidateDashboards();
        log.debug("Invalidated car cache for ID: {}", carId);
    }

    /**
     * Invalidate sale-related caches
     * Call when sales are created or updated
     */
    public void invalidateSaleCaches() {
        clearCache("sales");
        clearCache("revenueMetrics");
        invalidateDashboards();
        log.info("Invalidated sale caches");
    }

    /**
     * Invalidate client-related caches
     */
    public void invalidateClientCaches() {
        clearCache("clients");
        invalidateDashboards();
        log.info("Invalidated client caches");
    }

    /**
     * Invalidate employee-related caches
     */
    public void invalidateEmployeeCaches() {
        clearCache("employees");
        clearCache("employeeRoles");
        invalidateDashboards();
        log.info("Invalidated employee caches");
    }

    /**
     * Invalidate inquiry-related caches
     */
    public void invalidateInquiryCaches() {
        clearCache("inquiries");
        invalidateDashboards();
        log.info("Invalidated inquiry caches");
    }

    /**
     * Invalidate reservation-related caches
     */
    public void invalidateReservationCaches() {
        clearCache("reservations");
        invalidateDashboards();
        log.info("Invalidated reservation caches");
    }

    /**
     * Invalidate financial transaction caches
     */
    public void invalidateFinancialCaches() {
        clearCache("financialTransactions");
        clearCache("revenueMetrics");
        invalidateDashboards();
        log.info("Invalidated financial caches");
    }

    /**
     * Invalidate storage location caches
     */
    public void invalidateLocationCaches() {
        clearCache("storageLocations");
        clearCache("locationCapacity");
        invalidateDashboards();
        log.info("Invalidated location caches");
    }

    /**
     * Invalidate inspection caches
     */
    public void invalidateInspectionCaches() {
        clearCache("inspections");
        invalidateDashboards();
        log.info("Invalidated inspection caches");
    }

    /**
     * Invalidate task-related caches
     */
    public void invalidateTaskCaches() {
        clearCache("tasks");
        invalidateDashboards();
        log.info("Invalidated task caches");
    }

    /**
     * Invalidate notification caches
     */
    public void invalidateNotificationCaches() {
        clearCache("notifications");
        log.info("Invalidated notification caches");
    }

    /**
     * Asynchronously invalidate caches (non-blocking)
     * Use this for background cache invalidation
     */
    @Async
    public void asyncInvalidateDashboards() {
        invalidateDashboards();
    }

    /**
     * Asynchronously clear all caches
     */
    @Async
    public void asyncClearAllCaches() {
        clearAllCaches();
    }

    /**
     * Get cache statistics (useful for monitoring)
     */
    public void logCacheStatistics() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                log.info("Cache: {} | Native Cache: {}", cacheName, cache.getNativeCache().getClass().getSimpleName());
            }
        });
    }
}

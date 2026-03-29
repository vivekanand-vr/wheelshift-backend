package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StorageLocation entity.
 */
@Repository
public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    /**
     * Find storage locations with available capacity.
     */
    @Query("SELECT sl FROM StorageLocation sl WHERE (sl.currentCarCount + sl.currentMotorcycleCount) < sl.totalCapacity")
    List<StorageLocation> findAvailableLocations();

    /**
     * Find storage locations by name containing (case-insensitive).
     */
    List<StorageLocation> findByNameContainingIgnoreCase(String name);

    /**
     * Check if location exists by name.
     */
    boolean existsByName(String name);

    /**
     * Check if another location exists with the same name, excluding the given ID.
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Get locations ordered by available capacity descending.
     */
    @Query("SELECT sl FROM StorageLocation sl ORDER BY (sl.totalCapacity - sl.currentCarCount - sl.currentMotorcycleCount) DESC")
    List<StorageLocation> findAllOrderByAvailableCapacityDesc();
}

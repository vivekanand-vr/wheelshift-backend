package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.MotorcycleMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for MotorcycleMovement entity.
 * Provides data access methods for tracking motorcycle location changes.
 */
@Repository
public interface MotorcycleMovementRepository extends JpaRepository<MotorcycleMovement, Long> {

    /**
     * Find all movements for a specific motorcycle
     */
    List<MotorcycleMovement> findByMotorcycleIdOrderByMovedAtDesc(Long motorcycleId);

    /**
     * Find all movements from a specific location
     */
    List<MotorcycleMovement> findByFromLocationId(Long locationId);

    /**
     * Find all movements to a specific location
     */
    List<MotorcycleMovement> findByToLocationId(Long locationId);

    /**
     * Find movements within a date range
     */
    List<MotorcycleMovement> findByMovedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find movements by employee
     */
    List<MotorcycleMovement> findByMovedById(Long employeeId);

    /**
     * Get latest movement for a motorcycle
     */
    @Query("SELECT mm FROM MotorcycleMovement mm WHERE mm.motorcycle.id = :motorcycleId ORDER BY mm.movedAt DESC")
    List<MotorcycleMovement> findLatestMovementByMotorcycleId(@Param("motorcycleId") Long motorcycleId);

    /**
     * Count movements for a motorcycle
     */
    Long countByMotorcycleId(Long motorcycleId);

    /**
     * Find movements between two specific locations
     */
    @Query("SELECT mm FROM MotorcycleMovement mm WHERE mm.fromLocation.id = :fromLocationId AND mm.toLocation.id = :toLocationId")
    List<MotorcycleMovement> findMovementsBetweenLocations(@Param("fromLocationId") Long fromLocationId, 
                                                            @Param("toLocationId") Long toLocationId);

    /**
     * Get recent movements (last N days)
     */
    @Query("SELECT mm FROM MotorcycleMovement mm WHERE mm.movedAt >= :sinceDate ORDER BY mm.movedAt DESC")
    List<MotorcycleMovement> findRecentMovements(@Param("sinceDate") LocalDateTime sinceDate);
}

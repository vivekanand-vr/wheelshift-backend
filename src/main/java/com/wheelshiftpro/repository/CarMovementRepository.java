package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.CarMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository interface for CarMovement entity.
 */
@Repository
public interface CarMovementRepository extends JpaRepository<CarMovement, Long> {

    /**
     * Find all movements for a car.
     */
    Page<CarMovement> findByCarId(Long carId, Pageable pageable);

    /**
     * Find movements within a date range.
     */
    Page<CarMovement> findByMovedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find movements to a specific location.
     */
    Page<CarMovement> findByToLocationId(Long locationId, Pageable pageable);

    /**
     * Find movements from a specific location.
     */
    Page<CarMovement> findByFromLocationId(Long locationId, Pageable pageable);
}

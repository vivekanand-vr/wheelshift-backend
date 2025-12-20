package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.CarInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository interface for CarInspection entity.
 */
@Repository
public interface CarInspectionRepository extends JpaRepository<CarInspection, Long>, JpaSpecificationExecutor<CarInspection> {

    /**
     * Find all inspections for a car.
     */
    Page<CarInspection> findByCarId(Long carId, Pageable pageable);

    /**
     * Find latest inspection for a car.
     */
    @Query("SELECT ci FROM CarInspection ci WHERE ci.car.id = :carId ORDER BY ci.inspectionDate DESC")
    Optional<CarInspection> findLatestByCarId(@Param("carId") Long carId);

    /**
     * Find inspections by date range.
     */
    Page<CarInspection> findByInspectionDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find inspections by inspector.
     */
    Page<CarInspection> findByInspectorNameContainingIgnoreCase(String inspectorName, Pageable pageable);

    /**
     * Find inspections by pass status.
     */
    Page<CarInspection> findByInspectionPass(Boolean pass, Pageable pageable);

    /**
     * Count inspections by car.
     */
    long countByCarId(Long carId);
}

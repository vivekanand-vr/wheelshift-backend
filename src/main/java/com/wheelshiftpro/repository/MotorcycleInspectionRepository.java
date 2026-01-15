package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.MotorcycleInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MotorcycleInspection entity.
 * Provides data access methods for motorcycle inspection records.
 */
@Repository
public interface MotorcycleInspectionRepository extends JpaRepository<MotorcycleInspection, Long> {

    /**
     * Find all inspections for a specific motorcycle
     */
    List<MotorcycleInspection> findByMotorcycleIdOrderByInspectionDateDesc(Long motorcycleId);

    /**
     * Find inspections by inspector
     */
    List<MotorcycleInspection> findByInspectorId(Long inspectorId);

    /**
     * Find latest inspection for a motorcycle
     */
    @Query("SELECT mi FROM MotorcycleInspection mi WHERE mi.motorcycle.id = :motorcycleId " +
           "ORDER BY mi.inspectionDate DESC")
    Optional<MotorcycleInspection> findLatestInspectionForMotorcycle(@Param("motorcycleId") Long motorcycleId);

    /**
     * Find inspections within date range
     */
    List<MotorcycleInspection> findByInspectionDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find failed inspections
     */
    List<MotorcycleInspection> findByPassedFalse();

    /**
     * Find inspections requiring repair
     */
    List<MotorcycleInspection> findByRequiresRepairTrue();

    /**
     * Find inspections with accident history
     */
    List<MotorcycleInspection> findByHasAccidentHistoryTrue();

    /**
     * Find inspections by overall condition
     */
    Page<MotorcycleInspection> findByOverallCondition(String condition, Pageable pageable);

    /**
     * Count inspections by inspector
     */
    Long countByInspectorId(Long inspectorId);
}

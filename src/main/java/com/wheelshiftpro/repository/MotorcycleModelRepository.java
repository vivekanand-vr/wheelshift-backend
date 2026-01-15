package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.MotorcycleModel;
import com.wheelshiftpro.enums.MotorcycleVehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MotorcycleModel entity.
 * Provides data access methods for motorcycle model catalog.
 */
@Repository
public interface MotorcycleModelRepository extends JpaRepository<MotorcycleModel, Long>, 
                                                    JpaSpecificationExecutor<MotorcycleModel> {

    /**
     * Find motorcycle model by make, model, variant and year
     */
    Optional<MotorcycleModel> findByMakeAndModelAndVariantAndYear(
            String make, String model, String variant, Integer year);

    /**
     * Find all models by make
     */
    List<MotorcycleModel> findByMakeIgnoreCase(String make);

    /**
     * Find all models by vehicle type
     */
    List<MotorcycleModel> findByVehicleType(MotorcycleVehicleType vehicleType);

    /**
     * Find all active models
     */
    List<MotorcycleModel> findByIsActiveTrue();

    /**
     * Find models by year range
     */
    List<MotorcycleModel> findByYearBetween(Integer startYear, Integer endYear);

    /**
     * Search models by make or model name (case-insensitive)
     */
    @Query("SELECT mm FROM MotorcycleModel mm WHERE " +
           "LOWER(mm.make) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mm.model) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<MotorcycleModel> searchByMakeOrModel(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Get all unique makes
     */
    @Query("SELECT DISTINCT mm.make FROM MotorcycleModel mm ORDER BY mm.make")
    List<String> findAllUniqueMakes();

    /**
     * Count models by make
     */
    Long countByMake(String make);

    /**
     * Check if model exists
     */
    boolean existsByMakeAndModelAndVariantAndYear(
            String make, String model, String variant, Integer year);
}

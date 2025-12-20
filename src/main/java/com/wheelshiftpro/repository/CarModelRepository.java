package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.CarModel;
import com.wheelshiftpro.enums.FuelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CarModel entity.
 */
@Repository
public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    /**
     * Find car model by make, model, and variant.
     */
    Optional<CarModel> findByMakeAndModelAndVariant(String make, String model, String variant);

    /**
     * Check if a car model exists with given make, model, and variant.
     */
    boolean existsByMakeAndModelAndVariant(String make, String model, String variant);

    /**
     * Find all car models by make.
     */
    List<CarModel> findByMake(String make);

    /**
     * Find all car models by fuel type.
     */
    List<CarModel> findByFuelType(FuelType fuelType);

    /**
     * Find all car models by body type.
     */
    List<CarModel> findByBodyType(String bodyType);

    /**
     * Get all unique makes.
     */
    @Query("SELECT DISTINCT cm.make FROM CarModel cm ORDER BY cm.make")
    List<String> findDistinctMakes();

    /**
     * Get all models for a specific make.
     */
    @Query("SELECT DISTINCT cm.model FROM CarModel cm WHERE cm.make = :make ORDER BY cm.model")
    List<String> findDistinctModelsByMake(@Param("make") String make);

    /**
     * Get all variants for a specific make and model.
     */
    @Query("SELECT DISTINCT cm.variant FROM CarModel cm WHERE cm.make = :make AND cm.model = :model ORDER BY cm.variant")
    List<String> findDistinctVariantsByMakeAndModel(@Param("make") String make, @Param("model") String model);

    /**
     * Search car models by criteria.
     */
    @Query("SELECT cm FROM CarModel cm WHERE " +
           "(:make IS NULL OR cm.make LIKE %:make%) AND " +
           "(:model IS NULL OR cm.model LIKE %:model%) AND " +
           "(:fuelType IS NULL OR cm.fuelType = :fuelType) AND " +
           "(:bodyType IS NULL OR cm.bodyType LIKE %:bodyType%)")
    Page<CarModel> searchCarModels(
            @Param("make") String make,
            @Param("model") String model,
            @Param("fuelType") FuelType fuelType,
            @Param("bodyType") String bodyType,
            Pageable pageable
    );
}

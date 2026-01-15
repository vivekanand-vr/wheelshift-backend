package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Motorcycle;
import com.wheelshiftpro.enums.MotorcycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Motorcycle entity.
 * Provides data access methods for motorcycle inventory management.
 */
@Repository
public interface MotorcycleRepository extends JpaRepository<Motorcycle, Long>, 
                                              JpaSpecificationExecutor<Motorcycle> {

    /**
     * Find motorcycle by VIN number
     */
    Optional<Motorcycle> findByVinNumber(String vinNumber);

    /**
     * Find motorcycle by registration number
     */
    Optional<Motorcycle> findByRegistrationNumber(String registrationNumber);

    /**
     * Find all motorcycles by status
     */
    List<Motorcycle> findByStatus(MotorcycleStatus status);

    /**
     * Find motorcycles by status with pagination
     */
    Page<Motorcycle> findByStatus(MotorcycleStatus status, Pageable pageable);

    /**
     * Find all motorcycles at a storage location
     */
    List<Motorcycle> findByStorageLocationId(Long storageLocationId);

    /**
     * Find motorcycles by model
     */
    List<Motorcycle> findByMotorcycleModelId(Long motorcycleModelId);

    /**
     * Find motorcycles by make (through model relationship)
     */
    @Query("SELECT m FROM Motorcycle m WHERE m.motorcycleModel.make = :make")
    List<Motorcycle> findByMake(@Param("make") String make);

    /**
     * Find motorcycles by price range
     */
    @Query("SELECT m FROM Motorcycle m WHERE m.sellingPrice BETWEEN :minPrice AND :maxPrice")
    Page<Motorcycle> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                       @Param("maxPrice") BigDecimal maxPrice, 
                                       Pageable pageable);

    /**
     * Find motorcycles by mileage range
     */
    @Query("SELECT m FROM Motorcycle m WHERE m.mileageKm BETWEEN :minMileage AND :maxMileage")
    List<Motorcycle> findByMileageRange(@Param("minMileage") Integer minMileage, 
                                         @Param("maxMileage") Integer maxMileage);

    /**
     * Find motorcycles with expired insurance
     */
    @Query("SELECT m FROM Motorcycle m WHERE m.insuranceExpiryDate < :currentDate")
    List<Motorcycle> findMotorcyclesWithExpiredInsurance(@Param("currentDate") LocalDate currentDate);

    /**
     * Find motorcycles purchased within date range
     */
    List<Motorcycle> findByPurchaseDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Count motorcycles by status
     */
    Long countByStatus(MotorcycleStatus status);

    /**
     * Count available motorcycles
     */
    @Query("SELECT COUNT(m) FROM Motorcycle m WHERE m.status = 'AVAILABLE'")
    Long countAvailableMotorcycles();

    /**
     * Search motorcycles by various fields
     */
    @Query("SELECT m FROM Motorcycle m WHERE " +
           "LOWER(m.vinNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.registrationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.motorcycleModel.make) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.motorcycleModel.model) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Motorcycle> searchMotorcycles(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find motorcycles needing attention (maintenance, inspection pending, etc.)
     */
    @Query("SELECT m FROM Motorcycle m WHERE m.status IN ('MAINTENANCE', 'INSPECTION_PENDING', 'DAMAGED')")
    List<Motorcycle> findMotorcyclesNeedingAttention();

    /**
     * Check if VIN number exists
     */
    boolean existsByVinNumber(String vinNumber);

    /**
     * Check if registration number exists
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Get total inventory value
     */
    @Query("SELECT SUM(m.purchasePrice) FROM Motorcycle m WHERE m.status != 'SOLD'")
    BigDecimal getTotalInventoryValue();

    /**
     * Get average selling price
     */
    @Query("SELECT AVG(m.sellingPrice) FROM Motorcycle m WHERE m.sellingPrice IS NOT NULL")
    BigDecimal getAverageSellingPrice();

    /**
     * Find recently added motorcycles
     */
    @Query("SELECT m FROM Motorcycle m ORDER BY m.createdAt DESC")
    Page<Motorcycle> findRecentlyAdded(Pageable pageable);
}

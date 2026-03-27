package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.enums.CarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Car entity.
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    /**
     * Find car by VIN number.
     */
    Optional<Car> findByVinNumber(String vinNumber);

    /**
     * Find car by registration number.
     */
    Optional<Car> findByRegistrationNumber(String registrationNumber);

    /**
     * Check if car exists by VIN.
     */
    boolean existsByVinNumber(String vinNumber);

    /**
     * Check if another car (different id) already uses this VIN.
     */
    boolean existsByVinNumberAndIdNot(String vinNumber, Long id);

    /**
     * Check if car exists by registration number.
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Check if another car (different id) already uses this registration number.
     */
    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long id);

    /**
     * Find all cars by status.
     */
    Page<Car> findByStatus(CarStatus status, Pageable pageable);

    /**
     * Find all cars by storage location.
     */
    Page<Car> findByStorageLocationId(Long locationId, Pageable pageable);

    /**
     * Find all cars by car model.
     */
    Page<Car> findByCarModelId(Long carModelId, Pageable pageable);

    /**
     * Find cars by make.
     */
    @Query("SELECT c FROM Car c WHERE c.carModel.make = :make")
    Page<Car> findByMake(@Param("make") String make, Pageable pageable);

    /**
     * Find cars by model.
     */
    @Query("SELECT c FROM Car c WHERE c.carModel.model = :model")
    Page<Car> findByModel(@Param("model") String model, Pageable pageable);

    /**
     * Find cars by fuel type.
     */
    @Query("SELECT c FROM Car c WHERE c.carModel.fuelType = :fuelType")
    Page<Car> findByFuelType(@Param("fuelType") String fuelType, Pageable pageable);

    /**
     * Find cars by body type.
     */
    @Query("SELECT c FROM Car c WHERE c.carModel.bodyType = :bodyType")
    Page<Car> findByBodyType(@Param("bodyType") String bodyType, Pageable pageable);

    /**
     * Complex search with multiple filters.
     */
    @Query("SELECT c FROM Car c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:locationId IS NULL OR c.storageLocation.id = :locationId) AND " +
           "(:make IS NULL OR c.carModel.make LIKE %:make%) AND " +
           "(:model IS NULL OR c.carModel.model LIKE %:model%) AND " +
           "(:minYear IS NULL OR c.year >= :minYear) AND " +
           "(:maxYear IS NULL OR c.year <= :maxYear) AND " +
           "(:minPrice IS NULL OR c.sellingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR c.sellingPrice <= :maxPrice)")
    Page<Car> searchCars(
            @Param("status") CarStatus status,
            @Param("locationId") Long locationId,
            @Param("make") String make,
            @Param("model") String model,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Count cars by status.
     */
    long countByStatus(CarStatus status);

    /**
     * Get statistics.
     */
    @Query("SELECT c.status as status, COUNT(c) as count FROM Car c GROUP BY c.status")
    List<Object[]> getStatusStatistics();

    /**
     * Calculate total inventory value.
     */
    @Query("SELECT SUM(c.purchasePrice) FROM Car c WHERE c.status IN ('AVAILABLE', 'RESERVED')")
    BigDecimal calculateTotalInventoryValue();

    /**
     * Find all cars by status (non-paginated).
     */
    List<Car> findByStatus(CarStatus status);

    /**
     * Find cars by status and purchase date before (for aging inventory).
     */
    List<Car> findByStatusAndPurchaseDateBefore(CarStatus status, java.time.LocalDate date, Pageable pageable);
}

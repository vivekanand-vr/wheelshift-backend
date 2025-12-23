package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Sale entity.
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    /**
     * Find sale by car.
     */
    Optional<Sale> findByCarId(Long carId);

    /**
     * Check if sale exists for a car.
     */
    boolean existsByCarId(Long carId);

    /**
     * Find sales by client.
     */
    Page<Sale> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find sales by employee.
     */
    Page<Sale> findByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Find sales within date range.
     */
    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    Page<Sale> findBySaleDateBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate, 
                                     Pageable pageable);

    /**
     * Calculate total sales revenue within date range.
     */
    @Query("SELECT SUM(s.salePrice) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total commission within date range.
     */
    @Query("SELECT SUM(s.totalCommission) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalCommission(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Get sales count by employee.
     */
    @Query("SELECT s.employee.id as employeeId, s.employee.name as employeeName, COUNT(s) as salesCount, SUM(s.salePrice) as totalRevenue " +
           "FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate GROUP BY s.employee.id, s.employee.name")
    List<Object[]> getEmployeeSalesPerformance(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Count sales by employee.
     */
    long countByEmployeeId(Long employeeId);

    /**
     * Count total sales.
     */
    @Query("SELECT COUNT(s) FROM Sale s")
    long countTotalSales();

    /**
     * Find sales by employee and date after.
     */
    List<Sale> findByEmployeeIdAndSaleDateAfter(Long employeeId, LocalDateTime date);
}

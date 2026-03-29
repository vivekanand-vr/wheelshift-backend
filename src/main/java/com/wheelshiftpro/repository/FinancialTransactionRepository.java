package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.FinancialTransaction;
import com.wheelshiftpro.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for FinancialTransaction entity.
 */
@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long>, JpaSpecificationExecutor<FinancialTransaction> {

    /**
     * Check if any transaction is linked to the given car.
     */
    boolean existsByCarId(Long carId);

    /**
     * Check if any transaction is linked to the given motorcycle.
     */
    boolean existsByMotorcycleId(Long motorcycleId);

    /**
     * Check if any transaction is linked to the given sale (via car).
     */
    @Query("SELECT CASE WHEN COUNT(ft) > 0 THEN true ELSE false END FROM FinancialTransaction ft WHERE ft.car.id = :carId")
    boolean existsBySaleCarId(@Param("carId") Long carId);

    /**
     * Find transactions by car.
     */
    Page<FinancialTransaction> findByCarId(Long carId, Pageable pageable);

    /**
     * Find transactions by type.
     */
    Page<FinancialTransaction> findByTransactionType(TransactionType type, Pageable pageable);

    /**
     * Find transactions by car and type.
     */
    List<FinancialTransaction> findByCarIdAndTransactionType(Long carId, TransactionType type);

    /**
     * Find transactions within date range.
     */
    @Query("SELECT ft FROM FinancialTransaction ft WHERE ft.transactionDate BETWEEN :startDate AND :endDate")
    Page<FinancialTransaction> findByTransactionDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                              @Param("endDate") LocalDateTime endDate, 
                                                              Pageable pageable);

    /**
     * Calculate total transactions amount by type for a car.
     */
    @Query("SELECT SUM(ft.amount) FROM FinancialTransaction ft WHERE ft.car.id = :carId AND ft.transactionType = :type")
    BigDecimal sumAmountByCarIdAndType(@Param("carId") Long carId, @Param("type") TransactionType type);

    /**
     * Calculate total transactions by type within date range.
     */
    @Query("SELECT SUM(ft.amount) FROM FinancialTransaction ft WHERE ft.transactionType = :type AND ft.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateRange(@Param("type") TransactionType type, 
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Get transaction summary for a car.
     */
    @Query("SELECT ft.transactionType as type, SUM(ft.amount) as total FROM FinancialTransaction ft WHERE ft.car.id = :carId GROUP BY ft.transactionType")
    List<Object[]> getTransactionSummaryByCarId(@Param("carId") Long carId);

    /**
     * Get monthly transaction summary.
     */
    @Query("SELECT ft.transactionType as type, SUM(ft.amount) as total FROM FinancialTransaction ft WHERE ft.transactionDate BETWEEN :startDate AND :endDate GROUP BY ft.transactionType")
    List<Object[]> getTransactionSummaryByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
}

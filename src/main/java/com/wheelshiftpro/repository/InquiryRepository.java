package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Inquiry;
import com.wheelshiftpro.enums.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Inquiry entity.
 */
@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long>, JpaSpecificationExecutor<Inquiry> {

    /**
     * Find inquiries by status.
     */
    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);

    /**
     * Find inquiries by car.
     */
    Page<Inquiry> findByCarId(Long carId, Pageable pageable);

    /**
     * Find inquiries by client.
     */
    Page<Inquiry> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find inquiries assigned to an employee.
     */
    Page<Inquiry> findByAssignedEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Find inquiries by status and assigned employee.
     */
    Page<Inquiry> findByStatusAndAssignedEmployeeId(InquiryStatus status, Long employeeId, Pageable pageable);

    /**
     * Find inquiries created within date range.
     */
    @Query("SELECT i FROM Inquiry i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    Page<Inquiry> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate, 
                                         Pageable pageable);

    /**
     * Count inquiries by status.
     */
    long countByStatus(InquiryStatus status);

    /**
     * Count inquiries by assigned employee.
     */
    long countByAssignedEmployeeId(Long employeeId);

    /**
     * Get inquiry statistics.
     */
    @Query("SELECT i.status as status, COUNT(i) as count FROM Inquiry i GROUP BY i.status")
    List<Object[]> getInquiryStatistics();
}

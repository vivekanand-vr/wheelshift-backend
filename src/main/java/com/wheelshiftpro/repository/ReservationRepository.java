package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Reservation;
import com.wheelshiftpro.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Reservation entity.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find reservation by car.
     */
    Optional<Reservation> findByCarId(Long carId);

    /**
     * Check if reservation exists for a car.
     */
    boolean existsByCarId(Long carId);

    /**
     * Find reservations by client.
     */
    Page<Reservation> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find reservations by status.
     */
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    /**
     * Find active reservations (pending or confirmed).
     */
    @Query("SELECT r FROM Reservation r WHERE r.status IN ('PENDING', 'CONFIRMED')")
    Page<Reservation> findActiveReservations(Pageable pageable);

    /**
     * Find expired reservations that need status update.
     */
    @Query("SELECT r FROM Reservation r WHERE r.expiryDate < :currentTime AND r.status NOT IN ('EXPIRED', 'CANCELLED')")
    List<Reservation> findExpiredReservations(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find reservations expiring soon (for notifications).
     */
    @Query("SELECT r FROM Reservation r WHERE r.expiryDate BETWEEN :startTime AND :endTime AND r.status = 'CONFIRMED'")
    List<Reservation> findReservationsExpiringSoon(@Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * Count reservations by status.
     */
    long countByStatus(ReservationStatus status);
}

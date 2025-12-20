package com.wheelshiftpro.repository;

import com.wheelshiftpro.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Event entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find events by type.
     */
    Page<Event> findByType(String type, Pageable pageable);

    /**
     * Find events by car.
     */
    Page<Event> findByCarId(Long carId, Pageable pageable);

    /**
     * Find events within date range.
     */
    @Query("SELECT e FROM Event e WHERE e.startTime BETWEEN :startTime AND :endTime ORDER BY e.startTime")
    List<Event> findEventsBetween(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * Find upcoming events.
     */
    @Query("SELECT e FROM Event e WHERE e.startTime >= :currentTime ORDER BY e.startTime")
    List<Event> findUpcomingEvents(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * Find events by type within date range.
     */
    @Query("SELECT e FROM Event e WHERE e.type = :type AND e.startTime BETWEEN :startTime AND :endTime ORDER BY e.startTime")
    List<Event> findByTypeAndDateRange(@Param("type") String type, 
                                       @Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * Find events by title containing (case insensitive).
     */
    Page<Event> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}

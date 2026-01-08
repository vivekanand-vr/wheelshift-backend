package com.wheelshiftpro.repository.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wheelshiftpro.entity.notifications.NotificationEvent;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    
    Page<NotificationEvent> findByEventTypeOrderByOccurredAtDesc(String eventType, Pageable pageable);
    
    Page<NotificationEvent> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
            String entityType, Long entityId, Pageable pageable);
    
    List<NotificationEvent> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);
    
    Page<NotificationEvent> findAllByOrderByOccurredAtDesc(Pageable pageable);
}

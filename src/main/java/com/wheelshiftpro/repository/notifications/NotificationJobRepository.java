package com.wheelshiftpro.repository.notifications;

import com.wheelshiftpro.entity.notifications.NotificationJob;
import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.enums.notifications.NotificationStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationJobRepository extends JpaRepository<NotificationJob, Long> {
    
    Optional<NotificationJob> findByDedupKey(String dedupKey);
    
    Page<NotificationJob> findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(
            RecipientType recipientType, Long recipientId, Pageable pageable);
    
    Page<NotificationJob> findByRecipientTypeAndRecipientIdAndChannelOrderByCreatedAtDesc(
            RecipientType recipientType, Long recipientId, NotificationChannel channel, Pageable pageable);
    
    List<NotificationJob> findByStatusAndScheduledForBefore(
            NotificationStatus status, LocalDateTime scheduledFor);
    
    @Query("SELECT j FROM NotificationJob j WHERE j.recipientType = :recipientType " +
           "AND j.recipientId = :recipientId AND j.channel = :channel " +
           "AND j.status IN :statuses ORDER BY j.createdAt DESC")
    Page<NotificationJob> findByRecipientAndChannelAndStatusIn(
            @Param("recipientType") RecipientType recipientType,
            @Param("recipientId") Long recipientId,
            @Param("channel") NotificationChannel channel,
            @Param("statuses") List<NotificationStatus> statuses,
            Pageable pageable);
    
    Long countByRecipientTypeAndRecipientIdAndStatus(
            RecipientType recipientType, Long recipientId, NotificationStatus status);
    
    @Query("SELECT COUNT(j) FROM NotificationJob j WHERE j.recipientType = :recipientType " +
           "AND j.recipientId = :recipientId AND j.channel = :channel AND j.sentAt IS NULL")
    Long countUnreadNotifications(
            @Param("recipientType") RecipientType recipientType,
            @Param("recipientId") Long recipientId,
            @Param("channel") NotificationChannel channel);
}

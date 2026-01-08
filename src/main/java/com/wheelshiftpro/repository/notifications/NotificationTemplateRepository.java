package com.wheelshiftpro.repository.notifications;

import com.wheelshiftpro.entity.notifications.NotificationTemplate;
import com.wheelshiftpro.enums.notifications.NotificationChannel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    
    Optional<NotificationTemplate> findByNameAndChannelAndLocale(
            String name, NotificationChannel channel, String locale);
    
    @Query("SELECT t FROM NotificationTemplate t WHERE t.name = :name " +
           "AND t.channel = :channel AND t.locale = :locale " +
           "ORDER BY t.version DESC LIMIT 1")
    Optional<NotificationTemplate> findLatestByNameAndChannelAndLocale(
            @Param("name") String name,
            @Param("channel") NotificationChannel channel,
            @Param("locale") String locale);
    
    Page<NotificationTemplate> findByChannelOrderByNameAsc(NotificationChannel channel, Pageable pageable);
    
    Page<NotificationTemplate> findAllByOrderByNameAsc(Pageable pageable);
}

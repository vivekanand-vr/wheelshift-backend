package com.wheelshiftpro.repository.notifications;

import com.wheelshiftpro.entity.notifications.NotificationPreference;
import com.wheelshiftpro.enums.PrincipalType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    Optional<NotificationPreference> findByPrincipalTypeAndPrincipalIdAndEventTypeAndChannel(
            PrincipalType principalType, Long principalId, String eventType, NotificationChannel channel);
    
    List<NotificationPreference> findByPrincipalTypeAndPrincipalId(
            PrincipalType principalType, Long principalId);
    
    @Query("SELECT p FROM NotificationPreference p WHERE " +
           "(p.principalType = :principalType AND p.principalId = :principalId) OR " +
           "(p.principalType = 'COMPANY' AND p.principalId IS NULL) " +
           "ORDER BY p.principalType DESC")
    List<NotificationPreference> findApplicablePreferences(
            @Param("principalType") PrincipalType principalType,
            @Param("principalId") Long principalId);
    
    Page<NotificationPreference> findByPrincipalTypeAndPrincipalId(
            PrincipalType principalType, Long principalId, Pageable pageable);
    
    Page<NotificationPreference> findAllByOrderByPrincipalTypeAsc(Pageable pageable);
}

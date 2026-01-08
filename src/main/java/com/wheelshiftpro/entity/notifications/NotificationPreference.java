package com.wheelshiftpro.entity.notifications;

import com.wheelshiftpro.enums.PrincipalType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.enums.notifications.NotificationFrequency;
import com.wheelshiftpro.enums.notifications.NotificationSeverity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "principal_type", nullable = false)
    private PrincipalType principalType;
    
    @Column(name = "principal_id")
    private Long principalId;
    
    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;
    
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    @Builder.Default
    private NotificationFrequency frequency = NotificationFrequency.IMMEDIATE;
    
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;
    
    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity_threshold")
    @Builder.Default
    private NotificationSeverity severityThreshold = NotificationSeverity.INFO;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

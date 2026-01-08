package com.wheelshiftpro.dto.response.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {
    
    private Long totalNotifications;
    private Long unreadNotifications;
    private Long pendingNotifications;
    private Long sentNotifications;
    private Long failedNotifications;
}

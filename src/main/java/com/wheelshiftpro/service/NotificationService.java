package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.CreateNotificationEventRequest;
import com.wheelshiftpro.dto.request.CreateNotificationJobRequest;
import com.wheelshiftpro.dto.response.NotificationEventResponse;
import com.wheelshiftpro.dto.response.NotificationJobResponse;
import com.wheelshiftpro.dto.response.NotificationStatsResponse;
import com.wheelshiftpro.enums.NotificationChannel;
import com.wheelshiftpro.enums.RecipientType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface NotificationService {
    
    /**
     * Create a notification event and automatically generate jobs for applicable recipients
     */
    NotificationEventResponse createNotificationEvent(CreateNotificationEventRequest request);
    
    /**
     * Create a specific notification job
     */
    NotificationJobResponse createNotificationJob(CreateNotificationJobRequest request);
    
    /**
     * Get notifications for a specific recipient
     */
    Page<NotificationJobResponse> getNotificationsForRecipient(
            RecipientType recipientType, 
            Long recipientId, 
            NotificationChannel channel,
            Pageable pageable);
    
    /**
     * Get a specific notification job by ID
     */
    NotificationJobResponse getNotificationById(Long jobId);
    
    /**
     * Mark a notification as read (sent)
     */
    NotificationJobResponse markNotificationAsRead(Long jobId);
    
    /**
     * Mark all notifications as read for a recipient
     */
    void markAllNotificationsAsRead(RecipientType recipientType, Long recipientId, NotificationChannel channel);
    
    /**
     * Get notification statistics for a recipient
     */
    NotificationStatsResponse getNotificationStats(RecipientType recipientType, Long recipientId);
    
    /**
     * Get unread notification count
     */
    Long getUnreadCount(RecipientType recipientType, Long recipientId, NotificationChannel channel);
    
    /**
     * Process pending scheduled notifications
     */
    void processScheduledNotifications();
    
    /**
     * Get all notification events
     */
    Page<NotificationEventResponse> getAllEvents(Pageable pageable);
}

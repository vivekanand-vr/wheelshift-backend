package com.wheelshiftpro.controller.notifications;

import com.wheelshiftpro.dto.request.notifications.CreateNotificationEventRequest;
import com.wheelshiftpro.dto.request.notifications.CreateNotificationJobRequest;
import com.wheelshiftpro.dto.response.notifications.NotificationEventResponse;
import com.wheelshiftpro.dto.response.notifications.NotificationJobResponse;
import com.wheelshiftpro.dto.response.notifications.NotificationStatsResponse;
import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.notification.NotificationSseEmitterManager;
import com.wheelshiftpro.service.notifications.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final NotificationSseEmitterManager sseEmitterManager;
    
    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Create a notification event", description = "Creates a new notification event and generates jobs for recipients")
    public ResponseEntity<NotificationEventResponse> createEvent(
            @Valid @RequestBody CreateNotificationEventRequest request) {
        NotificationEventResponse response = notificationService.createNotificationEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create a notification job", description = "Creates a specific notification job for a recipient")
    public ResponseEntity<NotificationJobResponse> createJob(
            @Valid @RequestBody CreateNotificationJobRequest request) {
        NotificationJobResponse response = notificationService.createNotificationJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/recipient/{recipientType}/{recipientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get notifications for recipient", description = "Retrieves notifications for a specific recipient")
    public ResponseEntity<Page<NotificationJobResponse>> getNotificationsForRecipient(
            @PathVariable RecipientType recipientType,
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "IN_APP") NotificationChannel channel,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationJobResponse> notifications = notificationService.getNotificationsForRecipient(
                recipientType, recipientId, channel, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get notification by ID", description = "Retrieves a specific notification by ID")
    public ResponseEntity<NotificationJobResponse> getNotificationById(@PathVariable Long id) {
        NotificationJobResponse notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }
    
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Mark notification as read", description = "Marks a notification as read")
    public ResponseEntity<NotificationJobResponse> markAsRead(@PathVariable Long id) {
        NotificationJobResponse notification = notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok(notification);
    }
    
    @PutMapping("/recipient/{recipientType}/{recipientId}/read-all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications as read for a recipient")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable RecipientType recipientType,
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "IN_APP") NotificationChannel channel) {
        notificationService.markAllNotificationsAsRead(recipientType, recipientId, channel);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/recipient/{recipientType}/{recipientId}/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get notification statistics", description = "Retrieves notification statistics for a recipient")
    public ResponseEntity<NotificationStatsResponse> getStats(
            @PathVariable RecipientType recipientType,
            @PathVariable Long recipientId) {
        NotificationStatsResponse stats = notificationService.getNotificationStats(recipientType, recipientId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/recipient/{recipientType}/{recipientId}/unread-count")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get unread notification count", description = "Retrieves unread notification count for a recipient")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable RecipientType recipientType,
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "IN_APP") NotificationChannel channel) {
        Long count = notificationService.getUnreadCount(recipientType, recipientId, channel);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get all notification events", description = "Retrieves all notification events")
    public ResponseEntity<Page<NotificationEventResponse>> getAllEvents(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationEventResponse> events = notificationService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * SSE stream — subscribe to real-time notifications for a recipient.
     * The connection stays open; the server pushes a {@code notification} event
     * each time a new notification arrives via the Kafka → Redis → SSE pipeline.
     *
     * <p>Usage (JavaScript):
     * <pre>
     * const es = new EventSource('/api/v1/notifications/stream/EMPLOYEE/42');
     * es.addEventListener('notification', e => console.log(JSON.parse(e.data)));
     * </pre>
     */
    @GetMapping(value = "/stream/{recipientType}/{recipientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Subscribe to real-time notifications",
               description = "Opens a Server-Sent Events stream that pushes new notifications as they arrive")
    public SseEmitter streamNotifications(
            @PathVariable RecipientType recipientType,
            @PathVariable Long recipientId) {
        return sseEmitterManager.addEmitter(recipientType, recipientId);
    }
}

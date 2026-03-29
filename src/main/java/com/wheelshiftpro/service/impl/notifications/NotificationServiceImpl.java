package com.wheelshiftpro.service.impl.notifications;

import com.wheelshiftpro.dto.request.notifications.CreateNotificationEventRequest;
import com.wheelshiftpro.dto.request.notifications.CreateNotificationJobRequest;
import com.wheelshiftpro.dto.response.notifications.NotificationEventResponse;
import com.wheelshiftpro.dto.response.notifications.NotificationJobResponse;
import com.wheelshiftpro.dto.response.notifications.NotificationStatsResponse;
import com.wheelshiftpro.entity.notifications.NotificationEvent;
import com.wheelshiftpro.entity.notifications.NotificationJob;
import com.wheelshiftpro.entity.notifications.NotificationPreference;
import com.wheelshiftpro.entity.notifications.NotificationTemplate;
import com.wheelshiftpro.enums.PrincipalType;
import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.enums.notifications.NotificationFrequency;
import com.wheelshiftpro.enums.notifications.NotificationStatus;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.messaging.NotificationKafkaProducer;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.notifications.NotificationEventRepository;
import com.wheelshiftpro.repository.notifications.NotificationJobRepository;
import com.wheelshiftpro.repository.notifications.NotificationPreferenceRepository;
import com.wheelshiftpro.repository.notifications.NotificationTemplateRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.notifications.NotificationService;
import com.wheelshiftpro.service.notifications.NotificationTemplateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationEventRepository eventRepository;
    private final NotificationJobRepository jobRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateService templateService;
    private final NotificationKafkaProducer kafkaProducer;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationEventResponse createNotificationEvent(CreateNotificationEventRequest request) {
        log.info("Creating notification event: type={}, entityType={}, entityId={}", 
                request.getEventType(), request.getEntityType(), request.getEntityId());
        
        NotificationEvent event = NotificationEvent.builder()
                .eventType(request.getEventType())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .payload(request.getPayload())
                .severity(request.getSeverity())
                .occurredAt(request.getOccurredAt())
                .createdAt(LocalDateTime.now())
                .build();
        
        event = eventRepository.save(event);
        
        // Auto-generate jobs for IN_APP channel based on event payload
        List<NotificationJob> jobs = generateNotificationJobs(event);

        // Publish each persisted job to Kafka (async, non-blocking for the caller)
        jobs.forEach(kafkaProducer::publishJob);

        auditService.log(AuditCategory.SYSTEM, event.getId(), "CREATE_NOTIFICATION_EVENT",
                AuditLevel.REGULAR, resolveCurrentEmployee(),
                "Type: " + event.getEventType() + ", entity: " + event.getEntityType() + "/" + event.getEntityId());

        return mapToEventResponse(event);
    }
    
    private List<NotificationJob> generateNotificationJobs(NotificationEvent event) {
        // Extract recipient info from payload if available
        Object recipientIdObj = event.getPayload().get("recipientId");
        Object recipientTypeObj = event.getPayload().get("recipientType");
        
        if (recipientIdObj != null && recipientTypeObj != null) {
            try {
                Long recipientId = Long.parseLong(recipientIdObj.toString());
                RecipientType recipientType = RecipientType.valueOf(recipientTypeObj.toString());
                
                // Create IN_APP notification job with preference checks
                return createJobForRecipient(event, recipientType, recipientId, NotificationChannel.IN_APP)
                        .map(List::of).orElse(List.of());
            } catch (Exception e) {
                log.warn("Failed to parse recipient info from event payload: {}", e.getMessage());
            }
        }
        return List.of();
    }
    
    private Optional<NotificationJob> createJobForRecipient(NotificationEvent event, RecipientType recipientType,
                                      Long recipientId, NotificationChannel channel) {
        // 1. CHECK OPT-OUT & FETCH PREFERENCE
        NotificationPreference preference = preferenceRepository
            .findByPrincipalTypeAndPrincipalIdAndEventTypeAndChannel(
                toPrincipalType(recipientType), 
                recipientId,
                event.getEventType(),
                channel
            )
            .or(() -> preferenceRepository.findByPrincipalTypeAndPrincipalIdAndEventTypeAndChannel(
                toPrincipalType(recipientType), 
                recipientId,
                com.wheelshiftpro.enums.notifications.NotificationEventType.ALL,
                channel
            ))
            .orElse(null);
        
        // Skip if explicitly disabled
        if (preference != null && Boolean.FALSE.equals(preference.getEnabled())) {
            log.debug("Channel {} disabled for recipient {}:{}", 
                channel, recipientType, recipientId);
            return Optional.empty();
        }
        
        // 2. CHECK SEVERITY THRESHOLD
        if (preference != null && preference.getSeverityThreshold() != null) {
            if (event.getSeverity().ordinal() < preference.getSeverityThreshold().ordinal()) {
                log.debug("Event severity {} below threshold {} for recipient {}:{}",
                    event.getSeverity(), preference.getSeverityThreshold(),
                    recipientType, recipientId);
                return Optional.empty();
            }
        }
        
        // 3. HANDLE DIGEST FREQUENCY
        LocalDateTime scheduledFor = null;
        NotificationStatus initialStatus = NotificationStatus.PENDING;
        if (preference != null && preference.getFrequency() == NotificationFrequency.DIGEST) {
            scheduledFor = calculateNextDigestTime();
            initialStatus = NotificationStatus.SCHEDULED;
            log.debug("Digest mode: scheduling notification for {}", scheduledFor);
        }
        
        // 4. BUILD DEDUP KEY
        String dedupKey = generateDedupKey(event.getId(), recipientType, recipientId, channel);
        
        // 5. CHECK DEDUP
        if (jobRepository.existsByDedupKey(dedupKey)) {
            log.debug("Notification job already exists with dedupKey: {}", dedupKey);
            return Optional.empty();
        }
        
        // 6. CREATE JOB
        NotificationJob job = NotificationJob.builder()
                .event(event)
                .recipientType(recipientType)
                .recipientId(recipientId)
                .channel(channel)
                .status(initialStatus)
                .scheduledFor(scheduledFor)
                .dedupKey(dedupKey)
                .retries(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        jobRepository.save(job);
        log.debug("Created notification job: id={}, status={}, scheduledFor={}", 
            job.getId(), initialStatus, scheduledFor);
        return Optional.of(job);
    }
    
    private String generateDedupKey(Long eventId, RecipientType recipientType,
                                    Long recipientId, NotificationChannel channel) {
        return String.format("%d-%s-%d-%s", eventId, recipientType, recipientId, channel);
    }
    
    private PrincipalType toPrincipalType(RecipientType recipientType) {
        return switch (recipientType) {
            case EMPLOYEE -> PrincipalType.EMPLOYEE;
            case CLIENT -> PrincipalType.CLIENT;
            case ROLE -> PrincipalType.ROLE;
        };
    }
    
    private LocalDateTime calculateNextDigestTime() {
        // Default: next 9 AM
        LocalDateTime now = LocalDateTime.now();
        LocalTime digestTime = LocalTime.of(9, 0);
        LocalDateTime next9AM = now.toLocalDate().atTime(digestTime);
        if (now.toLocalTime().isAfter(digestTime)) {
            next9AM = next9AM.plusDays(1);
        }
        return next9AM;
    }

    private void validateNotificationOwnership(NotificationJob job) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof EmployeeUserDetails u)) return;
        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdminOrSuperAdmin && !Objects.equals(job.getRecipientId(), u.getId())) {
            throw new BusinessException(
                    "You can only manage your own notifications", "NOT_NOTIFICATION_RECIPIENT");
        }
    }

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationJobResponse createNotificationJob(CreateNotificationJobRequest request) {
        log.info("Creating notification job: eventId={}, recipientType={}, recipientId={}, channel={}",
                request.getEventId(), request.getRecipientType(), request.getRecipientId(), request.getChannel());

        NotificationEvent event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("NotificationEvent", "id", request.getEventId()));

        String dedupKey = generateDedupKey(request.getEventId(), request.getRecipientType(),
                request.getRecipientId(), request.getChannel());

        // Return existing job unchanged (idempotent)
        Optional<NotificationJob> existing = jobRepository.findByDedupKey(dedupKey);
        if (existing.isPresent()) {
            return mapToJobResponse(existing.get());
        }

        NotificationJob job = NotificationJob.builder()
                .event(event)
                .recipientType(request.getRecipientType())
                .recipientId(request.getRecipientId())
                .channel(request.getChannel())
                .status(NotificationStatus.PENDING)
                .scheduledFor(request.getScheduledFor())
                .dedupKey(dedupKey)
                .retries(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        job = jobRepository.save(job);

        auditService.log(AuditCategory.SYSTEM, job.getId(), "CREATE_NOTIFICATION_JOB",
                AuditLevel.REGULAR, resolveCurrentEmployee(), "dedupKey: " + dedupKey);

        return mapToJobResponse(job);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationJobResponse> getNotificationsForRecipient(
            RecipientType recipientType, Long recipientId, NotificationChannel channel, Pageable pageable) {
        
        Page<NotificationJob> jobs = jobRepository.findByRecipientTypeAndRecipientIdAndChannelOrderByCreatedAtDesc(
                recipientType, recipientId, channel, pageable);
        
        return jobs.map(this::mapToJobResponseWithDetails);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationJobResponse getNotificationById(Long jobId) {
        NotificationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationJob", "id", jobId));

        return mapToJobResponseWithDetails(job);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationJobResponse markNotificationAsRead(Long jobId) {
        NotificationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationJob", "id", jobId));

        validateNotificationOwnership(job);

        if (job.getSentAt() == null) {
            job.setSentAt(LocalDateTime.now());
            job.setStatus(NotificationStatus.SENT);
            job = jobRepository.save(job);

            auditService.log(AuditCategory.SYSTEM, jobId, "MARK_AS_READ",
                    AuditLevel.REGULAR, resolveCurrentEmployee(),
                    "Notification " + jobId + " marked as read for recipient " + job.getRecipientId());
        }

        return mapToJobResponse(job);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllNotificationsAsRead(RecipientType recipientType, Long recipientId, NotificationChannel channel) {
        List<NotificationStatus> unreadStatuses = List.of(NotificationStatus.PENDING, NotificationStatus.SCHEDULED);
        Page<NotificationJob> jobs = jobRepository.findByRecipientAndChannelAndStatusIn(
                recipientType, recipientId, channel, unreadStatuses, Pageable.unpaged());
        
        LocalDateTime now = LocalDateTime.now();
        jobs.forEach(job -> {
            job.setSentAt(now);
            job.setStatus(NotificationStatus.SENT);
        });
        
        jobRepository.saveAll(jobs.getContent());
        log.info("Marked {} notifications as read for recipient: {}:{}",
                jobs.getTotalElements(), recipientType, recipientId);

        auditService.log(AuditCategory.SYSTEM, recipientId, "MARK_ALL_AS_READ",
                AuditLevel.REGULAR, resolveCurrentEmployee(),
                "All " + jobs.getTotalElements() + " notifications marked as read for recipient " + recipientId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationStatsResponse getNotificationStats(RecipientType recipientType, Long recipientId) {
        Long total = jobRepository.countByRecipientTypeAndRecipientIdAndStatus(
                recipientType, recipientId, null);
        Long pending = jobRepository.countByRecipientTypeAndRecipientIdAndStatus(
                recipientType, recipientId, NotificationStatus.PENDING);
        Long sent = jobRepository.countByRecipientTypeAndRecipientIdAndStatus(
                recipientType, recipientId, NotificationStatus.SENT);
        Long failed = jobRepository.countByRecipientTypeAndRecipientIdAndStatus(
                recipientType, recipientId, NotificationStatus.FAILED);
        Long unread = jobRepository.countUnreadNotifications(
                recipientType, recipientId, NotificationChannel.IN_APP);
        
        return NotificationStatsResponse.builder()
                .totalNotifications(total != null ? total : 0L)
                .unreadNotifications(unread != null ? unread : 0L)
                .pendingNotifications(pending != null ? pending : 0L)
                .sentNotifications(sent != null ? sent : 0L)
                .failedNotifications(failed != null ? failed : 0L)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(RecipientType recipientType, Long recipientId, NotificationChannel channel) {
        Long count = jobRepository.countUnreadNotifications(recipientType, recipientId, channel);
        return count != null ? count : 0L;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationJob> scheduledJobs = jobRepository.findByStatusAndScheduledForBefore(
                NotificationStatus.SCHEDULED, now);
        
        scheduledJobs.forEach(job -> {
            job.setStatus(NotificationStatus.PENDING);
            job.setUpdatedAt(now);
        });
        
        jobRepository.saveAll(scheduledJobs);
        log.info("Processed {} scheduled notifications", scheduledJobs.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationEventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(this::mapToEventResponse);
    }
    
    private NotificationEventResponse mapToEventResponse(NotificationEvent event) {
        return NotificationEventResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .payload(event.getPayload())
                .severity(event.getSeverity())
                .occurredAt(event.getOccurredAt())
                .createdAt(event.getCreatedAt())
                .build();
    }
    
    private NotificationJobResponse mapToJobResponse(NotificationJob job) {
        return NotificationJobResponse.builder()
                .id(job.getId())
                .eventId(job.getEvent().getId())
                .recipientType(job.getRecipientType())
                .recipientId(job.getRecipientId())
                .channel(job.getChannel())
                .status(job.getStatus())
                .scheduledFor(job.getScheduledFor())
                .dedupKey(job.getDedupKey())
                .retries(job.getRetries())
                .lastError(job.getLastError())
                .sentAt(job.getSentAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
    
    private NotificationJobResponse mapToJobResponseWithDetails(NotificationJob job) {
        NotificationEvent event = job.getEvent();
        
        // Render template with event payload
        String title = "";
        String message = "";
        
        try {
            NotificationTemplate template = templateRepository
                    .findLatestByNameAndChannelAndLocale(event.getEventType().getTemplateName(), job.getChannel(), "en")
                    .orElse(null);
            
            if (template != null) {
                title = template.getSubject();
                message = templateService.renderTemplate(template.getContent(), event.getPayload());
            } else {
                // Fallback rendering
                title = event.getEventType().name();
                message = event.getPayload().toString();
            }
        } catch (Exception e) {
            log.warn("Failed to render template for job {}: {}", job.getId(), e.getMessage());
            title = event.getEventType().name();
            message = "Notification available";
        }
        
        return NotificationJobResponse.builder()
                .id(job.getId())
                .eventId(event.getId())
                .recipientType(job.getRecipientType())
                .recipientId(job.getRecipientId())
                .channel(job.getChannel())
                .status(job.getStatus())
                .scheduledFor(job.getScheduledFor())
                .dedupKey(job.getDedupKey())
                .retries(job.getRetries())
                .lastError(job.getLastError())
                .sentAt(job.getSentAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .title(title)
                .message(message)
                .eventType(event.getEventType())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .build();
    }
}

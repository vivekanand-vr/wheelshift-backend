package com.wheelshiftpro.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheelshiftpro.config.KafkaConfig;
import com.wheelshiftpro.entity.notifications.NotificationJob;
import com.wheelshiftpro.entity.notifications.NotificationPreference;
import com.wheelshiftpro.enums.PrincipalType;
import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationStatus;
import com.wheelshiftpro.repository.notifications.NotificationJobRepository;
import com.wheelshiftpro.repository.notifications.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Consumes IN_APP notification jobs from Kafka and fans them out via Redis Pub/Sub
 * so every app instance can push the event to its connected SSE clients.
 *
 * Enforces quiet hours by rescheduling jobs that arrive during the recipient's quiet period.
 *
 * Redis channel convention: {@code notification:{RECIPIENT_TYPE}:{recipientId}}
 * e.g. {@code notification:EMPLOYEE:42}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationJobRepository jobRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    @KafkaListener(
            topics     = KafkaConfig.TOPIC_INAPP,
            groupId    = KafkaConfig.GROUP_INAPP,
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consumeInApp(
            NotificationJobMessage message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET)             long offset,
            Acknowledgment ack) {

        log.debug("Consuming IN_APP job {} from partition {} offset {}", message.getJobId(), partition, offset);

        try {
            // 1. FETCH JOB FROM DB (to get latest status)
            NotificationJob job = jobRepository.findById(message.getJobId())
                .orElseThrow(() -> new IllegalStateException("Job not found: " + message.getJobId()));
            
            // 2. CHECK QUIET HOURS
            NotificationPreference preference = preferenceRepository
                .findByPrincipalTypeAndPrincipalIdAndEventTypeAndChannel(
                    toPrincipalType(job.getRecipientType()),
                    job.getRecipientId(),
                    job.getEvent().getEventType(),
                    job.getChannel()
                )
                .orElse(null);
            
            if (preference != null && isInQuietHours(preference)) {
                // Reschedule for end of quiet hours
                LocalDateTime rescheduleFor = calculateQuietHoursEnd(preference);
                job.setScheduledFor(rescheduleFor);
                job.setStatus(NotificationStatus.SCHEDULED);
                jobRepository.save(job);
                
                log.info("Job {} in quiet hours, rescheduled for {}", 
                    job.getId(), rescheduleFor);
                
                // ACK to avoid redelivery - scheduler will pick it up later
                ack.acknowledge();
                return;
            }
            
            // 3. DELIVER NOTIFICATION
            String channel = redisChannel(message);
            String payload = objectMapper.writeValueAsString(message);
            stringRedisTemplate.convertAndSend(channel, payload);
            log.debug("Published notification job {} to Redis channel {}", message.getJobId(), channel);
            
            // 4. UPDATE JOB STATUS
            job.setStatus(NotificationStatus.SENT);
            job.setSentAt(LocalDateTime.now());
            jobRepository.save(job);
            
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification job {} for Redis: {}", message.getJobId(), e.getMessage());
            // Acknowledge to avoid infinite retry on serialization errors
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to publish notification job {} to Redis: {}", message.getJobId(), e.getMessage());
            // Do NOT acknowledge — Kafka will redeliver
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String redisChannel(NotificationJobMessage msg) {
        return "notification:" + msg.getRecipientType().name() + ":" + msg.getRecipientId();
    }
    
    private PrincipalType toPrincipalType(RecipientType recipientType) {
        return switch (recipientType) {
            case EMPLOYEE -> PrincipalType.EMPLOYEE;
            case CLIENT -> PrincipalType.CLIENT;
            case ROLE -> PrincipalType.ROLE;
        };
    }
    
    private boolean isInQuietHours(NotificationPreference preference) {
        if (preference.getQuietHoursStart() == null || preference.getQuietHoursEnd() == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        LocalTime start = preference.getQuietHoursStart();
        LocalTime end = preference.getQuietHoursEnd();
        
        // Handle overnight quiet hours (e.g., 22:00 - 08:00)
        if (start.isAfter(end)) {
            return now.isAfter(start) || now.isBefore(end);
        } else {
            return now.isAfter(start) && now.isBefore(end);
        }
    }
    
    private LocalDateTime calculateQuietHoursEnd(NotificationPreference preference) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime end = preference.getQuietHoursEnd();
        LocalDate targetDate = now.toLocalDate();
        
        // If quiet hours cross midnight and we're before end time, target is today
        if (preference.getQuietHoursStart().isAfter(end) && now.toLocalTime().isBefore(end)) {
            return targetDate.atTime(end);
        }
        
        // Otherwise, target is end time today or tomorrow
        LocalDateTime endDateTime = targetDate.atTime(end);
        if (endDateTime.isBefore(now)) {
            endDateTime = endDateTime.plusDays(1);
        }
        
        return endDateTime;
    }
}

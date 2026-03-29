package com.wheelshiftpro.scheduler;

import com.wheelshiftpro.entity.notifications.NotificationJob;
import com.wheelshiftpro.enums.RecipientType;
import com.wheelshiftpro.enums.notifications.NotificationChannel;
import com.wheelshiftpro.enums.notifications.NotificationEventType;
import com.wheelshiftpro.enums.notifications.NotificationSeverity;
import com.wheelshiftpro.enums.notifications.NotificationStatus;
import com.wheelshiftpro.messaging.NotificationJobMessage;
import com.wheelshiftpro.messaging.NotificationKafkaProducer;
import com.wheelshiftpro.repository.notifications.NotificationJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scheduled job that runs every hour to pick up digest-scheduled notifications
 * and batch them together for efficient delivery.
 *
 * Groups notifications by recipient + channel and sends a single digest message
 * containing a summary of all pending notifications.
 *
 * Protected by ShedLock to ensure only one node executes in clustered deployments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDigestScheduler {

    private final NotificationJobRepository jobRepository;
    private final NotificationKafkaProducer kafkaProducer;

    @Scheduled(cron = "${notification.digest.cron:0 0 * * * *}")  // Every hour at :00
    @SchedulerLock(name = "NotificationDigestScheduler", lockAtMostFor = "55m", lockAtLeastFor = "5m")
    @Transactional(rollbackFor = Exception.class)
    public void processDigestBatch() {
        log.info("Running digest batch processor");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Find all jobs scheduled for delivery now or earlier
        List<NotificationJob> dueJobs = jobRepository
            .findByStatusAndScheduledForLessThanEqual(
                NotificationStatus.SCHEDULED, 
                now
            );
        
        if (dueJobs.isEmpty()) {
            log.debug("No digest jobs due for delivery");
            return;
        }
        
        // Group by recipient + channel
        Map<String, List<NotificationJob>> grouped = dueJobs.stream()
            .collect(Collectors.groupingBy(job -> 
                job.getRecipientType() + ":" + job.getRecipientId() + ":" + job.getChannel()
            ));
        
        for (Map.Entry<String, List<NotificationJob>> entry : grouped.entrySet()) {
            try {
                sendDigest(entry.getValue());
            } catch (Exception e) {
                log.error("Failed to send digest for {}", entry.getKey(), e);
            }
        }
        
        log.info("Processed {} digest batches", grouped.size());
    }
    
    private void sendDigest(List<NotificationJob> jobs) {
        if (jobs.isEmpty()) return;
        
        NotificationJob firstJob = jobs.get(0);
        
        // Build digest message
        NotificationJobMessage digestMessage = NotificationJobMessage.builder()
            .jobId(firstJob.getId())  // Use first job ID as reference
            .eventType(NotificationEventType.NOTIFICATION_DIGEST)  // Special digest type
            .recipientType(firstJob.getRecipientType())
            .recipientId(firstJob.getRecipientId())
            .channel(firstJob.getChannel())
            .title("You have " + jobs.size() + " new notification" + (jobs.size() > 1 ? "s" : ""))
            .message(buildDigestContent(jobs))
            .payload(Map.of(
                "count", jobs.size(),
                "jobIds", jobs.stream().map(NotificationJob::getId).collect(Collectors.toList())
            ))
            .severity(NotificationSeverity.INFO)
            .occurredAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
        
        // Publish to Kafka
        kafkaProducer.publishJobMessage(digestMessage);
        
        // Mark all jobs as SENT
        jobs.forEach(job -> {
            job.setStatus(NotificationStatus.SENT);
            job.setSentAt(LocalDateTime.now());
        });
        jobRepository.saveAll(jobs);
        
        log.info("Sent digest with {} notifications to {}:{}", 
            jobs.size(), firstJob.getRecipientType(), firstJob.getRecipientId());
    }
    
    private String buildDigestContent(List<NotificationJob> jobs) {
        StringBuilder sb = new StringBuilder("Your notification summary:\n\n");
        
        // Group by event type
        Map<NotificationEventType, Long> counts = jobs.stream()
            .collect(Collectors.groupingBy(
                job -> job.getEvent().getEventType(),
                Collectors.counting()
            ));
        
        counts.forEach((type, count) -> 
            sb.append("• ").append(count).append(" ").append(formatEventType(type)).append("\n")
        );
        
        return sb.toString();
    }
    
    private String formatEventType(NotificationEventType type) {
        // Convert SCREAMING_SNAKE_CASE to Title Case
        String[] words = type.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1));
            }
        }
        return result.toString();
    }
}

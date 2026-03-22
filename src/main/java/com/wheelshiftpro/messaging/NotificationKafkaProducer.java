package com.wheelshiftpro.messaging;

import com.wheelshiftpro.config.KafkaConfig;
import com.wheelshiftpro.entity.notifications.NotificationJob;
import com.wheelshiftpro.entity.notifications.NotificationTemplate;
import com.wheelshiftpro.repository.notifications.NotificationTemplateRepository;
import com.wheelshiftpro.service.notifications.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes persisted {@link NotificationJob}s to the appropriate Kafka topic.
 * The message includes a pre-rendered title+message so consumers don't need a DB lookup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaProducer {

    private final KafkaTemplate<String, NotificationJobMessage> kafkaTemplate;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateService templateService;

    /**
     * Publish a notification job to Kafka.
     * Runs on the {@code notificationExecutor} thread pool so the calling transaction
     * is already committed before the message is sent.
     */
    @Async("notificationExecutor")
    public void publishJob(NotificationJob job) {
        String topic = resolveTopic(job);
        NotificationJobMessage message = buildMessage(job);

        CompletableFuture<SendResult<String, NotificationJobMessage>> future =
                kafkaTemplate.send(topic, String.valueOf(job.getRecipientId()), message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish notification job {} to topic {}: {}",
                        job.getId(), topic, ex.getMessage());
            } else {
                log.debug("Published notification job {} to topic {} partition {} offset {}",
                        job.getId(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String resolveTopic(NotificationJob job) {
        return switch (job.getChannel()) {
            case EMAIL  -> KafkaConfig.TOPIC_EMAIL;
            default     -> KafkaConfig.TOPIC_INAPP;
        };
    }

    private NotificationJobMessage buildMessage(NotificationJob job) {
        var event = job.getEvent();

        String title   = event.getEventType().name();
        String message = "";

        try {
            Optional<NotificationTemplate> template = templateRepository
                    .findLatestByNameAndChannelAndLocale(
                            event.getEventType().getTemplateName(), job.getChannel(), "en");

            if (template.isPresent()) {
                title   = template.get().getSubject();
                message = templateService.renderTemplate(template.get().getContent(), event.getPayload());
            }
        } catch (Exception e) {
            log.warn("Template render failed for job {}: {}", job.getId(), e.getMessage());
        }

        return NotificationJobMessage.builder()
                .jobId(job.getId())
                .eventId(event.getId())
                .eventType(event.getEventType())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .recipientType(job.getRecipientType())
                .recipientId(job.getRecipientId())
                .channel(job.getChannel())
                .severity(event.getSeverity())
                .title(title)
                .message(message)
                .payload(event.getPayload())
                .occurredAt(event.getOccurredAt())
                .createdAt(job.getCreatedAt())
                .build();
    }
}

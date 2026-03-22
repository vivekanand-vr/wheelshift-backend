package com.wheelshiftpro.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheelshiftpro.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes IN_APP notification jobs from Kafka and fans them out via Redis Pub/Sub
 * so every app instance can push the event to its connected SSE clients.
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
            String channel = redisChannel(message);
            String payload = objectMapper.writeValueAsString(message);
            stringRedisTemplate.convertAndSend(channel, payload);
            log.debug("Published notification job {} to Redis channel {}", message.getJobId(), channel);
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
}

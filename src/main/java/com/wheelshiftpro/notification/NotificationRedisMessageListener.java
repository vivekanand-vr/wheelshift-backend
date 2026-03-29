package com.wheelshiftpro.notification;

import com.wheelshiftpro.enums.RecipientType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub listener.
 *
 * When a notification is published to {@code notification:{TYPE}:{id}} by {@link
 * com.wheelshiftpro.messaging.NotificationKafkaConsumer}, this listener picks it up on
 * ALL app instances and forwards the payload to any active SSE emitters for that recipient.
 *
 * Channel pattern subscription is configured in {@link com.wheelshiftpro.config.RedisConfig}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRedisMessageListener implements MessageListener {

    private final NotificationSseEmitterManager sseEmitterManager;

    /**
     * @param message  Redis message; channel bytes are e.g. {@code notification:EMPLOYEE:42}
     * @param pattern  subscribed pattern bytes (not used)
     */
    @Override
    public void onMessage(@Nonnull Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String payload = new String(message.getBody());

        log.debug("Redis pub/sub received on channel={}", channel);

        try {
            // channel format: notification:{RECIPIENT_TYPE}:{recipientId}
            String[] parts = channel.split(":");
            if (parts.length != 3) {
                log.warn("Unexpected Redis channel format: {}", channel);
                return;
            }
            RecipientType recipientType = RecipientType.valueOf(parts[1]);
            Long recipientId = Long.parseLong(parts[2]);

            sseEmitterManager.sendToRecipient(recipientType, recipientId, payload);
        } catch (Exception e) {
            log.error("Error processing Redis pub/sub message on channel {}: {}", channel, e.getMessage());
        }
    }
}

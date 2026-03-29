package com.wheelshiftpro.notification;

import com.wheelshiftpro.enums.RecipientType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages active SSE connections for all recipients.
 *
 * Key: {@code "{RECIPIENT_TYPE}:{recipientId}"}, e.g. {@code "EMPLOYEE:42"}
 * Value: list of active {@link SseEmitter}s for that recipient (multiple browser tabs supported)
 */
@Component
@Slf4j
public class NotificationSseEmitterManager {

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * Register a new SSE connection for a recipient.
     * Automatically removes the emitter when the connection times out, completes, or errors.
     */
    public SseEmitter addEmitter(RecipientType recipientType, Long recipientId) {
        String key = buildKey(recipientType, recipientId);

        SseEmitter emitter = new SseEmitter(0L); // no timeout; rely on client reconnect

        emitters.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.debug("SSE emitter registered for key={} (total={})", key, emitters.get(key).size());

        Runnable cleanup = () -> removeEmitter(key, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(t -> {
            log.debug("SSE emitter error for key={}: {}", key, t.getMessage());
            cleanup.run();
        });

        return emitter;
    }

    /**
     * Push a payload to all active SSE connections for the recipient.
     * Silently drops dead connections.
     */
    public void sendToRecipient(RecipientType recipientType, Long recipientId, String jsonPayload) {
        String key = buildKey(recipientType, recipientId);
        List<SseEmitter> recipientEmitters = emitters.get(key);
        if (recipientEmitters == null || recipientEmitters.isEmpty()) {
            return;
        }

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("notification")
                .data(jsonPayload);

        recipientEmitters.removeIf(emitter -> {
            try {
                emitter.send(event);
                return false;
            } catch (IOException e) {
                log.debug("Removing dead SSE emitter for key={}", key);
                return true; // remove broken emitter
            }
        });
    }

    /** @return number of active connections across all recipients */
    public int activeConnectionCount() {
        return emitters.values().stream().mapToInt(List::size).sum();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void removeEmitter(String key, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(key);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(key, list);
            }
            log.debug("SSE emitter removed for key={}", key);
        }
    }

    private String buildKey(RecipientType recipientType, Long recipientId) {
        return recipientType.name() + ":" + recipientId;
    }
}

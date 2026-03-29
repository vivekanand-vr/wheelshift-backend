package com.wheelshiftpro.config;

import com.wheelshiftpro.messaging.NotificationJobMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for async notification delivery.
 * Uses the official apache/kafka image (KRaft mode, no Zookeeper).
 *
 * Topics:
 *   notification.jobs.inapp  — IN_APP jobs
 *   notification.jobs.email  — EMAIL jobs (ready when email is implemented)
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    // ── Topic name constants ─────────────────────────────────────────────────
    public static final String TOPIC_INAPP  = "notification.jobs.inapp";
    public static final String TOPIC_EMAIL  = "notification.jobs.email";
    public static final String GROUP_INAPP  = "notification-inapp-consumer-group";
    public static final String GROUP_EMAIL  = "notification-email-consumer-group";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    // ── Topics ───────────────────────────────────────────────────────────────

    @Bean
    public NewTopic inappTopic() {
        return TopicBuilder.name(TOPIC_INAPP)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(TOPIC_EMAIL)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // ── Producer ─────────────────────────────────────────────────────────────

    @Bean
    public ProducerFactory<String, NotificationJobMessage> notificationProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.springframework.kafka.support.serializer.JsonSerializer");
        // Idempotent producer — prevents duplicate deliveries on retries
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, NotificationJobMessage> notificationKafkaTemplate() {
        return new KafkaTemplate<>(notificationProducerFactory());
    }

    // ── Consumer ─────────────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, NotificationJobMessage> notificationConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                "org.springframework.kafka.support.serializer.JsonDeserializer");
        config.put("spring.json.value.default.type",
                "com.wheelshiftpro.messaging.NotificationJobMessage");
        config.put("spring.json.trusted.packages", "com.wheelshiftpro.*");
        config.put("spring.json.use.type.headers", false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationJobMessage>
    notificationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationJobMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        // Manual acknowledgement — we ack only after successful Redis publish
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(2);
        return factory;
    }
}

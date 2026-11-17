package com.digitalfix.audit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Configuración del consumidor Kafka de auditoría. Kafka no tiene DLQ nativa;
 * Spring Kafka la simula: tras agotar los reintentos, los mensajes fallidos se
 * publican al topic {@code workorder.audit.DLT}.
 */
@Configuration
public class KafkaConfig {

    public static final String AUDIT_TOPIC = "workorder.audit";
    public static final String AUDIT_GROUP_ID = "ms-digitalfix-audit";

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public CommonErrorHandler auditErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    log.error("Mensaje {} enviado a DLQ tras agotar reintentos: {}",
                            record.key(), exception.getMessage());
                    return new org.apache.kafka.common.TopicPartition(
                            record.topic() + ".DLT", record.partition());
                });
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}

package com.digitalfix.report.config;

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
 * Configuración del consumidor Kafka de reportes/KPIs. Los mensajes fallidos,
 * tras agotar los reintentos, se publican al topic {@code workorder.kpi.DLT}.
 */
@Configuration
public class KafkaConfig {

    public static final String KPI_TOPIC = "workorder.kpi";
    public static final String KPI_GROUP_ID = "ms-digitalfix-report";

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public CommonErrorHandler kpiErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
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

package com.digitalfix.report.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    public static final String KPI_TOPIC = "workorder.kpi";
    public static final String KPI_GROUP_ID = "ms-digitalfix-report";

    @Bean
    public CommonErrorHandler commonErrorHandler() {
        Logger log = LoggerFactory.getLogger(KafkaConfig.class);
        return new DefaultErrorHandler(
                (record, exception) -> log.error(
                        "Error consumiendo registro de Kafka (offset {}): {}",
                        record.offset(), exception.getMessage(), exception),
                new FixedBackOff(1000L, 3L));
    }
}

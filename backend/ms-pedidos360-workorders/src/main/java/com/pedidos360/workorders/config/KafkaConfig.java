package com.pedidos360.workorders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String AUDIT_TOPIC = "workorder.audit";
    public static final String KPI_TOPIC = "workorder.kpi";

    @Bean
    public NewTopic workOrderAuditTopic() {
        return TopicBuilder.name(AUDIT_TOPIC).partitions(3).replicas(3).build();
    }

    @Bean
    public NewTopic workOrderKpiTopic() {
        return TopicBuilder.name(KPI_TOPIC).partitions(3).replicas(3).build();
    }
}

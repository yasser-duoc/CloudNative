package com.digitalfix.workorders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declaración programática de los topics de Kafka con su configuración
 * avanzada ajustada a cada flujo de datos.
 *
 * <ul>
 *   <li><b>workorder.audit</b> (auditoría/event sourcing): retención larga
 *       (7 días) porque el historial debe poder reprocesarse (replay).</li>
 *   <li><b>workorder.kpi</b> (analítica): retención corta (1 día), los KPIs
 *       solo necesitan el flujo reciente.</li>
 * </ul>
 *
 * Ambos usan cleanup.policy=delete (purga por tiempo) y 3 particiones para
 * permitir hasta 3 consumidores en paralelo por grupo.
 */
@Configuration
public class KafkaConfig {

    public static final String AUDIT_TOPIC = "workorder.audit";
    public static final String KPI_TOPIC = "workorder.kpi";

    public static final String AUDIT_RETENTION_MS = "604800000";
    public static final String KPI_RETENTION_MS = "86400000";

    @Bean
    public NewTopic workOrderAuditTopic() {
        return TopicBuilder.name(AUDIT_TOPIC)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, AUDIT_RETENTION_MS)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    @Bean
    public NewTopic workOrderKpiTopic() {
        return TopicBuilder.name(KPI_TOPIC)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, KPI_RETENTION_MS)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }
}

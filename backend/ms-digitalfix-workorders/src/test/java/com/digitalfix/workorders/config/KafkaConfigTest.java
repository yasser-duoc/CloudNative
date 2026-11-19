package com.digitalfix.workorders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica la configuración avanzada de los topics: particiones, factor de
 * replicación, retención y política de limpieza por flujo.
 */
class KafkaConfigTest {

    private final KafkaConfig config = new KafkaConfig();

    @Test
    void auditTopicKeepsLongRetentionForReplay() {
        NewTopic topic = config.workOrderAuditTopic();

        assertThat(topic.name()).isEqualTo("workorder.audit");
        assertThat(topic.numPartitions()).isEqualTo(3);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
        assertThat(topic.configs())
                .containsEntry(TopicConfig.RETENTION_MS_CONFIG, KafkaConfig.AUDIT_RETENTION_MS)
                .containsEntry(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);
        assertThat(Long.parseLong(KafkaConfig.AUDIT_RETENTION_MS))
                .isGreaterThan(Long.parseLong(KafkaConfig.KPI_RETENTION_MS));
    }

    @Test
    void kpiTopicUsesShortRetention() {
        NewTopic topic = config.workOrderKpiTopic();

        assertThat(topic.name()).isEqualTo("workorder.kpi");
        assertThat(topic.numPartitions()).isEqualTo(3);
        assertThat(topic.configs())
                .containsEntry(TopicConfig.RETENTION_MS_CONFIG, KafkaConfig.KPI_RETENTION_MS)
                .containsEntry(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);
    }
}

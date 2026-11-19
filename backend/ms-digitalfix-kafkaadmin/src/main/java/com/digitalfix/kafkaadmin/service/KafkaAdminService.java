package com.digitalfix.kafkaadmin.service;

import com.digitalfix.kafkaadmin.dto.ConsumerGroupResponse;
import com.digitalfix.kafkaadmin.dto.TopicRequest;
import com.digitalfix.kafkaadmin.dto.TopicResponse;
import com.digitalfix.kafkaadmin.dto.TopicUpdateRequest;
import com.digitalfix.kafkaadmin.exception.ConflictException;
import com.digitalfix.kafkaadmin.exception.InvalidResourceException;
import com.digitalfix.kafkaadmin.exception.ResourceNotFoundException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Encapsula toda la administración de Kafka (topics, configuraciones y
 * consumer groups) sobre {@link AdminClient}. Los controladores solo invocan
 * esta API de alto nivel y no conocen detalles del broker.
 */
@Service
public class KafkaAdminService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);

    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    // ---------- Topics ----------

    public List<String> listTopics() {
        return await(adminClient.listTopics().names()).stream().sorted().toList();
    }

    public TopicResponse createTopic(TopicRequest request) {
        validateTopicName(request.name());
        if (request.partitions() < 1) {
            throw new InvalidResourceException("El número de particiones debe ser al menos 1");
        }
        if (request.replicas() < 1) {
            throw new InvalidResourceException("El factor de replicación debe ser al menos 1");
        }
        if (topicExists(request.name())) {
            throw new ConflictException("El topic '" + request.name() + "' ya existe");
        }

        Map<String, String> configs = new HashMap<>();
        if (request.retentionMs() != null) {
            configs.put(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(request.retentionMs()));
        }
        if (request.cleanupPolicy() != null) {
            configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, request.cleanupPolicy());
        }

        NewTopic topic = new NewTopic(request.name(), request.partitions(), request.replicas())
                .configs(configs);
        await(adminClient.createTopics(List.of(topic)).all());
        log.info("Topic '{}' creado (particiones={}, replicas={}, configs={})",
                request.name(), request.partitions(), request.replicas(), configs);
        return describeTopic(request.name());
    }

    public TopicResponse describeTopic(String name) {
        validateTopicName(name);
        if (!topicExists(name)) {
            throw new ResourceNotFoundException("El topic '" + name + "' no existe");
        }

        TopicDescription description = await(adminClient.describeTopics(List.of(name)).allTopicNames())
                .get(name);
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
        Config config = await(adminClient.describeConfigs(List.of(resource)).all()).get(resource);

        Map<String, String> configs = new TreeMap<>();
        config.entries().forEach(entry -> {
            if (TopicConfig.RETENTION_MS_CONFIG.equals(entry.name())
                    || TopicConfig.CLEANUP_POLICY_CONFIG.equals(entry.name())) {
                configs.put(entry.name(), entry.value());
            }
        });

        int replicas = description.partitions().isEmpty()
                ? 0 : description.partitions().get(0).replicas().size();

        return new TopicResponse(name, description.partitions().size(), replicas, configs);
    }

    public TopicResponse updateTopic(String name, TopicUpdateRequest request) {
        TopicResponse current = describeTopic(name);

        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
        List<AlterConfigOp> ops = new ArrayList<>();
        if (request.retentionMs() != null) {
            ops.add(new AlterConfigOp(new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG,
                    String.valueOf(request.retentionMs())), AlterConfigOp.OpType.SET));
        }
        if (request.cleanupPolicy() != null) {
            ops.add(new AlterConfigOp(new ConfigEntry(TopicConfig.CLEANUP_POLICY_CONFIG,
                    request.cleanupPolicy()), AlterConfigOp.OpType.SET));
        }
        if (!ops.isEmpty()) {
            await(adminClient.incrementalAlterConfigs(Map.of(resource, ops)).all());
        }

        if (request.partitions() != null) {
            if (request.partitions() < current.partitions()) {
                throw new InvalidResourceException(
                        "El número de particiones solo puede aumentarse (actual: " + current.partitions() + ")");
            }
            if (request.partitions() > current.partitions()) {
                await(adminClient.createPartitions(Map.of(name, NewPartitions.increaseTo(request.partitions()))).all());
            }
        }

        log.info("Topic '{}' actualizado (retentionMs={}, cleanupPolicy={}, particiones={})",
                name, request.retentionMs(), request.cleanupPolicy(), request.partitions());
        return describeTopic(name);
    }

    public void deleteTopic(String name) {
        if (!topicExists(name)) {
            throw new ResourceNotFoundException("El topic '" + name + "' no existe");
        }
        await(adminClient.deleteTopics(List.of(name)).all());
        log.info("Topic '{}' eliminado", name);
    }

    private boolean topicExists(String name) {
        return await(adminClient.listTopics().names()).contains(name);
    }

    // ---------- Consumer groups ----------

    public List<String> listConsumerGroups() {
        return await(adminClient.listConsumerGroups().all()).stream()
                .map(ConsumerGroupListing::groupId)
                .sorted()
                .toList();
    }

    public ConsumerGroupResponse describeConsumerGroup(String groupId) {
        if (!consumerGroupExists(groupId)) {
            throw new ResourceNotFoundException("El consumer group '" + groupId + "' no existe");
        }

        ConsumerGroupDescription description = await(
                adminClient.describeConsumerGroups(List.of(groupId)).all()).get(groupId);

        List<ConsumerGroupResponse.Member> members = description.members().stream()
                .map(member -> new ConsumerGroupResponse.Member(
                        member.consumerId(), member.clientId(), member.host()))
                .toList();

        return new ConsumerGroupResponse(groupId, description.state().toString(), members);
    }

    public void deleteConsumerGroup(String groupId) {
        if (!consumerGroupExists(groupId)) {
            throw new ResourceNotFoundException("El consumer group '" + groupId + "' no existe");
        }
        await(adminClient.deleteConsumerGroups(List.of(groupId)).all());
        log.info("Consumer group '{}' eliminado", groupId);
    }

    private boolean consumerGroupExists(String groupId) {
        Set<String> groupIds = await(adminClient.listConsumerGroups().all()).stream()
                .map(ConsumerGroupListing::groupId)
                .collect(java.util.stream.Collectors.toSet());
        return groupIds.contains(groupId);
    }

    // ---------- Helpers ----------

    private void validateTopicName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidResourceException("El nombre del topic es obligatorio");
        }
        if (!name.matches("^[a-zA-Z0-9._\\-]{1,200}$")) {
            throw new InvalidResourceException(
                    "El nombre del topic solo puede contener letras, números, '_', '-', y '.'");
        }
        if (name.startsWith("amq.") || name.equals("__consumer_offsets")) {
            throw new InvalidResourceException(
                    "El nombre '" + name + "' está reservado por Kafka y no puede administrarse");
        }
    }

    private <T> T await(org.apache.kafka.common.KafkaFuture<T> future) {
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidResourceException("Operación interrumpida");
        } catch (ExecutionException | TimeoutException e) {
            throw new InvalidResourceException("Kafka no disponible: " + e.getMessage());
        }
    }
}

package com.digitalfix.kafkaadmin.controller;

import com.digitalfix.kafkaadmin.dto.ConsumerGroupResponse;
import com.digitalfix.kafkaadmin.dto.TopicRequest;
import com.digitalfix.kafkaadmin.dto.TopicResponse;
import com.digitalfix.kafkaadmin.dto.TopicUpdateRequest;
import com.digitalfix.kafkaadmin.service.KafkaAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST del administrador de Kafka: topics (crear, listar, inspeccionar,
 * actualizar y eliminar), configuraciones (retención/cleanup) y consumer groups.
 */
@RestController
@RequestMapping("/api/kafka")
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;

    public KafkaAdminController(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    // ---------- Topics ----------

    @GetMapping("/topics")
    public ResponseEntity<List<String>> listTopics() {
        return ResponseEntity.ok(kafkaAdminService.listTopics());
    }

    @PostMapping("/topics")
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kafkaAdminService.createTopic(request));
    }

    @GetMapping("/topics/{name}")
    public ResponseEntity<TopicResponse> describeTopic(@PathVariable String name) {
        return ResponseEntity.ok(kafkaAdminService.describeTopic(name));
    }

    @PatchMapping("/topics/{name}")
    public ResponseEntity<TopicResponse> updateTopic(@PathVariable String name,
                                                     @Valid @RequestBody TopicUpdateRequest request) {
        return ResponseEntity.ok(kafkaAdminService.updateTopic(name, request));
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<Void> deleteTopic(@PathVariable String name) {
        kafkaAdminService.deleteTopic(name);
        return ResponseEntity.noContent().build();
    }

    // ---------- Consumer groups ----------

    @GetMapping("/consumer-groups")
    public ResponseEntity<List<String>> listConsumerGroups() {
        return ResponseEntity.ok(kafkaAdminService.listConsumerGroups());
    }

    @GetMapping("/consumer-groups/{groupId}")
    public ResponseEntity<ConsumerGroupResponse> describeConsumerGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(kafkaAdminService.describeConsumerGroup(groupId));
    }

    @DeleteMapping("/consumer-groups/{groupId}")
    public ResponseEntity<Void> deleteConsumerGroup(@PathVariable String groupId) {
        kafkaAdminService.deleteConsumerGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}

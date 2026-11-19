package com.digitalfix.kafkaadmin.dto;

import java.util.Map;

public record TopicResponse(
        String name,
        int partitions,
        int replicas,
        Map<String, String> configs) {
}

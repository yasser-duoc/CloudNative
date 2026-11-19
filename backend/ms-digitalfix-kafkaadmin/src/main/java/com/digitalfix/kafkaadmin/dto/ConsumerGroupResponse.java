package com.digitalfix.kafkaadmin.dto;

import java.util.List;

public record ConsumerGroupResponse(
        String groupId,
        String state,
        List<Member> members) {

    public record Member(
            String consumerId,
            String clientId,
            String host) {
    }
}

package com.digitalfix.rabbitadmin.dto;

public record QueueInfoResponse(
        String name,
        int messageCount,
        int consumerCount) {
}

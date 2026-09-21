package com.pedidos360.notify.domain;

import java.time.Instant;

public record WorkOrderEvent(
        String eventId,
        String eventType,
        Long workOrderId,
        String status,
        String customerName,
        String customerEmail,
        String actor,
        Instant timestamp) {
}

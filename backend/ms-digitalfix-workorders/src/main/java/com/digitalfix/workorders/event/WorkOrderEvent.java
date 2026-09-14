package com.digitalfix.workorders.event;

import com.digitalfix.workorders.domain.WorkOrderStatus;

import java.time.Instant;

public record WorkOrderEvent(
        String eventId,
        String eventType,
        Long workOrderId,
        WorkOrderStatus status,
        String customerName,
        String customerEmail,
        String actor,
        Instant timestamp) {

    public static final String TYPE_CREATED = "WORKORDER_CREATED";
    public static final String TYPE_STATUS_CHANGED = "WORKORDER_STATUS_CHANGED";
    public static final String TYPE_COMPLETED = "WORKORDER_COMPLETED";
}

package com.digitalfix.workorders.event;

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

    public static final String TYPE_CREATED = "WORK_ORDER_CREATED";
    public static final String TYPE_STATUS_CHANGED = "WORK_ORDER_STATUS_CHANGED";
    public static final String TYPE_COMPLETED = "WORK_ORDER_CLOSED";
}

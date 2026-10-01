package com.digitalfix.workorders.audit;

import java.time.Instant;

public record WorkOrderEvent(String eventId, String eventType, Long workOrderId, String status,
                             String customerName, String customerEmail, String actor, Instant timestamp) {
}

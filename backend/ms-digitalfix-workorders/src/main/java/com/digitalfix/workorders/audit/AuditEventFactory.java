package com.digitalfix.workorders.audit;

import com.digitalfix.workorders.domain.WorkOrder;

import java.time.Instant;
import java.util.UUID;

public final class AuditEventFactory {
    private AuditEventFactory() {
    }

    public static WorkOrderEvent create(String type, WorkOrder order, String actor) {
        return new WorkOrderEvent(UUID.randomUUID().toString(), type, order.getId(),
                order.getStatus().name(), order.getCustomerName(), null, actor, Instant.now());
    }
}

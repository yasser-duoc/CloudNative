package com.digitalfix.workorders.audit;

public interface AuditEventClient {
    void record(WorkOrderEvent event, String bearerToken);
}

package com.digitalfix.audit.listener;

import com.digitalfix.audit.domain.WorkOrderEvent;
import com.digitalfix.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditEventListener listener;

    @Test
    void consumesEventAndRecordsIt() {
        WorkOrderEvent event = new WorkOrderEvent(
                "evt-1", "WORK_ORDER_CREATED", 10L, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());

        listener.onEvent(event);

        verify(auditService).record(any(WorkOrderEvent.class));
    }
}

package com.digitalfix.audit.listener;

import com.digitalfix.audit.domain.WorkOrderEvent;
import com.digitalfix.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditEventListener listener;

    private WorkOrderEvent event(String eventId, Long workOrderId) {
        return new WorkOrderEvent(
                eventId, "WORK_ORDER_CREATED", workOrderId, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());
    }

    @Test
    void consumesValidEventAndRecordsIt() {
        listener.onEvent(event("evt-1", 10L), 0, 42L);

        verify(auditService).record(any(WorkOrderEvent.class));
    }

    @Test
    void rejectsEventWithoutEventIdSoItGoesToDlqAfterRetries() {
        assertThatThrownBy(() -> listener.onEvent(event(null, 10L), 0, 43L))
                .isInstanceOf(InvalidEventException.class);

        verify(auditService, never()).record(any());
    }

    @Test
    void rejectsEventWithoutWorkOrderId() {
        assertThatThrownBy(() -> listener.onEvent(event("evt-2", null), 1, 44L))
                .isInstanceOf(InvalidEventException.class);

        verify(auditService, never()).record(any());
    }
}

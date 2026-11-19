package com.digitalfix.report.listener;

import com.digitalfix.report.domain.WorkOrderEvent;
import com.digitalfix.report.service.ReportService;
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
class KpiListenerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private KpiListener listener;

    private WorkOrderEvent event(String eventId, Long workOrderId) {
        return new WorkOrderEvent(
                eventId, "WORK_ORDER_CREATED", workOrderId, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());
    }

    @Test
    void consumesValidEventAndRecordsKpi() {
        listener.onEvent(event("evt-1", 10L), 0, 7L);

        verify(reportService).record(any(WorkOrderEvent.class));
    }

    @Test
    void rejectsEventWithoutEventIdSoItGoesToDlqAfterRetries() {
        assertThatThrownBy(() -> listener.onEvent(event("  ", 10L), 1, 8L))
                .isInstanceOf(InvalidEventException.class);

        verify(reportService, never()).record(any());
    }
}

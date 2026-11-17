package com.digitalfix.report.listener;

import com.digitalfix.report.domain.WorkOrderEvent;
import com.digitalfix.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KpiListenerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private KpiListener listener;

    @Test
    void consumesEventAndRecordsKpi() {
        WorkOrderEvent event = new WorkOrderEvent(
                "evt-1", "WORK_ORDER_CREATED", 10L, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());

        listener.onEvent(event);

        verify(reportService).record(any(WorkOrderEvent.class));
    }
}

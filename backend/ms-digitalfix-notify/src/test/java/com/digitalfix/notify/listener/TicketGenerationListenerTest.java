package com.digitalfix.notify.listener;

import com.digitalfix.notify.domain.WorkOrderEvent;
import com.digitalfix.notify.service.TicketGenerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketGenerationListenerTest {

    @Mock
    private TicketGenerationService ticketGenerationService;

    @InjectMocks
    private TicketGenerationListener listener;

    @Test
    void consumesCreateEventAndGeneratesTicket() {
        WorkOrderEvent event = new WorkOrderEvent(
                "evt-2", "WORK_ORDER_CREATED", 10L, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());

        listener.onWorkOrderCreated(event);

        verify(ticketGenerationService).generate(any(WorkOrderEvent.class));
    }
}

package com.digitalfix.notify.listener;

import com.digitalfix.notify.domain.WorkOrderEvent;
import com.digitalfix.notify.service.TicketGenerationService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketGenerationListenerTest {

    @Mock
    private TicketGenerationService ticketGenerationService;

    @Mock
    private Channel channel;

    @InjectMocks
    private TicketGenerationListener listener;

    @Test
    void acksMessageWhenTicketIsGenerated() throws IOException {
        WorkOrderEvent event = new WorkOrderEvent(
                "evt-2", "WORK_ORDER_CREATED", 10L, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());

        listener.onWorkOrderCreated(event, channel, 3L);

        verify(ticketGenerationService).generate(any(WorkOrderEvent.class));
        verify(channel).basicAck(3L, false);
    }

    @Test
    void nacksToDlqWhenMessageIsInvalid() throws IOException {
        WorkOrderEvent invalid = new WorkOrderEvent(
                "evt-3", "WORK_ORDER_CREATED", null, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());

        listener.onWorkOrderCreated(invalid, channel, 4L);

        verify(channel).basicNack(4L, false, false);
        verify(channel, never()).basicAck(4L, false);
        verify(ticketGenerationService, never()).generate(any());
    }
}

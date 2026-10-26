package com.digitalfix.notify.service;

import com.digitalfix.notify.domain.Ticket;
import com.digitalfix.notify.domain.WorkOrderEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TicketGenerationServiceTest {

    private final TicketGenerationService service = new TicketGenerationService();

    @Test
    void generatesTicketWithNumberAndOrderData() {
        WorkOrderEvent event = new WorkOrderEvent(
                "evt-3", "WORK_ORDER_CREATED", 42L, "CREADA",
                "Cliente Demo", "cliente@demo.cl", "tester@digitalfix.cl", Instant.now());

        Ticket ticket = service.generate(event);

        assertThat(ticket.ticketNumber()).startsWith("TKT-");
        assertThat(ticket.ticketNumber()).hasSize(12);
        assertThat(ticket.workOrderId()).isEqualTo(42L);
        assertThat(ticket.customerName()).isEqualTo("Cliente Demo");
        assertThat(ticket.generatedBy()).isEqualTo("tester@digitalfix.cl");
        assertThat(ticket.generatedAt()).isNotNull();
    }
}

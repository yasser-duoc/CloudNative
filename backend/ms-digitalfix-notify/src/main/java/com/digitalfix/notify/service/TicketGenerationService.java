package com.digitalfix.notify.service;

import com.digitalfix.notify.domain.Ticket;
import com.digitalfix.notify.domain.WorkOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Tarea asíncrona: generación de tickets/documentos a partir del evento
 * de creación de una orden de trabajo. Se ejecuta de forma desacoplada
 * mediante RabbitMQ, sin afectar la lógica del microservicio productor.
 */
@Service
public class TicketGenerationService {

    private static final Logger log = LoggerFactory.getLogger(TicketGenerationService.class);

    public Ticket generate(WorkOrderEvent event) {
        String ticketNumber = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Ticket ticket = new Ticket(
                ticketNumber,
                event.workOrderId(),
                event.customerName(),
                event.actor(),
                Instant.now());

        log.info("[TICKET] Ticket {} generado para la orden {} (cliente '{}')",
                ticket.ticketNumber(), ticket.workOrderId(), ticket.customerName());
        return ticket;
    }
}
